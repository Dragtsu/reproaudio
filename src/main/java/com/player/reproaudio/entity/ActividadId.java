package com.player.reproaudio.entity;


import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class ActividadId implements Serializable {

    private int parcial_id;
    private int actividad_id;

    public ActividadId() {}

    public ActividadId(int parcial_id, int actividad_id) {
        this.parcial_id = parcial_id;
        this.actividad_id = actividad_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActividadId)) return false;
        ActividadId that = (ActividadId) o;
        return Objects.equals(parcial_id, that.parcial_id) && Objects.equals(actividad_id, that.actividad_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parcial_id, actividad_id);
    }

}
