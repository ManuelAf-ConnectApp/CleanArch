# PROMPT MAESTRO — RESTAURANTAPP

**Proyecto:** RestaurantApp  
**Plataformas:** Android e iOS (teléfonos móviles)  
**Tecnología principal:** Kotlin Multiplatform (KMP)  
**Naturaleza:** Open Source, licencia Apache License 2.0 (sujeta a compatibilidad de la plantilla y dependencias)  
**Tipo de producto:** MVP educativo y funcional de descubrimiento y reserva de restaurantes con API y pagos simulados  
**Destinatario:** Claude, Codex u otro agente de programación con acceso al repositorio

---

## 0. Rol, misión y forma de trabajo

Actúa como **Principal Software Engineer, Mobile Software Architect y especialista en Kotlin Multiplatform**, con experiencia práctica en Android, iOS, Arquitectura Limpia, SOLID, Clean Code, UX móvil, seguridad de aplicaciones y pruebas automatizadas.

Tu misión es **analizar el repositorio existente y evolucionar la plantilla RestaurantApp** hasta crear **RestaurantApp**, una aplicación móvil Open Source que permita descubrir restaurantes, consultar su información y disponibilidad simulada, realizar reservas ficticias, consultar/cancelar reservas y administrar el perfil.

No te limites a generar pantallas estáticas: implementa un **flujo integral ejecutable** con modelos, estados de interfaz, validaciones, persistencia apropiada, errores, datos de demostración, navegación y pruebas. El objetivo es servir como **base reutilizable y didáctica**, fácil de adaptar posteriormente a servicios reales.

### Reglas de ejecución

1. **Inspecciona antes de modificar:** analiza estructura, versiones, módulos, dependencias, UI, navegación, DI y convenciones de la plantilla. **No inventes** archivos o capacidades inexistentes.
2. **Preserva lo útil:** reutiliza la arquitectura base cuando sea correcta, evitando reescrituras innecesarias. Justifica las desviaciones.
3. **Renombra completamente** la identidad del producto de `RestaurantApp`/`RestaurantApp`/variantes a `RestaurantApp`, preservando únicamente atribuciones legales y referencias históricas obligatorias. No hagas reemplazos ciegos que rompan librerías, nombres de terceros o rutas externas.
4. **Distingue claramente simulación y producción:** no existe un backend de reservas ni una pasarela de cobro real. Nunca presentes pagos o confirmaciones mock como transacciones comerciales reales.
5. **Prioriza un MVP fiable** frente a arquitectura excesiva. No añadas funcionalidades no solicitadas si incrementan mucho el alcance.
6. Sigue la tecnología y arquitectura reales del repositorio; cuando haya decisiones no especificadas, selecciona la alternativa KMP más mantenible, explica brevemente por qué y documenta los trade-offs.
7. Implementa de forma incremental. Tras cada módulo, compila y ejecuta las pruebas que permita el entorno. No afirmes que una compilación o test ha pasado si no lo has ejecutado.
8. No solicites confirmación por decisiones técnicas menores; documenta suposiciones razonables. Para bloqueos inevitables (credenciales, certificados de Apple, claves de terceros), informa del impedimento y continúa con lo que pueda verificarse.
9. Entrega código real y ejecutable, no pseudocódigo, donde se solicite implementación. Evita TODOs en el camino crítico del MVP.
10. Redacta la documentación principal en inglés y su equivalente en español.

---

## 1. Objetivo y alcance funcional

RestaurantApp será una aplicación para **Android e iOS** que facilita:

- Registro, inicio de sesión simulado y gestión local de sesión.
- Descubrimiento de restaurantes cercanos (si se autoriza la ubicación) o por ciudad.
- Búsqueda por nombre y filtrado por categoría gastronómica.
- Consulta de ficha, precios, políticas de cancelación y disponibilidad **ficticia pero coherente**.
- Reserva con fecha, hora y número de comensales.
- Selección de un método de pago **de demostración**.
- Procesamiento mock con éxito, rechazo o fallo de red configurables.
- Confirmación de reserva, historial, próximos eventos y cancelación simulada.
- Perfil y ajustes de privacidad/permisos.

### Fuera de alcance del MVP

No desarrollar: backend real, transacciones bancarias, registro de comercios reales, verificación de restaurantes, sincronización entre dispositivos, envío de emails/SMS reales, programa de fidelización, cupones complejos, críticas públicas, pedidos a domicilio ni facturación fiscal automatizada. Diseñar interfaces y límites arquitectónicos que permitan incorporarlos más adelante, sin fingir que ya existen.

---

## 2. Stack y arquitectura

### 2.1 Kotlin Multiplatform

- Compartir preferentemente **dominio, casos de uso, repositorios, modelos, validaciones, lógica de reservas y gestión de errores** en `commonMain`.
- Elegir **Compose Multiplatform** para UI compartida si la plantilla y sus versiones lo permiten de forma estable; si la base utiliza pantallas nativas, respetar inicialmente dicha estrategia y explicar la elección.
- Evitar acoplar código común a `android.*`, `UIKit` o SDKs de una sola plataforma.
- Implementar adaptadores específicos de plataforma para permisos, localización, preferencias seguras, notificaciones, enlaces externos y configuración del sistema.
- Utilizar **coroutines** y `Flow`/`StateFlow` para operaciones asíncronas y estado, de acuerdo con el stack disponible.
- Elegir un mecanismo de DI coherente con el repositorio (por ejemplo Koin o inyección manual); no introducir frameworks redundantes.
- Para almacenamiento local evaluar SQLDelight/Room KMP o solución ya existente. Para preferencias no sensibles, utilizar una abstracción de almacenamiento multiplataforma; los secretos/tokens de demostración, si se guardan, deben tener una implementación segura por plataforma.
- Los nombres exactos de las librerías y sus versiones se fijarán **tras inspeccionar el repositorio y confirmar compatibilidad**.

### 2.2 Arquitectura Limpia

Separar claramente:

1. **Presentation:** pantallas, componentes, navegación, ViewModels/presenters, estados y eventos.
2. **Domain:** entidades, reglas de negocio, value objects, casos de uso e interfaces de repositorios.
3. **Data:** implementaciones de repositorio, fuentes local/remota mock, DTOs, mappers, caché y persistencia.
4. **Platform:** adaptadores Android/iOS para permisos, ubicación, accesos a ajustes y seguridad.

Dependencias dirigidas hacia el dominio; evitar lógica de negocio dentro de composables/vistas. Aplicar SOLID, separación de responsabilidades y API cohesionada. No usar patrones innecesarios para operaciones triviales.

### 2.3 Estructura orientativa

```text
RestaurantApp/
├── androidApp/
├── iosApp/
├── shared/
│   └── src/
│       ├── commonMain/kotlin/.../
│       │   ├── core/                # Resultados, errores, utilidades y validaciones
│       │   ├── domain/              # Modelos, interfaces y casos de uso
│       │   ├── data/                # Repositorios, mock API, DTOs y persistencia
│       │   ├── feature/             # Auth, discovery, booking, profile, settings
│       │   └── design/              # Componentes y tokens de diseño compartidos
│       ├── commonTest/kotlin/.../
│       ├── androidMain/kotlin/.../
│       └── iosMain/kotlin/.../
├── docs/
├── screenshots/
├── README.md
├── README.es.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE
├── NOTICE
└── ...                         # Archivos del build real de la plantilla
```

**Adaptar la estructura a la plantilla existente:** este árbol es un objetivo lógico, no una orden de mover archivos indiscriminadamente.

---

## 3. Renombrado integral de RestaurantApp a RestaurantApp

Auditar, según proceda:

- `rootProject.name`, módulos, nombres de artefacto y recursos de build.
- Nombre visible, cadenas de interfaz, Splash, iconos, pantalla «Acerca de».
- Namespaces/paquetes Kotlin y rutas físicas correspondientes.
- Identificadores de aplicación Android, bundle ID iOS, manifest, Info.plist y esquemas.
- Configuraciones Gradle/Kotlin, targets, imports, documentación y pruebas.
- Identificadores de preferencias/caché y nombres de base de datos, cuidando posibles migraciones.
- Pipeline CI y referencias internas bajo control del proyecto.

**Criterio de aceptación:** ninguna aparición residual de la marca anterior en código o UI propia, salvo referencias necesarias para crédito legal o documentación histórica. Comprobar por búsqueda textual y compilación; reportar excepciones.

---

## 4. Diseño, accesibilidad y navegación

Crear una identidad visual moderna, cercana y profesional, basada en Material Design como referencia conceptual, adaptada a las convenciones de iOS donde proceda.

- Interfaz mobile-first y responsive para distintos tamaños de teléfono, orientaciones y escalado de fuente.
- Estados explícitos: **loading, contenido, vacío, error, éxito y sin conexión**.
- Componentes reutilizables: `RestaurantCard`, `SearchBar`, `CategoryChip`, `PrimaryButton`, `PriceSummary`, `FormField`, `PermissionExplanation`, `EmptyState`, `ErrorState`, `BookingStatusChip`.
- Contraste suficiente, etiquetas accesibles, tamaños táctiles adecuados, foco/VoiceOver/TalkBack y mensajes de error comprensibles.
- No bloquear tareas básicas por ausencia de permisos de ubicación o notificaciones.
- Idioma por defecto español; preparar recursos para inglés con localización de textos, fechas, importes y plurales.
- Navegación consistente con retroceso y restauración razonable del estado.

### Barra inferior personalizada

Visible únicamente en las áreas principales autenticadas:

1. **Inicio**.
2. **Reservas**.
3. **Perfil**.
4. **Ajustes**.

Ocultarla en Splash, Login, Registro, Recuperación, flujo de pago y confirmación cuando sea conveniente. Mantener una sola instancia lógica y estado seleccionado; no duplicar stacks innecesariamente.

---

## 5. Pantallas y comportamientos

### 5.1 Splash

- Mostrar icono/logotipo y texto **RestaurantApp**.
- Duración visual objetivo: **3 segundos**, pero evitar demoras artificiales adicionales cuando el sistema ya haya mostrado una Splash nativa; no bloquear tareas ni navegación indefinidamente.
- Comprobar si existe sesión de demostración válida y dirigir a Inicio o Login.
- Manejar fallo de inicialización con salida controlada.

### 5.2 Login (`feature/auth/login`)

- Email y contraseña con validación y errores por campo.
- Mostrar/ocultar contraseña, indicador de carga y opción **Mantener la sesión iniciada**.
- Accesos visibles a **Registrarse** y **Olvidé mi contraseña**.
- Inicio de sesión contra repositorio de autenticación simulado; errores diferenciados: usuario no existe, contraseña incorrecta, timeout, indisponibilidad del servicio y error inesperado.
- No revelar en producción información que facilite enumerar cuentas; en modo demo utilizar mensajes neutrales y documentar comportamiento.
- Con «mantener sesión» habilitado, persistir de forma apropiada la sesión simulada; deshabilitado, no conservarla tras el cierre completo de la aplicación según las capacidades del entorno. Cierre de sesión manual en Ajustes.

### 5.3 Registro (`feature/auth/register`)

Campos:

- Nombre y apellidos (obligatorios, espacios normalizados y límites razonables).
- Email obligatorio, formato válido y sin duplicados en el repositorio mock.
- Teléfono opcional, con validación de formato y prefijo internacional cuando se introduzca.
- Ciudad obligatoria, con texto normalizado.
- Provincia seleccionable entre las **50 provincias españolas**; considerar **Ceuta y Melilla** como ciudades autónomas diferenciadas para una experiencia completa. Definir coherencia entre ciudad y provincia cuando sea posible sin depender de un servicio externo.
- Contraseña con mostrar/ocultar y normas explícitas (mínimo de longitud y comprobación de fortaleza; no imponer reglas arbitrarias que perjudiquen la usabilidad sin justificación).
- Repetir contraseña: debe coincidir.
- Aceptación explícita de términos y política de privacidad de demostración si corresponde; no utilizar consentimientos ficticios como sustituto de textos legales reales.

Tras crear usuario, registrar cuenta mock, iniciar sesión y navegar a Inicio. Guardar contraseñas **solo mediante un hash adecuado con salt** si se usa almacenamiento local; nunca en texto plano, logs o repositorio. Explicar que la autenticación local es didáctica y **no sustituye a un servidor de identidad** para producción.

### 5.4 Recuperación de contraseña

- Solicitar email y ofrecer flujo **totalmente simulado** de código de recuperación con caducidad, intentos limitados y validación.
- No afirmar que se ha enviado un correo si no existe un servicio real; en modo demo ofrecer un código de prueba claramente identificado, visible únicamente en una sección de demostración controlada.
- Restablecer contraseña únicamente tras validar el código; invalidar códigos usados.
- Documentar la implementación real futura (servidor, canal verificado, rate limiting, anti-enumeración).

### 5.5 Inicio (`feature/discovery/home`)

**Permisos:**

- Explicar para qué se usarán ubicación y notificaciones mediante una pantalla/bloque contextual antes de solicitar permiso del sistema; pedirlos cuando sean necesarios y de acuerdo con las políticas de Android/iOS.
- Si acepta ubicación, buscar restaurantes a partir de coordenadas simuladas o las autorizadas, sin mantener seguimiento continuo.
- Si rechaza ubicación, ofrecer por defecto los restaurantes de **su ciudad registrada**, con opción de cambiarla manualmente.
- Si rechaza notificaciones, la aplicación sigue funcionando; mostrar opción de abrir ajustes del sistema, sin prometer que puede cambiar el permiso directamente.
- Si se bloquea el permiso definitivamente, ofrecer ruta informativa a Ajustes del sistema.

**Contenido:**

- Barra de búsqueda por nombre de restaurante (y opcionalmente ciudad), con debounce si corresponde.
- Chips/tabs desplazables para **Todos, Italiana, Española, Asiática, Mexicana, Vegetariana**, etc., según la semilla mock.
- Listado vertical de tarjetas: nombre, fotografía local/de demostración, categoría, ciudad/distancia **solo si es calculable**, intervalo de precio, descripción corta y disponibilidad indicativa.
- Ordenado relevante y estado vacío con acción para borrar filtros.
- Al seleccionar una tarjeta, navegar a **Detalle de Restaurante**.
- No inventar valoraciones verificadas, restaurantes reales ni distancias exactas cuando los datos son ficticios.

### 5.6 Detalle de restaurante (`feature/restaurant/detail`)

Mostrar:

- Nombre, imágenes autorizadas, descripción de 4–5 líneas, tipo de cocina, dirección/ciudad y servicios relevantes.
- **Precio de reserva por persona**: aclarar expresamente si es importe de anticipo, cargo fijo de reserva o precio estimado; para este MVP usar **tarifa simulada de reserva por comensal**, distinta del consumo en restaurante.
- Política de cancelación gratuita (por ejemplo, hasta N horas/días antes de la reserva), con interpretación inequívoca según la **hora local del establecimiento**.
- Selector de **fecha** (nunca fechas pasadas), **hora disponible** y **número de comensales** (indispensables aunque no apareciesen en la descripción inicial).
- No habilitar días/horarios sin disponibilidad de demostración; gestionar cambios de fecha y zona horaria.
- Precio calculado como `tarifaPorPersona × comensales`, con desglose transparente y moneda EUR; usar tipo decimal apropiado o unidades enteras de céntimos, **no Float/Double para importes monetarios**.
- Disponibilidad de la franja horaria consultada al repositorio mock.
- Bloque de elección de método de pago con selección única:
  - **Tarjeta (demo)**.
  - **PayPal (demo)**.
  - **Google Pay (demo)**.
  - **Apple Pay (demo)**.
- Mostrar Google Pay/Apple Pay solo cuando corresponda a plataforma/capacidad, o señalarlos claramente como opciones exclusivamente simuladas; nunca invocar SDKs reales en el MVP.
- Botón **Realizar reserva** con validación final, precio y política de cancelación previamente visibles.

### 5.7 Tratamiento especial del pago con tarjeta (seguridad obligatoria)

**No solicitar, almacenar, transmitir ni registrar números reales de tarjeta, CVV o fecha de caducidad.** Aunque el borrador inicial incluya estos campos, en una aplicación demo sin procesador certificado es innecesario y crea un riesgo evitable.

En su lugar:

- Presentar una tarjeta ficticia visual (por ejemplo «Tarjeta de prueba •••• 4242») y un aviso explícito **«Simulación: no se realizará ningún cobro»**.
- Opcionalmente permitir seleccionar un **escenario de tarjeta ficticia** (éxito/rechazo) sin escribir datos financieros.
- Si en el futuro se incorporan pagos reales, delegar íntegramente captura y tokenización en SDKs/componentes de un proveedor habilitado; nunca manejar PAN/CVV en código propio.
- Los otros métodos serán igualmente **simulaciones de selección**, sin autenticaciones OAuth ni wallets reales.

### 5.8 Procesando pago (`feature/payment/processing`)

- Indicador de progreso con texto **«Procesando pago de demostración…»**.
- Ejecutar `MockPaymentGateway` configurable; simular latencia razonable sin congelar UI.
- Escenarios: aprobación, rechazo, timeout, pérdida de conectividad y respuesta inesperada.
- Deshabilitar dobles pulsaciones y utilizar identificador de idempotencia para evitar reservas duplicadas en reintentos.
- Si falla: pantalla informativa con **Reintentar** o **Volver al detalle**, sin crear una reserva confirmada.
- No permitir que navegación/rotación duplique el cargo simulado.

### 5.9 Confirmación (`feature/booking/confirmation`)

Tras la **aprobación simulada** y la confirmación del repositorio de reservas:

- Mensaje visible: **«Reserva de demostración confirmada»**.
- Identificador único de reserva, restaurante, fecha, hora local, número de personas, titular, método de pago mock, importe y referencia de la política de cancelación.
- Mostrar **fecha/hora límite de cancelación gratuita** calculada según política y zona horaria del restaurante.
- Estado «Confirmada (demo)» y nota de que **no existe reserva real** en el establecimiento.
- Botones **Ver mis reservas** y **Volver a inicio**.
- Persistir la reserva localmente para que aparezca en Reservas tras reiniciar cuando se implemente persistencia.

### 5.10 Reservas (`feature/booking/list`)

Añadir esta pantalla, necesaria para la barra inferior ya solicitada:

- Dos vistas: **Próximas** e **Historial** (confirmadas, canceladas y pasadas).
- Datos clave de cada reserva, filtros básicos y acceso al detalle.
- Cancelación simulada con diálogo de confirmación, cálculo explícito de coste/cancelación gratuita según política, y actualización de estado.
- Si la reserva es cancelada, no debe volver a aparecer como activa. En el MVP la **devolución, si procede, es ficticia**; no afirmar que se ha procesado un reembolso bancario.
- Tratar cambios de disponibilidad y conflictos mock con mensajes y acciones recuperables.

### 5.11 Perfil (`feature/profile`)

Permitir editar:

- NIF/NIE opcional, con validación apropiada cuando se proporcione.
- Nombre y apellidos.
- Dirección y código postal.
- Ciudad/provincia y teléfono (si estaban registrados).
- Email visible pero **no editable** en el MVP.

Añadir los datos de perfil para una **eventual solicitud de factura al establecimiento**, pero **no generar facturas reales ni afirmar obligaciones fiscales cumplidas**. Validar cambios y reflejarlos en la sesión/persistencia correspondiente.

### 5.12 Ajustes (`feature/settings`)

Incluir:

- Estado de permisos de ubicación y notificaciones, explicación de su utilidad y enlace a ajustes del sistema.
- Preferencia de ciudad/ubicación manual para el descubrimiento.
- Idioma (si hay recursos disponibles) y preferencias de accesibilidad pertinentes.
- Tema claro/oscuro o seguir el sistema, si la plantilla lo soporta.
- **Mantener sesión** (estado informativo o cambio efectivo según diseño), **Cerrar sesión** y **Eliminar datos de demostración** con confirmación.
- Número de versión/build.
- **Acerca de RestaurantApp**: propósito educativo, repositorio, licencia, atribuciones y advertencia de simulación.
- Enlaces a política de privacidad de ejemplo y condiciones; indicar que deben adaptarse antes de distribuir una app comercial.
- Opción para restablecer datos mock de prueba, ideal para desarrolladores.

---

## 6. Modelo de dominio mínimo

Definir entidades y value objects con invariantes, IDs estables y una separación clara entre DTO y modelo de dominio:

- `User`: ID, nombre, apellidos, email, teléfono, ciudad, provincia, hash de contraseña de demo y datos de perfil.
- `Restaurant`: ID, nombre, cocina/categoría, descripción, ciudad, provincia, localización opcional, tarifa de reserva, moneda, imágenes y política de cancelación.
- `RestaurantAvailability`: restaurante, fecha, zona horaria, franjas y plazas disponibles simuladas.
- `Booking`: ID, usuario, restaurante, fecha/hora local o instante inequívoco, número de comensales, importe, estado, método mock, timestamps y política aplicada.
- `CancellationPolicy`: plazo de cancelación y forma de cálculo del coste.
- `PaymentAttempt`: ID, reserva candidata, escenario, estado, referencia mock e idempotency key.
- `Session`: identidad autenticada, persistencia deseada, caducidad/estado.
- Enumeraciones o tipos sellados para `PaymentMethod`, `PaymentStatus`, `BookingStatus`, `AppError` y `PermissionStatus`.

### Reglas fundamentales

1. No permitir reservas anteriores al momento actual del restaurante.
2. Toda reserva requiere al menos un comensal y una franja válida disponible.
3. Verificar disponibilidad **otra vez al confirmar**, para simular cambios concurrentes.
4. No confirmar reservas cuando el pago mock falla o la respuesta es indeterminada; resolver el estado idempotentemente.
5. Evitar reservas duplicadas por doble clic o reintentos.
6. Importes calculados de manera determinista con céntimos enteros o decimal exacto.
7. Una reserva cancelada no puede volver a confirmarse por error.
8. Las políticas de cancelación deben quedar asociadas a la reserva en el momento de su creación.
9. Usar fechas y zonas horarias correctamente: fecha del restaurante y timestamps absolutos para auditoría.

---

## 7. API externa simulada (requisito central)

Diseñar una capa de red intercambiable con **interfaces de repositorio**. Puede implementarse mediante un adaptador `MockApiClient` que reproduzca semántica HTTP sin acceso a Internet; opcionalmente utilizar Ktor MockEngine si la plantilla ya emplea Ktor y ello simplifica la migración.

### Endpoints conceptuales

| Método | Ruta conceptual | Función |
|---|---|---|
| POST | `/auth/register` | Alta ficticia |
| POST | `/auth/login` | Autenticación ficticia |
| POST | `/auth/recover` | Inicio de recuperación mock |
| GET | `/restaurants` | Búsqueda, filtros, ciudad y paginación |
| GET | `/restaurants/{id}` | Detalle |
| GET | `/restaurants/{id}/availability` | Fechas/horas/plazas |
| POST | `/payments/simulate` | Pago ficticio |
| POST | `/bookings` | Crear reserva ficticia |
| GET | `/bookings` | Reservas por usuario |
| POST | `/bookings/{id}/cancel` | Cancelación ficticia |
| GET/PUT | `/users/me` | Perfil |

### Escenarios de simulación

- Respuesta `200/201` satisfactoria con JSON/DTO consistente.
- `400/422`: validación de entrada.
- `401/403`: sesión no válida.
- `404`: recurso inexistente.
- `409`: fecha/hora sin plazas o conflicto de reserva.
- `429`: límite simulado de solicitudes.
- `500/503`: error del servidor o servicio no disponible.
- Timeout y desconexión, con retry solo cuando sea seguro.
- Latencia configurable y datasets deterministas para test.

**Requisito:** crear un pequeño **panel/ajuste de desarrollador (oculto o limitado a builds debug)** que permita elegir escenario (`success`, `timeout`, `unauthorized`, `conflict`, `server_error`, `payment_declined`) y latencia, para verificar todos los estados sin modificar código fuente.

### Persistencia

- Seed local con **restaurantes completamente ficticios** y fotografías con licencia compatible o recursos originales.
- Persistencia local de usuario mock, sesión, reservas, perfil y preferencias; documentar si los datos se comparten o no entre instalaciones.
- La API mock no debe aparentar garantizar reservas globales reales ni disponibilidad sincronizada con establecimientos.

---

## 8. Seguridad y privacidad — nivel educativo intermedio

Implementar salvaguardas realistas y documentadas, no una falsa promesa de «seguridad garantizada»:

- Validación de entradas en UI y dominio; no confiar solo en el cliente cuando exista backend futuro.
- No almacenar contraseñas en claro ni en logs. Si hay usuarios locales mock, aplicar derivación de contraseña con sal y parámetros razonables utilizando una solución apropiada para plataformas soportadas.
- Reducir persistencia de credenciales/sesión; usar Keychain iOS y Android Keystore/almacenamiento seguro cuando corresponda.
- Evitar incluir secretos, tokens reales o claves privadas en repositorio o recursos de la app.
- Nunca manejar ni persistir datos bancarios reales; no integrar una pantalla que acepte PAN/CVV en la demo.
- Manejo de errores sin exposición de trazas o datos personales en UI.
- Permisos mínimos: ubicación solo cuando sea necesaria; notificaciones opt-in. No acceso a ubicación en segundo plano.
- No enviar coordenadas ni datos personales a servicios reales; datos de ejemplo sin PII auténtica.
- No enviar telemetría ni analítica de terceros por defecto.
- Definir una política razonable de eliminación de datos locales y qué ocurre al cerrar sesión.
- Documentar límites de seguridad de una autenticación simulada y checklist de endurecimiento para producción (backend, auth server, TLS, rate limiting, controles antifraude, pasarela de pago, privacidad/RGPD, pruebas de seguridad).

---

## 9. Calidad y pruebas obligatorias

### Pruebas de dominio (`commonTest`)

- Registro con campos válidos e inválidos; emails duplicados.
- Login válido/incorrecto, mantener sesión y logout.
- Validación de fechas anteriores, horarios pasados y franjas sin aforo.
- Cálculo de importe con distintos comensales, formato en EUR y precisión monetaria.
- Cálculo del plazo de cancelación y zona horaria.
- Creación de reserva con éxito y fallo; no duplicación en retry.
- Cambios de estado (confirmada → cancelada), sin transiciones ilegales.
- Recuperación mock de contraseña con código erróneo, caducado y reutilizado.
- Filtros por categoría, ciudad y búsqueda sin resultados.

### Pruebas de UI/integración

- Navegación Splash → Login/Inicio según sesión.
- Formularios, errores por campo, carga, estados vacíos y reintentos.
- Permisos concedidos/denegados y fallback a ciudad.
- Pago mock éxito/rechazo/timeout y confirmación correcta.
- Reservas visibles tras reiniciar y cancelación confirmada.
- Bottom bar con selección y restauración de estado.

### Verificaciones técnicas

- Compilar targets que permita el entorno; informar si no se dispone de macOS/Xcode para iOS.
- Ejecutar pruebas en el entorno disponible y listar resultados exactos.
- Usar linters/análisis estático disponibles y revisar errores de compilación.
- Evitar bloqueos del hilo principal, fugas de recursos y estados inconsistentes.
- No inventar benchmarks o coberturas.

---

## 10. Documentación y licencia Open Source

En la raíz del repositorio, mantener como mínimo:

1. `README.md`: documentación completa en **inglés**.
2. `README.es.md`: equivalente fiel en **español**.
3. `LICENSE`: texto íntegro de **Apache License, Version 2.0** con avisos correctos.
4. `NOTICE`: atribuciones pertinentes, incluidos avisos exigidos por la plantilla original y terceros.
5. `CHANGELOG.md`: historial según Keep a Changelog o convención equivalente.
6. `CONTRIBUTING.md`: cómo desarrollar, probar y contribuir.
7. `.gitignore`: archivos generados, secretos y artefactos locales excluidos.

### Contenido obligatorio del README

- Qué es RestaurantApp y objetivos educativos/Open Source.
- Distinción **explícita** entre reservas/pagos simulados y producción.
- Capturas de pantalla reales del proyecto (cuando estén disponibles; no inventarlas).
- Stack, módulos y decisiones de arquitectura.
- Requisitos de desarrollo (JDK, Android SDK, Xcode/macOS para iOS, etc., según versiones reales).
- Instalación, build, pruebas, ejecución en Android e iOS.
- Cuentas/datos mock para la demostración sin contraseñas sensibles.
- Configuración del panel debug y escenarios de error.
- Flujo de reserva y política de cancelación ficticia.
- Seguridad, privacidad, limitaciones y ruta hacia backend/pagos reales.
- Licencia, atribuciones y contribución.

**Importante:** verificar que el código/activos de la plantilla son compatibles con Apache 2.0; no sustituir licencias de terceros por la propia ni borrar avisos legales obligatorios.

---

## 11. Roadmap de implementación

### Fase 0 — Auditoría y adaptación

- Inspeccionar el repositorio.
- Identificar stack real, necesidades de refactor y riesgos.
- Diseñar navegación, estado global y modelos.
- Renombrar RestaurantApp → RestaurantApp cuidadosamente.
- Entregable: proyecto que conserva la capacidad de compilar y lista de decisiones técnicas.

### Fase 1 — Fundamentos y autenticación

- Splash, Login, Registro y Recuperación mock.
- Repositorios, errores tipados, sesión y persistencia.
- Pruebas de validación y navegación.

### Fase 2 — Descubrimiento

- Permisos, ciudad alternativa, filtros, búsqueda y cards.
- Datos mock de restaurantes y pantalla de detalle.
- Estados de error, loading, vacío y sin conexión.

### Fase 3 — Reserva y pagos simulados

- Fecha/hora/comensales, disponibilidad, política de cancelación y precio.
- Selección de métodos y pasarela simulada segura.
- Confirmación, idempotencia y escenarios de error.

### Fase 4 — Reservas, perfil y ajustes

- Próximas/historial/cancelación.
- Perfil editable, ajustes de permisos, idioma/tema si procede y logout.
- Persistencia y pruebas de flujos completos.

### Fase 5 — Calidad y entrega Open Source

- Compilaciones, pruebas, refactor, accesibilidad y documentación.
- Auditoría final de renombrado y licencias.
- Preparar release de MVP sin afirmaciones falsas sobre reservas y pagos reales.

Cada fase debe cerrar con **qué se implementó, archivos modificados, comandos ejecutados, pruebas que pasaron/no pudieron ejecutarse y pendientes reales**.

---

## 12. Criterios de aceptación global

El MVP se considerará completo cuando:

- [ ] El proyecto se identifica como **RestaurantApp** en código, interfaz y configuración propia.
- [ ] Existen builds Android y la configuración iOS válida, con límites del entorno claramente indicados.
- [ ] Registro, login, recuperación y sesión persistente mock funcionan.
- [ ] Se puede descubrir un restaurante por ubicación autorizada o ciudad alternativa.
- [ ] Búsqueda y categorías filtran datos coherentemente.
- [ ] Los detalles muestran precio, cancelación, fecha, hora y comensales.
- [ ] Se bloquean fechas/horas anteriores y franjas no disponibles.
- [ ] El importe se calcula correctamente y no usa coma flotante inexacta para dinero.
- [ ] Se simula el pago sin recopilar datos de tarjetas reales.
- [ ] Hay confirmación solo tras éxito mock, con identificador y datos íntegros.
- [ ] Las reservas aparecen en Reservas y pueden cancelarse según su política.
- [ ] Se pueden editar perfil y ajustes y cerrar sesión.
- [ ] Se han contemplado errores de red, timeout, 401, 409, 5xx y rechazo de pago.
- [ ] El usuario puede usar la aplicación sin conceder ubicación o notificaciones.
- [ ] La bottom bar funciona en Inicio, Reservas, Perfil y Ajustes.
- [ ] Existen pruebas relevantes ejecutadas cuando el entorno lo permite.
- [ ] README en inglés/español, CHANGELOG, CONTRIBUTING, LICENSE y NOTICE son correctos.
- [ ] Todas las referencias a la plantilla anterior se han auditado.
- [ ] La interfaz señala claramente que pagos y reservas son **de demostración**.

---

## 13. Formato exigido de tu respuesta y ejecución inicial

Cuando recibas este prompt **junto con el repositorio**, actúa en este orden:

1. **Auditoría inicial:** resume la estructura actual, el stack detectado, las dependencias y los obstáculos.
2. **Decisiones de arquitectura:** explica organización propuesta, flujo de datos, localización, permisos, persistencia y estrategia de mock. Proporciona un diagrama Mermaid sencillo.
3. **Mapa de pantallas y navegación:** especifica rutas, principales estados y los ViewModels/casos de uso.
4. **Modelo de datos:** muestra entidades y contratos de repositorios, con ejemplos Kotlin compilables.
5. **Plan por fases:** orden de trabajo, prioridades, riesgos y criterios verificables.
6. **Implementación efectiva:** comienza por **Fase 0 + Fase 1**, modificando realmente el repositorio si dispones de acceso; continúa en fases cuando sea posible sin sacrificar calidad.
7. **Verificación:** proporciona comandos utilizados, resultados reales, tests pendientes y bloqueos del entorno.
8. **Informe final:** lista de archivos creados/modificados y funcionalidades completadas; separa lo implementado de lo pendiente.

Si **no** tienes acceso al repositorio, no simules haberlo inspeccionado ni modificado: entrega una arquitectura de referencia y solicita el código únicamente cuando sea imprescindible para proceder con cambios concretos.

**Objetivo final:** construir una base KMP Open Source **mantenible, didáctica, segura dentro de su alcance, fácilmente extensible y funcional sin servicios externos**, que permita a terceros reemplazar el mock por una API real sin reescribir el dominio ni la interfaz de usuario.
