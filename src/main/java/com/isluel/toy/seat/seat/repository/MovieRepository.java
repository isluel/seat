package com.isluel.toy.seat.seat.repository;

import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
