package io.github.genkidoudou.web.auth;

import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.config.service.SysConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link LoginLockServiceImpl} 单元测试（Spring {@link org.springframework.cache.concurrent.ConcurrentMapCacheManager}）。
 */
@ExtendWith(MockitoExtension.class)
class LoginLockServiceImplTest {

    @Mock
    private SysConfigService sysConfigService;

    @Test
    void lockAfterMaxRetryThenClear() {
        when(sysConfigService.getConfigValueByKey("qc.login.fail-lock-enabled")).thenReturn("true");
        when(sysConfigService.getConfigValueByKey("qc.login.max-retry")).thenReturn("2");
        when(sysConfigService.getConfigValueByKey("qc.login.lock-minutes")).thenReturn("60");

        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(LoginLockServiceImpl.CACHE_NAME);
        LoginLockServiceImpl svc = new LoginLockServiceImpl(cacheManager, sysConfigService);
        String u = "tester";

        assertDoesNotThrow(() -> svc.assertNotLocked(u));
        svc.recordFailure(u);
        assertDoesNotThrow(() -> svc.assertNotLocked(u));
        svc.recordFailure(u);
        assertThrows(WarningException.class, () -> svc.assertNotLocked(u));
        svc.clearForUserName(u);
        assertDoesNotThrow(() -> svc.assertNotLocked(u));
    }
}
