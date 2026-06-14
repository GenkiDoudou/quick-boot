package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.template.LoopTestWorkflowTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 循环测试工作流模板图结构校验。
 */
class LoopTestWorkflowTemplateTest {

    private WorkflowGraphValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkflowGraphValidator();
    }

    @Test
    void validate_loopCountTestTemplate_success() {
        var graph = new LoopTestWorkflowTemplate().build().getGraph();
        assertDoesNotThrow(() -> validator.validate(graph));
    }
}
