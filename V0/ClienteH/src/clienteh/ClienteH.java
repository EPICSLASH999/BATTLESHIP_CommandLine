package clienteh;

import Ejemplo.Mensaje;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClienteH {
    
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);

        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
        Scanner teclado = new Scanner(System.in);
        DataInputStream entrada = new DataInputStream(socket.getInputStream());
        String message = "";

        System.out.println("--> Ingrese su nombre: ");
        String nombre = teclado.nextLine();
        salida.writeUTF(nombre);
        message = entrada.readUTF();
        if (message.equals("true")) {
            System.out.println("Ese nombre esta repetido.");
            
            socket.close();
            salida.close();
            entrada.close();
            teclado.close();
            
            System.exit(0);
        }
        System.out.println("================================");

        ParaRecibir paraRecibir = new ParaRecibir(socket);
        Thread hiloRecibir = new Thread(paraRecibir);
        hiloRecibir.start();

        ParaEnviar paraEnviar = new ParaEnviar(socket);
        Thread hiloEnviar = new Thread(paraEnviar);
        hiloEnviar.start();
    }

}
