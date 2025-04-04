package com.suave.matematch.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Swagger配置类
 *
 * @author Suave
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        Contact contact = new Contact()
                .name("Suave") // 设置作者姓名
                .email("852635477@qq.com") // 设置作者邮箱 (可选)
                .url("https://github.com/suavion"); // 设置作者网站 (可选)

        return new OpenAPI()
                .info(new Info().title("伙伴匹配系统")
                        .description("伙伴匹配系统API文档")
                        .version("v1.0")
                        .contact(contact)
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("https://springshop.wiki.github.org/docs"));
    }

}