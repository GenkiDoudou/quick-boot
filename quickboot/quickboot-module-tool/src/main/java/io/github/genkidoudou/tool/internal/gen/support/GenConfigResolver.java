package io.github.genkidoudou.tool.internal.gen.support;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.config.ConfigValueLookup;
import io.github.genkidoudou.tool.internal.gen.config.GenProperties;
import io.github.genkidoudou.tool.internal.gen.dto.GenDefaultsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 代码生成默认项：优先 {@code sys_config}（qc.gen.*），回退 {@link GenProperties}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenConfigResolver {

  static final String KEY_AUTHOR = "qc.gen.author";
  static final String KEY_PACKAGE_NAME = "qc.gen.package-name";
  static final String KEY_MODULE_NAME = "qc.gen.module-name";
  static final String KEY_TPL_CATEGORY = "qc.gen.tpl-category";
  static final String KEY_PARENT_MENU_ID = "qc.gen.parent-menu-id";

  private final ObjectProvider<ConfigValueLookup> configValueLookup;
  private final GenProperties genProperties;

  /**
   * 供前端编辑页与导入默认值使用。
   */
  public GenDefaultsVo resolveDefaults() {
    GenDefaultsVo vo = new GenDefaultsVo();
    vo.setPackageName(getPackageName());
    vo.setModuleName(getModuleName());
    vo.setFunctionAuthor(getAuthor());
    vo.setTplCategory(getTplCategory());
    vo.setParentMenuId(getParentMenuId());
    return vo;
  }

  /** 生成代码注释作者，优先 sys_config。 */
  public String getAuthor() {
    return firstNonBlank(config(KEY_AUTHOR), genProperties.getAuthor());
  }

  /** Java 根包名，优先 sys_config。 */
  public String getPackageName() {
    return firstNonBlank(config(KEY_PACKAGE_NAME), genProperties.getPackageName());
  }

  /** 模块名（前端路径 / 权限前缀），优先 sys_config。 */
  public String getModuleName() {
    return firstNonBlank(config(KEY_MODULE_NAME), genProperties.getModuleName());
  }

  /** 模板类型，默认 crud。 */
  public String getTplCategory() {
    return firstNonBlank(config(KEY_TPL_CATEGORY), "crud");
  }

  /** 生成菜单的上级菜单 ID，未配置时返回 {@code null}。 */
  public Long getParentMenuId() {
    String raw = config(KEY_PARENT_MENU_ID);
    if (StrUtil.isNotBlank(raw) && NumberUtil.isLong(raw.trim())) {
      return Long.parseLong(raw.trim());
    }
    return null;
  }

  private String config(String key) {
    try {
      ConfigValueLookup lookup = configValueLookup.getIfAvailable();
      if (lookup == null) {
        return null;
      }
      return lookup.getConfigValueByKey(key);
    } catch (Exception ex) {
      log.warn("读取参数 {} 失败，使用 yml 回退: {}", key, ex.getMessage());
      return null;
    }
  }

  private static String firstNonBlank(String preferred, String fallback) {
    if (StrUtil.isNotBlank(preferred)) {
      return preferred.trim();
    }
    return StrUtil.blankToDefault(fallback, "");
  }
}
