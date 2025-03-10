package com.isluel.toy.seat.seat.service;

import com.isluel.toy.seat.seat.dto.SeatExpireResponse;
import com.isluel.toy.seat.waiting.service.WaitingService;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SeatSendMessageScheduler {

    public static String movieId = "1";

    @Autowired
    private SimpMessageSendingOperations sendingOperations;
    @Autowired
    private WaitingService waitingService;
    @Autowired
    private SeatService seatService;

    @Scheduled(initialDelay = 0)
    public void initialize() {

        seatService.initialize();
    }

    // 만료된 항목 Usage에서 삭제
    @Scheduled(fixedRate = 1000)
    public void expireUsage() {
        // usage 에서 일정 시간 지난 사람 내쫓기
        var expiredUser = waitingService.getExpiredUser(movieId);

        var userSessionMap = WaitingRestController.userSessionList;

        for (var user : expiredUser) {
            var userSessionOption = userSessionMap.stream().filter(m -> m.getUsername().equalsIgnoreCase(user))
                    .findFirst();

            if (userSessionOption.isEmpty()) {
                return;
            }

            var userSession = userSessionOption.get();

            var response = SeatExpireResponse.builder()
                    .url("/")
                    .build();

            log.info("KICK " + user);

            sendingOperations.convertAndSend("/sub/seat/kick", response
                , Map.of("sessionId", userSession.getSessionId()));
        }
    }

    // 좌석 List 가져오기
    @Scheduled(fixedRate = 1000)
    public void checkSeatReserve() {
        // movie Id에 해당되는 seat 정보 가져오기
        var seatList = seatService.findSeatListByMovieId(movieId);

        sendingOperations.convertAndSend("/sub/seat/" + movieId+ "/update", seatList);
    }
}
