package io.github.genkidoudou.quartz.internal.support;

import io.github.genkidoudou.quartz.internal.dto.JobHttpConfigBo;
import io.github.genkidoudou.quartz.internal.dto.JobScriptConfigBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobSaveBo;
import io.github.genkidoudou.quartz.internal.entity.SysJob;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JobPayloadAssembler} 结构化配置与表字段互转测试。
 */
class JobPayloadAssemblerTest {

    @Test
    void applyHttp_writesUrlToInvokeTargetAndJsonParams() {
        SysJobSaveBo bo = new SysJobSaveBo();
        bo.setJobType("1");
        JobHttpConfigBo http = new JobHttpConfigBo();
        http.setUrl("https://example.com/ping");
        http.setMethod("POST");
        http.setBody("{\"ok\":true}");
        bo.setHttpConfig(http);

        SysJob entity = new SysJob();
        JobPayloadAssembler.applyToEntity(bo, entity);

        assertEquals("1", entity.getJobType());
        assertEquals("https://example.com/ping", entity.getInvokeTarget());
        assertNotNull(entity.getParams());
        assertTrue(entity.getParams().contains("POST"));
    }

    @Test
    void applyScript_writesPathToInvokeTarget() {
        SysJobSaveBo bo = new SysJobSaveBo();
        bo.setJobType("2");
        JobScriptConfigBo script = new JobScriptConfigBo();
        script.setScriptPath("/opt/scripts/run.sh");
        bo.setScriptConfig(script);

        SysJob entity = new SysJob();
        JobPayloadAssembler.applyToEntity(bo, entity);

        assertEquals("2", entity.getJobType());
        assertEquals("/opt/scripts/run.sh", entity.getInvokeTarget());
        assertTrue(entity.getParams().contains("timeoutSec"));
    }
}
