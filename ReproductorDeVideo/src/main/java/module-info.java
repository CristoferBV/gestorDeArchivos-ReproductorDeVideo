module com.mycompany.reproductordevideo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.reproductordevideo to javafx.fxml;
    exports com.mycompany.reproductordevideo;
}
