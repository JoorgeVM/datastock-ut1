package es.iesisidra.datastock.validacion;

import es.iesisidra.datastock.modelo.Producto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validador de las reglas de negocio de un Producto según la especificación de UT1.
 */
public final class ValidadorProducto {

    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Za-z0-9-]+$");

    private ValidadorProducto() { }

    /**
     * Valida los campos de un objeto Producto.
     *
     * @param p Producto a validar
     * @return ResultadoValidacion con el estado y los errores encontrados
     */
    public static ResultadoValidacion validar(Producto p) {
        if (p == null) {
            return ResultadoValidacion.error("El producto no puede ser nulo.");
        }

        List<String> errores = new ArrayList<>();

        // 1. Código
        String codigo = p.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            errores.add("el código es obligatorio");
        } else {
            String codigoLimpio = codigo.trim();
            if (codigoLimpio.length() > 12) {
                errores.add("el código excede el máximo de 12 caracteres");
            }
            if (!PATRON_CODIGO.matcher(codigoLimpio).matches()) {
                errores.add("el código no cumple el patrón [A-Z0-9-]+");
            }
        }

        // 2. Nombre
        String nombre = p.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            errores.add("el nombre es obligatorio");
        } else {
            String nombreLimpio = nombre.trim();
            if (nombreLimpio.length() < 3 || nombreLimpio.length() > 100) {
                errores.add("el nombre debe tener entre 3 y 100 caracteres");
            }
        }

        // 3. Precio
        BigDecimal precio = p.getPrecio();
        if (precio == null) {
            errores.add("el precio es obligatorio");
        } else if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("el precio debe ser mayor que 0");
        }

        // 4. Stock
        if (p.getStock() < 0) {
            errores.add("el stock no puede ser negativo");
        }

        // 5. CategoriaId (entero positivo en UT1; en UT2 se verificará la FK en MySQL)
        if (p.getCategoriaId() <= 0) {
            errores.add("el identificador de categoría debe ser un entero positivo");
        }

        // 6. ProveedorId (entero positivo en UT1; en UT2 se verificará la FK en MySQL)
        if (p.getProveedorId() <= 0) {
            errores.add("el identificador de proveedor debe ser un entero positivo");
        }

        return ResultadoValidacion.errores(errores);
    }

    /**
     * Limpia y normaliza el código (elimina espacios extremos).
     */
    public static String normalizarCodigo(String codigo) {
        return codigo != null ? codigo.trim() : "";
    }
}
