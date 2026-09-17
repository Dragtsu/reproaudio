package com.player.reproaudio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
@Entity
public class Parcial implements Serializable {

    @Id
    @NotNull(message= "El \"Parcial\"  es requerido")
    private int parcial;

    @NotBlank(message = "Es necesario indiciar el directorio en donde se guardarán los audios")
    private String directorioDestino;

    @OneToMany(mappedBy = "parcial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actividad> actividades = new ArrayList<>();

    @Override
    public String toString() {
        return parcial+"";
    }

}