package servidorb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class UnCliente implements Runnable {

    final DataInputStream entrada;
    final DataOutputStream salida;
    final String nombre;

    final String[] comandos = {"HELP", "BATTLESHIP nombreCliente"};
    ArrayList<String> partidasActivas = new ArrayList<>();

    final String palabraLock;
    String clienteLock;
    boolean lock = false;

    public UnCliente(Socket s, String nombre) throws IOException {
        entrada = new DataInputStream(s.getInputStream());
        salida = new DataOutputStream(s.getOutputStream());
        this.nombre = nombre;

        palabraLock = "LISTO LOCK";
        salida.writeUTF(palabraLock);
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");

        if (mensaje.length() <= 1) {
            return false;
        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        //BATTLESHIP Mario
        try {
            if (parts[0].equals("HELP")) {
                salida.writeUTF("***LISTA DE COMANDOS***");
                for (String s : comandos) {
                    salida.writeUTF("-->" + s);
                }
                
            } 
            else if (parts[0].equals("TABLERO")){
                TableroO t = new TableroO();
                t.getTablero();
            }

            if (parts.length < 2) {
                return;
            }

            if (parts[0].equals("BATTLESHIP")) {

                if (partidasActivas.contains(parts[1])) {
                    salida.writeUTF("Ya tiene una partida activa con ese cliente.");
                    return;
                }

                if (!ServidorB.lista.containsKey(parts[1])) {
                    salida.writeUTF("No existe ese cliente.");
                    return;
                }
                
                if (parts[1].equals(nombre)){
                    salida.writeUTF("No se puede jugar contra si mismo.");
                    return;
                }

                for (UnCliente cliente : ServidorB.lista.values()) {
                    if (parts[1].equals(cliente.nombre)) {
                        salida.writeUTF(comando);

                        cliente.lock = true;
                        cliente.clienteLock = nombre;
                        cliente.salida.writeUTF(comando + " " + nombre);
                        break;
                    }
                }

            } else if (parts[0].equals("PARTIDA")) {

                //Esto es una vez que acepten la partida
                for (UnCliente cliente : ServidorB.lista.values()) {
                    if (parts[1].equals(cliente.nombre)) {
                        partidasActivas.add(parts[1]);
                        cliente.partidasActivas.add(nombre);
                        //Faltaria quitarlas una vez que terminen
                        break;
                    }
                }

            } else if (comando.equals(palabraLock)) {
                lock = false;
            }
        } catch (IOException e) {

        }

    }

    @Override
    public void run() {
        String mensaje;
        while (true) {
            try {
                mensaje = entrada.readUTF();

                if (esComando(mensaje)) {
                    modoComando(mensaje);
                } else {
                    for (UnCliente cliente : ServidorB.lista.values()) {

                        if (!mensaje.contains("@")) {
                            if (!cliente.nombre.equalsIgnoreCase(nombre)) {
                                if (lock) {
                                    if (cliente.nombre.equals(clienteLock)) {
                                        cliente.salida.writeUTF(mensaje);
                                        break;
                                    }
                                } else {
                                    cliente.salida.writeUTF(mensaje);
                                }
                            }

                        } else {
                            //Hola @Mario
                            String[] parts = mensaje.split("@");
                            if (parts.length >= 2) {
                                if (cliente.nombre.equalsIgnoreCase(parts[1])) {
                                    cliente.salida.writeUTF(parts[0]);
                                    break;
                                }
                            }

                        }

                    }
                }

            } catch (IOException ex) {

            }
        }
    }
}
