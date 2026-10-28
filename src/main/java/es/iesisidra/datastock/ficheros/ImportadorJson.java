package es.iesisidra.datastock.ficheros;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.validacion.ResultadoValidacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio para importar catálogos de productos en formato JSON usando Gson (RF-FIC-02).
 */
public class ImportadorJson {

    private final Inventario inventario;

    public ImportadorJson(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Importa productos desde el fichero JSON especificado.
     *
     * @param rutaStr Ruta del fichero JSON
     */
    public void importar(String rutaStr) {
        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR JSON =====", ColoresConsola.CIAN));
        if (rutaStr == null || rutaStr.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir una ruta de fichero.", ColoresConsola.ROJO));
            return;
        }

        File fichero = new File(rutaStr.trim());
        System.out.println("Ruta: " + rutaStr.trim());

        if (!fichero.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + fichero.getPath(), ColoresConsola.ROJO));
            return;
        }
        if (fichero.isDirectory()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta corresponde a un directorio, no a un fichero.", ColoresConsola.ROJO));
            return;
        }
        if (!fichero.canRead()) {
            System.out.println(ColoresConsola.pintar("Error: No se tienen permisos de lectura sobre el fichero.", ColoresConsola.ROJO));
            return;
        }

        JsonElement raizElement;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fichero), StandardCharsets.UTF_8)) {
            raizElement = JsonParser.parseReader(reader);
        } catch (JsonSyntaxException e) {
            System.out.println(ColoresConsola.pintar("Error: Fichero JSON mal formado. Se cancela la importación.", ColoresConsola.ROJO));
            return;
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al leer el fichero: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        if (raizElement == null || !raizElement.isJsonObject()) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del JSON debe ser un objeto. Se cancela la importación.", ColoresConsola.ROJO));
            return;
        }

        JsonObject objRaiz = raizElement.getAsJsonObject();
        if (!objRaiz.has("productos") || !objRaiz.get("productos").isJsonArray()) {
            System.out.println(ColoresConsola.pintar("Error: El JSON no contiene el array 'productos'. Se cancela la importación.", ColoresConsola.ROJO));
            return;
        }

        JsonArray arrayProductos = objRaiz.getAsJsonArray("productos");
        int encontrados = arrayProductos.size();
        int importados = 0;
        int rechazados = 0;
        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnFichero = new HashSet<>();
        List<Producto> productosValidos = new ArrayList<>();

        for (int i = 0; i < encontrados; i++) {
            int numProducto = i + 1;
            JsonElement el = arrayProductos.get(i);
            if (!el.isJsonObject()) {
                rechazados++;
                incidencias.add("Producto " + numProducto + ": el elemento no es un objeto JSON válido");
                continue;
            }

            JsonObject pObj = el.getAsJsonObject();
            String codigo = null;
            String nombre = null;
            BigDecimal precio = null;
            Integer stock = null;
            Integer categoriaId = null;
            Integer proveedorId = null;
            List<String> erroresCampos = new ArrayList<>();

            // Campo codigo
            if (pObj.has("codigo") && !pObj.get("codigo").isJsonNull()) {
                codigo = pObj.get("codigo").getAsString();
            } else {
                erroresCampos.add("el código es obligatorio");
            }

            // Campo nombre
            if (pObj.has("nombre") && !pObj.get("nombre").isJsonNull()) {
                nombre = pObj.get("nombre").getAsString();
            } else {
                erroresCampos.add("el nombre es obligatorio");
            }

            // Campo precio
            if (pObj.has("precio") && !pObj.get("precio").isJsonNull()) {
                try {
                    precio = pObj.get("precio").getAsBigDecimal();
                } catch (Exception ex) {
                    erroresCampos.add("el precio debe ser un número decimal válido");
                }
            } else {
                erroresCampos.add("el precio es obligatorio");
            }

            // Campo stock
            if (pObj.has("stock") && !pObj.get("stock").isJsonNull()) {
                try {
                    stock = pObj.get("stock").getAsInt();
                } catch (Exception ex) {
                    erroresCampos.add("el stock debe ser un número entero");
                }
            } else {
                erroresCampos.add("el stock es obligatorio");
            }

            // Campo categoriaId
            if (pObj.has("categoriaId") && !pObj.get("categoriaId").isJsonNull()) {
                try {
                    categoriaId = pObj.get("categoriaId").getAsInt();
                } catch (Exception ex) {
                    erroresCampos.add("categoriaId debe ser un número entero");
                }
            } else {
                erroresCampos.add("categoriaId es obligatorio");
            }

            // Campo proveedorId
            if (pObj.has("proveedorId") && !pObj.get("proveedorId").isJsonNull()) {
                try {
                    proveedorId = pObj.get("proveedorId").getAsInt();
                } catch (Exception ex) {
                    erroresCampos.add("proveedorId debe ser un número entero");
                }
            } else {
                erroresCampos.add("proveedorId es obligatorio");
            }

            String idReferencia = (codigo != null && !codigo.trim().isEmpty()) ? codigo.trim() : "Producto " + numProducto;

            if (!erroresCampos.isEmpty()) {
                rechazados++;
                incidencias.add(idReferencia + ": " + String.join(", ", erroresCampos));
                continue;
            }

            // Crear objeto Producto para validaciones de negocio
            Producto p = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            ResultadoValidacion res = ValidadorProducto.validar(p);
            if (!res.esValido()) {
                rechazados++;
                incidencias.add(idReferencia + ": " + res.getMensajeErrores());
                continue;
            }

            // Comprobar duplicado en el mismo fichero o en memoria
            String codigoNorm = ValidadorProducto.normalizarCodigo(codigo).toUpperCase();
            if (codigosEnFichero.contains(codigoNorm) || inventario.existeCodigo(codigo)) {
                rechazados++;
                incidencias.add(idReferencia + ": código duplicado");
                continue;
            }

            codigosEnFichero.add(codigoNorm);
            p.setCodigo(ValidadorProducto.normalizarCodigo(codigo));
            p.setNombre(nombre.trim());
            productosValidos.add(p);
            importados++;
        }

        // Añadir válidos al inventario
        for (Producto p : productosValidos) {
            inventario.agregarProducto(p);
        }

        // Mostrar resumen
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);
        if (!incidencias.isEmpty()) {
            System.out.println("INCIDENCIAS:");
            for (String inc : incidencias) {
                System.out.println("- " + inc);
            }
            System.out.println(ColoresConsola.pintar("Importación finalizada con incidencias.", ColoresConsola.AMARILLO));
        } else {
            System.out.println(ColoresConsola.pintar("Importación finalizada.", ColoresConsola.VERDE));
        }
    }
}
