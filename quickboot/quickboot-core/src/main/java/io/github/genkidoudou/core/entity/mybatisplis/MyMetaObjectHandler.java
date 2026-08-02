package io.github.genkidoudou.core.entity.mybatisplis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

public class MyMetaObjectHandler implements MetaObjectHandler {
  @Override
  public void insertFill(MetaObject metaObject) {
    LoginUser loginUser = LoginUserUtils.getLoginUser();

    LocalDateTime now = LocalDateTime.now();

    strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
    strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    if (null != loginUser) {
      strictInsertFill(metaObject, "createBy", String.class, loginUser.getUserId() + "");
      strictInsertFill(metaObject, "updateBy", String.class, loginUser.getUserId() + "");
    }

  }

  @Override
  public void updateFill(MetaObject metaObject) {
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    if (null != loginUser) {
      strictUpdateFill(metaObject, "updateBy", String.class, loginUser.getUserId() + "");
    }
  }


}
