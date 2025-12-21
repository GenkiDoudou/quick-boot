package com.su60.quickboot.data.excel;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.apache.ibatis.cursor.Cursor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.core.io.ClassPathResource;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ExcelUtils2
 *
 * ===========================
 * 一个【单类、自包含】Excel 导出引擎
 * ===========================
 *
 * 特点：
 * 1. 支持 Cursor / 分页 导出
 * 2. 支持 batchSize 全局 + Sheet 级配置
 * 3. 支持 Excel 模板
 * 4. 支持多 Sheet
 * 5. 支持 Builder 链式 API
 *
 * ❌ 不做导出限流
 * ❌ 不依赖 Spring Bean（仅 ClassPathResource）
 */
public class ExcelUtils2 {

	/* =========================================================
	 * ======================= Builder 入口 ====================
	 * ========================================================= */

	/** Workbook Builder 入口 */
	public static ExcelBuilder builder(HttpServletResponse response) {
		return new ExcelBuilder(response);
	}

	/** Sheet Builder 入口 */
	public static <T> SheetBuilder<T> sheet(Class<T> modelClass) {
		return new SheetBuilder<>(modelClass);
	}

	/* =========================================================
	 * ======================= Builder 实现 ====================
	 * ========================================================= */

	/**
	 * ExcelBuilder
	 * Workbook 级配置
	 */
	public static class ExcelBuilder {

		private final HttpServletResponse response;

		private final List<SheetExportSpec<?>> sheets = new ArrayList<>();

		/** 全局默认 batchSize */
		private int defaultBatchSize = 500;

		ExcelBuilder(HttpServletResponse response) {
			this.response = response;
		}

		/** 设置全局 batchSize */
		public ExcelBuilder defaultBatchSize(int batchSize) {
			this.defaultBatchSize = batchSize;
			return this;
		}

		/** 添加 Sheet */
		public ExcelBuilder addSheet(SheetExportSpec<?> sheet) {
			this.sheets.add(sheet);
			return this;
		}

		/** 执行导出 */
		public void export(String fileName) throws Exception {
			exportInternal(response, sheets, defaultBatchSize, fileName);
		}

		/** 执行导出 */
		public void export() throws Exception {
			export(null);
		}
	}

	/**
	 * SheetBuilder
	 * 单 Sheet 链式配置
	 */
	public static class SheetBuilder<T> {

		private String sheetName = "Sheet1";
		private final Class<T> modelClass;
		private BatchProvider<T> provider;
		private Integer batchSize;
		private String templatePath;

		SheetBuilder(Class<T> modelClass) {
			this.modelClass = modelClass;
		}

		/** Sheet 名称 */
		public SheetBuilder<T> name(String name) {
			this.sheetName = name;
			return this;
		}

		/** Cursor 数据源 */
		public SheetBuilder<T> cursor(Cursor<T> cursor) {
			this.provider = new CursorBatchProvider<>(cursor);
			return this;
		}

		/** 分页数据源 */
		public SheetBuilder<T> page(Function<Integer, List<T>> loader) {
			this.provider = new PageBatchProvider<>(loader);
			return this;
		}


		/** 分页数据源 */
		public SheetBuilder<T> list(List<T> loader) {
			this.provider = new ListProvider<T>(loader);
			return this;
		}


		/** 单 Sheet batchSize */
		public SheetBuilder<T> batchSize(int batchSize) {
			this.batchSize = batchSize;
			return this;
		}

		/** 使用模板（classpath） */
		public SheetBuilder<T> template(String templatePath) {
			this.templatePath = templatePath;
			return this;
		}

		/** 构建 SheetExportSpec */
		public SheetExportSpec<T> build() {
			if (provider == null) {
				throw new IllegalStateException(
						"Sheet [" + sheetName + "] 未配置数据源"
				);
			}
			return new SheetExportSpec<>(
					sheetName,
					modelClass,
					provider,
					batchSize,
					templatePath
			);
		}
	}

	/* =========================================================
	 * ======================= 导出内核 ========================
	 * ========================================================= */

	private static void exportInternal(HttpServletResponse response,
									   List<SheetExportSpec<?>> sheets,
									   int defaultBatchSize, String fileName) throws Exception {
		if (StrUtil.isBlank(fileName)) {
			fileName = DateUtil.date().toString();
		}
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("UTF-8");

		fileName = URLUtil.encode(fileName, StandardCharsets.UTF_8);
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		response.setHeader("Content-filename", "filename=" + fileName + ".xlsx");

		validateTemplate(sheets);

		ExcelWriter writer = buildWriter(response, sheets);

		try {
			for (int i = 0; i < sheets.size(); i++) {
				writeSheet(writer, sheets.get(i), i, defaultBatchSize);
			}
		} finally {
			writer.finish();
			closeProviders(sheets);
		}
	}

	/** 写入单个 Sheet */
	private static <T> void writeSheet(ExcelWriter writer,
									   SheetExportSpec<T> spec,
									   int index,
									   int defaultBatchSize) {

		int batchSize = spec.batchSize != null
				? spec.batchSize
				: defaultBatchSize;

		WriteSheet sheet = EasyExcel
				.writerSheet(index, spec.sheetName)
				.head(spec.modelClass)
				.build();

		BatchProvider<T> provider = spec.provider;

		while (provider.hasNext()) {
			List<T> batch = provider.nextBatch(batchSize);
			if (!batch.isEmpty()) {
				writer.write(batch, sheet);
			}
		}
	}

	/** 构建 ExcelWriter */
	private static ExcelWriter buildWriter(HttpServletResponse response,
										   List<SheetExportSpec<?>> sheets)
			throws Exception {

		SheetExportSpec<?> templateSheet = sheets.stream()
				.filter(SheetExportSpec::isTemplateExport)
				.findFirst()
				.orElse(null);

		if (templateSheet != null) {
			InputStream templateStream =
					new ClassPathResource(templateSheet.templatePath)
							.getInputStream();

			return EasyExcel
					.write(response.getOutputStream())
					.withTemplate(templateStream)
					.build();
		}

		return EasyExcel
				.write(response.getOutputStream())
				.build();
	}

	/** 校验模板：一个 Workbook 只能有一个模板 */
	private static void validateTemplate(List<SheetExportSpec<?>> sheets) {

		Set<String> templates = new HashSet<>();

		for (SheetExportSpec<?> sheet : sheets) {
			if (sheet.isTemplateExport()) {
				templates.add(sheet.templatePath);
			}
		}

		if (templates.size() > 1) {
			throw new IllegalStateException(
					"一个 Excel 只能使用一个模板：" + templates
			);
		}
	}

	/** 关闭 Provider（Cursor） */
	private static void closeProviders(List<SheetExportSpec<?>> sheets) {
		for (SheetExportSpec<?> sheet : sheets) {
			try {
				sheet.provider.close();
			} catch (Exception ignored) {
			}
		}
	}

	/* =========================================================
	 * ======================= Provider 抽象 ===================
	 * ========================================================= */

	/** 批量数据提供者 */
	interface BatchProvider<T> extends AutoCloseable {

		boolean hasNext();

		List<T> nextBatch(int batchSize);

		@Override
		default void close() {
		}
	}

	/** Cursor Provider */
	static class CursorBatchProvider<T> implements BatchProvider<T> {

		private final Cursor<T> cursor;
		private final Iterator<T> iterator;

		CursorBatchProvider(Cursor<T> cursor) {
			this.cursor = cursor;
			this.iterator = cursor.iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public List<T> nextBatch(int batchSize) {
			List<T> list = new ArrayList<>(batchSize);
			int count = 0;
			while (iterator.hasNext() && count < batchSize) {
				list.add(iterator.next());
				count++;
			}
			return list;
		}

		@Override
		public void close() {
			try {
				cursor.close();
			} catch (Exception ignored) {
			}
		}
	}

	/** 分页 Provider */
	static class PageBatchProvider<T> implements BatchProvider<T> {

		private final Function<Integer, List<T>> loader;
		private int pageIndex = 1;
		private boolean finished = false;

		PageBatchProvider(Function<Integer, List<T>> loader) {
			this.loader = loader;
		}

		@Override
		public boolean hasNext() {
			return !finished;
		}

		@Override
		public List<T> nextBatch(int batchSize) {
			List<T> data = loader.apply(pageIndex++);
			if (data == null || data.isEmpty()) {
				finished = true;
				return Collections.emptyList();
			}
			return data;
		}
	}


	static class ListProvider<T> implements BatchProvider<T> {
		private final List<T> data;
		private boolean finished = false;

		ListProvider(List<T> data) {
			this.data = data;
		}

		@Override
		public boolean hasNext() {
			return !finished;
		}

		@Override
		public List<T> nextBatch(int batchSize) {
			this.finished = true;
			return data;
		}

	}
	/* =========================================================
	 * ======================= Sheet 配置 ======================
	 * ========================================================= */

	/** Sheet 描述 */
	static class SheetExportSpec<T> {

		final String sheetName;
		final Class<T> modelClass;
		final BatchProvider<T> provider;
		final Integer batchSize;
		final String templatePath;

		SheetExportSpec(String sheetName,
						Class<T> modelClass,
						BatchProvider<T> provider,
						Integer batchSize,
						String templatePath) {
			this.sheetName = sheetName;
			this.modelClass = modelClass;
			this.provider = provider;
			this.batchSize = batchSize;
			this.templatePath = templatePath;
		}

		boolean isTemplateExport() {
			return templatePath != null && !templatePath.isEmpty();
		}
	}

//@GetMapping("/export/users")
//public void export(HttpServletResponse response) throws Exception {
//
//	Cursor<UserExcelVO> cursor = userMapper.selectUserCursor();
//
//	ExcelUtils2.builder(response)
//			.defaultBatchSize(500)
//			.addSheet(
//					ExcelUtils2.sheet(UserExcelVO.class)
//							.name("用户列表")
//							.cursor(cursor)
//							.batchSize(300)
//							.build()
//			)
//			.export();
//}

	/************************************************************读******************************************************/

	/* ============================================================
	 * =============== Validation 初始化 ============================
	 * ============================================================ */

	private static final Validator VALIDATOR;

	static {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		VALIDATOR = factory.getValidator();
	}

	/* ============================================================
	 * =============== 对外入口 ====================================
	 * ============================================================ */

	public static ReadBuilder read(InputStream inputStream) {
		return new ReadBuilder(inputStream);
	}

	public static <T> ReadSheetSpec<T> readSheet(Class<T> clazz) {
		return new ReadSheetSpec<>(clazz);
	}

	/* ============================================================
	 * =============== Builder ====================================
	 * ============================================================ */

	public static class ReadBuilder {

		private final InputStream inputStream;
		private final List<ReadSheetSpec<?>> sheets = new ArrayList<>();

		private ReadBuilder(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public <T> ReadSheetSpec<T> sheet(Class<T> clazz) {
			ReadSheetSpec<T> spec = new ReadSheetSpec<>(clazz);
			sheets.add(spec);
			return spec;
		}

		public ReadBuilder addSheet(ReadSheetSpec<?> spec) {
			sheets.add(spec);
			return this;
		}

		@SuppressWarnings("unchecked")
		public <T> ReadResult<T> read() {
			if (sheets.size() != 1) {
				throw new IllegalStateException("read() 仅支持单 Sheet");
			}
			return (ReadResult<T>) readInternal(inputStream, sheets.get(0));
		}

		public Map<String, ReadResult<?>> readAll() {
			Map<String, ReadResult<?>> map = new LinkedHashMap<>();
			for (ReadSheetSpec<?> spec : sheets) {
				ReadResult<?> result = readInternal(inputStream, spec);
				String key = spec.sheetName != null
						? spec.sheetName
						: String.valueOf(spec.sheetIndex);
				map.put(key, result);
			}
			return map;
		}
	}

	/* ============================================================
	 * =============== Sheet 定义 ==================================
	 * ============================================================ */

	public static class ReadSheetSpec<T> {

		private final Class<T> headClass;
		private Integer sheetIndex;
		private String sheetName;
		private RowValidator<T> validator;

		private ReadSheetSpec(Class<T> headClass) {
			this.headClass = headClass;
		}

		public ReadSheetSpec<T> sheetIndex(int index) {
			this.sheetIndex = index;
			return this;
		}

		public ReadSheetSpec<T> sheetName(String name) {
			this.sheetName = name;
			return this;
		}

		public ReadSheetSpec<T> validator(RowValidator<T> validator) {
			this.validator = validator;
			return this;
		}
	}

	/* ============================================================
	 * =============== 校验接口 ====================================
	 * ============================================================ */

	@FunctionalInterface
	public interface RowValidator<T> {
		List<String> validate(T row, int rowIndex);
	}

	/* ============================================================
	 * =============== 结果模型 ====================================
	 * ============================================================ */

	public static class RowError<T> {

		private final int rowIndex;
		private final T rowData;
		private final List<String> errors;

		public RowError(int rowIndex, T rowData, List<String> errors) {
			this.rowIndex = rowIndex;
			this.rowData = rowData;
			this.errors = errors;
		}

		public T getRowData() {
			return rowData;
		}

		public String joinErrors() {
			return String.join("；", errors);
		}
	}

	public static class ReadResult<T> {

		private final List<T> successList = new ArrayList<>();
		private final List<RowError<T>> errorList = new ArrayList<>();

		public List<T> getSuccessList() {
			return successList;
		}

		public List<RowError<T>> getErrorList() {
			return errorList;
		}

		public boolean hasError() {
			return !errorList.isEmpty();
		}

		public void addSuccess(T data) {
			successList.add(data);
		}

		public void addError(RowError<T> error) {
			errorList.add(error);
		}

		public String buildErrorSummary() {

			Map<String, Long> counter = errorList.stream()
					.flatMap(e -> e.errors.stream())
					.collect(Collectors.groupingBy(
							e -> e,
							LinkedHashMap::new,
							Collectors.counting()
					));

			StringBuilder sb = new StringBuilder();
			sb.append("共 ").append(errorList.size()).append(" 行错误：\n");
			counter.forEach((msg, count) ->
					sb.append("- ").append(msg).append("：")
							.append(count).append(" 次\n")
			);
			return sb.toString();
		}
	}

	/* ============================================================
	 * =============== 读取实现 ====================================
	 * ============================================================ */

	private static <T> ReadResult<T> readInternal(
			InputStream inputStream,
			ReadSheetSpec<T> spec
	) {

		ReadResult<T> result = new ReadResult<>();

		AnalysisEventListener<T> listener =
				new AnalysisEventListener<>() {

					private int rowIndex = 1;

					@Override
					public void invoke(T data, AnalysisContext context) {

						List<String> errors = new ArrayList<>();

						// ① Bean Validation
						errors.addAll(validateByBeanValidation(data));

						// ② Lambda 校验
						if (spec.validator != null) {
							List<String> custom =
									spec.validator.validate(data, rowIndex);
							if (custom != null) {
								errors.addAll(custom);
							}
						}

						if (errors.isEmpty()) {
							result.addSuccess(data);
						} else {
							result.addError(new RowError<>(rowIndex, data, errors));
						}

						rowIndex++;
					}

					@Override
					public void doAfterAllAnalysed(AnalysisContext context) {
					}
				};

		ExcelReaderBuilder reader =
				EasyExcel.read(inputStream, spec.headClass, listener);

		if (spec.sheetIndex != null) {
			reader.sheet(spec.sheetIndex);
		} else if (spec.sheetName != null) {
			reader.sheet(spec.sheetName);
		} else {
			reader.sheet();
		}

		reader.doReadAll();
		return result;
	}

	private static <T> List<String> validateByBeanValidation(T row) {

		Set<ConstraintViolation<T>> violations = VALIDATOR.validate(row);
		if (violations == null || violations.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> errors = new ArrayList<>();
		for (ConstraintViolation<T> v : violations) {
			errors.add(v.getMessage());
		}
		return errors;
	}

	/* ============================================================
	 * =============== 错误 Excel 导出 =============================
	 * ============================================================ */

	public static <T> void exportErrorExcel(
			HttpServletResponse response,
			String fileName,
			ReadResult<T> result
	) throws Exception {

		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		response.setHeader(
				"Content-Disposition",
				"attachment;filename=" +
						URLEncoder.encode(fileName, "UTF-8") + ".xlsx"
		);

		List<Map<String, Object>> rows = new ArrayList<>();

		for (RowError<T> error : result.getErrorList()) {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("数据", error.getRowData());
			map.put("错误信息", error.joinErrors());
			rows.add(map);
		}

		EasyExcel.write(response.getOutputStream())
				.sheet("错误数据")
				.doWrite(rows);
	}


//	import jakarta.validation.constraints.*;
//import lombok.Data;
//
//	@Data
//	public class UserImportVO {
//
//		@NotBlank(message = "用户名不能为空")
//		private String username;
//
//		@Min(value = 0, message = "年龄不能小于 0")
//		@Max(value = 120, message = "年龄不能大于 120")
//		private Integer age;
//
//		@Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
//		private String phone;
//	}
//
//
//	@PostMapping("/importUser")
//	public void importUser(MultipartFile file,
//						   HttpServletResponse response) throws Exception {
//
//		ExcelUtils2.ReadResult<UserImportVO> result =
//				ExcelUtils2.read(file.getInputStream())
//						.sheet(UserImportVO.class)
//						.sheetIndex(0)
//						.validator((row, rowIndex) -> {
//
//							List<String> errors = new ArrayList<>();
//
//							if ("admin".equals(row.getUsername())) {
//								errors.add("admin 用户不允许导入");
//							}
//							return errors;
//						})
//						.read();
//
//		if (result.hasError()) {
//			ExcelUtils2.exportErrorExcel(
//					response,
//					"用户导入失败.xlsx",
//					result
//			);
//			return;
//		}
//
//		userService.batchSave(result.getSuccessList());
//	}
//	Map<String, ExcelUtils2.ReadResult<?>> map =
//			ExcelUtils2.read(file.getInputStream())
//					.addSheet(
//							ExcelUtils2.readSheet(UserImportVO.class)
//									.sheetName("用户")
//					)
//					.addSheet(
//							ExcelUtils2.readSheet(RoleImportVO.class)
//									.sheetName("角色")
//					)
//					.readAll();
//	共 2 行错误：
//			- 用户名不能为空：1 次
//- 手机号格式错误：2 次
//- 年龄不能小于 0：1 次

}