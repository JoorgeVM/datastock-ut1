package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Punto de entrada para RF-FIC-08: Acceso aleatorio mediante RandomAccessFile.
 * <p>Actividad técnica complementaria e independiente del menú principal.</p>
 * <p>Cada registro tiene exactamente 36 bytes:
 * - 12 caracteres (UTF-16 BE, writeChars) = 24 bytes
 * - double (writeDouble) = 8 bytes
 * - int (writeInt) = 4 bytes
 * Total: 36 bytes por registro.</p>
 */
public final class AccesoAleatorioActividad {

    public static final int TAMANO_REGISTRO = 36;
    public static final int OFFSET_STOCK = 32;
    public static final int LONGITUD_CODIGO = 12;

    private AccesoAleatorioActividad() { }

    public static void main(String[] args) {
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("  RF-FIC-08 · ACCESO ALEATORIO CON RANDOMACCESSFILE", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));

        String ruta = LectorConsola.leerTexto("Ruta del fichero binario (por defecto datos/salidas/stock.bin): ").trim();
        if (ruta.isEmpty()) {
            ruta = "datos/salidas/stock.bin";
        }

        int opcion;
        do {
            System.out.println("\n===== ACCESO ALEATORIO =====");
            System.out.println("Fichero actual: " + ruta);
            System.out.println("1. Generar fichero stock.bin con datos de prueba");
            System.out.println("2. Consultar registro por posición lógica (seek)");
            System.out.println("3. Modificar únicamente stock de un registro (seek)");
            System.out.println("4. Mostrar todos los registros (lectura secuencial)");
            System.out.println("5. Cambiar ruta del fichero");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 5);

            switch (opcion) {
                case 1 -> generarFichero(ruta, true);
                case 2 -> consultarPosicion(ruta);
                case 3 -> modificarStock(ruta);
                case 4 -> mostrarTodos(ruta);
                case 5 -> {
                    String nuevaRuta = LectorConsola.leerTexto("Nueva ruta: ").trim();
                    if (!nuevaRuta.isEmpty()) {
                        ruta = nuevaRuta;
                    }
                }
                case 0 -> System.out.println(ColoresConsola.pintar("Fin de actividad RandomAccessFile.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    /**
     * Genera un fichero stock.bin con 4 productos didácticos de prueba (144 bytes).
     */
    public static void generarFichero(String ruta) {
        generarFichero(ruta, false);
    }

    public static void generarFichero(String ruta, boolean confirmarSobrescritura) {
        List<Producto> lista = new ArrayList<>();
        lista.add(new Producto("CAB-001", "Cable HDMI", new BigDecimal("9.99"), 20, 1, 1));
        lista.add(new Producto("IMP-002", "Impresora Láser", new BigDecimal("89.50"), 5, 2, 2));
        lista.add(new Producto("MON-003", "Monitor 24 pulgadas", new BigDecimal("149.99"), 8, 2, 2));
        lista.add(new Producto("TEC-004", "Teclado USB", new BigDecimal("19.95"), 12, 1, 1));

        escribirFicheroDesdeProductos(ruta, lista, confirmarSobrescritura);
    }

    /**
     * Escribe una lista de productos en el fichero binario en formato de 36 bytes por registro.
     */
    public static void escribirFicheroDesdeProductos(String ruta, List<Producto> lista) {
        escribirFicheroDesdeProductos(ruta, lista, false);
    }

    public static void escribirFicheroDesdeProductos(String ruta, List<Producto> lista, boolean confirmarSobrescritura) {
        if (lista == null || lista.isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: La lista de productos está vacía. No se genera el fichero.", ColoresConsola.AMARILLO));
            return;
        }

        File fichero = new File(ruta);
        File dir = fichero.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        if (fichero.exists() && confirmarSobrescritura) {
            boolean confirmar = LectorConsola.confirmar("El fichero ya existe. ¿Deseas sobrescribirlo?");
            if (!confirmar) {
                System.out.println(ColoresConsola.pintar("Operación cancelada. El fichero se conserva intacto.", ColoresConsola.AMARILLO));
                return;
            }
        }

        // Ordenar por código
        List<Producto> ordenados = new ArrayList<>(lista);
        ordenados.sort(Comparator.comparing(p -> p.getCodigo().toUpperCase()));

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "rw")) {
            // Truncar el fichero antes de escribir
            raf.setLength(0);

            for (Producto p : ordenados) {
                // 1. Código formateado a exactamente 12 caracteres (24 bytes)
                String cod = p.getCodigo() != null ? p.getCodigo().trim() : "";
                if (cod.length() > LONGITUD_CODIGO) {
                    cod = cod.substring(0, LONGITUD_CODIGO);
                } else {
                    cod = String.format("%-" + LONGITUD_CODIGO + "s", cod);
                }
                raf.writeChars(cod);

                // 2. Precio double (8 bytes)
                raf.writeDouble(p.getPrecio() != null ? p.getPrecio().doubleValue() : 0.0);

                // 3. Stock int (4 bytes)
                raf.writeInt(p.getStock());
            }

            System.out.println(ColoresConsola.pintar("Fichero generado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + fichero.getPath());
            System.out.println("Registros: " + ordenados.size() + " | Tamaño: " + raf.length() + " bytes");

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al generar el fichero binario: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Consulta un registro por su posición lógica en base 0 usando seek(posicion * 36).
     */
    public static void consultarPosicion(String ruta) {
        File fichero = new File(ruta);
        if (!validarFichero(fichero)) return;

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "r")) {
            int numRegistros = validarTamano(raf);
            if (numRegistros <= 0) return;

            int pos = LectorConsola.leerEntero("Consultar posición (0-" + (numRegistros - 1) + "): ");
            if (pos < 0 || pos >= numRegistros) {
                System.out.println(ColoresConsola.pintar("Error: Posición fuera de rango. Debe estar entre 0 y " + (numRegistros - 1) + ".", ColoresConsola.ROJO));
                return;
            }

            // Desplazamiento directo: posición * 36
            raf.seek((long) pos * TAMANO_REGISTRO);

            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stock = raf.readInt();

            System.out.println(codigo + " | " + String.format("%.2f €", precio) + " | stock " + stock);

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error de lectura: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Modifica el campo stock de un registro específico usando seek(posicion * 36 + 32).
     */
    public static void modificarStock(String ruta) {
        File fichero = new File(ruta);
        if (!validarFichero(fichero)) return;

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "rw")) {
            int numRegistros = validarTamano(raf);
            if (numRegistros <= 0) return;

            int pos = LectorConsola.leerEntero("Posición del registro a modificar (0-" + (numRegistros - 1) + "): ");
            if (pos < 0 || pos >= numRegistros) {
                System.out.println(ColoresConsola.pintar("Error: Posición fuera de rango. Debe estar entre 0 y " + (numRegistros - 1) + ".", ColoresConsola.ROJO));
                return;
            }

            // Leemos el registro completo para mostrarlo antes
            raf.seek((long) pos * TAMANO_REGISTRO);
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stockActual = raf.readInt();
            System.out.println("Registro actual: " + codigo + " | " + String.format("%.2f €", precio) + " | stock " + stockActual);

            int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");
            if (nuevoStock < 0) {
                System.out.println(ColoresConsola.pintar("Error: El stock no puede ser negativo.", ColoresConsola.ROJO));
                return;
            }

            // Desplazamiento exacto al campo stock: posición * 36 + 32
            long desplazamientoStock = ((long) pos * TAMANO_REGISTRO) + OFFSET_STOCK;
            raf.seek(desplazamientoStock);
            raf.writeInt(nuevoStock);

            System.out.println(ColoresConsola.pintar("Actualización correcta: stock " + stockActual + " -> " + nuevoStock, ColoresConsola.VERDE));

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al modificar el fichero: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Muestra todos los registros mediante lectura secuencial.
     */
    public static void mostrarTodos(String ruta) {
        File fichero = new File(ruta);
        if (!validarFichero(fichero)) return;

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "r")) {
            int numRegistros = validarTamano(raf);
            if (numRegistros <= 0) return;

            System.out.println("\nRegistros: " + numRegistros + " | Tamaño: " + raf.length() + " bytes");
            System.out.println("----------------------------------------------");
            for (int i = 0; i < numRegistros; i++) {
                String codigo = leerCodigo(raf);
                double precio = raf.readDouble();
                int stock = raf.readInt();
                System.out.println(String.format("[%d] %-12s | %8.2f € | stock %d", i, codigo, precio, stock));
            }
            System.out.println("----------------------------------------------");

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al leer el fichero: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static boolean validarFichero(File fichero) {
        if (!fichero.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + fichero.getPath(), ColoresConsola.ROJO));
            return false;
        }
        if (fichero.isDirectory()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta corresponde a un directorio.", ColoresConsola.ROJO));
            return false;
        }
        return true;
    }

    private static int validarTamano(RandomAccessFile raf) throws Exception {
        long longitud = raf.length();
        if (longitud == 0) {
            System.out.println(ColoresConsola.pintar("El fichero está vacío (0 bytes).", ColoresConsola.AMARILLO));
            return 0;
        }
        if (longitud % TAMANO_REGISTRO != 0) {
            System.out.println(ColoresConsola.pintar("Error: El tamaño del fichero (" + longitud + " bytes) no es múltiplo de " + TAMANO_REGISTRO + ". Fichero binario inválido.", ColoresConsola.ROJO));
            return -1;
        }
        return (int) (longitud / TAMANO_REGISTRO);
    }

    private static String leerCodigo(RandomAccessFile raf) throws Exception {
        char[] chars = new char[LONGITUD_CODIGO];
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            chars[i] = raf.readChar();
        }
        return new String(chars).trim();
    }
}
