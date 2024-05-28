/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package proyectodeprogramacionfinalahorasi;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//se encarga de la conexion del cliente y el usuario
public class Flujo extends Thread {
        //son los atributos usados para la comunicacion 
    private Socket nsfd;
    private DataOutputStream salida;
    private DataInputStream entrada;
    private DataInputStream flujoLectura;
    private DataOutputStream flujoEscritura;

    public Flujo(Socket sfd) {
        nsfd = sfd;
        try {
            flujoLectura = new DataInputStream(new BufferedInputStream(sfd.getInputStream()));
            flujoEscritura = new DataOutputStream(new BufferedOutputStream(sfd.getOutputStream()));
        } catch (IOException ioe) {
            System.out.println("IOException(Flujo): " + ioe);
        }
    }
            // se encaraga de la comunicacion con el cliente
    public void run() {
        broadcast(nsfd.getInetAddress() + "> se ha conectado");
        Servidor.usuarios.add(this);
        boolean isConnected = true;
        while (isConnected) {
            try {
                String tipoMensaje = flujoLectura.readUTF();
                if (tipoMensaje.equals("mensaje")) {
                    String linea = flujoLectura.readUTF();
                    if (!linea.equals("")) {
                        linea = nsfd.getInetAddress() + "> " + linea;
                        broadcast(linea);
                    }
                } else if (tipoMensaje.equals("registro")) {
                    String username = flujoLectura.readUTF();
                    String password = flujoLectura.readUTF();
                    String tipoUsuario = flujoLectura.readUTF();
                    CodificacionRegistro(username, password, tipoUsuario);
                } else if (tipoMensaje.equals("login")) {
                    String username = flujoLectura.readUTF();
                    String password = flujoLectura.readUTF();
                    login(username, password);
                }
            } catch (IOException ioe) {
                Servidor.usuarios.remove(this);
                broadcast(nsfd.getInetAddress() + "> se ha desconectado");
                isConnected = false;
            }
        }
    }
        //es el metodo utilizado para enviar mensajes a los clientes
    public void broadcast(String mensaje) {
        synchronized (Servidor.usuarios) {
            for (Flujo f : Servidor.usuarios) {
                try {
                    synchronized (f.flujoEscritura) {
                        f.flujoEscritura.writeUTF("mensaje");
                        f.flujoEscritura.writeUTF(mensaje);
                        f.flujoEscritura.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error: " + ioe);
                }
            }
        }
    }
            //metodo utilizado para que el cliente registre su usuario y contraseña
    private void CodificacionRegistro(String username, String Contraseña, String tipoUsuario) {
        String Maldades = "123456789ABCDEFGHIJKMNÑOPQRSTUVWXYZ";
        boolean nombreDeUsuarioExistente = false;
        String password = "";
        
        try ( BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\usuario\\Music\\Usuarios.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Usuario: ")) {
                    String existingUsername = line.substring("Usuario: ".length()).trim();
                    if (existingUsername.equals(username)) {
                        nombreDeUsuarioExistente = true;
                        flujoEscritura.writeUTF("registroFallido");
                        flujoEscritura.flush();
                        System.out.println("¡El nombre de usuario ya está en uso! Por favor, elija otro nombre de usuario.");
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Flujo.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!nombreDeUsuarioExistente) {
            password = Codificar(Maldades, Contraseña);
            txt(password, username, tipoUsuario);
            try {
                flujoEscritura.writeUTF("registroExitoso");
                flujoEscritura.flush();
            } catch (IOException ex) {
                Logger.getLogger(Flujo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
                //se encarga de verificar si usuario y contraseña son iguales los que estan en el txt
      public void login(String username, String contraseña) {
        boolean userReal = false;
        String password = "";
        String maldades = "123456789ABCDEFGHIJKMNÑOPQRSTUVWXYZ";
        password = Codificar(maldades, contraseña);

        try ( BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\usuario\\Music\\Usuarios.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Usuario: ")) { 
                    String existingUsername = line.substring("Usuario: ".length()).trim();
                    if (existingUsername.equals(username)) {
                        line = reader.readLine();
                        if (line != null && line.startsWith("Contraseña: ")) {
                            String existingPassword = line.substring("Contraseña: ".length()).trim();
                            if (existingPassword.equals(password)) {
                                line = reader.readLine();
                                if (line != null && line.startsWith("TipoUsuario: ")) {
                                    String existingTipoUsuario = line.substring("TipoUsuario: ".length()).trim();
                                    userReal = true;
                                    if (existingTipoUsuario.equalsIgnoreCase("Ver Todo")) {
                                        flujoEscritura.writeUTF("VisualizarTodo");
                                    } else if (existingTipoUsuario.equalsIgnoreCase("Solo Videos")) {
                                        flujoEscritura.writeUTF("VisualizarVideos");
                                    } else if (existingTipoUsuario.equalsIgnoreCase("Solo Imagenes")) {
                                        flujoEscritura.writeUTF("VisualizarImagenes");
                                    } else if (existingTipoUsuario.equalsIgnoreCase("Solo Musica")) {
                                        flujoEscritura.writeUTF("VisualizarMusica");
                                    } else if (existingTipoUsuario.equalsIgnoreCase("Solo Documentos")) {
                                        flujoEscritura.writeUTF("VisualizarDocumentos");
                                    }
                                    flujoEscritura.flush();

                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        if (!userReal) {
            try {
                flujoEscritura.writeUTF("loginFallido");
                flujoEscritura.flush();
            } catch (IOException ex) {
                Logger.getLogger(Flujo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            //metodo que se encarga de la codificacion de la contraseña del usuario
    public static String Codificar(String Maldades, String password) {
        StringBuilder textoCodificado = new StringBuilder();
        password = password.toUpperCase();
        char caracter;
        for (int i = 0; i < password.length(); i++) {
            caracter = password.charAt(i);
            int pos = Maldades.indexOf(caracter);
            if (pos == -1) {
                textoCodificado.append(caracter);
            } else {
                textoCodificado.append(Maldades.charAt((pos + 5) % Maldades.length()));
            }
        }
        return textoCodificado.toString();
    }
            //luego de haber codificado la contraseña y escogido el usuario esa informacion se guardara en el txt
    public static void txt(String password, String username, String tipoUsuario) {
        String cliente = "Usuario: " + username + "\nContraseña: " + password + "\nTipoUsuario: " + tipoUsuario + "\n";

        try ( FileWriter fw = new FileWriter("C:\\Users\\usuario\\Music\\Usuarios.txt", true)) {
            fw.write(cliente);
            System.out.println("Los datos del cliente han sido guardados en el archivo.");
        } catch (IOException ex) {
            Logger.getLogger(Flujo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}