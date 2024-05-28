/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package proyectodeprogramacionfinalahorasi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cliente extends Application {

    private static Socket sfd = null;
    private static DataOutputStream SalidaSocket;
    private static DataInputStream EntradaSocket;

    private TextField usuarioField;
    private PasswordField contraseñaField;
    private TextField rolField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Cliente");
        primaryStage.setWidth(400);
        primaryStage.setHeight(300);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10, 10, 10, 10));
        borderPane.setStyle("-fx-background-color: #6C567B;"); // Color de fondo

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);

        Rectangle rectangle = new Rectangle(300, 200);
        rectangle.setArcWidth(30);
        rectangle.setArcHeight(30);
        rectangle.setFill(Color.WHITE); // Color de fondo del rectángulo
        rectangle.setStroke(Color.LIGHTGRAY); // Color del borde
        rectangle.setStrokeWidth(2);

        Label usuarioLabel = new Label("Usuario:");
        usuarioField = new TextField();
        usuarioLabel.setTextFill(Color.RED);  // Cambio de color de las letras

        Label contraseñaLabel = new Label("Contraseña:");
        contraseñaField = new PasswordField();
        contraseñaLabel.setTextFill(Color.RED);  // Cambio de color de las letras

        // Añadiendo el ComboBox para los tipos de usuario
        Label tipoUsuarioLabel = new Label("Tipo de Usuario:");
        ComboBox<String> tipoUsuarioComboBox = new ComboBox<>();
        tipoUsuarioComboBox.getItems().addAll("Solo Videos", "Solo Imagenes", "Solo Musica", "Solo Documentos", "Ver Todo");

        Button btnLogin = new Button("Iniciar Sesión");
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnLogin.setOnAction(event -> {
            try {
                login(usuarioField.getText(), contraseñaField.getText(), primaryStage);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        Button btnRegister = new Button("Registrarse");
        btnRegister.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white;");
        btnRegister.setOnAction(event -> {
            try {
                CodificacionRegistro(usuarioField.getText(), contraseñaField.getText(), tipoUsuarioComboBox.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(10, btnLogin, btnRegister);
        buttonBox.setAlignment(Pos.CENTER);

        gridPane.add(usuarioLabel, 0, 0);
        gridPane.add(usuarioField, 1, 0);
        gridPane.add(contraseñaLabel, 0, 1);
        gridPane.add(contraseñaField, 1, 1);
        gridPane.add(tipoUsuarioLabel, 0, 2);
        gridPane.add(tipoUsuarioComboBox, 1, 2);

        StackPane stackPane = new StackPane(rectangle, gridPane);
        stackPane.setAlignment(Pos.CENTER);

        borderPane.setCenter(stackPane);
        borderPane.setBottom(buttonBox);

        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            sfd = new Socket("localhost", 8000);
            SalidaSocket = new DataOutputStream(sfd.getOutputStream());
            EntradaSocket = new DataInputStream(sfd.getInputStream());
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void login(String username, String contraseña, Stage primaryStage) throws IOException, ClassNotFoundException {
        SalidaSocket.writeUTF("login");
        SalidaSocket.writeUTF(username);
        SalidaSocket.writeUTF(contraseña);

        SalidaSocket.flush();

        String respuesta = EntradaSocket.readUTF();
        switch (respuesta) {
            case "VisualizarTodo":
                
            case "VisualizarVideos":
                
            case "VisualizarImagenes":
                
            case "VisualizarMusica":
                
            case "VisualizarDocumentos":
                // Recibir la lista de archivos del servidor
                Map<String, List<String>> archivosPorCarpeta = recibirListaArchivosDelServidor(respuesta);
                // Obtener la lista de archivos para la carpeta actual
                List<String> listaArchivos = archivosPorCarpeta.get(respuesta.substring(10)); // Elimina "Visualizar" del inicio
                // Mostrar los archivos en el listView
                mostrarArchivos(respuesta.substring(10), FXCollections.observableArrayList(listaArchivos));
                break;
            case "loginFallido":
                System.out.println("Usuario o contraseña incorrectos.");
                break;
            default:
                break;
        }
    }

    private Map<String, List<String>> recibirListaArchivosDelServidor(String carpeta) throws IOException, ClassNotFoundException {
        // Envía el nombre de la carpeta al servidor
        SalidaSocket.writeUTF(carpeta);
        SalidaSocket.flush();

        // Recibe la lista de archivos del servidor
        ObjectInputStream inputStream = new ObjectInputStream(EntradaSocket);
        List<String> listaArchivos = (List<String>) inputStream.readObject();

        // Crear un mapa que contenga la carpeta y la lista de archivos
        Map<String, List<String>> archivosPorCarpeta = new HashMap<>();
        archivosPorCarpeta.put(carpeta, listaArchivos);

        return archivosPorCarpeta;
    }

    private void CodificacionRegistro(String username, String contraseña, String tipoUsuario) throws IOException {
        SalidaSocket.writeUTF("registro");
        SalidaSocket.writeUTF(username);
        SalidaSocket.writeUTF(contraseña);
        SalidaSocket.writeUTF(tipoUsuario);
        SalidaSocket.flush();

        String respuesta = EntradaSocket.readUTF();
        if (respuesta.equals("registroExitoso")) {
            System.out.println("Registro exitoso.");
        } else {
            System.out.println("El nombre de usuario ya está en uso.");
        }
    }

    private void mostrarArchivos(String titulo, javafx.collections.ObservableList<String> archivos) {
        Stage ventanaArchivos = new Stage();
        ventanaArchivos.initModality(Modality.APPLICATION_MODAL);
        ventanaArchivos.setTitle(titulo);

        ListView<String> listView = new ListView<>();
        listView.setItems(archivos);

                // Botón de descarga
        Button btnDescargar = new Button("Descargar");
        btnDescargar.setOnAction(event -> descargarArchivo(listView.getSelectionModel().getSelectedItem()));

        // Manejar la acción al hacer doble clic en cualquier elemento de la lista
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String carpeta = titulo; // Obtiene la carpeta actual desde el título
                    switch (carpeta) {
                        case "Videos":
                            reproducirVideo(selectedItem, carpeta);
                            break;
                        case "Imágenes":
                            mostrarImagen(selectedItem, carpeta);
                            break;
                        case "Música":
                            reproducirMusica(selectedItem, carpeta);
                            break;
                        default:
                            // No hacer nada por defecto o mostrar un mensaje de error, por ejemplo
                            break;
                    }
                }
            }
        });

        VBox vBox = new VBox(10, listView, btnDescargar);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));

        Scene scene = new Scene(vBox, 300, 200);
        ventanaArchivos.setScene(scene);
        ventanaArchivos.show();
    }

    private void descargarArchivo(String archivo) {
        if (archivo != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo");
            fileChooser.setInitialFileName(archivo);

            // Mostrar el diálogo de selección de archivo y obtener la ubicación de destino del cliente
            File selectedFile = fileChooser.showSaveDialog(new Stage());
            if (selectedFile != null) {
                // Aquí podrías implementar la lógica para descargar el archivo en la ubicación seleccionada
                System.out.println("Archivo guardado en: " + selectedFile.getAbsolutePath());
            }
        }
    }

    public void reproducirVideo(String videoName, String carpeta) {
        String videoFilePath = carpeta + File.separator + videoName;
        Media media = new Media(new File(videoFilePath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);
        // Asegurarse de que el MediaView se ajuste al tamaño del video
        mediaView.setFitWidth(800);
        mediaView.setFitHeight(600);
        mediaView.setPreserveRatio(true);

        // StackPane que contiene el MediaView
        StackPane root = new StackPane();
        root.getChildren().add(mediaView);

        // Crear la escena y mostrar la ventana
        Scene scene = new Scene(root, 800, 600);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Video Player");
        primaryStage.setScene(scene);

        // Asegurarse de ejecutar en el hilo de la aplicación de JavaFX
        Platform.runLater(() -> {
            primaryStage.show();
            // Reproducir el video después de mostrar la ventana
            mediaPlayer.play();
        });
    }

    private void mostrarImagen(String imageName, String carpeta) {
        String imageFilePath = carpeta + File.separator + imageName;
        Image image = new Image(new File(imageFilePath).toURI().toString());
        ImageView imageView = new ImageView(image);

        StackPane root = new StackPane();
        root.getChildren().add(imageView);

        Scene scene = new Scene(root, 800, 600);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Image Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void reproducirMusica(String musicName, String carpeta) {
        String musicFilePath = carpeta + File.separator + musicName;
        Media media = new Media(new File(musicFilePath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // Crear un reproductor de medios (MediaView)
        MediaView mediaView = new MediaView(mediaPlayer);

        // Crear controles de reproducción
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");

        playButton.setOnAction(event -> mediaPlayer.play());
        pauseButton.setOnAction(event -> mediaPlayer.pause());
        stopButton.setOnAction(event -> mediaPlayer.stop());

        HBox controlBox = new HBox(playButton, pauseButton, stopButton);

        // Crear un VBox para contener el reproductor de medios y los controles de reproducción
        VBox vbox = new VBox();
        vbox.getChildren().addAll(mediaView, controlBox);

        // Crear una escena y mostrar la ventana
        Scene scene = new Scene(vbox, 400, 300);
        Stage stage = new Stage();
        stage.setTitle("Reproductor de música");
        stage.setScene(scene);
        stage.show();

        // Iniciar la reproducción de la música
        mediaPlayer.play();
    }
}