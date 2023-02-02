package servidorb;

import java.util.ArrayList;
import java.util.HashMap;

public class TableroO {

    final String oceano = "-";
    final String nave = "O";
    final String tiro = "X";
    final String adyacente = "*";

    final String[] filas = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    final String[] columnas = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    HashMap<String, String> listaBarcos = new HashMap<>();
    ArrayList<Barco> barcos = new ArrayList<>();

    public enum barco {
        PORTAAVIONES, BUQUE, SUBMARINO, CRUCERO, LANCHA
    };

    int portaAviones = 5;
    int buque = 4;
    int submarino = 3;
    int crucero = 2;
    int lancha = 1;

    String[][] tablero = new String[10][10];

    public int casillasDisp = 30;

    public TableroO() {

        listaBarcos.put("5", barco.PORTAAVIONES.toString());
        listaBarcos.put("4", barco.BUQUE.toString());
        listaBarcos.put("3", barco.SUBMARINO.toString());
        listaBarcos.put("2", barco.CRUCERO.toString());
        listaBarcos.put("1", barco.LANCHA.toString());

        construirTablero();

    }

    public String construirBarco(Barco b) {
        String estado = "";

        if (calcularCasillas(b)) {
            String e = construirCasillasBarco(b);
            String[] pts = e.split(" ");
            if (pts[0].equals("true")) {
                construirCasillasAdyacentes(b);
                barcos.add(b);
                estado = "true" + " " + "bien";
            } else if (pts[0].equals("false")) {
                if (pts[1].equals("ocupado")) {
                    //Barco se sobrepone en otro ya colocado.
                    estado = "false" + " " + "ocupado";
                } else if (pts[1].equals("pegado")){
                    estado = "false" + " " + "adyacente";
                }
            }

        } else {
            //Barco no cabe.
            estado = "false" + " " + "excede";
        }
        
        return estado;
    }

    public boolean calcularCasillas(Barco b) {

        boolean correcto = true;
        try {
            String start = b.casillaInicial;
            int size = b.size;
            String pos = b.posicion;

            String n = "";
            if (start.length() == 3) {
                n = String.valueOf(start.charAt(1)) + String.valueOf(start.charAt(2));
            } else if (start.length() == 2) {
                n = String.valueOf(start.charAt(1));
            }
            int col = Integer.parseInt(n) - 1;

            int fila = calcularFila(b.casillaInicial);

            if (pos.equals("V")) {
                for (int x = fila; x < (fila + size); x++) {
                    tablero[x][col] = tablero[x][col];
                }
            } else {
                for (int x = col; x < (col + size); x++) {
                    tablero[fila][x] = tablero[fila][x];
                }
            }
        } catch (Exception e) {
            correcto = false;
        }
        return correcto;
    }

    public String construirCasillasBarco(Barco b) {

        String correcto = "true" + " " + "bien";
        try {
            String start = b.casillaInicial;
            int size = b.size;
            String pos = b.posicion;

            String n = "";
            if (start.length() == 3) {
                n = String.valueOf(start.charAt(1)) + String.valueOf(start.charAt(2));
            } else if (start.length() == 2) {
                n = String.valueOf(start.charAt(1));
            }
            int col = Integer.parseInt(n) - 1;

            int fila = calcularFila(b.casillaInicial);

            if (pos.equals("V")) {
                for (int x = fila; x < (fila + size); x++) {
                    if (tablero[x][col].equals(nave)) {
                        correcto = "false" + " " + "ocupado";
                        break;
                    } else if (tablero[x][col].equals(adyacente)) {
                        correcto = "false" + " " + "pegado";
                        break;
                    } else {
                        tablero[x][col] = nave;
                    }
                    if (((fila + size) - 1) == x) {
                        b.casillaFinal = "" + filas[x] + (col + 1);

                    }
                }
            } else {
                for (int x = col; x < (col + size); x++) {
                    if (tablero[fila][x].equals(nave)) {
                        correcto = "false" + " " + "ocupado";
                        break;
                    } else if (tablero[fila][x].equals(adyacente)) {
                        correcto = "false" + " " + "pegado";
                        break;
                    } else {
                        tablero[fila][x] = nave;
                    }

                    if (((col + size) - 1) == x) {
                        b.casillaFinal = String.valueOf(start.charAt(0)) + (x + 1);

                    }
                }
            }
        } catch (Exception e) {
            correcto = "false" + " " + "excepcion";
        }
        return correcto;
    }

    public void construirCasillasAdyacentes(Barco b) {

        try {
            String start = b.casillaInicial;
            int size = b.size;
            String pos = b.posicion;

            String n = "";
            if (start.length() == 3) {
                n = String.valueOf(start.charAt(1)) + String.valueOf(start.charAt(2));
            } else if (start.length() == 2) {
                n = String.valueOf(start.charAt(1));
            }

            int col = Integer.parseInt(n) - 1;
            int fila = calcularFila(b.casillaInicial);

            if (pos.equalsIgnoreCase("V")) {

                if (fila > 0) {
                    tablero[fila - 1][col] = "*";
                }

                fila = calcularFila(b.casillaFinal);
                if (fila < 9) {

                    tablero[fila + 1][col] = "*";
                }

            } else if (pos.equalsIgnoreCase("H")) {
                int s = col;

                String end = b.casillaFinal;
                if (end.length() == 3) {
                    n = String.valueOf(end.charAt(1)) + String.valueOf(end.charAt(2));
                } else if (end.length() == 2) {
                    n = String.valueOf(end.charAt(1));
                }

                col = Integer.parseInt(n) - 1;

                for (int x = s; x <= col; x++) {
                    if (fila < 9) {
                        tablero[fila + 1][x] = "*";
                    }
                    if (fila > 0) {
                        tablero[fila - 1][x] = "*";
                    }

                }

            }
        } catch (Exception e) {

        }

    }

    public int calcularFila(String coord) {
        int c = 0;
        String fila = String.valueOf(coord.charAt(0));
        for (String s : filas) {
            if (filas[c].equals(fila)) {
                break;
            }
            c++;
        }

        return c;
    }

    public void construirTablero() {

        for (int x = 0; x < filas.length; x++) {
            for (int y = 0; y < columnas.length; y++) {
                tablero[x][y] = oceano;

            }
        }

    }

    public String getTablero() {
        String espaciado = "  ";

        String cadena = "";
        cadena += espaciado + " ";
        //Esto imprime los numeros de arriba
        for (String columna : columnas) {
            cadena += columna + espaciado;
        }
        cadena += "ln";

        for (int x = 0; x < filas.length; x++) {
            cadena += filas[x] + espaciado;
            for (int y = 0; y < columnas.length; y++) {
                cadena += tablero[x][y] + espaciado;
            }
            cadena += "ln";
        }
        cadena += "ln";

        return cadena;
    }

}
