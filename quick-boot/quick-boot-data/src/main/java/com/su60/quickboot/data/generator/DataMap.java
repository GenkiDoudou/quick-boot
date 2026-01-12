package com.su60.quickboot.data.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

/**
 * 数据map
 *
 * @author luyanan
 * @since 2024/10/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataMap extends Dict {


	/**
	 * 作者
	 *
	 * @since 2024/10/28
	 */

	private String author;


	/**
	 * 作者
	 *
	 * @return 作者
	 * @since 2024/10/28
	 */
	public String getAuthor() {
		return MapUtil.getStr(this, "author");
	}

	/**
	 * 设置作者
	 *
	 * @param author 作者
	 * @return DataMap
	 * @author luyanan
	 * @since 2024/10/28
	 */
	public DataMap setAuthor(String author) {
		super.put("author", author);
		return this;
	}

	/**
	 * 日期
	 *
	 * @since 2024/10/28
	 */

	private String date;


	/**
	 * 获取日期
	 *
	 * @return 日期
	 * @author luyanan
	 * @since 2024/10/28
	 */
	public String getDate() {
		return MapUtil.getStr(this, "date");
	}

	/**
	 * 设置日期
	 *
	 * @param date 日期
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setDate(String date) {
		super.put("date", date);
		return this;
	}


	/**
	 * 父包路径
	 *
	 * @since 2024/10/28
	 */

	private String parentPackage;

	/**
	 * 父包路径
	 *
	 * @return 父包路径
	 * @since 2024/10/28
	 */
	public String getParentPackage() {
		return MapUtil.getStr(this, "parentPackage");
	}

	/**
	 * 设置父包路径
	 *
	 * @param parentPackage 父包路径
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setParentPackage(String parentPackage) {
		super.put("parentPackage", parentPackage);
		return this;
	}


	/**
	 * 包路径
	 *
	 * @since 2024/10/28
	 */

	private String packag;

	/**
	 * 获取包路径
	 *
	 * @return 获取包路径
	 * @author luyanan
	 * @since 2024/10/28
	 */
	public String getPackag() {
		return MapUtil.getStr(this, "packag");
	}

	/**
	 * 设置包路径
	 *
	 * @param packag 包路径
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setPackag(String packag) {
		super.put("packag", packag);
		return this;
	}


	/**
	 * 模块名
	 *
	 * @since 2024/10/28
	 */

	private String moduleName;

	/**
	 * 获取模块名
	 *
	 * @return 模块名
	 * @since 2024/10/28
	 */
	public String getModuleName() {
		return MapUtil.getStr(this, "moduleName");
	}

	/**
	 * 设置模块名
	 *
	 * @param moduleName 模块名
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setModuleName(String moduleName) {
		super.put("moduleName", moduleName);
		return this;
	}

	/**
	 * 表注释
	 *
	 * @since 2024/10/28
	 */

	private String tableComment;

	/**
	 * 获取表注释
	 *
	 * @return 获取表注释
	 * @since 2024/10/28
	 */
	public String getTableComment() {
		return MapUtil.getStr(this, "tableComment");
	}

	/**
	 * 设置表注释
	 *
	 * @param tableComment 表注释
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setTableComment(String tableComment) {
		super.put("tableComment", tableComment);
		return this;
	}

	/**
	 * 表名称
	 *
	 * @since 2024/10/28
	 */

	private String tableName;

	/**
	 * 获取表名称
	 *
	 * @return 获取表名称
	 * @since 2024/10/28
	 */
	public String getTableName() {
		return MapUtil.getStr(this, "tableName");
	}

	/**
	 * 设置表名称
	 *
	 * @param tableName 表名称
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setTableName(String tableName) {
		super.put("tableName", tableName);
		return this;
	}

	/**
	 * 类名称
	 *
	 * @since 2024/10/28
	 */

	private String className;

	/**
	 * 获取类名称
	 *
	 * @return 类名称
	 * @since 2024/10/28
	 */
	public String getClassName() {
		return MapUtil.getStr(this, "className");
	}

	/**
	 * 设置类名
	 *
	 * @param className 类名称
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setClassName(String className) {
		super.put("className", className);
		return this;
	}


	/**
	 * 类名全小写
	 *
	 * @since 2024/10/28
	 */

	private String classNameLower;

	/**
	 * 获取小写类名
	 *
	 * @return 小写类名
	 * @since 2024/10/28
	 */
	public String getClassNameLower() {
		return MapUtil.getStr(this, "classNameLower");
	}

	/**
	 * 设置小写类名
	 *
	 * @param classNameLower 小写类名
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setClassNameLower(String classNameLower) {
		super.put("classNameLower", classNameLower);
		return this;
	}

	/**
	 * 表字段列表
	 *
	 * @since 2024/10/28
	 */

	private List<TableField> tableFields;

	/**
	 * 获取表字段配置
	 *
	 * @return 表字段配置
	 * @since 2024/10/28
	 */
	public List<TableField> getTableFields() {
		return MapUtil.get(this, "tableFields", new TypeReference<List<TableField>>() {
		});
	}

	/**
	 * 设置表字段
	 *
	 * @param tableFields 表字段
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setTableFields(List<TableField> tableFields) {
		super.put("tableFields", tableFields);
		return this;
	}


	/**
	 * 导入package
	 *
	 * @since 2024/10/28
	 */

	private Set<String> importPackage;

	/**
	 * 获取导入package
	 *
	 * @return 导入package
	 * @since 2024/10/28
	 */
	public Set<String> getImportPackage() {
		return MapUtil.get(this, "importPackage", new TypeReference<Set<String>>() {
		});
	}

	/**
	 * 设置 导入package
	 *
	 * @param importPackage 导入package
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap addImportPackage(String... importPackage) {
		Set<String> importPackages = getImportPackage();
		importPackages.addAll(Set.of(importPackage));
		return this;
	}

	/**
	 * 设置 导入package
	 *
	 * @param importPackage 导入package
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setImportPackage(Set<String> importPackage) {
		super.put("importPackage", importPackage);
		return this;
	}

	/**
	 * 模板名
	 *
	 * @since 2024/10/28
	 */

	private String templateName;

	/**
	 * 获取模板名
	 *
	 * @return 模板名
	 * @since 2024/10/28
	 */
	public String getTemplateName() {
		return MapUtil.getStr(this, "templateName");
	}

	/**
	 * 设置模板名
	 *
	 * @param templateName 模板名称
	 * @return DataMap
	 * @since 2024/10/28
	 */
	public DataMap setTemplateName(String templateName) {
		super.put("templateName", templateName);
		return this;
	}



}
