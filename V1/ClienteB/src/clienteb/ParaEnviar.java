package clienteb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ParaEnviar implements Runnable {

    final DataOutputStream salida;
    Scanner teclado;

    boolean deslock = false;
    String palabraLock;
    
    public ParaEnviar(Socket socket) throws IOException {
        teclado = new Scanner(System.in);
        salida = new DataOutputStream(socket.getOutputStream());
    }
    
    public void deslock(String palabraLock){
        deslock = true;
        this.palabraLock = palabraLock;
    }

    @Override
    public void run() {
        String mensaje;
        while (true) {

            mensaje = teclado.nextLine();
            try {
                salida.writeUTF(mensaje);
                if (deslock){
                    salida.writeUTF(palabraLock);
                    deslock = false;
                }

            } catch (IOException ex) {

            }

        }

    }
}
