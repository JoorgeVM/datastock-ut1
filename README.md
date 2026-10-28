# DataStock – Fase 1 (UT1): Ficheros y Formatos de Intercambio

**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Autor:** Jorge  
**Tecnologías:** Java 21 LTS · Maven 3.9 · Gson 2.11 · DOM (JAXP) · XPath · RandomAccessFile · Serialización Java

---

## 1. Descripción del Proyecto

DataStock es una aplicación modular de consola orientada a la gestión de inventario para una pequeña empresa de distribución informática. En esta primera fase (**UT1**), la aplicación gestiona catálogos de productos mediante diferentes técnicas de tratamiento y almacenamiento en ficheros:
- Exploración y diagnóstico del sistema de ficheros.
- Importación e interoperabilidad con formatos JSON (Gson) y XML (DOM seguro frente a XXE/DTD).
- Consultas y filtrado en memoria sin alterar las colecciones de trabajo.
- Exportación del catálogo a JSON y XML preservando la integridad de datos.
- Persistencia binaria y recuperación mediante serialización de objetos (`.dat`).
- Actividades técnicas complementarias independientes: consultas directas XML con XPath y acceso aleatorio a registros binarios de longitud fija (36 bytes) con `RandomAccessFile`.

---

## 2. Requisitos Previos y Entorno

- **JDK 21** o superior instalado y configurado (`java -version`).
- **Apache Maven 3.9+** configurado en el sistema (`mvn -version`).
- Terminal compatible con UTF-8 para la correcta visualización de caracteres en español y códigos ANSI (opcionalmente configurable con `datastock.color=false` o `NO_COLOR=1`).

---

## 3. Instrucciones de Compilación y Ejecución

### Compilar el proyecto completo
```bash
mvn clean compile
```

### Ejecutar la aplicación principal (Menú interactivo)
```bash
mvn exec:java
```
O directamente con `java`:
```bash
java -cp "target/classes;%USERPROFILE%/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" es.iesisidra.datastock.App
```

### Ejecutar las Actividades Complementarias Independientes
1. **RF-FIC-07 · Consultas XML mediante XPath:**
   ```bash
   java -cp "target/classes;%USERPROFILE%/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" es.iesisidra.datastock.complementarias.XPathActividad
   ```
2. **RF-FIC-08 · Acceso aleatorio con RandomAccessFile:**
   ```bash
   java -cp "target/classes;%USERPROFILE%/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
   ```

### Ejecutar la Batería Automatizada de Pruebas de Aceptación
```bash
java -cp "target/classes;%USERPROFILE%/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" es.iesisidra.datastock.pruebas.EjecutorPruebasAceptacion
```
Para regenerar la plantilla Word y el informe Markdown de pruebas:
```bash
java -cp "target/classes;%USERPROFILE%/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" es.iesisidra.datastock.pruebas.GeneradorDocumentoPruebas
```

---

## 4. Estructura del Proyecto

```text
DataStock_UT1_Proyecto_Inicial/
├── pom.xml                                                 # Configuración Maven y dependencias
├── README.md                                               # Documentación técnica del proyecto
├── REGISTRO_PRUEBAS.md                                     # Informe completo de pruebas de aceptación
├── DataStock_UT1_Plantilla_Registro_Pruebas_Rellenada.docx # Plantilla oficial Word cumplimentada
├── .gitignore                                              # Filtro de ficheros compilados y temporales
├── datos/
│   ├── ejemplos/                                           # Catálogos de prueba válidos e inválidos
│   │   ├── catalogo_valido.json
│   │   ├── catalogo_valido.xml
│   │   ├── catalogo_con_errores.json
│   │   ├── catalogo_malformado.json
│   │   ├── cp_json_01_tres_validos.json
│   │   ├── cp_json_02_tres_validos_dos_invalidos.json
│   │   ├── cp_json_04_duplicado.json
│   │   ├── cp_xml_01_tres_validos.xml
│   │   ├── cp_xml_02_invalidos.xml
│   │   ├── cp_xml_03_dtd.xml
│   │   ├── cp_xml_04_duplicado.xml
│   │   ├── cp_xpath_3_productos.xml
│   │   └── directorio_prueba/                              # Estructura para prueba de exploración
│   └── salidas/                                            # Exportaciones y ficheros binarios generados
│       ├── cp_exp_01.json
│       ├── cp_exp_02.xml
│       ├── cp_ser_01_inventario.dat
│       └── stock.bin
├── material_referencia/                                    # Esquema relacional y SQL para UT2
└── src/main/java/es/iesisidra/datastock/
    ├── App.java                                            # Clase principal de arranque
    ├── modelo/                                             # Entidades del dominio
    │   ├── Producto.java                                   # Entidad de catálogo (Serializable)
    │   ├── Categoria.java                                  # Entidad categoría
    │   ├── Proveedor.java                                  # Entidad proveedor
    │   └── MovimientoStock.java                            # Entidad histórico de stock
    ├── validacion/                                         # Reglas de negocio e integridad
    │   ├── ValidadorProducto.java                          # Validaciones de campos (código, precio, stock...)
    │   └── ResultadoValidacion.java                        # Objeto resultado con detalle de incidencias
    ├── servicio/                                           # Lógica de aplicación
    │   ├── Inventario.java                                 # Colección central en memoria
    │   └── GestorConsultas.java                            # Operaciones de consulta y búsqueda
    ├── ficheros/                                           # Gestión de I/O y formatos
    │   ├── ExploradorRutas.java                            # RF-FIC-01: Inspección de rutas y directorios
    │   ├── ImportadorJson.java                             # RF-FIC-02: Parseo Gson y control de incidencias
    │   ├── ImportadorXml.java                              # RF-FIC-03: DOM seguro contra XXE/DTD
    │   ├── GestorExportacion.java                          # RF-FIC-05: Generación JSON (Gson) y XML (DOM)
    │   └── GestorSerializacion.java                        # RF-FIC-06: Persistencia ObjectOutput/InputStream
    ├── complementarias/                                    # Actividades obligatorias independientes
    │   ├── XPathActividad.java                             # RF-FIC-07: Consultas directas XML con XPath
    │   └── AccesoAleatorioActividad.java                   # RF-FIC-08: Registros fijos de 36 bytes (RAF)
    ├── ui/                                                 # Capa de presentación por consola
    │   └── MenuPrincipal.java                              # Navegación y delegación limpia sin lógica en switch
    ├── util/                                               # Utilidades auxiliares
    │   ├── LectorConsola.java                              # Scanner centralizado seguro
    │   └── ColoresConsola.java                             # Formato y colores ANSI
    └── pruebas/                                            # Automatización de pruebas
        ├── EjecutorPruebasAceptacion.java                  # Suite de las 34 pruebas de aceptación
        └── GeneradorDocumentoPruebas.java                  # Generador de Word .docx y Markdown
```

---

## 5. Arquitectura y Buenas Prácticas Aplicadas

1. **Separación de responsabilidades:** La interfaz de usuario (`MenuPrincipal`) no alberga la lógica de validación, transformación ni persistencia; delega en clases especializadas (`ficheros`, `servicio`, `validacion`).
2. **Cierre seguro de flujos:** Uso de `try-with-resources` en todas las aperturas de ficheros, flujos binarios y streams de texto para prevenir fugas de descriptores (`FileDescriptor leaks`).
3. **Seguridad contra vulnerabilidades XML:** Configuración defensiva en `DocumentBuilderFactory` deshabilitando `DOCTYPE` y carga de entidades externas para mitigar ataques XXE (XML External Entity Injection).
4. **Validación estricta de datos:** El validador centraliza las restricciones de negocio (formato regex de código `[A-Z0-9-]+`, precio > 0 con `BigDecimal`, stock >= 0, longitud de texto y unicidad insensible a mayúsculas/minúsculas).
5. **Aislamiento de almacenes:** Los cambios realizados sobre el fichero binario didáctico `stock.bin` (`RandomAccessFile`) son completamente independientes de la colección en memoria, demostrando el acceso aleatorio mediante offsets fijos (`pos * 36` y `pos * 36 + 32`).

---

## 6. Resultados de las Pruebas de Aceptación

Se han ejecutado y superado el **100% de las pruebas de aceptación** descritas en el enunciado oficial:
- **Total ejecutadas:** 34
- **Superadas:** 34
- **No superadas:** 0

El desglose completo por caso de prueba (`CP-FIC-01` a `CP-RAF-05`), junto con los datos de entrada, comportamiento esperado, resultado obtenido y evidencias, se encuentra recogido en:
- `REGISTRO_PRUEBAS.md` (formato Markdown integrado en el repositorio).
- `DataStock_UT1_Plantilla_Registro_Pruebas_Rellenada.docx` (plantilla oficial Word de entrega).
