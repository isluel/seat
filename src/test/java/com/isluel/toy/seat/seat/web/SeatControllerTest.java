package com.isluel.toy.seat.seat.web;

import com.isluel.toy.seat.AbstractContainerBase;
import com.isluel.toy.seat.seat.service.SeatService;
import com.isluel.toy.seat.vo.UsernameSession;
import com.isluel.toy.seat.waiting.service.WaitingService;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = SeatController.class)
public class SeatControllerTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;
    @MockitoBean
    private SeatService seatService;

    @AfterEach
    void teardown() {
        WaitingRestController.userSessionList = new CopyOnWriteArrayList<>();
    }

    @DisplayName("나의 Seat 화면에서 좌석을 표시힌다")
    @Test
    void seat() throws Exception {
        // given
        String sessionId = "sessionIdTest123";
        String username = "user1";
        String movieId = "1";
        UsernameSession us = new UsernameSession();
        us.setUsername(username);
        us.setSessionId(sessionId);
        WaitingRestController.userSessionList.add(us);
        BDDMockito.given(waitingService.checkUsage(movieId, username)).willReturn(true);

        // when
        // then
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/seat")
                        .flashAttr("s", sessionId)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    @DisplayName("세션이 없는 경우 나의 Seat 화면으로 이동하지 않고 메인으로 이동한다.")
    @Test
    void seatNoSession() throws Exception {
        // given
        String sessionId = "sessionIdTest123";
        String sessionId2 = "sessionIdTest12333";
        String username = "user1";
        UsernameSession us = new UsernameSession();
        us.setUsername(username);
        us.setSessionId(sessionId2);
        WaitingRestController.userSessionList.add(us);

        // when
        // then
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/seat")
                        .flashAttr("s", sessionId)
                )
                .andExpect(status().is(302))
                .andReturn().getResponse();
        assertThat(result.getRedirectedUrl())
                .isEqualTo("/?error=1");
    }

    @DisplayName("대기인원이 아닌 경우 없는 경우 나의 Seat 화면으로 이동하지 않고 메인으로 이동한다.")
    @Test
    void seatNoUsage() throws Exception {
        // given
        String sessionId = "sessionIdTest123";
        String username = "user1";
        String movieId = "1";
        UsernameSession us = new UsernameSession();
        us.setUsername(username);
        us.setSessionId(sessionId);
        WaitingRestController.userSessionList.add(us);
        BDDMockito.given(waitingService.checkUsage(movieId, username)).willReturn(false);

        // when
        // then
        var result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/seat")
                        .flashAttr("s", sessionId)
                )
                .andExpect(status().is(302))
                .andReturn().getResponse();
        assertThat(result.getRedirectedUrl())
                .isEqualTo("/?error=2");
    }
}
