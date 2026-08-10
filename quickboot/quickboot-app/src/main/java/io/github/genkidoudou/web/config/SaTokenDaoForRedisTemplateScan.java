package io.github.genkidoudou.web.config;

import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token Redis DAO：{@code searchData} 使用 {@code SCAN} 替代 {@code KEYS}。
 * <p>本地 Luban 嵌入式 Redis 不支持 {@code KEYS}，在线用户等检索会直接失败。</p>
 */
public class SaTokenDaoForRedisTemplateScan extends SaTokenDaoForRedisTemplate {

  private static final long SCAN_COUNT = 200L;

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List searchData(String prefix, String keyword, int start, int size, boolean sortType) {
    String pattern = prefix + "*" + keyword + "*";
    Set<String> keys = new LinkedHashSet<>();
    ScanOptions options = ScanOptions.scanOptions()
      .match(pattern)
      .count(SCAN_COUNT)
      .build();
    try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
      while (cursor.hasNext()) {
        keys.add(cursor.next());
      }
    }
    List list = new ArrayList<>(keys);
    return SaFoxUtil.searchList(list, start, size, sortType);
  }
}
