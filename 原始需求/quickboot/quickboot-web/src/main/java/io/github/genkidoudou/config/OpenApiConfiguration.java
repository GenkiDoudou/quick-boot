package io.github.genkidoudou.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfiguration {

//    @Autowired
//    private ClientProperties clientProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()

                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))

                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic"))
                )
                .security(Arrays.asList(
                        new SecurityRequirement().addList("bearerAuth"),
                        new SecurityRequirement().addList("basicAuth")
                ));
    }

//    @Bean
//    public OperationCustomizer globalHeaderOperationCustomizer() {
//        Parameter example = new Parameter()
//                .in("header")
//                .name(clientProperties.getClientIdHeader())
//                .description("客户端id")
//                .required(true)
//                .schema(new StringSchema())
//                .example("cs");
//        ;
//        return (operation, handlerMethod) -> {
//            operation.addParametersItem(example);
//            return operation;
//        };
//    }
}