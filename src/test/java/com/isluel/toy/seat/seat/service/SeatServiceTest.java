package com.isluel.toy.seat.seat.service;

import com.isluel.toy.seat.AbstractContainerBase;
import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.reserve.vo.Reserved;
import com.isluel.toy.seat.seat.dto.SeatReserveRequest;
import com.isluel.toy.seat.seat.repository.MovieRepository;
import com.isluel.toy.seat.seat.repository.MovieSeatMapRepository;
import com.isluel.toy.seat.seat.repository.SeatRepository;
import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import com.isluel.toy.seat.seat.vo.SeatVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.set;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class SeatServiceTest {

    @Autowired
    private HashOperations<String, String, String> hashOperations;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieSeatMapRepository mapRepository;
    @Autowired
    private ReservedRepository reservedRepository;
    @Autowired
    private SeatService seatService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        reservedRepository.deleteAllInBatch();
        mapRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        seatService.reserved = new ConcurrentHashMap<>();
    }

    @DisplayName("저장되어 있는 Movie, Seat, 예약 정보로 Redis에 데이터를 초기화 한다.")
    @Test
    void initialize() {
        // given
        var seat1 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-1")
                .build());
        var seat2 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-2")
                .build());
        var movie = movieRepository.save(Movie.builder()
                .name("영화 A")
                .ordinal(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat1, seat2))
                .build());
        var movie2 = movieRepository.save(Movie.builder()
                .name("영화 B")
                .ordinal(2)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat1, seat2))
                .build());
        var firstMap = movie.getMovieSeatMaps().getFirst();
        reservedRepository.save(Reserved.builder()
                .username("admin").movieSeatMap(firstMap).build());

        // when
        seatService.initialize();

        // then
        assertThat(hashOperations.hasKey(movie.getId().toString(), firstMap.getSeat().getName()))
                .isEqualTo(true);
        assertThat(hashOperations.get(movie.getId().toString(), firstMap.getSeat().getName()))
                .isEqualTo("1");
        assertThat(hashOperations.hasKey(movie.getId().toString(), movie.getMovieSeatMaps().getLast().getSeat().getName()))
                .isEqualTo(true);
        assertThat(hashOperations.get(movie.getId().toString(), movie.getMovieSeatMaps().getLast().getSeat().getName()))
                .isEqualTo("0");

        assertThat(hashOperations.hasKey(movie2.getId().toString(), movie.getMovieSeatMaps().getLast().getSeat().getName()))
                .isEqualTo(true);
        assertThat(hashOperations.get(movie2.getId().toString(), movie2.getMovieSeatMaps().getLast().getSeat().getName()))
                .isEqualTo("0");
    }

    @DisplayName("Movie 아이디와, Seat Name 으로 예약된 값이 없으면 true 이다.")
    @Test
    void checkReserve() {
        // given
        String username = "admin";
        int theaterId = 1;
        String seatName = "A-1";
        var seat = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name(seatName)
                .build());
        var movie = movieRepository.save(Movie.builder()
                .name("영화 A")
                .ordinal(theaterId)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat))
                .build());
        hashOperations.put(movie.getId().toString(), seatName, "0");
        SeatReserveRequest request = SeatReserveRequest.builder()
                .movieId(movie.getId().toString()).seatId(seatName).build();

        // when
        var check = seatService.checkReserve(request, username);

        // then
        assertThat(check).isEqualTo(true);
        assertThat(check).isNotEqualTo(false);
    }

    @DisplayName("username, movie id, seat name 을 입력하여 데이터를 저장한다.")
    @Test
    void saveSeat() {
        // given
        String username = "admin";
        int theaterId = 1;
        String seatName = "A-1";
        var seat = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name(seatName)
                .build());
        var movie = movieRepository.save(Movie.builder()
                .name("영화 A")
                .ordinal(theaterId)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat))
                .build());
        SeatReserveRequest request = SeatReserveRequest.builder()
                .movieId(movie.getId().toString()).seatId(seatName).build();

        // when
        var reserved = seatService.saveSeat(request, username);

        // then
        assertThat(reserved).isNotNull()
                .extracting("username", "movieSeatMap.id")
                .contains(username, movie.getMovieSeatMaps().get(0).getId());
    }

    @DisplayName("MoveId 로 예약된 좌석 데이터를 모두 가져온다.")
    @Test
    void findSeatListByMovieId() {
        // given
        int theaterId = 1;
        var seat1 = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name("A-1")
                .build());
        var seat2 = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name("A-2")
                .build());
        var seat3 = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name("A-3")
                .build());
        var seat4 = seatRepository.save(Seat.builder()
                .theaterId(theaterId)
                .name("A-4")
                .build());
        var movie = movieRepository.save(Movie.builder()
                .name("영화 A")
                .ordinal(theaterId)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat1, seat2, seat3, seat4))
                .build());
        hashOperations.put(movie.getId().toString(), seat1.getName(), "1");
        hashOperations.put(movie.getId().toString(), seat2.getName(), "0");
        hashOperations.put(movie.getId().toString(), seat3.getName(), "1");
        hashOperations.put(movie.getId().toString(), seat4.getName(), "0");

        // when
        List<SeatVO> resultList = seatService.findSeatListByMovieId(movie.getId().toString());

        // then
        assertThat(resultList).hasSize(4)
                .extracting("name", "reserved")
                .containsExactlyInAnyOrder(
                        tuple(seat1.getName(), true),
                        tuple(seat2.getName(), false),
                        tuple(seat3.getName(), true),
                        tuple(seat4.getName(), false)
                );
    }
}