package io.github.genkidoudou.web.system.user.datascope;

/**
 * 登录时归并后的数据范围类型（与历史 quick-boot {@code LoginUser#dataScopeType} 一致）。
 */
public enum DataScopeType {

    /** 全部数据（任一角色 data_scope=1）。 */
    ALL,

    /**
     * 部门范围：可见部门 id 已展开在 {@link DataScopeSession#visibleDeptIds()}；
     * 对应原项目中自定义、本部门、本部门及以下等合并结果。
     */
    DEPT,

    /**
     * 仅本人：单角色且 data_scope=5；SQL 侧以 {@link DataPermission#userField()} 等于当前用户 id 为主，
     * 若仍带部门 id 列表则与部门条件取 OR（与旧 quick-boot 规则引擎一致）。
     */
    SELF
}
