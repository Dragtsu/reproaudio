package com.player.reproaudio.repository;

import com.player.reproaudio.entity.ParcialActividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface  ParcialActividadRepository extends JpaRepository<ParcialActividad,Integer> {

    @Query(value = "SELECT count(id) FROM ParcialActividad  where concat(id,'') like ?1 and concat(parcial,'')  like ?2 and concat(actividad,'')  like ?3 ")
    Integer countLike(String id, String parcial, String actividad );

    @Query(value = "SELECT M FROM ParcialActividad M  where concat(id,'') like ?1 and concat(parcial,'')  like ?2 and concat(actividad,'')  like ?3 ")
    Page<ParcialActividad> finAllByLike(String id, String parcial, String actividad , Pageable page);

}