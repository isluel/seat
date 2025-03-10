package com.isluel.toy.seat.waiting.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author KYC. Infotrol Technology.
 * @version 1.0
 * DATE: 25. 2. 12.
 */
@Service
@Slf4j
public class WaitingService {

    // Capacity는 default 로 2 로 설정.
    @Getter
    private static final int CAPACITY = 2;
    // 만료 시간
    private static final int EXPIRE_SECOND = 60 * 1;

    @Autowired
    private ZSetOperations<String, String> zSetOperations;

    // 해당 Key의 모든 value를 지운다
    public Long removeAll(String movieId) {
        return zSetOperations.removeRange("WAITING_"+movieId, 0, -1);
    }

    // 전체 대기 인원 확인
    public Set<String> checkWaitingList(String movieId) {
        return zSetOperations.range("WAITING_" + movieId, 0, -1);
    }
    // 대기열 등록
    public Boolean registerWaiting(String movieId, String username) {
        long nowTime = System.currentTimeMillis();
        return zSetOperations.add("WAITING_" + movieId, username, nowTime);
    }
    // 대기열 해제
    public void removeWaiting(String movieId, String username) {
        zSetOperations.remove("WAITING_" + movieId, username);
    }
    // 앞에 몇명있는지 확인 해서 전달.
    public Long checkWaiting(String movieId, String username) {
        return zSetOperations.rank("WAITING_" + movieId, username);
    }


    // 사용인원 목록에 등록
    public Boolean setUsage(String movieId, String username) {
        long nowTime = System.currentTimeMillis();
        return zSetOperations.add("USAGE_" + movieId, username, nowTime);
    }
    // 기존의 사용 인원 삭제
    public void removeUsage(String movieId, String username) {
        zSetOperations.remove("USAGE_" + movieId, username);
    }
    // 모든 사용 인원 삭제
    public void removeUsageAll(String movieId) {
        zSetOperations.removeRange("USAGE_" + movieId, 0, -1);
    }
    // username이 사용인원이 맞는지 확인
    public boolean checkUsage(String movieId, String username) {
        zSetOperations.rank("USAGE_" + movieId, username);
        Double score = zSetOperations.score("USAGE_" + movieId, username);
        return score != null;
    }
    // 현재 사용 가능 인원 확인 후 전달
    public ArrayList<String> checkAndSet(String movieId) {
        // 추가 허용 인원 List
        var result = new ArrayList<String>();

        // 현재 사용하는 인원 확인
        var nowUseUserSet = zSetOperations.range("USAGE_" + movieId, 0, -1);
        if (nowUseUserSet == null || nowUseUserSet.isEmpty()) {
            result = getWaitingUserByCount(movieId, CAPACITY);
        } else {
            // 몇개인지 확인해서 해당 인원만 pop 해서 전달.
            var nowCount = nowUseUserSet.size();
            if (nowCount < CAPACITY)
                result = getWaitingUserByCount(movieId, CAPACITY - nowCount);
        }

        // 좌석 페이지 사용 인원에 업데이트.
        for (var r : result) {
            setUsage(movieId, r);
        }

        return result;
    }

    // 대기인원 중 count 수 만큼만 return.
    // popMin 은 window 에서 지원하지 않기 때문에
    // range 로 얻은다음에 remove 한다.
    public ArrayList<String> getWaitingUserByCount(String movieId, int count) {
        var result = new ArrayList<String>();
        // index 가 0 으로 부터 시작함.
        int endIndex = count - 1;
        var sortedSet = zSetOperations.range("WAITING_" + movieId, 0, endIndex);
        if (sortedSet != null && !sortedSet.isEmpty()) {
            result.addAll(sortedSet);

            zSetOperations.removeRange("WAITING_" + movieId, 0, endIndex);
        }
        return result;
    }

    // usage 에서 만료 Check.
    public ArrayList<String> getExpiredUser(String movieId) {
        var result = new ArrayList<String>();
        var now = LocalDateTime.now();

        var usageSet = zSetOperations.range("USAGE_" + movieId, 0, -1);
        if (usageSet == null)
            return result;

        for (var r : usageSet) {
            var score = zSetOperations.score("USAGE_" + movieId, r);

            // 값이 없으면 return
            if (score == null || score == 0) {
                return result;
            }

            var setTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(score.longValue()),
                    TimeZone.getDefault().toZoneId());

            // set time 에서 만료시간을 더한 시간이 지금 보다 크면 Expire
            if (setTime.plusSeconds(EXPIRE_SECOND).isBefore(now)) {
                result.add(r);

                // usage에서 삭제.
                removeUsage(movieId, r);
            }
        }

        return result;
    }
}
