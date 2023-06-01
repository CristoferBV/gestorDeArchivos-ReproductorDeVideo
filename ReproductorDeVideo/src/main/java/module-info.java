module com.mycompany.reproductordevideo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.mycompany.reproductordevideo to javafx.fxml;
    exports com.mycompany.reproductordevideo;
    requires javafx.mediaEmpty;
}
