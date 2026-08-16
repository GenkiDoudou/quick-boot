package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存 H5 首页快捷偏好请求体。
 * <p>{@code menuIds} 为空表示清除偏好并恢复系统默认。</p>
 */
@Data
public class H5HomeShortcutSaveVo {

  /** 有序菜单 id（字符串）；最多 8；空=恢复默认 */
  private List<String> menuIds = new ArrayList<>();
}
