package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.role.dto.SysRoleQueryBo;
import io.github.genkidoudou.web.system.role.service.SysRoleService;
import org.springframework.stereotype.Component;

/**
 * 角色管理导出 {@code system:role}。
 */
@Component
public class RoleBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "system:role";

    private final SysRoleService roleService;

    public RoleBizExportHandler(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return roleService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return roleService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "role-export.xlsx";
    }

    private SysRoleQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysRoleQueryBo.class);
    }
}
