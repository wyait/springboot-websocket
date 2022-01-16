package com.websocket.utils;

import cn.hutool.core.io.FileUtil;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: PDF转换为图片，并保存工具类
 * 【遗留问题：图片压缩，超过指定大小，进行压缩处理】
 */
@Component
public class PdfToImageUtils {

    private static final Logger log = LoggerFactory.getLogger(PdfToImageUtils.class);

    /**
     * 图片文件后缀名
     */
    public static final String PNG_SUFFIX = ".png";
    /**
     * dpi越大转换后越清晰，相对转换速度越慢
     */
    private static final Integer DPI = 100;

    /**
     * 转换后的图片类型
     */
    private static final String IMG_TYPE = "png";

    /**
     * 文件保存路径
     */
    public static final String IMG_PATH = "D:\\data\\images\\pdf\\";

    /**
     * 用于将PDF文件转换为图片文件并保存
     *
     * @param file 获取的pdf文件
     * @throws Exception
     */
    public static List<File> pdfToImage(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        log.debug("将PDF文件转换为图片文件--开始");
        List<File>  fileList=saveImageFile(file);
        log.debug("pdf文件集合信息：{}",fileList);
        log.debug("将PDF文件转换为图片文件--完成，耗时：{}ms",System.currentTimeMillis()-start);
        return fileList;
    }

    /**
     * 将PDF文件转换为多张图片并保存在指定路径
     *
     * @param file 获取的图片文件
     * @return 图片文件压缩包
     * @throws Exception 抛出异常
     */
    private static List<File> saveImageFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        log.debug("pdf文件名称：{}",fileName);
        // 生成文件字节数组
        byte[] pngBytes = file.getBytes();
        List<byte[]> pngBytesList = pdf2Png(pngBytes);
        List<File> pngFileList = new ArrayList();
        for (int i = 0; i < pngBytesList.size(); i++) {
            // 文件名称可自定义  格式：/202201/.png
            String newFileName=getHashFilePath(i+1,fileName+(i + 1) + PNG_SUFFIX);
            //将文件字节码保存为图片
            File pngFile = FileUtil.writeBytes(pngBytesList.get(i), IMG_PATH+newFileName);
            pngFileList.add(pngFile);
        }
        log.debug("保存生成的pdf图片，结束！");
        return pngFileList;
    }

    /**
     * `pdf` 转 `png`
     *
     * @param pdfBytes:
     *            pdf字节码
     * @return 转换后的png字节码
     */
    public static List<byte[]> pdf2Png(byte[] pdfBytes) throws Exception {
        List<byte[]> result = new ArrayList<>();
//        PDDocument document = PDDocument.load(pdfBytes);
//        PDDocumentInformation information= document.getDocumentInformation();
//        log.debug("pdf文件信息information：{}",information);
//        log.debug("pdf文件信息information：{}",information);
//        if(information!=null){
//
//        }
//        PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
//        if(names!=null){
//
//            PDDestinationNameTreeNode  treeNode=names.getDests();
//            if(treeNode!=null){
//                Map map=treeNode.getNames();
//                log.debug("1存在的附件信息：{}",map);
//            }
//
//            PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
//
//            if(efTree!=null){
//                Map existedNames = efTree.getNames();
//                log.debug("2存在的附件信息：{}",existedNames);
//            }
//        }

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            //判断是否包含附件
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); ++i) {
                document.getNumberOfPages();
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, DPI);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, IMG_TYPE, out);
                result.add(out.toByteArray());
            }
        }
        return result;
    }

    /**
     * @描述：使用hash打散方式创建图片路径
     * @示例： /202201/1-2022011403163095902822.png
     *
     */
    private static String getHashFilePath(int index,String sourceFileName)
            throws Exception {
        // 获取图片的hash值
        Integer hashCode = sourceFileName.hashCode();
        String hashStr = Integer.toHexString(hashCode);
        // 截取十六进制的前四位
        String headHash = hashStr.substring(0, 4);
        // 用多级文件夹目录
        LocalDateTime now =LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        // 年月
        // 根据新目录创建file对象
        File file = new File(IMG_PATH + File.separator
                + format.format(now));
        if (!file.isDirectory()) {
            // 如果目录不存在，则创建目录
            boolean flag = file.mkdirs();
            if (!flag) {
                return "";
            }
        }
        DateTimeFormatter milliSecondFormat = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSS");
        // 生成新的文件名  /202201/1-202201...png
        return File.separator
                + format.format(now) + File.separator + index + "-" + milliSecondFormat.format(now)
                + headHash + PNG_SUFFIX;
    }

}
