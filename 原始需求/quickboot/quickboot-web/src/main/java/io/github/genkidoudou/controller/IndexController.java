package io.github.genkidoudou.controller;

import io.github.genkidoudou.common.info.AppInfoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class IndexController {

    private final AppInfoProperties appInfoProperties;


    @GetMapping()
    public String index() {
        log.info("欢迎访问");
        return "欢迎使用" + appInfoProperties.getApplicationName() + "(" + appInfoProperties.getVersion() + ")管理系统";
    }


}
