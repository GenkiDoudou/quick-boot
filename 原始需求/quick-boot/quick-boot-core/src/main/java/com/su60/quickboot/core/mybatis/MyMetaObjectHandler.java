package com.su60.quickboot.core.mybatis;

import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 字段 自动填充
 *
 * @author luyanan
 * @since 2024/10/11
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
	@Override
	public void insertFill(MetaObject metaObject) {
		this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
		this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());

		Long userId = LoginUserUtils.getUserId();
		if (null != userId) {
			this.strictInsertFill(metaObject, "createBy", Long.class, userId);
			this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
		}
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
		Long userId = LoginUserUtils.getUserId();
		if (null != userId) {
			this.strictInsertFill(metaObject, "updateBy", Long.class,userId);
		}
	}
}
