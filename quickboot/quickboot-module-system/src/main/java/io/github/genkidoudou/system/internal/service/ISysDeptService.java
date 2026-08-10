package io.github.genkidoudou.system.internal.service;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
public interface ISysDeptService {
  List<SysDeptVo> list(String name, String status);
  List<SysDeptVo> treeSelect();
  SysDeptVo getDetail(Long id);
  Long add(SysDeptVo vo);
  boolean update(SysDeptVo vo);
  void remove(Collection<Long> ids);
  List<SysDeptVo> export(SysDeptVo query);
  ExcelResult<SysDeptImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
