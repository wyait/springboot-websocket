package com.websocket.utils;

import cn.hutool.core.io.FileUtil;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    //1m超过  执行压缩图片处理
    private static final long MAX_SIZE = 1024000;

    /**
     * 文件保存路径
     */
    public static final String IMG_PATH = "D:\\data\\images\\pdf\\20220117\\";
    //设置压缩图片的宽和高
    private int outputWidth = 1920; // 默认输出图片宽
    private int outputHeight = 1280; // 默认输出图片高
    //比例


    private boolean proportion = true; // 是否等比缩放标记(默认为等比缩放)

    /**
     * 用于将PDF文件转换为图片文件并保存
     *
     * @param file 获取的pdf文件
     * @throws Exception
     */
    public List<File> pdfToImage(MultipartFile file) throws Exception {
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
    private  List<File> saveImageFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        log.debug("pdf文件名称：{}",fileName);
        // 生成文件字节数组
        byte[] pngBytes = file.getBytes();
        List<byte[]> pngBytesList = pdf2Png(pngBytes);
        List<File> pngFileList = new ArrayList();
        for (int i = 0; i < pngBytesList.size(); i++) {
            // 文件名称可自定义  格式：/202201/.png
            String newFileName=getHashFilePath(i+1+"",IMG_PATH,fileName+(i + 1) + PNG_SUFFIX);
            //将文件字节码保存为图片
            File pngFile = FileUtil.writeBytes(pngBytesList.get(i), IMG_PATH+newFileName);
            /** 压缩图片 */
            long fileSize =  pngFile.length();
            log.debug("第{}张图片尺寸：{}",i+1,fileSize);
            if(fileSize > 0 && fileSize>MAX_SIZE){
                //超出指定尺寸，执行压缩文件处理  保存文件路径：IMG_PATH
                String compressFileName=getHashFilePath(i+1+"z",IMG_PATH,newFileName);
                log.debug("压缩文件名称：{}",compressFileName);
                compressPic(pngFile,IMG_PATH,compressFileName);
                // 删除老图片
                boolean tempflag = pngFile.delete();
                log.debug("删除原始图片结果：{}",tempflag);
                if (!tempflag) {
                    log.error("删除原始图片失败！");
                }
            }
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
    private static String getHashFilePath(String index,String imgPath,String sourceFileName)
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
        File file = new File(imgPath + File.separator
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

    /**
     * 图片压缩处理
     * @param file 文件
     * @param outLocation 压缩后文件保存位置
     * @param fileName 文件名称
     * @return
     */
    public String compressPic(File file,String outLocation,String fileName) {
        try {
            // 获得源文件
            if (!file.exists()) {
                log.error("需要压缩的文件不存在");
                return "";
            }
            Image img = ImageIO.read(file);
            // 判断图片格式是否正确
            if (img.getWidth(null) == -1) {
                log.debug("读取不到文件宽度!" + "<BR>");
                return "no";
            } else {
                int newWidth;
                int newHeight;
                // 判断是否是等比缩放
                if (this.proportion == true) {
                    // 【分母数值可调】 为等比缩放计算输出的图片宽度及高度  -0.1，保证1-2M的图片压缩之后的清晰和大小
                    double rate1 = ((double) img.getWidth(null))
                            / (double) outputWidth - 0.1;
                    double rate2 = ((double) img.getHeight(null))
                            / (double) outputHeight - 0.1;
                    // 根据缩放比率大的进行缩放控制
                    double rate = rate1 > rate2 ? rate1 : rate2;
                    newWidth = (int) (((double) img.getWidth(null)) / rate);
                    newHeight = (int) (((double) img.getHeight(null)) / rate);
                } else {
                    newWidth = outputWidth; // 输出的图片宽度
                    newHeight = outputHeight; // 输出的图片高度
                }
                log.debug("设置压缩文件的宽度newWidth：{}",newWidth);
                log.debug("设置压缩文件的高度newHeight：{}",newHeight);
                BufferedImage tag = new BufferedImage((int) newWidth,
                        (int) newHeight, BufferedImage.TYPE_INT_RGB);

                /*
                 * Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢
                 */
                tag.getGraphics().drawImage(
                        img.getScaledInstance(newWidth, newHeight,
                                Image.SCALE_SMOOTH), 0, 0, null);
                String fileurl = outLocation + fileName;
                log.debug("设置压缩文件的路径fileurl：{}",fileurl);
                FileOutputStream fos = new FileOutputStream(fileurl); // 输出到文件流

                // 旧的使用 jpeg classes进行处理的方法
                // JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
                // JPEGEncodeParam jep =
                // JPEGCodec.getDefaultJPEGEncodeParam(image_to_save);
                /* 压缩质量 */
                // jep.setQuality(per, true);
                // encoder.encode(image_to_save, jep);

                // 新的方法   【0.95】数值可调
                saveAsJPEG(100, tag, (float) 0.95, fos);

                fos.close();
                // JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                // encoder.encode(dstImage);
                /*
                 * FileOutputStream out = new FileOutputStream(outputDir +
                 * outputFileName); // JPEGImageEncoder可适用于其他图片类型的转换
                 * JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                 * encoder.encode(tag); out.close();
                 */
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("保存图片异常：{}",ex);
        }
        log.debug("压缩文件完成！");
        return "ok";
    }

    /**
     * 以JPEG编码保存图片
     * @param dpi  分辨率
     * @param image_to_save  要处理的图像图片
     * @param JPEGcompression  压缩比【数值可调】
     * @param fos 文件输出流
     * @throws IOException
     */
    public static void saveAsJPEG(Integer dpi, BufferedImage image_to_save,
                                  float JPEGcompression, FileOutputStream fos) throws IOException {

        // useful documentation at
        // http://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html
        // useful example program at
        // http://johnbokma.com/java/obtaining-image-metadata.html to output
        // JPEG data

        // old jpeg class
        // com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder =
        // com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(fos);
        // com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam =
        // jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);

        // Image writer
        // JPEGImageWriter imageWriter = (JPEGImageWriter)
        // ImageIO.getImageWritersBySuffix("jpeg").next();
        ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
        imageWriter.setOutput(ios);
        // and metadata
        IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(
                new ImageTypeSpecifier(image_to_save), null);

        if (dpi != null) {

            // old metadata
            // jpegEncodeParam.setDensityUnit(com.sun.image.codec.jpeg.JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            // jpegEncodeParam.setXDensity(dpi);
            // jpegEncodeParam.setYDensity(dpi);

            // new metadata
            Element tree = (Element) imageMetaData
                    .getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF")
                    .item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));

        }

        if (JPEGcompression >= 0 && JPEGcompression <= 1f) {

            // old compression
            // jpegEncodeParam.setQuality(JPEGcompression,false);

            // new Compression
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter
                    .getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(JPEGcompression);

        }

        // old write and clean
        // jpegEncoder.encode(image_to_save, jpegEncodeParam);

        // new Write and clean up
        imageWriter.write(imageMetaData,
                new IIOImage(image_to_save, null, null), null);
        ios.close();
        imageWriter.dispose();

    }

}
