package io.github.genkidoudou.web.workflow.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.config.WorkflowProperties;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 工作流代码节点执行器：在隔离线程中运行 JavaScript（Rhino）或 Python 子进程，并解析 main 返回值。
 */
@Component
public class WorkflowCodeExecutor {

    private static final String LANGUAGE_JAVASCRIPT = "javascript";
    private static final String LANGUAGE_PYTHON = "python";

    private final WorkflowProperties.CodeNode config;

    public WorkflowCodeExecutor(WorkflowProperties properties) {
        this.config = properties.getCodeNode();
    }

    /**
     * 执行用户代码并返回 main 函数的结果对象。
     *
     * @param language   javascript / python
     * @param code       用户代码，须定义 main 入口
     * @param params     输入参数映射，代码内通过 params['key'] 访问
     * @param timeoutMs  超时毫秒
     * @return 解析后的 Map 结果
     */
    public Map<String, Object> execute(String language, String code, Map<String, Object> params, long timeoutMs) {
        if (!config.isEnabled()) {
            throw new IllegalStateException("代码节点未启用");
        }
        if (StrUtil.isBlank(code)) {
            throw new IllegalArgumentException("代码不能为空");
        }
        if (code.length() > config.getMaxCodeLength()) {
            throw new IllegalArgumentException("代码长度超过限制（最大 " + config.getMaxCodeLength() + " 字符）");
        }
        long safeTimeout = Math.max(100L, Math.min(timeoutMs, config.getMaxTimeoutMs()));
        String lang = StrUtil.blankToDefault(language, LANGUAGE_JAVASCRIPT).trim().toLowerCase();

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wf-code-node");
            t.setDaemon(true);
            return t;
        });
        try {
            Future<Map<String, Object>> future = executor.submit(() -> {
                if (LANGUAGE_PYTHON.equals(lang)) {
                    return executePython(code, params);
                }
                return executeJavaScript(code, params);
            });
            return future.get(safeTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new IllegalStateException("代码执行超时（" + safeTimeout + " ms）");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new IllegalStateException(cause.getMessage() == null ? "代码执行失败" : cause.getMessage(), cause);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("代码执行被中断");
        } finally {
            executor.shutdownNow();
        }
    }

    private Map<String, Object> executeJavaScript(String code, Map<String, Object> params) {
        Context cx = Context.enter();
        try {
            cx.setLanguageVersion(Context.VERSION_ES6);
            cx.setOptimizationLevel(-1);
            Scriptable scope = cx.initStandardObjects();
            Scriptable paramsObject = toJsObject(params, scope);
            ScriptableObject.putProperty(scope, "params", paramsObject);

            String runnable = normalizeJavaScriptForRhino(code);
            cx.evaluateString(scope, runnable, "userCode.js", 1, null);
            Object mainFn = scope.get("main", scope);
            if (!(mainFn instanceof Callable callable)) {
                throw new IllegalStateException("JavaScript 代码须定义 function main({ params }) 或 function main(params) 并 return 对象");
            }
            NativeObject argsObject = new NativeObject();
            argsObject.put("params", argsObject, paramsObject);
            Object ret = callable.call(cx, scope, scope, new Object[]{argsObject});
            return toPlainMap(ret);
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException illegal) {
                throw illegal;
            }
            throw new IllegalStateException("JavaScript 执行失败: " + ex.getMessage(), ex);
        } finally {
            Context.exit();
        }
    }

    /**
     * 将 Java Map 转为 Rhino {@link NativeObject}，避免 {@link Context#javaToJS} 生成 NativeJavaMap
     * 导致 JS 中 {@code params.key} 无法读取业务字段。
     */
    private static Scriptable toJsObject(Map<String, Object> map, Scriptable scope) {
        NativeObject object = new NativeObject();
        if (map == null || map.isEmpty()) {
            return object;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            object.put(entry.getKey(), object, toJsValue(entry.getValue(), scope));
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    private static Object toJsValue(Object value, Scriptable scope) {
        if (value instanceof Map<?, ?> map) {
            NativeObject object = new NativeObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                object.put(String.valueOf(entry.getKey()), object, toJsValue(entry.getValue(), scope));
            }
            return object;
        }
        if (value instanceof List<?> list) {
            NativeArray array = new NativeArray(list.size());
            for (int i = 0; i < list.size(); i++) {
                array.put(i, array, toJsValue(list.get(i), scope));
            }
            return array;
        }
        return Context.javaToJS(value, scope);
    }

    /**
     * Rhino 不支持 ES 参数解构，将 {@code function main({ params })} 转为显式取参写法。
     */
    private static String normalizeJavaScriptForRhino(String code) {
        if (StrUtil.isBlank(code)) {
            return code;
        }
        String normalized = code;
        normalized = normalized.replaceAll(
            "(?m)^(\\s*)(async\\s+)?function\\s+main\\s*\\(\\s*\\{\\s*params\\s*\\}\\s*\\)\\s*\\{",
            "$1$2function main(__wfArgs) { var params = (__wfArgs && __wfArgs.params !== undefined) ? __wfArgs.params : __wfArgs;"
        );
        return normalized;
    }

    private Map<String, Object> executePython(String code, Map<String, Object> params) throws Exception {
        String wrapper = """
            import json, sys

            def __wf_run():
            %s
                if 'main' not in dir():
                    raise RuntimeError('Python 代码须定义 def main(args) 并 return 字典')
                payload = json.loads(sys.stdin.read())
                result = main({'params': payload})
                if result is None:
                    raise RuntimeError('main 必须 return 字典对象')
                if not isinstance(result, dict):
                    raise RuntimeError('main 必须 return 字典对象')
                print(json.dumps(result, ensure_ascii=False))

            if __name__ == '__main__':
                __wf_run()
            """.formatted(indentPythonUserCode(code));

        ProcessBuilder pb = new ProcessBuilder(config.getPythonCommand(), "-c", wrapper);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        try (var out = process.getOutputStream()) {
            out.write(JSONUtil.toJsonStr(params).getBytes(StandardCharsets.UTF_8));
        }
        String output;
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.lines().reduce((a, b) -> a + "\n" + b).orElse("").trim();
        }
        boolean finished = process.waitFor(config.getMaxTimeoutMs(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("Python 执行超时");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(StrUtil.isBlank(output) ? "Python 执行失败" : output);
        }
        if (StrUtil.isBlank(output)) {
            throw new IllegalStateException("Python 未返回有效 JSON 输出");
        }
        JSONObject json = JSONUtil.parseObj(output);
        return new LinkedHashMap<>(json);
    }

    private static String indentPythonUserCode(String code) {
        String[] lines = code.replace("\r\n", "\n").split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append("    ").append(line).append('\n');
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toPlainMap(Object value) {
        if (value == null || value instanceof Undefined) {
            throw new IllegalStateException("main 必须 return 对象");
        }
        if (value instanceof NativeJavaObject njo) {
            Object unwrapped = njo.unwrap();
            if (unwrapped instanceof Map<?, ?> map) {
                return normalizeMap(map);
            }
        }
        if (value instanceof NativeObject nativeObject) {
            Map<String, Object> map = new LinkedHashMap<>();
            Object[] ids = nativeObject.getIds();
            for (Object id : ids) {
                String key = String.valueOf(id);
                map.put(key, toPlainValue(nativeObject.get(key, nativeObject)));
            }
            return map;
        }
        if (value instanceof Map<?, ?> map) {
            return normalizeMap(map);
        }
        if (value instanceof String str) {
            JSONObject json = JSONUtil.parseObj(str);
            return new LinkedHashMap<>(json);
        }
        throw new IllegalStateException("main 必须 return 对象");
    }

    private static Map<String, Object> normalizeMap(Map<?, ?> source) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            map.put(String.valueOf(entry.getKey()), toPlainValue(entry.getValue()));
        }
        return map;
    }

    private static Object toPlainValue(Object value) {
        if (value == null || value instanceof Undefined) {
            return null;
        }
        if (value instanceof NativeJavaObject njo) {
            return toPlainValue(njo.unwrap());
        }
        if (value instanceof NativeObject nativeObject) {
            return toPlainMap(nativeObject);
        }
        if (value instanceof NativeArray nativeArray) {
            List<Object> list = new ArrayList<>();
            long length = nativeArray.getLength();
            for (long i = 0; i < length; i++) {
                list.add(toPlainValue(nativeArray.get((int) i, nativeArray)));
            }
            return list;
        }
        if (value instanceof Map<?, ?> map) {
            return normalizeMap(map);
        }
        if (value instanceof List<?> list) {
            List<Object> normalized = new ArrayList<>(list.size());
            for (Object item : list) {
                normalized.add(toPlainValue(item));
            }
            return normalized;
        }
        if (value instanceof Number number) {
            double n = number.doubleValue();
            if (Double.isNaN(n) || Double.isInfinite(n)) {
                throw new IllegalStateException("代码返回值包含非法数字（NaN 或 Infinity）");
            }
            if (value instanceof Double || value instanceof Float) {
                return n;
            }
            return value;
        }
        return value;
    }
}
