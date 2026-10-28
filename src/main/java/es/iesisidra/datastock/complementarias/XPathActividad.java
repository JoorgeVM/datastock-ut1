package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * Punto de entrada para RF-FIC-07: Consultas XML mediante XPath.
 * <p>Actividad técnica complementaria e independiente del menú principal.</p>
 */
public final class XPathActividad {

    private XPathActividad() { }

    public static void main(String[] args) {
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("       RF-FIC-07 · CONSULTAS XML CON XPATH", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));

        String ruta = LectorConsola.leerTexto("Introduce la ruta del fichero XML: ");
        if (ruta == null || ruta.trim().isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: Ruta vacía.", ColoresConsola.ROJO));
            return;
        }

        File fichero = new File(ruta.trim());
        if (!fichero.exists()) {
            System.out.println(ColoresConsola.pintar("Error: El fichero no existe: " + fichero.getPath(), ColoresConsola.ROJO));
            return;
        }
        if (fichero.isDirectory()) {
            System.out.println(ColoresConsola.pintar("Error: La ruta corresponde a un directorio.", ColoresConsola.ROJO));
            return;
        }

        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Protección contra DTD y XXE
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
            doc.getDocumentElement().normalize();
        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doctype")) {
                System.out.println(ColoresConsola.pintar("Error: Fichero rechazado por contener DTD o entidades externas no permitidas.", ColoresConsola.ROJO));
            } else {
                System.out.println(ColoresConsola.pintar("Error: Documento XML mal formado (" + e.getMessage() + ").", ColoresConsola.ROJO));
            }
            return;
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al procesar el XML: " + e.getMessage(), ColoresConsola.ROJO));
            return;
        }

        Element raiz = doc.getDocumentElement();
        if (!"catalogo".equalsIgnoreCase(raiz.getTagName())) {
            System.out.println(ColoresConsola.pintar("Error: La raíz del documento debe ser <catalogo>. Encontrada: <" + raiz.getTagName() + ">", ColoresConsola.ROJO));
            return;
        }

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        System.out.println(ColoresConsola.pintar("\nFichero cargado correctamente: " + fichero.getName(), ColoresConsola.VERDE));

        int opcion;
        do {
            System.out.println("\n===== CONSULTAS XPATH =====");
            System.out.println("Fichero: " + fichero.getPath());
            System.out.println("1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);

            switch (opcion) {
                case 1 -> consultarPorCodigo(doc, xpath);
                case 2 -> consultarPorCategoria(doc, xpath);
                case 3 -> consultarPorPrecioSuperior(doc, xpath);
                case 4 -> contarProductos(doc, xpath);
                case 0 -> System.out.println(ColoresConsola.pintar("Fin de consultas XPath.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private static void consultarPorCodigo(Document doc, XPath xpath) {
        String codigoBuscado = LectorConsola.leerTexto("Introduce el código del producto: ").trim();
        if (codigoBuscado.isEmpty()) {
            System.out.println(ColoresConsola.pintar("Error: El código no puede estar vacío.", ColoresConsola.ROJO));
            return;
        }

        try {
            // Se seleccionan los nodos y se compara en Java para evitar concatenación no segura
            NodeList lista = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);
            boolean encontrado = false;
            for (int i = 0; i < lista.getLength(); i++) {
                Node nodo = lista.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nodo;
                    String codigo = obtenerTextoEtiqueta(el, "codigo");
                    if (codigo != null && codigo.trim().equalsIgnoreCase(codigoBuscado)) {
                        imprimirProducto(el);
                        encontrado = true;
                        break;
                    }
                }
            }
            if (!encontrado) {
                System.out.println(ColoresConsola.pintar("No se encontró ningún producto con el código: " + codigoBuscado, ColoresConsola.AMARILLO));
            }
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error en la consulta XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static void consultarPorCategoria(Document doc, XPath xpath) {
        int categoriaId = LectorConsola.leerEntero("Introduce el ID de categoría: ");
        if (categoriaId <= 0) {
            System.out.println(ColoresConsola.pintar("Error: El ID de categoría debe ser un entero positivo.", ColoresConsola.AMARILLO));
            return;
        }

        try {
            String expresion = "/catalogo/producto[categoriaId=" + categoriaId + "]";
            NodeList lista = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
            if (lista.getLength() == 0) {
                System.out.println(ColoresConsola.pintar("No se encontraron productos para la categoría " + categoriaId, ColoresConsola.AMARILLO));
            } else {
                for (int i = 0; i < lista.getLength(); i++) {
                    imprimirProducto((Element) lista.item(i));
                }
                System.out.println("Productos encontrados: " + lista.getLength());
            }
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error en la consulta XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static void consultarPorPrecioSuperior(Document doc, XPath xpath) {
        String precioStr = LectorConsola.leerTexto("Precio mínimo: ").trim();
        double precioMin;
        try {
            precioMin = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            System.out.println(ColoresConsola.pintar("Error: Introduce un valor numérico válido para el precio.", ColoresConsola.ROJO));
            return;
        }

        try {
            String expresion = "/catalogo/producto[precio > " + precioMin + "]";
            NodeList lista = (NodeList) xpath.evaluate(expresion, doc, XPathConstants.NODESET);
            if (lista.getLength() == 0) {
                System.out.println(ColoresConsola.pintar("No se encontraron productos con precio superior a " + precioMin, ColoresConsola.AMARILLO));
            } else {
                for (int i = 0; i < lista.getLength(); i++) {
                    imprimirProducto((Element) lista.item(i));
                }
                System.out.println("Productos encontrados: " + lista.getLength());
            }
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error en la consulta XPath: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static void contarProductos(Document doc, XPath xpath) {
        try {
            Double total = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
            System.out.println("Resultado: " + total.intValue());
        } catch (Exception e) {
            System.out.println(ColoresConsola.pintar("Error al contar productos: " + e.getMessage(), ColoresConsola.ROJO));
        }
    }

    private static void imprimirProducto(Element el) {
        String codigo = obtenerTextoEtiqueta(el, "codigo");
        String nombre = obtenerTextoEtiqueta(el, "nombre");
        String precio = obtenerTextoEtiqueta(el, "precio");
        System.out.println(codigo + " " + nombre + " " + precio + " €");
    }

    private static String obtenerTextoEtiqueta(Element padre, String etiqueta) {
        NodeList nl = padre.getElementsByTagName(etiqueta);
        if (nl != null && nl.getLength() > 0) {
            return nl.item(0).getTextContent();
        }
        return "";
    }
}
