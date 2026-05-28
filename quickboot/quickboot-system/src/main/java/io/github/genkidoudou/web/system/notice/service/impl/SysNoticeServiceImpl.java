package io.github.genkidoudou.web.system.notice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.notice.domain.SysNotice;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeQueryBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeVo;
import io.github.genkidoudou.web.system.notice.mapper.SysNoticeMapper;
import io.github.genkidoudou.web.system.notice.service.SysNoticeService;
import io.github.genkidoudou.web.system.notice.support.NoticeHtmlSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告服务实现。
 */
@Service
public class SysNoticeServiceImpl implements SysNoticeService {

    /** 消毒后正文最大字符数（与规格一致）。 */
    public static final int MAX_NOTICE_CONTENT_LENGTH = 65535;

    private final SysNoticeMapper mapper;

    public SysNoticeServiceImpl(SysNoticeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageInfo<SysNoticeVo> page(SysNoticeQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysNotice> w = Wrappers.<SysNotice>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getNoticeTitle()), SysNotice::getNoticeTitle, query.getNoticeTitle())
            .eq(StrUtil.isNotBlank(query.getNoticeType()), SysNotice::getNoticeType, query.getNoticeType())
            .like(StrUtil.isNotBlank(query.getCreateBy()), SysNotice::getCreateBy, query.getCreateBy())
            .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysNoticeVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysNotice row : mp.getRecords()) {
            SysNoticeVo vo = BeanUtil.copyProperties(row, SysNoticeVo.class);
            vo.setNoticeContent(null);
            rows.add(vo);
        }
        Page<SysNoticeVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public SysNoticeVo getById(Long noticeId) {
        SysNotice row = mapper.selectById(noticeId);
        if (row == null) {
            return null;
        }
        return BeanUtil.copyProperties(row, SysNoticeVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysNoticeBo req) {
        SysNotice entity = BeanUtil.copyProperties(req, SysNotice.class);
        entity.setNoticeContent(resolveAndValidateContent(req.getNoticeContent()));
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("0");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(currentOperator());
        entity.setUpdateBy(currentOperator());
        mapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysNoticeBo req) {
        SysNotice old = mapper.selectById(req.getNoticeId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "公告不存在或已删除");
        }
        SysNotice entity = BeanUtil.copyProperties(req, SysNotice.class);
        entity.setNoticeContent(resolveAndValidateContent(req.getNoticeContent()));
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("0");
        }
        entity.setCreateTime(old.getCreateTime());
        entity.setCreateBy(old.getCreateBy());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(currentOperator());
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除公告ID不能为空");
        }
        List<SysNotice> rows = mapper.selectBatchIds(noticeIds);
        if (rows.size() != noticeIds.size()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的公告ID");
        }
        mapper.deleteByIds(noticeIds);
    }

    /**
     * 消毒并校验正文；允许 null/空白表示无正文。
     */
    String resolveAndValidateContent(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        String safe = NoticeHtmlSanitizer.sanitize(trimmed);
        if (safe == null || safe.isBlank()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "公告内容不合法或全部为不允许的标签");
        }
        if (safe.length() > MAX_NOTICE_CONTENT_LENGTH) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "公告内容长度不能超过65535个字符");
        }
        return safe;
    }

    private static String currentOperator() {
        try {
            if (StpUtil.isLogin()) {
                return String.valueOf(StpUtil.getLoginId());
            }
        } catch (Exception ignored) {
            // 非 Web 线程或未登录
        }
        return "system";
    }
}
