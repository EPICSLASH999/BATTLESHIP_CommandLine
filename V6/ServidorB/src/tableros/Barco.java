package tableros;

import java.util.ArrayList;

public class Barco {

    final int size;
    final String posicion;
    
    ArrayList<String> casillas = new ArrayList<>();
    ArrayList<String> casillasRestantes = new ArrayList<>();

    public Barco(String tipo, String posicion, String casillaInicial) {
        switch (tipo) {
            case "PORTAAVIONES":
                size = 5;
                break;
            case "BUQUE":
                size = 4;
                break;
            case "SUBMARINO":
                size = 3;
                break;
            case "CRUCERO":
                size = 2;
                break;
            case "LANCHA":
                size = 1;
                break;
            default:
                size = 0;
                System.out.println("Something went wrong...");
        }
      
        this.posicion = posicion;
        
        casillas.add(casillaInicial);
        
    }
    
    public void agregarCasilla(String casilla){
        if (!casillas.contains(casilla)){
            casillas.add(casilla);
        }
        if (casillas.size() == size){
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