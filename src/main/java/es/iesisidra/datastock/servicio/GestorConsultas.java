package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;

import java.util.List;

/**
 * Servicio para consultar y buscar productos en la colección en memoria (RF-FIC-04).
 */
public class GestorConsultas {

    private final Inventario inventario;

    public GestorConsultas(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Lista todos los productos disponibles en memoria, ordenados alfabéticamente por código.
     */
    public void listarTodos() {
        System.out.println(ColoresConsola.pintar("\n===== LISTADO DE PRODUCTOS =====", ColoresConsola.CIAN));
        if (inventario.estaVacio()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío. No hay productos para mostrar.", ColoresConsola.AMARILLO));
            return;
        }

        List<Producto> lista = inventario.getProductosOrdenadosPorCodigo();
        for (Producto p : lista) {
            mostrarFichaProducto(p);
        }
        System.out.println("Total productos en inventario: " + lista.size());
    }

    /**
     * Busca un producto por su código exacto (ignorando mayúsculas y espacios extremos).
     *
     * @param codigo Código a buscar
     */
    public void buscarPorCodigo(String codigo) {
        System.out.println(ColoresConsola.pintar("\n===== BÚSQUEDA POR CÓDIGO =====", ColoresConsola.CIAN));
        if (inventario.estaVacio()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío.", ColoresConsola.AMARILLO));
            return;
        }
        if (codigo == null || codigo.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir un código para buscar.", ColoresConsola.ROJO));
            return;
        }

        Producto p = inventario.buscarPorCodigo(codigo);
        if (p == null) {
            System.out.println(ColoresConsola.pintar("No se encontró ningún producto con el código: " + codigo.trim(), ColoresConsola.AMARILLO));
            return;
        }

        mostrarFichaProducto(p);
    }

    /**
     * Busca productos cuyo nombre contenga el fragmento indicado (ignorando mayúsculas/minúsculas).
     *
     * @param fragmento Texto a buscar dentro del nombre
     */
    public void buscarPorNombre(String fragmento) {
        System.out.println(ColoresConsola.pintar("\n===== BÚSQUEDA POR NOMBRE =====", ColoresConsola.CIAN));
        if (inventario.estaVacio()) {
            System.out.println(ColoresConsola.pintar("El inventario está vacío.", ColoresConsola.AMARILLO));
            return;
        }
        if (fragmento == null || fragmento.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir un texto para buscar.", ColoresConsola.ROJO));
            return;
        }

        List<Producto> coincidentes = inventario.buscarPorNombre(fragmento);
        if (coincidentes.isEmpty()) {
            System.out.println(ColoresConsola.pintar("No se encontraron productos que contengan: \"" + fragmento.trim() + "\"", ColoresConsola.AMARILLO));
            return;
        }

        for (Producto p : coincidentes) {
            mostrarFichaProducto(p);
        }
        System.out.println("Coincidencias encontradas: " + coincidentes.size());
    }

    private void mostrarFichaProducto(Producto p) {
        System.out.println("----------------------------------------------");
        System.out.println("Código:    " + p.getCodigo());
        System.out.println("Nombre:    " + p.getNombre());
        System.out.println(String.format(java.util.Locale.ROOT, "Precio:    %.2f € | Stock: %d", p.getPrecio(), p.getStock()));
        System.out.println("Categoría: " + p.getCategoriaId() + " | Proveedor: " + p.getProveedorId());
    }
}
