package io.github.genkidoudou.web.monitor.job.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.monitor.job.config.JobMonitorProperties;
import io.github.genkidoudou.web.monitor.job.domain.SysJob;
import io.github.genkidoudou.web.monitor.job.dto.SysJobExcelRow;
import io.github.genkidoudou.web.monitor.job.dto.SysJobQueryBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobSaveBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobVo;
import io.github.genkidoudou.web.monitor.job.mapper.SysJobMapper;
import io.github.genkidoudou.web.monitor.job.quartz.CronUtils;
import io.github.genkidoudou.web.monitor.job.quartz.ITask;
import io.github.genkidoudou.web.monitor.job.quartz.JobExecutionLogger;
import io.github.genkidoudou.web.monitor.job.quartz.JobTaskInvoker;
import io.github.genkidoudou.web.monitor.job.quartz.JobTaskSnapshot;
import io.github.genkidoudou.web.monitor.job.quartz.ScheduleUtils;
import io.github.genkidoudou.web.monitor.job.service.SysJobService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务服务实现。
 */
@Slf4j
@Service
public class SysJobServiceImpl implements SysJobService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysJobMapper mapper;
    private final Scheduler scheduler;
    private final ApplicationContext applicationContext;
    private final JobMonitorProperties properties;
    private final JobTaskInvoker jobTaskInvoker;
    private final JobExecutionLogger jobExecutionLogger;

    public SysJobServiceImpl(
        SysJobMapper mapper,
        Scheduler scheduler,
        ApplicationContext applicationContext,
        JobMonitorProperties properties,
        JobTaskInvoker jobTaskInvoker,
        JobExecutionLogger jobExecutionLogger
    ) {
        this.mapper = mapper;
        this.scheduler = scheduler;
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.jobTaskInvoker = jobTaskInvoker;
        this.jobExecutionLogger = jobExecutionLogger;
    }

    @Override
    public PageInfo<SysJobVo> page(SysJobQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysJob> w = Wrappers.<SysJob>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getJobName()), SysJob::getJobName, query.getJobName())
            .eq(StrUtil.isNotBlank(query.getJobGroup()), SysJob::getJobGroup, query.getJobGroup())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysJob::getStatus, query.getStatus())
            .orderByDesc(SysJob::getCreateTime);
        Page<SysJob> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysJobVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysJob row : mp.getRecords()) {
            rows.add(toVo(row, false));
        }
        Page<SysJobVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public SysJobVo getById(Long jobId) {
        SysJob row = requireJob(jobId);
        return toVo(row, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysJobSaveBo bo) {
        validateCronAndTarget(bo);
        SysJob entity = fromBo(bo);
        entity.setStatus("1");
        entity.setCreateTime(LocalDateTime.now());
        mapper.insert(entity);
        try {
            ScheduleUtils.createScheduleJob(scheduler, JobTaskSnapshot.from(entity));
        } catch (SchedulerException e) {
            log.warn("注册调度任务失败 jobId={} cron={} cause={}", entity.getJobId(), entity.getCronExpression(),
                e.getMessage(), e);
            throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE,
                "注册调度任务失败：" + StrUtil.blankToDefault(e.getMessage(), "请检查 Quartz 表与锁数据是否已迁移"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(SysJobSaveBo bo) {
        if (bo.getJobId() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "任务ID不能为空");
        }
        requireJob(bo.getJobId());
        validateCronAndTarget(bo);
        SysJob entity = fromBo(bo);
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
        SysJob updated = requireJob(bo.getJobId());
        try {
            ScheduleUtils.updateScheduleJob(scheduler, JobTaskSnapshot.from(updated));
        } catch (SchedulerException e) {
            throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "更新调度任务失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的任务");
        }
        List<SysJob> jobs = mapper.selectList(Wrappers.<SysJob>lambdaQuery().in(SysJob::getJobId, jobIds));
        for (SysJob job : jobs) {
            try {
                scheduler.deleteJob(ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup()));
            } catch (SchedulerException e) {
                throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "删除调度任务失败");
            }
        }
        mapper.delete(Wrappers.<SysJob>lambdaQuery().in(SysJob::getJobId, jobIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long jobId, String status) {
        SysJob job = requireJob(jobId);
        SysJob patch = new SysJob();
        patch.setJobId(jobId);
        patch.setStatus(status);
        patch.setUpdateTime(LocalDateTime.now());
        mapper.updateById(patch);
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, job.getJobGroup());
        try {
            if ("0".equals(status)) {
                if (!scheduler.checkExists(jobKey)) {
                    ScheduleUtils.createScheduleJob(scheduler, JobTaskSnapshot.from(requireJob(jobId)));
                } else {
                    scheduler.resumeJob(jobKey);
                }
            } else if ("1".equals(status)) {
                if (scheduler.checkExists(jobKey)) {
                    scheduler.pauseJob(jobKey);
                }
            }
        } catch (SchedulerException e) {
            throw new WarningException(ErrorCodes.System.DEPENDENCY_UNAVAILABLE, "修改任务状态失败");
        }
    }

    @Override
    public void runOnce(Long jobId) {
        SysJob job = requireJob(jobId);
        JobTaskSnapshot snapshot = JobTaskSnapshot.from(job);
        LocalDateTime start = LocalDateTime.now();
        Exception error = null;
        try {
            jobTaskInvoker.invoke(snapshot);
        } catch (WarningException e) {
            error = e;
            throw e;
        } catch (Exception e) {
            error = e;
            throw new WarningException(ErrorCodes.Job.JOB_NOT_IN_SCHEDULER,
                "立即执行失败：" + StrUtil.blankToDefault(e.getMessage(), e.getClass().getSimpleName()));
        } finally {
            jobExecutionLogger.write(snapshot, start, error);
        }
        ensureRegisteredInScheduler(job, snapshot);
    }

    /**
     * 保证 Quartz 中已注册（供 Cron 调度）；与「立即执行」解耦，避免暂停/反序列化导致 trigger 空跑。
     */
    private void ensureRegisteredInScheduler(SysJob job, JobTaskSnapshot snapshot) {
        JobKey jobKey = ScheduleUtils.getJobKey(job.getJobId(), job.getJobGroup());
        try {
            if (!scheduler.checkExists(jobKey)) {
                ScheduleUtils.createScheduleJob(scheduler, snapshot);
            }
        } catch (SchedulerException e) {
            log.warn("同步 Quartz 注册失败 jobId={}，Cron 调度可能不可用: {}", job.getJobId(), e.getMessage());
        }
    }

    @Override
    public void export(SysJobQueryBo query, HttpServletResponse response) {
        LambdaQueryWrapper<SysJob> w = Wrappers.<SysJob>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getJobName()), SysJob::getJobName, query.getJobName())
            .eq(StrUtil.isNotBlank(query.getJobGroup()), SysJob::getJobGroup, query.getJobGroup())
            .eq(StrUtil.isNotBlank(query.getStatus()), SysJob::getStatus, query.getStatus())
            .orderByDesc(SysJob::getCreateTime);
        int max = Math.max(1, properties.getExportMaxRows());
        List<SysJob> list = mapper.selectList(w.last("LIMIT " + (max + 1)));
        if (list.size() > max) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导出数据超过上限（" + max + " 条），请缩小筛选条件");
        }
        List<SysJobExcelRow> rows = new ArrayList<>(list.size());
        for (SysJob row : list) {
            SysJobExcelRow excel = BeanUtil.copyProperties(row, SysJobExcelRow.class);
            rows.add(excel);
        }
        ExcelUtils.exportExcel(rows, "job", SysJobExcelRow.class, response);
    }

    private SysJob requireJob(Long jobId) {
        SysJob row = mapper.selectById(jobId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "任务不存在");
        }
        return row;
    }

    private void validateCronAndTarget(SysJobSaveBo bo) {
        if (StrUtil.isNotBlank(bo.getCronExpression())) {
            bo.setCronExpression(bo.getCronExpression().trim());
        }
        String fixed = CronUtils.fixSecondWildcardWithRestrictedMinute(bo.getCronExpression());
        if (fixed != null) {
            log.info("Cron 秒字段 * 与限定分钟组合将每秒触发，已自动修正: {} -> {}", bo.getCronExpression(), fixed);
            bo.setCronExpression(fixed);
        }
        if (!CronUtils.isValid(bo.getCronExpression())) {
            throw new WarningException(ErrorCodes.Job.CRON_INVALID, "Cron 表达式不正确");
        }
        Object bean;
        try {
            bean = applicationContext.getBean(bo.getInvokeTarget());
        } catch (Exception e) {
            bean = null;
        }
        if (bean == null) {
            throw new WarningException(ErrorCodes.Job.INVOKE_TARGET_NOT_FOUND, "调用目标 Bean 不存在");
        }
        if (!(bean instanceof ITask)) {
            throw new WarningException(ErrorCodes.Job.INVOKE_TARGET_NOT_TASK, "调用目标必须实现 ITask 接口");
        }
    }

    private SysJob fromBo(SysJobSaveBo bo) {
        SysJob entity = new SysJob();
        entity.setJobId(bo.getJobId());
        entity.setJobName(bo.getJobName());
        entity.setJobGroup(bo.getJobGroup());
        entity.setInvokeTarget(bo.getInvokeTarget());
        entity.setCronExpression(StrUtil.trim(bo.getCronExpression()));
        entity.setMisfirePolicy(bo.getMisfirePolicy());
        entity.setConcurrent(bo.getConcurrent());
        if (StrUtil.isNotBlank(bo.getStatus())) {
            entity.setStatus(bo.getStatus());
        }
        entity.setParams(bo.getParams());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    private SysJobVo toVo(SysJob row, boolean withNextTimes) {
        SysJobVo vo = BeanUtil.copyProperties(row, SysJobVo.class);
        if (CronUtils.isValid(row.getCronExpression())) {
            vo.setCronDescription(CronUtils.describe(row.getCronExpression()));
        }
        if (withNextTimes && CronUtils.isValid(row.getCronExpression())) {
            List<Date> next = CronUtils.getNextExecutions(row.getCronExpression(), 5);
            vo.setNextTimes(next.stream()
                .map(d -> DT_FMT.format(LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault())))
                .collect(Collectors.joining("\n")));
        }
        return vo;
    }
}
