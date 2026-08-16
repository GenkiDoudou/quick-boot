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

    /**
     * 分页查询已导入的生成配置。
     *
     * @param query 表名、注释、时间范围等筛选条件
     * @return 分页结果
     */
    PageInfo<GenTableVo> page(GenTableQueryBo query);

    /**
     * 列出物理库中尚未导入的候选表。
     *
     * @param tableName    表名模糊匹配，可为空
     * @param tableComment 表注释模糊匹配，可为空
     * @return 未导入的物理表列表
     */
    List<GenDbTableVo> listDbTables(String tableName, String tableComment);

    /**
     * 查询单表生成配置及列明细。
     *
     * @param tableId 生成配置主键
     * @return 表头 + 列配置
     */
    GenTableDetailVo getDetail(Long tableId);

    /**
     * 全局默认生成配置（参数设置 qc.gen.*）。
     */
    GenDefaultsVo getDefaults();

    /**
     * 保存表头与列配置；会先删后插列记录。
     *
     * @param req 表配置与列列表
     */
    void update(GenTableUpdateBo req);

    /**
     * 删除生成配置及其列配置。
     *
     * @param tableId 生成配置主键
     */
    void remove(Long tableId);

    /**
     * 从物理库导入表结构并写入默认生成配置。
     *
     * @param tableNames 物理表名列表
     */
    void importTables(List<String> tableNames);

    /**
     * 执行用户提交的建表 SQL（仅允许 CREATE TABLE）。
     *
     * @param req 含 SQL 文本
     */
    void createTable(GenCreateTableBo req);

    /**
     * 按物理表最新结构刷新列配置，保留用户自定义的模板/路径等字段。
     *
     * @param tableName 物理表名
     */
    void synchDb(String tableName);

    /**
     * 渲染模板预览，不落盘。
     *
     * @param tableId 生成配置主键
     * @return 模板相对路径与内容列表
     */
    List<GenPreviewVo> preview(Long tableId);

    /**
     * 批量生成代码并以 Zip 流写入 HTTP 响应。
     *
     * @param tables   逗号分隔的物理表名
     * @param response HTTP 响应，用于输出 Zip
     */
    void batchGenCode(String tables, HttpServletResponse response) throws IOException;

    /**
     * 按配置将代码写入自定义路径（gen_type=1）。
     *
     * @return 实际写入根路径
     */
    String genCodeToPath(String tableName) throws IOException;
}
