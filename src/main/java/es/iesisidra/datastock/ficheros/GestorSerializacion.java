package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.validacion.ValidadorProducto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para persistir y recuperar el inventario mediante serialización binaria (RF-FIC-06).
 */
public class GestorSerializacion {

    private final Inventario inventario;

    public GestorSerializacion(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Guarda la colección completa de productos en un fichero binario serializado.
     */
    public void guardar(String rutaStr) {
        guardar(rutaStr, false);
    }

    /**
     * Guarda la colección completa permitiendo omitir la confirmación interactiva.
     */
    public void guardar(String rutaStr, boolean forzarSobrescritura) {
        System.out.println(ColoresConsola.pintar("\n===== GUARDAR INVENTARIO =====", ColoresConsola.CIAN));
        if (inventario.estaVacio()) {
            System.out.println(ColoresConsola.pintar("Error: El inventario está vacío. No hay productos para guardar.", ColoresConsola.AMARILLO));
            return;
        }

        if (rutaStr == null || rutaStr.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes indicar una ruta de destino.", ColoresConsola.ROJO));
            return;
        }

        File fichero = new File(rutaStr.trim());
        File dirPadre = fichero.getParentFile();
        if (dirPadre != null && !dirPadre.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El directorio de destino no existe: " + dirPadre.getPath(), ColoresConsola.ROJO));
            return;
        }

        if (fichero.exists() && !forzarSobrescritura) {
            boolean sobreescribir = LectorConsola.confirmar("El fichero ya existe. ¿Deseas sobrescribirlo?");
            if (!sobreescribir) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. El fichero no se ha modificado.", ColoresConsola.AMARILLO));
                return;
            }
        }

        List<Producto> copia = new ArrayList<>(inventario.getProductos());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichero))) {
            oos.writeObject(copia);
            System.out.println(ColoresConsola.pintar("Inventario guardado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + fichero.getPath());
            System.out.println("Productos guardados: " + copia.size());
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al guardar el inventario: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Recupera el inventario desde un fichero binario serializado previamente por DataStock.
     */
    public void recuperar(String rutaStr) {
        recuperar(rutaStr, true);
    }

    /**
     * Recupera el inventario permitiendo controlar si se requiere confirmación por consola.
     */
    public void recuperar(String rutaStr, boolean pedirConfirmacionSiHayDatos) {
        System.out.println(ColoresConsola.pintar("\n===== RECUPERAR INVENTARIO =====", ColoresConsola.CIAN));
        if (rutaStr == null || rutaStr.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes indicar la ruta del fichero a recuperar.", ColoresConsola.ROJO));
            return;
        }

        File fichero = new File(rutaStr.trim());
        System.out.println("Ruta: " + rutaStr.trim());

        if (!fichero.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + fichero.getPath(), ColoresConsola.ROJO));
            return;
        }
        if (fichero.isDirectory()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta corresponde a un directorio.", ColoresConsola.ROJO));
            return;
        }
        if (!fichero.canRead()) {
            System.out.println(ColoresConsola.pintar("Error: No se tienen permisos de lectura sobre el fichero.", ColoresConsola.ROJO));
            return;
        }

        if (!inventario.estaVacio()) {
            System.out.println("Actualmente hay " + inventario.getProductos().size() + " productos en memoria.");
            System.out.println("La recuperación sustituirá el inventario actual.");
            if (pedirConfirmacionSiHayDatos) {
                boolean confirmar = LectorConsola.confirmar("¿Continuar?");
                if (!confirmar) {
                    System.out.println(ColoresConsola.pintar("Operación cancelada. Se conserva el inventario actual.", ColoresConsola.AMARILLO));
                    return;
                }
            }
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichero))) {
            Object obj = ois.readObject();
            if (!(obj instanceof List<?> listaRecuperada)) {
                System.out.println(ColoresConsola.pintar("Error: El contenido del fichero no es una lista válida de productos.", ColoresConsola.ROJO));
                return;
            }

            List<Producto> productosValidados = new ArrayList<>();
            for (Object item : listaRecuperada) {
                if (!(item instanceof Producto p)) {
                    System.out.println(ColoresConsola.pintar("Error: El fichero contiene elementos que no son objetos Producto.", ColoresConsola.ROJO));
                    return;
                }
                if (!ValidadorProducto.validar(p).esValido()) {
                    System.out.println(ColoresConsola.pintar("Error: El fichero contiene productos con datos no válidos.", ColoresConsola.ROJO));
                    return;
                }
                productosValidados.add(p);
            }

            // Solo sustituimos tras comprobar que todo se ha recuperado y validado sin fallo
            inventario.sustituirInventario(productosValidados);
            System.out.println(ColoresConsola.pintar("Inventario recuperado correctamente.", ColoresConsola.VERDE));
            System.out.println("Productos disponibles: " + inventario.getProductos().size());

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al recuperar el fichero (dañado, incompatible o corrupto). El inventario en memoria no ha sido modificado.", ColoresConsola.ROJO));
        }
    }
}
