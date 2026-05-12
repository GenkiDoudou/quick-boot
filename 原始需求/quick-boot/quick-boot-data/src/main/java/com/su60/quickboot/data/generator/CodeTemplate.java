package com.su60.quickboot.data.generator;

import com.su60.quickboot.data.generator.query.GeneratorInterceptor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 代码模板
 *
 * @author luyanan
 * @since 2024/06/24
 **/
@Data
@Accessors(chain = true)
public class CodeTemplate {


	/**
	 * 包路径
	 *
	 * @since 2024/06/24
	 */

	private String packagePath;


	/**
	 * 存放路径
	 *
	 * @since 2024/06/24
	 */

	private String savePath;


	/**
	 * 文件名
	 *
	 * @since 2024/06/24
	 */

	private String fileName;

//
//	/**
//	 * 类名
//	 *
//	 * @since 2024/06/27
//	 */
//
//	private String className;
//

	/**
	 * 模板内容
	 *
	 * @since 2024/06/24
	 */

	private String templateContent;

	/**
	 * 模板文件路径
	 *
	 * @since 2024/06/26
	 */

	private String templateFilePath;


	/**
	 * 模板渲染之后 的内容
	 *
	 * @since 2024/06/27
	 */

	private String templateViewContent;

	/**
	 * 父包路径
	 *
	 * @since 2024/06/29
	 */

	private String parentPackagePath;

	/**
	 * 拦截器
	 *
	 * @since 2024/10/29
	 */

	private GeneratorInterceptor interceptor;

}
