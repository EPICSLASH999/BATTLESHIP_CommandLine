
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
        ServerSocket socketServidor = new ServerSocket(8080);
        int contador = 0;
        
        System.out.println("SERVIDOR GENERADO !!");
        System.out.println("");
        
        while (true) {
            Socket s = socketServidor.accept();

            DataInputStream entrada = new DataInputStream(s.getInputStream());
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());
            
            String nombre = null;
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
            
            if (!seguir)
                continue;
            
            UnCliente unCliente = new UnCliente(s, nombre);

            System.out.println("--> Se ha conectado el cliente: " + nombre);

            Thread hilo = new Thread(unCliente);
            lista.put(nombre, unCliente);
            hilo.start();
            contador++;
        }
    }
    
}