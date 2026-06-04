package io.github.genkidoudou.web.system.importtask.handler.impl;

import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandler;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;
import io.github.genkidoudou.web.system.role.dto.SysRoleImportExcelRow;
import io.github.genkidoudou.web.system.role.service.SysRoleService;
import org.springframework.stereotype.Component;

/**
 * 角色导入 {@code system:role}。
 */
@Component
public class RoleBizImportHandler implements BizImportHandler {

    public static final String BIZ_TYPE = "system:role";

    private final SysRoleService roleService;

    public RoleBizImportHandler(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public Class<?> rowClass() {
        return SysRoleImportExcelRow.class;
    }

    @Override
    public String processRow(Object row, boolean overwrite, ImportHandlerContext context) {
        try {
            roleService.importRoleExcelRow((SysRoleImportExcelRow) row, overwrite);
            return null;
        } catch (ExcelDataCheckException ex) {
            return ex.getMessage();
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }
}
