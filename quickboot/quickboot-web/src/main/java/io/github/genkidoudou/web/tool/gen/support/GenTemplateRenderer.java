package io.github.genkidoudou.web.tool.gen.support;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FreeMarker 模板渲染（classpath:vm/quickboot）。
 */
@Component
public class GenTemplateRenderer {

    private Configuration configuration;

    @PostConstruct
    void init() throws IOException {
        configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "vm/quickboot");
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 渲染指定模板。
     */
    public String render(String templateName, GenContext context) throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, context.getModel());
    }

    /**
     * 渲染代码生成所需的全部模板。
     *
     * @return Zip 内相对路径 -> 内容
     */
    public Map<String, String> renderAll(GenContext context) throws IOException, TemplateException {
        String pkg = String.valueOf(context.getModel().get("packageName")).replace('.', '/');
        String module = String.valueOf(context.getModel().get("moduleName"));
        String className = String.valueOf(context.getModel().get("className"));
        String business = String.valueOf(context.getModel().get("businessName"));
        String tableName = String.valueOf(context.getModel().get("tableName"));

        Map<String, String> files = new LinkedHashMap<>();
        String[][] defs = {
            {"domain.java.ftl", "main/java/" + pkg + "/" + module + "/domain/" + className + ".java"},
            {"mapper.java.ftl", "main/java/" + pkg + "/" + module + "/mapper/" + className + "Mapper.java"},
            {"mapper.xml.ftl", "main/resources/mapper/" + module + "/" + className + "Mapper.xml"},
            {"service.java.ftl", "main/java/" + pkg + "/" + module + "/service/" + className + "Service.java"},
            {"serviceImpl.java.ftl", "main/java/" + pkg + "/" + module + "/service/impl/" + className + "ServiceImpl.java"},
            {"controller.java.ftl", "main/java/" + pkg + "/" + module + "/controller/" + className + "Controller.java"},
            {"queryBo.java.ftl", "main/java/" + pkg + "/" + module + "/dto/" + className + "QueryBo.java"},
            {"bo.java.ftl", "main/java/" + pkg + "/" + module + "/dto/" + className + "Bo.java"},
            {"vo.java.ftl", "main/java/" + pkg + "/" + module + "/dto/" + className + "Vo.java"},
            {"api.js.ftl", "vue/api/" + module + "/" + business + ".js"},
            {"index.vue.ftl", "vue/views/" + module + "/" + business + "/index.vue"},
            {"menu.sql.ftl", "sql/" + module + "/" + tableName + "_menu.sql"}
        };
        String vueTemplate = resolveVueTemplate(context);
        for (String[] def : defs) {
            String tpl = "index.vue.ftl".equals(def[0]) ? vueTemplate : def[0];
            files.put(def[1], render(tpl, context));
        }
        return files;
    }

    /**
     * 按 tplWebType 选择列表页模板：c7 组件或 Element Plus 原生。
     */
    private String resolveVueTemplate(GenContext context) {
        Object table = context.getModel().get("table");
        if (table instanceof io.github.genkidoudou.web.tool.gen.domain.GenTable genTable) {
            String webType = genTable.getTplWebType();
            if ("element-plus".equalsIgnoreCase(webType) || "element".equalsIgnoreCase(webType)) {
                return "index.vue.element.ftl";
            }
        }
        return "index.vue.ftl";
    }
}
