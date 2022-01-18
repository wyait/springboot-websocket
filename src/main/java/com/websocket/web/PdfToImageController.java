package com.websocket.web;

import com.websocket.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * pdf 转 图片
 */
@RestController
@RequestMapping("pdfNew")
public class PdfToImageController {

    private static final Logger log = LoggerFactory.getLogger(PdfToImageController.class);

    @PostMapping("/to/image")
    public void pdfToImage(@RequestParam("file") MultipartFile file) throws Exception{
        log.debug("上传pdf信息：{}",file);
        ImageUtils.pdfToImage(file);
    }

    @PostMapping("/to/imageZip")
    public void pdfToImageZip(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception{
        log.debug("上传pdf信息：{}",file);
        ImageUtils.pdfToImageZip(file,response);
    }

}
