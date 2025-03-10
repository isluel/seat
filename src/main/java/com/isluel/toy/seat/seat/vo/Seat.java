package com.isluel.toy.seat.seat.vo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thread_id", nullable = false)
    private Integer theaterId;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    private Seat(Integer theaterId, String name) {
        this.theaterId = theaterId;
        this.name = name;
    }
}
