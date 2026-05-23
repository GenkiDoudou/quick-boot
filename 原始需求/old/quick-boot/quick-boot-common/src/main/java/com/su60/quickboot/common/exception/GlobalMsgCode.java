package com.su60.quickboot.common.exception;

/**
 * 全局错误异常码
 *
 * @author luyanan
 * @since 2023/09/08
 **/
public interface GlobalMsgCode {


	/**
	 * 成功
	 *
	 * @since 2023/09/08
	 */

	int SUCCESS = 200;


	/**
	 * 内部服务器错误
	 *
	 * @since 2023/09/08
	 */
	int INTERNAL_SERVER_ERROR = 500;


	/**
	 * 错误请求
	 *
	 * @since 2023/09/08
	 */


	int BAD_REQUEST = 400;


	/**
	 * 未授权
	 *
	 * @since 2023/09/08
	 */

	int UNAUTHORIZED = 401;


	/**
	 * 禁止
	 *
	 * @since 2023/09/08
	 */

	int FORBIDDEN = 403;

	/**
	 * 未找到
	 *
	 * @since 2023/09/08
	 */

	int NOT_FOUND = 404;

	/**
	 * 方法未允许
	 *
	 * @since 2023/09/08
	 */

	int METHOD_NOT_ALLOWED = 405;

	/**
	 * 添加失败
	 *
	 * @since 2023/09/08
	 */

	int SAVE_FAIR = 600;


	/**
	 * 修改失败
	 *
	 * @since 2023/09/08
	 */


	int UPDATE_FAIL = 601;


	/**
	 * 删除失败
	 *
	 * @since 2023/09/08
	 */
	int DELETE_FAIL = 602;
}
