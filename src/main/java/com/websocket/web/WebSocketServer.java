package com.websocket.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ClassName: webSocketServer <br/>
 * Description: <br/>
 * date: 2020/2/4 11:05<br/>
 *
 * @author ccsert<br />
 * @since JDK 1.8
 */
@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        //加入set中
        webSocketSet.add(this);
        //在线人数加1
        addOnlineCount();
        LOG.info(sid + "连接成功" + "----当前在线人数为：" + onlineCount);
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        //在线人数减1
        subOnlineCount();
        //从set中删除
        webSocketSet.remove(this);
        LOG.info(sid + "已关闭连接" + "----剩余在线人数为：" + onlineCount);
    }

    /**
     * 接收客户端发送的消息时调用的方法
     *
     * @param message 接收的字符串消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) throws Exception {
        LOG.info(sid + "发送消息为:" + message);
        sendInfo(message, sid);
//        sendPic("1","1");
    }

    /**
     * 服务器主动提推送消息
     *
     * @param message 消息内容
     * @throws IOException io异常抛出
     */
    public void sendMessage(String message) throws IOException {

        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发消息功能
     *
     * @param message 消息内容
     * @param sid     房间号
     */
    public static void sendInfo(String message, @PathParam("sid") String sid) {
        LOG.info("推送消息到窗口" + sid + "，推送内容:" + message);
        for (WebSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                item.sendMessage(message);
            } catch (IOException e) {
                LOG.error("消息发送失败" + e.getMessage(), e);
                return;
            }
        }
    }

    /**
     * 主动推送到客户端，发文件流消息
     *
     * @param fileName 文件名称
     * @param sid 用户标识【测试只支持 1 和 2 】
     */
    public void sendPic(String fileName, String sid) throws Exception {
        LOG.info("【手动执行图片切换】推送消息到窗口,sid：{}，推送文件名称:{}",sid , fileName);
        LOG.info("【手动执行图片切换】webSocketSet={}",webSocketSet);
        for (WebSocketServer item : webSocketSet) {
            try {
                //不同sid，获取的文件不一样,目前只区分：1和其他
                String filePath="D:\\data\\images\\2022\\01\\13\\";
                if(sid=="1"){
                    filePath="D:\\data\\images\\2022\\02\\13\\";
                }
                filePath=filePath+fileName+".jpg";
                LOG.debug("【手动执行图片切换】图片路径：{}",filePath);
                //读取文件
                File file=new File(filePath);
                //生成字节数组
                if(file==null || !file.exists()){
                    LOG.debug("【手动执行图片切换】文件不存在，不执行消息推送");
                    return;
                }
                byte[] bytes=fileToByte(file);
                //转为ByteBuffer
                item.sendFileMessage(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                LOG.error("消息发送失败" + e.getMessage(), e);
                return;
            }
        }
        LOG.debug("【手动执行图片切换】推送成功");
        return;
    }

    public static byte[] fileToByte(File img) throws Exception {
        byte[] bytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage bi;
            bi = ImageIO.read(img);
            ImageIO.write(bi, "jpg", baos);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baos.close();
        }
        return bytes;
    }

    /**
     * 服务器主动提推送消息
     *
     * @param fileByteBuffer 文件流内容
     * @throws IOException io异常抛出
     */
    public void sendFileMessage(ByteBuffer fileByteBuffer) throws IOException {
        LOG.debug("【手动执行图片切换】推送fileByteBuffer:{}",fileByteBuffer);
        this.session.getBasicRemote().sendBinary(fileByteBuffer);
    }

    /**
     * 原子性的++操作
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
