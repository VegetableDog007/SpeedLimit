package com.rate_controller.controller;

import com.rate_controller.annotation.RateControl;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/***
 * 测试的controller
 */
@Controller
public class TestRateControlController {

    @Autowired
    private RedisTemplate redisTemplate;

    // 10 秒中，可以访问2次
    @RateControl(keySuffix = "test", time = 10, count = 2)
    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        // 这里用一个简单的计数器在页面上显示测试一下而已
        RedisAtomicInteger testCnt = new RedisAtomicInteger("testCnt", redisTemplate.getConnectionFactory());

        String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");

        return date + " number of successful calling test controller ：" + testCnt.getAndIncrement();
    }
}
