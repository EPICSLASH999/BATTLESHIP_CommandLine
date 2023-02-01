package servidorh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class UnCliente implements Runnable {

    final DataInputStream entrada;
    final DataOutputStream salida;
    final String nombre;

    public UnCliente(Socket s, String nombre) throws IOException {
        entrada = new DataInputStream(s.getInputStream());
        salida = new DataOutputStream(s.getOutputStream());
        this.nombre = nombre;
    }

    @Override
    public void run() {
        String mensaje;
        while (true) {
            try {
                mensaje = entrada.readUTF();
                String[] parts = mensaje.split("@");
                boolean existe = false;
                boolean tieneArr = false;
                UnCliente c = null;
                for (UnCliente cliente : ServidorH.lista.values()) {

                    //if (!cliente.nombre.equals(nombre)) {
                        if (!mensaje.contains("@")) {
                            if (!cliente.nombre.equalsIgnoreCase(nombre)) {
                                cliente.salida.writeUTF(mensaje);
                            }
                            
                        } else {
                            //tieneArr = true;
                            
                            if (parts.length >= 2) {
                                if (cliente.nombre.equalsIgnoreCase(parts[1])) {
                                    cliente.salida.writeUTF(parts[0]);
                                    //existe = true;
                                }
                            }

                        }
                    /*}
                    else{
                        //c = cliente;
                    }*/

                }
                /*if (!existe && tieneArr){
                    c.salida.writeUTF("No existe el destinatario.");
                }*/
            } catch (IOException ex) {

            }
        }
    }
}
