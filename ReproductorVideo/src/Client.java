import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.swing.SwingWorker;

/**
 *
 * @author Cristofer
 */

public class Client {
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private JList<String> videoListView;
    private JButton playButton;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cliente de Video");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            videoListView = new JList<>();
            playButton = new JButton("Reproducir");
            playButton.setEnabled(false);
            playButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedVideo = videoListView.getSelectedValue();
                    if (selectedVideo != null) {
                        playVideo(selectedVideo);
                    }
                }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(new JScrollPane(videoListView), BorderLayout.CENTER);
            panel.add(playButton, BorderLayout.SOUTH);

            frame.getContentPane().add(panel);
            frame.setSize(300, 200);
            frame.setVisible(true);

            connectToServer();
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                clientSocket = new Socket("192.168.0.5", 5000);
                inputStream = new DataInputStream(clientSocket.getInputStream());
                outputStream = new DataOutputStream(clientSocket.getOutputStream());

                // Recibir lista de videos del servidor
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    String[] videoList = (String[]) objectInputStream.readObject();

                    SwingUtilities.invokeLater(() -> {
                        videoListView.setListData(videoList);
                        playButton.setEnabled(true);
                    });

                    objectInputStream.close();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playVideo(String videoName) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Enviar solicitud de reproducción al servidor
                outputStream.writeUTF(videoName);
                outputStream.flush();

                // Recibir video del servidor
                byte[] videoBytes = new byte[8192];
                File videoFile = new File(videoName);
                FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                int bytesRead;
                while ((bytesRead = inputStream.read(videoBytes)) != -1) {
                    bufferedOutputStream.write(videoBytes, 0, bytesRead);
                }

                bufferedOutputStream.close();

                // Reproducir video en un reproductor multimedia (por ejemplo, JavaFX MediaPlayer)
                SwingUtilities.invokeLater(() -> {
                    // Lógica para reproducir el video en Swing
                });

                System.out.println("Video recibido y reproducido.");

                // Eliminar el video después de reproducirlo
                videoFile.delete();
                return null;
            }
        };

        worker.execute();
    }
}
