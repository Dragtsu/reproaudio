package com.player.reproaudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
public class Actividad implements Serializable {


    @EmbeddedId
    private ActividadId actividadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("parcial_id")      // <-- sincroniza id.parcialId con Parcial.id
    @JoinColumn(name = "parcial_id")
    private Parcial parcial;

    private String nombre;

    private String texto;



    @Override
    public String toString() {
        return " .. ";
    }

}