package servidorb;

public class Barco {

    final int size;
    final String posicion;
    final String casillaInicial;
    

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
        this.casillaInicial = casillaInicial;
    }
    
    

}
