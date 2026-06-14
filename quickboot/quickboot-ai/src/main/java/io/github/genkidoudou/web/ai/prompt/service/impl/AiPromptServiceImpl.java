package io.github.genkidoudou.web.ai.prompt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.prompt.constants.AiPromptConstants;
import io.github.genkidoudou.web.ai.prompt.constants.AiPromptStatus;
import io.github.genkidoudou.web.ai.prompt.constants.AiPromptType;
import io.github.genkidoudou.web.ai.prompt.domain.AiPrompt;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptContent;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptBo;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptionVo;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptQueryBo;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptVo;
import io.github.genkidoudou.web.ai.prompt.mapper.AiPromptContentMapper;
import io.github.genkidoudou.web.ai.prompt.mapper.AiPromptMapper;
import io.github.genkidoudou.web.ai.prompt.service.AiPromptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 提示词 CRUD 实现（名称、分类、描述、内容四字段）。
 */
@Service
public class AiPromptServiceImpl implements AiPromptService {

    private final AiPromptMapper promptMapper;
    private final AiPromptContentMapper contentMapper;

    public AiPromptServiceImpl(AiPromptMapper promptMapper, AiPromptContentMapper contentMapper) {
        this.promptMapper = promptMapper;
        this.contentMapper = contentMapper;
    }

    @Override
    public PageInfo<AiPromptVo> page(AiPromptQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<AiPrompt> wrapper = Wrappers.<AiPrompt>lambdaQuery()
            .eq(AiPrompt::getDeleted, AiPromptConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), AiPrompt::getName, query.getName())
            .like(StrUtil.isNotBlank(query.getCategory()), AiPrompt::getCategory, query.getCategory())
            .orderByDesc(AiPrompt::getUpdateTime);

        Page<AiPrompt> mp = promptMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AiPromptVo> rows = new ArrayList<>(mp.getRecords().size());
        for (AiPrompt row : mp.getRecords()) {
            rows.add(toVo(row, false));
        }
        Page<AiPromptVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public AiPromptVo getInfo(Long promptId) {
        AiPrompt row = getById(promptId);
        if (row == null) {
            return null;
        }
        return toVo(row, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AiPromptBo req) {
        if (StrUtil.isBlank(req.getName())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "提示词名称不能为空");
        }

        AiPrompt entity = new AiPrompt();
        entity.setName(req.getName().trim());
        entity.setCategory(StrUtil.nullToEmpty(req.getCategory()).trim());
        entity.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        entity.setCode("p" + System.currentTimeMillis());
        entity.setPromptType(AiPromptType.LLM);
        entity.setStatus(AiPromptStatus.PUBLISHED);
        entity.setCurrentVersionNo(0);
        entity.setDeleted(AiPromptConstants.NOT_DELETED);
        promptMapper.insert(entity);

        saveContent(entity.getPromptId(), req.getContent());
        return entity.getPromptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AiPromptBo req) {
        AiPrompt old = getById(req.getPromptId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "提示词不存在或已删除");
        }
        if (StrUtil.isBlank(req.getName())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "提示词名称不能为空");
        }

        old.setName(req.getName().trim());
        old.setCategory(StrUtil.nullToEmpty(req.getCategory()).trim());
        old.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        promptMapper.updateById(old);

        saveContent(old.getPromptId(), req.getContent());
    }

    @Override
    public List<AiPromptOptionVo> listOptions() {
        List<AiPrompt> rows = promptMapper.selectList(Wrappers.<AiPrompt>lambdaQuery()
            .eq(AiPrompt::getDeleted, AiPromptConstants.NOT_DELETED)
            .orderByDesc(AiPrompt::getUpdateTime));
        List<AiPromptOptionVo> options = new ArrayList<>(rows.size());
        for (AiPrompt row : rows) {
            AiPromptOptionVo vo = new AiPromptOptionVo();
            vo.setPromptId(row.getPromptId());
            vo.setName(row.getName());
            vo.setCategory(row.getCategory());
            options.add(vo);
        }
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> promptIds) {
        if (promptIds == null || promptIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除 ID 不能为空");
        }
        for (Long promptId : promptIds) {
            AiPrompt row = getById(promptId);
            if (row == null) {
                continue;
            }
            row.setDeleted(AiPromptConstants.DELETED);
            promptMapper.updateById(row);
        }
    }

    private void saveContent(Long promptId, String content) {
        contentMapper.delete(Wrappers.<AiPromptContent>lambdaQuery()
            .eq(AiPromptContent::getPromptId, promptId)
            .eq(AiPromptContent::getVersionId, AiPromptConstants.DRAFT_VERSION_ID)
            .eq(AiPromptContent::getSectionKey, AiPromptConstants.CONTENT_SECTION_KEY));

        AiPromptContent row = new AiPromptContent();
        row.setPromptId(promptId);
        row.setVersionId(AiPromptConstants.DRAFT_VERSION_ID);
        row.setSectionKey(AiPromptConstants.CONTENT_SECTION_KEY);
        row.setContent(StrUtil.nullToEmpty(content));
        contentMapper.insert(row);
    }

    private String loadContent(Long promptId) {
        AiPromptContent row = contentMapper.selectOne(Wrappers.<AiPromptContent>lambdaQuery()
            .eq(AiPromptContent::getPromptId, promptId)
            .eq(AiPromptContent::getVersionId, AiPromptConstants.DRAFT_VERSION_ID)
            .eq(AiPromptContent::getSectionKey, AiPromptConstants.CONTENT_SECTION_KEY)
            .last("LIMIT 1"));
        return row == null ? "" : StrUtil.nullToEmpty(row.getContent());
    }

    private AiPromptVo toVo(AiPrompt row, boolean withContent) {
        AiPromptVo vo = new AiPromptVo();
        vo.setPromptId(row.getPromptId());
        vo.setName(row.getName());
        vo.setCategory(row.getCategory());
        vo.setDescription(row.getDescription());
        vo.setUpdateTime(row.getUpdateTime());
        if (withContent) {
            vo.setContent(loadContent(row.getPromptId()));
        }
        return vo;
    }

    private AiPrompt getById(Long promptId) {
        if (promptId == null) {
            return null;
        }
        AiPrompt row = promptMapper.selectById(promptId);
        if (row == null || row.getDeleted() == AiPromptConstants.DELETED) {
            return null;
        }
        return row;
    }
}
