package com.isluel.toy.seat.waiting.web;

import com.isluel.toy.seat.vo.UsernameSession;
import com.isluel.toy.seat.waiting.dto.WaitingExitRequest;
import com.isluel.toy.seat.waiting.dto.WaitingRegisterRequest;
import com.isluel.toy.seat.waiting.dto.WaitingWaitingResponse;
import com.isluel.toy.seat.waiting.service.WaitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@RestController
@Slf4j
public class WaitingRestController {

    private static Map<String, Set<String>> movieSubscript = new ConcurrentHashMap<>();

    public static List<UsernameSession> userSessionList = new CopyOnWriteArrayList<>();

    @Autowired
    private SimpMessageSendingOperations sendingOperations;
    @Autowired
    private WaitingService waitingService;

    @MessageMapping("/waiting/register")
    public void registerEnter(WaitingRegisterRequest request,
                              @Header("simpSessionId") String sessionId) {
        var movieId = request.getMovieId();
        var username = request.getUsername();
        movieSubscript.computeIfAbsent(movieId, k -> ConcurrentHashMap.newKeySet()).add(username);

        log.info("simp SESSION ID: " + sessionId);
        userSessionList.add(UsernameSession.builder()
                        .sessionId(sessionId)
                        .movieId(movieId)
                        .username(username).build());

        // 대기열 등록
        waitingService.registerWaiting(movieId, username);
        // 현재 count 반환
        var rank = waitingService.checkWaiting(movieId, username);
        var response = WaitingWaitingResponse.builder()
                        .username(username)
                        .rank(rank)
                        .url(null)
                        .build();

        sendingOperations.convertAndSend("/sub/waiting/" + movieId + "/" + username, response);
    }

    @MessageMapping("/waiting/exit")
    public void exitEnter(WaitingExitRequest request, @Header("simpSessionId") String sessionId) {
        var movieId = request.getMovieId();

        var userSessionOption = WaitingRestController.userSessionList.stream().filter(u -> u.getSessionId().equals(sessionId))
                .findFirst();

        if (userSessionOption.isPresent()) {
            var username = userSessionOption.get().getUsername();
            System.out.println("waiting Exit "+ movieId + " " + username);
            movieSubscript.getOrDefault(movieId, Collections.emptySet()).remove(username);
            waitingService.removeWaiting(movieId, username);
        }
    }
}
