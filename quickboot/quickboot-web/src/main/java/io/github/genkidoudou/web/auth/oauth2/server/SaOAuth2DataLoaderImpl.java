package io.github.genkidoudou.web.auth.oauth2.server;

import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.dev33.satoken.oauth2.exception.SaOAuth2ClientModelException;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.oauth2.Oauth2Properties;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.auth.oauth2.support.Oauth2SecretSupport;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthUserOpenid;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthClientMapper;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthUserOpenidMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 从业务库加载 OAuth2 Client 与 openid。
 */
@Component
@RequiredArgsConstructor
public class SaOAuth2DataLoaderImpl implements SaOAuth2DataLoader {

    private final SysOauthClientMapper clientMapper;
    private final SysOauthUserOpenidMapper openidMapper;
    private final PasswordCodec passwordCodec;
    private final Oauth2Properties oauth2Properties;

    @Override
    public SaClientModel getClientModel(String clientId) {
        SysOauthClient row = clientMapper.selectById(clientId);
        if (row == null || !"0".equals(row.getStatus()) || !"0".equals(row.getDelFlag())) {
            return null;
        }
        String plain = Oauth2SecretSupport.resolvePlainSecret(passwordCodec, row.getClientSecret());
        return Oauth2ClientModelConverter.toSaModel(row, oauth2Properties, plain);
    }

    @Override
    public SaClientModel getClientModelNotNull(String clientId) {
        SaClientModel model = getClientModel(clientId);
        if (model == null) {
            throw new SaOAuth2ClientModelException("无效 client_id: " + clientId);
        }
        return model;
    }

    @Override
    public String getOpenid(String clientId, Object loginId) {
        Long userId = Long.valueOf(String.valueOf(loginId));
        SysOauthUserOpenid existing = openidMapper.selectOne(Wrappers.<SysOauthUserOpenid>lambdaQuery()
                .eq(SysOauthUserOpenid::getClientId, clientId)
                .eq(SysOauthUserOpenid::getUserId, userId)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing.getOpenid();
        }
        String openid = IdUtil.fastSimpleUUID();
        SysOauthUserOpenid row = new SysOauthUserOpenid();
        row.setClientId(clientId);
        row.setUserId(userId);
        row.setOpenid(openid);
        openidMapper.insert(row);
        return openid;
    }
}
