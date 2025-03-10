package com.isluel.toy.seat.reserve.repository;

import com.isluel.toy.seat.reserve.vo.Reserved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservedRepository extends JpaRepository<Reserved, Integer> {

    List<Reserved> findAllByUsername(String username);
}
