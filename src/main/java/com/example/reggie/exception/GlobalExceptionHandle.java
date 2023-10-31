package com.example.reggie.exception;


import com.example.reggie.common.CustomException;
import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

//标识什么地方需要
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandle {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
//        exception.printStackTrace();
        log.error(exception.getMessage());
        if (exception.getMessage().contains("Duplicate entry")) {
            String[] split = exception.getMessage().split(" ");
            return R.error("用户名"+split[9]+"已存在");
        }

        return R.error("操作失败,未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception) {
//        exception.printStackTrace();
        log.error(exception.getMessage());
        return R.error(exception.getMessage());
    }
}
