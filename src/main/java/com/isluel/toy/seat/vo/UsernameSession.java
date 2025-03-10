package com.isluel.toy.seat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsernameSession {
    private String username;
    private String sessionId;
    private String movieId = "1";
}
