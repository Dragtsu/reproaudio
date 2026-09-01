
package com.player.reproaudio.utils;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class Mensaje {
    
     private static final Logger log = LoggerFactory.getLogger(Mensaje.class);

    public static String ELIMINAR_MSJ = "¿Eliminar registro?";
    public static String PRODUCTO_EXISTE_MSJ = "Ya existe un producto con este código de barras";

    private static Alert alerta=null;

    private static void creaAlert(String mensaje, Alert.AlertType alertType){

        alerta= new Alert(alertType);

        alerta.setResizable(true);
        alerta.initStyle(StageStyle.UNDECORATED);
        alerta.getDialogPane().setStyle("-fx-border-color: -color-accent-3;-fx-border-width: 2px;");
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.setContentText(mensaje);

    }

    public static void mensaje(String mensaje, Alert.AlertType alertType){

        creaAlert(mensaje,alertType);

        if(alertType == Alert.AlertType.INFORMATION){
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (alerta.isShowing()) {
                        Platform.runLater(() -> alerta.close());
                    }
                } catch (Exception exp) {
                    log.error("Error al cerrar la alerta");
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        alerta.show();
    }

    public static boolean mensajeConfirmacion( String mensaje){

        creaAlert(mensaje , Alert.AlertType.CONFIRMATION );
        Optional<ButtonType> result = alerta.showAndWait();
        return (result.isPresent()) && (result.get() == ButtonType.OK);
    }

}