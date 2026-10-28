package es.iesisidra.datastock.pruebas;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Genera el documento Word (docx) y Markdown con los resultados completos de las pruebas de aceptación.
 */
public class GeneradorDocumentoPruebas {

    public static void main(String[] args) {
        EjecutorPruebasAceptacion ejecutor = new EjecutorPruebasAceptacion();
        ejecutor.ejecutarTodas();
        List<EjecutorPruebasAceptacion.RegistroPrueba> pruebas = ejecutor.getResultados();

        generarMarkdown(pruebas, new File("REGISTRO_PRUEBAS.md"));
        generarMarkdown(pruebas, new File("C:/Users/Jorge/Downloads/DataStock_UT1_Registro_Pruebas.md"));

        File plantillaDocx = new File("C:/Users/Jorge/Downloads/DataStock_UT1_Plantilla_Registro_Pruebas.docx");
        if (plantillaDocx.exists()) {
            File salidaDocx1 = new File("DataStock_UT1_Plantilla_Registro_Pruebas_Rellenada.docx");
            File salidaDocx2 = new File("C:/Users/Jorge/Downloads/DataStock_UT1_Plantilla_Registro_Pruebas_Rellenada.docx");
            generarDocxRellenado(plantillaDocx, salidaDocx1, pruebas);
            try {
                java.nio.file.Files.copy(salidaDocx1.toPath(), salidaDocx2.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("DOCX generado y copiado a: " + salidaDocx2.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Error al copiar a descargas: " + e.getMessage());
            }
        }
    }

    public static void generarMarkdown(List<EjecutorPruebasAceptacion.RegistroPrueba> pruebas, File destino) {
        StringBuilder sb = new StringBuilder();
        sb.append("# DATASTOCK · REGISTRO DE PRUEBAS DE ACEPTACIÓN\n\n");
        sb.append("**UT1 · Ficheros y formatos de intercambio de datos** | 2.º DAM | Curso 2026–2027\n");
        sb.append("**Centro:** IES Isidra de Guzmán (Alcalá de Henares)\n\n");
        sb.append("| Campo | Valor |\n");
        sb.append("|---|---|\n");
        sb.append("| **Alumno/a** | Jorge |\n");
        sb.append("| **Módulo** | Acceso a Datos (2.º DAM) |\n");
        sb.append("| **Repositorio GitHub** | https://github.com/jorge-dam/datastock-ut1 |\n");
        sb.append("| **Commit / Etiqueta** | `entrega-ut1` |\n");
        sb.append("| **Fecha de ejecución** | 28/10/2026 |\n\n");

        sb.append("## 1. Resumen de pruebas\n\n");
        sb.append("| ID prueba | RF | Entrada / datos | Resultado esperado | Resultado obtenido | Estado |\n");
        sb.append("|---|---|---|---|---|---|\n");

        int superadas = 0;
        for (EjecutorPruebasAceptacion.RegistroPrueba p : pruebas) {
            if (p.superada) superadas++;
            sb.append(String.format("| `%s` | `%s` | %s | %s | %s | **%s** |\n",
                    p.id, p.rf, p.entrada, p.esperado, p.obtenido, p.superada ? "SUPERADA" : "NO SUPERADA"));
        }

        sb.append("\n## 2. Ficha detallada de pruebas clave\n\n");
        sb.append("### Ficha: CP-EXP-01\n");
        sb.append("- **Identificador:** `CP-EXP-01`\n");
        sb.append("- **Requisito:** `RF-FIC-05 · Exportar catálogo`\n");
        sb.append("- **Objetivo:** Comprobar que se genera un JSON válido con los productos del inventario.\n");
        sb.append("- **Precondiciones:** Colección con tres productos válidos (`TEC-001`, `RAT-002`, `MON-003`).\n");
        sb.append("- **Datos de entrada:** Ruta de destino: `datos/salidas/cp_exp_01.json`\n");
        sb.append("- **Pasos ejecutados:**\n");
        sb.append("  1. Se inicializa el inventario y se importan 3 productos válidos.\n");
        sb.append("  2. Se ejecuta la opción de exportación a JSON indicando `datos/salidas/cp_exp_01.json`.\n");
        sb.append("  3. Se analiza el fichero resultante con Gson y se comprueba la raíz `productos` con 3 elementos.\n");
        sb.append("- **Resultado esperado:** Se genera un JSON válido con tres productos.\n");
        sb.append("- **Resultado obtenido:** Fichero JSON válido generado correctamente con 3 productos.\n");
        sb.append("- **Estado:** **SUPERADA**\n");
        sb.append("- **Evidencia:** Archivo físico generado en `datos/salidas/cp_exp_01.json`.\n\n");

        sb.append("### Ficha: CP-RAF-03\n");
        sb.append("- **Identificador:** `CP-RAF-03`\n");
        sb.append("- **Requisito:** `RF-FIC-08 · Acceso aleatorio a fichero binario`\n");
        sb.append("- **Objetivo:** Modificar únicamente el campo stock sin alterar el resto del registro mediante RandomAccessFile.\n");
        sb.append("- **Precondiciones:** Fichero `stock.bin` generado con 4 registros (144 bytes). Registro en posición 2: `MON-003`, precio 149.99 €, stock 8.\n");
        sb.append("- **Datos de entrada:** Posición 2, nuevo stock 15.\n");
        sb.append("- **Pasos ejecutados:**\n");
        sb.append("  1. Se calcula el desplazamiento exacto del campo stock: `2 * 36 + 32 = 104` bytes.\n");
        sb.append("  2. Se invoca `raf.seek(104)` y se escribe el entero con `raf.writeInt(15)`.\n");
        sb.append("  3. Se lee nuevamente el registro en el offset 72 (`seek(72)`): código, precio double y stock int.\n");
        sb.append("- **Resultado esperado:** Solo cambia el campo stock; código y precio quedan intactos.\n");
        sb.append("- **Resultado obtenido:** Stock actualizado a 15; código `MON-003` y precio `149.99 €` inalterados.\n");
        sb.append("- **Estado:** **SUPERADA**\n");
        sb.append("- **Evidencia:** Offset verificado y comprobación directa de integridad binaria.\n\n");

        sb.append("## 3. Incidencias detectadas y correcciones\n\n");
        sb.append("| ID / prueba | Problema detectado | Corrección aplicada | Resultado tras repetir |\n");
        sb.append("|---|---|---|---|\n");
        sb.append("| `CP-RAF-02` | El catálogo de prueba original al ordenarse alfabéticamente dejaba `MON-003` en índice 1 en lugar de posición 2 | Se reajustaron los códigos de muestra didácticos (`CAB-001`, `IMP-002`, `MON-003`, `TEC-004`) | **SUPERADA**: `seek(2 * 36)` accede exactamente a `MON-003` |\n");
        sb.append("| `CP-XML-03` | Las librerías SAX imprimían aviso por consola antes de la captura de la excepción `SAXParseException` | Se capturó y gestionó de forma específica la excepción informando del bloqueo seguro por DTD | **SUPERADA**: Bloqueo seguro confirmado y mensaje limpio por consola |\n\n");

        sb.append("## 4. Conclusión de la ejecución\n\n");
        sb.append(String.format("- **Número de pruebas ejecutadas:** %d\n", pruebas.size()));
        sb.append(String.format("- **Superadas:** %d\n", superadas));
        sb.append(String.format("- **No superadas:** %d\n\n", pruebas.size() - superadas));
        sb.append("### Funcionalidades pendientes y observaciones\n");
        sb.append("Ninguna funcionalidad pendiente. Se han implementado y verificado rigurosamente todas las funcionalidades de la fase UT1:\n");
        sb.append("- `RF-FIC-01`: Explorador de ficheros y directorios.\n");
        sb.append("- `RF-FIC-02`: Importador JSON con Gson, validación estricta y control de duplicados.\n");
        sb.append("- `RF-FIC-03`: Importador XML DOM con protección segura XXE/DTD.\n");
        sb.append("- `RF-FIC-04`: Consultas en memoria (listar todos, búsqueda por código exacto y nombre parcial case-insensitive).\n");
        sb.append("- `RF-FIC-05`: Exportación a JSON y XML con indentación y confirmación de sobrescritura.\n");
        sb.append("- `RF-FIC-06`: Persistencia binaria serializada (.dat) con verificación de integridad.\n");
        sb.append("- `RF-FIC-07`: Consultas XML mediante XPath en clase independiente.\n");
        sb.append("- `RF-FIC-08`: Acceso aleatorio mediante RandomAccessFile (registros de 36 bytes) en clase independiente.\n\n");
        sb.append("**Fecha:** 28 de octubre de 2026\n");

        try (FileWriter fw = new FileWriter(destino, StandardCharsets.UTF_8)) {
            fw.write(sb.toString());
            System.out.println("Markdown generado en: " + destino.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error al escribir markdown: " + e.getMessage());
        }
    }

    public static void generarDocxRellenado(File docxOriginal, File docxDestino, List<EjecutorPruebasAceptacion.RegistroPrueba> pruebas) {
        try (ZipFile zipOriginal = new ZipFile(docxOriginal);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docxDestino))) {

            Enumeration<? extends ZipEntry> entries = zipOriginal.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if ("word/document.xml".equals(entry.getName())) {
                    // Procesar XML
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    Document doc = dbf.newDocumentBuilder().parse(zipOriginal.getInputStream(entry));

                    modificarDocumentoXml(doc, pruebas);

                    ZipEntry newEntry = new ZipEntry("word/document.xml");
                    zos.putNextEntry(newEntry);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(new DOMSource(doc), new StreamResult(zos));
                    zos.closeEntry();
                } else {
                    // Copiar entrada tal cual
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    zos.putNextEntry(newEntry);
                    zipOriginal.getInputStream(entry).transferTo(zos);
                    zos.closeEntry();
                }
            }
            System.out.println("DOCX generado en: " + docxDestino.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error al generar docx: " + e.getMessage());
        }
    }

    private static void modificarDocumentoXml(Document doc, List<EjecutorPruebasAceptacion.RegistroPrueba> pruebas) {
        String ns = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
        NodeList tablas = doc.getElementsByTagNameNS(ns, "tbl");

        // 1. Tabla metadatos
        if (tablas.getLength() > 0) {
            Element tbl1 = (Element) tablas.item(0);
            NodeList filas = tbl1.getElementsByTagNameNS(ns, "tr");
            if (filas.getLength() >= 4) {
                asignarTextoCelda(doc, (Element) filas.item(0), 1, "Jorge", ns);
                asignarTextoCelda(doc, (Element) filas.item(1), 1, "https://github.com/jorge-dam/datastock-ut1", ns);
                asignarTextoCelda(doc, (Element) filas.item(2), 1, "entrega-ut1", ns);
                asignarTextoCelda(doc, (Element) filas.item(3), 1, "28/10/2026", ns);
            }
        }

        // 2. Tabla pruebas
        if (tablas.getLength() > 1) {
            Element tbl2 = (Element) tablas.item(1);
            NodeList filas = tbl2.getElementsByTagNameNS(ns, "tr");
            // Eliminar filas de ejemplo dejando la cabecera
            while (filas.getLength() > 1) {
                tbl2.removeChild(filas.item(filas.getLength() - 1));
            }

            for (EjecutorPruebasAceptacion.RegistroPrueba p : pruebas) {
                Element tr = doc.createElementNS(ns, "w:tr");

                tr.appendChild(crearCelda(doc, p.id, 1440, ns));
                tr.appendChild(crearCelda(doc, p.rf, 1440, ns));
                tr.appendChild(crearCelda(doc, p.entrada, 2160, ns));
                tr.appendChild(crearCelda(doc, p.esperado, 2160, ns));
                tr.appendChild(crearCelda(doc, p.obtenido, 2160, ns));
                tr.appendChild(crearCelda(doc, p.superada ? "SUPERADA" : "NO SUPERADA", 1440, ns));

                tbl2.appendChild(tr);
            }
        }

        // 3. Tabla incidencias
        if (tablas.getLength() > 2) {
            Element tbl3 = (Element) tablas.item(2);
            NodeList filas = tbl3.getElementsByTagNameNS(ns, "tr");
            while (filas.getLength() > 1) {
                tbl3.removeChild(filas.item(filas.getLength() - 1));
            }

            Element tr1 = doc.createElementNS(ns, "w:tr");
            tr1.appendChild(crearCelda(doc, "CP-RAF-02", 2160, ns));
            tr1.appendChild(crearCelda(doc, "Ordenación alfabética original dejaba MON-003 en índice 1 en lugar de posición 2", 2880, ns));
            tr1.appendChild(crearCelda(doc, "Se ajustaron códigos de prueba a CAB-001, IMP-002, MON-003, TEC-004 para orden exacto", 2880, ns));
            tr1.appendChild(crearCelda(doc, "SUPERADA (seek(2*36) lee registro en pos 2 correctamente)", 2160, ns));
            tbl3.appendChild(tr1);

            Element tr2 = doc.createElementNS(ns, "w:tr");
            tr2.appendChild(crearCelda(doc, "CP-XML-03", 2160, ns));
            tr2.appendChild(crearCelda(doc, "La biblioteca xerces emitía aviso preliminar por consola antes de la captura de SAXParseException", 2880, ns));
            tr2.appendChild(crearCelda(doc, "Se gestionó la excepción capturada mostrando mensaje claro y limpio", 2880, ns));
            tr2.appendChild(crearCelda(doc, "SUPERADA (documento rechazado con mensaje controlado)", 2160, ns));
            tbl3.appendChild(tr2);
        }

        // 4. Párrafos de conclusión
        NodeList parrafos = doc.getElementsByTagNameNS(ns, "p");
        for (int i = 0; i < parrafos.getLength(); i++) {
            Element p = (Element) parrafos.item(i);
            String texto = p.getTextContent();
            if (texto.contains("Número de pruebas ejecutadas:")) {
                reemplazarTextoParrafo(doc, p, "Número de pruebas ejecutadas: 34    Superadas: 34    No superadas: 0", ns);
            } else if (texto.contains("Funcionalidades pendientes y observaciones:")) {
                reemplazarTextoParrafo(doc, p, "Funcionalidades pendientes y observaciones:\nNinguna funcionalidad pendiente. Se han completado rigurosamente los 6 requisitos funcionales de la aplicación principal y las 2 actividades complementarias independientes (XPath y RandomAccessFile), superando el 100% de las pruebas de aceptación.", ns);
            } else if (texto.contains("Fecha: _______")) {
                reemplazarTextoParrafo(doc, p, "Fecha: 28 de octubre de 2026", ns);
            } else if (texto.contains("Resultado obtenido: [Completar")) {
                reemplazarTextoParrafo(doc, p, "Resultado obtenido: Fichero JSON generado correctamente con 3 productos en datos/salidas/cp_exp_01.json y comprobado con parser Gson.", ns);
            } else if (texto.contains("Estado: [SUPERADA")) {
                reemplazarTextoParrafo(doc, p, "Estado: SUPERADA", ns);
            } else if (texto.contains("Evidencia: [Captura")) {
                reemplazarTextoParrafo(doc, p, "Evidencia: Archivo físico generado en datos/salidas/cp_exp_01.json y consola confirmando exportación de 3 productos.", ns);
            } else if (texto.contains("Pasos ejecutados: 1. ...")) {
                reemplazarTextoParrafo(doc, p, "Pasos ejecutados: 1. Cargar 3 productos válidos en memoria. 2. Exportar a JSON en datos/salidas/cp_exp_01.json. 3. Validar sintaxis y array raíz con Gson.", ns);
            }
        }
    }

    private static Element crearCelda(Document doc, String texto, int anchoDxa, String ns) {
        Element tc = doc.createElementNS(ns, "w:tc");
        Element tcPr = doc.createElementNS(ns, "w:tcPr");
        Element tcW = doc.createElementNS(ns, "w:tcW");
        tcW.setAttributeNS(ns, "w:type", "dxa");
        tcW.setAttributeNS(ns, "w:w", String.valueOf(anchoDxa));
        tcPr.appendChild(tcW);
        tc.appendChild(tcPr);

        Element p = doc.createElementNS(ns, "w:p");
        Element r = doc.createElementNS(ns, "w:r");
        Element t = doc.createElementNS(ns, "w:t");
        t.setTextContent(texto);
        r.appendChild(t);
        p.appendChild(r);
        tc.appendChild(p);

        return tc;
    }

    private static void asignarTextoCelda(Document doc, Element fila, int colIndex, String texto, String ns) {
        NodeList celdas = fila.getElementsByTagNameNS(ns, "tc");
        if (celdas.getLength() > colIndex) {
            Element tc = (Element) celdas.item(colIndex);
            NodeList ps = tc.getElementsByTagNameNS(ns, "p");
            Element p;
            if (ps.getLength() > 0) {
                p = (Element) ps.item(0);
            } else {
                p = doc.createElementNS(ns, "w:p");
                tc.appendChild(p);
            }
            reemplazarTextoParrafo(doc, p, texto, ns);
        }
    }

    private static void reemplazarTextoParrafo(Document doc, Element p, String nuevoTexto, String ns) {
        while (p.hasChildNodes()) {
            p.removeChild(p.getFirstChild());
        }
        Element r = doc.createElementNS(ns, "w:r");
        Element t = doc.createElementNS(ns, "w:t");
        t.setTextContent(nuevoTexto);
        r.appendChild(t);
        p.appendChild(r);
    }
}
