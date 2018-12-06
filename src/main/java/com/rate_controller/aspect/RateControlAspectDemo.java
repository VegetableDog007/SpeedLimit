package com.rate_controller.aspect;

import com.rate_controller.annotation.RateControl;
import com.rate_controller.utils.RequestInfoUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * 这里根据你不同的拦截需求
 * 指定不同的拦截器即可
 **/
@Aspect
@Configuration
@Import({com.rate_controller.config.RateControlConfig.class})
public class RateControlAspectDemo {

    private static final Logger logger = LoggerFactory.getLogger(RateControlAspectDemo.class);

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("redisLuaScript")
    private DefaultRedisScript<Number> redisLuaScript;

    // 有空把这个around这个'path1 && path2 ...'配置成从properties中获取即可
    @Around("execution(* com.rate_controller.controller ..*(..) )")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取被拦截的方法并判断其是否被限流注解标记
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateControl rateLimit = method.getAnnotation(RateControl.class);
        // 用于拼接限流计数器那个key
        Class<?> targetClass = method.getDeclaringClass();

        if (rateLimit != null) {
            // 从request中获取ip地址
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = RequestInfoUtil.getIpAddr(request);

            // 拼接key的字符串
            // 拼接方式：ip地址+类名+方法名+注解中自己给定的名字寄存起来
            // 这里不知道是不是因为key设置比较长那个redis-desktop那个插件好像是看不到这个key 用redus-cli查
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(ipAddress).append("-")
                    .append(targetClass.getName()).append("-")
                    .append(method.getName()).append("-")
                    .append(rateLimit.keySuffix());
            // 构造lua脚本的keys
            List<String> keys = new ArrayList<String>();
            keys.add(stringBuffer.toString());

            // 这里当时简单打印一下keys而已，以后删除掉没用的打印
            System.out.println(keys);
            for(String k : keys){
                System.out.println("k: "+k);
            }

            // 返回0表示不成功就是达到了限制次数
            // 正常返回的大于0小于等于注解中标识的特定次数即为正常操作
            Number number = redisTemplate.execute(redisLuaScript, keys, rateLimit.count(), rateLimit.time());
            if (number != null && number.intValue() != 0 && number.intValue() <= rateLimit.count()) {
                logger.info("{} of {} in {} second", number.toString(), rateLimit.count(), rateLimit.time());
                // 在限流次数中成功访问
                return joinPoint.proceed();
            }

        } else {
            // 这里沉默回避，如果能够进入这个是个奇葩，既然被标注了却又返回null
            return joinPoint.proceed();
        }
        // 表示在特定时间已经返回了限流次数，这里为了简单测试抛出异常
        // 这里根据你不同特定场景的定制限流后应对策略即可，可以做出多个不同类型的切面类
        throw new RuntimeException("");
    }
}
