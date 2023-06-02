
package servervideos;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author maria
 */
public class ServerVideos {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket
                        .getInetAddress().getHostName());

                Thread thread = new Thread(new VideoHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class VideoHandler implements Runnable {

    private Socket clientSocket;

    public VideoHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Conectado: " + clientSocket.getInetAddress()
                    .getHostName());

            // Enviar lista de videos al cliente
            ObjectOutputStream outputStream = new ObjectOutputStream
                (clientSocket.getOutputStream());
            String[] videoList = {"video1.mp4","video2.mp4","video3.mp4",
                "video4.mp4","video5.mp4"};
            outputStream.writeObject(videoList);

            while (true) {
                try {

                    if (clientSocket.isConnected()) {

                        if (clientSocket.getInputStream().available() > 0) {
                            // Recibir solicitud del cliente
                            ObjectInputStream inputStream = new ObjectInputStream
                                (clientSocket.getInputStream());
                            String selectedVideo = (String) inputStream
                                    .readObject();
                            
                            int bytes = 0;
                           
                            // Open the File where he located in your pc
                            File file = new File(selectedVideo);
                             System.out.println(file.length());
                            FileInputStream fileInputStream
                                    = new FileInputStream(file);
                             DataOutputStream    dataOutputStream = new 
                                DataOutputStream(clientSocket.getOutputStream());
                            // Here we send the File to Server
                            dataOutputStream.writeLong(file.length());
                            // Here we  break file into chunks
                            byte[] buffer = new byte[4 * 1024];
                            while ((bytes = fileInputStream.read(buffer))
                                    != -1) {
                                // Send the file to Server Socket 
                                dataOutputStream.write(buffer, 0, bytes);
                                dataOutputStream.flush();
                            }
                            // close the file here
                            fileInputStream.close();

                            // Cerrar conexiones
                            outputStream.close();
                            inputStream.close();
                            clientSocket.close();

                        }

                    }
                } catch (Exception ex) {

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
