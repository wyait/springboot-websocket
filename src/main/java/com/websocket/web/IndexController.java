package com.websocket.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("log")
@Slf4j
public class IndexController {

    @RequestMapping("/test")
    @ResponseBody
    public String getMsg() {

        log.debug("===========debug信息>>>>");

        log.info("===========info信息>>>>");

        log.trace("I am trace log.");

        log.debug("I am debug log.");

        log.warn("I am warn log.");

        log.error("I am error log.");

        // 手动异常

//        System.out.println(1 / 0);

        // 会有中文乱码问题 TODO

        return "ok";

    }
}
