package com.mycompany.reproductordevideo;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;


/**
 *
 * @author maria
 */


public class Client extends Application {
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private ListView<String> videoListView;
    private Button playButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        videoListView = new ListView<>();
        playButton = new Button("Reproducir");
        playButton.setDisable(true);
        playButton.setOnAction(event -> {
            String selectedVideo = videoListView.getSelectionModel()
                    .getSelectedItem();
            if (selectedVideo != null) {
                playVideo(selectedVideo);
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(videoListView, playButton);

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Cliente de Video");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                clientSocket = new Socket("localhost", 5000);
                inputStream = new DataInputStream
                        (clientSocket.getInputStream());
                outputStream = new DataOutputStream
                        (clientSocket.getOutputStream());

                // Recibir lista de videos del servidor
                try {
                    ObjectInputStream objectInputStream = 
                            new ObjectInputStream(inputStream);
                    String[] videoList = (String[]) 
                            objectInputStream.readObject();
                    Platform.runLater(() -> {
                        ObservableList<String> items = 
                                FXCollections.observableArrayList(videoList);
                        videoListView.setItems(items);
                        playButton.setDisable(false);
                    });

                    objectInputStream.close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playVideo(String videoName) {
    new Thread(() -> {
        try {
            // Enviar solicitud de reproducción al servidor
            outputStream.writeUTF(videoName);
            outputStream.flush();

            // Recibir video del servidor
            byte[] videoBytes = new byte[8192];
            File videoFile = new File(videoName);
            FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
            BufferedOutputStream bufferedOutputStream = 
                    new BufferedOutputStream(fileOutputStream);

            int bytesRead;
            while ((bytesRead = inputStream.read(videoBytes)) != -1) {
                bufferedOutputStream.write(videoBytes, 0, bytesRead);
            }

            bufferedOutputStream.close();

            // Reproducir video en un reproductor multimedia (por ejemplo, JavaFX MediaPlayer)
            Platform.runLater(() -> {
                Media media = new Media(videoFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setAutoPlay(true);

                mediaPlayer.setOnReady(() -> {
                    Stage videoStage = new Stage();
                    videoStage.setTitle("Reproductor de Video");
                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.fitWidthProperty().bind(videoStage.
                            widthProperty());
                    mediaView.fitHeightProperty().bind(videoStage.
                            heightProperty());

                    StackPane root = new StackPane(mediaView);
                    Scene videoScene = new Scene(root, 800, 600);
                    videoStage.setScene(videoScene);
                    videoStage.show();
                });
            });

            System.out.println("Video recibido y reproducido.");

            // Eliminar el video después de reproducirlo
            videoFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }).start();
}

    @Override
    public void stop() {
        try {
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
