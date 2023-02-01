package servidorb;

import java.util.ArrayList;
import java.util.HashMap;

public class TableroO {

    final String oceano = "-";
    final String acierto = "O";
    final String tiro = "X";
    final String nave = "[]";

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

    public TableroO() {

        listaBarcos.put(barco.PORTAAVIONES.toString(), "1");
        listaBarcos.put(barco.BUQUE.toString(), "2");
        listaBarcos.put(barco.SUBMARINO.toString(), "3");
        listaBarcos.put(barco.CRUCERO.toString(), "4");
        listaBarcos.put(barco.LANCHA.toString(), "5");

        construirTablero();
        construirBarcos();

    }

    public void construirBarcos() {
        barcos.add(new Barco(barco.PORTAAVIONES.toString(), "V", "C1"));
        barcos.add(new Barco(barco.SUBMARINO.toString(), "H", "D7"));
        barcos.add(new Barco(barco.CRUCERO.toString(), "V", "E5"));

        for (Barco b : barcos) {
            calcularCasillas(b);
        }

    }

    public boolean calcularCasillas(Barco b) {

        boolean correcto = true;
        try {
            String start = b.casillaInicial;
            int size = b.size;
            String pos = b.posicion;

            int col = Integer.parseInt(String.valueOf(start.charAt(1))) - 1;
            int fila = calcularFila(b.casillaInicial);

            if (pos.equals("V")) {
                for (int x = fila; x < (fila + size); x++) {
                    tablero[x][col] = acierto;
                }
            } else {
                for (int x = col; x < (col + size); x++) {
                    tablero[fila][x] = acierto;
                }
            }
        } catch (Exception e) {
            correcto = false;
        }
        return correcto;
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

    public void calcularNumCasillas() {
        /*int casillas = 0;
        Barco bu;
        //Regresa cuantos barcos hay
        //listaBarcos.get(b.toString())
        for (barco b: barco.values()){
            bu = new Barco(b.toString(), "V", "C1");
            
            casillas += Integer.parseInt(listaBarcos.get(b.toString())) * bu.size;
        }
        //Barco b = new Barco(barco.PORTAAVIONES.toString());
         */
    }

    public void construirTablero() {

        for (int x = 0; x < filas.length; x++) {
            for (int y = 0; y < columnas.length; y++) {
                tablero[x][y] = oceano;

            }
        }

    }

    public void getTablero() {
        System.out.print("  ");
        for (String columna : columnas) {
            System.out.print(columna + " ");
        }
        System.out.println("");

        for (int x = 0; x < filas.length; x++) {
            System.out.print(filas[x] + " ");
            for (int y = 0; y < columnas.length; y++) {
                System.out.print(tablero[x][y] + " ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

}
