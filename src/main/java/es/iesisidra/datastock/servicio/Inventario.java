package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.validacion.ResultadoValidacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Estado común del inventario durante la ejecución de UT1.
 * <p>Mantiene la colección de productos en memoria y proporciona métodos
 * para consultar, buscar y modificar el catálogo cumpliendo las reglas de negocio.</p>
 */
public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    /**
     * Obtiene la colección que comparten las opciones del menú.
     */
    public List<Producto> getProductos() {
        return productos;
    }

    /**
     * Comprueba si el inventario está vacío.
     */
    public boolean estaVacio() {
        return productos.isEmpty();
    }

    /**
     * Comprueba si ya existe un producto con el código dado, ignorando mayúsculas/minúsculas.
     */
    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String busqueda = codigo.trim();
        for (Producto p : productos) {
            if (p.getCodigo() != null && p.getCodigo().trim().equalsIgnoreCase(busqueda)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Añade un producto al inventario si es válido y no está duplicado.
     *
     * @param p Producto a añadir
     * @return true si se añadió con éxito; false si fue rechazado
     */
    public boolean agregarProducto(Producto p) {
        ResultadoValidacion res = ValidadorProducto.validar(p);
        if (!res.esValido()) {
            return false;
        }
        if (existeCodigo(p.getCodigo())) {
            return false;
        }
        p.setCodigo(ValidadorProducto.normalizarCodigo(p.getCodigo()));
        if (p.getNombre() != null) {
            p.setNombre(p.getNombre().trim());
        }
        return productos.add(p);
    }

    /**
     * Busca un producto por coincidencia exacta de código (ignorando mayúsculas y espacios).
     */
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) return null;
        String busqueda = codigo.trim();
        for (Producto p : productos) {
            if (p.getCodigo() != null && p.getCodigo().trim().equalsIgnoreCase(busqueda)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Busca productos cuyo nombre contenga el fragmento dado (sin distinguir mayúsculas ni minúsculas).
     */
    public List<Producto> buscarPorNombre(String fragmento) {
        List<Producto> resultado = new ArrayList<>();
        if (fragmento == null || fragmento.trim().isEmpty()) {
            return resultado;
        }
        String buscado = fragmento.trim().toLowerCase();
        for (Producto p : productos) {
            if (p.getNombre() != null && p.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Devuelve una nueva lista con los productos ordenados por código.
     */
    public List<Producto> getProductosOrdenadosPorCodigo() {
        List<Producto> ordenados = new ArrayList<>(productos);
        ordenados.sort(Comparator.comparing(
                p -> p.getCodigo() != null ? p.getCodigo().trim().toUpperCase() : "",
                String.CASE_INSENSITIVE_ORDER
        ));
        return Collections.unmodifiableList(ordenados);
    }

    /**
     * Sustituye la colección actual por la nueva lista de productos.
     */
    public void sustituirInventario(List<Producto> nuevosProductos) {
        productos.clear();
        if (nuevosProductos != null) {
            for (Producto p : nuevosProductos) {
                if (p != null) {
                    p.setCodigo(ValidadorProducto.normalizarCodigo(p.getCodigo()));
                    if (p.getNombre() != null) {
                        p.setNombre(p.getNombre().trim());
                    }
                    productos.add(p);
                }
            }
        }
    }
}
