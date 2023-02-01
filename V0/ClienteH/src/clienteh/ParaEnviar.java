package clienteh;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParaEnviar implements Runnable {

    final DataOutputStream salida;
    Scanner teclado;

    public ParaEnviar(Socket socket) throws IOException {
        teclado = new Scanner(System.in);
        salida = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        
        while (true) {
            String mensaje = teclado.nextLine();
            try {
                salida.writeUTF(mensaje);
            } catch (IOException ex) {

            }
        }

    }

}
