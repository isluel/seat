package com.isluel.toy.seat.seat.vo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theater_id")
    private Integer theaterId;

    @Column(name = "name")
    private String name;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_Date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieSeatMap> movieSeatMaps = new ArrayList<>();

    @Builder
    private Movie(Integer theaterId, String name, Integer ordinal, LocalDateTime startDate, LocalDateTime endDate, List<Seat> seats) {
        this.theaterId = theaterId;
        this.name = name;
        this.ordinal = ordinal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.movieSeatMaps = seats.stream()
                .map(seat -> new MovieSeatMap(seat, this))
                .collect(Collectors.toList());
    }
}
