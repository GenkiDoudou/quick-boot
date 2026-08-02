package io.github.genkidoudou.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类（H2 + Luban-RDS + Spring Authorization Server）。
 */
@SpringBootApplication(scanBasePackages = {
  "io.github.genkidoudou.auth",
  "io.github.genkidoudou.core",
  "io.github.genkidoudou.system",
  "io.github.genkidoudou.web"
})
@MapperScan("io.github.genkidoudou.system.mapper")
public class WebApplication {

  /**
   * @param args 命令行参数
   */
  public static void main(String[] args) {
    SpringApplication.run(WebApplication.class, args);
  }
}
