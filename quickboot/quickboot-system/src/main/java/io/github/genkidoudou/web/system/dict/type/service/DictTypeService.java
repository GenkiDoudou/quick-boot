package io.github.genkidoudou.web.system.dict.type.service;

import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeBo;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeQueryBo;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DictTypeService {
    List<SysDictType> list(String dictName, String dictType, String status);
    SysDictType getById(Long dictId);
    void add(SysDictTypeBo req);
    void update(SysDictTypeBo req);
    void remove(Long dictId);
    List<SysDictType> export(String dictName, String dictType, String status);

    /** 按筛选条件统计可导出行数。 */
    long countExportRows(SysDictTypeQueryBo query);

    /** 生成导出 Excel 字节（最多 {@code maxRows} 行）。 */
    byte[] exportExcelBytes(SysDictTypeQueryBo query, int maxRows);
    void refreshAllCache();
    void refreshTypeCache(String dictType);
    ExcelImportResult importData(MultipartFile file, boolean updateSupport) throws IOException;

    /**
     * 导入单行字典类型（供平台导入编排调用）。
     *
     * @throws io.github.genkidoudou.common.excel.exception.ExcelDataCheckException 业务校验失败
     */
    void importDictTypeExcelRow(io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeExcelRow row, boolean updateSupport);
}
