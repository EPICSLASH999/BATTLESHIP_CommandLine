package clienteb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ParaRecibir implements Runnable {

    final DataInputStream entrada;

    final DataOutputStream salida;
    Scanner teclado;
    String palabraLock;

    ParaEnviar paraEnviar;

    public ParaRecibir(Socket socket, ParaEnviar pE) throws IOException {
        entrada = new DataInputStream(socket.getInputStream());

        salida = new DataOutputStream(socket.getOutputStream());
        teclado = new Scanner(System.in);

        paraEnviar = pE;
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");
        
        if (parts[0].length() <= 1){
            return false;
        }

        if (parts.length < 2) {
            return false;
        }
        
        if (parts[0].equals("-->")){
            return false;
        }
        
        if (String.valueOf(mensaje.charAt(0)).equals("*")){ // Esto es al enviar mensjaes autogenerados que inicien con * no lo tome como comando
            return false;
        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        switch (parts[0]) {
            case "BATTLESHIP":
                Battleship(comando);
                break;
            case "TABLERO":
                mostrarTablero(comando.replace("TABLERO ", ""));
                break;
            case "SELECCION":
                Seleccion(comando);
                break;
            default:
                break;
        }
    }

    public void Seleccion(String comando) {
        // Método de selección de barcos para construir el tablero
        String[] parts = comando.split(" ");
        
        try {
            String tablero = entrada.readUTF();
            mostrarTablero(tablero);
        } catch (IOException e) {

        }

        if (Integer.parseInt(parts[2]) <= 0) {
            System.out.println("EL LLENADO DE BARCOS HA FINALIZADO");
            String m = "";
            try {
                m = entrada.readUTF();
                
            } catch (IOException ex) {
            }
            System.out.println(m);
            System.out.println("");
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

        String recibido = "";
        String[] recbs = null;

        try {
            recibido = entrada.readUTF(); // Aqui se recibe el llenado respectivo del tablero ( 5 V A1 )
            recbs = recibido.split(" ");

            if (comprobarParametros(recbs, parts)) {
                salida.writeUTF("SELECCION " + parts[1] + " " + recbs[0] + " " + recbs[1] + " " + recbs[2]);
            }
        } catch (IOException e) {
        }

    }

    public boolean comprobarParametros(String[] param, String[] parts) {

        // COMPROBACION DE QUE SOLO SEAN 3 PARAMETROS (PARA EL LLENADO DE TRABLERO "Seleccion()")
        // 4 V C4
        try {
            if (param.length != 3) {
                System.out.println("Ingrese 3 datos. EJEMPLO: 1 H C1");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }
        } catch (IOException e) {

        }

        // --------------------- COMPROBACION DEL PRIMER PARAMETRO ---------------------------- //
        try {
            // Comprueba que el primer parámetro sea entero
            int n = Integer.parseInt(param[0]);
            if (n > 5 || n <= 0) {
                // Si el primer parámetro es diferente al rango 1-5 pasa esto
                // Rango es de 1-5 por los tamaños de los barcis de BATTLESHIP
                System.out.println("**RANGO 1 - 5**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }

            // Comprueba que el primer parámetro sea menor o igual a las casillas restantes
            if ((Integer.parseInt(parts[2]) - n) < 0) {
                System.out.println("**EXCEDE LAS CASILLAS RESTANTES**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }

        } catch (Exception e) {
            try {
                // Si no es entero el primer parametro pasa esto
                System.out.println("**RANGO 1 - 5**");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
            } catch (IOException ex) {

            }
            return false;
        }
        // ------------------------------------------------------------------------------------- //

        // --------------------- COMPROBACION DEL SEGUNDO PARAMETRO ---------------------------- //
        try {
            String[] positionChoices = {"V", "H"};
            if (!param[1].equalsIgnoreCase(positionChoices[0]) && !param[1].equalsIgnoreCase(positionChoices[1])) {
                System.out.println("La posicion debe ser: H o V");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }
        } catch (IOException e) {

        }
        // ------------------------------------------------------------------------------------- //

        // ---------------------- COMPROBACION DEL TERCER PARAMETRO ---------------------------- //
        try {
            if (param[2].length() > 3) {
                System.out.println("Coordenada incorrecta. Ej: E5");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;

            }
        } catch (IOException e) {

        }

        String n = "";
        if (param[2].length() == 3) {
            n = String.valueOf(param[2].charAt(1)) + String.valueOf(param[2].charAt(2));
        } else if (param[2].length() == 2) {
            n = String.valueOf(param[2].charAt(1));
        }
        try {
            int num = Integer.parseInt(n);
            if (num < 0 || num > 10) {
                System.out.println("Coordenada incorrecta.");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }
        } catch (Exception e) {
            try {
                System.out.println("Coordenada incorrecta. Ej: E5");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
            } catch (IOException ex) {

            }

            return false;
        }

        n = String.valueOf(param[2].charAt(0));
        try {
            int num = Integer.parseInt(n);
            try {
                System.out.println("Coordenada incorrecta. Ej: E5");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
            } catch (IOException ex) {

            }

            return false;
        } catch (Exception e) {

        }
        String[] fils = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        ArrayList<String> filas = new ArrayList<>();
        filas.addAll(Arrays.asList(fils));
        
        String casillaFila = String.valueOf(param[2].charAt(0)).toUpperCase();
        
        try {
            if (!filas.contains(casillaFila)) {
                System.out.println("Coordenada incorrecta. Ej: E5");
                salida.writeUTF("SELECCION " + parts[1] + " " + "0");
                return false;
            }
        } catch (IOException e) {

        }

        // ------------------------------------------------------------------------------------- //
        return true;

    }

    public void mostrarTablero(String comando) {
        // Método para imprimir un tablero a partir de una sola cadena como String
        String[] tab = comando.split("ln");
        for (String l : tab) {
            System.out.println(l);
        }
        System.out.println("");
    }

    public void Battleship(String comando) {
        
        String[] parts = comando.split(" ");
        String[] opt = {"S", "N"};
        if (parts.length == 2) {
            // Si se recibe el comando BATTLESHIP de 2 parámetros,
            // significa que se ha autoenviado por el servidos con el fin de que el cliente que envió la solicitud
            // se encuentre a la escucha de la respuesta y tome una acción
            try {
                String respuesta = entrada.readUTF();

                if (respuesta.equalsIgnoreCase(opt[0])) {
                    System.out.println("LA BATALLA HA INICIADO !!");
                    System.out.println("");
                    salida.writeUTF("PARTIDA " + parts[1]);
                    paraEnviar.deslock(palabraLock);
                } else if (respuesta.equalsIgnoreCase(opt[1])) {
                    System.out.println("El cliente ha rechazado la partida.");
                    salida.writeUTF(palabraLock);
                } else if (respuesta.equalsIgnoreCase("Tempo")){
                    System.out.println("El tiempo de espera se ha agotado.");
                    salida.writeUTF(palabraLock);
                } 
                else {
                    //Si el cliente ha respondido con algun mensaje al azar "asd" o "9" vuelve a enviar la invitación
                    salida.writeUTF("BATTLESHIP " + parts[1]);
                }
                

            } catch (IOException ex) {
            }

        } else if (parts.length == 3) {
            // Si se recibe el comando BATTLESHIP de 2 parámetros,
            // significa que este es el cliente que ha recibido la solicitud

            System.out.println(parts[2] + " lo ha invitado a jugar BATTLESHIP.");
            System.out.println(opt[0] + " / " + opt[1]);

            
            //Desloquea al mandar mensaje. 
            paraEnviar.deslock(palabraLock); //En caso de volver a la invitación el lock vuelve y esto es un ciclo de lockear y desbloquear lo cual funciona perfectamente
            paraEnviar.Temporizador(5); //Temporizador de 5 segundos, por si no responde expira la invitación
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

            } catch (Exception ex) {
                System.out.println("");
                System.out.println("--> SE HA PERDIDO LA CONEXIÓN.");
                System.exit(0);
            }
        }
    }
}
