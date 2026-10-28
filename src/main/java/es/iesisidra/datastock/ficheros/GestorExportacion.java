package es.iesisidra.datastock.ficheros;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servicio para exportar el catálogo de productos a JSON y XML (RF-FIC-05).
 */
public class GestorExportacion {

    private final Inventario inventario;

    public GestorExportacion(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Exporta los productos en memoria a un fichero JSON solicitando confirmación si existe.
     */
    public void exportarJson(String rutaStr) {
        exportarJson(rutaStr, false);
    }

    /**
     * Exporta los productos a JSON permitiendo forzar sobrescritura.
     */
    public void exportarJson(String rutaStr, boolean forzarSobrescritura) {
        System.out.println(ColoresConsola.pintar("\n===== EXPORTAR A JSON =====", ColoresConsola.CIAN));
        if (!validarInventario()) return;
        File ficheroDestino = validarRutaYConfirmar(rutaStr, forzarSobrescritura);
        if (ficheroDestino == null) return;

        List<Producto> productos = inventario.getProductosOrdenadosPorCodigo();
        JsonObject raiz = new JsonObject();
        JsonArray arrayProds = new JsonArray();

        for (Producto p : productos) {
            JsonObject obj = new JsonObject();
            obj.addProperty("codigo", p.getCodigo());
            obj.addProperty("nombre", p.getNombre());
            obj.addProperty("precio", p.getPrecio());
            obj.addProperty("stock", p.getStock());
            obj.addProperty("categoriaId", p.getCategoriaId());
            obj.addProperty("proveedorId", p.getProveedorId());
            arrayProds.add(obj);
        }
        raiz.add("productos", arrayProds);

        // Configuración de Gson con formato legible e indentado
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ficheroDestino), StandardCharsets.UTF_8)) {
            gson.toJson(raiz, writer);
            System.out.println(ColoresConsola.pintar("Fichero generado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + ficheroDestino.getPath());
            System.out.println("Formato: JSON");
            System.out.println("Productos exportados: " + productos.size());
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al escribir el fichero JSON: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    /**
     * Exporta los productos en memoria a un fichero XML solicitando confirmación si existe.
     */
    public void exportarXml(String rutaStr) {
        exportarXml(rutaStr, false);
    }

    /**
     * Exporta los productos a XML permitiendo forzar sobrescritura.
     */
    public void exportarXml(String rutaStr, boolean forzarSobrescritura) {
        System.out.println(ColoresConsola.pintar("\n===== EXPORTAR A XML =====", ColoresConsola.CIAN));
        if (!validarInventario()) return;
        File ficheroDestino = validarRutaYConfirmar(rutaStr, forzarSobrescritura);
        if (ficheroDestino == null) return;

        List<Producto> productos = inventario.getProductosOrdenadosPorCodigo();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element raiz = doc.createElement("catalogo");
            doc.appendChild(raiz);

            for (Producto p : productos) {
                Element elProd = doc.createElement("producto");

                agregarHijoTexto(doc, elProd, "codigo", p.getCodigo());
                agregarHijoTexto(doc, elProd, "nombre", p.getNombre());
                agregarHijoTexto(doc, elProd, "precio", p.getPrecio().toPlainString());
                agregarHijoTexto(doc, elProd, "stock", String.valueOf(p.getStock()));
                agregarHijoTexto(doc, elProd, "categoriaId", String.valueOf(p.getCategoriaId()));
                agregarHijoTexto(doc, elProd, "proveedorId", String.valueOf(p.getProveedorId()));

                raiz.appendChild(elProd);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ficheroDestino), StandardCharsets.UTF_8)) {
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
            }

            System.out.println(ColoresConsola.pintar("Fichero generado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + ficheroDestino.getPath());
            System.out.println("Formato: XML");
            System.out.println("Productos exportados: " + productos.size());

        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al exportar a XML: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private void agregarHijoTexto(Document doc, Element padre, String etiqueta, String valor) {
        Element el = doc.createElement(etiqueta);
        el.setTextContent(valor != null ? valor : "");
        padre.appendChild(el);
    }

    private boolean validarInventario() {
        if (inventario.estaVacio()) {
            System.out.println(ColoresConsola.pintar("Error: El inventario está vacío. No hay productos para exportar.", ColoresConsola.AMARILLO));
            return false;
        }
        return true;
    }

    private File validarRutaYConfirmar(String rutaStr, boolean forzarSobrescritura) {
        if (rutaStr == null || rutaStr.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Debes introducir una ruta de destino.", ColoresConsola.ROJO));
            return null;
        }

        File fichero = new File(rutaStr.trim());
        File dirPadre = fichero.getParentFile();
        if (dirPadre != null && !dirPadre.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El directorio de destino no existe: " + dirPadre.getPath(), ColoresConsola.ROJO));
            return null;
        }

        if (fichero.exists() && !forzarSobrescritura) {
            boolean continuar = LectorConsola.confirmar("El fichero ya existe en destino. ¿Deseas sobrescribirlo?");
            if (!continuar) {
                System.out.println(ColoresConsola.pintar("Exportación cancelada. El fichero existente se mantiene intacto.", ColoresConsola.AMARILLO));
                return null;
            }
        }

        return fichero;
    }
}
