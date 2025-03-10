package com.isluel.toy.seat.seat.repository;

import com.isluel.toy.seat.seat.vo.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

//    @Query(nativeQuery = true, value = "SELECT s.* from seat s where s.id in (select m.id from movie_seat_map m where m.movie_id =:movieId) and s.name =:name")
//    Seat findByMovieIdAndName(Long movieId, String name);
}
