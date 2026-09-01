package com.player.reproaudio.utils;

public enum EnumVistas {

    CREAAUDIO("creaAudio.fxml"),

    REGISTRAACTIVIDAD("parcialActividad.fxml");

    public final String location;

    EnumVistas(String location) {
        this.location = "/com/player/reproaudio/views/" + location;
    }

}