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

    public void deslock(String palabraLock) {
        // Método para quitarse el lock propio despues de escribir algo (como una respuesta)
        deslock = true;
        this.palabraLock = palabraLock;
    }

    public boolean esComando(String mensaje) {
        String[] parts = mensaje.split(" ");

        if (parts[0].length() <= 1) {
            return false;
        }

        return parts[0].equals(parts[0].toUpperCase());
    }

    public void modoComando(String comando) {
        String[] parts = comando.split(" ");

        try {
            if (deslock) {
                return;
            }
            switch (parts[0]) {
                case "BATTLESHIP":
                    if (parts.length != 2) {
                        System.out.println("Comando no reconocido.");
                    } else {
                        salida.writeUTF(quitarEspFinal(comando));
                    }
                    break;
                case "TABLERO":
                    if (parts.length == 2) {
                        salida.writeUTF(quitarEspFinal(comando));
                    } else {
                        System.out.println("Comando no reconocido.");
                    }
                    break;
                case "BATTLE":
                    if (parts.length == 3) {
                        salida.writeUTF(quitarEspFinal(comando));
                    } else {
                        System.out.println("Comando no reconocido.");
                    }
                    break;
                case "RENDIRSE":
                    if (parts.length != 2) {
                        System.out.println("Comando no reconocido.");
                    } else {
                        salida.writeUTF(quitarEspFinal(comando));
                    }
                    break;
                case "PARTIDA":
                    System.out.println("Comando no reconocido.");
                    break;
                case "SELECCION":
                    System.out.println("Comando no reconocido.");
                    break;
                case "CHAT":
                    if (parts.length > 2) {
                        System.out.println("Comando no reconocido.");
                    } else {
                        salida.writeUTF(quitarEspFinal(comando));
                    }
                    break;
                default:
                    salida.writeUTF(comando);
                    break;
            }
        } catch (IOException e) {

        }
    }

    public String quitarEspFinal(String mensaje) {
        //Quita el espacio inicial y final de una cadena, sin importarle los espacios entre medio

        //Debido a la naturaleza del ptrograma, al llegar a este punto el mensaje no tiene espacio inicial, 
        //por lo que este método realmente solo estaría quitando el espacio final
        return mensaje.trim();
    }

    public void Temporizador(int segundos) {
        // Método de temporizador de segundos,
        //Este método sirve para, en caso de no responder la invitación de juego, la misma expira
        for (int s = 0; s < segundos; s++) {
            if (!deslock) {
                return;
            }
            delaySegundo();
        }
        try {
            if (deslock) {
                salida.writeUTF("Tempo");
                System.out.println("La invitación ha expirado.");
                deslock = false;
                salida.writeUTF(palabraLock);
            }

        } catch (IOException e) {
        }
    }

    private void delaySegundo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        String mensaje;
        while (true) {

            try {
                mensaje = teclado.nextLine();
                
                if (esComando(mensaje)) {
                    modoComando(mensaje);
                } else {
                    salida.writeUTF(mensaje);
                    if (deslock) {
                        salida.writeUTF(palabraLock);
                        deslock = false;
                    }
                }
            } catch (Exception ex) {
                System.out.println("");
                System.out.println("¡ HASTA LA PRÓXIMA !");
                System.exit(0);
            }

        }

    }
}
