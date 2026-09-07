package com.player.reproaudio.utils.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class GeneracionAudio {

    private static final Logger log = LoggerFactory.getLogger(GeneracionAudio.class);
    private static final String DEFAULT_VOICE = "af_heart";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String KOKORO_URL = "http://localhost:8880/dev/captioned_speech";
    private double speed = 1.0;

    public Optional<GenerationResult> generarAudio(String text) throws Exception {

        Optional<GenerationResult> optional = Optional.empty();

        GenerationResult result = generateSpeech(text, DEFAULT_VOICE, "mp3", speed);

        if (result != null)
            optional = Optional.of(result);

        return optional;

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

    public static class GenerationResult {
        public byte[] audioBytes;
        public List<Timestamp> timestamps;

        public GenerationResult(byte[] audioBytes, List<Timestamp> timestamps) {
            this.audioBytes = audioBytes;
            this.timestamps = timestamps;
        }
    }

    private static String formatTime(Duration duration) {
        if (duration == null || duration.toSeconds() < 0) return "0:00";
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

}
