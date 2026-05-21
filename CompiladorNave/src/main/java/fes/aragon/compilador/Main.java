package fes.aragon.compilador;

public class Main {
    public static void main(String[] args) throws Exception {
        // Ruta del archivo que escribe el niño
        String entrada = "programa.nave";
        // Ruta del .fes que leerá el TableroInterprete
        String salida  = "salida.fes";

        System.out.println("Compilando " + entrada + "...");
        parser.compilar(entrada, salida);
    }
}