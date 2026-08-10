package io.github.genkidoudou.tool.internal.gen.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.tool.internal.gen.dto.GenCreateTableBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDefaultsVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDbTableVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenPreviewVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableDetailVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableQueryBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableUpdateBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableVo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 代码生成业务服务。
 */
public interface GenTableService {

    PageInfo<GenTableVo> page(GenTableQueryBo query);

    List<GenDbTableVo> listDbTables(String tableName, String tableComment);

    GenTableDetailVo getDetail(Long tableId);

    /**
     * 全局默认生成配置（参数设置 qc.gen.*）。
     */
    GenDefaultsVo getDefaults();

    void update(GenTableUpdateBo req);

    void remove(Long tableId);

    void importTables(List<String> tableNames);

    void createTable(GenCreateTableBo req);

    void synchDb(String tableName);

    List<GenPreviewVo> preview(Long tableId);

    void batchGenCode(String tables, HttpServletResponse response) throws IOException;

    /**
     * 按配置将代码写入自定义路径（gen_type=1）。
     *
     * @return 实际写入根路径
     */
    String genCodeToPath(String tableName) throws IOException;
}
