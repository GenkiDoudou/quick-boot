package io.github.genkidoudou.report.internal.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 从积木内置表读取报表 / BI 大屏列表，供系统菜单绑定预览地址。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JimuCatalogService {

    private static final String SQL_REPORTS = """
            SELECT id, name, code
            FROM jimu_report
            WHERE del_flag IS NULL OR del_flag = 0
            ORDER BY update_time DESC, create_time DESC
            """;

    private static final String SQL_BI_PAGES = """
            SELECT id, name, path AS code
            FROM onl_drag_page
            WHERE del_flag IS NULL OR del_flag = 0
            ORDER BY update_time DESC, create_time DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public List<JimuCatalogItemVo> listReports() {
        return queryCatalog(SQL_REPORTS);
    }

    public List<JimuCatalogItemVo> listBiPages() {
        return queryCatalog(SQL_BI_PAGES);
    }

    private List<JimuCatalogItemVo> queryCatalog(String sql) {
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                JimuCatalogItemVo vo = new JimuCatalogItemVo();
                vo.setId(rs.getString("id"));
                vo.setName(rs.getString("name"));
                vo.setCode(rs.getString("code"));
                return vo;
            });
        } catch (DataAccessException ex) {
            log.warn("读取积木目录失败（可能尚未初始化 Jimu 表）: {}", ex.getMessage());
            return List.of();
        }
    }
}
