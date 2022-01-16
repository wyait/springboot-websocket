//package com.websocket.utils;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.File;
//import java.util.List;
//
///**
// * <p>
// * 文件转换工具类$
// * </p>
// * @description
// */
//@Slf4j
//public class FileConvertUtil {
//
//
//    /**
//     * `pdf` 转 `png`
//     *
//     * @param pdfBytes: pdf字节码
//     * @return 转换后的png字节码
//     * @author zhengqing
//     * @date 2021/1/28 9:56
//     */
//    public static List<byte[]> pdfBytes2PngBytes(byte[] pdfBytes) {
//        return new Pdf2PngUtil().pdf2Png(pdfBytes);
//    }
//
//    /**
//     * `pdf` 转 `png`
//     *
//     * @param pdfBytes:    pdf字节码
//     * @param imgRootPath: 需转换的`png`文件路径
//     * @return png文件列表
//     * @author zhengqing
//     * @date 2021/1/28 9:56
//     */
//    public static List<File> pdfBytes2PngFileList(byte[] pdfBytes, String imgRootPath) {
//        return new Pdf2PngUtil().pdf2Png(pdfBytes, imgRootPath);
//    }
//
//}
