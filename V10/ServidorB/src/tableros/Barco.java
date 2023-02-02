package tableros;

import java.util.ArrayList;

public class Barco {

    final int size;
    final String posicion;
    
    ArrayList<String> casillas = new ArrayList<>();
    ArrayList<String> casillasRestantes = new ArrayList<>();

    public Barco(String size, String posicion, String casillaInicial) {
        
        this.size = Integer.parseInt(size);
        this.posicion = posicion;
        
        casillas.add(casillaInicial);
        
    }
    
    public void agregarCasilla(String casilla){
        if (!casillas.contains(casilla)){
            casillas.add(casilla);
        }
        if (casillas.size() == size){ //Si se termina de llenar el tamaño total de casillas, las envía a casillasRestantes
            casillas.forEach((c) -> {
                casillasRestantes.add(c);
            });
        }
    }
    
    public String getCasillaInicial(){
        return casillas.get(0);
    }
    
    public String getCasillaFinal(){
        return casillas.get(size-1);
    }

}