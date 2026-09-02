package io.github.genkidoudou.web.config;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 在 Environment 就绪后、上下文刷新前启动 mariadb4j（由 {@code WebApplication} 显式注册）。
 */
public class DevMariaDb4jStartupListener
  implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

  private static final Logger log = LoggerFactory.getLogger(DevMariaDb4jStartupListener.class);

  private static volatile DB embeddedDb;

  static DB embeddedDb() {
    return embeddedDb;
  }

  static void stopQuietly() {
    DB db = embeddedDb;
    if (db == null) {
      return;
    }
    try {
      db.stop();
    } catch (Exception ex) {
      log.warn("Stop mariadb4j failed: {}", ex.toString());
    } finally {
      embeddedDb = null;
    }
  }

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    startIfNeeded(event.getEnvironment());
  }

  /**
   * 在 dev + embedded-mariadb 启用时启动 mariadb4j（供 main 与集成测试共用）。
   */
  public static void startIfNeeded(ConfigurableEnvironment environment) {
    if (!environment.acceptsProfiles(Profiles.of("dev"))) {
      return;
    }
    if (!environment.getProperty("qc.dev.embedded-mariadb.enabled", Boolean.class, true)) {
      return;
    }
    if (embeddedDb != null) {
      return;
    }

    int port = environment.getProperty("qc.dev.embedded-mariadb.port", Integer.class, 3307);
    String dataDir = environment.getProperty("qc.dev.embedded-mariadb.data-dir", "./data/mariadb");
    String baseDir = environment.getProperty("qc.dev.embedded-mariadb.base-dir", "./data/mariadb-base");
    String database = environment.getProperty("qc.dev.embedded-mariadb.database", "quickboot");

    try {
      Path dataPath = Path.of(dataDir).toAbsolutePath().normalize();
      Path basePath = Path.of(baseDir).toAbsolutePath().normalize();
      Files.createDirectories(dataPath);
      Files.createDirectories(basePath);

      DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
      builder.setPort(port);
      builder.setDataDir(dataPath.toFile());
      builder.setBaseDir(basePath.toFile());
      builder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
      builder.setDefaultCharacterSet("utf8mb4");
      builder.addArg("--character-set-server=utf8mb4");
      builder.addArg("--collation-server=utf8mb4_unicode_ci");
      builder.addArg("--max-allowed-packet=268435456");
      // 保持默认反斜杠转义：V15 mysqldump 中 JSON 的 \" 才能正确落成 "。
      // Windows 路径已在 V15 中改为正斜杠，避免 \t / \' 在默认模式下破坏脚本。

      System.out.println("[mariadb4j] Starting on 127.0.0.1:" + port
        + " dataDir=" + dataPath + " baseDir=" + basePath);
      log.info(
        "Starting mariadb4j on 127.0.0.1:{} (dataDir={}, baseDir={})",
        port,
        dataPath,
        basePath
      );
      DB db = DB.newEmbeddedDB(builder.build());
      db.start();
      db.createDB(database);
      db.run(
        "ALTER DATABASE `" + database + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
        "root",
        "",
        database
      );
      embeddedDb = db;
      System.out.println("[mariadb4j] ready; database=" + database);
      log.info("mariadb4j ready; database={}", database);

      Runtime.getRuntime().addShutdownHook(
        new Thread(DevMariaDb4jStartupListener::stopQuietly, "mariadb4j-shutdown")
      );
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to start embedded MariaDB (mariadb4j)", ex);
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 50;
  }
}
