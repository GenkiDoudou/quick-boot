package com.su60.quickboot.data.generator;


public class CodeGeneratorTest {

	public static void main(String[] args) {

		CodeGenerator.ResultHandler execute = CodeGenerator.create("jdbc:mysql://192.168.50.105:3406/quickboot?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true&autoReconnect=true&failOverReadOnly=false", "root", "rootroot")
				.addInclude("gen_table_column")
//				.addTablePrefix("gen_table")

				.author("luyanan")
				.parentPackage("com.su60.quickboot")
				.moduleName("generator")
				.registerTemplate(list -> {
					list.add(new CodeTemplate()
							.setTemplateFilePath("template/entity.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("entity")
							.setFileName("%sEntity.java"));

					list.add(new CodeTemplate()
							.setTemplateFilePath("template/do.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("dos")
							.setFileName("%sDo.java"));

					list.add(new CodeTemplate()
							.setTemplateFilePath("template/mapper.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("mapper")
							.setFileName("%sMapper.java"));
					list.add(new CodeTemplate()
							.setTemplateFilePath("template/mapper.xml.ftl")
							.setSavePath("resource/mapper/{moduleName}")
							.setFileName("%sMapper.xml"));
					list.add(new CodeTemplate()
							.setTemplateFilePath("template/service.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("service")
							.setFileName("I%sService.java"));

					list.add(new CodeTemplate()
							.setTemplateFilePath("template/serviceImpl.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("service.impl")
							.setFileName("%sServiceImpl.java"));

					list.add(new CodeTemplate()
							.setTemplateFilePath("template/controller.java.ftl")
							.setSavePath("src/main/java")
							.setPackagePath("controller")
							.setFileName("%sController.java"));

					// index.vue
					list.add(new CodeTemplate()
							.setTemplateFilePath("template/index.vue.ftl")
							.setSavePath("views/{moduleName}/{classNameLower}")
							.setFileName("index.vue"));


					// add-or-update.vue
					list.add(new CodeTemplate()
							.setTemplateFilePath("template/add-or-update.vue.ftl")
							.setSavePath("views/{moduleName}/{classNameLower}")
							.setFileName("add-or-update.vue"));
				})
				.execute();

		execute.saveFile("E:/code");


	}


}