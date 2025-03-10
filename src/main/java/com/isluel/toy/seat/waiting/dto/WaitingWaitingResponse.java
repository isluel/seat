package com.isluel.toy.seat.waiting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaitingWaitingResponse {
    private String username;
    private Long rank;
    private String url;
    private String sessionId;
}
