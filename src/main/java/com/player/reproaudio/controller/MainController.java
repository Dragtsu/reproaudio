package com.player.reproaudio.controller;

import com.player.reproaudio.utils.EnumVistas;
import com.player.reproaudio.utils.SingletonViews;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    @FXML
    AnchorPane panelCenterPrincipal;

    @FXML
    private void initialize() { }

    private void mostrarVistaPanelPrincipal(EnumVistas frxml){
        SingletonViews.getInstance().mostrarVista(frxml,panelCenterPrincipal);
    }

    @FXML
    public void mostrarCrearAudio(){
        mostrarVistaPanelPrincipal(EnumVistas.CREAAUDIO);
    }

    @FXML
    private void minimizarVentana(ActionEvent event) {
        Stage stage = obtenerStage(event);
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    private void cerrarAplicacion(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private Stage obtenerStage(ActionEvent event) {
        try {
            return (Stage) ((Node) event.getSource()).getScene().getWindow();
        } catch (Exception e) {
            System.err.println("Error al obtener el Stage: " + e.getMessage());
            return null;
        }
    }


}
