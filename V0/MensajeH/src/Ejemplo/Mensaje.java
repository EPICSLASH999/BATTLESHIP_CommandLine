
package Ejemplo;

public class Mensaje {
    
    enum TiposMensaje{GLOBAL, INCORRECTO};
    enum SubtiposMensaje{PARTICULAR, GLOBAL};
    
    String destinatario;
    String remitente;
    
    public Mensaje(){
        
    }
    
    public Mensaje(String cadena){
       TiposMensaje tipo = obtenerTipoMensaje(cadena);
       if (tipo == TiposMensaje.GLOBAL){
           SubtiposMensaje subTipo = obtenerSubtipoMensaje(cadena); 
           if (subTipo == SubtiposMensaje.PARTICULAR){
               String[] parts = cadena.split("@");
               destinatario = parts[1];
           }
       }
    }
    
    TiposMensaje obtenerTipoMensaje(String cadena){
        
        if (cadena.contains("@")){
            String[] parts = cadena.split("@");
            if (parts.length<2){
                return TiposMensaje.INCORRECTO;
            }
        }
        
        return TiposMensaje.GLOBAL;
    }
    
    SubtiposMensaje obtenerSubtipoMensaje(String cadena){
         SubtiposMensaje subTipo = null;
         if (cadena.contains("@")){
             subTipo = SubtiposMensaje.PARTICULAR;
         }
         else{
             subTipo = SubtiposMensaje.GLOBAL;
         }
         
         return subTipo;
    }
    
}
