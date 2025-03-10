package com.isluel.toy.seat.waiting.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Controller
public class WaitingController {

    @RequestMapping("/")
    public String enterHome() {

        return "home/waiting";
    }
}
