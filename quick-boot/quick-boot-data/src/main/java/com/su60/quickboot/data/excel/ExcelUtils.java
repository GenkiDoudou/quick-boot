package com.su60.quickboot.data.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.data.mybatisplus.BaseBaseMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * excel工具类
 *
 * @author luyanan
 * @since 2024/06/16
 **/
@UtilityClass
public class ExcelUtils {
	/**************************导出***************************/


	/**
	 * 导出到excel
	 *
	 * @param response  响应类
	 * @param fileName  文件名
	 * @param sheetName sheetName
	 * @param datas     数据
	 * @param pojoClass 对象class
	 * @since 2024/06/16
	 */
	public void exportExcel(HttpServletResponse response, String fileName,
							String sheetName, List<?> datas, Class<?> pojoClass) throws IOException {

		if (StrUtil.isBlank(fileName)) {
			fileName = DateUtil.date().toString();
		}
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("UTF-8");

		fileName = URLUtil.encode(fileName, StandardCharsets.UTF_8);
		response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
		response.setHeader("Content-filename", "filename=" + fileName + ".xlsx");
		// 👍 不用 doWrite，手动控制 finish()
		ExcelWriter writer = null;
		try {
			writer = EasyExcel.write(response.getOutputStream(), pojoClass)
					.registerConverter(new LongStringConverter())
					.build();
			WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();

			writer.write(datas, sheet);
		} finally {
			if (writer != null) {
				writer.finish(); // EasyExcel 自己关闭流，不要手动关闭 response.getOutputStream()
			}
		}
	}


	public static interface ExcelConsumer<T> {
		boolean hasNext();

		T next();
	}

	public <D> void exportExcel(HttpServletResponse response, String fileName,
								String sheetName, Class<D> pojoClass, ExcelConsumer<List<D>> excelConsumer) {
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		ExcelWriter writer = null;
		try {
			response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
			writer = EasyExcel.write(response.getOutputStream()).head(pojoClass).registerConverter(new LongStringConverter()).build();
			WriteSheet sheet = EasyExcel.writerSheet(sheetName).head(pojoClass).build();
			if (excelConsumer.hasNext()) {
				List<D> next = excelConsumer.next();
				writer.write(next, sheet);
			}

		} catch (Exception e) {
			// 记录异常
			e.printStackTrace();
			throw new RuntimeException("导出Excel失败", e);
		} finally {
			if (writer != null) {
				try {
					writer.finish();
				} catch (Exception e) {
					// 忽略关闭异常
				}
			}
		}
	}

	public <T, D> void exportExcelByCursor(HttpServletResponse response, String fileName,
										   String sheetName, Class<D> pojoClass, Class<? extends BaseBaseMapper> mapperClass,
										   Wrapper wrapper, BiConsumer<List<T>, List<D>> consumer, Integer size) {
		SqlSessionFactory sqlSessionFactory = SpringUtil.getBean(SqlSessionFactory.class);
		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			BaseBaseMapper baseBaseMapper = sqlSession.getMapper(mapperClass);
			try (Cursor<T> cursor = baseBaseMapper.selectCursor(wrapper)) {
				List<T> list = new ArrayList<>();
				for (T item : cursor) {
					// 处理数据
					if (null != item) {
						list.add(item);
					}
				}
				List<D> ds = new ArrayList<>();
				if (CollectionUtil.isNotEmpty(list)) {
					ds = BeanConvertUtils.convertListTo(list, pojoClass);
					if (null != consumer) {
						consumer.accept(list, ds);
					}
				}
				List<D> finalDs = ds;
				exportExcel(response, fileName, sheetName, pojoClass, new ExcelConsumer<List<D>>() {
					@Override
					public boolean hasNext() {
						return !list.isEmpty();
					}

					@Override
					public List<D> next() {
						return finalDs;
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

//	public <T, D> void exportExcelByCursor(HttpServletResponse response, String fileName,
//										   String sheetName, Class<D> pojoClass, Class<? extends BaseBaseMapper> mapperClass,
//										   Wrapper wrapper, BiConsumer<List<T>, List<D>> consumer, Integer size) throws IOException {
//		response.setContentType("application/vnd.ms-excel");
//		response.setCharacterEncoding("utf-8");
//		response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
//		ExcelWriter writer = null;
//		try {
//			writer = EasyExcel.write(response.getOutputStream()).head(pojoClass).registerConverter(new LongStringConverter()).build();
//
//			WriteSheet sheet = EasyExcel.writerSheet(sheetName).head(pojoClass).build();
//			SqlSessionFactory sqlSessionFactory = SpringUtil.getBean(SqlSessionFactory.class);
//			try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//				BaseBaseMapper baseBaseMapper = sqlSession.getMapper(mapperClass);
//				try (Cursor<T> cursor = baseBaseMapper.selectByPkCursor(0L, size, wrapper)) {
//					List<T> list = new ArrayList<>();
//					for (T item : cursor) {
//						// 处理数据
//						if (null != item) {
//							list.add(item);
//						}
//					}
//					if (CollectionUtil.isNotEmpty(list)) {
//						List<D> ds = BeanConvertUtils.convertListTo(list, pojoClass);
//						if (null != consumer) {
//							consumer.accept(list, ds);
//						}
//						writer.write(list, sheet);
//					}
//				}
//			}
//		} catch (Exception e) {
//			// 记录异常
//			e.printStackTrace();
//			throw new IOException("导出Excel失败", e);
//		} finally {
//			if (writer != null) {
//				try {
//					writer.finish();
//				} catch (Exception e) {
//					// 忽略关闭异常
//				}
//			}
//		}
//	}
	/******************读取excel***********************/

	/**
	 *
	 * 读取excel
	 *
	 * @param file        文件
	 * @param eClass      excel的class类
	 * @param lineHandler 行处理器
	 * @param <E>         execl 类的泛型
	 * @return
	 * @since 2024/06/28
	 */
	public <E> void read(MultipartFile file, Class<E> eClass, LineHandler<E> lineHandler) throws
			IOException {

		EasyExcel.read(file.getInputStream(), eClass, new ReadListener<E>() {
			@Override
			public void invoke(E data, AnalysisContext context) {
				lineHandler.handler(data);
			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {

			}
		}).sheet().doRead();

	}


	/**
	 * 行处理器
	 *
	 * @param <E> excel的泛型
	 * @author luyanan
	 * @since 2024/06/28
	 */
	public interface LineHandler<E> {

		/**
		 * 行处理
		 *
		 * @param e excel的泛型
		 * @return
		 * @since 2024/06/16
		 */
		void handler(E e);

	}

}
