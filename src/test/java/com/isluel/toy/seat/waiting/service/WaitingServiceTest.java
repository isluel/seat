package com.isluel.toy.seat.waiting.service;

import com.isluel.toy.seat.AbstractContainerBase;
import com.isluel.toy.seat.waiting.dto.WaitingWaitingResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@ActiveProfiles("test")
@SpringBootTest
 public class WaitingServiceTest extends AbstractContainerBase {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ZSetOperations<String, String> zSetOperations;
    @Autowired
    private HashOperations<String, String, String> hashOperations;

    @AfterEach
    void teardown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("대기 인원을 조회한다.")
    @Test
    void checkWaitingList() {
        // given
        String movieId = "1";
        String adminUser = "admin";
        String user1User = "user1";
        zSetOperations.add(makeWaitingRedisKey(movieId), adminUser, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), user1User, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), adminUser, System.currentTimeMillis());

        // when
        Set<String> waitingList =  waitingService.checkWaitingList(movieId);

        // then
        assertThat(waitingList).hasSize(2)
                .contains(adminUser, user1User);
    }

    @DisplayName("대기 인원을 등록한다.")
    @Test
    void registerWaiting() {
        // given
        String movieId = "1";
        String username = "user2";

        // when
        Boolean result = waitingService.registerWaiting(movieId, username);

        // then
        assertThat(result).isTrue();
        Set<String> waitingList = zSetOperations.range(makeWaitingRedisKey(movieId), 0, -1);
        assertThat(waitingList).hasSize(1)
                .contains(username);
    }

    @DisplayName("대기 인원을 대기 목록에서 해제한다.")
    @Test
    void removeWaiting() {
        // given
        String movieId = "1";
        String username = "user123";
        String username2 = "user1234";
        zSetOperations.add(makeWaitingRedisKey(movieId), username, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());

        // when
        waitingService.removeWaiting(movieId, username);

        // then
        Set<String> waitingList = zSetOperations.range(makeWaitingRedisKey(movieId), 0, -1);
        assertThat(waitingList).hasSize(1)
                .contains(username2);
    }

    @DisplayName("앞의 대기 인원을 조회한다")
    @Test
    void checkWaiting() {
        // given
        String movieId = "1";
        String username = "user99";
        String username2 = "user23";
        zSetOperations.add(makeWaitingRedisKey(movieId), username, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());

        // when
        var front1 = waitingService.checkWaiting(movieId, username);
        var front2 = waitingService.checkWaiting(movieId, username2);

        // then
        assertThat(front1).isEqualTo(0);
        assertThat(front2).isEqualTo(1);
    }

    private String makeWaitingRedisKey(String movieId) {
        return "WAITING_" + movieId;
    }

    @DisplayName("사용 인원 목록에 사용자를 등록한다.")
    @Test
    void setUsage() {
        // given
        String movieId = "1";
        String username = "user123";

        // when
        Boolean result = waitingService.setUsage(movieId, username);

        // then
        assertThat(result).isTrue();
        Set<String> usageList = zSetOperations.range(makeUsageRedisKey(movieId), 0, -1);
        assertThat(usageList).hasSize(1)
                .contains(username);
        System.out.println(usageList);
    }

    @DisplayName("사용 인원 목록에서 사용자를 삭제한다.")
    @Test
    void removeUsage() {
        // given
        String movieId = "1";
        String username = "user123";
        zSetOperations.add(makeUsageRedisKey(movieId), username, System.currentTimeMillis());

        // when
        waitingService.removeUsage(movieId, username);

        // then
        var usageList = zSetOperations.range(makeUsageRedisKey(movieId), 0, -1);
        assertThat(usageList).hasSize(0);
    }

    @DisplayName("사용 인원 목록에서 전체 사용자를 삭제한다.")
    @Test
    void removeUsageAll() {
        // given
        String movieId = "1";
        String movieId2 = "2";
        String username = "user123";
        zSetOperations.add(makeUsageRedisKey(movieId), username, System.currentTimeMillis());
        zSetOperations.add(makeUsageRedisKey(movieId2), username, System.currentTimeMillis());

        // when
        waitingService.removeUsageAll(movieId);

        // then
        var usageList = zSetOperations.range(makeUsageRedisKey(movieId), 0, -1);
        var usageList2 = zSetOperations.range(makeUsageRedisKey(movieId2), 0, -1);
        assertThat(usageList).hasSize(0);
        assertThat(usageList2).hasSize(1);
    }

    @DisplayName("사용 목록에 사용자가 들어가 있는지 조회한다.")
    @Test
    void checkUsage() {
        // given
        String movieId = "1";
        String username = "admin1233";
        zSetOperations.add(makeUsageRedisKey(movieId), username, System.currentTimeMillis());

        // when
        var result = waitingService.checkUsage(movieId, username);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("사용 목록에 사용자가 안들어가 있는지 조회한다.")
    @Test
    void checkUsageNot() {
        // given
        String movieId = "1";
        String username = "admin1233";

        // when
        var result = waitingService.checkUsage(movieId, username);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("현재 사용 목록을 확인 한 후 대기 인원에서 추가한다.")
    @Test
    void checkAndSet() {
        // given
        String movieId = "1";
        String username1 = "user1";
        String username2 = "user2";
        String username3 = "user3";
        zSetOperations.add(makeWaitingRedisKey(movieId), username1, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());
        zSetOperations.add(makeUsageRedisKey(movieId), username3, System.currentTimeMillis());

        // when
        waitingService.checkAndSet(movieId);

        // then
        Set<String> waitingList = zSetOperations.range(makeWaitingRedisKey(movieId), 0, -1);
        Set<String> usageList = zSetOperations.range(makeUsageRedisKey(movieId), 0, -1);
        assertThat(waitingList).hasSize(1)
                .contains(username2);
        assertThat(usageList).hasSize(2)
                .contains(username1, username3);
    }

    @DisplayName("현재 사용 목록을 확인 한 후 가득 찼으면 대기 인원에서 추가하지 않는다.")
    @Test
    void checkAndSetNot() {
        // given
        String movieId = "1";
        String username1 = "user1";
        String username2 = "user2";
        String username3 = "user3";
        String username4 = "user4";
        zSetOperations.add(makeWaitingRedisKey(movieId), username1, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());
        zSetOperations.add(makeUsageRedisKey(movieId), username3, System.currentTimeMillis());
        zSetOperations.add(makeUsageRedisKey(movieId), username4, System.currentTimeMillis());

        // when
        waitingService.checkAndSet(movieId);

        // then
        Set<String> waitingList = zSetOperations.range(makeWaitingRedisKey(movieId), 0, -1);
        Set<String> usageList = zSetOperations.range(makeUsageRedisKey(movieId), 0, -1);
        assertThat(waitingList).hasSize(2)
                .contains(username1, username2);
        assertThat(usageList).hasSize(2)
                .contains(username3, username4);
    }

    @DisplayName("대기인원 목록에서 입력 수 만큼만 출력한다.")
    @Test
    void getWaitingUserByCount() {
        // given
        String movieId = "1";
        String username1 = "user1";
        String username2 = "user2";
        zSetOperations.add(makeWaitingRedisKey(movieId), username1, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());
        int count = 2;

        // when
        List<String> result = waitingService.getWaitingUserByCount(movieId, count);

        // then
        assertThat(result).hasSize(2)
                .contains(username1, username2);
    }

    @DisplayName("대기인원 목록에서 목록 인원 수 보다 많은 입력 수를 입력해도 목록 인원만 출력한다.")
    @Test
    void getWaitingUserByCountOver() {
        // given
        String movieId = "1";
        String username1 = "user1";
        String username2 = "user2";
        zSetOperations.add(makeWaitingRedisKey(movieId), username1, System.currentTimeMillis());
        zSetOperations.add(makeWaitingRedisKey(movieId), username2, System.currentTimeMillis());
        int count = 3;

        // when
        List<String> result = waitingService.getWaitingUserByCount(movieId, count);

        // then
        assertThat(result).hasSize(2)
                .contains(username1, username2);
    }

    @DisplayName("사용 목록에서 만료된 사용자를 가져온다.")
    @Test
    void getExpiredUser() {
        // given
        String movieId = "1";
        String username1 = "user1";
        String username2 = "user2";
        LocalDateTime now = LocalDateTime.now().plusDays(-1);
        ZonedDateTime zdt = now.atZone(ZoneId.of("America/Los_Angeles"));
        zSetOperations.add(makeUsageRedisKey(movieId), username1, zdt.toInstant().toEpochMilli());
        zSetOperations.add(makeUsageRedisKey(movieId), username2, System.currentTimeMillis());

        // when
        List<String> result = waitingService.getExpiredUser(movieId);

        // then
        assertThat(result).hasSize(1)
                .contains(username1);
    }


    private String makeUsageRedisKey(String movieId) {
        return "USAGE_" + movieId;
    }

    @Disabled
    @Test
    public void registerTest() {
        String subject = "44";

        waitingService.removeAll(subject);
        waitingService.registerWaiting(subject, "admin");
        waitingService.registerWaiting(subject, "admin1");
        var rank = waitingService.checkWaiting(subject, "admin1");

        System.out.println(rank);
    }

    @Disabled
    @Test
    public void initial() {
        var subjectId = "1";

        waitingService.removeAll(subjectId);
        waitingService.removeUsageAll(subjectId);
    }

    @Disabled
    @Test
    public void checkWaitingTest() {
        // subject list 가져와서 EnterRestController에 있는 목록 가져와 count 표시
        var subjectId = "44";
        waitingService.removeAll(subjectId);
        waitingService.removeUsageAll(subjectId);

        var rr = waitingService.registerWaiting(subjectId, "admin");
        rr = waitingService.registerWaiting(subjectId, "admin1");
        rr = waitingService.registerWaiting(subjectId, "admin2");
        rr = waitingService.registerWaiting(subjectId, "admin3");

        var range = waitingService.checkWaitingList(subjectId);
        var allowList = waitingService.checkAndSet(subjectId);
        var rank = 0L;
        for(var r : range) {
            String url = null;
            if (allowList.contains(r)) {
                url = "/seat";
            }
            var response = WaitingWaitingResponse.builder()
                    .username(r)
                    .rank(rank)
                    .url(url)
                    .build();
            System.out.println(response.toString());
            //sendingOperations.convertAndSend("/sub/waiting/" + subjectId + "/" + r, response);
            rank++;
        }
    }
}
