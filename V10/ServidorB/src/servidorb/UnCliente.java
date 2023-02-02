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
        "PARTIDAS ACTIVAS",
        "CHAT",
        "CHAT ON",
        "CHAT OFF"
    };

    final String palabraLock;
    String clienteLock;
    boolean lock = false;

    HashMap<String, TableroO> listaTableros = new HashMap<>();
    HashMap<UnCliente, String> ganadas = new HashMap<>();
    HashMap<UnCliente, String> perdidas = new HashMap<>();

    int puntaje = 0;

    private boolean ciclo = true;

    boolean chat = true;

    public UnCliente(Socket s, String nombre) throws IOException {
        entrada = new DataInputStream(s.getInputStream());
        salida = new DataOutputStream(s.getOutputStream());
        this.nombre = nombre;

        palabraLock = "LISTO LOCK"; // Tiene que comenzar con palabra mayúscula para que entre en el modoComando. 
        salida.writeUTF(palabraLock); // La manda al cliente para que la reciba y pueda utilizarla para quitarse el lock
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");

        if (mensaje.length() <= 1) {// Si solo es una letra o un mensaje en blanco
            return false;
        }

        try {
            Integer.parseInt(parts[0]);// Si el primer elemento (o todo el mensaje) es un numero
            return false;
        } catch (NumberFormatException e) {
        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");
        boolean fueComando = false;

        // --------------- COMANDOS DE 1 PALABRA ------------------ //
        switch (parts[0]) {
            case "HELP":
                Help();
                fueComando = true;
                break;
            case "JUGADORES":
                Jugadores();
                fueComando = true;
                break;
            case "GANADORES":
                Ganadores();
                fueComando = true;
                break;
            case "TORTA":
                Torta();
                fueComando = true;
                break;
            case "ENEMIGO":
                Enemigo();
                fueComando = true;
                break;
            case "CHAT":
                Chat(comando);
                fueComando = true;
                break;
            default:
                break;
        }
        // --------------------------------------------------------- //

        // --------------- COMANDOS DE 2 PALABRAS ------------------ //
        if (parts[0].equals("BATTLESHIP")) {
            //BATTLESHIP Mario
            Battleship(comando); //Inicio de la solicitud para jugar
            fueComando = true;
        } else if (parts[0].equals("PARTIDA")) {
            //PARTIDA Mario
            //Esto es una vez que acepten la partida
            //Esto se hace automático
            Partida(parts[1]);
            SeleccionDeBarcos(parts[1]); //SeleccionDeBarcos inicia el comando de SELECCION para cada cliente
            fueComando = true;
        } else if (parts[0].equals("TABLERO")) {
            //TABLERO Mario
            Tablero(parts[1]);
            fueComando = true;
        } else if (parts[0].equals("RENDIRSE")) {
            //RENDIRSE Mario
            finalizarPartida(parts[1], "ninguno", true);
            fueComando = true;
        } else if (comando.equals("PARTIDAS ACTIVAS")) {
            PartidasActivas();
            fueComando = true;
        } else if (comando.equals(palabraLock)) {
            lock = false;
            fueComando = true;
        }
        // --------------------------------------------------------- //

        // --------------- COMANDOS DE 3 PALABRAS ------------------ //
        if (parts[0].equals("BATTLE")) {
            // BATTLE Mario C1
            Battle(comando);
            fueComando = true;
        }
        // --------------------------------------------------------- //

        // --------------- COMANDOS DE 5 PALABRAS ------------------ //
        if (parts[0].equals("SELECCION")) {
            //SELECCION Mario 4 H C1
            //Este comando no se escribe. Es automático al posicionar los barcos cuando se crea el tablero.
            Seleccion(comando);
            fueComando = true;
        }
        // --------------------------------------------------------- //
        try {
            if (!fueComando) {
                salida.writeUTF("Comando no reconocido.");
            }
        } catch (IOException e){
            
        }

    }

    private void Chat(String comando) {
        // Este comando permite desactivar o activar el chat para el envio de mensajes entre clientes
        // O simplemente ver el estado del chat (si esta activado o desactivado

        String[] parts = comando.split(" ");

        if (parts.length == 1) {
            String estado = chat ? "ON" : "OFF";
            try {
                salida.writeUTF("--> " + estado);
                salida.writeUTF("");
            } catch (IOException e) {
            }
        } else if (parts.length == 2) {
            if (parts[1].equalsIgnoreCase("ON") || parts[1].equalsIgnoreCase("OFF")) {
                String p = parts[1].toUpperCase();

                try {
                    switch (p) {
                        case "ON":
                            chat = true;
                            salida.writeUTF("El chat se ha activado.");
                            salida.writeUTF("");
                            break;
                        case "OFF":
                            chat = false;
                            salida.writeUTF("El chat se ha desactivado.");
                            salida.writeUTF("");
                            break;
                        default:
                            break;
                    }
                } catch (IOException e) {
                }

            }
        }

    }

    private void PartidasActivas() {
        // Muestra las partidas que se tienen activas con distintos clientes
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
        // Si el cliente se desconecta sucede esto

        ServidorB.lista.remove(nombre); // Se elimina de la lista del Servidor para que otros clientes no accedan a esta conexion
        if (!listaTableros.isEmpty()) {
            for (UnCliente c : ServidorB.lista.values()) {
                if (c.listaTableros.containsKey(nombre)) {
                    // Se elimina de las partidas activas de los clientes con los que estaba jugando,
                    // en caso de desconectarse a mitad de una partida
                    c.listaTableros.remove(nombre);
                }
            }
        }

        try {
            entrada.close();
            salida.close();
        } catch (IOException ex) {

        }

        ciclo = false; // Esto es para salirse del ciclo while de la comunicación

        System.out.println("** Se desconectó el cliente: " + nombre); //  Lo imprime la consola del servidor
    }

    private int getRandomNumber(int min, int max) {
        //Devuelve un número aleatorio entre 2 valores
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void Enemigo() {
        // Devuelve quién es el peor enemigo del cliente
        // (Con el que ha perdido más veces)
        try {
            if (perdidas.isEmpty()) {
                salida.writeUTF("Aun no tienes peor enemigo.");
                salida.writeUTF("");
                return;
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
            salida.writeUTF("* * * TU PEOR ENEMIGO * * *");
            salida.writeUTF("--> " + cl.nombre);
            salida.writeUTF("");

        } catch (IOException e) {
        }

    }

    private void Torta() {
        // Devuelve quién es la torta del cliente
        // (Al que le ha ganado más veces)
        try {
            if (ganadas.isEmpty()) {
                salida.writeUTF("Aun no tienes torta.");
                salida.writeUTF("");
                return;
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
            salida.writeUTF("* * * TU TORTA * * *");
            salida.writeUTF("--> " + cl.nombre);
            salida.writeUTF("");

        } catch (IOException e) {
        }

    }

    private void Ganadores() {
        // Devuelve la lista de puntajes que tiene cada cliente de mayor a menor

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
        // Culmina una partida, ya sea de manera natural o rindiendose
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
        // Comprueba el parámetro coordenada si es o no correcto

        // --------------------- VALIDACION DE COORDENADA ---------------------- //
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
        // --------------------------------------------------------------------- //

        return true;
    }

    private void Battle(String comando) {
        // Comando para "tirar" a otro jugador.
        // Dentro del juego BATTLESHIP

        String[] parts = comando.split(" ");
        try {
            if (!listaTableros.containsKey(parts[1])) {
                salida.writeUTF("No tiene una partida activa con ese cliente.");
                return;
            }

            if (!comprobarCoord(parts[2])) {
                return;
            }

            if (lock) {
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
                                    salida.writeUTF("La partida con \"" + cliente.nombre + "\" ha culminado.");
                                    salida.writeUTF("");
                                    cliente.salida.writeUTF(" * * * DERROTA * * *");
                                    cliente.salida.writeUTF("La partida con \"" + nombre + "\" ha culminado.");
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
        // Devuelve la lista de clientes activos en el servidor
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
        //Este comando es para seleccionar y posicionar los barcos al construir el tablero
        //SELECCION Mario 4 H C1

        String[] parts = comando.split(" ");

        try {
            lock = true;
            clienteLock = nombre;

            TableroO t = listaTableros.get(parts[1]);

            if (parts.length >= 5) {
                String men = "";
                try {
                    men = t.construirBarco(new Barco(parts[2], parts[3].toUpperCase(), parts[4].toUpperCase()));
                } catch (Exception e) {
                    salida.writeUTF("El cliente \"" + parts[1] + "\" se ha desconectado.");
                    salida.writeUTF("");
                    lock = false;
                    clienteLock = "";
                    return;
                }

                String[] ps = men.split(" ");
                if (ps[0].equals("true")) {
                    t.casillasDisp -= Integer.parseInt(parts[2]);
                    salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "true");
                    salida.writeUTF(t.getTablero());
                } else if (ps[0].equals("false")) {
                    switch (ps[1]) {
                        case "excede":
                            salida.writeUTF("El barco excede los límites.");
                            salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                            salida.writeUTF("");
                            break;
                        case "ocupado":
                            salida.writeUTF("El barco choca con otro barco.");
                            salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                            salida.writeUTF("");
                            break;
                        case "adyacente":
                            salida.writeUTF("El barco no se puede pegar con otro barco.");
                            salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                            salida.writeUTF("");
                            break;
                        default:
                            break;
                    }
                }

            } else {
                salida.writeUTF("SELECCION " + parts[1] + " " + t.casillasDisp + " " + "false");
                salida.writeUTF("");
            }

            if (t.casillasDisp == 0) { //Significa que ya culminó el llenado del tablero
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
        // Comando automático generado por el servidor al aceptarse una invitación
        // Este método inicia el ciclo de llenado del tablero, tanto para sí mismo como para el cliente
        try {
            for (UnCliente cliente : ServidorB.lista.values()) {
                if (clienteAJugar.equals(cliente.nombre)) {

                    TableroO t = listaTableros.get(clienteAJugar);
                    salida.writeUTF("SELECCION " + cliente.nombre + " " + t.casillasDisp + " " + "true");
                    salida.writeUTF(t.getTablero());
                    lock = true;
                    clienteLock = nombre; // Se lockea para recibir mensajes de si mismo

                    t = cliente.listaTableros.get(nombre);
                    cliente.salida.writeUTF("SELECCION " + nombre + " " + t.casillasDisp + " " + "true");
                    cliente.salida.writeUTF(t.getTablero());
                    cliente.lock = true;
                    cliente.clienteLock = cliente.nombre; // Lockea al cliente para que solo reciba mensajes de si mismo
                    break;
                }
            }
        } catch (IOException e) {
        }

    }

    private void Help() {
        // Devuelve la lista de comandos
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
        // Devuelve el tableroO y TableroT de la partida que se tenga con clienteAJugar
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
        // Comando/método generado automático por el servidor al aceptar una partida
        // Crea la partida dentro de su lista y en la de la lista del cliente que aceptó jugar

        for (UnCliente cliente : ServidorB.lista.values()) {
            if (clienteAJugar.equals(cliente.nombre)) {

                if (getRandomNumber(1, 11) > 5) { // El primer turno se elige al azar
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
        // Comando primordial e inicial del programa
        // Con el se manda una invitación a jugar
        // Es el inicio de todo...

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

            if (lock && clienteLock.equals(nombre)) {
                salida.writeUTF("Primero termine de posicionar sus barcos.");
                return;
            }

            for (UnCliente cliente : ServidorB.lista.values()) {
                if (parts[1].equals(cliente.nombre)) {

                    if (cliente.lock) {
                        salida.writeUTF("El cliente se encuentra ocupado. Intentelo en un momento.");
                        return;
                    }

                    lock = true; // Lock para pevenir que nadie más le envie mensajes mientras espera respuesta.
                    clienteLock = ""; // Que sus mensajes mientrs espera no le lleguen a nadie ni se tomen en cuenta
                    salida.writeUTF(comando);

                    cliente.lock = true; // Lock para enviar la respuesta al cliente que lo invitó a jugar
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
                        // -------------------------- MENSAJES GLOBALES -------------------------- //
                        if (!mensaje.contains("@")) {
                            if (!cliente.nombre.equalsIgnoreCase(nombre)) {
                                if (lock) {
                                    if (cliente.nombre.equals(clienteLock)) {
                                        cliente.salida.writeUTF(mensaje);
                                        break;
                                    }
                                } else {
                                    if (!cliente.lock) {
                                        // Esta sección es para enviar un mensaje normal de chat
                                        if (chat) {
                                            if (cliente.chat) {
                                                cliente.salida.writeUTF(nombre + ": " + mensaje);
                                            }

                                        }

                                    }
                                }
                            } else {
                                // Si se tiene lock a si mismo, el mensaje le llega a uno mismo
                                if (lock) {
                                    if (cliente.nombre.equals(clienteLock)) {
                                        cliente.salida.writeUTF(mensaje);
                                        break;
                                    }
                                }
                            }

                        } // --------------------------------------------------------------------- //
                        // -------------------------- MENSAJES PRIVADOS -------------------------- //
                        else {
                            //Hola @Mario
                            String[] parts = mensaje.split("@");
                            if (parts.length >= 2) {
                                if (cliente.nombre.equalsIgnoreCase(parts[1])) {
                                    if (!cliente.lock) {
                                        // Esta sección es para enviar un mensaje privado de chat
                                        if (chat) {
                                            if (cliente.chat) {
                                                cliente.salida.writeUTF(nombre + " susurra: " + parts[0]);
                                            }
                                        }

                                    } else { // Si el cliente se encuentra en estado de lock (a mitad de construir tablero o invitando a jugar)
                                        salida.writeUTF("El cliente se encuentra ocupado.");
                                    }
                                    break;
                                }
                            }

                        }
                        // --------------------------------------------------------------------- //

                    }
                }

            } catch (IOException ex) {
                Terminar();
            }
        }
    }
}
