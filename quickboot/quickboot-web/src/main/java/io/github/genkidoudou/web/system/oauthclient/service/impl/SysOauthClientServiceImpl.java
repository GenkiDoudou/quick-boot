package io.github.genkidoudou.web.system.oauthclient.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.auth.AuthLoginService;
import io.github.genkidoudou.web.auth.oauth2.support.Oauth2SecretSupport;
import io.github.genkidoudou.web.auth.oauth2.support.OauthClientApiPathAuthService;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientBo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientCredentialsVo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientVo;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import io.github.genkidoudou.web.system.oauthclient.service.SysOauthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link SysOauthClientService} 实现。
 */
@Service
@RequiredArgsConstructor
public class SysOauthClientServiceImpl implements SysOauthClientService {

    private final SysOauthClientMapper mapper;
    private final PasswordCodec passwordCodec;
    private final AuthLoginService authLoginService;
    private final OauthClientApiPathAuthService apiPathAuthService;

    @Override
    public List<SysOauthClient> list(String clientName) {
        return mapper.selectList(Wrappers.<SysOauthClient>lambdaQuery()
                .like(StrUtil.isNotBlank(clientName), SysOauthClient::getClientName, clientName)
                .orderByDesc(SysOauthClient::getCreateTime));
    }

    @Override
    public SysOauthClient getById(String clientId) {
        return mapper.selectById(clientId);
    }

    @Override
    public SysOauthClientVo getDetailVo(String clientId) {
        SysOauthClient row = mapper.selectById(clientId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "客户端不存在");
        }
        return BeanUtil.copyProperties(row, SysOauthClientVo.class);
    }

    @Override
    public SysOauthClientCredentialsVo revealCredentials(String clientId, String currentPassword) {
        authLoginService.verifyCurrentUserPassword(currentPassword);
        SysOauthClient row = mapper.selectById(clientId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "客户端不存在");
        }
        SysOauthClientCredentialsVo vo = new SysOauthClientCredentialsVo();
        vo.setClientId(row.getClientId());
        vo.setClientSecret(Oauth2SecretSupport.resolvePlainSecret(passwordCodec, row.getClientSecret()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysOauthClientBo req) {
        if (mapper.selectById(req.getClientId()) != null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_ALREADY_IMPORTED, "client_id 已存在");
        }
        assertRedirectWhenRequired(req.getGrantTypes(), req.getRedirectUris());
        normalizeAndValidateClient(req);
        SysOauthClient row = BeanUtil.copyProperties(req, SysOauthClient.class);
        row.setRedirectUris(normalizeRedirectUris(req.getRedirectUris()));
        row.setScopes(OauthClientApiPathAuthService.DEFAULT_OAUTH_SCOPES);
        row.setApiPathPatterns(normalizeApiPathPatterns(req.getApiPathPatterns()));
        row.setSignVerify(normalizeSignVerify(req.getSignVerify()));
        row.setClientSecret(Oauth2SecretSupport.encodeForStorage(passwordCodec, req.getClientSecret()));
        mapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysOauthClientBo req) {
        SysOauthClient existing = mapper.selectById(req.getClientId());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "客户端不存在");
        }
        assertRedirectWhenRequired(req.getGrantTypes(), req.getRedirectUris());
        normalizeAndValidateClient(req);
        SysOauthClient row = BeanUtil.copyProperties(req, SysOauthClient.class);
        row.setRedirectUris(normalizeRedirectUris(req.getRedirectUris()));
        row.setScopes(OauthClientApiPathAuthService.DEFAULT_OAUTH_SCOPES);
        row.setApiPathPatterns(normalizeApiPathPatterns(req.getApiPathPatterns()));
        row.setSignVerify(normalizeSignVerify(req.getSignVerify()));
        if (StrUtil.isNotBlank(req.getClientSecret())) {
            row.setClientSecret(Oauth2SecretSupport.encodeForStorage(passwordCodec, req.getClientSecret()));
        } else {
            row.setClientSecret(existing.getClientSecret());
        }
        mapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return;
        }
        mapper.deleteByIds(clientIds);
    }

    private static void assertRedirectWhenRequired(String grantTypes, String redirectUris) {
        if (!grantTypesRequireRedirect(grantTypes)) {
            return;
        }
        if (StrUtil.isBlank(redirectUris)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                    "启用 authorization_code 或 implicit 时须填写回调地址");
        }
    }

    private static boolean grantTypesRequireRedirect(String grantTypes) {
        if (StrUtil.isBlank(grantTypes)) {
            return false;
        }
        Set<String> allowed = Arrays.stream(grantTypes.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        return allowed.contains("authorization_code") || allowed.contains("implicit");
    }

    private static String normalizeRedirectUris(String redirectUris) {
        return StrUtil.nullToEmpty(redirectUris).trim();
    }

    private static String normalizeApiPathPatterns(String patterns) {
        if (StrUtil.isBlank(patterns)) {
            return "";
        }
        return patterns.replace("\r\n", "\n").trim();
    }

    private void normalizeAndValidateClient(SysOauthClientBo req) {
        req.setSignVerify(normalizeSignVerify(req.getSignVerify()));
        if ("1".equals(req.getSignVerify())) {
            if (StrUtil.isBlank(req.getApiPathPatterns())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "启用验签时须配置接口授权（Ant 路径）");
            }
            apiPathAuthService.validatePathPatterns(req.getApiPathPatterns());
        }
    }

    private static String normalizeSignVerify(String signVerify) {
        if (StrUtil.isBlank(signVerify) || "1".equals(signVerify)) {
            return "1";
        }
        if ("0".equals(signVerify)) {
            return "0";
        }
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "是否验签须为 0 或 1");
    }
}
