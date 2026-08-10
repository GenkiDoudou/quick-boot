package io.github.genkidoudou.tool.internal.gen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import freemarker.template.TemplateException;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.tool.internal.gen.config.GenProperties;
import io.github.genkidoudou.tool.internal.gen.domain.GenTable;
import io.github.genkidoudou.tool.internal.gen.domain.GenTableColumn;
import io.github.genkidoudou.tool.internal.gen.dto.GenCreateTableBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDefaultsVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDbTableVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenPreviewVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableColumnVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableDetailVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableQueryBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableUpdateBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableVo;
import io.github.genkidoudou.tool.internal.gen.mapper.GenTableColumnMapper;
import io.github.genkidoudou.tool.internal.gen.mapper.GenTableMapper;
import io.github.genkidoudou.tool.internal.gen.service.GenTableService;
import io.github.genkidoudou.tool.internal.gen.support.GenCodePathWriter;
import io.github.genkidoudou.tool.internal.gen.support.GenColumnDefaults;
import io.github.genkidoudou.tool.internal.gen.support.GenConfigResolver;
import io.github.genkidoudou.tool.internal.gen.support.GenContext;
import io.github.genkidoudou.tool.internal.gen.support.GenDbIntrospector;
import io.github.genkidoudou.tool.internal.gen.support.GenTemplateRenderer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成业务服务实现。
 */
@Service
public class GenTableServiceImpl implements GenTableService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private final GenDbIntrospector dbIntrospector;
    private final GenColumnDefaults columnDefaults;
    private final GenConfigResolver genConfigResolver;
    private final GenTemplateRenderer templateRenderer;
    private final GenProperties genProperties;
    private final JdbcTemplate jdbcTemplate;
    private final GenCodePathWriter genCodePathWriter;

    public GenTableServiceImpl(
        GenTableMapper genTableMapper,
        GenTableColumnMapper genTableColumnMapper,
        GenDbIntrospector dbIntrospector,
        GenColumnDefaults columnDefaults,
        GenConfigResolver genConfigResolver,
        GenTemplateRenderer templateRenderer,
        GenProperties genProperties,
        JdbcTemplate jdbcTemplate,
        GenCodePathWriter genCodePathWriter
    ) {
        this.genTableMapper = genTableMapper;
        this.genTableColumnMapper = genTableColumnMapper;
        this.dbIntrospector = dbIntrospector;
        this.columnDefaults = columnDefaults;
        this.genConfigResolver = genConfigResolver;
        this.templateRenderer = templateRenderer;
        this.genProperties = genProperties;
        this.jdbcTemplate = jdbcTemplate;
        this.genCodePathWriter = genCodePathWriter;
    }

    @Override
    public PageInfo<GenTableVo> page(GenTableQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<GenTable> w = Wrappers.<GenTable>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getTableName()), GenTable::getTableName, query.getTableName())
            .like(StrUtil.isNotBlank(query.getTableComment()), GenTable::getTableComment, query.getTableComment())
            .ge(query.getBeginTime() != null, GenTable::getCreateTime, query.getBeginTime())
            .le(query.getEndTime() != null, GenTable::getCreateTime, query.getEndTime())
            .orderByDesc(GenTable::getCreateTime);
        Page<GenTable> mp = genTableMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<GenTableVo> rows = mp.getRecords().stream()
            .map(r -> BeanUtil.copyProperties(r, GenTableVo.class))
            .collect(Collectors.toList());
        Page<GenTableVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public List<GenDbTableVo> listDbTables(String tableName, String tableComment) {
        Set<String> imported = genTableMapper.selectList(Wrappers.emptyWrapper()).stream()
            .map(GenTable::getTableName)
            .collect(Collectors.toSet());
        return dbIntrospector.listTableInfo().stream()
            .filter(t -> !imported.contains(t.getName()))
            .filter(t -> StrUtil.isBlank(tableName) || t.getName().contains(tableName))
            .filter(t -> StrUtil.isBlank(tableComment)
                || StrUtil.blankToDefault(t.getComment(), "").contains(tableComment))
            .map(t -> {
                GenDbTableVo vo = new GenDbTableVo();
                vo.setTableName(t.getName());
                vo.setTableComment(t.getComment());
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public GenTableDetailVo getDetail(Long tableId) {
        GenTable table = genTableMapper.selectById(tableId);
        if (table == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "生成配置不存在");
        }
        GenTableDetailVo detail = new GenTableDetailVo();
        detail.setInfo(BeanUtil.copyProperties(table, GenTableVo.class));
        List<GenTableColumn> columns = genTableColumnMapper.selectList(
            Wrappers.<GenTableColumn>lambdaQuery()
                .eq(GenTableColumn::getTableId, tableId)
                .orderByAsc(GenTableColumn::getSort)
        );
        detail.setColumns(columns.stream()
            .map(c -> BeanUtil.copyProperties(c, GenTableColumnVo.class))
            .collect(Collectors.toList()));
        return detail;
    }

    @Override
    public GenDefaultsVo getDefaults() {
        return genConfigResolver.resolveDefaults();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GenTableUpdateBo req) {
        GenTable existing = genTableMapper.selectById(req.getTableId());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "生成配置不存在");
        }
        if ("1".equals(req.getGenType()) && StrUtil.isBlank(req.getGenPath())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "自定义路径不能为空");
        }
        GenTable table = BeanUtil.copyProperties(req, GenTable.class);
        table.setUpdateTime(LocalDateTime.now());
        genTableMapper.updateById(table);
        genTableColumnMapper.delete(
            Wrappers.<GenTableColumn>lambdaQuery().eq(GenTableColumn::getTableId, req.getTableId())
        );
        int sort = 1;
        for (GenTableColumnVo colVo : req.getColumns()) {
            GenTableColumn col = BeanUtil.copyProperties(colVo, GenTableColumn.class);
            col.setColumnId(null);
            col.setTableId(req.getTableId());
            col.setSort(sort++);
            col.setUpdateTime(LocalDateTime.now());
            genTableColumnMapper.insert(col);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long tableId) {
        genTableMapper.deleteById(tableId);
        genTableColumnMapper.delete(
            Wrappers.<GenTableColumn>lambdaQuery().eq(GenTableColumn::getTableId, tableId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTables(List<String> tableNames) {
        if (CollUtil.isEmpty(tableNames)) {
            throw new WarningException(ErrorCodes.Gen.IMPORT_TABLES_EMPTY, "请选择要导入的表");
        }
        List<GenTable> exists = genTableMapper.selectList(
            Wrappers.<GenTable>lambdaQuery().in(GenTable::getTableName, tableNames)
        );
        if (CollUtil.isNotEmpty(exists)) {
            String names = exists.stream().map(GenTable::getTableName).collect(Collectors.joining(","));
            throw new WarningException(ErrorCodes.Gen.TABLE_ALREADY_IMPORTED, "表已导入: " + names);
        }
        List<TableInfo> tableInfos = dbIntrospector.listTableInfo(tableNames.toArray(new String[0]));
        for (TableInfo info : tableInfos) {
            GenTable table = columnDefaults.toGenTable(info);
            table.setCreateTime(LocalDateTime.now());
            table.setUpdateTime(LocalDateTime.now());
            genTableMapper.insert(table);
            List<GenTableColumn> columns = columnDefaults.toGenColumns(info, table.getTableId());
            for (GenTableColumn col : columns) {
                col.setCreateTime(LocalDateTime.now());
                col.setUpdateTime(LocalDateTime.now());
                genTableColumnMapper.insert(col);
            }
        }
    }

    @Override
    public void createTable(GenCreateTableBo req) {
        String sql = req.getSql();
        if (StrUtil.isBlank(sql)) {
            throw new WarningException(ErrorCodes.Gen.SQL_INVALID, "SQL 不能为空");
        }
        List<SQLStatement> statements;
        try {
            statements = SQLUtils.parseStatements(sql, DbType.mysql);
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Gen.SQL_INVALID, "SQL 解析失败: " + e.getMessage());
        }
        if (statements.size() > genProperties.getCreateTableMaxStatements()) {
            throw new WarningException(ErrorCodes.Gen.SQL_INVALID, "超过允许的建表语句条数");
        }
        int executed = 0;
        for (SQLStatement statement : statements) {
            if (statement instanceof MySqlCreateTableStatement create) {
                jdbcTemplate.execute(create.toString());
                executed++;
            } else {
                throw new WarningException(ErrorCodes.Gen.SQL_INVALID, "仅允许 CREATE TABLE 语句");
            }
        }
        if (executed == 0) {
            throw new WarningException(ErrorCodes.Gen.SQL_INVALID, "未找到有效的 CREATE TABLE 语句");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void synchDb(String tableName) {
        GenTable table = genTableMapper.selectOne(
            Wrappers.<GenTable>lambdaQuery().eq(GenTable::getTableName, tableName)
        );
        if (table == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "生成配置不存在");
        }
        TableInfo info = dbIntrospector.getTableInfo(tableName);
        if (info == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "物理表不存在: " + tableName);
        }
        GenTable updated = columnDefaults.toGenTable(info);
        updated.setTableId(table.getTableId());
        updated.setTplCategory(table.getTplCategory());
        updated.setTplWebType(table.getTplWebType());
        updated.setPackageName(table.getPackageName());
        updated.setModuleName(table.getModuleName());
        updated.setBusinessName(table.getBusinessName());
        updated.setFunctionName(table.getFunctionName());
        updated.setFunctionAuthor(table.getFunctionAuthor());
        updated.setGenType(table.getGenType());
        updated.setGenPath(table.getGenPath());
        updated.setParentMenuId(table.getParentMenuId());
        updated.setRemark(table.getRemark());
        updated.setUpdateTime(LocalDateTime.now());
        genTableMapper.updateById(updated);
        genTableColumnMapper.delete(
            Wrappers.<GenTableColumn>lambdaQuery().eq(GenTableColumn::getTableId, table.getTableId())
        );
        List<GenTableColumn> columns = columnDefaults.toGenColumns(info, table.getTableId());
        for (GenTableColumn col : columns) {
            col.setUpdateTime(LocalDateTime.now());
            genTableColumnMapper.insert(col);
        }
    }

    @Override
    public List<GenPreviewVo> preview(Long tableId) {
        return renderFiles(tableId).entrySet().stream().map(e -> {
            GenPreviewVo vo = new GenPreviewVo();
            vo.setTemplateName(e.getKey());
            vo.setContent(e.getValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String genCodeToPath(String tableName) throws IOException {
        if (StrUtil.isBlank(tableName)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "表名不能为空");
        }
        GenTable table = genTableMapper.selectOne(
            Wrappers.<GenTable>lambdaQuery().eq(GenTable::getTableName, tableName.trim())
        );
        if (table == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "表未导入: " + tableName);
        }
        if (!"1".equals(table.getGenType())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "该表未配置为自定义路径生成");
        }
        assertTemplateAllowed(table);
        Map<String, String> files = renderFiles(table.getTableId());
        return genCodePathWriter.write(table.getGenPath(), files);
    }

    @Override
    public void batchGenCode(String tables, HttpServletResponse response) throws IOException {
        if (StrUtil.isBlank(tables)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "tables 参数不能为空");
        }
        List<String> tableNames = Arrays.stream(tables.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toList());
        if (tableNames.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "tables 参数不能为空");
        }
        for (String tableName : tableNames) {
            GenTable table = genTableMapper.selectOne(
                Wrappers.<GenTable>lambdaQuery().eq(GenTable::getTableName, tableName)
            );
            if (table != null && "1".equals(table.getGenType())) {
                throw new WarningException(
                    ErrorCodes.Common.INVALID_PARAM,
                    "表「" + tableName + "」为自定义路径生成，请使用「生成」写盘或改为 Zip 方式"
                );
            }
        }
        String zipName = genProperties.getZipFileName();
        response.setContentType("application/zip");
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"" + URLEncoder.encode(zipName, StandardCharsets.UTF_8) + "\""
        );
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            for (String tableName : tableNames) {
                GenTable table = genTableMapper.selectOne(
                    Wrappers.<GenTable>lambdaQuery().eq(GenTable::getTableName, tableName)
                );
                if (table == null) {
                    throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "表未导入: " + tableName);
                }
                assertTemplateAllowed(table);
                Map<String, String> files = renderFiles(table.getTableId());
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String path = tableName + "/" + entry.getKey();
                    zip.putNextEntry(new ZipEntry(path));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
            zip.finish();
        }
    }

    private Map<String, String> renderFiles(Long tableId) {
        GenTable table = genTableMapper.selectById(tableId);
        if (table == null) {
            throw new WarningException(ErrorCodes.Gen.TABLE_NOT_FOUND, "生成配置不存在");
        }
        assertTemplateAllowed(table);
        List<GenTableColumn> columns = genTableColumnMapper.selectList(
            Wrappers.<GenTableColumn>lambdaQuery()
                .eq(GenTableColumn::getTableId, tableId)
                .orderByAsc(GenTableColumn::getSort)
        );
        try {
            return templateRenderer.renderAll(new GenContext(table, columns));
        } catch (IOException | TemplateException e) {
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "模板渲染失败: " + e.getMessage());
        }
    }

    private void assertTemplateAllowed(GenTable table) {
        if ("tree".equalsIgnoreCase(table.getTplCategory())) {
            throw new WarningException(ErrorCodes.Gen.TREE_TEMPLATE_NOT_SUPPORTED, "树表模板尚未开放");
        }
    }
}
