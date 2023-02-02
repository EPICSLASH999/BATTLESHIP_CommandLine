package tableros;

import java.util.ArrayList;
import java.util.HashMap;

public class TableroO {

    final String oceano = "-";
    final String nave = "O";
    final String adyacente = "*";
    
    final String tiroAcertado = "X";
    final String tiroFallido = "+";

    final String[] filas = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    final String[] columnas = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    public HashMap<String, String> listaBarcos = new HashMap<>();
    ArrayList<Barco> barcos = new ArrayList<>();

    public enum barco {
        PORTAAVIONES, BUQUE, SUBMARINO, CRUCERO, LANCHA
    };

    String[][] tablero = new String[10][10];

    public int casillasDisp = 30;
    
    public boolean listo = false;
    public boolean esTurno = false;
    
    public TableroT tabT;

    public TableroO(boolean esTurno) {

        listaBarcos.put("5", barco.PORTAAVIONES.toString());
        listaBarcos.put("4", barco.BUQUE.toString());
        listaBarcos.put("3", barco.SUBMARINO.toString());
        listaBarcos.put("2", barco.CRUCERO.toString());
        listaBarcos.put("1", barco.LANCHA.toString());

        construirTablero();
        
        tabT = new TableroT();
        
        this.esTurno = esTurno;
    }
    
    public boolean estaListo(){
        return listo;
    }
    
    public boolean esTurno(){
        return esTurno;
    }
    
    public String Tiro(String coord){
        String elTiroFue = "";
        int col = calcularColumna(coord) - 1;
        int fila = calcularFila(coord);
        
        String tiro = tablero[fila][col];
        
        switch(tiro){
            case oceano:
                elTiroFue = "¡Agua!";
                tablero[fila][col] = tiroFallido;
                esTurno = true;
                break;
            case adyacente:
                elTiroFue = "¡Agua!";
                tablero[fila][col] = tiroFallido;
                esTurno = true;
                break;
            case nave:
                
                for(Barco b:barcos){
                    if(b.casillasRestantes.contains(coord)){
                        b.casillasRestantes.remove(coord);
                        tablero[fila][col] = tiroAcertado;
                        if(b.casillasRestantes.size()>0){
                            elTiroFue = "¡Impacto!";
                        } else {
                            elTiroFue = "¡Hundido!";
                            barcos.remove(b);
                            if(barcos.isEmpty()){
                                elTiroFue = "¡Fin!";
                            }
                        }
                        break;
                    }
                    esTurno = false;
                }
                break;
            default:
                elTiroFue = "¡Repetido!";
                esTurno = false;
                break;
        }
        
        return elTiroFue;
    }

    public String construirBarco(Barco b) {

        String e = calcularCasillas(b);
        String[] parts = e.split(" ");

        if (parts[0].equals("true")) {
            construirCasillasBarco(b);
            construirCasillasAdyacentes(b);
            barcos.add(b);
        }

        return e;
    }

    public String calcularCasillas(Barco b) {

        String correcto = "true" + " " + "bien";
        try {
            String start = b.getCasillaInicial();
            int size = b.size;
            String pos = b.posicion;

            int col = calcularColumna(start) - 1;
            int fila = calcularFila(b.getCasillaInicial());

            if (pos.equals("V")) {
                for (int x = fila; x < (fila + size); x++) {
                    if (tablero[x][col].equals(nave)) {
                        correcto = "false" + " " + "ocupado";
                        break;
                    } else if (tablero[x][col].equals(adyacente)) {
                        correcto = "false" + " " + "adyacente";
                        break;
                    } else {
                        tablero[x][col] = tablero[x][col];
                    }
                }
            } else {
                for (int x = col; x < (col + size); x++) {
                    if (tablero[fila][x].equals(nave)) {
                        correcto = "false" + " " + "ocupado";
                        break;
                    } else if (tablero[fila][x].equals(adyacente)) {
                        correcto = "false" + " " + "adyacente";
                        break;
                    } else {
                        tablero[fila][x] = tablero[fila][x];
                    }
                }
            }
        } catch (Exception e) {
            correcto = "false" + " " + "excede";
        }
        return correcto;
    }

    public void construirCasillasBarco(Barco b) {

        try {
            String start = b.getCasillaInicial();
            int size = b.size;
            String pos = b.posicion;

            int col = calcularColumna(start) - 1;
            int fila = calcularFila(b.getCasillaInicial());

            if (pos.equals("V")) {
                for (int x = fila; x < (fila + size); x++) {
                    tablero[x][col] = nave;
                    b.agregarCasilla(filas[x] + columnas[col]);
                }
            } else {
                for (int x = col; x < (col + size); x++) {
                    tablero[fila][x] = nave;
                    b.agregarCasilla(filas[fila] + columnas[x]);
                }
            }
        } catch (Exception e) {
        }
    }

    public void construirCasillasAdyacentes(Barco b) {

        try {
            adyacentesVerticales(b);
            adyacentesLaterales(b);
            adyacentesEsquinas(b);
        } catch (Exception e) {

        }

    }

    public void adyacentesVerticales(Barco b) {
        String start = b.getCasillaInicial();
        String pos = b.posicion;

        int col = calcularColumna(start) - 1;
        int fila = calcularFila(b.getCasillaInicial());

        if (pos.equalsIgnoreCase("V")) {

            if (fila > 0) {
                tablero[fila - 1][col] = adyacente;
            }

            fila = calcularFila(b.getCasillaFinal());
            if (fila < 9) {
                tablero[fila + 1][col] = adyacente;
            }

        } else if (pos.equalsIgnoreCase("H")) {
            int s = col;

            String end = b.getCasillaFinal();

            col = calcularColumna(end) - 1;

            for (int x = s; x <= col; x++) {
                if (fila < 9) {
                    tablero[fila + 1][x] = adyacente;
                }
                if (fila > 0) {
                    tablero[fila - 1][x] = adyacente;
                }
            }
        }
    }

    public void adyacentesLaterales(Barco b) {
        String pos = b.posicion;
        int colu;
        int fil;

        if (pos.equalsIgnoreCase("V")) {

            for (String cas : b.casillas) {
                colu = calcularColumna(cas) - 1;
                fil = calcularFila(cas);

                if (colu > 0) {
                    tablero[fil][colu - 1] = adyacente;
                }
                if (colu < (columnas.length - 1)) {
                    tablero[fil][colu + 1] = adyacente;
                }
            }
        } else if (pos.equalsIgnoreCase("H")) {

            String coord = b.getCasillaInicial();
            colu = calcularColumna(coord) - 1;
            fil = calcularFila(coord);
            if (colu > 0) {
                tablero[fil][colu - 1] = adyacente;
            }

            coord = b.getCasillaFinal();
            colu = calcularColumna(coord) - 1;
            fil = calcularFila(coord);
            if (colu < (columnas.length - 1)) {
                tablero[fil][colu + 1] = adyacente;
            }
        }
    }

    public void adyacentesEsquinas(Barco b) {
        String pos = b.posicion;
        String start = b.getCasillaInicial();
        int col;
        int fil;

        if (pos.equalsIgnoreCase("V")) {

            col = calcularColumna(start) - 1;
            fil = calcularFila(start);

            if (fil > 0) {
                if (col > 0) {
                    tablero[fil - 1][col - 1] = adyacente;
                }
                if (col < (columnas.length - 1)) {
                    tablero[fil - 1][col + 1] = adyacente;
                }
            }

            String end = b.getCasillaFinal();
            col = calcularColumna(end) - 1;
            fil = calcularFila(end);

            if (fil < (filas.length - 1)) {
                if (col > 0) {
                    tablero[fil + 1][col - 1] = adyacente;
                }
                if (col < (columnas.length - 1)) {
                    tablero[fil + 1][col + 1] = adyacente;
                }
            }

        } else if (pos.equalsIgnoreCase("H")) {

            col = calcularColumna(start) - 1;
            fil = calcularFila(start);

            if (col > 0) {
                if (fil > 0) {
                    tablero[fil - 1][col - 1] = adyacente;
                }
                if (fil < (filas.length - 1)) {
                    tablero[fil + 1][col - 1] = adyacente;
                }
            }

            String end = b.getCasillaFinal();
            col = calcularColumna(end) - 1;
            fil = calcularFila(end);

            if (col < (columnas.length - 1)) {
                if (fil < (filas.length - 1)) {
                    tablero[fil + 1][col + 1] = adyacente;
                }
                if (fil > 0) {
                    tablero[fil - 1][col + 1] = adyacente;
                }
            }

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

    public int calcularColumna(String coord) {
        String n = "";
        if (coord.length() == 3) {
            n = String.valueOf(coord.charAt(1)) + String.valueOf(coord.charAt(2));
        } else if (coord.length() == 2) {
            n = String.valueOf(coord.charAt(1));
        }
        return Integer.parseInt(n);
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
        
        cadena += "* * * * * MAPA DE OCEANO * * * * *ln";
        cadena += espaciado + " ";
        //Esto imprime los numeros de arriba
        for (String columna : columnas) {
            cadena += columna + espaciado;
        }
        cadena += "ln";

        for (int x = 0; x < filas.length; x++) {
            cadena += filas[x] + espaciado;
            for (int y = 0; y < columnas.length; y++) {
                if (tablero[x][y].equals(adyacente)) {
                    cadena += oceano + espaciado;
                }
                else{
                    cadena += tablero[x][y] + espaciado;
                }
                
            }
            cadena += "ln";
        }
        cadena += "ln";

        return cadena;
    }

    public String getTableroTiro(){
        return tabT.getTablero();
    }
}
