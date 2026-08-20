
package com.player.reproaudio.utils;

import com.player.reproaudio.controller.CommonController;
import com.player.reproaudio.utils.EnumVistas;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class SingletonViews {

    private final static SingletonViews singleton = new SingletonViews();
    public static Stage stage;
    private static final Map<EnumVistas, Parent> views = new HashMap<>();
    private static final Map<EnumVistas, CommonController> controllers = new HashMap<>();
    public static ConfigurableApplicationContext context;
    private static final Logger log = LoggerFactory.getLogger(SingletonViews.class);

    private SingletonViews() {
    }

    public static SingletonViews getInstance() {
        return singleton;
    }


    public void add(EnumVistas view) {

        if (views.containsKey(view)) {

            limpiarShorcuts();
            controllers.get(view).resetLayout();

            return;
        }

        var loader = new FXMLLoader(getClass().getResource(view.location));
        loader.setControllerFactory(context::getBean);
        try {
            Parent root = loader.load();
            views.put(view, root);
            controllers.put(view, loader.getController());
        } catch (IOException e) {
            log.info(" ** Error al agregar la vista ** {}", e.getMessage());
        }
    }

    public Parent viewPath(EnumVistas view) {
        return views.get(view);
    }

    public void hideView(EnumVistas view) {
        // views.remove(view);
    }

    public void showDialog(EnumVistas view) {
        add(view);
        stage.getScene().setRoot(views.get(view));
    }

    public void mostrarVista(EnumVistas view, AnchorPane panelCenterPrincipal) {
        // views.clear();
        add(view);
        panelCenterPrincipal.getChildren().clear();
        AnchorPane.setTopAnchor(views.get(view), 0D);
        AnchorPane.setLeftAnchor(views.get(view), 0D);
        AnchorPane.setRightAnchor(views.get(view), 0D);
        AnchorPane.setBottomAnchor(views.get(view), 0D);
        panelCenterPrincipal.getChildren().add(views.get(view));
    }

    private static void limpiarShorcuts() {
        stage.getScene().getAccelerators().clear();
    }

}