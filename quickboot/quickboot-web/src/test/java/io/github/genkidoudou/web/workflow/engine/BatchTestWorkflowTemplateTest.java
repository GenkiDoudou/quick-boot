package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.template.BatchTestWorkflowTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 批处理测试工作流模板图结构校验。
 */
class BatchTestWorkflowTemplateTest {

    private WorkflowGraphValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkflowGraphValidator();
    }

    @Test
    void validate_batchArrayTestTemplate_success() {
        var graph = new BatchTestWorkflowTemplate().build().getGraph();
        assertDoesNotThrow(() -> validator.validate(graph));
    }
}
