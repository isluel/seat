package com.isluel.toy.seat.seat.web;

import com.isluel.toy.seat.seat.service.SeatService;
import com.isluel.toy.seat.waiting.service.WaitingService;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Controller
@Slf4j
public class SeatController {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private SeatService seatService;

    // url에  sessionId 안보이게 하기 위해 redirect...
    @RequestMapping("/move")
    public String move(HttpServletRequest request, RedirectAttributes attributes, Model model) {
        var sessionId = request.getParameter("s");
        log.info("SESSION ID: " + sessionId);

        model.addAttribute("s", sessionId);
        attributes.addFlashAttribute("s", sessionId);

        return "redirect:/seat";
    }

    // 전달 받은 username을 url에 표시하기 싫어서 redirect로 데이터를 넘긴다음에 session에 넣어서 사용.
    @RequestMapping("/seat")
    public String seat(HttpServletRequest request, Model model) {

        // session Id, username 가져오기
        var sessionId = model.getAttribute("s");
        log.info("SESSION ID " + sessionId);
        var userSessionOption = WaitingRestController.userSessionList.stream().filter(u -> u.getSessionId().equals(sessionId))
                .findFirst();

        // 없으면 waiting 으로
        if (userSessionOption.isEmpty()) {
            log.info("NOT Exist userSession " + sessionId);
            return "redirect:/?error=1";
        }

        var userSession = userSessionOption.get();

        model.addAttribute("movieId", userSession.getMovieId());

        request.getSession().setAttribute("username", userSession.getUsername());
        request.getSession().setAttribute("sessionId", sessionId);

        // 들어와도 되는 사람인지 Check...
        log.info("USER SESSION " + userSession.toString());
        var available = waitingService.checkUsage(userSession.getMovieId(), userSession.getUsername());
        if (!available)
            return "redirect:/?error=2";

        // 좌석 정보 데이터.
        var seatList = seatService.findSeatListByMovieId(userSession.getMovieId());
        model.addAttribute("seats", seatList);

        return "home/seat";
    }
}
