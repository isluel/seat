package com.isluel.toy.seat.reserve.repository;

import com.isluel.toy.seat.reserve.vo.Reserved;
import com.isluel.toy.seat.seat.repository.MovieRepository;
import com.isluel.toy.seat.seat.repository.MovieSeatMapRepository;
import com.isluel.toy.seat.seat.repository.SeatRepository;
import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class ReservedRepositoryTest {

    @Autowired
    SeatRepository seatRepository;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    private MovieSeatMapRepository movieSeatMapRepository;
    @Autowired
    private ReservedRepository reservedRepository;

    @AfterEach
    void tearDown() {
        reservedRepository.deleteAllInBatch();
        movieSeatMapRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
    }

    @DisplayName("사용자 이름으로 예약 정보를 가져온다.")
    @Test
    void findAllByUsername() {
        // given
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
                    .username("admin").movieSeatMap(map).build());
        }

        for (var map: movie2.getMovieSeatMaps()) {
            reservedRepository.save(Reserved.builder()
                    .username("user1").movieSeatMap(map).build());
        }

        // when
        var reserveList = reservedRepository.findAllByUsername("admin");

        // then
        assertThat(reserveList).hasSize(2)
                .extracting("username", "movieSeatMap.id")
                .containsExactlyInAnyOrder(
                        tuple("admin", movie1.getMovieSeatMaps().get(0).getId()),
                        tuple("admin", movie1.getMovieSeatMaps().get(1).getId())
                );
    }
}