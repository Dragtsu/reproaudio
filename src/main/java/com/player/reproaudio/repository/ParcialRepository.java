package com.player.reproaudio.repository;

import com.player.reproaudio.entity.Parcial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ParcialRepository extends JpaRepository<Parcial,Integer> {

    @Query(value = "SELECT count(id) FROM Parcial   where concat(parcial,'')  like ?1   ")
    Integer countLike(String parcial);

    @Query(value = "SELECT M FROM Parcial M  where concat(parcial,'')  like ?1  ")
    Page<Parcial> finAllByLike(String parcial , Pageable page);

}