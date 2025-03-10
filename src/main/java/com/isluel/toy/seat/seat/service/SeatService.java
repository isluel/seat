package com.isluel.toy.seat.seat.service;

import com.isluel.toy.seat.reserve.repository.ReservedRepository;
import com.isluel.toy.seat.reserve.vo.Reserved;
import com.isluel.toy.seat.seat.dto.SeatReserveRequest;
import com.isluel.toy.seat.seat.repository.MovieRepository;
import com.isluel.toy.seat.seat.repository.MovieSeatMapRepository;
import com.isluel.toy.seat.seat.repository.SeatRepository;
import com.isluel.toy.seat.seat.vo.SeatVO;
import com.isluel.toy.seat.waiting.service.WaitingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Transactional
public class SeatService {

    // 좌석 데이터 표시
    private final HashOperations<String, String, String> hashOperations;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final MovieSeatMapRepository mapRepository;
    private final ReservedRepository reservedRepository;
    private final WaitingService waitingService;

    public SeatService(HashOperations<String, String, String> hashOperations, SeatRepository seatRepository
            , MovieSeatMapRepository mapRepository, ReservedRepository reservedRepository, MovieRepository movieRepository
            , WaitingService waitingService) {

        this.hashOperations = hashOperations;
        this.seatRepository = seatRepository;
        this.mapRepository = mapRepository;
        this.reservedRepository = reservedRepository;
        this.movieRepository = movieRepository;
        this.waitingService = waitingService;
    }

    public static Map<String, Map<String, String>> reserved = new ConcurrentHashMap<>();

    public void initialize() {
        var movieList = movieRepository.findAll();
        var mapTotalList = mapRepository.findAll();
        var reserveList = reservedRepository.findAll();

        for (var movie: movieList) {
            var mapList = mapTotalList.stream().filter(m -> m.getMovie().getId().equals(movie.getId())).toList();

            for (var map : mapList) {
                //var seat = seatList.stream().filter(s -> s.getId().equals(map.getSeatId())).findFirst();
                if (map.getSeat() == null) {
                    continue;
                }

                var reserved = reserveList.stream()
                        .filter(r -> r.getMovieSeatMap().getId().equals(map.getId()))
                        .findFirst();

                hashOperations.put(movie.getId().toString(), map.getSeat().getName(), reserved.isPresent() ? "1" : "0");
            }
        }
    }

    public boolean checkReserve(SeatReserveRequest request, String username) {
        // 해당 영화의 좌석이 예약 되어있는지 확인.
        // 예약이 되어있으면 increment 결과가 1보다 클것이다.
        var value = hashOperations.increment(request.getMovieId(), request.getSeatId(), 1);

        if (value > 1) {
            // 해당 좌석은 이미 선점 되었음.
            return false;
        } else {
            // 해당 좌석은 요청자가 선점 및 DB에 저장.
            saveSeat(request, username);
            // 해당 인원 사용 인원에서 제거
            waitingService.removeUsage(request.getMovieId(), username);
            return true;
        }
    }

    public Reserved saveSeat(SeatReserveRequest request, String username) {
        if (reserved.containsKey(username)) {
            reserved.get(username).put(request.getMovieId(), request.getSeatId());
        } else {
            reserved.put(username, Map.of(request.getMovieId(), request.getSeatId()));
        }
        // var seat = seatRepository.findByMovieIdAndName(Long.parseLong(request.getMovieId()), request.getSeatId());
        var movie = movieRepository.findById(Long.parseLong(request.getMovieId())).get();
        var movieSeatMapList = movie.getMovieSeatMaps();
        var seat = movieSeatMapList.stream().filter(msm -> msm.getSeat().getName().equalsIgnoreCase(request.getSeatId()))
                .findFirst().get().getSeat();
        var movieSeatMap = mapRepository.findByMovieAndSeat(movie, seat);
        var reserved = Reserved.builder()
                        .username(username)
                                .movieSeatMap(movieSeatMap)
                                        .build();

        return reservedRepository.save(reserved);
    }

    public List<SeatVO> findSeatListByMovieId(String movieId) {
        var seatHashMap = hashOperations.entries(movieId);

        List<SeatVO> seatList = new ArrayList<>();

        for (var entry : seatHashMap.entrySet()) {
            seatList.add(SeatVO.builder()
                    .name(entry.getKey())
                    .reserved(Integer.parseInt(entry.getValue()) > 0)
                    .build());
        }

        return seatList;
    }
}
