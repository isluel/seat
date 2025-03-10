package com.isluel.toy.seat.reserve.service;

import com.isluel.toy.seat.reserve.dto.ReserveListResponse;
import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.reserve.vo.Reserved;
import com.isluel.toy.seat.seat.repository.MovieRepository;
import com.isluel.toy.seat.seat.repository.MovieSeatMapRepository;
import com.isluel.toy.seat.seat.repository.SeatRepository;
import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@ActiveProfiles("test")
public class ReserveServiceTest {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private MovieSeatMapRepository movieSeatMapRepository;
    @Autowired
    private ReservedRepository reservedRepository;
    @Autowired
    ReserveService reserveService;

    @AfterEach
    void tearDown() {
        reservedRepository.deleteAllInBatch();
        movieSeatMapRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
    }

    @Disabled
    @Test
    public void speedTest() {
        String username = "admin";
        var result = new ArrayList<ReserveListResponse>();

        long startMs = System.currentTimeMillis();
        System.out.println("====Start");

        var reservedList =  reservedRepository.findAllByUsername(username);
        for (var reserved : reservedList) {
            var map = reserved.getMovieSeatMap();
            var movie = movieRepository.findById(map.getMovie().getId()).orElse(null);
            var seat = seatRepository.findById(map.getSeat().getId()).orElse(null);
            if (movie == null || seat == null)
                continue;

            result.add(ReserveListResponse.builder().movieName(movie.getName()).seatName(seat.getName()).build());
        }

        long endMs = System.currentTimeMillis();
        System.out.println("====End: " + (endMs - startMs));
        // 7871, 8269
    }

    @Disabled
    @Test
    public void speedTest2() {
        String username = "admin";
        var result = new ArrayList<ReserveListResponse>();

        long startMs = System.currentTimeMillis();
        System.out.println("====Start");
        var reservedList =  reservedRepository.findAllByUsername(username);
        var movieList = movieRepository.findAll();
        var seatList = seatRepository.findAll();
        for (var reserved : reservedList) {
            var map = reserved.getMovieSeatMap();
            var movie = movieList.stream().filter(m -> m.getId().equals(map.getMovie().getId())).findFirst();
            var seat = seatList.stream().filter(m -> m.getId().equals(map.getSeat().getId())).findFirst();
            if (movie.isEmpty() || seat.isEmpty())
                continue;

            result.add(ReserveListResponse.builder().movieName(movie.get().getName()).seatName(seat.get().getName()).build());
        }

        long endMs = System.currentTimeMillis();
        System.out.println("====End: " + (endMs - startMs));
        // 64, 69
    }

    @DisplayName("사용자 이름으로 예약 데이터를 조회한다.")
    @Test
    void findByUsername() {
        // given
        String username = "admin";
        var seat1 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-1")
                .build());
        var seat2 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-2")
                .build());
        var movie1 = movieRepository.save(Movie.builder()
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

        for (var map: movie1.getMovieSeatMaps()) {
            reservedRepository.save(Reserved.builder()
                    .username(username).movieSeatMap(map).build());
        }

        for (var map: movie2.getMovieSeatMaps()) {
            reservedRepository.save(Reserved.builder()
                    .username("user1").movieSeatMap(map).build());
        }

        // when
        var result = reserveService.findByUsername(username);

        // then
        assertThat(result).hasSize(2)
                .extracting("movieName", "seatName")
                .containsExactlyInAnyOrder(
                        tuple(movie1.getName(), seat1.getName()),
                        tuple(movie1.getName(), seat2.getName())
                );
    }
}
