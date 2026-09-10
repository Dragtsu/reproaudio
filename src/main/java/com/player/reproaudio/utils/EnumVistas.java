package com.player.reproaudio.utils;

public enum EnumVistas {

    CREAAUDIO("creaAudio.fxml"),
    REGISTRAPARCIAL("parcial.fxml"),
    REGISTRAACTIVIDAD("actividad.fxml");

    public final String location;

    EnumVistas(String location) {
        this.location = "/com/player/reproaudio/views/" + location;
    }

}