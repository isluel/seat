package com.isluel.toy.seat.waiting.service;

import com.isluel.toy.seat.seat.dto.SeatExpireResponse;
import com.isluel.toy.seat.waiting.dto.WaitingWaitingResponse;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Service
@Slf4j
public class WaitingSendMessageScheduler {

    @Autowired
    private SimpMessageSendingOperations sendingOperations;
    @Autowired
    private WaitingService waitingService;


    @Scheduled(initialDelay = 0)
    public void initialize() {
        var movieId = "1";

        waitingService.removeAll(movieId);
        waitingService.removeUsageAll(movieId);
    }

    public static String movieId = "1";

    // 1 초 마다 실행해서
    // Redis의 waiting 와 Usage 확인하여 Usage 가 Capacity 보다 작으면 Waiting 에서 꺼내와 Usage 에 추가.
    @Scheduled(initialDelay = 2000, fixedRate = 1000)
    public void sendMessage() {
        // movie list 가져와서 EnterRestController 에 있는 목록 가져와 count 표시
        var range = waitingService.checkWaitingList(movieId);
        var allowList = waitingService.checkAndSet(movieId);
        var rank = WaitingService.getCAPACITY();

        var userSessionMap = WaitingRestController.userSessionList;

        for(var r : range) {
            String url = null;

            if (allowList.contains(r)) {
                url = "/move";
            }

            var userSessionOption = userSessionMap.stream().filter(m -> m.getUsername().equalsIgnoreCase(r))
                    .findFirst();

            if (userSessionOption.isEmpty()) {
                continue;
            }

            var userSession = userSessionOption.get();


            var response = WaitingWaitingResponse.builder()
                    .username(r)
                    .rank((long) rank)
                    .url(url)
                    .sessionId(userSession.getSessionId())
                    .build();

            sendingOperations.convertAndSend("/sub/waiting/wait", response
                    , Map.of("sessionId", userSession.getSessionId()));
            rank++;
        }
    }
}
