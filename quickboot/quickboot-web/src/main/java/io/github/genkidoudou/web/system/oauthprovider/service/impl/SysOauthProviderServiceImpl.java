package io.github.genkidoudou.web.system.oauthprovider.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.auth.oauth2.support.Oauth2SecretSupport;
import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthProvider;
import io.github.genkidoudou.web.system.oauthprovider.dto.SysOauthProviderBo;
import io.github.genkidoudou.web.system.oauthprovider.mapper.SysOauthProviderMapper;
import io.github.genkidoudou.web.system.oauthprovider.service.SysOauthProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link SysOauthProviderService} 实现。
 */
@Service
@RequiredArgsConstructor
public class SysOauthProviderServiceImpl implements SysOauthProviderService {

    private final SysOauthProviderMapper mapper;
    private final PasswordCodec passwordCodec;

    @Override
    public List<SysOauthProvider> list(String providerName) {
        return mapper.selectList(Wrappers.<SysOauthProvider>lambdaQuery()
                .like(StrUtil.isNotBlank(providerName), SysOauthProvider::getProviderName, providerName)
                .orderByDesc(SysOauthProvider::getCreateTime));
    }

    @Override
    public List<SysOauthProvider> listEnabledForLogin() {
        return mapper.selectList(Wrappers.<SysOauthProvider>lambdaQuery()
                .eq(SysOauthProvider::getEnabled, "1")
                .orderByAsc(SysOauthProvider::getProviderName));
    }

    @Override
    public SysOauthProvider getByCode(String providerCode) {
        return mapper.selectById(providerCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysOauthProviderBo req) {
        if (mapper.selectById(req.getProviderCode()) != null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_ALREADY_IMPORTED, "provider_code 已存在");
        }
        SysOauthProvider row = BeanUtil.copyProperties(req, SysOauthProvider.class);
        row.setClientSecret(Oauth2SecretSupport.encodeForStorage(passwordCodec, req.getClientSecret()));
        mapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysOauthProviderBo req) {
        SysOauthProvider existing = mapper.selectById(req.getProviderCode());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "IdP 不存在");
        }
        SysOauthProvider row = BeanUtil.copyProperties(req, SysOauthProvider.class);
        if (StrUtil.isNotBlank(req.getClientSecret())) {
            row.setClientSecret(Oauth2SecretSupport.encodeForStorage(passwordCodec, req.getClientSecret()));
        } else {
            row.setClientSecret(existing.getClientSecret());
        }
        mapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<String> providerCodes) {
        if (providerCodes == null || providerCodes.isEmpty()) {
            return;
        }
        mapper.deleteByIds(providerCodes);
    }
}
