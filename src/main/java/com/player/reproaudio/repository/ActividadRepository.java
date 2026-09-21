package com.player.reproaudio.repository;

import com.player.reproaudio.entity.Actividad;
import com.player.reproaudio.entity.ActividadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository  extends JpaRepository<Actividad, ActividadId> {

    @Query("SELECT COALESCE(MAX(a.actividadId.actividad_id), 0) FROM Actividad a WHERE a.actividadId.parcial_id = :parcialId")
    Integer findMaxNumeroByParcial(@Param("parcialId") Long parcialId);
}



