
package tableros;

public class TableroT {
    
    final String[] filas = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    final String[] columnas = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    
    String[][] tablero = new String[10][10];
    
    final String oceano = "-";
    
    final String tiroAcertado = "O";
    final String tiroFallido = "X";
    
    public TableroT(){
        construirTablero();
    }
    
    public void Tiro(String coord, boolean acertado){
        // Registra un tiro propio
        int col = calcularColumna(coord) - 1;
        int fil = calcularFila(coord);
        
        if (acertado){
            tablero[fil][col] = tiroAcertado;
        } else{
            tablero[fil][col] = tiroFallido;
        }
        
    }
    
    public int calcularFila(String coord) {
        // Devuelve el valor numerico de la fila a partir de una coordenada
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
        // Devuelve el valor numerico de la columna a partir de una coordenada
        String n = "";
        if (coord.length() == 3) {
            n = String.valueOf(coord.charAt(1)) + String.valueOf(coord.charAt(2));
        } else if (coord.length() == 2) {
            n = String.valueOf(coord.charAt(1));
        }
        return Integer.parseInt(n);
    }
    
    public void construirTablero() {
        // Método de generación de tablero.
        // Solo se crea 1 vez, y todo es puesto como agua por defecto
        for (int x = 0; x < filas.length; x++) {
            for (int y = 0; y < columnas.length; y++) {
                tablero[x][y] = oceano;
            }
        }
    }
    
    public String getTablero() {
        String margen = "               ";
        String espaciado = "  ";
        String cadena = "";
        
        cadena += margen + "* * * * * MAPA DE TIRO * * * * *ln";
        cadena += espaciado + " " + margen;
        //Esto imprime los numeros de arriba (columnas)
        for (String columna : columnas) {
            cadena += columna + espaciado;
        }
        cadena += "ln";

        for (int x = 0; x < filas.length; x++) {
            cadena += margen + filas[x] + espaciado;
            for (int y = 0; y < columnas.length; y++) {
                cadena += tablero[x][y] + espaciado;
                
            }
            cadena += "ln";
        }
        cadena += "ln";

        return cadena;
    }

}
