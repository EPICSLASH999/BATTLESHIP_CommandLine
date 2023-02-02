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

    final String[] comandos = {"HELP", "BATTLESHIP nombreCliente", "TABLERO nombreCliente"};
    ArrayList<String> partidasActivas = new ArrayList<>();

    final String palabraLock;
    String clienteLock;
    boolean lock = false;

    HashMap<String, TableroO> listaTableros = new HashMap<>();

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

        // --------------- COMANDOS DE 1 PALABRA ------------------ //
        if (parts[0].equals("HELP")) {
            Help();
        }
        if (parts.length < 2) {
            return;
        }
        // --------------- COMANDOS DE 2 PALABRAS ------------------ //
        //BATTLESHIP Mario
        if (parts[0].equals("BATTLESHIP")) {
            Battleship(comando);

        } else if (parts[0].equals("PARTIDA")) {
            //Esto es una vez que acepten la partida
            Partida(parts[1]);
            //Faltaria quitarlas una vez que terminen la partda
            SeleccionDeBarcos(comando);

        } else if (parts[0].equals("TABLERO")) {
            Tablero(comando);
        } else if (parts[0].equals("SELECCION")) {
            
            Seleccion(comando);
        } else if (comando.equals(palabraLock)) {
            lock = false;
        }

    }

    private void Seleccion(String comando) {
        String[] parts = comando.split(" ");
        
        try {
            lock = true;
            clienteLock = nombre;

            TableroO t = listaTableros.get(parts[1]);

            t.casillasDisp -= Integer.parseInt(parts[2]);

            salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp);

        } catch (IOException e) {

        }
    }

    private void SeleccionDeBarcos(String comando) {
        String[] parts = comando.split(" ");

        try {
            for (UnCliente cliente : ServidorB.lista.values()) {
                if (parts[1].equals(cliente.nombre)) {

                    TableroO t = listaTableros.get(parts[1]);
                    salida.writeUTF("SELECCION " + cliente.nombre + " " + t.casillasDisp);
                    lock = true;
                    clienteLock = nombre;

                    t = cliente.listaTableros.get(nombre);
                    cliente.salida.writeUTF("SELECCION " + nombre + " " + t.casillasDisp);
                    cliente.lock = true;
                    cliente.clienteLock = cliente.nombre;
                    break;
                }
            }
        } catch (IOException e) {

        }

    }

    private void Help() {
        try {
            salida.writeUTF("***LISTA DE COMANDOS***");
            for (String s : comandos) {
                salida.writeUTF("-->" + s);
            }

        } catch (IOException e) {

        }

    }

    private void Tablero(String comando) {
        String[] parts = comando.split(" ");

        try {
            if (!listaTableros.containsKey(parts[1])) {
                salida.writeUTF("No se encuentra jugando con ese cliente.");
                return;
            }

            TableroO t = listaTableros.get(parts[1]);

            salida.writeUTF("TABLERO" + " " + t.getTablero());

        } catch (IOException e) {

        }

    }

    private void Partida(String clienteAJugar) {

        for (UnCliente cliente : ServidorB.lista.values()) {
            if (clienteAJugar.equals(cliente.nombre)) {
                partidasActivas.add(clienteAJugar);
                cliente.partidasActivas.add(nombre);

                listaTableros.put(clienteAJugar, new TableroO());
                cliente.listaTableros.put(nombre, new TableroO());

                break;
            }
        }
    }

    private void Battleship(String comando) {
        String[] parts = comando.split(" ");
        try {

            if (partidasActivas.contains(parts[1])) {
                salida.writeUTF("Ya tiene una partida activa con ese cliente.");
                return;
            }

            if (!ServidorB.lista.containsKey(parts[1])) {
                salida.writeUTF("No existe ese cliente.");
                return;
            }

            if (parts[1].equals(nombre)) {
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
                            } else {
                                if (lock) {
                                    if (cliente.nombre.equals(clienteLock)) {
                                        cliente.salida.writeUTF(mensaje);
                                        break;
                                    }
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
