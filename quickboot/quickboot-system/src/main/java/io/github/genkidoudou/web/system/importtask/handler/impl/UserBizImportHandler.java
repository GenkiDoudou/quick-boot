package io.github.genkidoudou.web.system.importtask.handler.impl;

import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandler;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.user.dto.SysUserImportExcelRow;
import io.github.genkidoudou.web.system.user.service.SysUserService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户导入 {@code system:user}。
 */
@Component
public class UserBizImportHandler implements BizImportHandler {

    public static final String BIZ_TYPE = "system:user";

    private final SysUserService userService;

    public UserBizImportHandler(SysUserService userService) {
        this.userService = userService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public Class<?> rowClass() {
        return SysUserImportExcelRow.class;
    }

    @Override
    public void beforeImport(ImportHandlerContext context) {
        Map<String, SysRole> roles = userService.loadActiveRolesByKeyForImport();
        context.setAttribute("roleByKey", roles);
    }

    @Override
    public String processRow(Object row, boolean overwrite, ImportHandlerContext context) {
        SysUserImportExcelRow excelRow = (SysUserImportExcelRow) row;
        try {
            userService.importExcelRow(excelRow, overwrite);
            return null;
        } catch (WarningException ex) {
            return ex.getMsg();
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }
}
