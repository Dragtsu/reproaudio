package com.player.reproaudio;

import atlantafx.base.theme.PrimerLight;
import com.player.reproaudio.utils.SingletonViews;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MainApplication extends Application {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // Iniciar la aplicación JavaFX
        launch(args);
    }

    @Override
    public void init() {
        // Inicializar Spring Boot ANTES de que JavaFX muestre la ventana
        context = SpringApplication.run(MainApplication.class);
        SingletonViews.context = context;
        System.out.println("Iniciando Spring Boot...");
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Iniciando JavaFX Stage...");
        SingletonViews.stage = stage;

        // Configurar el tema
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Cargar el FXML
        System.out.println("Llamando al loader");
        FXMLLoader fxml = new FXMLLoader(
                getClass().getResource("/com/player/reproaudio/views/main.fxml")
        );
        System.out.println("Loader llamado");

        // IMPORTANTE: Usar el contexto de Spring para los controladores
        fxml.setControllerFactory(context::getBean);

        Parent root = fxml.load();
        Scene escena = new Scene(root);
        stage.setScene(escena);
        stage.setTitle("ReproAudio");

        // Configurar la ventana
        stage.setMaximized(true);
        stage.initStyle(StageStyle.UNDECORATED);

        BorderPane princ = (BorderPane) root;

        // Configurar dimensiones
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        double heightRatio = visualBounds.getHeight() / screenBounds.getHeight();
        double widthRatio = visualBounds.getWidth() / screenBounds.getWidth();

        stage.setX(visualBounds.getMinX());
        stage.setY(visualBounds.getMinY());
        stage.setWidth(visualBounds.getWidth());
        stage.setHeight(visualBounds.getHeight());

        princ.setPrefWidth(stage.getWidth() * widthRatio);
        princ.setPrefHeight(stage.getHeight() * heightRatio);

        stage.show();
        System.out.println("Stage mostrado");
    }

    @Override
    public void stop() {
        // Cerrar Spring Boot cuando se cierra la aplicación
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }
}