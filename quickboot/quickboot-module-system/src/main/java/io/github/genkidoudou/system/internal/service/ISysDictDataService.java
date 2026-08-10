package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysDictDataImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ISysDictDataService {
  PageInfo<SysDictDataVo> page(PageRequest<SysDictDataVo> pageRequest);

  SysDictDataVo getDetail(Long dictCode);

  Long add(SysDictDataVo vo);

  boolean update(SysDictDataVo vo);

  void remove(Collection<Long> ids);

  List<SysDictDataVo> listByType(String dictType);

  List<SysDictDataVo> export(SysDictDataVo query);

  ExcelResult<SysDictDataImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
