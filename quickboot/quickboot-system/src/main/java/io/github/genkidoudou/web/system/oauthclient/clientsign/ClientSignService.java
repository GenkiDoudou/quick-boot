package io.github.genkidoudou.web.system.oauthclient.clientsign;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.system.oauthclient.service.OauthClientApiPathAuthService;
import io.github.genkidoudou.web.system.oauthclient.service.OauthClientSignVerifySupport;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import io.github.genkidoudou.web.system.oauthclient.support.Oauth2SecretSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

/**
 * OAuth2 Client HMAC 签名校验服务。
 * <p>
 * 与前端 {@code quick-ui/src/utils/clientSign.js} 对齐：
 * 请求头 X-Client-Id / X-Client-Timestamp / X-Client-Nonce / X-Client-Signature，
 * canonical 串经 HMAC-SHA256 + Base64 后与签名比对。
 * </p>
 * <p>
 * multipart 上传与前端 FormData 约定一致：body 参与哈希时使用空字节数组。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ClientSignService {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final ClientSignProperties properties;
    private final SysOauthClientMapper oauthClientMapper;
    private final PasswordCodec passwordCodec;
    private final CacheManager cacheManager;
    private final OauthClientApiPathAuthService apiPathAuthService;

    /**
     * 校验请求签名；失败抛 {@link WarningException}。
     *
     * @param request 原始请求，或已缓存 body 的 {@link CachedBodyHttpServletRequest}
     */
    public void verify(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return;
        }
        String servletPath = ClientRequestPathSupport.resolveServletPath(request);
        if (isExcluded(servletPath) || HttpMethod.OPTIONS.matches(request.getMethod())) {
            return;
        }

        String clientId = header(request, "X-Client-Id");
        if (StrUtil.isNotBlank(clientId)) {
            SysOauthClient early = oauthClientMapper.selectById(clientId.trim());
            if (early != null && "0".equals(early.getDelFlag()) && "0".equals(early.getStatus())
                    && !OauthClientSignVerifySupport.isSignVerifyEnabled(early)) {
                return;
            }
        }

        String timestamp = header(request, "X-Client-Timestamp");
        String nonce = header(request, "X-Client-Nonce");
        String signature = header(request, "X-Client-Signature");
        if (StrUtil.hasBlank(clientId, timestamp, nonce, signature)) {
            throw fail("缺少 Client 签名请求头");
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException ex) {
            throw fail("时间戳格式无效");
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > properties.getWindowSeconds()) {
            throw fail("请求已过期");
        }

        assertNonceFresh(clientId.trim(), nonce.trim());

        SysOauthClient client = oauthClientMapper.selectById(clientId.trim());
        if (client == null || !"0".equals(client.getDelFlag())) {
            throw fail("无效的客户端");
        }
        if (!"0".equals(client.getStatus())) {
            throw forbidden("客户端已停用");
        }

        String secret = Oauth2SecretSupport.resolvePlainSecret(passwordCodec, client.getClientSecret());
        if (StrUtil.isBlank(secret)) {
            throw fail("客户端密钥未配置");
        }

        String canonical = buildCanonical(
                request.getMethod(),
                servletPath,
                bodyBytesForSignature(request),
                timestamp.trim(),
                nonce.trim(),
                clientId.trim());
        String expected = hmacSha256Base64(secret, canonical);
        if (!constantTimeEquals(expected, signature.trim())) {
            throw fail("签名校验失败");
        }

        apiPathAuthService.assertPathAllowed(client, servletPath);
    }

    private void assertNonceFresh(String clientId, String nonce) {
        String cacheName = properties.getNonceCacheName();
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "nonce 缓存未配置: " + cacheName);
        }
        String key = clientId + ":" + nonce;
        Cache.ValueWrapper existing = cache.putIfAbsent(key, Boolean.TRUE);
        if (existing != null) {
            throw fail("重复请求（nonce 已使用）");
        }
    }

    /**
     * 是否为 multipart 请求（与前端 FormData 上传一致）。
     *
     * @param contentType {@link HttpServletRequest#getContentType()}
     * @return true 表示 multipart
     */
    public static boolean isMultipartContentType(String contentType) {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    /**
     * 参与签名的 body 字节：{@code multipart/*} 与前端 {@code FormData} 约定一致，使用空 body 哈希。
     */
    private static byte[] bodyBytesForSignature(HttpServletRequest request) {
        if (isMultipartContentType(request.getContentType())) {
            return new byte[0];
        }
        if (request instanceof CachedBodyHttpServletRequest cached) {
            return cached.getCachedBody();
        }
        return new byte[0];
    }

    /**
     * 构建规范化串（与前端 clientSign.js 一致）。
     * <p>
     * 格式（字段以 {@code \\n} 分隔）：
     * {@code METHOD\\nPATH\\nSHA256(bodyHex)\\nTIMESTAMP\\nNONCE\\nCLIENT_ID}
     * </p>
     * <ul>
     *   <li>METHOD：大写 HTTP 方法</li>
     *   <li>PATH：servletPath（不含 context-path）</li>
     *   <li>bodyHex：body 的 SHA-256 十六进制；multipart 为空 body</li>
     * </ul>
     *
     * @param method    HTTP 方法
     * @param path      servlet 路径
     * @param body      原始 body 字节
     * @param timestamp 秒级时间戳（与请求头一致）
     * @param nonce     随机串
     * @param clientId  客户端 ID
     * @return 待 HMAC 的 canonical 字符串
     */
    public static String buildCanonical(String method, String path, byte[] body,
                                        String timestamp, String nonce, String clientId) {
        String upperMethod = method == null ? "" : method.toUpperCase(Locale.ROOT);
        String bodyHash = sha256Hex(body == null ? new byte[0] : body);
        return upperMethod + '\n'
                + path + '\n'
                + bodyHash + '\n'
                + timestamp + '\n'
                + nonce + '\n'
                + clientId;
    }

    private static String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "计算 body 摘要失败");
        }
    }

    /**
     * HMAC-SHA256 签名并 Base64 编码（与前端 crypto-js 输出一致）。
     */
    private static String hmacSha256Base64(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception ex) {
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "计算签名失败");
        }
    }

    /** 恒定时间比较，降低时序攻击风险。 */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(x, y);
    }

    private boolean isExcluded(String servletPath) {
        for (String pattern : properties.getExcludePaths()) {
            if (PATH_MATCHER.match(pattern, servletPath)) {
                return true;
            }
        }
        return false;
    }

    private static String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    private static WarningException fail(String msg) {
        return new WarningException(ErrorCodes.Security.CLIENT_SIGN_INVALID, msg);
    }

    private static WarningException forbidden(String msg) {
        return new WarningException(ErrorCodes.Security.FORBIDDEN, msg);
    }
}
