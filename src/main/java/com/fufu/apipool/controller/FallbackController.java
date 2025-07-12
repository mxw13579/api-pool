package com.fufu.apipool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lizelin
 * @date 2025-07-12 19:24
 */
@Controller
public class FallbackController {
    // 排除 api 前缀，所有其他 GET 都返回 index.html
    @GetMapping(value = "/{path:^(?!api|static|public).*$}/**")
    public String fallback() {
        return "forward:/index.html";
    }
}
