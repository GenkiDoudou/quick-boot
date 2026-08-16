package io.github.genkidoudou.web;

import io.github.genkidoudou.web.config.DevMariaDb4jStartupListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类（MariaDB Embedded + Luban-RDS + Spring Authorization Server）。
 */
@SpringBootApplication(scanBasePackages = {
  "io.github.genkidoudou.auth",
  "io.github.genkidoudou.core",
  "io.github.genkidoudou.system",
  "io.github.genkidoudou.quartz",
  "io.github.genkidoudou.tool",
  "io.github.genkidoudou.report",
  "io.github.genkidoudou.monitor",
  "io.github.genkidoudou.web"
})
@MapperScan({
  "io.github.genkidoudou.system.internal.mapper",
  "io.github.genkidoudou.quartz.internal.mapper",
  "io.github.genkidoudou.tool.internal.gen.mapper",
  "io.github.genkidoudou.monitor.internal.slowsql.mapper",
  "io.github.genkidoudou.monitor.internal.litetrace.mapper"
})
public class WebApplication {

  /**
   * @param args 命令行参数
   */
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(WebApplication.class);
    application.addListeners(new DevMariaDb4jStartupListener());
    application.run(args);
  }
}
