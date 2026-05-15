package io.github.genkidoudou.common.firewall.client.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SM4;
import io.github.genkidoudou.common.firewall.client.ClientService;
import io.github.genkidoudou.common.firewall.client.exception.ClientAuthException;
import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.firewall.client.ClientProperties;
import io.github.genkidoudou.common.firewall.client.OauthClient;
import io.github.genkidoudou.common.firewall.password.DelegatingPasswordEncoder;
import io.github.genkidoudou.common.firewall.password.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置文件客户端服务实现
 * 默认实现，从配置文件读取客户端信息
 *
 * @author luyanan
 * @since 2026-03-04
 */
@RequiredArgsConstructor
@Slf4j
public class ConfigClientServiceImpl implements ClientService {

    private final ClientProperties clientProperties;


    private final DelegatingPasswordEncoder delegatingPasswordEncoder;

    /**
     * 客户端缓存
     */
    private Map<String, OauthClient> clientCache;


    @PostConstruct
    public void init() {
        log.info("初始化配置文件客户端服务");
        loadClients();
    }

    /**
     * 从配置文件加载客户端信息
     */
    private void loadClients() {
        List<OauthClient> clients = clientProperties.getClients();

        if (clients == null || clients.isEmpty()) {
            log.warn("未配置任何客户端信息");
            clientCache = new HashMap<>();
            return;
        }

        // 构建缓存
        clientCache = clients.stream()
                .collect(Collectors.toMap(
                        OauthClient::getClientId,
                        client -> client,
                        (old, newClient) -> {
                            log.warn("发现重复的客户端ID: {}", old.getClientId());
                            return newClient;
                        }
                ));

        log.info("成功加载 {} 个客户端", clientCache.size());
    }

    @Override
    public OauthClient getClientById(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            return null;
        }
        return clientCache.get(clientId);
    }

    @Override
    public boolean validateClient(String clientId, String clientSecret) {
        OauthClient client = getClientById(clientId);

        if (client == null) {
            log.debug("客户端不存在: {}", clientId);
            throw ClientAuthException.clientNotFound(clientId);
        }

        if (!Boolean.TRUE.equals(client.getEnabled())) {
            log.debug("客户端已禁用: {}", clientId);
            throw ClientAuthException.clientDisabled(clientId);
        }

        // 检查是否过期
        if (client.getExpireTime() != null
                && client.getExpireTime().before(new Date())) {
            log.debug("客户端已过期: {}", clientId);
            throw ClientAuthException.clientExpired(clientId);
        }

        // 验证密钥
        if (!client.getClientSecret().equals(clientSecret)) {
            log.debug("客户端密钥错误: {}", clientId);
            throw ClientAuthException.clientSecretInvalid(clientId);
        }

        return true;
    }

    @Override
    public List<OauthClient> getAllEnabledClients() {
        return clientCache.values().stream()
                .filter(client -> Boolean.TRUE.equals(client.getEnabled()))
                .filter(client -> client.getExpireTime() == null
                        || client.getExpireTime().after(new Date()))
                .collect(Collectors.toList());
    }

    @Override
    public OauthClient parserClientId(String clientIdStr) {
        if (StrUtil.isBlank(clientIdStr)) {
            throw new ClientAuthException(ErrorCode.CLIENT_NOT_FOUND);
        }

        String s = Base64.decodeStr(clientIdStr);
        if (s.split("\\|").length != 2) {
            throw new ClientAuthException(ErrorCode.CLIENT_NOT_FOUND);
        }
        String clientId = s.split("\\|")[0];
        // 根据客户端id 获取密钥
        OauthClient oauthClient = this.getClientById(clientId);
        if (null == oauthClient) {
            throw new ClientAuthException(ErrorCode.CLIENT_NOT_FOUND);
        }
        String s1 = authClientIdEncrypt(oauthClient);
        if (!s1.equals(clientIdStr)) {
            throw new ClientAuthException(ErrorCode.CLIENT_NOT_FOUND);
        }
        return oauthClient;
    }


    /**
     * 客户端加密
     *
     * @param oauthClient 客户端信息
     * @return
     * @since 2026/3/8
     */
    @Override
    public String authClientIdEncrypt(OauthClient oauthClient) {
        String clientId = oauthClient.getClientId();
        String key = StrUtil.fillAfter(oauthClient.getClientSecret(), '0', 16).substring(0, 16);
        PasswordEncoder encoderPasswordEncoder = delegatingPasswordEncoder.getPasswordEncoder("sm4");
        Properties properties = new Properties();
        properties.put("key", key);
        encoderPasswordEncoder.setProperties(properties);
        String content = clientId + "|" + oauthClient.getClientSecret();
        String encrypt = encoderPasswordEncoder.encrypt(content);
        return Base64.encode(clientId + "|" + encrypt);
    }

    public static void main(String[] args) {
        String clientSecret = StrUtil.fillAfter("cs", '0', 16).substring(0, 16);

        SM4 sm4 = SmUtil.sm4(clientSecret.getBytes(StandardCharsets.UTF_8));
        String s = sm4.encryptHex("cs|cs");
        System.out.println(Base64.encode("cs|" + s));
    }

}
