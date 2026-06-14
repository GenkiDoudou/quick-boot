package io.github.genkidoudou.web.workflow.engine;

/**
 * 单次循环迭代结束方式。
 */
public enum LoopIterationResult {

    /** 正常完成本轮循环体。 */
    NORMAL,

    /** 继续循环：跳过本轮剩余节点，进入下一轮。 */
    CONTINUE,

    /** 终止循环：结束整个循环节点。 */
    BREAK
}
