package servidorh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServidorH {

    static HashMap<String, UnCliente> lista = new HashMap<String, UnCliente>();

    public static void main(String[] args) throws IOException {
        ServerSocket socketServidor = new ServerSocket(8080);
        int contador = 0;
        
        while (true) {
            Socket s = socketServidor.accept();

            DataInputStream entrada = new DataInputStream(s.getInputStream());
            DataOutputStream salida = new DataOutputStream(s.getOutputStream());
            boolean ciclo = false;
            String mensaje = null;
            boolean seguir = true;

            mensaje = entrada.readUTF();
            for (String n : ServidorH.lista.keySet()) {
                if (mensaje.equalsIgnoreCase(n)) {
                    salida.writeUTF("true");
                    seguir = false;
                }
            }
            salida.writeUTF("false");
            if (!seguir)
                continue;
            
            

            /*do {
                ciclo = false;
                mensaje = entrada.readUTF();
                for (String n : ServidorH.lista.keySet()) {
                    if (mensaje.equals(n)) {
                        salida.writeUTF("El nombre esta repetido.");
                        ciclo = true;
                    }
                }
            } while (ciclo);
            salida.writeUTF("FIN");*/
            UnCliente unCliente = new UnCliente(s, mensaje);

            System.out.println("--> Se ha conectado el cliente: " + mensaje);

            Thread hilo = new Thread(unCliente);
            lista.put(mensaje, unCliente);
            hilo.start();
            contador++;
        }
    }

}
