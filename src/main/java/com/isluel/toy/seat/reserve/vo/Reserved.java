package com.isluel.toy.seat.reserve.vo;

import com.isluel.toy.seat.seat.vo.Movie;
import com.isluel.toy.seat.seat.vo.MovieSeatMap;
import com.isluel.toy.seat.seat.vo.Seat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reserved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @OneToOne
    private MovieSeatMap movieSeatMap;

    @Builder
    private Reserved(MovieSeatMap movieSeatMap, String username) {
        this.movieSeatMap = movieSeatMap;
        this.username = username;
    }

}
