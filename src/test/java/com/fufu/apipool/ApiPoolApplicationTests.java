package com.fufu.apipool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.fufu.apipool.domain.newapi.Channel;
import com.fufu.apipool.domain.newapi.ChannelPageData;
import com.fufu.apipool.domain.newapi.LoginRequest;
import com.fufu.apipool.domain.newapi.R;
import com.fufu.apipool.domain.newapi.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.HttpCookie;
import java.util.List;

@Slf4j
@SpringBootTest
class ApiPoolApplicationTests {


    @Test
    void contextLoads() {
        try {
            // 构建登录请求参数
            LoginRequest request = new LoginRequest();
            request.setUsername("you-username");
            request.setPassword("you-password");
            // 发送登录POST请求
            HttpRequest postRequest = HttpUtil.createPost("http://45.192.111.246:3000/api/user/login?turnstile=");
            postRequest.body(JSON.toJSONString(request));
            HttpResponse loginResponse = postRequest.execute();
            // 获取登录返回的cookie
            List<HttpCookie> cookies = loginResponse.getCookies();
            System.out.println("登录返回的Cookies: " + cookies);
            // 解析登录响应
            R<User> newApiR = JSON.parseObject(loginResponse.body(), new TypeReference<R<User>>() {});
            User user = newApiR.getData();


            // 构建数据查询GET请求
//            HttpRequest getRequest = HttpUtil.createGet("http://45.192.111.246:3000/api/data/?username=&start_timestamp=1751858998&end_timestamp=1751948998&default_time=hour");
            HttpRequest getRequest = HttpUtil.createGet("http://45.192.111.246:3000/api/channel/?p=1&page_size=10&id_sort=false&tag_mode=false");
            if (!cookies.isEmpty()) {
                getRequest.cookie(cookies.get(0));
            }
            if (user != null) {
                getRequest.header("New-Api-User", String.valueOf(user.getId()));
            }
            // 发送GET请求并输出结果


            HttpResponse dataResponse = getRequest.execute();
            log.info("dataResponse:{}",dataResponse);
//            R r = JSON.parseObject(dataResponse.body(), R.class);
//            log.info("r:{}",r);
//            ChannelPageData<Channel> channelPageData = JSON.parseObject(JSON.toJSONString(r.getData()), ChannelPageData.class);
//            log.info("channelPageData:{}",channelPageData);
//            List<Channel> items = channelPageData.getItems();
//            log.info("items:{}",items);

            R<ChannelPageData<Channel>> r = JSON.parseObject(
                    dataResponse.body(),
                    new TypeReference<R<ChannelPageData<Channel>>>() {}
            );
            log.info("r:{}", r);
            ChannelPageData<Channel> channelPageData = r.getData();
            log.info("channelPageData:{}", channelPageData);
            List<Channel> items = channelPageData.getItems();
            log.info("items:{}", items);

//
//
//            R<ChannelPageData<Channel>> channels = JSON.parseObject(loginResponse.body(), new TypeReference<R<ChannelPageData<Channel>>>() {});
//            log.info("channels:{}",channels);
//            List<Channel> data = channels.getData().getItems();
//            log.info("data:{}",data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





//    /**
//     *
//     */
//    @Test
//    void contextLoads() {
//
//        NewApiLoginRequest request = new NewApiLoginRequest();
//
//        request.setUsername("lizelin");
//        request.setPassword("U93OzLV7yBD06d9e");
//        HttpRequest post = HttpUtil.createPost("http://45.192.111.246:3000/api/user/login?turnstile=");
//
//        HttpRequest body = post.body(JSON.toJSONString(request));
//        HttpResponse execute = body.execute();
//
//
//        System.out.println(execute.getCookies());
//        NewApiR newApiR = JSON.parseObject(execute.body(), NewApiR.class);
//        System.out.println(newApiR.toString());
//        NewApiUser data = JSON.parseObject(newApiR.getData(),NewApiUser.class);
//        //
//        //http://45.192.111.246:3000/api/data/?username=&start_timestamp=1751858998&end_timestamp=1751948998&default_time=hour
//        HttpRequest get = HttpUtil.createGet("http://45.192.111.246:3000/api/data/?username=&start_timestamp=1751858998&end_timestamp=1751948998&default_time=hour");
//        List<HttpCookie> cookies = execute.getCookies();
//        get.cookie(cookies.get(0));
//        get.header("New-Api-User",String.valueOf(data.getId()));
//        HttpResponse execute1 = get.execute();
//        System.out.println(execute1.body());
//    }

}
