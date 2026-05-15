package io.github.genkidoudou.web.system.user.datascope;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableMatchUtilTest {

    @Test
    void exactMatch() {
        assertThat(TableMatchUtil.match("sys_user", new String[] {"sys_user"})).isTrue();
        assertThat(TableMatchUtil.match("SYS_USER", new String[] {"sys_user"})).isTrue();
    }

    @Test
    void wildcardMatch() {
        assertThat(TableMatchUtil.match("order_2026", new String[] {"order_*"})).isTrue();
        assertThat(TableMatchUtil.match("other", new String[] {"order_*"})).isFalse();
    }
}
