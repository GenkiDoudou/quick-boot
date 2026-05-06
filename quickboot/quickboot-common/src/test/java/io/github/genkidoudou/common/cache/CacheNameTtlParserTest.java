package io.github.genkidoudou.common.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheNameTtlParserTest {

    @Test
    void userCacheWith3600() {
        CacheNameTtl p = CacheNameTtlParser.parse("userCache#3600");
        assertThat(p.logicalName()).isEqualTo("userCache");
        assertThat(p.ttlSeconds()).isEqualTo(3600);
    }

    @Test
    void aWith1() {
        CacheNameTtl p = CacheNameTtlParser.parse("a#1");
        assertThat(p.logicalName()).isEqualTo("a");
        assertThat(p.ttlSeconds()).isEqualTo(1);
    }

    @Test
    void noSuffixUsesDefaultTtl() {
        CacheNameTtl p = CacheNameTtlParser.parse("userCache");
        assertThat(p.logicalName()).isEqualTo("userCache");
        assertThat(p.ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
    }

    @Test
    void invalidSuffixUsesDefaultTtlAndKeepsFullLogicalName() {
        CacheNameTtl p = CacheNameTtlParser.parse("bad#x");
        assertThat(p.logicalName()).isEqualTo("bad#x");
        assertThat(p.ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
    }

    @Test
    void zeroOrNegativeUsesDefault() {
        assertThat(CacheNameTtlParser.parse("z#0").ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
        assertThat(CacheNameTtlParser.parse("z#-3").logicalName()).isEqualTo("z#-3");
        assertThat(CacheNameTtlParser.parse("z#-3").ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
    }

    @Test
    void loneHashOrTrailingHashUsesDefault() {
        CacheNameTtl p = CacheNameTtlParser.parse("#3600");
        assertThat(p.logicalName()).isEqualTo("#3600");
        assertThat(p.ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);

        CacheNameTtl p2 = CacheNameTtlParser.parse("abc#");
        assertThat(p2.logicalName()).isEqualTo("abc#");
        assertThat(p2.ttlSeconds()).isEqualTo(QuickbootCacheDefaults.DEFAULT_TTL_SECONDS);
    }
}
