package com.player.reproaudio.controller;


import com.player.reproaudio.utils.GeneracionAudio;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreaAudioController implements CommonController {

    private static final Logger log = LoggerFactory.getLogger(CreaAudioController.class);
    @FXML
    TextArea txtTexto;

    @Override
    public void resetLayout() {

    }

    @FXML
    public void generarAudio(){

        log.info("Generando audio,boton pulsado..");
        GeneracionAudio generacionAudio= new GeneracionAudio();
        generacionAudio.generarAudio(txtTexto.getText().trim());

    }
}
