package com.player.reproaudio.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class DialogController<T> {

    @FXML
    ChoiceBox<Integer> choiceNumRegistros;

    @FXML
    Button btnPaginaPrevia;

    @FXML
    Button btnPaginaSiguiente;

    @FXML
    Label lblCount;

    @FXML
    Label labelPage;

    protected int paginaActual = 0;
    protected int totalRegistros = 0;
    private final int elementosPorPagina = 5;
    private int elementosPorPaginaActual = elementosPorPagina;
    protected int numMaximoPaginas = 0;
    protected ObservableList<T> tableViewObservableList = FXCollections.observableArrayList();
    protected ObservableList<Integer> choiceObservableList = FXCollections.observableArrayList();
    protected Pageable pagina;
    protected Page<T> page;
    protected static String style = ""+
            "-color-cell-bg-selected: -color-accent-emphasis; "+
            "-color-cell-fg-selected: -color-fg-emphasis; "+
            "-color-cell-bg-selected-focused: -color-accent-emphasis; "+
            "-color-cell-fg-selected-focused: -color-fg-emphasis; ";


    public DialogController() {
    }

    protected void calcularPaginas() {
        numMaximoPaginas = (int) Math.ceil((double) totalRegistros / elementosPorPaginaActual);
    }

    protected void actualizarLeyendas() {

        labelPage.setText((paginaActual + 1) + " de " + numMaximoPaginas);
        lblCount.setText(" Total : " + totalRegistros);
    }

    protected void actualizarBotones() {
        btnPaginaPrevia.setDisable(paginaActual <= 0);
        btnPaginaSiguiente.setDisable((paginaActual + 1) == numMaximoPaginas);
    }

    protected void fromBusqueda() {

        choiceObservableList.clear();

        if (totalRegistros <= 0) {
            paginaActual = -1;
            numMaximoPaginas = 0;
            actualizarLeyendas();
            tableViewObservableList.clear();
            actualizarBotones();
            return;
        }

        elementosPorPaginaActual = elementosPorPagina;
        calcularPaginas();
        paginaActual = 0;
        actualizarLeyendas();
        actualizarBotones();

        for (int i = 1; i <= numMaximoPaginas; i++) {

            if (i == numMaximoPaginas) {
                if ((i * elementosPorPagina) >= totalRegistros)
                    choiceObservableList.add(totalRegistros);
            } else
                choiceObservableList.add(i * elementosPorPagina);
        }

        choiceNumRegistros.getSelectionModel().selectFirst();
    }

    protected void setTotalPaginas() {

        int index = choiceNumRegistros.getSelectionModel().getSelectedIndex();
        elementosPorPaginaActual = choiceNumRegistros.getItems().get(index);
    }

    protected void setPaginaParameters() {

        setTotalPaginas();
        pagina = PageRequest.of(paginaActual, elementosPorPaginaActual);
    }

    protected void actualizarTabla() {

        tableViewObservableList.clear();
        tableViewObservableList.addAll(page.getContent());
    }

    public boolean retrocederPagina() {

        if (paginaActual > 0) {
            paginaActual--;
            return true;
        }
        return false;
    }

    @FXML
    public boolean avanzarPagina() {

        if ((paginaActual + 1) < numMaximoPaginas) {
            paginaActual++;
            return true;
        }
        return false;
    }
}