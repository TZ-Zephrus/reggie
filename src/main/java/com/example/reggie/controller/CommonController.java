package com.example.reggie.controller;

import com.example.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    //文件上传
    public R<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传");
        log.info(file.toString());
        String originalFilename = file.getOriginalFilename();
        String s = originalFilename.substring(originalFilename.lastIndexOf("."));
        String name = UUID.randomUUID().toString();
        file.transferTo(new File( basePath + name + s));
        return R.success(name+s);
    }


    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //读文件
            FileInputStream fileInputStream = new FileInputStream(basePath+name);
            //写文件
            ServletOutputStream servletOutputStream = response.getOutputStream();

            //设置文件类型
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes)) != -1) {
                servletOutputStream.write(bytes, 0 ,len);
                servletOutputStream.flush();
            }
            servletOutputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
