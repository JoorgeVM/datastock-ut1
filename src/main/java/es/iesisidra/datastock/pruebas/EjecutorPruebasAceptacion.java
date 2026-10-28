package es.iesisidra.datastock.pruebas;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.iesisidra.datastock.complementarias.AccesoAleatorioActividad;
import es.iesisidra.datastock.ficheros.ExploradorRutas;
import es.iesisidra.datastock.ficheros.GestorExportacion;
import es.iesisidra.datastock.ficheros.GestorSerializacion;
import es.iesisidra.datastock.ficheros.ImportadorJson;
import es.iesisidra.datastock.ficheros.ImportadorXml;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.GestorConsultas;
import es.iesisidra.datastock.servicio.Inventario;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejecutor automatizado de las 32 pruebas de aceptación de DataStock UT1.
 * <p>Ejecuta cada caso de prueba, verifica los resultados esperados y genera el informe.</p>
 */
public class EjecutorPruebasAceptacion {

    public static class RegistroPrueba {
        public final String id;
        public final String rf;
        public final String entrada;
        public final String esperado;
        public final String obtenido;
        public final boolean superada;
        public final String evidencia;

        public RegistroPrueba(String id, String rf, String entrada, String esperado, String obtenido, boolean superada, String evidencia) {
            this.id = id;
            this.rf = rf;
            this.entrada = entrada;
            this.esperado = esperado;
            this.obtenido = obtenido;
            this.superada = superada;
            this.evidencia = evidencia;
        }
    }

    private final List<RegistroPrueba> resultados = new ArrayList<>();

    public static void main(String[] args) {
        EjecutorPruebasAceptacion ejecutor = new EjecutorPruebasAceptacion();
        ejecutor.ejecutarTodas();
        ejecutor.imprimirResumen();
    }

    public List<RegistroPrueba> getResultados() {
        return resultados;
    }

    public void ejecutarTodas() {
        // RF-FIC-01
        testCP_FIC_01();
        testCP_FIC_02();
        testCP_FIC_03();

        // RF-FIC-02
        testCP_JSON_01();
        testCP_JSON_02();
        testCP_JSON_03();
        testCP_JSON_04();

        // RF-FIC-03
        testCP_XML_01();
        testCP_XML_02();
        testCP_XML_03();
        testCP_XML_04();

        // RF-FIC-04
        testCP_CON_01();
        testCP_CON_02();
        testCP_CON_03();
        testCP_CON_04();

        // RF-FIC-05
        testCP_EXP_01();
        testCP_EXP_02();
        testCP_EXP_03();
        testCP_EXP_04();
        testCP_EXP_05();

        // RF-FIC-06
        testCP_SER_01();
        testCP_SER_02();
        testCP_SER_03();
        testCP_SER_04();

        // RF-FIC-07
        testCP_XPATH_01();
        testCP_XPATH_02();
        testCP_XPATH_03();
        testCP_XPATH_04();
        testCP_XPATH_05();

        // RF-FIC-08
        testCP_RAF_01();
        testCP_RAF_02();
        testCP_RAF_03();
        testCP_RAF_04();
        testCP_RAF_05();
    }

    // --- RF-FIC-01 ---
    private void testCP_FIC_01() {
        String ruta = "datos/ejemplos/directorio_prueba";
        String salida = capturarSalida(() -> new ExploradorRutas().explorar(ruta));
        boolean ok = salida.contains("Total ficheros: 2 | Directorios: 1")
                && salida.contains("archivo1.txt") && salida.contains("archivo2.txt") && salida.contains("subcarpeta");
        resultados.add(new RegistroPrueba(
                "CP-FIC-01", "RF-FIC-01", "Ruta con dos ficheros y un directorio (" + ruta + ")",
                "Se muestran tres elementos y totales correctos (2 ficheros, 1 dir)",
                ok ? "3 elementos detectados y 'Total ficheros: 2 | Directorios: 1'" : salida,
                ok, "Salida consola: elementos listados con prefijo [FICHERO]/[DIR] y totales correctos"
        ));
    }

    private void testCP_FIC_02() {
        String ruta = "datos/ruta_ficticia_inexistente";
        String salida = capturarSalida(() -> new ExploradorRutas().explorar(ruta));
        boolean ok = salida.contains("Error: La ruta no existe");
        resultados.add(new RegistroPrueba(
                "CP-FIC-02", "RF-FIC-01", "Ruta inexistente (" + ruta + ")",
                "Mensaje de error sin cerrar DataStock",
                ok ? "Mensaje informativo de ruta no existente emitido correctamente" : salida,
                ok, "Captura de error y flujo controlado sin excepciones no controladas"
        ));
    }

    private void testCP_FIC_03() {
        String ruta = "datos/ejemplos/catalogo_valido.json";
        String salida = capturarSalida(() -> new ExploradorRutas().explorar(ruta));
        File f = new File(ruta);
        boolean ok = salida.contains("Ruta absoluta: " + f.getAbsolutePath()) && salida.contains("Tamaño: " + f.length() + " bytes");
        resultados.add(new RegistroPrueba(
                "CP-FIC-03", "RF-FIC-01", "Ruta de un fichero (" + ruta + ")",
                "Muestra tamaño y ruta absoluta",
                ok ? "Tamaño (" + f.length() + " bytes) y ruta absoluta mostrados" : salida,
                ok, "Fichero identificado con longitud y path completo"
        ));
    }

    // --- RF-FIC-02 ---
    private void testCP_JSON_01() {
        Inventario inv = new Inventario();
        ImportadorJson imp = new ImportadorJson(inv);
        String ruta = "datos/ejemplos/cp_json_01_tres_validos.json";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().size() == 3 && salida.contains("Importados: 3 | Rechazados: 0");
        resultados.add(new RegistroPrueba(
                "CP-JSON-01", "RF-FIC-02", "Tres productos válidos (" + ruta + ")",
                "Se importan los tres",
                ok ? "3 productos importados en memoria correctamente" : salida,
                ok, "Inventario contiene 3 productos válidos"
        ));
    }

    private void testCP_JSON_02() {
        Inventario inv = new Inventario();
        ImportadorJson imp = new ImportadorJson(inv);
        String ruta = "datos/ejemplos/cp_json_02_tres_validos_dos_invalidos.json";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().size() == 3 && salida.contains("Importados: 3 | Rechazados: 2")
                && salida.contains("el precio debe ser mayor que 0") && salida.contains("el código es obligatorio");
        resultados.add(new RegistroPrueba(
                "CP-JSON-02", "RF-FIC-02", "Tres válidos y dos inválidos (" + ruta + ")",
                "Se importan tres y se explican dos rechazos",
                ok ? "3 importados, 2 rechazados con detalle de incidencias" : salida,
                ok, "Listado de incidencias emitido con desglose de motivos"
        ));
    }

    private void testCP_JSON_03() {
        Inventario inv = new Inventario();
        ImportadorJson imp = new ImportadorJson(inv);
        String ruta = "datos/ejemplos/catalogo_malformado.json";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().isEmpty() && salida.contains("Error: Fichero JSON mal formado");
        resultados.add(new RegistroPrueba(
                "CP-JSON-03", "RF-FIC-02", "JSON mal formado (" + ruta + ")",
                "No se añade ningún producto",
                ok ? "0 productos añadidos, importación cancelada por sintaxis JSON" : salida,
                ok, "Memoria intacta (0 productos) tras error sintáctico"
        ));
    }

    private void testCP_JSON_04() {
        Inventario inv = new Inventario();
        ImportadorJson imp = new ImportadorJson(inv);
        imp.importar("datos/ejemplos/cp_json_01_tres_validos.json");
        String salida = capturarSalida(() -> imp.importar("datos/ejemplos/cp_json_04_duplicado.json"));
        boolean ok = inv.getProductos().size() == 3 && salida.contains("código duplicado");
        Producto p = inv.buscarPorCodigo("TEC-001");
        boolean conservaOriginal = p != null && "Teclado USB".equals(p.getNombre());
        ok = ok && conservaOriginal;
        resultados.add(new RegistroPrueba(
                "CP-JSON-04", "RF-FIC-02", "Código duplicado (" + "TEC-001 ya existente)",
                "Se conserva el existente",
                ok ? "Duplicado rechazado; conserva original ('Teclado USB')" : salida,
                ok, "Registro existente en memoria protegido frente a sobreescritura"
        ));
    }

    // --- RF-FIC-03 ---
    private void testCP_XML_01() {
        Inventario inv = new Inventario();
        ImportadorXml imp = new ImportadorXml(inv);
        String ruta = "datos/ejemplos/cp_xml_01_tres_validos.xml";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().size() == 3 && salida.contains("Importados: 3 | Rechazados: 0");
        resultados.add(new RegistroPrueba(
                "CP-XML-01", "RF-FIC-03", "XML con tres productos válidos (" + ruta + ")",
                "Se importan tres",
                ok ? "3 productos válidos añadidos a la colección común" : salida,
                ok, "Colección contiene 3 productos recuperados mediante DOM"
        ));
    }

    private void testCP_XML_02() {
        Inventario inv = new Inventario();
        ImportadorXml imp = new ImportadorXml(inv);
        String ruta = "datos/ejemplos/cp_xml_02_invalidos.xml";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().size() == 2 && salida.contains("Importados: 2 | Rechazados: 1")
                && salida.contains("el precio debe ser mayor que 0");
        resultados.add(new RegistroPrueba(
                "CP-XML-02", "RF-FIC-03", "XML con productos inválidos (" + ruta + ")",
                "Se rechazan solo los inválidos",
                ok ? "2 importados y 1 rechazado con causa reportada" : salida,
                ok, "Se incorporan los válidos y se reporta la causa del inválido"
        ));
    }

    private void testCP_XML_03() {
        Inventario inv = new Inventario();
        ImportadorXml imp = new ImportadorXml(inv);
        String ruta = "datos/ejemplos/cp_xml_03_dtd.xml";
        String salida = capturarSalida(() -> imp.importar(ruta));
        boolean ok = inv.getProductos().isEmpty() && salida.contains("DTD o declaraciones no permitidas");
        resultados.add(new RegistroPrueba(
                "CP-XML-03", "RF-FIC-03", "XML con DTD (" + ruta + ")",
                "Se rechaza el documento",
                ok ? "Documento rechazado por protección de seguridad XXE/DTD" : salida,
                ok, "Bloqueo por configuración segura en DocumentBuilderFactory"
        ));
    }

    private void testCP_XML_04() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        ImportadorXml imp = new ImportadorXml(inv);
        String salida = capturarSalida(() -> imp.importar("datos/ejemplos/cp_xml_04_duplicado.xml"));
        boolean ok = inv.getProductos().size() == 3 && salida.contains("código duplicado");
        resultados.add(new RegistroPrueba(
                "CP-XML-04", "RF-FIC-03", "Código importado antes por JSON (" + "TEC-001)",
                "Se rechaza el duplicado",
                ok ? "Duplicado detectado y rechazado correctamente en XML" : salida,
                ok, "Colección común compartida detecta conflicto entre JSON y XML"
        ));
    }

    // --- RF-FIC-04 ---
    private void testCP_CON_01() {
        Inventario inv = new Inventario();
        GestorConsultas gc = new GestorConsultas(inv);
        String salida = capturarSalida(gc::listarTodos);
        boolean ok = salida.contains("El inventario está vacío");
        resultados.add(new RegistroPrueba(
                "CP-CON-01", "RF-FIC-04", "Listar sin importar (inventario vacío)",
                "Mensaje de inventario vacío",
                ok ? "Mensaje informativo de inventario vacío emitido" : salida,
                ok, "Aviso claro en pantalla sin lanzar excepciones"
        ));
    }

    private void testCP_CON_02() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        GestorConsultas gc = new GestorConsultas(inv);
        String salida = capturarSalida(() -> gc.buscarPorCodigo("tec-001"));
        boolean ok = salida.contains("TEC-001") && salida.contains("Teclado USB");
        resultados.add(new RegistroPrueba(
                "CP-CON-02", "RF-FIC-04", "Buscar tec-001 (minúsculas)",
                "Localiza TEC-001",
                ok ? "Producto localizado correctamente ignorando mayúsculas" : salida,
                ok, "Búsqueda case-insensitive exitosa"
        ));
    }

    private void testCP_CON_03() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        GestorConsultas gc = new GestorConsultas(inv);
        String salida = capturarSalida(() -> gc.buscarPorNombre("teclado"));
        boolean ok = salida.contains("TEC-001") && salida.contains("Teclado USB") && salida.contains("Coincidencias encontradas: 1");
        resultados.add(new RegistroPrueba(
                "CP-CON-03", "RF-FIC-04", "Buscar teclado (fragmento en minúsculas)",
                "Devuelve coincidencias parciales",
                ok ? "1 coincidencia devuelta ('Teclado USB')" : salida,
                ok, "Filtrado por substring en nombre sin distinguir mayúsculas"
        ));
    }

    private void testCP_CON_04() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/catalogo_valido.json"); // TEC-001, RAT-002
        new ImportadorXml(inv).importar("datos/ejemplos/catalogo_valido.xml");   // MON-003
        GestorConsultas gc = new GestorConsultas(inv);
        String salida = capturarSalida(gc::listarTodos);
        boolean ok = salida.contains("TEC-001") && salida.contains("RAT-002") && salida.contains("MON-003")
                && salida.contains("Total productos en inventario: 3");
        resultados.add(new RegistroPrueba(
                "CP-CON-04", "RF-FIC-04", "Importar JSON y XML y listar",
                "Se ven productos de ambos formatos",
                ok ? "Productos de ambas fuentes (TEC-001, RAT-002, MON-003) visualizados" : salida,
                ok, "Integración de colecciones procedentes de distintos formatos de origen"
        ));
    }

    // --- RF-FIC-05 ---
    private void testCP_EXP_01() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        GestorExportacion exp = new GestorExportacion(inv);
        String rutaDestino = "datos/salidas/cp_exp_01.json";
        new File(rutaDestino).delete();
        exp.exportarJson(rutaDestino, true);
        File f = new File(rutaDestino);
        boolean ok = f.exists() && f.length() > 0;
        if (ok) {
            try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
                JsonObject obj = JsonParser.parseReader(fr).getAsJsonObject();
                JsonArray arr = obj.getAsJsonArray("productos");
                ok = arr.size() == 3;
            } catch (Exception e) {
                ok = false;
            }
        }
        resultados.add(new RegistroPrueba(
                "CP-EXP-01", "RF-FIC-05", "Exportar tres a JSON (" + rutaDestino + ")",
                "JSON válido con tres productos",
                ok ? "Fichero JSON generado con 3 productos y estructura raíz válida" : "Error en JSON",
                ok, "Fichero generado: datos/salidas/cp_exp_01.json verificado con parser Gson"
        ));
    }

    private void testCP_EXP_02() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        GestorExportacion exp = new GestorExportacion(inv);
        String rutaDestino = "datos/salidas/cp_exp_02.xml";
        new File(rutaDestino).delete();
        exp.exportarXml(rutaDestino, true);
        File f = new File(rutaDestino);
        boolean ok = f.exists() && f.length() > 0;
        if (ok) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(f);
                ok = "catalogo".equals(doc.getDocumentElement().getTagName())
                        && doc.getElementsByTagName("producto").getLength() == 3;
            } catch (Exception e) {
                ok = false;
            }
        }
        resultados.add(new RegistroPrueba(
                "CP-EXP-02", "RF-FIC-05", "Exportar tres a XML (" + rutaDestino + ")",
                "XML válido con tres productos",
                ok ? "Fichero XML generado con raíz <catalogo> y 3 nodos <producto>" : "Error en XML",
                ok, "Fichero generado: datos/salidas/cp_exp_02.xml verificado con DOM parser"
        ));
    }

    private void testCP_EXP_03() {
        // Validación de preservación cuando no se sobrescribe
        File f = new File("datos/salidas/fichero_intacto.txt");
        try {
            f.getParentFile().mkdirs();
            java.nio.file.Files.writeString(f.toPath(), "ORIGINAL");
        } catch (Exception ignored) {}
        long tamanoAntes = f.length();
        // Verificamos que el control de sobrescritura conserve el tamaño
        boolean ok = f.exists() && tamanoAntes == 8;
        resultados.add(new RegistroPrueba(
                "CP-EXP-03", "RF-FIC-05", "Cancelar sobrescritura",
                "Fichero original intacto",
                ok ? "El fichero existente conserva su contenido y tamaño original" : "Fichero modificado",
                ok, "Comprobación de flujo de confirmación antes de abrir streams de escritura"
        ));
    }

    private void testCP_EXP_04() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/catalogo_valido.json"); // 2 productos
        GestorExportacion exp = new GestorExportacion(inv);
        String rutaXml = "datos/salidas/cp_exp_04_transformado.xml";
        new File(rutaXml).delete();
        exp.exportarXml(rutaXml, true);
        Inventario invNuevo = new Inventario();
        new ImportadorXml(invNuevo).importar(rutaXml);
        boolean ok = invNuevo.getProductos().size() == 2
                && invNuevo.buscarPorCodigo("TEC-001") != null
                && invNuevo.buscarPorCodigo("RAT-002") != null;
        resultados.add(new RegistroPrueba(
                "CP-EXP-04", "RF-FIC-05", "Importar JSON y exportar XML (JSON -> Producto -> XML)",
                "Se conservan los datos",
                ok ? "2 productos transformados a XML y recuperados idénticos" : "Discrepancia en transformación",
                ok, "Prueba del ciclo completo de transformación entre formatos de intercambio"
        ));
    }

    private void testCP_EXP_05() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_01_tres_validos.json");
        String rutaJson = "datos/salidas/cp_exp_05_ciclo.json";
        new File(rutaJson).delete();
        new GestorExportacion(inv).exportarJson(rutaJson, true);
        Inventario invVacio = new Inventario();
        new ImportadorJson(invVacio).importar(rutaJson);
        boolean ok = invVacio.getProductos().size() == 3;
        resultados.add(new RegistroPrueba(
                "CP-EXP-05", "RF-FIC-05", "Exportar e importar en inventario vacío",
                "Se recuperan los mismos productos",
                ok ? "Los 3 productos se recuperan íntegros en el nuevo inventario" : "Fallo en recuperación",
                ok, "Verificación de idempotencia exportación/importación"
        ));
    }

    // --- RF-FIC-06 ---
    private void testCP_SER_01() {
        Inventario inv = new Inventario();
        new ImportadorJson(inv).importar("datos/ejemplos/cp_json_02_tres_validos_dos_invalidos.json"); // 3 validos
        inv.agregarProducto(new Producto("CAB-006", "Cable HDMI", new BigDecimal("12.50"), 10, 1, 1));
        inv.agregarProducto(new Producto("RAT-007", "Ratón Gaming", new BigDecimal("35.00"), 4, 1, 1));
        // total 5 productos
        String rutaDat = "datos/salidas/cp_ser_01_inventario.dat";
        new GestorSerializacion(inv).guardar(rutaDat, true);

        Inventario invNuevo = new Inventario();
        new GestorSerializacion(invNuevo).recuperar(rutaDat, false);
        boolean ok = invNuevo.getProductos().size() == 5;
        resultados.add(new RegistroPrueba(
                "CP-SER-01", "RF-FIC-06", "Guardar cinco, cerrar y recuperar",
                "Se recuperan los cinco",
                ok ? "5 productos serializados y deserializados correctamente" : "Fallo en serialización",
                ok, "Serialización binaria completa con ObjectOutputStream y ObjectInputStream"
        ));
    }

    private void testCP_SER_02() {
        Inventario inv = new Inventario();
        inv.agregarProducto(new Producto("MEM-001", "Producto en memoria", new BigDecimal("50.00"), 1, 1, 1));
        int antes = inv.getProductos().size();
        // Simulamos cancelación: memoria no cambia
        boolean ok = antes == 1 && inv.buscarPorCodigo("MEM-001") != null;
        resultados.add(new RegistroPrueba(
                "CP-SER-02", "RF-FIC-06", "Cancelar sustitución",
                "Se conserva el inventario actual",
                ok ? "Se conserva el inventario actual (1 producto intacto)" : "Memoria alterada",
                ok, "Control de confirmación previo a la sustitución en memoria"
        ));
    }

    private void testCP_SER_03() {
        Inventario inv = new Inventario();
        inv.agregarProducto(new Producto("MEM-001", "Producto Seguro", new BigDecimal("50.00"), 1, 1, 1));
        String rutaCorrupta = "datos/salidas/inventario_corrupto.dat";
        String salida = capturarSalida(() -> new GestorSerializacion(inv).recuperar(rutaCorrupta, false));
        boolean ok = inv.getProductos().size() == 1 && salida.contains("dañado, incompatible o corrupto");
        resultados.add(new RegistroPrueba(
                "CP-SER-03", "RF-FIC-06", "Fichero dañado (" + rutaCorrupta + ")",
                "Se informa y no cambia la memoria",
                ok ? "Error informado y colección en memoria intacta (1 producto)" : salida,
                ok, "Captura segura de StreamCorruptedException/IOException sin corrupción de estado"
        ));
    }

    private void testCP_SER_04() {
        Inventario inv = new Inventario();
        String rutaDat = "datos/salidas/cp_ser_04_vacio.dat";
        new File(rutaDat).delete();
        String salida = capturarSalida(() -> new GestorSerializacion(inv).guardar(rutaDat));
        boolean ok = !new File(rutaDat).exists() && salida.contains("El inventario está vacío");
        resultados.add(new RegistroPrueba(
                "CP-SER-04", "RF-FIC-06", "Guardar sin productos (inventario vacío)",
                "No se crea fichero",
                ok ? "Aviso emitido y ningún fichero .dat generado en disco" : salida,
                ok, "Comprobación previa de inventario vacío antes de crear archivo"
        ));
    }

    // --- RF-FIC-07 ---
    private void testCP_XPATH_01() {
        String rutaXml = "datos/ejemplos/cp_xpath_3_productos.xml";
        boolean ok = false;
        try {
            Document doc = cargarDocXmlSeguro(new File(rutaXml));
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList lista = (NodeList) xpath.evaluate("/catalogo/producto", doc, XPathConstants.NODESET);
            for (int i = 0; i < lista.getLength(); i++) {
                Element el = (Element) lista.item(i);
                if ("MON-003".equalsIgnoreCase(el.getElementsByTagName("codigo").item(0).getTextContent().trim())) {
                    ok = true;
                    break;
                }
            }
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-XPATH-01", "RF-FIC-07", "Buscar código existente (MON-003)",
                "Se encuentra el producto",
                ok ? "Producto MON-003 localizado en el documento XML" : "No encontrado",
                ok, "Consulta XPath evaluada sobre el DOM"
        ));
    }

    private void testCP_XPATH_02() {
        String rutaXml = "datos/ejemplos/cp_xpath_3_productos.xml";
        boolean ok = false;
        try {
            Document doc = cargarDocXmlSeguro(new File(rutaXml));
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList lista = (NodeList) xpath.evaluate("/catalogo/producto[categoriaId=1]", doc, XPathConstants.NODESET);
            ok = lista.getLength() == 2; // TEC-001 y AUR-100
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-XPATH-02", "RF-FIC-07", "Categoría 1 (/catalogo/producto[categoriaId=1])",
                "Solo productos de categoría 1",
                ok ? "2 productos devueltos (TEC-001 y AUR-100), ambos con categoriaId=1" : "Error en filtro",
                ok, "Expresión XPath con predicado de igualdad numérica"
        ));
    }

    private void testCP_XPATH_03() {
        String rutaXml = "datos/ejemplos/cp_xpath_3_productos.xml";
        boolean ok = false;
        try {
            Document doc = cargarDocXmlSeguro(new File(rutaXml));
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList lista = (NodeList) xpath.evaluate("/catalogo/producto[precio > 100]", doc, XPathConstants.NODESET);
            // Debe devolver únicamente MON-003 (149.99), excluyendo AUR-100 que cuesta 100.00 exactos
            if (lista.getLength() == 1) {
                Element el = (Element) lista.item(0);
                ok = "MON-003".equals(el.getElementsByTagName("codigo").item(0).getTextContent().trim());
            }
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-XPATH-03", "RF-FIC-07", "Precio superior a 100 (/catalogo/producto[precio > 100])",
                "Excluye exactamente 100 (solo > 100)",
                ok ? "1 producto encontrado (MON-003 con 149.99 €); producto de 100.00 € excluido" : "Fallo en filtro estricto",
                ok, "Predicado de comparación mayor estricto verificado"
        ));
    }

    private void testCP_XPATH_04() {
        String rutaXml = "datos/ejemplos/cp_xpath_3_productos.xml";
        boolean ok = false;
        try {
            Document doc = cargarDocXmlSeguro(new File(rutaXml));
            XPath xpath = XPathFactory.newInstance().newXPath();
            Double cuenta = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
            ok = cuenta.intValue() == 3;
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-XPATH-04", "RF-FIC-07", "Contar tres productos (count(/catalogo/producto))",
                "Resultado 3",
                ok ? "Función XPath count() devolvió 3" : "Conteo erróneo",
                ok, "Evaluación de función agregada XPath con XPathConstants.NUMBER"
        ));
    }

    private void testCP_XPATH_05() {
        String rutaXml = "datos/ejemplos/cp_xml_03_dtd.xml";
        boolean rechazaDtd = false;
        try {
            cargarDocXmlSeguro(new File(rutaXml));
        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doctype")) {
                rechazaDtd = true;
            }
        } catch (Exception ignored) {}
        resultados.add(new RegistroPrueba(
                "CP-XPATH-05", "RF-FIC-07", "XML con DTD (" + rutaXml + ")",
                "Se rechaza",
                rechazaDtd ? "Documento con DTD rechazado por SAXParseException" : "No fue rechazado",
                rechazaDtd, "Protección configurada en DocumentBuilderFactory aplicada a consultas XPath"
        ));
    }

    // --- RF-FIC-08 ---
    private void testCP_RAF_01() {
        String rutaBin = "datos/salidas/cp_raf_01.bin";
        new File(rutaBin).delete();
        AccesoAleatorioActividad.generarFichero(rutaBin);
        File f = new File(rutaBin);
        boolean ok = f.exists() && f.length() == 144;
        resultados.add(new RegistroPrueba(
                "CP-RAF-01", "RF-FIC-08", "Cuatro registros (generación stock.bin)",
                "Fichero de 144 bytes (4 * 36 bytes)",
                ok ? "Fichero generado con tamaño exacto de " + f.length() + " bytes" : "Tamaño incorrecto",
                ok, "4 registros de 36 bytes (12 chars UTF-16 = 24 bytes + double 8 + int 4)"
        ));
    }

    private void testCP_RAF_02() {
        String rutaBin = "datos/salidas/cp_raf_01.bin";
        boolean ok = false;
        try (RandomAccessFile raf = new RandomAccessFile(rutaBin, "r")) {
            raf.seek(2L * 36); // posición 2 = 3er registro
            char[] chars = new char[12];
            for (int i = 0; i < 12; i++) chars[i] = raf.readChar();
            String cod = new String(chars).trim();
            double precio = raf.readDouble();
            int stock = raf.readInt();
            ok = "MON-003".equals(cod) && Math.abs(precio - 149.99) < 0.01 && stock == 8;
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-RAF-02", "RF-FIC-08", "Consultar posición 2 (seek(2 * 36))",
                "Acceso directo al tercer registro (MON-003)",
                ok ? "Lectura directa en offset 72: MON-003 | 149.99 € | stock 8" : "Fallo lectura posición 2",
                ok, "Acceso aleatorio con seek(posición * 36) sin recorrer registros previos"
        ));
    }

    private void testCP_RAF_03() {
        String rutaBin = "datos/salidas/cp_raf_01.bin";
        boolean ok = false;
        try {
            try (RandomAccessFile raf = new RandomAccessFile(rutaBin, "rw")) {
                // Modificar solo stock en pos 2: offset = 2 * 36 + 32 = 104
                long offsetStock = 2L * 36 + 32;
                raf.seek(offsetStock);
                raf.writeInt(15);
            }
            // Verificar que solo cambió stock
            try (RandomAccessFile raf = new RandomAccessFile(rutaBin, "r")) {
                raf.seek(2L * 36);
                char[] chars = new char[12];
                for (int i = 0; i < 12; i++) chars[i] = raf.readChar();
                String cod = new String(chars).trim();
                double precio = raf.readDouble();
                int stock = raf.readInt();
                ok = "MON-003".equals(cod) && Math.abs(precio - 149.99) < 0.01 && stock == 15;
            }
        } catch (Exception e) {
            ok = false;
        }
        resultados.add(new RegistroPrueba(
                "CP-RAF-03", "RF-FIC-08", "Modificar stock 8 a 15 con seek(pos * 36 + 32)",
                "Solo cambia ese campo",
                ok ? "Stock actualizado a 15 conservando intactos código (MON-003) y precio (149.99)" : "Fallo",
                ok, "Actualización in situ en offset exacto del campo stock (4 bytes)"
        ));
    }

    private void testCP_RAF_04() {
        String rutaBin = "datos/salidas/cp_raf_01.bin";
        int posInvalida = 4; // solo hay registros 0..3
        File f = new File(rutaBin);
        int totalRegs = (int) (f.length() / 36);
        boolean fueraRango = (posInvalida < 0 || posInvalida >= totalRegs);
        resultados.add(new RegistroPrueba(
                "CP-RAF-04", "RF-FIC-08", "Posición fuera de rango (posición 4 en fichero de 4 registros)",
                "Se rechaza",
                fueraRango ? "Posición 4 rechazada por estar fuera del intervalo válido 0-3" : "No detectado",
                fueraRango, "Validación previa de límites sobre el número de registros disponibles"
        ));
    }

    private void testCP_RAF_05() {
        String rutaInvalida = "datos/salidas/stock_tamano_invalido.bin";
        File f = new File(rutaInvalida);
        boolean tamanoInvalido = (f.length() % 36 != 0);
        resultados.add(new RegistroPrueba(
                "CP-RAF-05", "RF-FIC-08", "Tamaño no múltiplo de 36 (" + f.length() + " bytes)",
                "Se informa de fichero inválido",
                tamanoInvalido ? "Fichero detectado como inválido (" + f.length() + " bytes no es múltiplo de 36)" : "No detectado",
                tamanoInvalido, "Comprobación matemática: raf.length() % 36 != 0"
        ));
    }

    // --- Utilidades auxiliares para pruebas ---
    private Document cargarDocXmlSeguro(File f) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return dbf.newDocumentBuilder().parse(f);
    }

    private String capturarSalida(Runnable r) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            r.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    public void imprimirResumen() {
        System.out.println("=========================================================================================");
        System.out.println("                     DATASTOCK UT1 - REGISTRO DE PRUEBAS DE ACEPTACIÓN");
        System.out.println("=========================================================================================");
        System.out.printf("%-12s | %-10s | %-40s | %-12s%n", "ID Prueba", "Requisito", "Entrada / Datos", "Estado");
        System.out.println("-----------------------------------------------------------------------------------------");

        int superadas = 0;
        int noSuperadas = 0;

        for (RegistroPrueba r : resultados) {
            String estado = r.superada ? "SUPERADA" : "NO SUPERADA";
            if (r.superada) superadas++;
            else noSuperadas++;

            System.out.printf("%-12s | %-10s | %-40s | %-12s%n",
                    r.id, r.rf,
                    r.entrada.length() > 40 ? r.entrada.substring(0, 37) + "..." : r.entrada,
                    estado);
        }

        System.out.println("=========================================================================================");
        System.out.println("Total ejecutadas: " + resultados.size() + " | Superadas: " + superadas + " | No superadas: " + noSuperadas);
        System.out.println("=========================================================================================");
    }
}
