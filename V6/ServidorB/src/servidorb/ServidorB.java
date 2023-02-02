package servidorb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServidorB {

    static HashMap<String, UnCliente> lista = new HashMap<String, UnCliente>();
    
    

    public static void main(String[] args) throws IOException {
        
        if (args.length <= 0) {
            System.out.println("Ingrese un puerto.");
            System.exit(1);
        }
        
        ServidorB b = new ServidorB();
        b.verificarPuerto(args[0]);
        int p = Integer.parseInt(args[0]);
        ServerSocket socketServidor = null;
        try {
            socketServidor = new ServerSocket(p);
        } catch (IOException ex) {
            System.out.println("Ya se encuentra en uso el puerto: " + p);
            System.exit(1);
        } finally {
            System.out.println("---->EL SERVIDOR SE GENERO EN EL PUERTO: " + p);
            System.out.println("");
        }
        
        // --------------------------------------------------------------------- //
        while (true) {
            Socket s = socketServidor.accept();

            DataInputStream entrada = new DataInputStream(s.getInputStream());
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());

            String nombre;
            boolean seguir = true;

            nombre = entrada.readUTF();
            for (String n : ServidorB.lista.keySet()) {
                if (nombre.equalsIgnoreCase(n)) {
                    salida.writeUTF("true");
                    seguir = false;
                }
            }
            salida.writeUTF("false");

            String proseguir = entrada.readUTF();
            seguir = !proseguir.equals("false");

            if (!seguir) {
                continue;
            }

            UnCliente unCliente = new UnCliente(s, nombre);

            System.out.println("--> Se ha conectado el cliente: " + nombre);

            Thread hilo = new Thread(unCliente);
            lista.put(nombre, unCliente);
            hilo.start();
        }

    }
    
    public void verificarPuerto(String p) {
        int puerto = 0;

        // -------------- Sentencia Original -------------- //
        boolean error = false;
        try {
            puerto = Integer.parseInt(p);
        } catch (NumberFormatException e) {
            System.out.println("El puerto solo debe contener numeros enteros.");
            error = true;

        } finally {
            if (p.length() > 5) {
                System.out.println("El puerto no debe exceder los 5 digitos.");
                error = true;
            }
            if (error) {
                System.exit(1);
            }
        }

        //65535 maximo puerto
        if (puerto < 1 || puerto > 65535) {
            System.out.println("El puerto debe encontrarse entre 1 y 65535.");
            System.exit(1);
        }

    }
    

}
