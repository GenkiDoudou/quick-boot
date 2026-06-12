package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流节点类型（12 种核心节点，不含 Code）。
 */
public final class WfNodeType {

    public static final String START = "start";
    public static final String ANSWER = "answer";
    public static final String LLM = "llm";
    public static final String KNOWLEDGE_RETRIEVAL = "knowledge-retrieval";
    public static final String IF_ELSE = "if-else";
    public static final String TEMPLATE_TRANSFORM = "template-transform";
    public static final String VARIABLE_ASSIGN = "variable-assign";
    public static final String VARIABLE_AGGREGATOR = "variable-aggregator";
    public static final String HTTP_REQUEST = "http-request";
    public static final String QUESTION_CLASSIFIER = "question-classifier";
    public static final String PARAMETER_EXTRACTOR = "parameter-extractor";
    public static final String LIST_OPERATOR = "list-operator";

    private WfNodeType() {
    }
}
