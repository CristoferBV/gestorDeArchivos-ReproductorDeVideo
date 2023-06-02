
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Cristofer
 */
public class Server {
    
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress().getHostName());

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
            // Enviar lista de videos al cliente
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            String[] videoList = {"video1.mp4"};
            outputStream.writeObject(videoList);

            // Recibir solicitud del cliente
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
            String selectedVideo = (String) inputStream.readObject();

            // Enviar video al cliente
            File videoFile = new File(selectedVideo);
            byte[] videoBytes = new byte[(int) videoFile.length()];
            FileInputStream fileInputStream = new FileInputStream(videoFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(videoBytes, 0, videoBytes.length);
            bufferedInputStream.close();
            outputStream.write(videoBytes, 0, videoBytes.length);
            outputStream.flush();

            System.out.println("Video enviado al cliente.");

            // Cerrar conexiones
            outputStream.close();
            inputStream.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}