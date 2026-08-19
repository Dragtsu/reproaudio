module com.player.reproaudio {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.player.reproaudio to javafx.fxml;
    exports com.player.reproaudio;
}
