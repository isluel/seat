package com.isluel.toy.seat.seat.web;

import com.isluel.toy.seat.seat.dto.SeatReserveRequest;
import com.isluel.toy.seat.seat.dto.SeatReserveResponse;
import com.isluel.toy.seat.seat.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatRestController {

    private final SeatService seatService;

    @RequestMapping("/reserve")
    public ResponseEntity<?> reserveSeat(@RequestBody SeatReserveRequest request, HttpServletRequest servletRequest) {
        try {
            var username = servletRequest.getSession().getAttribute("username");
            log.info("username: " + username);
            var isSuccess = seatService.checkReserve(request, (String) username);
            log.info("is success: " + isSuccess);

             return ResponseEntity.ok().body(SeatReserveResponse.builder().isSuccess(isSuccess).build());
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
