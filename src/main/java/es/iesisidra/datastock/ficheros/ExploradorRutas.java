package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.util.ColoresConsola;

import java.io.File;

/**
 * Servicio para explorar rutas, ficheros y directorios (RF-FIC-01).
 */
public class ExploradorRutas {

    /**
     * Explora la ruta indicada y muestra la información requerida por consola.
     *
     * @param rutaStr Ruta a explorar (relativa o absoluta)
     */
    public void explorar(String rutaStr) {
        System.out.println(ColoresConsola.pintar("\n===== EXPLORAR RUTA =====", ColoresConsola.CIAN));
        if (rutaStr == null || rutaStr.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir una ruta válida.", ColoresConsola.ROJO));
            return;
        }

        File destino = new File(rutaStr.trim());
        System.out.println("Ruta: " + rutaStr.trim());

        if (!destino.exists()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta no existe: " + destino.getPath(), ColoresConsola.ROJO));
            return;
        }

        if (destino.isFile()) {
            System.out.println(ColoresConsola.pintar("[FICHERO]", ColoresConsola.VERDE) + " " + destino.getName()
                    + " " + destino.length() + " bytes");
            System.out.println("Ruta absoluta: " + destino.getAbsolutePath());
            System.out.println("Tamaño: " + destino.length() + " bytes");
            return;
        }

        if (destino.isDirectory()) {
            File[] elementos = destino.listFiles();
            if (elementos == null) {
                System.out.println(ColoresConsola.pintar("Error: No se tienen permisos para acceder al directorio o hubo un error de lectura.", ColoresConsola.ROJO));
                return;
            }

            if (elementos.length == 0) {
                System.out.println(ColoresConsola.pintar("El directorio está vacío.", ColoresConsola.AMARILLO));
                System.out.println("Total ficheros: 0 | Directorios: 0");
                return;
            }

            int totalFicheros = 0;
            int totalDirectorios = 0;

            for (File elemento : elementos) {
                if (elemento.isDirectory()) {
                    totalDirectorios++;
                    System.out.println(ColoresConsola.pintar("[DIR]     ", ColoresConsola.AZUL) + elemento.getName());
                } else {
                    totalFicheros++;
                    System.out.println(ColoresConsola.pintar("[FICHERO] ", ColoresConsola.VERDE)
                            + String.format("%-25s %d bytes", elemento.getName(), elemento.length()));
                }
            }

            System.out.println("Total ficheros: " + totalFicheros + " | Directorios: " + totalDirectorios);
        }
    }
}
