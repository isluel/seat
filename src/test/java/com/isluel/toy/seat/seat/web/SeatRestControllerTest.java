package com.isluel.toy.seat.seat.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isluel.toy.seat.seat.dto.SeatReserveRequest;
import com.isluel.toy.seat.seat.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = SeatRestController.class)
public class SeatRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;
    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("좌석 예약을 진행한다.")
    @Test
    void reserveSeat() throws Exception {
        // given
        String userName = "admin123";
        String seat = "A-1";
        String movieId = "1";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", userName);
        BDDMockito.given(seatService.checkReserve(any(), any())).willReturn(true);
        SeatReserveRequest request = SeatReserveRequest.builder()
                .seatId(seat)
                .movieId(movieId)
                .build();

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/seat/reserve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse();
    }
}
