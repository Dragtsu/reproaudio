package com.player.reproaudio.controller;


import atlantafx.base.controls.ToggleSwitch;
import com.player.reproaudio.entity.Parcial;
import com.player.reproaudio.repository.ParcialRepository;
import com.player.reproaudio.utils.EntityValidator;
import com.player.reproaudio.utils.Mensaje;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static com.player.reproaudio.utils.Mensaje.mensajeConfirmacion;

@Slf4j
@Controller
public class ParcialController extends DialogController<Parcial> implements Initializable, CommonController {


    @FXML
    TextField txtParcial;

    @FXML
    TextField txtDirectorio;

    private Parcial parcial;

    @FXML
    TableView<Parcial> tablaParcial;

    @FXML
    TableColumn<Parcial, String> parcialColumn;

    @FXML
    TableColumn<Parcial, String> directorioColumn;

    @FXML
    CheckBox chkGenerarDirectorio;

    @FXML
    HBox hBoxBtnTabla;

    @FXML
    Button btnEditar;

    @FXML
    Button btnEliminar;

    ToggleSwitch modoBusqueda;

    @Autowired
    private ParcialRepository parcialRepository;

    public ParcialController() {
    }

    private EventHandler<? super KeyEvent> habilitarBusquedaEvent = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            actualizarChoiceBoxAndTabla();
        }
    };

    @FXML
    public void paginaAnterior() {
        if (retrocederPagina()) cambiarPagina();
    }

    @FXML
    public void paginaSiguiente() {
        if (avanzarPagina()) cambiarPagina();
    }

    private void contarRegistros() {
        totalRegistros = (int) parcialRepository.countLike(txtParcial.getText() + "%");
    }

    public void cambiarPagina() {

        reloadTabla();
        actualizarLeyendas();
        actualizarBotones();
    }

    public void actualizarChoiceBoxAndTabla() {

        contarRegistros();
        fromBusqueda();
    }

    public void reloadTabla() {

        setPaginaParameters();
        page = parcialRepository.finAllByLike(txtParcial.getText() + "%", pagina);
        actualizarTabla();
    }


    private String[] guardarParcialActividad(Parcial Parcial) {

        String[] validar = new EntityValidator().validateEntity(Parcial);

        if (validar[0].equals("OK"))
            parcialRepository.save(Parcial);

        return validar;
    }

    @FXML
    public void editarParcialActividad() {

        parcial = tablaParcial.getSelectionModel().getSelectedItem();
        txtParcial.setText(parcial.getParcial() + "");
        txtDirectorio.setText(parcial.getDirectorioDestino());
    }

    @FXML
    public void eliminar() {

        int i = tablaParcial.getSelectionModel().getSelectedIndex();

        if (i < 0)
            return;

        Parcial Parcial = tablaParcial.getSelectionModel().getSelectedItem();

        if (mensajeConfirmacion(Mensaje.ELIMINAR_MSJ)) {

            parcialRepository.delete(Parcial);
            limpiar();
            actualizarChoiceBoxAndTabla();
        }
    }

    @FXML
    public void limpiar() {

        txtParcial.setText("");
        chkGenerarDirectorio.setSelected(true);
        parcial = null;

        //  if (modoBusqueda.isSelected())
        //      txtParcial.setEditable(true);
        actualizarChoiceBoxAndTabla();
        txtParcial.requestFocus();
        tablaParcial.getSelectionModel().clearSelection();

    }

    @FXML
    public void guardar() {

        parcial = new Parcial();

        try {
            parcial.setParcial(Integer.parseInt(txtParcial.getText()));
        } catch (Exception e) {
            return;  // Pendiente comprobación de tipos
        }

        parcial.setDirectorioDestino(txtDirectorio.getText());

        String[] result = guardarParcialActividad(parcial);

        if (result[0].equals("OK")) {
            Mensaje.mensaje(result[1], Alert.AlertType.INFORMATION);
            actualizarChoiceBoxAndTabla();
            limpiar();
            txtParcial.requestFocus();
        } else
            Mensaje.mensaje(result[1], Alert.AlertType.ERROR);
    }

    public void selectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta Destino");

        // Set initial directory to last selected or user home
        String lastPath = txtDirectorio.getText();
        if (!lastPath.isEmpty()) {
            File lastDir = new File(lastPath);
            if (lastDir.exists() && lastDir.isDirectory()) {
                directoryChooser.setInitialDirectory(lastDir);
            }
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        Stage stage = (Stage) txtDirectorio.getScene().getWindow();

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            txtDirectorio.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        parcialColumn.setCellValueFactory(new PropertyValueFactory<Parcial, String>("parcial"));
        directorioColumn.setCellValueFactory(new PropertyValueFactory<Parcial, String>("directorioDestino"));

        resetLayout();

        choiceNumRegistros.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {

                        if ((int) number2 < 0)
                            return;

                        setTotalPaginas();
                        calcularPaginas();
                        paginaActual = 0;
                        cambiarPagina();
                    }
                }
        );

        tablaParcial.setItems(tableViewObservableList);
        choiceNumRegistros.setItems(choiceObservableList);
        tablaParcial.setStyle(style);
        actualizarChoiceBoxAndTabla();

        modoBusqueda = new ToggleSwitch("Modo búsqueda");

        modoBusqueda.selectedProperty().addListener((obs, old, val) -> {

            if (val) {
                txtParcial.setOnKeyReleased(habilitarBusquedaEvent);

            } else {

                //txtParcial.setEditable(false);
                txtParcial.setOnKeyReleased(null);
            }

            limpiar();
        });

        hBoxBtnTabla.getChildren().add(modoBusqueda);
        tablaParcial.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            btnEditar.setDisable(newSelection == null);
            btnEliminar.setDisable(newSelection == null);

        });
    }

    private void setDirectorioGenerado() {
        String directorio = System.getProperty("user.dir") + "\\audio_parcial_";
        if (chkGenerarDirectorio.isSelected() && !txtParcial.getText().isEmpty() )
            txtDirectorio.setText(directorio +  txtParcial.getText() );
        else if(chkGenerarDirectorio.isSelected()){
            txtDirectorio.setText( directorio + "X" );
        }
    }

    public void agregarListenerGenerarDirectorio() {

        chkGenerarDirectorio.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                                Boolean oldValue, Boolean newValue) {
                setDirectorioGenerado();
            }
        });
    }


    public void agregarListenerParcial() {

        txtParcial.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                setDirectorioGenerado();
            }
        });
    }

    @Override
    public void resetLayout() {
    }

    @Override
    public void init() {
        agregarListenerParcial();
        agregarListenerGenerarDirectorio();
        //chkGenerarDirectorio.setSelected(true);
        setDirectorioGenerado();
    }
}