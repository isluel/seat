package com.isluel.toy.seat.seat.vo;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movie_seat_map")
public class MovieSeatMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

//    @OneToOne(mappedBy = "movieSeatMap")
//    private Reserved reserved;

    public MovieSeatMap(Seat seat, Movie movie) {
        this.movie = movie;
        this.seat = seat;
    }
}
