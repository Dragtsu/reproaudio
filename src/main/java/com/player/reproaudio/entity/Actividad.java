package com.player.reproaudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
public class Actividad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parcial_actividad_seq")
    @SequenceGenerator(name = "parcial_actividad_seq", sequenceName = "parcial_actividad_seq", allocationSize = 1)
    private int id;

    @NotNull(message= "El \"Parcial\"  es requerido")
    private int parcial;
    @NotNull(message = "El \"Número de actividad\" es requerida")
    private int actividad;
    @NotBlank(message = "Es necesario indiciar el directorio en donde se guardarán los audios")
    private String directorioDestino;

    @Override
    public String toString() {
        return " .. ";
    }

}