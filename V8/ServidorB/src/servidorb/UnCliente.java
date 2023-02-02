package servidorb;

import tableros.TableroO;
import tableros.Barco;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UnCliente implements Runnable {

    final DataInputStream entrada;
    final DataOutputStream salida;
    final String nombre;

    final String[] comandos = {
        "HELP",
        "JUGADORES",
        "GANADORES",
        "TORTA",
        "ENEMIGO",
        "BATTLESHIP nombreCliente",
        "TABLERO nombreCliente",
        "BATTLE nombreCliente coordenada",
        "RENDIRSE nombreCliente",
        "PARTIDAS ACTIVAS"
    };

    final String palabraLock;
    String clienteLock;
    boolean lock = false;

    HashMap<String, TableroO> listaTableros = new HashMap<>();
    HashMap<UnCliente, String> ganadas = new HashMap<>();
    HashMap<UnCliente, String> perdidas = new HashMap<>();

    int puntaje = 0;

    private boolean ciclo = true;

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

        try {
            Integer.parseInt(parts[0]);
            return false;
        } catch (NumberFormatException e) {

        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        // --------------- COMANDOS DE 1 PALABRA ------------------ //
        if (parts[0].equals("HELP")) {
            Help();
        } else if (parts[0].equals("JUGADORES")) {
            Jugadores();
        } else if (parts[0].equals("GANADORES")) {
            Ganadores();
        } else if (parts[0].equals("TORTA")) {
            Torta();
        } else if (parts[0].equals("ENEMIGO")) {
            Enemigo();
        }
        if (parts.length < 2) {
            return;
        }
        // --------------------------------------------------------- //

        // --------------- COMANDOS DE 2 PALABRAS ------------------ //
        //BATTLESHIP Mario
        if (parts[0].equals("BATTLESHIP")) {
            //Inicio de la solicitud para jugar
            Battleship(comando);
        } else if (parts[0].equals("PARTIDA")) {
            //Esto es una vez que acepten la partida
            //Esto se hace automático
            Partida(parts[1]);
            //SeleccionDeBarcos inicia el comando de Seleccion para cada cliente
            SeleccionDeBarcos(parts[1]);
        } else if (parts[0].equals("TABLERO")) {
            Tablero(parts[1]);
        } else if (parts[0].equals("RENDIRSE")) {
            finalizarPartida(parts[1], "ninguno", true);
        } else if (comando.equals("PARTIDAS ACTIVAS")) {
            PartidasActivas();
        } else if (comando.equals(palabraLock)) {
            lock = false;
        }
        if (parts.length < 3) {
            return;
        }
        // --------------------------------------------------------- //

        // --------------- COMANDOS DE 3 PALABRAS ------------------ //
        if (parts[0].equals("BATTLE")) {
            // BATTLE Mario C1
            Battle(comando);
        }
        // --------------------------------------------------------- //
        
        // --------------- COMANDOS DE 5 PALABRAS ------------------ //
        if (parts[0].equals("SELECCION")) {
            //SELECCION Mario 4 H C1
            //Este comando no se escribe. Es automático al posicionar los barcos cuando se crea el tablero.
            Seleccion(comando);
        } 
        // --------------------------------------------------------- //
    }

    private void PartidasActivas() {

        try {
            if (listaTableros.isEmpty()) {
                salida.writeUTF("No tiene partidas activas.");
                salida.writeUTF("");
                return;
            }

            salida.writeUTF("* * * PARTIDAS ACTIVAS * * *");
            for (String s : listaTableros.keySet()) {
                salida.writeUTF("--> " + s);
            }
            salida.writeUTF("");
        } catch (IOException e) {

        }

    }

    private void Terminar() {
        ServidorB.lista.remove(nombre);
        if (!listaTableros.isEmpty()) {
            for (UnCliente c : ServidorB.lista.values()) {
                if (c.listaTableros.containsKey(nombre)) {
                    c.listaTableros.remove(nombre);
                }
            }
        }

        try {
            entrada.close();
            salida.close();
        } catch (IOException ex) {

        }

        ciclo = false;
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void Enemigo() {

        try {
            if (perdidas.isEmpty()) {
                salida.writeUTF("Aun no tienes peor enemigo.");
                salida.writeUTF("");
                return;
            }
        } catch (IOException e) {

        }

        int puntosV = 0;
        int puntosN;

        UnCliente cl = null;

        for (UnCliente c : perdidas.keySet()) {
            puntosN = Integer.parseInt(perdidas.get(c));
            if (puntosN >= puntosV) {
                puntosV = puntosN;
                cl = c;
            }
        }

        try {
            salida.writeUTF("* * * TU PEOR ENEMIGO * * *");
            salida.writeUTF("--> " + cl.nombre);
            salida.writeUTF("");
        } catch (IOException e) {

        }

    }

    private void Torta() {

        try {
            if (ganadas.isEmpty()) {
                salida.writeUTF("Aun no tienes torta.");
                salida.writeUTF("");
                return;
            }
        } catch (IOException e) {

        }

        int puntosV = 0;
        int puntosN;

        UnCliente cl = null;

        for (UnCliente c : ganadas.keySet()) {
            puntosN = Integer.parseInt(ganadas.get(c));
            if (puntosN >= puntosV) {
                puntosV = puntosN;
                cl = c;
            }
        }

        try {
            salida.writeUTF("* * * TU TORTA * * *");
            salida.writeUTF("--> " + cl.nombre);
            salida.writeUTF("");
        } catch (IOException e) {

        }

    }

    private void Ganadores() {
        ArrayList<UnCliente> players = new ArrayList<>();
        ArrayList<UnCliente> winnas = new ArrayList<>();

        ServidorB.lista.values().forEach((c) -> {
            players.add(c);
        });

        int tam = players.size();
        UnCliente client;
        for (int x = 0; x < tam; x++) {
            client = players.get(0);
            for (UnCliente player : players) {
                if (player.puntaje >= client.puntaje) {
                    client = player;
                }

            }
            winnas.add(client);
            players.remove(client);
        }

        try {
            for (UnCliente cl : winnas) {
                salida.writeUTF("--> " + cl.nombre + ": " + cl.puntaje);
            }
            salida.writeUTF("");
        } catch (IOException e) {
        }

    }

    private void finalizarPartida(String clienteJugando, String ganador, boolean seRindio) {

        try {
            if (!ServidorB.lista.containsKey(clienteJugando)) {
                salida.writeUTF("Ese cliente no existe.");
                return;
            }

            if (!listaTableros.containsKey(clienteJugando)) {
                salida.writeUTF("No tiene partida con ese cliente.");
                return;
            }

            for (UnCliente cliente : ServidorB.lista.values()) {
                if (cliente.nombre.equals(clienteJugando)) {

                    if (cliente.lock) {
                        salida.writeUTF("El Cliente se encuentra construyendo un tablero.");
                        salida.writeUTF("Considere bien rendirse...");
                        salida.writeUTF("");
                        return;
                    }

                    cliente.listaTableros.remove(nombre);
                    listaTableros.remove(cliente.nombre);

                    if (seRindio) {
                        salida.writeUTF("Se ha rendido ante " + clienteJugando);
                        salida.writeUTF("");
                        cliente.salida.writeUTF(nombre + " se ha rendido.");
                        cliente.salida.writeUTF("");
                    } else {
                        if (cliente.nombre.equals(ganador)) {
                            cliente.puntaje += 3;

                            if (!cliente.ganadas.containsKey(this)) {
                                cliente.ganadas.put(this, "0");
                            }
                            int ps = Integer.parseInt(cliente.ganadas.get(this));
                            ps += 3;
                            cliente.ganadas.remove(this);
                            cliente.ganadas.put(this, ps + "");

                            if (!perdidas.containsKey(cliente)) {
                                perdidas.put(cliente, "0");
                            }
                            ps = Integer.parseInt(perdidas.get(cliente));
                            ps += 3;
                            perdidas.remove(cliente);
                            perdidas.put(cliente, ps + "");

                        } else if (nombre.equals(ganador)) {
                            puntaje += 3;

                            if (!ganadas.containsKey(cliente)) {
                                ganadas.put(cliente, "0");
                            }
                            int ps = Integer.parseInt(ganadas.get(cliente));
                            ps += 3;
                            ganadas.remove(cliente);
                            ganadas.put(cliente, ps + "");

                            if (!cliente.perdidas.containsKey(this)) {
                                cliente.perdidas.put(this, "0");
                            }
                            ps = Integer.parseInt(cliente.perdidas.get(this));
                            ps += 3;
                            cliente.perdidas.remove(this);
                            cliente.perdidas.put(this, ps + "");

                        }

                    }
                    break;
                }
            }

        } catch (IOException e) {
        }

    }

    private boolean comprobarCoord(String coord) {
        try {
            if (coord.length() > 3) {
                salida.writeUTF("Coordenada incorrecta. Ej: E5");
                return false;

            }
        } catch (IOException e) {

        }

        String n = "";
        if (coord.length() == 3) {
            n = String.valueOf(coord.charAt(1)) + String.valueOf(coord.charAt(2));
        } else if (coord.length() == 2) {
            n = String.valueOf(coord.charAt(1));
        }
        try {
            int num = Integer.parseInt(n);
            if (num < 0 || num > 10) {
                salida.writeUTF("Coordenada incorrecta.");
                return false;
            }
        } catch (Exception e) {
            try {
                salida.writeUTF("Coordenada incorrecta. Ej: E5");
            } catch (IOException ex) {

            }

            return false;
        }

        n = String.valueOf(coord.charAt(0));
        try {
            Integer.parseInt(n);
            try {
                salida.writeUTF("Coordenada incorrecta. Ej: E5");
            } catch (IOException ex) {

            }

            return false;
        } catch (Exception e) {

        }
        String[] fils = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        ArrayList<String> filas = new ArrayList<>();
        filas.addAll(Arrays.asList(fils));

        String casillaFila = String.valueOf(coord.charAt(0)).toUpperCase();

        try {
            if (!filas.contains(casillaFila)) {
                salida.writeUTF("Coordenada incorrecta. Ej: E5");
                return false;
            }
        } catch (IOException e) {

        }

        return true;
    }

    private void Battle(String comando) {
        // BATTLE Mario C1
        String[] parts = comando.split(" ");
        try {
            if (!listaTableros.containsKey(parts[1])) {
                salida.writeUTF("No tiene una partida activa con ese cliente.");
                return;
            }

            if (!comprobarCoord(parts[2])) {
                return;
            }
            
            if(lock){
                salida.writeUTF("Primero termine lo que esta haciendo.");
                return;
            }

            for (UnCliente cliente : ServidorB.lista.values()) {
                if (cliente.nombre.equals(parts[1])) {

                    if (cliente.lock) {
                        salida.writeUTF("El cliente se encuentra construyendo un tablero. Intentelo en un momento.");
                        salida.writeUTF("");
                        return;
                    }

                    TableroO tC = cliente.listaTableros.get(nombre);
                    if (tC.estaListo() && listaTableros.get(cliente.nombre).estaListo()) {
                        TableroO t = listaTableros.get(parts[1]);
                        if (t.esTurno()) {
                            String tur = tC.Tiro(parts[2].toUpperCase());

                            switch (tur) {
                                case "¡Agua!":
                                    t.esTurno = false;
                                    t.tabT.Tiro(parts[2].toUpperCase(), false);
                                    cliente.salida.writeUTF(nombre + " ha tirado: " + tur);
                                    salida.writeUTF(tur);
                                    break;
                                case "¡Repetido!":
                                    salida.writeUTF(tur);
                                    salida.writeUTF("Vuelva a tirar.");
                                    break;
                                case "¡Fin!":
                                    salida.writeUTF("* * * VICTORIA * * *");
                                    salida.writeUTF("La partida ha culminado.");
                                    salida.writeUTF("");
                                    cliente.salida.writeUTF(" * * * DERROTA * * *");
                                    cliente.salida.writeUTF("La partida ha culminado.");
                                    cliente.salida.writeUTF("");
                                    finalizarPartida(parts[1], nombre, false);
                                    break;
                                default:
                                    t.tabT.Tiro(parts[2].toUpperCase(), true);
                                    cliente.salida.writeUTF(nombre + " ha tirado: " + tur);
                                    salida.writeUTF(tur);
                                    break;
                            }

                        } else {
                            salida.writeUTF("No es su turno.");
                        }
                    } else {
                        salida.writeUTF("El oponente sigue colocando sus barcos.");
                    }
                    break;
                }

            }

        } catch (IOException e) {

        }

    }

    private void Jugadores() {
        try {
            salida.writeUTF("***LISTA DE JUGADORES***");
            for (String j : ServidorB.lista.keySet()) {
                salida.writeUTF("--> " + j);
            }
            salida.writeUTF("");
        } catch (IOException e) {

        }

    }

    private void Seleccion(String comando) {
        String[] parts = comando.split(" ");
        //SELECCION Mario 4 H C1
        try {
            lock = true;
            clienteLock = nombre;

            TableroO t = listaTableros.get(parts[1]);

            if (parts.length >= 5) {
                String men = "";
                try {
                    men = t.construirBarco(new Barco(t.listaBarcos.get(parts[2]), parts[3].toUpperCase(), parts[4].toUpperCase()));
                } catch (Exception e) {
                    salida.writeUTF("El cliente \"" + parts[1] + "\" se ha desconectado.");
                    salida.writeUTF("");
                    lock = false;
                    return;
                }

                String[] ps = men.split(" ");
                if (ps[0].equals("true")) {
                    t.casillasDisp -= Integer.parseInt(parts[2]);
                    salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "true");
                    salida.writeUTF(t.getTablero());
                } else if (ps[0].equals("false")) {
                    if (ps[1].equals("excede")) {
                        salida.writeUTF("El barco excede los límites.");
                        salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                        salida.writeUTF("");
                    } else if (ps[1].equals("ocupado")) {
                        salida.writeUTF("El barco choca con otro barco.");
                        salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                        salida.writeUTF("");
                    } else if (ps[1].equals("adyacente")) {
                        salida.writeUTF("El barco no se puede pegar con otro barco.");
                        salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                        salida.writeUTF("");
                    }
                }

            } else {
                salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                salida.writeUTF("");
            }

            if (t.casillasDisp == 0) {
                t.listo = true;
                lock = false;

                if (t.esTurno()) {
                    salida.writeUTF("USTED COMIENZA!");
                } else {
                    salida.writeUTF("ESPERE SU TURNO...");
                }

            }

        } catch (IOException e) {

        }
    }

    private void SeleccionDeBarcos(String clienteAJugar) {

        try {
            for (UnCliente cliente : ServidorB.lista.values()) {
                if (clienteAJugar.equals(cliente.nombre)) {

                    TableroO t = listaTableros.get(clienteAJugar);
                    salida.writeUTF("SELECCION " + cliente.nombre + " " + t.casillasDisp + " " + "true");
                    salida.writeUTF(t.getTablero());
                    lock = true;
                    clienteLock = nombre;

                    t = cliente.listaTableros.get(nombre);
                    cliente.salida.writeUTF("SELECCION " + nombre + " " + t.casillasDisp + " " + "true");
                    cliente.salida.writeUTF(t.getTablero());
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
                salida.writeUTF("--> " + s);

            }
            salida.writeUTF("");

        } catch (IOException e) {

        }

    }

    private void Tablero(String clienteAJugar) {

        try {
            if (!listaTableros.containsKey(clienteAJugar)) {
                salida.writeUTF("No se encuentra jugando con ese cliente.");
                return;
            }

            TableroO t = listaTableros.get(clienteAJugar);
            salida.writeUTF("TABLERO" + " " + t.getTablero());
            salida.writeUTF("TABLERO" + " " + t.getTableroTiro());

        } catch (IOException e) {

        }

    }

    private void Partida(String clienteAJugar) {

        for (UnCliente cliente : ServidorB.lista.values()) {
            if (clienteAJugar.equals(cliente.nombre)) {

                if (getRandomNumber(1, 11) > 5) {
                    listaTableros.put(clienteAJugar, new TableroO(true));
                    cliente.listaTableros.put(nombre, new TableroO(false));
                } else {
                    listaTableros.put(clienteAJugar, new TableroO(false));
                    cliente.listaTableros.put(nombre, new TableroO(true));
                }

                break;
            }
        }
    }

    private void Battleship(String comando) {
        String[] parts = comando.split(" ");
        try {
            if (listaTableros.containsKey(parts[1])) {
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

                    if (cliente.lock) {
                        salida.writeUTF("El cliente se encuentra ocupado. Intentelo en un momento.");
                        return;
                    }

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
        while (ciclo) {
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
                                    if (!cliente.lock) {
                                        cliente.salida.writeUTF(nombre + ": " + mensaje);
                                    } 
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
                                    if (!cliente.lock) {
                                        cliente.salida.writeUTF(nombre + " susurra: " + parts[0]);
                                    } else {
                                        salida.writeUTF("El cliente se encuentra ocupado.");
                                    }
                                    break;
                                }
                            }

                        }

                    }
                }

            } catch (IOException ex) {
                Terminar();
            }
        }
    }
}
