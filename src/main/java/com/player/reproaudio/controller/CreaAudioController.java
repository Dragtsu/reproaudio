package com.player.reproaudio.controller;


import atlantafx.base.theme.Styles;
import com.player.reproaudio.entity.ParcialActividad;
import com.player.reproaudio.repository.ParcialActividadRepository;
import com.player.reproaudio.utils.audio.GeneracionAudio;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.ACCENT;
import static atlantafx.base.theme.Styles.SUCCESS;

@Component
public class CreaAudioController implements CommonController {

    private static final Logger log = LoggerFactory.getLogger(CreaAudioController.class);


    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label timeLabel;
    @FXML
    TextArea txtTexto;
    @FXML
    TextField txtServidor;
    @FXML
    TextField txtPuerto;
    @FXML
    Button btnTestServer;


    @Autowired
    private ParcialActividadRepository parcialActividadRepository;
    private ObservableList<ParcialActividad> listaParcial = FXCollections.observableArrayList();
    private Map<Integer, List<Integer>> mapaActividadesPorParcial = new HashMap<>();
    @FXML
    private ChoiceBox<Integer> cmbParcial;

    @FXML
    private ChoiceBox<Integer> cmbActividad;

    private MediaPlayer mediaPlayer;
    private List<GeneracionAudio.Timestamp> timestamps = new ArrayList<>();
    private List<Text> wordTexts = new ArrayList<>();
    private double speed = 1.0;


    @FXML
    Button btnPlay;
    private BooleanProperty isAudioLoaded = new SimpleBooleanProperty(false);
    private Timer wordTimer;


    private byte[] currentAudioBytes;
    private Path audioFilePath;
    private Media audioMedia;
    private String currentText = "";

    private Duration totalDuration = Duration.ZERO;
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(3))
            .build();


    public Task<Boolean> createHealthCheckTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {

                updateMessage("Verificando Kokoro...");

                try {

                    log.info("Task Health");
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(txtServidor.getText().trim() + ":" + txtPuerto.getText().trim() + "/health"))
                            .timeout(java.time.Duration.ofSeconds(3))
                            .header("Accept", "application/json")
                            .GET()
                            .build();

                    HttpResponse<Void> response = client.send(
                            request,
                            HttpResponse.BodyHandlers.discarding()
                    );

                    return response.statusCode() == 200;
                } catch (Exception e) {
                    log.info("Error al conectar a kokoro 0");
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                boolean available = getValue();
                updateMessage(available ? "✅ Kokoro disponible" : "❌ Kokoro no disponible");
                log.info("Kokoro disponible? " + available);
            }

            @Override
            protected void failed() {
                updateMessage("❌ Error al verificar Kokoro");
                log.info("Error al conectar a kokoro.. no deberia llegar aquí");
            }
        };
    }

    @FXML
    public void verificaStatusServidorKokoro() {

        Task<Boolean> health = createHealthCheckTask();

        health.setOnSucceeded(event -> {
            log.info("On Succed --> " + health.getValue());
            updateStatusUI(health.getValue());
        });

        health.setOnFailed(event -> {
            log.info("On Failed --> " + health.getValue());
        });

        new Thread(health).start();

    }

    private void updateStatusUI(boolean status) {
        if (status) {
            btnTestServer.setText("OK");
        } else {
            btnTestServer.setText("Test Failed");
        }
    }


    @Override
    public void resetLayout() {

        listaParcial.setAll(FXCollections.observableList(parcialActividadRepository.findAll()));
        Set<Integer> parcialesSet = listaParcial.stream()
                .map(ParcialActividad::getParcial)
                .collect(Collectors.toSet());
        ObservableList<Integer> listaParciales = FXCollections.observableArrayList(parcialesSet);
        cmbParcial.setItems(listaParciales);

        mapaActividadesPorParcial = listaParcial.stream()
                .collect(Collectors.groupingBy(
                        ParcialActividad::getParcial,
                        Collectors.mapping(ParcialActividad::getActividad, Collectors.toList())
                ));

        cmbParcial.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // Extraer el número del parcial del String "Parcial 1"
                        // int numeroParcial = Integer.parseInt(newValue.replaceAll("\\D+", ""));

                        // Obtener actividades del mapa
                        List<Integer> actividades = mapaActividadesPorParcial.getOrDefault(newValue, new ArrayList<>());

                        // CORRECCIÓN: Convertir a ObservableList
                        ObservableList<Integer> listaActividades = FXCollections.observableArrayList(actividades);

                        // ASIGNAR al ChoiceBox de actividades
                        cmbActividad.setItems(listaActividades);
                        cmbActividad.getSelectionModel().clearSelection();
                    }
                }
        );

        resetColorButton();


    }

    @Override
    public void init() {
        configurarListenerAudioCargado();
    }

    @FXML
    public void generarAudio() {

        if (txtTexto.getText().trim().equalsIgnoreCase(currentText))
            return;

        currentText = txtTexto.getText().trim();

        Task<Optional<GeneracionAudio.GenerationResult>> tarea = new Task<>() {
            @Override
            protected Optional call() throws Exception {
                return new GeneracionAudio().generarAudio(currentText);
            }
        };

        new Thread(tarea).start();

        tarea.setOnSucceeded(event -> {

            Optional<GeneracionAudio.GenerationResult> audio = tarea.getValue();

            if (audio.isPresent()) {

                currentAudioBytes = audio.get().audioBytes;
                Path audioDir = Paths.get(System.getProperty("user.home"));
                String fileName = "audio_" + System.currentTimeMillis() + ".mp3";
                audioFilePath = audioDir.resolve(fileName);
                System.out.println("Audio generado correctamente..");

                try {
                    Files.write(audioFilePath, currentAudioBytes);
                } catch (IOException e) {
                    log.info("Error al generar audio" + e);
                }
            }
        });
    }

    @FXML
    public void play() {

        log.info("A reproducir");
        isAudioLoaded.setValue(true);

        audioMedia = new Media(audioFilePath.toUri().toString());
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            if (audioMedia == null || audioFilePath == null || !Files.exists(audioFilePath)) {

                log.info("No se encontró el archivo de audio");
                return;
            }

            mediaPlayer = new MediaPlayer(audioMedia);
            mediaPlayer.setRate(speed);

            log.info("EN setOn ready");

            mediaPlayer.setOnReady(() -> {

                totalDuration = mediaPlayer.getMedia().getDuration();

                if (totalDuration != null && totalDuration.toSeconds() > 0) {
                    updateTimeLabel(Duration.ZERO, totalDuration);
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {

                    if (totalDuration != null && totalDuration.toSeconds() > 0) {
                        double progress = newVal.toSeconds() / totalDuration.toSeconds();
                        progressBar.setProgress(progress);
                        updateTimeLabel(newVal, totalDuration);
                    }
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                Platform.runLater(() -> {
                    progressBar.setProgress(100);
                });
            });

            log.info("To Play");
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void updateTimeLabel(Duration current, Duration total) {
        Platform.runLater(() -> {
            String currentStr = formatTime(current);
            String totalStr = formatTime(total);
            timeLabel.setText(currentStr + " / " + totalStr);
        });
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.toSeconds() < 0) return "0:00";
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    public void configurarListenerAudioCargado() {

        log.info("Listener configurado..");

        isAudioLoaded.addListener((observable, oldValue, newValue) -> {

            log.info("AudioLoaded Cambió de " + oldValue + " a " + newValue);

            if (newValue) {
                btnPlay.getStyleClass().add(SUCCESS);
            } else {
                btnPlay.getStyleClass().remove(ACCENT);
            }
        });
    }

    private void resetColorButton() {

            /*
            btnPlay.getStyleClass().add(Styles.BUTTON_ICON);
            btnPlay.setStyle("-color-button-bg:-color-base-0; " +
                    "-color-button-fg: -color-base-1;");

            String iconStyle = "-fx-icon-color: #FF5722;" ;

            FontIcon icon = (FontIcon) btnPlay.getGraphic();
            if (icon != null) {
                icon.setStyle(iconStyle);
            }*/

    }

}
