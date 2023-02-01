
package clienteh;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParaRecibir implements Runnable{

    final DataInputStream entrada;
    public ParaRecibir(Socket socket) throws IOException {
        entrada = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        String mensaje;
        while (true){
            try {
                mensaje = entrada.readUTF();
                System.out.println(mensaje);
            } catch (IOException ex) {
                
            }
        }
    }
    
}
