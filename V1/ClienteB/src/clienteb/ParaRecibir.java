package clienteb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParaRecibir implements Runnable {

    final DataInputStream entrada;

    final DataOutputStream salida;
    Scanner teclado;
    String palabraLock;
    
    ParaEnviar paraEnviar;
    

    public ParaRecibir(Socket socket) throws IOException {
        entrada = new DataInputStream(socket.getInputStream());

        salida = new DataOutputStream(socket.getOutputStream());
        teclado = new Scanner(System.in);
        
    }
    
    public ParaRecibir(Socket socket, ParaEnviar pE) throws IOException {
        entrada = new DataInputStream(socket.getInputStream());

        salida = new DataOutputStream(socket.getOutputStream());
        teclado = new Scanner(System.in);
        
        paraEnviar = pE;
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");

        if (mensaje.length() <= 1) {
            return false;
        }

        if (parts.length < 2) {
            return false;
        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        if (parts[0].equals("BATTLESHIP")) {
            String[] opt = {"S", "N"};
            if (parts.length == 2) {
                
                try {
                    String respuesta = entrada.readUTF();
                    if (respuesta.equals(opt[0])){
                        System.out.println("La batalla ha iniciado.");
                        salida.writeUTF("PARTIDA " + parts[1]);
                    }
                    else{
                        System.out.println("El cliente ha rechazado la partida.");
                        
                    }
                } catch (IOException ex) {
                }
                
            } else if (parts.length == 3) {
                
                System.out.println(parts[2] + " lo ha invitado a jugar BATTLESHIP.");
                System.out.println(opt[0] + " / " + opt[1]);
                paraEnviar.deslock(palabraLock);
            }

        }
    }

    @Override
    public void run() {
        String mensaje;
        try {
            palabraLock = entrada.readUTF();
        } catch (IOException ex) {
            System.out.println("Error al obtener la \"palabraLock\"");
        }
        while (true) {
            try {
                mensaje = entrada.readUTF();

                if (esComando(mensaje)) {
                    modoComando(mensaje);
                } else {
                    System.out.println(mensaje);
                }

            } catch (IOException ex) {

            }
        }
    }
}
