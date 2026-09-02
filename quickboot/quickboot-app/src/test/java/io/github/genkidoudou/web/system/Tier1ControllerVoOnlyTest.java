package io.github.genkidoudou.web.system;

import io.github.genkidoudou.system.internal.controller.SysConfigController;
import io.github.genkidoudou.system.internal.controller.SysDeployRecordController;
import io.github.genkidoudou.system.internal.controller.SysDictDataController;
import io.github.genkidoudou.system.internal.controller.SysDictTypeController;
import io.github.genkidoudou.system.internal.controller.SysFileClassifyController;
import io.github.genkidoudou.system.internal.controller.SysLogininforController;
import io.github.genkidoudou.system.internal.controller.SysOperLogController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tier-1 Controller OpenAPI 契约守卫：公开 HTTP 方法不得出现 Entity 类型参数/返回值（仅 Vo）。
 */
class Tier1ControllerVoOnlyTest {

  private static final List<Class<?>> TIER1_CONTROLLERS = List.of(
    SysConfigController.class,
    SysDictTypeController.class,
    SysDictDataController.class,
    SysFileClassifyController.class,
    SysDeployRecordController.class,
    SysLogininforController.class,
    SysOperLogController.class
  );

  @Test
  void tier1ControllersDoNotExposeEntityInSignatures() {
    List<String> violations = new ArrayList<>();
    for (Class<?> controller : TIER1_CONTROLLERS) {
      assertNotNull(controller.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class),
        controller.getSimpleName() + " 缺少 @Tag");
      for (Method method : controller.getDeclaredMethods()) {
        if (method.isSynthetic() || method.getName().startsWith("lambda$")) {
          continue;
        }
        collectEntityViolations(controller, method, method.getGenericReturnType(), "return", violations);
        for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
          collectEntityViolations(controller, method, method.getGenericParameterTypes()[i],
            "param" + i, violations);
        }
      }
    }
    if (!violations.isEmpty()) {
      fail(String.join(System.lineSeparator(), violations));
    }
  }

  private static void collectEntityViolations(Class<?> controller, Method method, Type type,
                                              String location, List<String> violations) {
    if (containsEntityType(type)) {
      violations.add(controller.getSimpleName() + "#" + method.getName() + " " + location
        + " uses Entity: " + type);
    }
  }

  private static boolean containsEntityType(Type type) {
    if (type == null) {
      return false;
    }
    if (type instanceof Class<?> clazz) {
      return isEntityClass(clazz);
    }
    if (type instanceof ParameterizedType pt) {
      if (containsEntityType(pt.getRawType())) {
        return true;
      }
      for (Type arg : pt.getActualTypeArguments()) {
        if (containsEntityType(arg)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isEntityClass(Class<?> clazz) {
    if (clazz.isArray()) {
      return isEntityClass(clazz.getComponentType());
    }
    Package pkg = clazz.getPackage();
    if (pkg == null) {
      return false;
    }
    String pkgName = pkg.getName();
    return pkgName.endsWith(".entity") || pkgName.contains(".internal.entity");
  }
}
