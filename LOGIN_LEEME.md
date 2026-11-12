# Modo Guardián – Guía del proyecto

App Android (Kotlin + Jetpack Compose, MVVM + StateFlow) de monitoreo de seguridad. Se eliminó la sección interna «Guías» y se implementó la funcionalidad de las guías 12–15 dentro de la app real.

## Abrir

Abrir la carpeta en Android Studio, JDK 17, **Sync Gradle** y ejecutar `app` (API 24+, compileSdk 35). Este código no pudo compilarse en el entorno de preparación: si Gradle muestra algún error, revisar primero imports.

## Usuarios ficticios (contraseña 123456)

| Cuenta | Rol | Acceso |
| --- | --- | --- |
| operador@guardian.test | Operador | Inicio, Cámaras, Alertas, Perfil |
| supervisor@guardian.test | Supervisor | Lo anterior + Eventos (historial) |
| admin@guardian.test | Admin | Lo anterior + editar/eliminar eventos (PUT/DELETE) |

## Pantallas

Login (con «Crear cuenta» → Registro/Resumen), Inicio (estado general, ModoGuardianCard, cámaras, eventos en vivo, reportar incidencia), Cámaras y detalle, Alertas y detalle de evento (plazo de 2 min, confirmar / falsa alarma / escalar, llamar a central, foto de evidencia), Eventos (búsqueda, filtros, exportar reporte), Reportar incidencia (POST + foto), Perfil (avatar cámara/galería, preferencias en DataStore, cerrar sesión).

## Guías implementadas

- Guía 12: estado y animaciones (`ModoGuardianCard`).
- Guía 13: cámara y galería (`SelectorImagen`, `ImagenInteligente`, FileProvider).
- Guía 14: Retrofit + JSONPlaceholder (GET/POST/PUT/DELETE) con repositorio y manejo de errores. Los posts se mapean a eventos de seguridad (userId → cámara/zona, id → tipo/severidad).
- Guía 15: pruebas con Kotest + MockK (`src/test`) y Compose UI Test (`src/androidTest/PostScreenTest.kt`).

## Eliminado

`MenuPrincipal`, `HomeScreen*`, `PantallaInicio/Perfil/Configuracion/Principal`, `NavegacionGuia10`, `AppNavigation` y las rutas guia9–guia13.

## Notas

- `telefono_central` en `strings.xml` es un número de ejemplo; reemplazar. Solo abre el marcador.
- Interruptores de notificaciones/alertas críticas solo guardan preferencia.
- Resoluciones de alertas y evidencias viven en memoria; las incidencias creadas son locales (la API falsa no las persiste).
- «Crear cuenta» no crea cuenta en servidor; «Exportar reporte» comparte texto; el visor de cámara es simulado; Google/Apple login no implementado.
- Ejecutar tests: `./gradlew test` y `./gradlew connectedAndroidTest`.
