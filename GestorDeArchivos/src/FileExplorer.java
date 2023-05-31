import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author Cristofer
 */

public class FileExplorer extends JFrame {

    private JTextField txtFileName;
    private JTextArea txtContent;
    private JButton btnCreate, btnOpen, btnFilter, btnMove, btnAttributes, btnDelete;

    public FileExplorer() {
        setTitle("File Explorer");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel lblFileName = new JLabel("File Path:");
        txtFileName = new JTextField(20);

        JLabel lblContent = new JLabel("Content:");
        txtContent = new JTextArea(15, 40);
        txtContent.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(txtContent);

        btnCreate = new JButton("Create File");
        btnOpen = new JButton("Open File");
        //btnFilter = new JButton("Filter Files");
        btnMove = new JButton("Move File");
        btnAttributes = new JButton("File Attributes");
        btnDelete = new JButton("Delete File");

        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createFile();
            }
        });

        btnOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        btnMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFile();
            }
        });

        btnAttributes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAttributes();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFile();
            }
        });

        panel.add(lblFileName);
        panel.add(txtFileName);
        panel.add(lblContent);
        panel.add(scrollPane);
        panel.add(btnCreate);
        panel.add(btnOpen);
        panel.add(btnMove);
        panel.add(btnAttributes);
        panel.add(btnDelete);

        add(panel);
    }

    private void createFile() {
        String fileName = txtFileName.getText();
        String content = txtContent.getText();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Elija el directorio para guardar el archivo");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            String filePath = selectedDirectory.getAbsolutePath();
            File outputFile = new File(filePath, fileName);

            try {
                FileWriter writer = new FileWriter(outputFile);
                writer.write(content);
                writer.close();
                JOptionPane.showMessageDialog(this, "Archivo creado con exito.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al crear el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtFileName.setText(selectedFile.getAbsolutePath());

            try {
                FileReader reader = new FileReader(selectedFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                bufferedReader.close();
                txtContent.setText(sb.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al abrir archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    private void moveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo para mover");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            JFileChooser destinationChooser = new JFileChooser();
            destinationChooser.setDialogTitle("Seleccionar carpeta de destino");
            destinationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int destinationResult = destinationChooser.showOpenDialog(this);

            if (destinationResult == JFileChooser.APPROVE_OPTION) {
                File destinationFolder = destinationChooser.getSelectedFile();
                String destinationPath = destinationFolder.getAbsolutePath();

                try {
                    Files.move(selectedFile.toPath(), Paths.get(destinationPath, selectedFile.getName()));
                    JOptionPane.showMessageDialog(this, "El archivo se movió con exito.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error al mover el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showAttributes() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                BasicFileAttributes attributes = Files.readAttributes(selectedFile.toPath(), BasicFileAttributes.class);

                long size = attributes.size();
                String sizeString;

                if (size < 1024) {
                    sizeString = size + " bytes";
                } else if (size < 1024 * 1024) {
                    double sizeKB = size / 1024.0;
                    sizeString = String.format("%.2f", sizeKB) + " KB";
                } else if (size < 1024 * 1024 * 1024) {
                    double sizeMB = size / (1024.0 * 1024.0);
                    sizeString = String.format("%.2f", sizeMB) + " MB";
                } else {
                    double sizeGB = size / (1024.0 * 1024.0 * 1024.0);
                    sizeString = String.format("%.2f", sizeGB) + " GB";
                }

                JOptionPane.showMessageDialog(this, "File attributes:\n"
                        + "Tiempo de creación: " + attributes.creationTime() + "\n"
                        + "Última hora de acceso: " + attributes.lastAccessTime() + "\n"
                        + "Hora de última modificación: " + attributes.lastModifiedTime() + "\n"
                        + "Es directorio: " + attributes.isDirectory() + "\n"
                        + "Es un archivo regular: " + attributes.isRegularFile() + "\n"
                        + "Es enlace simbólico: " + attributes.isSymbolicLink() + "\n"
                        + "Peso: " + sizeString);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al leer los atributos del archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo para eliminar");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getAbsolutePath();

            try {
                Files.deleteIfExists(Paths.get(fileName));
                JOptionPane.showMessageDialog(this, "Archivo eliminado con éxito.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FileExplorer().setVisible(true);
            }
        });
    }
}
