package com.example.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {

    //自定义元数据对象处理器

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充insert");
//        尝试
//        ThreadLocal<Long> threadLocal = new ThreadLocal<>();
//        log.info("你好？");
//        log.info("metahandler中id:{}",threadLocal.get());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());


    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充update");
//        ThreadLocal<Long> threadLocal = new ThreadLocal<>();
//        log.info("你好？");
//        log.info("metahandler中id:{}",threadLocal.get());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
