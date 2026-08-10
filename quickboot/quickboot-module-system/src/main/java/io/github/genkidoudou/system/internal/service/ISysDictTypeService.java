package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysDictTypeImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ISysDictTypeService {
  PageInfo<SysDictTypeVo> page(PageRequest<SysDictTypeVo> pageRequest);

  SysDictTypeVo getDetail(Long dictId);

  Long add(SysDictTypeVo vo);

  boolean update(SysDictTypeVo vo);

  void remove(Collection<Long> ids);

  void refreshAll();

  void refresh(String dictType);

  List<SysDictTypeVo> export(SysDictTypeVo query);

  ExcelResult<SysDictTypeImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
