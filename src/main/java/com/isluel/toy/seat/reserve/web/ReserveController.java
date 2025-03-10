package com.isluel.toy.seat.reserve.web;

import com.isluel.toy.seat.reserve.dto.ReserveListResponse;
import com.isluel.toy.seat.reserve.service.ReserveService;
import com.isluel.toy.seat.seat.service.SeatService;
import com.isluel.toy.seat.waiting.service.WaitingService;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

import static com.isluel.toy.seat.seat.service.SeatService.reserved;

@Controller
@Slf4j
public class ReserveController {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReserveService reserveService;

    @RequestMapping("/reserved")
    public String reserved(HttpServletRequest request, Model model) {

        var username = (String) request.getSession().getAttribute("username");
        var sessionId = (String) request.getSession().getAttribute("sessionId");
        var userSessionOption = WaitingRestController.userSessionList.stream().filter(u -> u.getSessionId().equals(sessionId))
                .findFirst();

        if (username == null || userSessionOption.isEmpty()) {
            log.info("NOT Exist username ");
            return "redirect:/?error=1";
        }

        // usernmae 으로 예약된 항목 가져오기.
        var reserveList = reserveService.findByUsername(username);

        model.addAttribute("reserveList", reserveList);

        // 사용 인원에서 제외
        waitingService.removeUsage(userSessionOption.get().getMovieId(), username);

        return "home/reserve";
    }
}
