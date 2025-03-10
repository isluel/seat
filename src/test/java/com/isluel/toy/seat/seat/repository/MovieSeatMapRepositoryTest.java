package com.isluel.toy.seat.seat.repository;

import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 테스트 후 컨텍스트 리로드
class MovieSeatMapRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private SeatRepository seatRepository;
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

    @DisplayName("영화와 좌석으로 데이터를 조회한다.")
    @Test
    void findByMovieAndSeat() {
        // given
        var seat1 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-1")
                .build());
        var seat2 = seatRepository.save(Seat.builder()
                .theaterId(1)
                .name("A-2")
                .build());

        var movie1 = Movie.builder()
                .name("영화 A")
                .ordinal(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat1, seat2))
                .build();
        var movie2 = Movie.builder()
                .name("영화 B")
                .ordinal(2)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .theaterId(1)
                .seats(List.of(seat1, seat2))
                .build();
        movieRepository.saveAll(List.of(movie1, movie2));

        // when
        var movieSeatMap = movieSeatMapRepository.findByMovieAndSeat(movie1, seat1);
        
        // then
        assertThat(movieSeatMap).isNotNull()
                .extracting("movie.id", "seat.id")
                .containsExactly(movie1.getId(), seat1.getId());
    }
}