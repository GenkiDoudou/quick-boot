package com.su60.quickboot.system.dos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TreeSelect树结构实体类
 *
 * @author luyanan
 * @since 2024/8/20
 */
@Data
@Accessors(chain = true)
public class TreeSelect implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 节点id
	 *
	 * @since 2024/8/20
	 */

	private Long id;


	/**
	 * 节点名称
	 *
	 * @since 2024/8/20
	 */

	private String label;


	/**
	 * 子节点
	 *
	 * @since 2024/8/20
	 */

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<TreeSelect> children;


	public TreeSelect(SysMenuDo sysMenuDo) {
		this.id = sysMenuDo.getId();
		this.label = sysMenuDo.getMenuName();
		this.children = sysMenuDo.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
	}
}
