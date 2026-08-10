package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysConfigImportRow;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ISysConfigService {

  PageInfo<SysConfigVo> page(PageRequest<SysConfigVo> pageRequest);

  SysConfigVo getDetail(Long configId);

  Long add(SysConfigVo vo);

  boolean update(SysConfigVo vo);

  void remove(Collection<Long> ids);

  String getConfigValueByKey(String configKey);

  void refreshCache();

  List<SysConfigVo> export(SysConfigVo request);

  ExcelResult<SysConfigImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
