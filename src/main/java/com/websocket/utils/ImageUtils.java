package com.websocket.utils;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @description: PDF转换为图片，并保存工具类
 */
@Component
public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 图片文件格式
     */
    public static final String FORMAT_NAME = "png";
    /**
     * 图片文件后缀名
     */
    public static final String PNG_SUFFIX = ".png";
    /**
     * 压缩文件后缀名
     */
    public static final String ZIP_SUFFIX = ".zip";

    public static final String IMG_PATH = "D:\\data\\images\\pdf\\";

    /**
     * 用于将PDF文件转换为图片文件并保存
     *
     * @param file 获取的pdf文件
     * @throws Exception
     */
    public static void pdfToImage(MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        log.debug("将PDF文件转换为图片文件--开始");
        saveImageFile(file);
        log.debug("将PDF文件转换为图片文件--完成，耗时：{}ms",System.currentTimeMillis()-start);
    }

    /**
     * 将PDF文件转换为多张图片并放入一个压缩包中
     *
     * @param file SpringMVC获取的图片文件
     * @return 图片文件压缩包
     * @throws Exception 抛出异常
     */
    private static void saveImageFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        log.debug("pdf文件名称：{}",fileName);
        Document document = new Document();
        document.setByteArray(file.getBytes(), 0, file.getBytes().length, fileName);

        List<File> fileList = new ArrayList<>();
        //获取pdf页码
        log.debug("pdf文件信息：{}",document);
        log.debug("pdf文件页码：{}",document.getNumberOfPages());

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            log.debug("pdf文件当前页码：{}",i+1);
            BufferedImage image = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN,
                    Page.BOUNDARY_CROPBOX, 0F, 2.5F);
            //保存文件到指定路径
            // 文件名称可自定义  格式：/202201/.png
            String newFileName=getHashFilePath(i+1,fileName+(i + 1) + PNG_SUFFIX,IMG_PATH);
            //默认   文件名称：文件名称+pdf文档页面.png
//            File imageFile = new File(IMG_PATH + fileName+(i + 1) + PNG_SUFFIX);
            File imageFile = new File(newFileName);

            ImageIO.write(image, FORMAT_NAME, imageFile);
            image.flush();
            log.debug("当前文件信息：{}",imageFile);
            //fileList.add(imageFile);
        }
        document.dispose();
        log.debug("保存生成的pdf图片，结束！");
    }

    /**
     * @描述：使用hash打散方式创建图片路径
     */
    private static String getHashFilePath(int index,String sourceFileName, String location)
            throws Exception {
        // 获取图片的hash值
        Integer hashCode = sourceFileName.hashCode();
        String hashStr = Integer.toHexString(hashCode);
        // 截取十六进制的前四位
        String headHash = hashStr.substring(0, 4);
        // 用多级文件夹目录
        LocalDateTime now =LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        // 年月日
        StringBuilder baseFolder = new StringBuilder(
                location + File.separator
                        + format.format(now));
        // 根据新目录创建file对象
        File file = new File(baseFolder.toString());
        if (!file.isDirectory()) {
            // 如果目录不存在，则创建目录
            boolean flag = file.mkdirs();
            if (!flag) {
                return "";
            }
        }
        DateTimeFormatter milliSecondFormat = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSS");
        // 生成新的文件名
        return baseFolder + File.separator + index + "-" + milliSecondFormat.format(now)
                + headHash + PNG_SUFFIX;
    }

    public static void main(String[] args) {
//        LocalDate now = LocalDate.now();
        LocalDateTime now =LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSS");
        System.out.println(formatter.format(now));
        System.out.println(format.format(now));
    }


    /**
     * 对外的开放接口，用于将PDF文件转换为图片文件压缩包进行下载
     *
     * @param file SpringMVC获取的图片文件
     * @param response
     * @throws Exception
     */
    public static void pdfToImageZip(MultipartFile file, HttpServletResponse response) throws Exception {
        long start = System.currentTimeMillis();
        log.debug("将PDF文件转换为图片文件--开始");
        File zipFile = generateImageFile(file);
        log.debug("将PDF文件转换为图片文件--完成，耗时：{}ms",System.currentTimeMillis()-start);

        long zipStart = System.currentTimeMillis();
        log.debug("压缩并下载图片文件--开始");
        downloadZipFile(zipFile, response);
        log.debug("压缩并下载图片文件--完成，耗时：{}ms",System.currentTimeMillis()-zipStart);
        log.debug("完成，总耗时：{}ms",System.currentTimeMillis()-start);
    }


    /**
     * 将PDF文件转换为多张图片并放入一个压缩包中
     *
     * @param file SpringMVC获取的图片文件
     * @return 图片文件压缩包
     * @throws Exception 抛出异常
     */
    private static File generateImageFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        log.debug("pdf文件名称：{}",fileName);
        Document document = new Document();
        document.setByteArray(file.getBytes(), 0, file.getBytes().length, fileName);

        List<File> fileList = new ArrayList<>();
        //获取pdf页码
        log.debug("pdf文件信息：{}",document);
        log.debug("pdf文件页码：{}",document.getNumberOfPages());

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            log.debug("pdf文件当前页码：{}",i+1);
            BufferedImage image = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN,
                    Page.BOUNDARY_CROPBOX, 0F, 2.5F);
            File imageFile = new File(+(i + 1) + PNG_SUFFIX);
            log.debug("文件信息FORMAT_NAME：{}",FORMAT_NAME);
            //log.debug("文件信息FORMAT_NAME：{}",FORMAT_NAME);
            ImageIO.write(image, FORMAT_NAME, imageFile);
            image.flush();
            fileList.add(imageFile);
        }
        document.dispose();

        String directoryName = fileName.substring(0, fileName.lastIndexOf("."));
        File zipFile = new File(directoryName + ZIP_SUFFIX);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zipFile(fileList, zos);
        zos.close();
        return zipFile;
    }

    /**
     * 下载zip文件
     *
     * @param zipFile  zip压缩文件
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    private static void downloadZipFile(File zipFile, HttpServletResponse response) throws IOException {
        FileInputStream fis = new FileInputStream(zipFile);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);
        fis.close();

        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(zipFile.getName(), "UTF-8"));
        OutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
        zipFile.delete();
    }

    /**
     * 压缩文件
     *
     * @param inputFiles 具体需要压缩的文件集合
     * @param zos        ZipOutputStream对象
     * @throws IOException IO异常
     */
    private static void zipFile(List<File> inputFiles, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[1024];
        for (File file : inputFiles) {
            if (file.exists()) {
                if (file.isFile()) {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    int size = 0;
                    while ((size = bis.read(buffer)) > 0) {
                        zos.write(buffer, 0, size);
                    }
                    zos.closeEntry();
                    bis.close();
                    file.delete();
                } else {
                    File[] files = file.listFiles();
                    List<File> childrenFileList = Arrays.asList(files);
                    zipFile(childrenFileList, zos);
                }
            }
        }
    }

}
