package com.player.reproaudio.controller;


import atlantafx.base.controls.ToggleSwitch;
import com.player.reproaudio.entity.ParcialActividad;
import com.player.reproaudio.repository.ParcialActividadRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.net.URL;
import java.util.ResourceBundle;
import static com.player.reproaudio.utils.Mensaje.mensajeConfirmacion;

@Slf4j
@Controller
public class ParcialActividadController extends DialogController<ParcialActividad> implements Initializable ,CommonController {

    @FXML
    TextField txtIdParcialActividad;

    @FXML
    TextField txtParcial;

    @FXML
    TextField txtActividad;

    

    private ParcialActividad parcialActividad;

    @FXML
    TableView<ParcialActividad> tablaParcialActividad;

    @FXML
    TableColumn<ParcialActividad, String> idColumn;

    @FXML
    TableColumn<ParcialActividad, String> parcialColumn;

    @FXML
    TableColumn<ParcialActividad, String> actividadColumn;

    
    @FXML
    HBox hBoxBtnTabla;

    @FXML
    Button btnEditar;

    @FXML
    Button btnEliminar;

    ToggleSwitch modoBusqueda;

    @Autowired
    private ParcialActividadRepository parcialActividadRepository;

    public ParcialActividadController() {
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
        totalRegistros = (int) parcialActividadRepository.countLike(txtIdParcialActividad.getText() + "%", txtParcial.getText() + "%", txtActividad.getText() + "%");
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
        page = parcialActividadRepository.finAllByLike(txtIdParcialActividad.getText() + "%", txtParcial.getText() + "%", txtActividad.getText() + "%", pagina);
        actualizarTabla();
    }


    private String[] guardarParcialActividad(ParcialActividad ParcialActividad) {

        String[] validar = new EntityValidator().validateEntity(ParcialActividad);

        if (validar[0].equals("OK"))
            parcialActividadRepository.save(ParcialActividad);

        return validar;
    }

    @FXML
    public void editarParcialActividad() {

        parcialActividad = tablaParcialActividad.getSelectionModel().getSelectedItem();
        txtIdParcialActividad.setText(parcialActividad.getId() + "");
        txtIdParcialActividad.setEditable(false);
        txtParcial.setText(parcialActividad.getParcial()+"");
        txtActividad.setText(parcialActividad.getActividad()+"");
    }

    @FXML
    public void eliminar() {

        int i = tablaParcialActividad.getSelectionModel().getSelectedIndex();

        if (i < 0)
            return;

        ParcialActividad ParcialActividad = tablaParcialActividad.getSelectionModel().getSelectedItem();

        if (mensajeConfirmacion(Mensaje.ELIMINAR_MSJ)) {

            parcialActividadRepository.delete(ParcialActividad);
            limpiar();
            actualizarChoiceBoxAndTabla();
        }
    }

    @FXML
    public void limpiar() {

        txtIdParcialActividad.setText("");
        txtParcial.setText("");
        txtActividad.setText("");
        parcialActividad = null;

        if (modoBusqueda.isSelected())
            txtIdParcialActividad.setEditable(true);
        actualizarChoiceBoxAndTabla();
        txtParcial.requestFocus();
        tablaParcialActividad.getSelectionModel().clearSelection();

    }

    @FXML
    public void guardar() {

        parcialActividad = new ParcialActividad();

        parcialActividad.setParcial(Integer.parseInt(txtParcial.getText()));
        parcialActividad.setActividad(Integer.parseInt(txtActividad.getText()));

        if (txtIdParcialActividad.getText() != null && !txtIdParcialActividad.getText().trim().isEmpty()) {
            parcialActividad.setId(Integer.parseInt(txtIdParcialActividad.getText()));
        }

        String[] result = guardarParcialActividad(parcialActividad);

        if (result[0].equals("OK")) {
            Mensaje.mensaje(result[1], Alert.AlertType.INFORMATION);
            actualizarChoiceBoxAndTabla();
            limpiar();
            txtParcial.requestFocus();
        } else
            Mensaje.mensaje(result[1], Alert.AlertType.ERROR);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        idColumn.setCellValueFactory(new PropertyValueFactory<ParcialActividad, String>("id"));
        parcialColumn.setCellValueFactory(new PropertyValueFactory<ParcialActividad, String>("parcial"));
        actividadColumn.setCellValueFactory(new PropertyValueFactory<ParcialActividad, String>("actividad"));

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

        tablaParcialActividad.setItems(tableViewObservableList);
        choiceNumRegistros.setItems(choiceObservableList);
        tablaParcialActividad.setStyle(style);
        actualizarChoiceBoxAndTabla();

        modoBusqueda = new ToggleSwitch("Modo búsqueda");

        modoBusqueda.selectedProperty().addListener((obs, old, val) -> {

            if (val) {
                txtIdParcialActividad.setOnKeyReleased(habilitarBusquedaEvent);
                txtParcial.setOnKeyReleased(habilitarBusquedaEvent);
                txtActividad.setOnKeyReleased(habilitarBusquedaEvent);                

            } else {
                txtIdParcialActividad.setText("");
                txtIdParcialActividad.setOnKeyReleased(null);
                txtIdParcialActividad.setEditable(false);
                txtParcial.setOnKeyReleased(null);
                txtActividad.setOnKeyReleased(null);
            }

            limpiar();
        });

        hBoxBtnTabla.getChildren().add(modoBusqueda);
        tablaParcialActividad.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            btnEditar.setDisable(newSelection == null);
            btnEliminar.setDisable(newSelection == null);

        });
    }

    @Override
    public void resetLayout() {       
    }
}