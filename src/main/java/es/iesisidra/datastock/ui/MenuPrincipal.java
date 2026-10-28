package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.ficheros.ExploradorRutas;
import es.iesisidra.datastock.ficheros.GestorExportacion;
import es.iesisidra.datastock.ficheros.GestorSerializacion;
import es.iesisidra.datastock.ficheros.ImportadorJson;
import es.iesisidra.datastock.ficheros.ImportadorXml;
import es.iesisidra.datastock.servicio.GestorConsultas;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Gestiona la interacción con el usuario y delega las operaciones
 * en los servicios correspondientes de acceso a datos y ficheros.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;
    private final ExploradorRutas exploradorRutas;
    private final ImportadorJson importadorJson;
    private final ImportadorXml importadorXml;
    private final GestorConsultas gestorConsultas;
    private final GestorExportacion gestorExportacion;
    private final GestorSerializacion gestorSerializacion;

    /**
     * @param inventario colección común de productos de esta ejecución.
     */
    public MenuPrincipal(Inventario inventario) {
        this.inventario = inventario;
        this.exploradorRutas = new ExploradorRutas();
        this.importadorJson = new ImportadorJson(inventario);
        this.importadorXml = new ImportadorXml(inventario);
        this.gestorConsultas = new GestorConsultas(inventario);
        this.gestorExportacion = new GestorExportacion(inventario);
        this.gestorSerializacion = new GestorSerializacion(inventario);
    }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("0. Salir");
            System.out.println("------------------------------------------------");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);
            switch (opcion) {
                case 1 -> opcionExplorar();
                case 2 -> opcionImportarJson();
                case 3 -> opcionImportarXml();
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void opcionExplorar() {
        String ruta = LectorConsola.leerTexto("Introduce la ruta a explorar: ");
        exploradorRutas.explorar(ruta);
        LectorConsola.pausar();
    }

    private void opcionImportarJson() {
        String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero JSON: ");
        importadorJson.importar(ruta);
        LectorConsola.pausar();
    }

    private void opcionImportarXml() {
        String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero XML: ");
        importadorXml.importar(ruta);
        LectorConsola.pausar();
    }

    private void menuConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            System.out.println("-------------------------------------------");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
            switch (opcion) {
                case 1 -> {
                    gestorConsultas.listarTodos();
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String codigo = LectorConsola.leerTexto("Introduce el código del producto: ");
                    gestorConsultas.buscarPorCodigo(codigo);
                    LectorConsola.pausar();
                }
                case 3 -> {
                    String fragmento = LectorConsola.leerTexto("Introduce texto a buscar en el nombre: ");
                    gestorConsultas.buscarPorNombre(fragmento);
                    LectorConsola.pausar();
                }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;
        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            System.out.println("----------------------------------------");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta de destino JSON: ");
                    gestorExportacion.exportarJson(ruta);
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta de destino XML: ");
                    gestorExportacion.exportarXml(ruta);
                    LectorConsola.pausar();
                }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;
        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            System.out.println("---------------------------------------------------");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero .dat para guardar: ");
                    gestorSerializacion.guardar(ruta);
                    LectorConsola.pausar();
                }
                case 2 -> {
                    String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero .dat a recuperar: ");
                    gestorSerializacion.recuperar(ruta);
                    LectorConsola.pausar();
                }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }
}
