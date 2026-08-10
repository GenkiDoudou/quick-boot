package io.github.genkidoudou.quartz.internal.support;

import io.github.genkidoudou.quartz.internal.dto.SysJobInvokeTargetVo;
import io.github.genkidoudou.quartz.api.ITask;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 扫描容器中所有 {@link ITask} Bean，供前端下拉选择调用目标。
 */
@Component
public class JobInvokeTargetRegistry {

    private final ApplicationContext applicationContext;

    public JobInvokeTargetRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 按 Bean 名称排序返回可选项。
     */
    public List<SysJobInvokeTargetVo> listTargets() {
        Map<String, ITask> beans = applicationContext.getBeansOfType(ITask.class);
        List<SysJobInvokeTargetVo> list = new ArrayList<>(beans.size());
        for (Map.Entry<String, ITask> e : beans.entrySet()) {
            String beanName = e.getKey();
            String simpleName = e.getValue().getClass().getSimpleName();
            String label = beanName.equals(simpleName) ? beanName : beanName + "（" + simpleName + "）";
            list.add(new SysJobInvokeTargetVo(beanName, label));
        }
        list.sort(Comparator.comparing(SysJobInvokeTargetVo::getBeanName));
        return list;
    }
}
