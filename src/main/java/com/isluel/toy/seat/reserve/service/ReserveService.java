package com.isluel.toy.seat.reserve.service;

import com.isluel.toy.seat.reserve.dto.ReserveListResponse;
import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.reserve.vo.Reserved;
import com.isluel.toy.seat.seat.repository.MovieRepository;
import com.isluel.toy.seat.seat.repository.MovieSeatMapRepository;
import com.isluel.toy.seat.seat.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReserveService {

    private final ReservedRepository reservedRepository;
    private final MovieSeatMapRepository mapRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;

    public ReserveService(ReservedRepository reservedRepository, MovieSeatMapRepository mapRepository
            , SeatRepository seatRepository, MovieRepository movieRepository) {
        this.reservedRepository = reservedRepository;
        this.mapRepository = mapRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
    }

    public List<ReserveListResponse> findByUsername(String username) {
        var result = new ArrayList<ReserveListResponse>();

        var mapList = mapRepository.findAll();
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

        return result;
    }

}
