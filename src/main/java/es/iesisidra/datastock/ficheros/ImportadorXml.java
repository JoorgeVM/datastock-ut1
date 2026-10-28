package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.validacion.ResultadoValidacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio para importar catálogos de productos en formato XML usando DOM con configuración segura (RF-FIC-03).
 */
public class ImportadorXml {

    private final Inventario inventario;

    public ImportadorXml(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * Importa productos desde el fichero XML especificado.
     *
     * @param rutaStr Ruta del fichero XML
     */
    public void importar(String rutaStr) {
        System.out.println(ColoresConsola.pintar("\n===== IMPORTAR XML =====", ColoresConsola.CIAN));
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

        System.out.println("Leyendo documento XML...");

        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Configuración segura frente a XXE y DTD
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(fichero);
            // Normalizar el árbol DOM para consolidar nodos de texto adyacentes
            doc.getDocumentElement().normalize();
        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doctype")) {
                System.out.println(ColoresConsola.pintar("Error: El fichero contiene DTD o declaraciones no permitidas (rechazado por seguridad XXE). Se cancela la importación.", ColoresConsola.ROJO));
            } else {
                System.out.println(ColoresConsola.pintar("Error: XML mal formado (" + e.getMessage() + "). Se cancela la importación.", ColoresConsola.ROJO));
            }
            return;
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al procesar el documento XML: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        Element raiz = doc.getDocumentElement();
        if (!"catalogo".equalsIgnoreCase(raiz.getTagName())) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del XML debe ser <catalogo>. Raíz encontrada: <" + raiz.getTagName() + ">. Se cancela la importación.", ColoresConsola.ROJO));
            return;
        }

        NodeList nodosProducto = raiz.getElementsByTagName("producto");
        int encontrados = nodosProducto.getLength();
        int importados = 0;
        int rechazados = 0;
        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnFichero = new HashSet<>();
        List<Producto> productosValidos = new ArrayList<>();

        for (int i = 0; i < encontrados; i++) {
            int numProducto = i + 1;
            Node nodo = nodosProducto.item(i);
            if (nodo.getNodeType() != Node.ELEMENT_NODE) {
                rechazados++;
                incidencias.add("Producto " + numProducto + ": nodo inválido");
                continue;
            }

            Element elProd = (Element) nodo;
            String codigo = obtenerTextoEtiqueta(elProd, "codigo");
            String nombre = obtenerTextoEtiqueta(elProd, "nombre");
            String precioStr = obtenerTextoEtiqueta(elProd, "precio");
            String stockStr = obtenerTextoEtiqueta(elProd, "stock");
            String catStr = obtenerTextoEtiqueta(elProd, "categoriaId");
            String provStr = obtenerTextoEtiqueta(elProd, "proveedorId");

            List<String> erroresCampos = new ArrayList<>();
            BigDecimal precio = null;
            Integer stock = null;
            Integer categoriaId = null;
            Integer proveedorId = null;

            if (codigo == null || codigo.trim().isEmpty()) {
                erroresCampos.add("el código es obligatorio");
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                erroresCampos.add("el nombre es obligatorio");
            }

            if (precioStr == null || precioStr.trim().isEmpty()) {
                erroresCampos.add("el precio es obligatorio");
            } else {
                try {
                    precio = new BigDecimal(precioStr.trim());
                } catch (Exception ex) {
                    erroresCampos.add("el precio debe ser un número decimal válido");
                }
            }

            if (stockStr == null || stockStr.trim().isEmpty()) {
                erroresCampos.add("el stock es obligatorio");
            } else {
                try {
                    stock = Integer.parseInt(stockStr.trim());
                } catch (Exception ex) {
                    erroresCampos.add("el stock debe ser un número entero");
                }
            }

            if (catStr == null || catStr.trim().isEmpty()) {
                erroresCampos.add("categoriaId es obligatorio");
            } else {
                try {
                    categoriaId = Integer.parseInt(catStr.trim());
                } catch (Exception ex) {
                    erroresCampos.add("categoriaId debe ser un número entero");
                }
            }

            if (provStr == null || provStr.trim().isEmpty()) {
                erroresCampos.add("proveedorId es obligatorio");
            } else {
                try {
                    proveedorId = Integer.parseInt(provStr.trim());
                } catch (Exception ex) {
                    erroresCampos.add("proveedorId debe ser un número entero");
                }
            }

            String idReferencia = (codigo != null && !codigo.trim().isEmpty()) ? codigo.trim() : "Producto " + numProducto;

            if (!erroresCampos.isEmpty()) {
                rechazados++;
                incidencias.add(idReferencia + ": " + String.join(", ", erroresCampos));
                continue;
            }

            Producto p = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            ResultadoValidacion res = ValidadorProducto.validar(p);
            if (!res.esValido()) {
                rechazados++;
                incidencias.add(idReferencia + ": " + res.getMensajeErrores());
                continue;
            }

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

        // Añadir válidos
        for (Producto p : productosValidos) {
            inventario.agregarProducto(p);
        }

        // Mostrar resumen
        System.out.println("Encontrados: " + encontrados + " | Importados: " + importados + " | Rechazados: " + rechazados);
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

    private String obtenerTextoEtiqueta(Element padre, String etiqueta) {
        NodeList nl = padre.getElementsByTagName(etiqueta);
        if (nl != null && nl.getLength() > 0) {
            Node n = nl.item(0);
            return n.getTextContent();
        }
        return null;
    }
}
