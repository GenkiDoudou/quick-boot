package io.github.genkidoudou.web.monitor.logininfor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.monitor.logininfor.domain.SysLogininfor;
import io.github.genkidoudou.web.monitor.logininfor.dto.SysLogininforExcelRow;
import io.github.genkidoudou.web.monitor.logininfor.dto.SysLogininforQueryBo;
import io.github.genkidoudou.web.monitor.logininfor.dto.SysLogininforVo;
import io.github.genkidoudou.web.monitor.logininfor.mapper.SysLogininforMapper;
import io.github.genkidoudou.web.monitor.logininfor.service.SysLogininforService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志服务实现。
 */
@Service
public class SysLogininforServiceImpl implements SysLogininforService {

    private final SysLogininforMapper mapper;

    public SysLogininforServiceImpl(SysLogininforMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageInfo<SysLogininforVo> page(SysLogininforQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysLogininfor> w = buildWrapper(query);
        applyOrder(w, query);
        Page<SysLogininfor> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysLogininforVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysLogininfor row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, SysLogininforVo.class));
        }
        Page<SysLogininforVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public void export(SysLogininforQueryBo query, HttpServletResponse response) {
        LambdaQueryWrapper<SysLogininfor> w = buildWrapper(query);
        applyOrder(w, query);
        List<SysLogininfor> list = mapper.selectList(w);
        List<SysLogininforExcelRow> rows = new ArrayList<>(list.size());
        for (SysLogininfor row : list) {
            rows.add(BeanUtil.copyProperties(row, SysLogininforExcelRow.class));
        }
        ExcelUtils.exportExcel(rows, "logininfor", SysLogininforExcelRow.class, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> infoIds) {
        if (infoIds == null || infoIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的日志");
        }
        mapper.deleteByIds(infoIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanAll() {
        mapper.delete(Wrappers.<SysLogininfor>lambdaQuery());
    }

    private LambdaQueryWrapper<SysLogininfor> buildWrapper(SysLogininforQueryBo query) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        return Wrappers.<SysLogininfor>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getIpaddr()), SysLogininfor::getIpaddr, query.getIpaddr())
            .like(StrUtil.isNotBlank(query.getUserName()), SysLogininfor::getUserName, query.getUserName())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysLogininfor::getStatus, query.getStatus())
            .ge(begin != null, SysLogininfor::getLoginTime, begin)
            .le(end != null, SysLogininfor::getLoginTime, end);
    }

    private void applyOrder(LambdaQueryWrapper<SysLogininfor> w, SysLogininforQueryBo query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String col = query.getOrderByColumn();
        if (StrUtil.isNotBlank(col) && "userName".equalsIgnoreCase(col.trim())) {
            w.orderBy(true, asc, SysLogininfor::getUserName);
            w.orderByDesc(SysLogininfor::getLoginTime);
            return;
        }
        if (StrUtil.isNotBlank(col) && "loginTime".equalsIgnoreCase(col.trim())) {
            w.orderBy(true, asc, SysLogininfor::getLoginTime);
            return;
        }
        w.orderByDesc(SysLogininfor::getLoginTime);
    }

    private LocalDateTime parseBeginTime(String beginTime) {
        if (StrUtil.isBlank(beginTime)) {
            return null;
        }
        return LocalDateTime.parse(beginTime.trim() + "T00:00:00");
    }

    private LocalDateTime parseEndTime(String endTime) {
        if (StrUtil.isBlank(endTime)) {
            return null;
        }
        return LocalDateTime.of(java.time.LocalDate.parse(endTime.trim()), LocalTime.MAX);
    }
}
