package com.websocket.web;

import com.websocket.utils.PdfToImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * pdf转为图片
 * 【pdfbox】
 */
@RestController
@RequestMapping("pdf")
public class PdfToImgController {
    private static final Logger log = LoggerFactory.getLogger(PdfToImageController.class);

    @PostMapping("/to/image")
    public void pdfToImage(@RequestParam("file") MultipartFile file) throws Exception{
        log.debug("上传pdf信息：{}",file);
        log.debug("上传pdf文件尺寸：{}",file.getSize());
        List<File> fileList =PdfToImageUtils.pdfToImage(file);
        log.debug("解析pdf生成图片文件信息：{}",fileList);
    }
}
