package io.github.genkidoudou.web.config;

import com.janeluo.luban.rds.server.EmbeddedRedisServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 嵌入式 Redis（Luban-RDS）：在应用进程内启动兼容 Redis 协议的服务，
 * 供 {@link DataRedisAutoConfiguration} / Lettuce 连接，避免本机安装或 Docker 起 Redis。
 * <p>
 * 默认启用；可通过 {@code qc.dev.embedded-redis.enabled=false} 关闭。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "qc.dev.embedded-redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(DataRedisAutoConfiguration.class)
public class DevLubanEmbeddedRedisConfiguration {

  private static final Logger log = LoggerFactory.getLogger(DevLubanEmbeddedRedisConfiguration.class);

  private final EmbeddedRedisServer embeddedRedisServer;

  public DevLubanEmbeddedRedisConfiguration(
    @Value("${qc.dev.embedded-redis.port:6379}") int port) {
    log.info("Starting Luban embedded Redis on 127.0.0.1:{} ", port);
    this.embeddedRedisServer = new EmbeddedRedisServer(port);
    this.embeddedRedisServer.start();
    waitUntilAccepting(port, 5_000L);
    log.info("Luban embedded Redis ready on port {}", this.embeddedRedisServer.getPort());
  }

  @Bean(destroyMethod = "stop")
  public EmbeddedRedisServer embeddedRedisServer() {
    return embeddedRedisServer;
  }

  private static void waitUntilAccepting(int port, long timeoutMs) {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
        return;
      } catch (Exception ignored) {
        try {
          Thread.sleep(50L);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
    log.warn("Luban embedded Redis port {} not accepting within {}ms; Lettuce may retry", port, timeoutMs);
  }
}
