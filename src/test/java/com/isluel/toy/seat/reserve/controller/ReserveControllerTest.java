package com.isluel.toy.seat.reserve.controller;

import com.isluel.toy.seat.AbstractContainerBase;
import com.isluel.toy.seat.reserve.service.ReserveService;
import com.isluel.toy.seat.reserve.web.ReserveController;
import com.isluel.toy.seat.vo.UsernameSession;
import com.isluel.toy.seat.waiting.service.WaitingService;
import com.isluel.toy.seat.waiting.web.WaitingRestController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = ReserveController.class) // Controller 관련 Bean 만 올림
public class ReserveControllerTest extends AbstractContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;
    @MockitoBean
    private ReserveService reserveService;

    @DisplayName("나의 예약 목록 화면을 표시힌다.")
    @Test
    void reserved() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        String username = "admin";
        String sessionId = "asdf123vzdf123";
        session.setAttribute("username", username);
        session.setAttribute("sessionId", sessionId);
        UsernameSession us = new UsernameSession();
        us.setUsername(username);
        us.setSessionId(sessionId);
        WaitingRestController.userSessionList.add(us);

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reserved")
                        .session(session))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @DisplayName("나의 예약 목록 화면 조회시 세션이 없으면 메인 화면으로 이동한다.")
    @Test
    void reservedNoSession() throws Exception {
        // given

        // when
        // then
        var url = mockMvc.perform(MockMvcRequestBuilders
                        .get("/reserved"))
                .andExpect(status().is(302))
                .andDo(print())
                .andReturn().getResponse().getRedirectedUrl();
        assertThat(url).isEqualTo("/?error=1");
    }

}
