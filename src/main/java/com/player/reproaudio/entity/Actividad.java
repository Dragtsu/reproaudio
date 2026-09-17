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
    private ParcialActividadId parcialActividadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("parcial_id")      // <-- sincroniza id.parcialId con Parcial.id
    @JoinColumn(name = "parcial_id")
    private Parcial parcial;





    @ManyToOne(fetch = FetchType.EAGER) // EAGER es por default
    @JoinColumn(name = "parcial_id", referencedColumnName = "parcial")
    private Parcial parcial;

    @NotNull(message = "El \"Número de actividad\" es requerida")
    private int actividad;

    @NotBlank(message = "Es necesario indiciar el directorio en donde se guardarán los audios")
    private String directorioDestino;

    @Override
    public String toString() {
        return " .. ";
    }

}