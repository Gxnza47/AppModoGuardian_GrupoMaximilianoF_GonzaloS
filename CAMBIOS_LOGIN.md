# Resumen de cambios

- Gradle: Retrofit, Gson, coroutines, material-icons-extended, Kotest, MockK, JUnit 5 (+ vintage para LoginTest); permiso INTERNET.
- Nuevo tema oscuro (paleta Insercode adaptada) con modo claro desde Perfil.
- Nuevos modelos: Evento, Severidad, TipoEvento, EstadoCamara, Turno, estados de UI; capa data (Post, ApiService, RetrofitInstance, PostRepository).
- Nuevos ViewModels: Post, Alerta, Ajustes; PerfilViewModel ahora persiste avatar por usuario.
- Navegación única en AppIntegrada con barra inferior por rol; Rol.puedeAcceder / puedeEditarEventos.
- Eliminada la sección «Guías» completa.
- Tests: LoginTest actualizado; nuevos PostViewModelTest, PostRepositoryTest, EventoYAlertasTest, PostScreenTest.
