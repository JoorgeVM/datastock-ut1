package es.iesisidra.datastock.validacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsula el resultado de validar los datos de un producto.
 */
public class ResultadoValidacion {
    private final boolean valido;
    private final List<String> errores;

    public ResultadoValidacion(boolean valido, List<String> errores) {
        this.valido = valido;
        this.errores = errores != null ? new ArrayList<>(errores) : new ArrayList<>();
    }

    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, Collections.emptyList());
    }

    public static ResultadoValidacion error(String error) {
        List<String> lista = new ArrayList<>();
        lista.add(error);
        return new ResultadoValidacion(false, lista);
    }

    public static ResultadoValidacion errores(List<String> errores) {
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }

    public boolean esValido() {
        return valido;
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public String getMensajeErrores() {
        return String.join("; ", errores);
    }
}
