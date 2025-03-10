package com.isluel.toy.seat.seat.repository;

import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 테스트 후 컨텍스트 리로드
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private MovieSeatMapRepository movieSeatMapRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReservedRepository reservedRepository;
    @AfterEach
    void teardown() {
        reservedRepository.deleteAllInBatch();
        movieSeatMapRepository.deleteAllInBatch();
        movieRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
    }

    @Disabled
    @DisplayName("영화 아이디와 좌석 이름으로 데이터를 조회한다.")
    @Test
    void findByMovieIdAndName() {
        // given
        int theaterId = 1;
        String seatName = "A-1";
        var seat1 = Seat.builder()
                .name(seatName)
                .theaterId(theaterId)
                .build();
        var seat2 = Seat.builder()
                .name("A-2")
                .theaterId(theaterId)
                .build();
        var seats = seatRepository.saveAll(List.of(seat1, seat2));

        var movie = movieRepository.save(Movie.builder()
                .theaterId(theaterId)
                .name("영화 A")
                .ordinal(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .seats(seats)
                .build());

        // when
//        var result = seatRepository.findByMovieIdAndName(movie.getId(), seatName);

        // then
//        assertThat(result)
//                .extracting("theaterId", "name")
//                .containsExactly(theaterId, seatName);
    }

}