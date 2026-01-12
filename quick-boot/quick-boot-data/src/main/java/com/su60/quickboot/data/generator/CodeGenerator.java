package com.su60.quickboot.data.generator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.su60.quickboot.data.generator.query.DbType;
import com.su60.quickboot.data.generator.query.GeneratorInterceptor;
import com.su60.quickboot.data.generator.query.ITableQuery;
import com.su60.quickboot.data.generator.query.MysqlTableQuery;
import com.su60.quickboot.data.utils.YmlUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import freemarker.template.Template;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 代码生成
 *
 * @author luyanan
 * @since 2024/06/23
 **/
public class CodeGenerator {

	/**
	 * 数据源
	 *
	 * @since 2024/06/23
	 */

	private DataSource dataSource;


	/**
	 * 模板列表
	 *
	 * @since 2024/06/24
	 */

	private List<CodeTemplate> templateList = new ArrayList<>();


	/**
	 * 代码模板渲染之后的结果
	 *
	 * @since 2024/06/30
	 */

	private List<CodeTemplateResult> codeTemplateResults = new ArrayList<>();
	/**
	 * 字段类型转换
	 *
	 * @since 2024/06/25
	 */

	private Map<String, FieldType> fieldTypeMap = new HashMap<>();


	/**
	 * 表信息
	 *
	 * @since 2024/06/25
	 */

	private List<TableInfo> tableInfos = new ArrayList<>();


	/**
	 * 包含的表
	 *
	 * @since 2024/06/26
	 */

	private Set<String> include = new HashSet<>();


	/**
	 * 表前缀过滤
	 *
	 * @since 2024/06/26
	 */

	private List<String> tablePrefixList = new ArrayList<>();


	/**
	 * 移除表前缀
	 *
	 * @since 2024/06/26
	 */

	private List<String> removeTablePrefixList = new ArrayList<>();


	/**
	 * 作者
	 *
	 * @since 2024/06/26
	 */

	private String author;

	/**
	 * 父包路径
	 *
	 * @since 2024/06/26
	 */

	private String parentPackage;

	/**
	 * 模块名称
	 *
	 * @since 2024/06/26
	 */

	private String moduleName;


	public CodeGenerator(DataSource dataSource) {
		init();
		this.dataSource = dataSource;
	}


	/**
	 * 创建
	 *
	 * @param dataSource 数据源
	 * @return 代码生成器
	 * @since 2024/06/23
	 */
	public static CodeGenerator create(DataSource dataSource) {
		return new CodeGenerator(dataSource);
	}


	/**
	 * 设置作者
	 *
	 * @param author 作者
	 * @return 代码生成器
	 * @since 2024/06/26
	 */
	public CodeGenerator author(String author) {
		this.author = author;
		return this;
	}

	/**
	 * 设置父包路径
	 *
	 * @param parentPackage 父包路径
	 * @return 代码生成器
	 * @since 2024/06/26
	 */
	public CodeGenerator parentPackage(String parentPackage) {
		this.parentPackage = parentPackage;
		return this;
	}

	/**
	 * 设置模块名称
	 *
	 * @param moduleName 模块名称
	 * @return 代码生成器
	 * @since 2024/06/26
	 */
	public CodeGenerator moduleName(String moduleName) {
		this.moduleName = moduleName;
		return this;
	}


	/**
	 * 读取yml配置文件饼并创建
	 *
	 * @param ymlPath yml配置文件
	 * @return 代码生成器
	 * @since 2024/06/24
	 */
	public static CodeGenerator create(String ymlPath) {
		Map<String, String> configs = YmlUtils.getYmlByFileName(ymlPath);
		String url = configs.get("spring.datasource.url");
		String username = configs.get("spring.datasource.username");
		String password = configs.get("spring.datasource.password");
		return CodeGenerator.create(url, username, password);

	}

	/**
	 * 创建
	 *
	 * @param url      连接地址
	 * @param username 用户名
	 * @param password 密码
	 * @return 代码生成器
	 * @since 2024/06/23
	 */
	public static CodeGenerator create(String url, String username, String password) {
		DataSource dataSource = new SimpleDataSource(url, username, password);

		return new CodeGenerator(dataSource);
	}

	/**
	 * 初始化
	 *
	 * @since 2024/06/26
	 */
	public void init() {

		this.defaultFieldTypeConversion();
	}


	/**
	 * 添加包含的表
	 *
	 * @param include 包含的表
	 * @return 代码生成器
	 * @since 2024/06/26
	 */

	public CodeGenerator addInclude(String... include) {
		this.include.addAll(Arrays.asList(include));
		return this;
	}

	/**
	 * 增加过滤表前缀
	 *
	 * @param tablePrefix 过滤表前缀
	 * @return 代码生成器
	 * @since 2024/06/26
	 */
	public CodeGenerator addTablePrefix(String... tablePrefix) {

		this.tablePrefixList.addAll(Arrays.asList(tablePrefix));
		return this;
	}


	/**
	 * 增加过滤表前缀
	 *
	 * @param tablePrefix 过滤表前缀
	 * @return 代码生成器
	 * @since 2024/06/26
	 */
	public CodeGenerator addRemoveTablePrefix(String... tablePrefix) {

		if (tablePrefix.length > 0) {
			this.removeTablePrefixList.addAll(Arrays.asList(tablePrefix));
		}
		return this;
	}


	/**
	 * 注册模板
	 *
	 * @param codeTemplates 模板配置
	 * @return 代码生成器
	 * @since 2024/06/24
	 */
	public CodeGenerator registerTemplate(List<CodeTemplate> codeTemplates) {

		if (CollectionUtil.isEmpty(codeTemplates)) {
			this.templateList.addAll(codeTemplates);
		}
		return this;
	}

	/**
	 * 注册模板
	 *
	 * @param codeTemplates listConsumer
	 * @return 代码生成器
	 * @since 2024/06/24
	 */
	public CodeGenerator registerTemplate(Consumer<List<CodeTemplate>> codeTemplates) {
		codeTemplates.accept(this.templateList);
		return this;
	}


	/**
	 * 注册字段类型
	 *
	 * @param fieldTypes 字段类型
	 * @return 代码生成器
	 * @since 2024/06/25
	 */
	public CodeGenerator registerFieldType(List<FieldType> fieldTypes) {
		if (CollectionUtil.isEmpty(fieldTypes)) {
			return this;
		}
		for (FieldType fieldType : fieldTypes) {
			this.fieldTypeMap.remove(fieldType.getColumnType());
			this.fieldTypeMap.put(fieldType.getColumnType(), fieldType);
		}
		return this;
	}

	/**
	 * 注册字段类型
	 *
	 * @param consumer 字段类型
	 * @return 代码生成器
	 * @since 2024/06/25
	 */
	public CodeGenerator registerFieldType(Consumer<Map<String, FieldType>> consumer) {
		consumer.accept(this.fieldTypeMap);
		return this;
	}

	/**
	 * 执行代码生成操作
	 *
	 * @param tableInfos 表信息
	 * @return 代码生成器
	 * @since 2024/06/25
	 */
	public ResultHandler execute(List<TableInfo> tableInfos) {

		for (TableInfo tableInfo : tableInfos) {
			execute(tableInfo);
		}
		return new ResultHandler(this.codeTemplateResults);
	}

	/**
	 * 执行
	 *
	 * @param tableInfo 表信息
	 * @since 2024/06/28
	 */
	private void execute(TableInfo tableInfo) {
		List<TableField> tableFields = tableInfo.getTableFields();
		Set<String> importPackage = new HashSet<>();
		for (TableField tableField : tableFields) {
			FieldType fieldType = this.fieldTypeMap.get(tableField.getColumnType());
//			if (null == fieldType) {
//				throw new NullPointerException(tableInfo.getTableName() + "表的字段:" + tableField.getColumnName() + "的类型为:" + tableField.getColumnType() + ",请添加类型转换");
//			}
//			tableField.setAttrName(NamingCase.toCamelCase(tableField.getColumnName()));
//			tableField.setAttrType(fieldType.getAttrType());
			if (StrUtil.isNotBlank(fieldType.getPackageName())) {
				importPackage.add(fieldType.getPackageName());
			}
		}
		for (CodeTemplate codeTemplate : this.templateList) {

			CodeTemplateResult codeTemplateResult = new CodeTemplateResult();
			String packagePath = this.parentPackage + "." + this.moduleName;


//			Map<String, Object> map = new HashMap<>();
//			map.put("author", this.author);
//			map.put("date", DateUtil.date().toString("yyyy/MM/dd"));
//			map.put("parentPackage", parentPackage);
//			map.put("package", packagePath);
//			map.put("moduleName", moduleName);
//			map.put("tableComment", tableInfo.getTableComment());
//			map.put("tableName", tableInfo.getTableName());
//			map.put("className", tableInfo.getClassName());
//			map.put("classNameLower", tableInfo.getClassName().toLowerCase());
//			map.put("tableFields", tableFields);
//			map.put("importPackage", importPackage);
//			map.put("templateName", tableInfo.getClassName());

			DataMap dataMap = new DataMap()
					.setAuthor(this.author)
					.setDate(DateUtil.date().toString("yyyy/MM/dd"))
					.setParentPackage(parentPackage)
					.setPackag(packagePath)
					.setModuleName(moduleName)
					.setTableComment(tableInfo.getTableComment())
					.setTableName(tableInfo.getTableName())
					.setClassName(tableInfo.getClassName())
					.setClassNameLower(tableInfo.getClassName().toLowerCase())
					.setTableFields(tableFields)
					.setImportPackage(importPackage)
					.setTemplateName(tableInfo.getClassName());

			// 主键字段
			TableField tableField = tableFields.stream().filter(a -> a.getIsPk().equals("0")).findFirst().orElse(null);
			dataMap.put("keyTableField", tableField);

			dataMap.put("keyField", Optional.ofNullable(tableField).map(TableField::getAttrName).stream().findFirst().orElse(null));

			// 主键类型
			dataMap.put("keyFieldType", Optional.ofNullable(tableField).map(TableField::getAttrType).stream().findFirst().orElse(null));
			String savePath = null;
			// savePath
			if (StrUtil.isBlank(codeTemplate.getPackagePath())) {
				//  xml/view文件,则文件路径为 savePath+module
				// 将Map中的内容对savePath 进行替换

//				codeTemplate.setSavePath(codeTemplate.getSavePath().replace("${module}", this.moduleName));

				savePath = StrUtil.format(codeTemplate.getSavePath(), dataMap);
//				savePath = codeTemplate.getSavePath() + "/" + this.moduleName;
			} else {
				savePath = codeTemplate.getSavePath() + "/" + (packagePath + "." + codeTemplate.getPackagePath()).replace(".", "/");
			}

			codeTemplateResult.setSavePath(savePath);

			codeTemplateResult.setFileName(StrUtil.format(codeTemplate.getFileName(), dataMap));
			codeTemplate.setParentPackagePath(packagePath);


//			codeTemplateResult.setFileName(String.format(codeTemplate.getFileName(), tableInfo.getClassName()));

			// 拦截器
			GeneratorInterceptor interceptor = codeTemplate.getInterceptor();
			if (null != interceptor) {
				interceptor.execute(tableInfo, dataMap);
			}
			String templateContent = codeTemplate.getTemplateContent();
			if (StrUtil.isBlank(templateContent)) {

				ClassPathResource resource = new ClassPathResource(codeTemplate.getTemplateFilePath());
				try {
					byte[] content = new byte[(int) resource.contentLength()];
					try (InputStream is = resource.getInputStream()) {
						is.read(content);
					}
					templateContent = new String(content, StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
			String content = getTemplateContent(templateContent, dataMap, codeTemplateResult.getFileName());
			codeTemplateResult.setResult(content);
			this.codeTemplateResults.add(codeTemplateResult);
//			codeTemplate.setTemplateViewContent(content);
			System.out.println("表:" + tableInfo.getTableName() + " 生成文件:" + codeTemplate.getSavePath() + "/" + codeTemplateResult.getFileName());
		}
	}

	/**
	 * 获取模板渲染后的内容
	 *
	 * @param content          模板内容
	 * @param dataModel        数据模型
	 * @param templateFilePath 模板文件路径
	 * @return 文件渲染之后的内容
	 * @since 2024/06/26
	 */

	public String getTemplateContent(String content, Map<String, Object> dataModel, String templateFilePath) {

		if (CollectionUtil.isEmpty(dataModel)) {
			return content;
		}
		StringReader reader = new StringReader(content);
		StringWriter sw = new StringWriter();
		String templateName = dataModel.getOrDefault("templateName", "generator").toString();
		try {
			Template template = new Template(templateName, reader, null, "utf-8");
			template.process(dataModel, sw);
		} catch (Exception e) {
			System.out.println("模板渲染失败:" + templateFilePath);
			throw new RuntimeException(e);
		}

		return sw.toString();
	}

	/**
	 * 执行代码生成操作
	 *
	 * @return 执行结果类
	 * @since 2024/06/25
	 */
	public ResultHandler execute() {
		List<TableInfo> infoList = getTableInfo();
		return execute(infoList);
	}

	/**
	 * 结果执行
	 *
	 * @author luyanan
	 * @since 2024/06/28
	 */
	public class ResultHandler {

		/**
		 * 代码模板执行
		 *
		 * @since 2024/06/28
		 */

		private List<CodeTemplateResult> codeTemplateResults;

		public ResultHandler(List<CodeTemplateResult> codeTemplateResults) {
			this.codeTemplateResults = codeTemplateResults;
		}

		/**
		 * 保存成文件
		 *
		 * @param path 文件路径
		 * @return
		 * @since 2024/06/25
		 */
		public void saveFile(String path) {

			for (CodeTemplateResult codeTemplate : this.codeTemplateResults) {
				String templateViewContent = codeTemplate.getResult();
				String fileName = codeTemplate.getFileName();
//				String savePath = codeTemplate.getSavePath();
//				String packagePath = codeTemplate.getPackagePath();
//				String filePath = null;
//				if (StrUtil.isNotBlank(packagePath)) {
//					filePath = path + "/" + codeTemplate.getSavePath() + "/" + codeTemplate.getParentPackagePath();
//					filePath = filePath.replace(".", "/");
//				} else {
//					filePath = path + "/" + savePath + "/";
//				}
				String templateSavePath = path + "/" + codeTemplate.getSavePath();
				FileUtil.mkdir(templateSavePath);
				FileUtil.writeString(templateViewContent, templateSavePath + "/" + fileName, "utf-8");
			}
		}


		/**
		 * 保存成内容
		 *
		 * @return 内容
		 * @since 2024/06/25
		 */
		public List<SaveStrVo> saveStr() {
			return this.codeTemplateResults.stream().map(a -> new SaveStrVo()
					.setContent(a.getResult())
					.setSavePath(a.getSavePath())
					.setFileName(a.getFileName())).collect(Collectors.toList());
		}
	}

	/**
	 * 保存vo类
	 *
	 * @author luyanan
	 * @since 2024/11/12
	 */

	@Accessors(chain = true)
	@Data
	public static class SaveStrVo {
		/**
		 * 文件名
		 *
		 * @since 2024/11/7
		 */

		private String fileName;


		/**
		 * 保存路径
		 *
		 * @since 2024/11/7
		 */

		private String savePath;


		/**
		 * 内容
		 *
		 * @since 2024/11/7
		 */

		private String content;
	}

	/**
	 * 查询表字段信息
	 *
	 * @param tableName 表名
	 * @return 字段信息
	 * @since 2024/06/26
	 */
	public List<TableField> getTableField(String tableName) {
		List<TableField> tableFields = new ArrayList<>();
		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			String url = connection.getMetaData().getURL();
			ITableQuery tableQuery = matchTableQuery(url);
			PreparedStatement preparedStatement = connection.prepareStatement(tableQuery.tableFieldsSql(tableName));
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				TableField tableField = new TableField();
				tableField.setColumnName(rs.getString(tableQuery.fieldName()));
				String columnType = rs.getString(tableQuery.fieldType());
				if (columnType.indexOf(" ") != -1) {
					columnType = columnType.substring(0, columnType.indexOf(" "));
				}
				tableField.setColumnType(columnType);
				tableField.setColumnComment(rs.getString(tableQuery.fieldComment()));
				String key = rs.getString(tableQuery.fieldKey());
				tableField.setIsPk(StringUtils.isNotBlank(key) && "PRI".equals(key.toUpperCase()) ? "0" : "1");
				// 是否必填
				tableField.setIsRequired(rs.getString(tableQuery.fieldIsNullable()).equals("NO") ? "0" : "1");
				tableFields.add(tableField);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return tableFields;
	}

	/**
	 * 根据数据库连接地址匹配表查询器
	 *
	 * @param url 数据库url
	 * @return 查询器
	 * @since 2024/06/26
	 */
	private ITableQuery matchTableQuery(String url) {
		DbType dbType = DbType.getDbTypeByUrl(url);
		ITableQuery tableQuery = null;
		switch (dbType) {
			case MYSQL:
				tableQuery = new MysqlTableQuery();
				break;
			default:
				break;

		}
		return tableQuery;
	}

	/**
	 * 获取表的信息
	 *
	 * @return 表信息
	 * @since 2024/06/25
	 */
	public List<TableInfo> getTableInfo() {
		List<TableInfo> tableInfos = new ArrayList<>();
		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
			String url = connection.getMetaData().getURL();
			ITableQuery tableQuery = matchTableQuery(url);
			String sql = tableQuery.tablesSql(null);
			//查询数据
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				TableInfo tableInfo = new TableInfo();
				tableInfo.setTableName(rs.getString(tableQuery.tableName()));
				tableInfo.setTableComment(rs.getString(tableQuery.tableComment()));
				// 转类名
				tableInfo.setClassName(tableNameToClassName(tableInfo.getTableName()));
				tableInfos.add(tableInfo);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}

		List<TableInfo> tableInfoList = tableInfos.stream().filter(a -> matchTable(a.getTableName(), this.include)).collect(Collectors.toList());
		// 查询表字段信息
		for (TableInfo tableInfo : tableInfoList) {
			List<TableField> tableField = this.getTableField(tableInfo.getTableName());
			// tableField 字段填充
			for (TableField field : tableField) {
				FieldType fieldType = this.fieldTypeMap.get(field.getColumnType());
				if (null == fieldType) {
					throw new NullPointerException(tableInfo.getTableName() + "表的字段:" + field.getColumnName() + "的类型为:" + field.getColumnType() + ",请添加类型转换");
				}
				field.setAttrName(NamingCase.toCamelCase(field.getColumnName()));
				field.setAttrType(fieldType.getAttrType());
			}

			tableInfo.setTableFields(tableField);
		}
		return tableInfoList;
	}


	/**
	 * 表名转换为类名
	 *
	 * @param tableName 表数据表名
	 * @return 类名
	 * @since 2024/06/26
	 */
	private String tableNameToClassName(String tableName) {
		if (CollectionUtil.isNotEmpty(this.removeTablePrefixList)) {

			String finalTableName = tableName;
			String prefix = this.removeTablePrefixList.stream().filter(a -> finalTableName.startsWith(a)).findFirst().orElse(null);
			if (StrUtil.isNotBlank(prefix)) {
				tableName = StrUtil.replaceFirst(tableName, prefix, "");
			}
		}
		return NamingCase.toPascalCase(tableName);

	}

	/**
	 * 表名匹配
	 *
	 * @param tableName   表名
	 * @param matchTables 匹配集合
	 * @return 是否匹配
	 * @since 2024/06/26
	 */
	private boolean matchTable(String tableName, Set<String> matchTables) {
		if (CollectionUtil.isEmpty(matchTables)) {
			return true;
		}
		return matchTables.stream().anyMatch(t -> tableNameMatches(t, tableName));
	}

	/**
	 * 表名匹配
	 *
	 * @param matchTableName 匹配表名
	 * @param dbTableName    数据库表名
	 * @return 是否匹配
	 * @since 2024/06/26
	 */
	private boolean tableNameMatches(String matchTableName, String dbTableName) {
		return matchTableName.equalsIgnoreCase(dbTableName) || StringUtils.matches(matchTableName, dbTableName);
	}


	/**
	 * 默认的字段类型转换
	 *
	 * @since 2024/06/26
	 */
	public void defaultFieldTypeConversion() {
		Map<String, FieldType> map = new HashMap<>();
		map.put("datetime", new FieldType().setAttrType("Date").setPackageName("java.util.Date"));
		map.put("date", new FieldType().setAttrType("Date").setPackageName("java.util.Date"));
		map.put("tinyint", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("smallint", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("mediumint", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("int", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("integer", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("bigint", new FieldType().setAttrType("Long").setPackageName(""));
		map.put("float", new FieldType().setAttrType("Float").setPackageName(""));
		map.put("double", new FieldType().setAttrType("Double").setPackageName(""));
		map.put("decimal", new FieldType().setAttrType("BigDecimal").setPackageName("java.math.BigDecimal"));
		map.put("bit", new FieldType().setAttrType("Boolean").setPackageName(""));
		map.put("char", new FieldType().setAttrType("String").setPackageName(""));
		map.put("varchar", new FieldType().setAttrType("String").setPackageName(""));
		map.put("tinytext", new FieldType().setAttrType("String").setPackageName(""));
		map.put("text", new FieldType().setAttrType("String").setPackageName(""));
		map.put("mediumtext", new FieldType().setAttrType("String").setPackageName(""));
		map.put("longtext", new FieldType().setAttrType("String").setPackageName(""));
		map.put("timestamp", new FieldType().setAttrType("Date").setPackageName("java.util.Date"));
		map.put("NUMBER", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("BINARY_INTEGER", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("BINARY_FLOAT", new FieldType().setAttrType("Float").setPackageName(""));
		map.put("BINARY_DOUBLE", new FieldType().setAttrType("Double").setPackageName(""));
		map.put("VARCHAR2", new FieldType().setAttrType("String").setPackageName(""));
		map.put("NVARCHAR", new FieldType().setAttrType("String").setPackageName(""));
		map.put("NVARCHAR2", new FieldType().setAttrType("String").setPackageName(""));
		map.put("CLOB", new FieldType().setAttrType("String").setPackageName(""));
		map.put("json", new FieldType().setAttrType("String").setPackageName(""));
		map.put("blob", new FieldType().setAttrType("String").setPackageName(""));
		map.put("int8", new FieldType().setAttrType("Long").setPackageName(""));
		map.put("int4", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("int2", new FieldType().setAttrType("Integer").setPackageName(""));
		map.put("numeric", new FieldType().setAttrType("BigDecimal").setPackageName("java.math.BigDecimal"));
		map.put("longblob", new FieldType().setAttrType("String").setPackageName(""));
		this.fieldTypeMap.putAll(map);
	}


	/**
	 * 模板渲染结果
	 *
	 * @since 2024/06/30
	 */

	@Data
	@Accessors(chain = true)
	public class CodeTemplateResult {

		/**
		 * 文件名
		 *
		 * @since 2024/06/30
		 */

		private String fileName;


		/**
		 * 保存路径
		 *
		 * @since 2024/06/30
		 */

		private String savePath;

		/**
		 * 结果
		 *
		 * @since 2024/06/30
		 */

		private String result;
	}

}
