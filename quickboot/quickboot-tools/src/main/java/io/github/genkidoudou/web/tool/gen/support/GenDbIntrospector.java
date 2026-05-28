package io.github.genkidoudou.web.tool.gen.support;

import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基于 MyBatis-Plus Generator 的库表内省。
 */
@Component
public class GenDbIntrospector {

    private final DataSource dataSource;

    public GenDbIntrospector(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 读取表结构；includes 为空时扫描当前库全部用户表。
     *
     * @param includes 物理表名，可为空
     * @return 表信息列表
     */
    public List<TableInfo> listTableInfo(String... includes) {
        StrategyConfig.Builder strategy = new StrategyConfig.Builder().enableSkipView();
        if (includes != null && includes.length > 0) {
            strategy.addInclude(includes);
        }
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(dataSource).build();
        ConfigBuilder configBuilder = new ConfigBuilder(
            null,
            dataSourceConfig,
            strategy.build(),
            null,
            null,
            null
        );
        List<TableInfo> list = configBuilder.getTableInfoList();
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 读取单表结构。
     */
    public TableInfo getTableInfo(String tableName) {
        List<TableInfo> list = listTableInfo(tableName);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
