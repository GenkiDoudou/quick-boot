package io.github.genkidoudou.web.system.operlog.support;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.monitor.operlog.OperLogCapturePayload;
import io.github.genkidoudou.common.monitor.operlog.OperLogPublishingAspect;
import io.github.genkidoudou.common.monitor.operlog.OperLogSensitiveMasker;
import io.github.genkidoudou.web.system.operlog.domain.SysOperLog;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 将采集载荷组装为 {@link SysOperLog} 实体（含脱敏与状态映射）。
 */
@Component
public class OperLogAssembler {

    private static final int MAX_ERROR_MSG = 3800;

    private final ObjectMapper objectMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    public OperLogAssembler(ObjectMapper objectMapper, SysUserMapper userMapper, SysDeptMapper deptMapper) {
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
    }

    /**
     * @param payload 切面线程发布的载荷
     * @return 待插入实体
     */
    public SysOperLog assemble(OperLogCapturePayload payload) {
        MethodSignature ms = (MethodSignature) payload.getSignature();
        Method method = ms.getMethod();
        Class<?> declaring = ms.getDeclaringType();
        OperLogMetaResolver.ResolvedOperLogMeta meta = OperLogMetaResolver.resolve(method, declaring);

        String operParam = "";
        if (payload.getArgs() != null) {
            operParam = OperLogSensitiveMasker.mask(OperLogPublishingAspect.serializeParams(ms, payload.getArgs(), objectMapper));
        }
        String jsonResult;
        String errorMsg;
        int status;
        if (payload.getThrowable() != null) {
            status = 1;
            Throwable root = ExceptionUtil.getRootCause(payload.getThrowable());
            String msg = root != null && root.getMessage() != null ? root.getMessage() : ExceptionUtil.getRootCauseMessage(payload.getThrowable());
            errorMsg = StrUtil.sub(msg, 0, MAX_ERROR_MSG);
            jsonResult = "";
        } else {
            status = 0;
            errorMsg = "";
            jsonResult = OperLogSensitiveMasker.mask(OperLogPublishingAspect.serializeResult(payload.getResult(), objectMapper));
        }

        SysOperLog row = new SysOperLog();
        row.setTitle(meta.title());
        row.setBusinessType(meta.businessType());
        row.setMethod(declaring.getSimpleName() + "." + method.getName());
        row.setRequestMethod(StrUtil.blankToDefault(payload.getRequestMethod(), ""));
        row.setOperUrl(StrUtil.blankToDefault(payload.getRequestUri(), ""));
        row.setOperIp(StrUtil.blankToDefault(payload.getRequestIp(), ""));
        row.setOperLocation("");
        row.setOperParam(operParam);
        row.setJsonResult(jsonResult);
        row.setStatus(status);
        row.setErrorMsg(errorMsg);
        row.setOperTime(LocalDateTime.now());
        row.setCostTime(Math.max(0, payload.getEndTimeMs() - payload.getStartTimeMs()));
        row.setTraceId(payload.getTraceId());

        Long loginUserId = payload.getLoginUserId();
        if (loginUserId != null && loginUserId > 0) {
            row.setOperatorType(meta.operatorType() > 0 ? meta.operatorType() : 1);
            SysUser u = userMapper.selectById(loginUserId);
            if (u != null) {
                row.setOperName(StrUtil.blankToDefault(u.getUserName(), ""));
                row.setDeptName(resolveDeptName(u.getDeptId()));
            } else {
                row.setOperName("");
                row.setDeptName("");
            }
        } else {
            row.setOperatorType(0);
            row.setOperName("");
            row.setDeptName("");
        }
        return row;
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return "";
        }
        SysDept d = deptMapper.selectById(deptId);
        return d == null || d.getDeptName() == null ? "" : d.getDeptName();
    }
}
