package com.player.reproaudio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;


@Entity
@Data
public class ParcialActividad implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "El \"Parcial\"  es requerido")
    private int parcial;
    @NotNull(message = "El \"Número de actividad\" es requerida")
    private int actividad;
    
     public String getFullName() {
        return getParcial() + " " + getActividad() ;
    }        

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the parcial
     */
    public int getParcial() {
        return parcial;
    }

    /**
     * @param parcial the parcial to set
     */
    public void setParcial(int parcial) {
        this.parcial = parcial;
    }

    /**
     * @return the actividad
     */
    public int getActividad() {
        return actividad;
    }

    /**
     * @param actividad the actividad to set
     */
    public void setActividad(int actividad) {
        this.actividad = actividad;
    }
   
}