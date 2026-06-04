package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.user.dto.SysUserQueryBo;
import io.github.genkidoudou.web.system.user.service.SysUserService;
import org.springframework.stereotype.Component;

/**
 * 用户管理导出 {@code system:user}。
 */
@Component
public class UserBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "system:user";

    private final SysUserService userService;

    public UserBizExportHandler(SysUserService userService) {
        this.userService = userService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return userService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return userService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "user-export.xlsx";
    }

    private SysUserQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysUserQueryBo.class);
    }
}
