package io.github.genkidoudou.report.internal.token;

import com.alibaba.fastjson.JSONObject;
import io.github.genkidoudou.report.api.JimuAuthBridge;
import lombok.RequiredArgsConstructor;
import org.jeecg.modules.drag.service.IOnlDragExternalService;
import org.jeecg.modules.drag.vo.DragDictModel;
import org.jeecg.modules.drag.vo.DragLogDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JimuBI 字典与日志扩展：字典对接 quickboot {@code sys_dict}。
 */
@Component
@RequiredArgsConstructor
public class JimuDragExternalServiceImpl implements IOnlDragExternalService {

    private final JimuAuthBridge jimuAuthBridge;

    /** 批量字典：按 dictType 从 quickboot 字典桥接。 */
    @Override
    public Map<String, List<DragDictModel>> getManyDictItems(List<String> codeList,
                                                              List<JSONObject> tableDictList) {
        Map<String, List<DragDictModel>> result = new HashMap<>();
        if (codeList != null) {
            for (String code : codeList) {
                result.put(code, toDragModels(jimuAuthBridge.listDictByType(code)));
            }
        }
        // tableDictList 为表字典配置，需对接业务表时可在此扩展
        return result;
    }

    /** 单字典类型查询。 */
    @Override
    public List<DragDictModel> getDictItems(String dictCode) {
        return toDragModels(jimuAuthBridge.listDictByType(dictCode));
    }

    /** 预留：对接操作日志，当前为空实现。 */
    @Override
    public void addLog(DragLogDTO dragLogDTO) {
        // 可选：对接 operlog
    }

    /** 预留：对接操作日志，当前为空实现。 */
    @Override
    public void addLog(String logMsg, int logType, int operateType) {
        // 可选：对接 operlog
    }

    private static List<DragDictModel> toDragModels(List<JimuAuthBridge.JimuDictEntry> entries) {
        List<DragDictModel> list = new ArrayList<>();
        if (entries == null) {
            return list;
        }
        for (JimuAuthBridge.JimuDictEntry e : entries) {
            DragDictModel m = new DragDictModel();
            m.setValue(e.value());
            m.setText(e.text());
            list.add(m);
        }
        return list;
    }
}
