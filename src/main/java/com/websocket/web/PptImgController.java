package com.websocket.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@RestController
@RequestMapping("ppt")
public class PptImgController {

    private static final Logger LOG = LoggerFactory.getLogger(PptImgController.class);
    @Autowired
    private WebSocketServer webSocketServer;

    @RequestMapping("/change/{sid}/{fileName}")
    public  String  change(@PathVariable("sid")String sid,
                           @PathVariable("fileName")String fileName) throws Exception {
        //触发websocket发送图片
        webSocketServer.sendPic(fileName,sid);
        return "执行完毕";
    }
}
