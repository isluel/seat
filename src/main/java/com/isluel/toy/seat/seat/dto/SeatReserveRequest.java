package com.isluel.toy.seat.seat.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
public class SeatReserveRequest {
    private String movieId;
    private String seatId;

    @Builder
    private SeatReserveRequest(String movieId, String seatId) {
        this.movieId = movieId;
        this.seatId = seatId;
    }
}
