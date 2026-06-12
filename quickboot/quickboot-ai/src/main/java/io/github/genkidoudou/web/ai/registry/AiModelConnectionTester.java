package io.github.genkidoudou.web.ai.registry;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.constants.AiModelType;
import io.github.genkidoudou.web.ai.constants.AiTestStatus;
import io.github.genkidoudou.web.ai.domain.AiModel;
import io.github.genkidoudou.web.ai.dto.AiTestResultVo;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AI 模型连接测试：Chat probe / Embedding 维度验证，并回写 {@code last_test_*} 字段。
 */
@Component
public class AiModelConnectionTester {

    private static final String CHAT_PROBE = "ping";

    private final AiModelRegistry modelRegistry;
    private final AiModelMapper modelMapper;

    public AiModelConnectionTester(AiModelRegistry modelRegistry, AiModelMapper modelMapper) {
        this.modelRegistry = modelRegistry;
        this.modelMapper = modelMapper;
    }

    /**
     * 对指定模型执行连接测试。
     *
     * @param modelId 模型主键
     * @return 测试结果
     */
    public AiTestResultVo test(Long modelId) {
        AiModel row = modelRegistry.loadModel(modelId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型不存在或已删除");
        }
        modelRegistry.evict(modelId);
        AiTestResultVo result = new AiTestResultVo();
        try {
            if (AiModelType.isLanguage(row.getModelType())) {
                testChat(row, result);
            } else if (AiModelType.isVector(row.getModelType())) {
                testEmbedding(row, result);
            } else if (AiModelType.isImage(row.getModelType())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "图像模型连接测试暂未实现");
            } else {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的模型类型: " + row.getModelType());
            }
            result.setSuccess(true);
            updateTestResult(modelId, AiTestStatus.SUCCESS, result.getMessage());
        } catch (WarningException ex) {
            result.setSuccess(false);
            result.setMessage(ex.getMessage());
            updateTestResult(modelId, AiTestStatus.FAILED, truncate(ex.getMessage()));
            throw ex;
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            result.setSuccess(false);
            result.setMessage(msg);
            updateTestResult(modelId, AiTestStatus.FAILED, truncate(msg));
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型连接测试失败: " + msg);
        }
        return result;
    }

    private void testChat(AiModel row, AiTestResultVo result) {
        ChatModel chatModel = modelRegistry.getChatModel(row.getModelId());
        if (chatModel == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无法构建 Chat 模型实例");
        }
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(CHAT_PROBE)));
        String text = response.getResult() == null || response.getResult().getOutput() == null
            ? null
            : response.getResult().getOutput().getText();
        if (StrUtil.isBlank(text)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "Chat 探测未返回有效内容");
        }
        result.setReplyPreview(truncate(text));
        result.setMessage("Chat 探测成功");
    }

    private void testEmbedding(AiModel row, AiTestResultVo result) {
        if (row.getDimensions() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "Embedding 模型未配置 dimensions");
        }
        EmbeddingModel embeddingModel = modelRegistry.getEmbeddingModel(row.getModelId());
        if (embeddingModel == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无法构建 Embedding 模型实例");
        }
        float[] vector = embeddingModel.embed("test");
        if (vector == null || vector.length == 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "Embedding 探测未返回向量");
        }
        if (vector.length != row.getDimensions()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "向量维度 " + vector.length + " 与配置 " + row.getDimensions() + " 不一致");
        }
        result.setActualDimensions(vector.length);
        result.setMessage("Embedding 探测成功，维度 " + vector.length);
    }

    private void updateTestResult(Long modelId, String status, String message) {
        AiModel upd = new AiModel();
        upd.setModelId(modelId);
        upd.setLastTestStatus(status);
        upd.setLastTestMsg(message);
        upd.setLastTestTime(LocalDateTime.now());
        modelMapper.updateById(upd);
    }

    private String truncate(String message) {
        if (message == null) {
            return "";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
