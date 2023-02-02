package clienteb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ParaEnviar implements Runnable {

    final DataOutputStream salida;
    Scanner teclado;

    boolean deslock = false;
    String palabraLock;

    public ParaEnviar(Socket socket) throws IOException {
        teclado = new Scanner(System.in);
        salida = new DataOutputStream(socket.getOutputStream());
    }

    public void deslock(String palabraLock) {
        deslock = true;
        this.palabraLock = palabraLock;
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        try {
            if (parts[0].equals("BATTLESHIP")) {
                if (parts.length != 2) {
                    System.out.println("Comando no reconocido.");
                } else {
                    salida.writeUTF(quitarEspFinal(comando));
                }
            } else if (parts[0].equals("TABLERO")) {
                if (parts.length == 2) {
                    salida.writeUTF(quitarEspFinal(comando));
                } else {
                    System.out.println("Comando no reconocido.");
                }
            } else if (parts[0].equals("BATTLE")) {
                if (parts.length == 3) {
                    salida.writeUTF(quitarEspFinal(comando));
                } else {
                    System.out.println("Comando no reconocido.");
                }
            } else if (parts[0].equals("RENDIRSE")) {
                if (parts.length != 2) {
                    System.out.println("Comando no reconocido.");
                } else {
                    salida.writeUTF(quitarEspFinal(comando));
                }
            } else if (parts[0].equals("PARTIDA")) {
                System.out.println("Comando no reconocido.");
            } else if (parts[0].equals("SELECCION")) {
                System.out.println("Comando no reconocido.");
            } else {
                salida.writeUTF(comando);

            }
        } catch (IOException e) {

        }
    }

    public String quitarEspFinal(String mensaje) {

        return mensaje.trim();
    }

    @Override
    public void run() {
        String mensaje;
        while (true) {

            mensaje = teclado.nextLine();
            try {
                if (esComando(mensaje)) {
                    modoComando(mensaje);
                } else {
                    salida.writeUTF(mensaje);
                    if (deslock) {
                        salida.writeUTF(palabraLock);
                        deslock = false;
                    }
                }
            } catch (IOException ex) {
                System.out.println("El servidor se ha desconectado.");
                System.exit(0);
            }

        }

    }
}
