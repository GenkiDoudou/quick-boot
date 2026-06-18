package io.github.genkidoudou.config.embeddedmariadb;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 嵌入式 MariaDB4J 生命周期：在 Spring 刷新 DataSource 之前启动，devtools 热重启时复用同一实例。
 */
public final class EmbeddedMariaDbBootstrap {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedMariaDbBootstrap.class);

    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);

    private static volatile DB runningDb;

    private EmbeddedMariaDbBootstrap() {
    }

    /**
     * 按配置启动嵌入式 MariaDB（幂等；同 JVM 内重复调用安全）。
     *
     * @param port         监听端口
     * @param dataDir      数据目录（持久化；可整目录拷贝迁移）
     * @param databaseName 业务库名（不存在则创建）
     */
    public static void ensureStarted(int port, Path dataDir, String databaseName) {
        if (runningDb != null) {
            log.debug("嵌入式 MariaDB 已在运行，跳过重复启动");
            return;
        }
        synchronized (EmbeddedMariaDbBootstrap.class) {
            if (runningDb != null) {
                return;
            }
            try {
                Files.createDirectories(dataDir);
                DBConfiguration config = buildConfiguration(port, dataDir);
                DB db = DB.newEmbeddedDB(config);
                db.start();
                createDatabaseIfAbsent(db, databaseName);
                runningDb = db;
                registerShutdownHookOnce();
                log.info("嵌入式 MariaDB 已启动：jdbc:mysql://127.0.0.1:{}/{} ，数据目录 {}",
                        port, databaseName, dataDir.toAbsolutePath());
            } catch (Exception ex) {
                throw new IllegalStateException(
                        "嵌入式 MariaDB 启动失败，请检查端口 " + port + " 是否占用、数据目录 "
                                + dataDir.toAbsolutePath() + " 是否可写", ex);
            }
        }
    }

    private static DBConfiguration buildConfiguration(int port, Path dataDir) {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(port);
        builder.setDataDir(dataDir.toAbsolutePath().toString());
        builder.addArg("--character-set-server=utf8mb4");
        builder.addArg("--collation-server=utf8mb4_unicode_ci");
        builder.addArg("--max_allowed_packet=256M");
        builder.addArg("--innodb_strict_mode=OFF");
        return builder.build();
    }

    private static void createDatabaseIfAbsent(DB db, String databaseName) {
        try {
            db.createDB(databaseName);
            log.info("已创建业务库 {}", databaseName);
        } catch (Exception ex) {
            log.debug("业务库 {} 可能已存在，跳过 createDB：{}", databaseName, ex.getMessage());
        }
    }

    private static void registerShutdownHookOnce() {
        if (!SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DB db = runningDb;
            if (db == null) {
                return;
            }
            try {
                db.stop();
                log.info("嵌入式 MariaDB 已停止");
            } catch (Exception ex) {
                log.warn("嵌入式 MariaDB 停止异常：{}", ex.getMessage());
            } finally {
                runningDb = null;
            }
        }, "embedded-mariadb-shutdown"));
    }

    /**
     * 解析数据目录：支持相对路径（相对 user.dir）。
     *
     * @param dataDir 配置值
     * @return 绝对路径
     */
    public static Path resolveDataDir(String dataDir) {
        Path path = Paths.get(dataDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path.normalize();
    }
}
