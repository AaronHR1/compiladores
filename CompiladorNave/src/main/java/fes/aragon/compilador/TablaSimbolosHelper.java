package fes.aragon.compilador;

import java.util.ArrayList;
import java.util.List;

public class TablaSimbolosHelper {
    private static ArrayList<String[]> simbolos = new ArrayList<>();

    public static void agregar(String nombre, String valorInicio, String valorFin) {
        simbolos.add(new String[]{nombre, valorInicio, valorFin});
    }

    public static ArrayList<String[]> getSimbolos() {
        return simbolos;
    }

    public static void limpiar() {
        simbolos.clear();
    }
}