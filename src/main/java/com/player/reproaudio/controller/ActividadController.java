package com.player.reproaudio.controller;


import atlantafx.base.controls.ToggleSwitch;
import com.player.reproaudio.entity.Actividad;
import com.player.reproaudio.entity.ActividadId;
import com.player.reproaudio.entity.Parcial;
import com.player.reproaudio.repository.ActividadRepository;
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
import java.util.List;
import java.util.ResourceBundle;
import static com.player.reproaudio.utils.Mensaje.mensajeConfirmacion;

@Slf4j
@Controller
public class ActividadController extends DialogController<Actividad> implements Initializable ,CommonController {

    @FXML
    ChoiceBox<Parcial> lstParcial;

    @FXML
    TextField txtActividad;

    @FXML
    TextField txtNombre;

    @FXML
    TextArea txtAudio;

    private Actividad actividad;

    @FXML
    TableView<Actividad> tablaActividad;

    @FXML
    TableColumn<Actividad, String> nombreColumn;

    @FXML
    TableColumn<Actividad, String> parcialColumn;

    @FXML
    TableColumn<Actividad, String> actividadColumn;

    @FXML
    TableColumn<Actividad, String> textoColumn;

    @FXML
    HBox hBoxBtnTabla;

    @FXML
    Button btnEditar;

    @FXML
    Button btnEliminar;

    ToggleSwitch modoBusqueda;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ParcialRepository parcialRepository;

    public ActividadController() {
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
     //   totalRegistros = (int) parcialRepository.countLike(txtIdActividad.getText() + "%", txtParcial.getText() + "%", txtActividad.getText() + "%");
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
    //    page = parcialRepository.finAllByLike(txtIdActividad.getText() + "%", txtParcial.getText() + "%", txtActividad.getText() + "%", pagina);
        actualizarTabla();
    }


    private String[] guardarActividad(Actividad actividad) {

        String[] validar = new EntityValidator().validateEntity(actividad);

        if (validar[0].equals("OK"))
            actividadRepository.save(actividad);

        return validar;
    }

    @FXML
    public void editarActividad() {

        actividad = tablaActividad.getSelectionModel().getSelectedItem();
       // txtIdActividad.setText(parcial.getId() + "");
        txtActividad.setEditable(false);

       // txtActividad.setText(parcial.getActividad()+"");
    }

    @FXML
    public void eliminar() {

        int i = tablaActividad.getSelectionModel().getSelectedIndex();

        if (i < 0)
            return;

        Actividad actividad = tablaActividad.getSelectionModel().getSelectedItem();

        if (mensajeConfirmacion(Mensaje.ELIMINAR_MSJ)) {

            actividadRepository.delete(actividad);
            limpiar();
            actualizarChoiceBoxAndTabla();
        }
    }

    @FXML
    public void limpiar() {

        txtActividad.setText("");

        actividad = null;

        if (modoBusqueda.isSelected())
            txtActividad.setEditable(true);
        actualizarChoiceBoxAndTabla();
        txtActividad.requestFocus();
        tablaActividad.getSelectionModel().clearSelection();

    }

    @FXML
    public void guardar() {

        actividad = new Actividad();

        try{
            //actividad.setActividad(Integer.parseInt(txtActividad.getText()));
            actividad.setActividadId(new ActividadId(lstParcial.getSelectionModel().getSelectedItem().getParcial(), Integer.parseInt(txtActividad.getText()) ));
            actividad.setNombre(txtNombre.getText().trim());
            actividad.setTexto(txtAudio.getText().trim());
            actividad.setParcial(lstParcial.getSelectionModel().getSelectedItem());
            
            //parcial.setActividad(Integer.parseInt(txtActividad.getText()));
        } catch (Exception e) {
            return;  // Pendiente comprobación de tipos
        }



        String[] result = guardarActividad(actividad);

        if (result[0].equals("OK")) {
            Mensaje.mensaje(result[1], Alert.AlertType.INFORMATION);
            actualizarChoiceBoxAndTabla();
            limpiar();
            txtActividad.requestFocus();
        } else
            Mensaje.mensaje(result[1], Alert.AlertType.ERROR);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        actividadColumn.setCellValueFactory(new PropertyValueFactory<Actividad, String>("actividad"));
        parcialColumn.setCellValueFactory(new PropertyValueFactory<Actividad, String>("parcial"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<Actividad, String>("nombre"));
        textoColumn.setCellValueFactory(new PropertyValueFactory<Actividad, String>("texto"));

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

        tablaActividad.setItems(tableViewObservableList);
        choiceNumRegistros.setItems(choiceObservableList);
        tablaActividad.setStyle(style);
        actualizarChoiceBoxAndTabla();

        modoBusqueda = new ToggleSwitch("Modo búsqueda");

        modoBusqueda.selectedProperty().addListener((obs, old, val) -> {

            if (val) {
                txtActividad.setOnKeyReleased(habilitarBusquedaEvent);


            } else {
                txtActividad.setText("");

                txtActividad.setOnKeyReleased(null);
            }

            limpiar();
        });

        hBoxBtnTabla.getChildren().add(modoBusqueda);
        tablaActividad.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            btnEditar.setDisable(newSelection == null);
            btnEliminar.setDisable(newSelection == null);

        });
        
        lstParcial.getSelectionModel().selectedItemProperty().addListener((obs,OldSelection, newSelection)->{
            
            int seleccionado = 0;
            
            if( (seleccionado = lstParcial.getSelectionModel().getSelectedIndex() ) >= 0 )
                txtActividad.setText( numeroActividadSiguiente(seleccionado)+"" );       
        });

        recargaParciales();
        
        
        
        
    }

    public void recargaParciales() {
        
        Parcial seleccionado = lstParcial.getSelectionModel().getSelectedItem(); // guardar selección actual

        List<Parcial> parciales = parcialRepository.findAll();
        lstParcial.getItems().setAll(parciales);

        // Restaurar selección si todavía existe
        if (seleccionado != null && parciales.contains(seleccionado)) {
            lstParcial.getSelectionModel().select(seleccionado);
        }
        
        if( !parciales.isEmpty() && seleccionado == null ){            
            lstParcial.getSelectionModel().selectFirst();
        }
    }
    
    private int numeroActividadSiguiente(int parcial){
        
        return actividadRepository.findMaxNumeroByParcial(parcial)+1;
    }

    @Override
    public void resetLayout() {

        recargaParciales();
    }

    @Override
    public void init() {

    }
}