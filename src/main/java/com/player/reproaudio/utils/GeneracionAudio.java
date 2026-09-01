package com.player.reproaudio.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.player.reproaudio.controller.CreaAudioController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Timer;


public class GeneracionAudio {

    private static final Logger log = LoggerFactory.getLogger(GeneracionAudio.class);

    private static final String DEFAULT_VOICE = "af_heart";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String KOKORO_URL = "http://localhost:8880/dev/captioned_speech";


    @FXML
    private ProgressBar progressBar;
    @FXML private Label timeLabel;

    private MediaPlayer mediaPlayer;
    private List<Timestamp> timestamps = new ArrayList<>();
    private List<Text> wordTexts = new ArrayList<>();
    private double speed = 1.0;
    private int currentWordIndex = -1;
    private boolean isPlayingWord = false;
    private boolean isAudioLoaded = false;
    private Timer wordTimer;
    private double wordStartTime = 0;
    private double wordEndTime = 0;

    private byte[] currentAudioBytes;
    private Path audioFilePath;
    private Media audioMedia;
    private String currentText = "";

    public void generarAudio( String text ){



        new Thread(() -> {
            try {

                GenerationResult result = generateSpeech(text, DEFAULT_VOICE, "mp3", speed);
                log.info("Audio generado");

                if (result != null) {
                    // ✅ Asignar el audio a la variable declarada
                    currentAudioBytes = result.audioBytes;
                    timestamps = result.timestamps;
                    currentWordIndex = -1;
                    isAudioLoaded = true;
                    currentText = text;

                    // Guardar el audio en un archivo local
                   // saveAudioToFile(currentAudioBytes);
                   // Path audioDir = Paths.get(System.getProperty("user.home"));
               /*     if (!Files.exists(audioDir)) {
                        Files.createDirectories(audioDir);
                    }*/
                    // Generar nombre de archivo único basado en el tiempo

                   // String fileName = "audio_" + System.currentTimeMillis() + ".mp3";

                    Path tempFile = Files.createTempFile("audio_" + System.currentTimeMillis(), ".mp3");

                    log.info("El audio generado será: {}", tempFile.toUri());

                    //audioFilePath = audioDir.resolve(fileName);
                    //Files.write(audioFilePath, currentAudioBytes);
                    Files.write(tempFile, currentAudioBytes);
                    //tempFile.toFile().deleteOnExit();
                    audioMedia = new Media(tempFile.toUri().toString());

                    List<Timestamp> timeStamps = result.timestamps;

                    for(int i=0;i<timeStamps.size();i++){
                        log.info(timeStamps.get(i).word+"  --  "+timeStamps.get(i).startTime+"  --  "+timeStamps.get(i).endTime);
                    }

                    Platform.runLater(() -> {
                       // displayTextWithHighlight(text, timestamps);
                        playLocalAudio();

                    });
                } else {
                    log.info("Result es null");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void playLocalAudio() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            if (audioMedia == null || audioFilePath == null || !Files.exists(audioFilePath)) {

                //showStatus("❌ No se encontró el archivo de audio", true);
                return;
            }

            mediaPlayer = new MediaPlayer(audioMedia);
            mediaPlayer.setRate(speed);

            log.info("EN setOn ready");
            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = mediaPlayer.getMedia().getDuration();
                if (totalDuration != null && totalDuration.toSeconds() > 0) {
                    updateTimeLabel(Duration.ZERO, totalDuration);
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Duration total = mediaPlayer.getMedia().getDuration();
                    if (total != null && total.toSeconds() > 0) {
                        double progress = newVal.toSeconds() / total.toSeconds();
                        progressBar.setProgress(progress);
                        updateTimeLabel(newVal, total);
                    }
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                Platform.runLater(() -> {

                    Duration total = mediaPlayer.getMedia().getDuration();
                    currentWordIndex = -1;
                    isPlayingWord = false;
                });
            });

            log.info("To Play");
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void guardarAudio(){

        //Files.write(destino, audioData);

    }

    private GenerationResult generateSpeech(String text, String voice, String format, double speed) throws Exception {
        ObjectNode requestJson = mapper.createObjectNode();
        requestJson.put("model", "kokoro");
        requestJson.put("input", text);
        requestJson.put("voice", voice);
        requestJson.put("response_format", format);
        requestJson.put("return_timestamps", true);
        requestJson.put("speed", speed);

        String jsonPayload = mapper.writeValueAsString(requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(KOKORO_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        String body = response.body();
        JsonNode root = mapper.readTree(body);

        if (!root.has("audio")) {
            return null;
        }

        log.info("Texto : "+text);
        String audioBase64 = root.get("audio").asText();
        byte[] audioBytes = Base64.getDecoder().decode(audioBase64);

        List<Timestamp> timestamps = new ArrayList<>();
        JsonNode timestampsNode = root.get("timestamps");
        if (timestampsNode != null && timestampsNode.isArray()) {
            for (JsonNode ts : timestampsNode) {
                String word = ts.get("word").asText();
                double startTime = ts.get("start_time").asDouble();
                double endTime = ts.get("end_time").asDouble();
                timestamps.add(new Timestamp(word, startTime, endTime));
            }
        }

        return new GenerationResult(audioBytes, timestamps);
    }

    public static class Timestamp {
        public String word;
        public double startTime;
        public double endTime;

        public Timestamp(String word, double startTime, double endTime) {
            this.word = (word != null) ? word : "";
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return String.format("'%s' → %.3fs - %.3fs", word, startTime, endTime);
        }
    }

    public  class GenerationResult {
        public byte[] audioBytes;
        public List<Timestamp> timestamps;

        public GenerationResult(byte[] audioBytes, List<Timestamp> timestamps) {
            this.audioBytes = audioBytes;
            this.timestamps = timestamps;
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


}
