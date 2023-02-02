package clienteb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClienteB {

    public static void main(String[] args) {
        try {
            // ---------------------- VALIDACION DE ARGUMENTOS --------------------- //
            if (args.length <= 0) {
                System.out.println("Ingrese la IP y el puerto.");
                System.exit(1);
            }
            if (args.length == 1) {
                System.out.println("Ingrese el puerto.");
                System.exit(1);
            }
            ClienteB cliente = new ClienteB();
            cliente.verificarArgumentos(args[0], args[1]);
            // --------------------------------------------------------------------- //

            Socket socket = null;
            String ip = args[0];
            int p = Integer.parseInt(args[1]);
            // Creación del socket con la IP y Puerto introducidos
            try {
                socket = new Socket(args[0], p);
            } catch (IOException ex) {
                System.out.println("No hubo conexión con la IP: " + ip + " " + "Y el puerto: " + p);
                System.exit(1);
            } catch (Exception e) {
                System.out.println("No hubo conexión con el puerto: " + p);
                System.exit(1);
            }

            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            Scanner teclado = new Scanner(System.in);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            // ------------------------- VALIDACION DE NOMBRE ----------------------- //
            System.out.println("--> Ingrese su nombre: ");
            String nombre = teclado.nextLine();
            salida.writeUTF(nombre);

            String message = entrada.readUTF();
            if (message.equals("true")) {
                System.out.println("Ese nombre esta repetido.");

                //Este mensaje es porque el servidor tambien espera el mensaje de abajo
                //Espera el parámetro de espacios
                salida.writeUTF("false"); // Le dice al servidor que no prosiga

                socket.close();
                salida.close();
                entrada.close();
                teclado.close();

                System.exit(0);
            }

            if (nombre.contains(" ")) {
                System.out.println("El nombre no puede contener espacios.");
                salida.writeUTF("false"); // Significa no proseguir
                System.exit(0);
            } else {
                salida.writeUTF("true"); // Significa proseguir
            }
            // --------------------------------------------------------------------- //

            System.out.println("=======================================");

            System.out.println("Escriba HELP para ver lista de comandos" + "\n");

            ParaEnviar paraEnviar = new ParaEnviar(socket);
            Thread hiloEnviar = new Thread(paraEnviar);
            hiloEnviar.start();

            ParaRecibir paraRecibir = new ParaRecibir(socket, paraEnviar);
            Thread hiloRecibir = new Thread(paraRecibir);
            hiloRecibir.start();
        } catch (IOException | NumberFormatException ex) {
            System.out.println("--> SE HA PERDIDO LA CONEXIÓN.");
            System.exit(0);
        } 

    }

    public void verificarArgumentos(String ip, String p) {
        int puerto = 0;
        boolean correcto = true; // Si todo esta correcto para el final procede el programa.

        // ------------------------------- IP ---------------------------------- //
        if (!ip.equals("localhost")) {

            boolean ipEsString = true;
            try {
                int ipPrueba = Integer.parseInt(ip);
                ipEsString = false;
            } catch (NumberFormatException e) {
            }

            if (ipEsString && !ip.contains(".")) {
                System.out.println("\"" + ip + "\"" + " No es una IP válida.");
                correcto = false;
            }
            if (!ipEsString && ip.length() <= 5) {
                System.out.println("Primero es la IP y luego el puerto.");
                correcto = false;
                System.exit(1);
            }

            int puntos = ip.length() - ip.replace(".", "").length();
            if (puntos != 3 && correcto) {
                System.out.println("\"" + ip + "\"" + " No es una IP válida.");
                correcto = false;
            }
        }
        // --------------------------------------------------------------------- //

        // ---------------------------- PUERTO --------------------------------- //
        boolean puertoEsInt = true;
        try {
            puerto = Integer.parseInt(p);

        } catch (NumberFormatException e) {
            System.out.println("El puerto debe ser numérico y entero.");
            correcto = false;
            puertoEsInt = false;
        }

        if (p.length() > 5) {
            System.out.println("El puerto no debe exceder los 5 digitos.");
            correcto = false;
        }
        //65535 maximo puerto
        if (puertoEsInt && (puerto < 1 || puerto > 65535)) {
            System.out.println("El puerto debe encontrarse en el rango de 1 a 65535.");
            correcto = false;
        }
        // --------------------------------------------------------------------- //

        // ----------------------- FIN DE VALIDACION ------------------------//
        //Si hubo error, no proceder.
        if (!correcto) {
            System.exit(1);
        }

    }

}
