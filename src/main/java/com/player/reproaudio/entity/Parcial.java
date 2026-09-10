package com.player.reproaudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import lombok.Data;

@Data
@Entity
public class Parcial implements Serializable {

    @Id
    @NotNull(message= "El \"Parcial\"  es requerido")
    private int parcial;

    @NotBlank(message = "Es necesario indiciar el directorio en donde se guardarán los audios")
    private String directorioDestino;

    @Override
    public String toString() {
        return parcial+"";
    }

}