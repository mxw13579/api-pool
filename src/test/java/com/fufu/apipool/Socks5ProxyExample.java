package com.fufu.apipool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author lizelin
 */
@Slf4j
public class Socks5ProxyExample {
    public static void main(String[] args) throws Exception {
        // 构建请求
        HttpRequest request = HttpRequest.of("https://ipinfo.io")
                .method(Method.GET).timeout(60000);
        //我的socket5代理地址为 socks5://jing68434:yTEdiC2fqK@195.123.232.19:10090
        // SOCKS5 代理信息
        String proxyHost = "195.123.232.19";
        int proxyPort = 10090;
        String proxyUser = "jing68434";
        String proxyPass = "yTEdiC2fqK";

        request.basicProxyAuth(proxyUser,proxyPass);
        request.setHttpProxy(proxyHost,proxyPort);

        HttpResponse execute = request.execute();
        log.info("{}",execute.body());

    }
}
