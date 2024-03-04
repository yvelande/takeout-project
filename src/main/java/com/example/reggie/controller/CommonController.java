package com.example.reggie.controller;


import com.example.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    //读取yml中写的basepath
    @Value("${reggie.path}")
    private String basepath;
    @PostMapping("/upload")
    public Result<String>upload(MultipartFile file)  {
        log.info("获取文件：{}", file.toString());
        //找到文件地址
        File dir=new File(basepath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String originalFilename=file.getOriginalFilename();
        String suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName= UUID.randomUUID()+suffix;
        try {
            file.transferTo(new File(dir+fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success(fileName);
    }

    @GetMapping ("/download")
    public void download(String name, HttpServletResponse httpServletResponse)  {
        FileInputStream fis= null;
        ServletOutputStream os= null;
        try {
            fis = new FileInputStream(new File(basepath+name));
            os = httpServletResponse.getOutputStream();
            int len=0;
            byte[]buffer=new byte[2048];
            while ((len = fis.read(buffer)) != -1)
                os.write(buffer, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(os!=null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
