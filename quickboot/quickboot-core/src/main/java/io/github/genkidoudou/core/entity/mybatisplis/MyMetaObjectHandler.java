package io.github.genkidoudou.core.entity.mybatisplis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充：写入 createTime/updateTime 及 createBy/updateBy。
 * <p>
 * 操作人取自当前登录用户；无登录上下文时仅填充时间戳。
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

  /**
   * INSERT 时填充创建/更新时间与操作人。
   *
   * @param metaObject 待填充实体元对象
   */
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

  /**
   * UPDATE 时填充更新时间与操作人。
   *
   * @param metaObject 待填充实体元对象
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    if (null != loginUser) {
      strictUpdateFill(metaObject, "updateBy", String.class, loginUser.getUserId() + "");
    }
  }


}
