# Integración frontend — Valentina + Verónica

Estado: **28 de octubre de 2026**. Rama: `veronica`.

Esta rama junta en una sola versión de la app:

1. Los cambios de **Valentina** (rama `valenfront`): tareas de frontend que
   dependían de lo que entregó Stiven en el backend (puntos 2, 3, 8 y 12).
2. Las tareas de **Verónica**: frontend de lo que entregó Bryan en el
   backend (puntos 1, 4, 6 y 9).
3. Las mejoras de experiencia de usuario que ya estaban en `feature/vitta-ux`
   (paleta de marca, navegación, demo, mapa de calor, íconos en un tono).

Backend usado: rama `main` del repositorio del backend, commit `58dbf0a`
(PR #2 "auditoría integral: logros automáticos, zona horaria, progreso,
recordatorios y perfil").

> La app **compila** (`assembleDebug`), pero esta versión todavía **no se
> probó en el emulador**. Antes de probarla hay que actualizar la base de
> datos local (ver "Cómo probar").

---

## 1. Qué aportó cada rama y qué se conservó

| Pantalla / tema | De Valentina | De la rama UX | Resultado |
| --- | --- | --- | --- |
| **Inicio** | Avance parcial, aviso "Racha terminada", modal "¡nuevo día de racha!", celebración de medalla | Colores oliva, desmarcar con confirmación, sin retos/tienda de ejemplo, snackbar con puntos | Base UX + las tres piezas de Valentina + notas |
| **Hoy** | Checklist por momento del día (mañana/tarde/noche según la hora del recordatorio) | — | Pantalla de Valentina + desmarcar, notas e íconos en un tono |
| **Progreso** | Totales por día con `GET /api/progress` | Mapa de calor interactivo con la lista de hábitos de cada día | Las dos cosas + la nota de cada hábito en el detalle del día |
| **Logros** | Pestaña propia con el avance de cada medalla | — | Pantalla de Valentina |
| **Perfil** | Tarjeta de nivel, estadísticas reales, "Contigo desde…", Ayuda | Enlace "Mis logros" | Pantalla de Valentina + enlace |
| **Crear / editar hábito** | Guarda `meta_valor`, `unidad`, `icono`, `categoria` y el recordatorio | Selector de ícono, eliminar/archivar | Las dos cosas |
| **Navegación** | Pantalla de Ayuda | Inicio según sesión, vuelta a Login con token vencido, atrás → Inicio, barra con indicador | Las dos cosas |

Archivos que se quitaron por estar duplicados: `ui/screens/today/TodayScreen.kt`
(queda `ui/screens/home/TodayScreen.kt`), `ui/screens/profile/AchievementsScreen.kt`
(queda `ui/screens/achievements/AchievementsScreen.kt`), `GamificationApi`,
`GamificationDto` y `GamificationRepository` (quedan `UserApi`, `UserDto` y
`UserRepository`) y `DashboardExtras.kt` (retos y tienda de ejemplo, el
backend no los tiene).

---

## 2. Tareas de Verónica

### Tarea 1 — Base de datos actualizada (Logros y Perfil)

- Logros (`GET /api/logros`) y Perfil (`GET /api/users/me`,
  `GET /api/dashboard`) ya leen del backend nuevo.
- En el backend: `npm run migrate` (aplica `schema.sql` y `catalogos.sql`,
  se puede correr varias veces) y opcionalmente `npm run seed`.

### Tarea 4 — Zona horaria

- `data/remote/ApiClient.kt` envía `X-Timezone` con la zona del teléfono
  (nombre IANA, p. ej. `America/Bogota`). Si el servidor no la reconoce,
  deja de enviarla y usa la del backend.
- "Hoy" es el día local en Inicio, Hoy, Progreso y en la cuenta demo (antes
  se usaba UTC y después de las 7 p. m. contaba como el día siguiente).
- Los registros usan el campo `dia` que devuelve el backend.

### Tarea 6 — Ícono y categoría del hábito

- Al crear y al editar se envían `icono` (clave del catálogo, p. ej. `water`)
  y `categoria` (área del ícono), además de `meta_valor` y `unidad`.
- La app muestra el ícono del backend en todas las pantallas.
  `HabitIconStore.sync()` lo copia al teléfono para Progreso. Los hábitos
  antiguos sin `icono` usan el ícono deducido del nombre.

### Tarea 9 — Notas del día

- Inicio y Hoy: cuando ya registraste algo hoy aparece **"Agregar nota del
  día"**. Abre un diálogo con una pregunta guía según el hábito ("¿Qué
  leíste hoy?", "¿Cómo te sentiste después de meditar?"…), con un límite de
  1000 caracteres. Si ya hay nota, se ve en la tarjeta y se puede editar o
  borrar.
- Se guarda con `PATCH /api/habits/:id/progreso` (`{valor, notas}`) sin
  cambiar el avance del día. Los hábitos de sí/no quedan con `meta_valor = 1`
  para que esa ruta funcione; en pantalla no cambia nada.
- Progreso: al tocar un día del mapa de calor se ve la nota de cada hábito.

---

## 3. Otros cambios por el backend nuevo

- **Desmarcar**: tocar el check de un hábito hecho pide confirmación y usa
  `DELETE /api/habits/:id/records/:registroId` (retira los 10 VitaPuntos y
  recalcula la racha).
- **Eliminar / archivar**: `DELETE /api/habits/:id` borra el hábito si no
  tiene registros y lo **archiva** si tiene historial. La pantalla de
  edición lo sabe antes de que toques: muestra "Archivar hábito" o
  "Eliminar hábito" y lo explica en la confirmación.
- **Recordatorio**: la sección "¿A qué hora?" volvió a Crear, Configurar y
  Editar. La hora se guarda en `/api/recordatorios` y ordena la pestaña Hoy.
  Las notificaciones todavía no existen.

---

## 4. Endpoints que usa la app

| Pantalla | Endpoints |
| --- | --- |
| Login / Registro / Demo | `POST /api/auth/login`, `POST /api/auth/register` |
| Inicio y Hoy | `GET /api/habits`, `GET /api/habits/:id/records`, `GET /api/habits/:id/streak`, `GET /api/dashboard`, `GET /api/recordatorios`, `POST /api/habits/:id/records`, `PATCH /api/habits/:id/progreso`, `DELETE /api/habits/:id/records/:registroId`, `GET /api/logros` |
| Progreso | `GET /api/progress`, `GET /api/habits`, `GET /api/habits/:id/records`, `GET /api/habits/:id/streak` |
| Logros | `GET /api/logros` |
| Perfil | `GET /api/users/me`, `GET /api/dashboard` |
| Crear / editar | `POST /api/habits`, `GET/PUT/DELETE /api/habits/:id`, `POST/PATCH /api/recordatorios` |

---

## 5. Cómo probar

```bash
# Backend (repositorio Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n, rama main)
docker start vitta-db
npm install
npm run migrate      # obligatorio con el backend nuevo
npm run dev          # http://localhost:3000

# App
git checkout veronica
./gradlew assembleDebug   # en Windows: gradlew.bat, con JAVA_HOME del JDK de Android Studio
```

En el login, **"Entrar con la cuenta demo"** crea `demo@vitta.test`
(contraseña `Vitta2026!`) con 4 hábitos y unas semanas de historial.

Checklist sugerido:

- [ ] Sin sesión abre la introducción; con sesión entra directo a Inicio.
- [ ] Marcar un hábito: +10 VitaPuntos, sube la racha y aparece el modal del día de racha.
- [ ] Ganar una medalla muestra la celebración y luego aparece en Logros.
- [ ] Avance parcial: marcar 3 de 8 vasos, cerrar la app y volver: siguen los 3.
- [ ] Desmarcar un hábito devuelve los puntos y la racha.
- [ ] Agregar, editar y borrar una nota; verla en Progreso al tocar el día.
- [ ] Crear un hábito con ícono y hora: se ve en Hoy en el momento del día correcto.
- [ ] Editar un hábito con historial muestra "Archivar"; uno nuevo muestra "Eliminar".
- [ ] Después de las 7 p. m. un registro cuenta para hoy, no para mañana.

---

## 6. Pendiente (tareas de Yiner y siguientes)

- Notificaciones locales de los recordatorios (WorkManager/AlarmManager y
  permiso `POST_NOTIFICATIONS`).
- Pantalla para editar perfil y contraseña (`PATCH /api/users/me` y
  `PATCH /api/users/me/password` ya existen en el backend).
- Lista de hábitos archivados con opción de restaurar
  (`POST /api/habits/:id/restore`).
