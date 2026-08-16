package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 文件分类管理。
 */
@Tag(name = "文件分类")
@RequiredArgsConstructor
@RestController
@RequestMapping("system/fileClassify")
public class SysFileClassifyController {

  private final ISysFileClassifyService sysFileClassifyService;

  /**
   * 分页列表（GET，供 C7JsonTable：data.records / data.total）。
   *
   * @param pageNum     页码，默认 1
   * @param pageSize    每页条数，默认 10
   * @param classify    分类键模糊
   * @param classifyName 分类名称模糊
   * @param status      启用状态
   * @return 分类分页
   */
  @Operation(summary = "分页查询分类")
  @SaCheckPermission("system:fileClassify:list")
  @GetMapping("/list")
  public R<PageInfo<SysFileClassifyVo>> list(
    @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
    @RequestParam(value = "classify", required = false) String classify,
    @RequestParam(value = "classifyName", required = false) String classifyName,
    @RequestParam(value = "status", required = false) String status
  ) {
    SysFileClassifyVo param = new SysFileClassifyVo();
    param.setClassify(classify);
    param.setClassifyName(classifyName);
    param.setStatus(status);
    int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
    int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
    return R.ok(sysFileClassifyService.page(new PageRequest<>(current, size, param)));
  }

  /**
   * 详情。
   *
   * @param id 分类主键
   * @return 分类 Vo
   */
  @Operation(summary = "分类详情")
  @SaCheckPermission(value = {"system:fileClassify:query", "system:fileClassify:list"}, mode = SaMode.OR)
  @GetMapping("/{id}")
  public R<SysFileClassifyVo> get(@Parameter(description = "主键") @PathVariable("id") Long id) {
    return R.ok(sysFileClassifyService.getDetail(id));
  }

  /**
   * 新增。
   *
   * @param vo 可写字段（含 classify 键）
   * @return 新建主键
   */
  @Operation(summary = "新增分类")
  @SaCheckPermission("system:fileClassify:add")
  @PostMapping({"", "/add"})
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysFileClassifyVo vo) {
    Long id = sysFileClassifyService.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改（不可改 classify 键）。
   *
   * @param vo 含主键的可写字段
   * @return 是否成功
   */
  @Operation(summary = "修改分类")
  @SaCheckPermission("system:fileClassify:edit")
  @PostMapping("/update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysFileClassifyVo vo) {
    return R.ok(sysFileClassifyService.update(vo));
  }

  /**
   * 批量删除。
   *
   * @param ids 分类主键数组
   * @return ok
   */
  @Operation(summary = "删除分类")
  @SaCheckPermission("system:fileClassify:remove")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody Long[] ids) {
    List<Long> idList = ids == null ? List.of() : Arrays.asList(ids);
    sysFileClassifyService.remove(idList);
    return R.ok();
  }
}
