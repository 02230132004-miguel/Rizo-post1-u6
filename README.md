# Post-contenido — Unidad 6: Diagnóstico y Refactorización de Antipatrones

**Estudiante:** Miguel Angel Rizo Arias  
**Materia / Módulo:** Arquitectura de Software y Patrones de Diseño  
**Stack Tecnológico:** Java 17, Spring Boot 3.2.3, Spring JDBC (`JdbcTemplate`), H2 Database (En Memoria), JUnit 5  

---

## 1. Estructura del Proyecto por Carpetas (Parte 1 y Parte 2)

El código fuente y las pruebas están organizados explícitamente en dos carpetas y paquetes principales:

```
src/main/java/com/tienda/pedidos/
├── PedidosApplication.java
│
├── dto/                                    # DTOs compartidos
│   ├── ItemPedido.java
│   ├── PedidoRequest.java
│   └── ResultadoPedido.java
│
├── parte1/                                 # PARTE 1: Refactorización Inicial
│   ├── validacion/                         # Cadena de Responsabilidad (Fail-Fast)
│   │   ├── ContextoPedido.java
│   │   ├── ValidadorPedido.java
│   │   ├── ValidadorStock.java             # 1° eslabón: inventario H2
│   │   └── ValidadorCliente.java           # 2° eslabón: cliente y mora < 20:00
│   ├── descuento/                          # Patrón Strategy (Descuentos de Cliente)
│   │   ├── EstrategiaDescuento.java
│   │   ├── DescuentoVip.java               # Escala 15%, 10%, 5%
│   │   ├── DescuentoFrecuente.java         # Escala 8% (> 10 pedidos), 4%
│   │   ├── DescuentoEstandar.java          # 0.0
│   │   └── SelectorEstrategiaDescuento.java
│   └── service/                            # Persistencia, Notificación y Orquestador
│       ├── EmailService.java
│       ├── EmailServiceImpl.java
│       ├── PedidoRepository.java           # Spring JDBC @Transactional
│       ├── NotificacionPedidoService.java
│       └── GestorPedidos.java              # Orquestador Parte 1
│
└── parte2/                                 # PARTE 2: Diagnóstico Golden Hammer y Corrección
    ├── descuento/                          # Estrategias de Promoción Desacopladas
    │   ├── DescuentoBlackFriday.java       # 25% (promo.black-friday.activa)
    │   ├── DescuentoCorporativo.java       # 10% (clientes con NIT)
    │   ├── DescuentoVolumen.java           # 12% (pedidos > 20 unidades)
    │   └── CalculadorDescuentoFinal.java   # Combina cliente y promociones con Math.max
    └── service/
        └── GestorPedidos.java              # Orquestador Parte 2 (Anti-Lava Flow)

src/test/java/com/tienda/pedidos/
├── parte1/
│   └── GestorPedidosParte1Test.java        # Pruebas automatizadas de la Parte 1
└── parte2/
    └── GestorPedidosParte2Test.java        # Pruebas automatizadas de la Parte 2
```

---

## 2. Instrucciones de Ejecución y Pruebas

### Requisitos Previos
- **Java Development Kit (JDK):** Versión 17 o superior.
- **Apache Maven:** Versión 3.8+ o 3.9+.

### Comandos de Compilación y Ejecución

1. **Ejecutar la suite completa de pruebas unitarias y de integración:**
   ```bash
   mvn clean test
   ```

2. **Empaquetar la aplicación en archivo JAR ejecutable:**
   ```bash
   mvn clean package
   ```

3. **Iniciar la aplicación Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```

4. **Acceso a la consola interactiva H2:**
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:pedidos_db`
   - Usuario: `sa`
   - Contraseña: *(vacía)*

---

## 3. Parte 1 — Diagnóstico y Refactorización de `GestorPedidos`

### 3.1. Diagnóstico de Antipatrones en el Diseño Legacy

El diseño original de `GestorPedidos` concentraba toda la lógica del sistema en un único método masivo:

#### A. God Object (Objeto Todopoderoso)
La clase acumulaba **6 responsabilidades disímiles**, violando el principio de Responsabilidad Única (**SRP**):
1. **Validación de Inventario:** Consultaba directamente la base de datos para verificar disponibilidad física.
2. **Validación de Cliente y Mora:** Verificaba existencia y reglas temporales de facturas pendientes.
3. **Cálculo de Precios:** Acumulaba el subtotal de cada ítem del pedido.
4. **Lógica de Descuentos:** Contenía reglas condicionales para clientes VIP, Frecuentes y Estándar.
5. **Persistencia de Datos:** Manejaba conexiones JDBC, inserciones en `pedidos`, `detalle_pedido` y actualización del `inventario`.
6. **Notificaciones:** Generaba el texto del correo electrónico y llamaba al despachador de correos.

*Cita de código conceptual del antipatrón legacy:*
```java
// ANTIPATRÓN GOD OBJECT: Múltiples responsabilidades acopladas en un solo método
public Resultado procesar(PedidoRequest req) {
    // 1. Stock
    int stock = jdbc.queryForObject("SELECT stock FROM inventario WHERE id = " + req.getProdId(), Integer.class);
    if (stock < req.getCantidad()) return rechazar("Sin stock");
    // 2. Cliente y Mora
    String tipo = jdbc.queryForObject("SELECT tipo FROM clientes WHERE id = " + req.getClienteId(), String.class);
    int facturas = jdbc.queryForObject("SELECT COUNT(*) FROM facturas WHERE pagada = FALSE AND cliente_id = " + req.getClienteId(), Integer.class);
    if (facturas > 0 && LocalTime.now().isBefore(LocalTime.of(20, 0))) return rechazar("Moroso");
    // 3. Precios y Descuentos
    double subtotal = req.getCantidad() * req.getPrecio();
    double desc = tipo.equals("VIP") ? (subtotal > 1000000 ? 0.15 : 0.10) : 0.0;
    // 4. Persistencia y Envío de Correo...
}
```

#### B. Spaghetti Code (Código Espagueti)
- **Estructuras anidadas profundas:** Condicionales `if-else` de más de 3 niveles de profundidad donde un error en una condición intermedia requería manipular variables de estado booleanas en cascada.
- **SQL disperso y concatenado:** Sentencias SQL embebidas directamente entre el flujo de control, dificultando el mantenimiento y la auditoría.
- **Falta de cohesión:** Imposibilidad de probar la lógica de descuentos o validaciones de manera unitaria y aislada.

### 3.2. Solución Aplicada en la Carpeta `parte1/`

1. **Validaciones con Chain of Responsibility (`com.tienda.pedidos.parte1.validacion`):**
   - `ContextoPedido`: Encapsula la solicitud, tipo de cliente, subtotal y estado de rechazo.
   - `ValidadorPedido`: Clase base abstracta con `encadenar()` y método plantilla `validar()`.
   - `ValidadorStock` (1° eslabón): Valida existencias en H2 con **corte anticipado (*fail-fast*)**.
   - `ValidadorCliente` (2° eslabón): Valida existencia en BD y mora de facturas en horario restringido (< 20:00 h).

2. **Descuentos con Strategy (`com.tienda.pedidos.parte1.descuento`):**
   - `EstrategiaDescuento`: Interfaz común `double calcular(ContextoPedido contexto)`.
   - `DescuentoVip`: Escala 15% (> $1M), 10% (> $500k), 5% (base).
   - `DescuentoFrecuente`: Consulta pedidos previos en BD (8% > 10 pedidos, 4% > 3 pedidos).
   - `DescuentoEstandar`: Retorna 0.0.
   - `SelectorEstrategiaDescuento`: Factoría que mapea por tipo de cliente.

3. **Persistencia y Notificación (`com.tienda.pedidos.parte1.service`):**
   - `PedidoRepository`: Maneja operaciones JDBC con `@Transactional` (inserta pedido, detalle y decrementa stock).
   - `NotificacionPedidoService` & `EmailService`: Desacoplan el envío de correo del flujo de negocio.

---

## 4. Parte 2 — Diagnóstico y Corrección de Golden Hammer y Lava Flow

### 4.1. Diagnóstico del Antipatrón Golden Hammer

```
+-----------------------------------------------------------------------------------------+
|                  ANTIPATRÓN DETECTADO: GOLDEN HAMMER (MARTILLO DE ORO)                   |
+-----------------------------------------------------------------------------------------+
| Síntoma: Forzar las promociones (Black Friday, Corporativo, Volumen) como eslabones     |
| de la Cadena de Responsabilidad (ValidadorPedido).                                      |
|                                                                                         |
| ¿Por qué es un error?                                                                   |
| 1. Semántica incorrecta: Una validación determina si un pedido DEBE RECHAZARSE.         |
|    Las promociones son BENEFICIOS COMERCIALES cuantitativos que nunca rechazan un pedido|
| 2. Ausencia de orden estricto: Las promociones no dependen secuencialmente unas de otras|
| 3. Regla de agregación: Las promociones requieren una política de selección (Math.max), |
|    lo cual es ajeno al flujo booleano de una cadena de eslabones.                       |
+-----------------------------------------------------------------------------------------+
```

### 4.2. Solución Aplicada en la Carpeta `parte2/`

1. **Estrategias de Promoción (`com.tienda.pedidos.parte2.descuento`):**
   - `DescuentoBlackFriday`: Retorna 25% si `${promo.black-friday.activa}` es true.
   - `DescuentoCorporativo`: Consulta la BD H2 y otorga 10% si el cliente posee NIT.
   - `DescuentoVolumen`: Otorga 12% si la suma de unidades del pedido supera 20 unidades.

2. **Calculador de Descuento Final (`CalculadorDescuentoFinal`):**
   - Inyecta las estrategias de cliente y las promociones disponibles.
   - Evalúa cada una y selecciona el mayor descuento aplicable mediante `Math.max(...)`.

3. **Orquestador Limpio (`com.tienda.pedidos.parte2.service.GestorPedidos`):**
   - Encadena **únicamente** `ValidadorStock` y `ValidadorCliente`.
   - Delega el cálculo tarifario a `CalculadorDescuentoFinal`.
   - Persiste a través de `PedidoRepository` y notifica con `NotificacionPedidoService`.

4. **Prevención de Lava Flow:**
   - No se dejaron clases obsoletas de promociones dentro de `validacion`.
   - Se eliminó cualquier vestigio de campos como `descuentoCampana` dentro de los validadores.

---

## 5. Matriz de Pruebas Automatizadas

| Suite | Clase de Prueba | Escenarios Evaluados | Estado |
|---|---|---|---|
| **Parte 1** | `GestorPedidosParte1Test` | - Rechazo por stock insuficiente (fail-fast)<br>- Rechazo por cliente inexistente<br>- Rechazo por mora (< 20:00 h)<br>- Escalas de descuento VIP (15%, 10%, 5%)<br>- Descuento frecuente (> 10 pedidos -> 8%) | **5/5 Aprobados** |
| **Parte 2** | `GestorPedidosParte2Test` | - Promoción Black Friday (25%)<br>- Promoción Corporativo con NIT (10%)<br>- Promoción por Volumen (> 20 uds -> 12%)<br>- Persistencia integral y decremento de stock en H2 | **4/4 Aprobados** |

**Total:** 9 pruebas automatizadas ejecutadas y aprobadas al 100%.

---

## 6. Conclusiones y Lecciones de Arquitectura

1. **Separación de Responsabilidades (SRP):** Cada clase tiene un único motivo de cambio, facilitando el mantenimiento y la extensibilidad.
2. **Principio Abierto/Cerrado (OCP):** Nuevas promociones o tipos de clientes se integran creando una nueva clase que implemente `EstrategiaDescuento`, sin modificar el código existente.
3. **Selección Adecuada de Patrones:** Chain of Responsibility para validaciones secuenciales con fail-fast; Strategy para algoritmos de descuento intercambiables.
4. **Higiene Arquitectónica:** Evitar el Golden Hammer y el Lava Flow mantiene la base de código limpia, modular y fácil de auditar.