package io.github.genkidoudou.web.monitor.operlog.support;

import io.github.genkidoudou.common.monitor.operlog.OperLogBusinessType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperLogMetaResolverTest {

    @Test
    void inferInsertOnPostCreate() throws Exception {
        Method m = SampleController.class.getMethod("create");
        assertEquals(OperLogBusinessType.INSERT, OperLogMetaResolver.inferBusinessType(m, "POST", "/system/user/create"));
    }

    @Test
    void inferDeleteOnPostRemove() throws Exception {
        Method m = SampleController.class.getMethod("remove");
        assertEquals(OperLogBusinessType.DELETE, OperLogMetaResolver.inferBusinessType(m, "POST", "/system/user/remove"));
    }

    @Test
    void inferExportOnPostExport() throws Exception {
        Method m = SampleController.class.getMethod("export");
        assertEquals(OperLogBusinessType.EXPORT, OperLogMetaResolver.inferBusinessType(m, "POST", "/system/user/export"));
    }

    @Test
    void inferOtherOnGetList() throws Exception {
        Method m = SampleController.class.getMethod("list");
        assertEquals(OperLogBusinessType.OTHER, OperLogMetaResolver.inferBusinessType(m, "GET", "/system/user/list"));
    }

    static class SampleController {
        public void create() {
        }

        public void remove() {
        }

        public void export() {
        }

        public void list() {
        }
    }
}
