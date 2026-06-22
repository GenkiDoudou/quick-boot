package io.github.genkidoudou.web.system.user.authcache;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色/权限二级缓存：Spring Cache（按 userId）+ Sa Session 快照（按登录会话）。
 * <p>
 * 菜单全局变更通过 bump 全局版本号使所有 Session 快照失效；单用户/单角色变更则精确 evict。
 */
@Service
@RequiredArgsConstructor
public class UserAuthCacheService {

    /** 用户角色 key 列表缓存（cacheNames 后缀 #ttl 秒，见 DynamicTtl*CacheManager）。 */
    public static final String ROLES_CACHE = "qc-user-roles#3600";
    /** 用户权限标识列表缓存。 */
    public static final String PERMS_CACHE = "qc-user-permissions#3600";
    private static final String GLOBAL_VERSION_CACHE = "qc-auth-global-version#86400";
    private static final String GLOBAL_VERSION_KEY = "v";

    private final MenuService menuService;
    private final CacheManager cacheManager;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 登录成功后刷新 Session 快照（并填充 Spring Cache）。
     *
     * @param userId 登录用户 id
     */
    public void refreshSessionOnLogin(long userId) {
        List<String> roles = menuService.listRoleKeysByUserId(userId);
        List<String> permissions = menuService.listPermissionsByUserId(userId);
        UserAuthSessionStore.save(roles, permissions, currentGlobalVersion());
    }

    /**
     * @return 当前全局权限版本（菜单变更时递增）
     */
    public long currentGlobalVersion() {
        Cache cache = requireCache(GLOBAL_VERSION_CACHE);
        Cache.ValueWrapper wrapper = cache.get(GLOBAL_VERSION_KEY);
        if (wrapper == null || wrapper.get() == null) {
            cache.put(GLOBAL_VERSION_KEY, 1L);
            return 1L;
        }
        Object val = wrapper.get();
        if (val instanceof Number n) {
            return n.longValue();
        }
        return 1L;
    }

    /**
     * 菜单增删改等全局影响：失效所有用户的 Spring Cache，并 bump 版本使 Session 快照失效。
     */
    public void evictAllUsersForMenuChange() {
        bumpGlobalVersion();
        clearCacheAllEntries(ROLES_CACHE);
        clearCacheAllEntries(PERMS_CACHE);
    }

    /**
     * 单用户角色变更：清 Spring Cache 与该用户 Session 快照。
     *
     * @param userId 用户 id
     */
    public void evictUser(long userId) {
        evictSpringCacheForUser(userId);
        UserAuthSessionStore.clearForLoginId(userId);
    }

    /**
     * 角色菜单/授权用户变更：失效持有该角色的所有在线用户缓存。
     *
     * @param roleId 角色 id
     */
    public void evictUsersByRoleId(long roleId) {
        if (roleId < 1) {
            return;
        }
        List<SysUserRole> bindings = userRoleMapper.selectList(
            Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, roleId));
        Set<Long> userIds = bindings.stream()
            .map(SysUserRole::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        for (Long userId : userIds) {
            evictUser(userId);
        }
    }

    private void bumpGlobalVersion() {
        Cache cache = requireCache(GLOBAL_VERSION_CACHE);
        long next = currentGlobalVersion() + 1;
        cache.put(GLOBAL_VERSION_KEY, next);
    }

    private void evictSpringCacheForUser(long userId) {
        Cache roles = requireCache(ROLES_CACHE);
        Cache perms = requireCache(PERMS_CACHE);
        roles.evict(userId);
        perms.evict(userId);
    }

    private void clearCacheAllEntries(String cacheName) {
        Cache cache = requireCache(cacheName);
        cache.clear();
    }

    private Cache requireCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("缓存未配置: " + cacheName);
        }
        return cache;
    }
}
