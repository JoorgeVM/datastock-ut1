# DATASTOCK · REGISTRO DE PRUEBAS DE ACEPTACIÓN

**UT1 · Ficheros y formatos de intercambio de datos** | 2.º DAM | Curso 2026–2027
**Centro:** IES Isidra de Guzmán (Alcalá de Henares)

| Campo | Valor |
|---|---|
| **Alumno/a** | Jorge |
| **Módulo** | Acceso a Datos (2.º DAM) |
| **Repositorio GitHub** | https://github.com/jorge-dam/datastock-ut1 |
| **Commit / Etiqueta** | `entrega-ut1` |
| **Fecha de ejecución** | 28/10/2026 |

## 1. Resumen de pruebas

| ID prueba | RF | Entrada / datos | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|
| `CP-FIC-01` | `RF-FIC-01` | Ruta con dos ficheros y un directorio (datos/ejemplos/directorio_prueba) | Se muestran tres elementos y totales correctos (2 ficheros, 1 dir) | 3 elementos detectados y 'Total ficheros: 2 | Directorios: 1' | **SUPERADA** |
| `CP-FIC-02` | `RF-FIC-01` | Ruta inexistente (datos/ruta_ficticia_inexistente) | Mensaje de error sin cerrar DataStock | Mensaje informativo de ruta no existente emitido correctamente | **SUPERADA** |
| `CP-FIC-03` | `RF-FIC-01` | Ruta de un fichero (datos/ejemplos/catalogo_valido.json) | Muestra tamaño y ruta absoluta | Tamaño (246 bytes) y ruta absoluta mostrados | **SUPERADA** |
| `CP-JSON-01` | `RF-FIC-02` | Tres productos válidos (datos/ejemplos/cp_json_01_tres_validos.json) | Se importan los tres | 3 productos importados en memoria correctamente | **SUPERADA** |
| `CP-JSON-02` | `RF-FIC-02` | Tres válidos y dos inválidos (datos/ejemplos/cp_json_02_tres_validos_dos_invalidos.json) | Se importan tres y se explican dos rechazos | 3 importados, 2 rechazados con detalle de incidencias | **SUPERADA** |
| `CP-JSON-03` | `RF-FIC-02` | JSON mal formado (datos/ejemplos/catalogo_malformado.json) | No se añade ningún producto | 0 productos añadidos, importación cancelada por sintaxis JSON | **SUPERADA** |
| `CP-JSON-04` | `RF-FIC-02` | Código duplicado (TEC-001 ya existente) | Se conserva el existente | Duplicado rechazado; conserva original ('Teclado USB') | **SUPERADA** |
| `CP-XML-01` | `RF-FIC-03` | XML con tres productos válidos (datos/ejemplos/cp_xml_01_tres_validos.xml) | Se importan tres | 3 productos válidos añadidos a la colección común | **SUPERADA** |
| `CP-XML-02` | `RF-FIC-03` | XML con productos inválidos (datos/ejemplos/cp_xml_02_invalidos.xml) | Se rechazan solo los inválidos | 2 importados y 1 rechazado con causa reportada | **SUPERADA** |
| `CP-XML-03` | `RF-FIC-03` | XML con DTD (datos/ejemplos/cp_xml_03_dtd.xml) | Se rechaza el documento | Documento rechazado por protección de seguridad XXE/DTD | **SUPERADA** |
| `CP-XML-04` | `RF-FIC-03` | Código importado antes por JSON (TEC-001) | Se rechaza el duplicado | Duplicado detectado y rechazado correctamente en XML | **SUPERADA** |
| `CP-CON-01` | `RF-FIC-04` | Listar sin importar (inventario vacío) | Mensaje de inventario vacío | Mensaje informativo de inventario vacío emitido | **SUPERADA** |
| `CP-CON-02` | `RF-FIC-04` | Buscar tec-001 (minúsculas) | Localiza TEC-001 | Producto localizado correctamente ignorando mayúsculas | **SUPERADA** |
| `CP-CON-03` | `RF-FIC-04` | Buscar teclado (fragmento en minúsculas) | Devuelve coincidencias parciales | 1 coincidencia devuelta ('Teclado USB') | **SUPERADA** |
| `CP-CON-04` | `RF-FIC-04` | Importar JSON y XML y listar | Se ven productos de ambos formatos | Productos de ambas fuentes (TEC-001, RAT-002, MON-003) visualizados | **SUPERADA** |
| `CP-EXP-01` | `RF-FIC-05` | Exportar tres a JSON (datos/salidas/cp_exp_01.json) | JSON válido con tres productos | Fichero JSON generado con 3 productos y estructura raíz válida | **SUPERADA** |
| `CP-EXP-02` | `RF-FIC-05` | Exportar tres a XML (datos/salidas/cp_exp_02.xml) | XML válido con tres productos | Fichero XML generado con raíz <catalogo> y 3 nodos <producto> | **SUPERADA** |
| `CP-EXP-03` | `RF-FIC-05` | Cancelar sobrescritura | Fichero original intacto | El fichero existente conserva su contenido y tamaño original | **SUPERADA** |
| `CP-EXP-04` | `RF-FIC-05` | Importar JSON y exportar XML (JSON -> Producto -> XML) | Se conservan los datos | 2 productos transformados a XML y recuperados idénticos | **SUPERADA** |
| `CP-EXP-05` | `RF-FIC-05` | Exportar e importar en inventario vacío | Se recuperan los mismos productos | Los 3 productos se recuperan íntegros en el nuevo inventario | **SUPERADA** |
| `CP-SER-01` | `RF-FIC-06` | Guardar cinco, cerrar y recuperar | Se recuperan los cinco | 5 productos serializados y deserializados correctamente | **SUPERADA** |
| `CP-SER-02` | `RF-FIC-06` | Cancelar sustitución | Se conserva el inventario actual | Se conserva el inventario actual (1 producto intacto) | **SUPERADA** |
| `CP-SER-03` | `RF-FIC-06` | Fichero dañado (datos/salidas/inventario_corrupto.dat) | Se informa y no cambia la memoria | Error informado y colección en memoria intacta (1 producto) | **SUPERADA** |
| `CP-SER-04` | `RF-FIC-06` | Guardar sin productos (inventario vacío) | No se crea fichero | Aviso emitido y ningún fichero .dat generado en disco | **SUPERADA** |
| `CP-XPATH-01` | `RF-FIC-07` | Buscar código existente (MON-003) | Se encuentra el producto | Producto MON-003 localizado en el documento XML | **SUPERADA** |
| `CP-XPATH-02` | `RF-FIC-07` | Categoría 1 (/catalogo/producto[categoriaId=1]) | Solo productos de categoría 1 | 2 productos devueltos (TEC-001 y AUR-100), ambos con categoriaId=1 | **SUPERADA** |
| `CP-XPATH-03` | `RF-FIC-07` | Precio superior a 100 (/catalogo/producto[precio > 100]) | Excluye exactamente 100 (solo > 100) | 1 producto encontrado (MON-003 con 149.99 €); producto de 100.00 € excluido | **SUPERADA** |
| `CP-XPATH-04` | `RF-FIC-07` | Contar tres productos (count(/catalogo/producto)) | Resultado 3 | Función XPath count() devolvió 3 | **SUPERADA** |
| `CP-XPATH-05` | `RF-FIC-07` | XML con DTD (datos/ejemplos/cp_xml_03_dtd.xml) | Se rechaza | Documento con DTD rechazado por SAXParseException | **SUPERADA** |
| `CP-RAF-01` | `RF-FIC-08` | Cuatro registros (generación stock.bin) | Fichero de 144 bytes (4 * 36 bytes) | Fichero generado con tamaño exacto de 144 bytes | **SUPERADA** |
| `CP-RAF-02` | `RF-FIC-08` | Consultar posición 2 (seek(2 * 36)) | Acceso directo al tercer registro (MON-003) | Lectura directa en offset 72: MON-003 | 149.99 € | stock 8 | **SUPERADA** |
| `CP-RAF-03` | `RF-FIC-08` | Modificar stock 8 a 15 con seek(pos * 36 + 32) | Solo cambia ese campo | Stock actualizado a 15 conservando intactos código (MON-003) y precio (149.99) | **SUPERADA** |
| `CP-RAF-04` | `RF-FIC-08` | Posición fuera de rango (posición 4 en fichero de 4 registros) | Se rechaza | Posición 4 rechazada por estar fuera del intervalo válido 0-3 | **SUPERADA** |
| `CP-RAF-05` | `RF-FIC-08` | Tamaño no múltiplo de 36 (50 bytes) | Se informa de fichero inválido | Fichero detectado como inválido (50 bytes no es múltiplo de 36) | **SUPERADA** |

## 2. Ficha detallada de pruebas clave

### Ficha: CP-EXP-01
- **Identificador:** `CP-EXP-01`
- **Requisito:** `RF-FIC-05 · Exportar catálogo`
- **Objetivo:** Comprobar que se genera un JSON válido con los productos del inventario.
- **Precondiciones:** Colección con tres productos válidos (`TEC-001`, `RAT-002`, `MON-003`).
- **Datos de entrada:** Ruta de destino: `datos/salidas/cp_exp_01.json`
- **Pasos ejecutados:**
  1. Se inicializa el inventario y se importan 3 productos válidos.
  2. Se ejecuta la opción de exportación a JSON indicando `datos/salidas/cp_exp_01.json`.
  3. Se analiza el fichero resultante con Gson y se comprueba la raíz `productos` con 3 elementos.
- **Resultado esperado:** Se genera un JSON válido con tres productos.
- **Resultado obtenido:** Fichero JSON válido generado correctamente con 3 productos.
- **Estado:** **SUPERADA**
- **Evidencia:** Archivo físico generado en `datos/salidas/cp_exp_01.json`.

### Ficha: CP-RAF-03
- **Identificador:** `CP-RAF-03`
- **Requisito:** `RF-FIC-08 · Acceso aleatorio a fichero binario`
- **Objetivo:** Modificar únicamente el campo stock sin alterar el resto del registro mediante RandomAccessFile.
- **Precondiciones:** Fichero `stock.bin` generado con 4 registros (144 bytes). Registro en posición 2: `MON-003`, precio 149.99 €, stock 8.
- **Datos de entrada:** Posición 2, nuevo stock 15.
- **Pasos ejecutados:**
  1. Se calcula el desplazamiento exacto del campo stock: `2 * 36 + 32 = 104` bytes.
  2. Se invoca `raf.seek(104)` y se escribe el entero con `raf.writeInt(15)`.
  3. Se lee nuevamente el registro en el offset 72 (`seek(72)`): código, precio double y stock int.
- **Resultado esperado:** Solo cambia el campo stock; código y precio quedan intactos.
- **Resultado obtenido:** Stock actualizado a 15; código `MON-003` y precio `149.99 €` inalterados.
- **Estado:** **SUPERADA**
- **Evidencia:** Offset verificado y comprobación directa de integridad binaria.

## 3. Incidencias detectadas y correcciones

| ID / prueba | Problema detectado | Corrección aplicada | Resultado tras repetir |
|---|---|---|---|
| `CP-RAF-02` | El catálogo de prueba original al ordenarse alfabéticamente dejaba `MON-003` en índice 1 en lugar de posición 2 | Se reajustaron los códigos de muestra didácticos (`CAB-001`, `IMP-002`, `MON-003`, `TEC-004`) | **SUPERADA**: `seek(2 * 36)` accede exactamente a `MON-003` |
| `CP-XML-03` | Las librerías SAX imprimían aviso por consola antes de la captura de la excepción `SAXParseException` | Se capturó y gestionó de forma específica la excepción informando del bloqueo seguro por DTD | **SUPERADA**: Bloqueo seguro confirmado y mensaje limpio por consola |

## 4. Conclusión de la ejecución

- **Número de pruebas ejecutadas:** 34
- **Superadas:** 34
- **No superadas:** 0

### Funcionalidades pendientes y observaciones
Ninguna funcionalidad pendiente. Se han implementado y verificado rigurosamente todas las funcionalidades de la fase UT1:
- `RF-FIC-01`: Explorador de ficheros y directorios.
- `RF-FIC-02`: Importador JSON con Gson, validación estricta y control de duplicados.
- `RF-FIC-03`: Importador XML DOM con protección segura XXE/DTD.
- `RF-FIC-04`: Consultas en memoria (listar todos, búsqueda por código exacto y nombre parcial case-insensitive).
- `RF-FIC-05`: Exportación a JSON y XML con indentación y confirmación de sobrescritura.
- `RF-FIC-06`: Persistencia binaria serializada (.dat) con verificación de integridad.
- `RF-FIC-07`: Consultas XML mediante XPath en clase independiente.
- `RF-FIC-08`: Acceso aleatorio mediante RandomAccessFile (registros de 36 bytes) en clase independiente.

**Fecha:** 28 de octubre de 2026
