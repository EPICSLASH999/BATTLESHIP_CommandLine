package clienteb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

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

            Battleship(comando);
        } else if (parts[0].equals("TABLERO")) {

            Tablero(comando.replace("TABLERO ", ""));
        } else if (parts[0].equals("SELECCION")) {
            Seleccion(comando);
        }
    }

    public void Seleccion(String comando) {
        String tablero = "";
        try {
            tablero = entrada.readUTF();
        } catch (IOException e) {

        }
        Tablero(tablero);

        String[] parts = comando.split(" ");

        if (Integer.parseInt(parts[2]) <= 0) {
            return;
        }

        if (parts[3].equals("true")) {
            System.out.println("");
            System.out.println("Seleccione un barco:");
            System.out.println("EJEMPLO: 1 H C1");
            System.out.println("");
            System.out.println("5. Portaaviones");
            System.out.println("4. Buque");
            System.out.println("3. Submarino");
            System.out.println("2. Crucero");
            System.out.println("1. Lancha");
            System.out.println("");
            System.out.println("Casillas disponibles: " + parts[2]);
        }

        paraEnviar.deslock(palabraLock);

        String recibido = "";
        String[] recbs = null;

        try {
            recibido = entrada.readUTF();
            recbs = recibido.split(" ");

            if (recbs.length != 3) {
                System.out.println("Ingrese los 3 datos. EJEMPLO: 1 H C1");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return;
            }

        } catch (IOException e) {
        }

        try {
            int n = Integer.parseInt(recbs[0]);
            if (n > 5 || n <= 0) {
                System.out.println("**RANGO 1 - 5**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return;
            }

            if ((Integer.parseInt(parts[2]) - n) < 0) {
                System.out.println("**EXCEDE LAS CASILLAS RESTANTES**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return;
            }

        } catch (Exception e) {
            try {
                System.out.println("**RANGO 1 - 5**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
            } catch (IOException ex) {

            }
            return;
        }

        try {
            salida.writeUTF("SELECCION " + parts[1] + " " + recbs[0] + " " + recbs[1] + " " + recbs[2]);
        } catch (IOException e) {

        }
    }

    public void Tablero(String comando) {
        String[] tab = comando.split("ln");
        for (String l : tab) {
            System.out.println(l);
        }
    }

    public void Battleship(String comando) {
        String[] parts = comando.split(" ");
        String[] opt = {"S", "N"};
        if (parts.length == 2) {

            try {
                String respuesta = entrada.readUTF();

                if (respuesta.equalsIgnoreCase(opt[0])) {
                    System.out.println("La batalla ha iniciado !!");
                    salida.writeUTF("PARTIDA " + parts[1]);
                } else if (respuesta.equalsIgnoreCase(opt[1])) {
                    System.out.println("El cliente ha rechazado la partida.");
                } else {
                    salida.writeUTF("BATTLESHIP " + parts[1]);
                }

            } catch (IOException ex) {
            }

        } else if (parts.length == 3) {

            System.out.println(parts[2] + " lo ha invitado a jugar BATTLESHIP.");
            System.out.println(opt[0] + " / " + opt[1]);

            //Redireccionar esto para solo deslockear si pone la opcion correcta
            paraEnviar.deslock(palabraLock);
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
