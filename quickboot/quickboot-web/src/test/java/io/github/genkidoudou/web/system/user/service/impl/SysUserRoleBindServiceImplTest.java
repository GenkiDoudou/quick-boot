package io.github.genkidoudou.web.system.user.service.impl;

import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserRoleBindServiceImpl} 单测：验证全量替换与幂等插入行为。
 */
@ExtendWith(MockitoExtension.class)
class SysUserRoleBindServiceImplTest {

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private SysUserRoleBindServiceImpl bindService;

    @Test
    void replaceAllRolesForUser_deletesThenInserts() {
        bindService.replaceAllRolesForUser(10L, List.of(2L, 3L));
        verify(userRoleMapper).delete(any());
        ArgumentCaptor<SysUserRole> cap = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper, times(2)).insert(cap.capture());
        assertThat(cap.getAllValues()).extracting(SysUserRole::getUserId).containsOnly(10L);
        assertThat(cap.getAllValues()).extracting(SysUserRole::getRoleId).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void ensureUserHasRole_insertsWhenAbsent() {
        when(userRoleMapper.selectCount(any())).thenReturn(0L);
        bindService.ensureUserHasRole(5L, 7L);
        verify(userRoleMapper).insert(any(SysUserRole.class));
    }

    @Test
    void ensureUserHasRole_skipsWhenExists() {
        when(userRoleMapper.selectCount(any())).thenReturn(1L);
        bindService.ensureUserHasRole(5L, 7L);
        verify(userRoleMapper, never()).insert(isA(SysUserRole.class));
    }
}
