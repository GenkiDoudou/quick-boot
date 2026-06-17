package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流节点类型（含固定开始/结束节点与可多个输出节点）。
 */
public final class WfNodeType {

    public static final String START = "start";
    /** 固定结束节点，每工作流唯一。 */
    public static final String END = "end";
    /** 输出节点，可多个。 */
    public static final String ANSWER = "answer";
    public static final String LLM = "llm";
    public static final String KNOWLEDGE_RETRIEVAL = "knowledge-retrieval";
    public static final String IF_ELSE = "if-else";
    public static final String TEMPLATE_TRANSFORM = "template-transform";
    /** 文本处理：字符串拼接 / 分隔。 */
    public static final String TEXT_PROCESS = "text-process";
    public static final String VARIABLE_ASSIGN = "variable-assign";
    public static final String VARIABLE_AGGREGATOR = "variable-aggregator";
    public static final String HTTP_REQUEST = "http-request";
    /** 自定义代码逻辑节点（JavaScript / Python）。 */
    public static final String CODE = "code";
    /** JSON 序列化：将 Object/Array 等转为 JSON 字符串。 */
    public static final String JSON_SERIALIZE = "json-serialize";
    /** JSON 反序列化：将 JSON 字符串解析为 Object/Array。 */
    public static final String JSON_DESERIALIZE = "json-deserialize";
    /** 意图识别（原 question-classifier）：LLM 多意图分支 + 兜底出口。 */
    public static final String QUESTION_CLASSIFIER = "question-classifier";
    public static final String PARAMETER_EXTRACTOR = "parameter-extractor";
    public static final String LIST_OPERATOR = "list-operator";
    /** 循环控制节点。 */
    public static final String LOOP = "loop";
    /** 循环体容器（不参与主画布调度）。 */
    public static final String LOOP_BODY = "loop-body";
    /** 循环体入口锚点（仅循环体内，透传调度）。 */
    public static final String LOOP_BODY_START = "loop-body-start";
    /** 循环体出口锚点（仅循环体内，透传调度）。 */
    public static final String LOOP_BODY_END = "loop-body-end";
    /** 终止循环（仅循环体内）。 */
    public static final String BREAK_LOOP = "break-loop";
    /** 继续循环（仅循环体内）。 */
    public static final String CONTINUE_LOOP = "continue-loop";
    /** 设置循环中间变量（仅循环体内）。 */
    public static final String LOOP_SET_VARIABLE = "loop-set-variable";
    /** 批处理节点。 */
    public static final String BATCH = "batch";
    /** 批处理体容器（不参与主画布调度）。 */
    public static final String BATCH_BODY = "batch-body";

    private WfNodeType() {
    }
}
