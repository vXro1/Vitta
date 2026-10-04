# Guía para desarrolladores — Vitta Android

Estado: **octubre de 2026**. Este documento resume lo que cambió en la app,
cómo correrla contra el backend local, con qué usuarios probarla y qué
reglas seguir al tocar el código. Los detalles de cada tema están en los
README específicos (enlazados en cada sección).

---

## 1. Qué cambió en esta versión

| Área | Cambio |
|---|---|
| Autenticación | Login y Registro rediseñados: textos siempre legibles, burbujas animadas (respetan "quitar animaciones"), botón de regreso, lista de requisitos de contraseña en vivo |
| Tipografía | **Nunito en toda la app** (reemplaza a Caprasimo + Figtree) |
| Paleta | Verde esmeralda, naranja, ámbar y fondos blanco-menta; se eliminó el beige grisáceo. Contrastes verificados (WCAG ≥ 4.5:1) |
| Tema | Siempre claro (`VittaTheme(useDarkTheme = false)`): el modo oscuro del sistema producía texto claro sobre fondo claro |
| Iconos | 166 iconos de **Icons8** (estilo "Color") con catálogo, categorías y búsqueda |
| Crear hábito | Flujo en 5 pasos: nombre → icono → meta (cantidad + unidad) → frecuencia → recordatorio (selector de hora Material 3) |
| Editar hábito | Pantalla nueva, usa `PUT /api/habits/:id` (ya existía en el backend) |
| Inicio | Dashboard con progreso del día, rachas, hábitos de hoy y de otros días |
| Yo | Perfil, estadísticas, créditos de Icons8, cerrar sesión con confirmación |
| Navegación | Barra inferior Inicio / Yo; se eliminaron rutas registradas dos veces |
| Validación | "Elige al menos un día" (antes el backend respondía 400 con la frecuencia vacía) |

---

## 2. Arranque rápido

### Backend (obligatorio para casi todas las pantallas)

Repositorio hermano: `../Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n`
(Node + Express + PostgreSQL).

**Base de datos (PostgreSQL en Docker, puerto 5433).** Requiere Docker
Desktop abierto. El contenedor `vitta-db` guarda los datos en el volumen
`vitta_postgres_data` y se reinicia solo con Docker.

```bash
# Primera vez (usa el usuario/contraseña/base de DATABASE_URL del .env):
docker run -d --name vitta-db --restart unless-stopped \
  -e POSTGRES_USER=vitta -e POSTGRES_PASSWORD=<la del .env> -e POSTGRES_DB=vitta \
  -p 5433:5432 -v vitta_postgres_data:/var/lib/postgresql/data postgres:16-alpine
docker exec -i vitta-db psql -U vitta -d vitta < src/db/schema.sql

# Días siguientes (si Docker estaba cerrado):
docker start vitta-db
```

> Si el login muestra **"Error del servidor. Intenta de nuevo."** y la API
> responde `{"error":"error interno"}`, casi siempre es la base: revisa que
> Docker Desktop esté abierto y `docker ps` muestre `vitta-db`.
>
> Historia: hasta el 2026-10-04 la base se levantaba con un contenedor
> temporal que se perdió al cerrarse Docker (junto con sus datos). Ese día
> se creó `vitta-db` con volumen persistente y se recrearon los usuarios de
> prueba de la sección 3.

**API:**

```bash
cd ../Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n
cp .env.example .env          # ajustar DATABASE_URL y JWT_SECRET
npm install
npm run dev                   # http://localhost:3000/api
```

La app apunta a `http://10.0.2.2:3000/api/` (`data/remote/ApiClient.kt`),
que es el `localhost` de tu PC visto desde el **emulador**. En un
dispositivo físico cambia esa URL por la IP de tu PC en la red local.

### App

```bash
# Windows: si "JAVA_HOME is not set", usa el JDK que trae Android Studio
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

./gradlew :app:assembleDebug
./gradlew :app:installDebug      # con un emulador/dispositivo conectado
```

Requisitos y problemas comunes: **README_INSTALACION.md**.

---

## 3. Usuarios de prueba

Creados en la **base de datos local** de desarrollo (`vitta-db`) el
2026-10-04. No existen en otros entornos: si tu BD está vacía, créalos con
el comando de abajo.

| Nombre | Correo | Contraseña | Estado | Úsalo para probar |
|---|---|---|---|---|
| Prueba Claude | `prueba.claude@vitta.test` | `Prueba123!` | Sin hábitos | Primer uso: pantalla "Empecemos por uno" → selección de hábitos → configuración |
| Prueba Dos | `prueba2.claude@vitta.test` | `Prueba1234` | 2 hábitos | Inicio con datos, edición, sección Yo, cerrar sesión |

Hábitos de **Prueba Dos**:

| id | Nombre | meta | frecuencia | Nota |
|---|---|---|---|---|
| 1 | Estirar | `10 minutos al día` | `Diaria` | Creado con el formato anterior de unidad (con "al día"); sirve para comprobar compatibilidad |
| 2 | Meditar | `10 veces` | `L,X,D` | Solo aparece en "Hábitos de hoy" lunes, miércoles y domingo; los demás días va en "Otros días" |

> Son credenciales **solo de desarrollo**. No reutilizar en ningún entorno
> compartido ni en producción.

### Crear más usuarios de prueba

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Prueba Tres","correo":"prueba3@vitta.test","password":"Prueba1234"}'
```

El backend solo exige contraseña de mínimo 8 caracteres. La lista de
requisitos del Registro (mayúscula, minúscula, número, carácter especial)
es **solo visual**: hoy ni la app ni el backend los obligan.

### Consultar los hábitos de un usuario

```bash
TOKEN=$(curl -s http://localhost:3000/api/auth/login -H "Content-Type: application/json" \
  -d '{"correo":"prueba2.claude@vitta.test","password":"Prueba1234"}' | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
curl -s http://localhost:3000/api/habits -H "Authorization: Bearer $TOKEN"
```

---

## 4. Arquitectura

Capas: **pantalla (Compose) → ViewModel (StateFlow) → Repository → Retrofit → backend**.
El token JWT y los datos básicos del usuario viven en DataStore (`TokenManager`).

```text
app/src/main/java/com/vitta/app/
├── MainActivity.kt                 (edge-to-edge con iconos de barra oscuros)
├── navigation/VittaNavHost.kt      (todas las rutas)
├── data/
│   ├── local/TokenManager.kt       (token, nombre y correo del usuario)
│   ├── remote/                     (ApiClient, AuthApi, HabitApi, DTOs)
│   ├── repository/                 (AuthRepository, HabitRepository)
│   └── mock/PredefinedHabits.kt    (catálogo de hábitos predefinidos + unidades sugeridas)
└── ui/
    ├── theme/                      (Color, Type = Nunito, Shape, Dimensions, Theme)
    ├── components/
    │   ├── auth/                   (scaffold, burbujas animadas, requisitos de contraseña)
    │   ├── icons/HabitIconCatalog.kt  (catálogo Icons8: categorías, búsqueda, icono por nombre)
    │   ├── buttons/ forms/ …       (componentes reutilizables)
    └── screens/
        ├── auth/                   (Login, Registro)
        ├── habits/                 (selección, configuración, crear, editar + HabitFormComponents)
        ├── home/                   (MainScreen con pestañas, ChecklistScreen = Inicio)
        └── profile/ProfileScreen.kt   (Yo)
```

### Archivos nuevos clave

| Archivo | Qué contiene |
|---|---|
| `ui/components/auth/VittaAuthComponents.kt` | Esqueleto de Login/Registro, burbujas animadas, banner de error, lista de requisitos |
| `ui/components/icons/HabitIconCatalog.kt` | `HabitIcon`, `HabitIcons.all`, `featured`, `search()`, `forHabitName()` |
| `ui/screens/habits/HabitFormComponents.kt` | Bloques del formulario: secciones numeradas, selector de icono y catálogo, meta, frecuencia, campo y diálogo de hora |
| `ui/screens/habits/HabitTextFormat.kt` | Cómo se arma y se lee `meta` y `frecuencia` (único lugar con ese formato) |
| `ui/screens/habits/EditHabitScreen.kt` / `EditHabitViewModel.kt` | Edición de hábito |
| `ui/screens/home/MainScreen.kt` | Barra inferior Inicio / Yo |
| `ui/screens/profile/ProfileScreen.kt` | Sección Yo |

---

## 5. Navegación

```text
Onboarding ──Omitir/Empezar──▶ Login ◀──── Registro (← vuelve a Login)
   ▲                             │
   └────── ← (vuelve) ───────────┘
                                 │ login/registro OK
                                 ▼
                             HomeGate (decide)
                 sin hábitos ┌───┴───┐ con hábitos
                             ▼       ▼
                  FirstHabitTeaser   Home [Inicio | Yo]
                             │       │  ├─ "Nuevo hábito" ─▶ HabitSelection
                             ▼       │  ├─ ✎ en una tarjeta ─▶ EditHabit (← vuelve y recarga)
                       HabitSelection│  └─ Yo → Cerrar sesión ─▶ Login (pila limpia)
                       │       │
              Continuar│       │Crear un hábito propio
                       ▼       ▼
                 HabitConfig  CustomHabit ──(si había predefinidos)──▶ HabitConfig
                       │       │
                       └──Guardar──▶ HomeGate (pila limpia)
```

Reglas:

- **Ninguna pantalla navega por sí sola al cambiar una selección.** Solo los
  botones (Continuar, Guardar, regreso) disparan navegación.
- Tras guardar hábitos se limpia la pila (`popUpTo(0)`): no se puede volver
  a un formulario ya guardado.
- Editar devuelve una señal a Home (`savedStateHandle["refresh_home"]`) para
  recargar la lista.
- `DesignSystemScreen` (catálogo de componentes) sigue en el código pero ya
  no tiene ruta: Home la reemplazó.

---

## 6. Datos de hábitos

El backend guarda los hábitos en la tabla `habitos` con solo estos campos:
`nombre`, `meta` (TEXT), `frecuencia` (TEXT) y `tipo`.

| Campo | Formato | Ejemplos |
|---|---|---|
| `meta` | `"<cantidad> <unidad>"` | `8 vasos`, `30 minutos`, `2 kilómetros` |
| `frecuencia` | `Diaria` o días separados por coma, en orden L→D | `Diaria`, `L,X,V` |

Siempre usa `HabitTextFormat` para leerlos y escribirlos. Las unidades
antiguas con "al día" (p. ej. `10 minutos al día`) se siguen leyendo bien.
Al editar, si el usuario no toca la frecuencia, se reenvía la original sin
cambios.

### Lo que el backend NO guarda (limitaciones)

| Dato | Comportamiento actual en la app |
|---|---|
| Hora del recordatorio | Se elige con el selector, pero **no se guarda**; la pantalla lo avisa. Tampoco existen notificaciones |
| Icono elegido | **No se guarda**; se deduce del nombre (`HabitIcons.forHabitName`). Si el usuario elige otro, la app avisa cuál verá |
| Perfil | No hay `GET/PUT /users/me`; Yo muestra nombre y correo guardados al iniciar sesión |

Tabla completa (problema → causa → endpoint → solución → impacto):
**README_RECURSOS.md → "Limitaciones conocidas (backend)"**.

---

## 7. Sistema visual: reglas para el código

- **Tipografía:** solo Nunito. Usa `VittaTextStyles.*` (o
  `MaterialTheme.typography`, que ya apunta a Nunito en todos sus estilos).
  No agregues otra `FontFamily`. Ver **README_ESTILOS.md**.
- **Colores:** solo `VittaColors.*` / `VittaColorRoles.*`. Nunca
  `Color(0xFF…)` ni `Color.White` en un Composable. Texto sobre verde
  primario → `VittaColorRoles.onPrimary`. No uses `textDisabled` para texto
  que deba leerse. Ver **README_COLORES.md**.
- **Fija los colores de texto de los componentes Material**
  (`OutlinedTextField`, diálogos, pickers): si se dejan los de Material,
  pueden salir claros sobre fondo claro.
- **Alineación:** centra con `contentAlignment` / `Arrangement` y tamaños
  con `weight` + `aspectRatio`, no con padding "a ojo".
- **Formularios de hábito:** reutiliza `HabitFormScaffold`,
  `HabitFormSection`, `HabitGoalInput`, `HabitFrequencySelector`,
  `HabitReminderTimeField` y `HabitIconPicker`.
- **Regreso:** `VittaBackButton` en todas las pantallas secundarias.

### Accesibilidad (mínimos)

- Áreas táctiles de 48 dp como mínimo (`VittaSizes.MinTouchTarget`).
- Iconos decorativos: `contentDescription = null`. Iconos de acción
  (editar, cerrar, +, −): descripción en español.
- Selección: `selectable` / `toggleable` con su `Role` (RadioButton,
  Checkbox, Tab) para que TalkBack anuncie el estado.
- Errores con icono + texto (no solo color).

---

## 8. Iconos (Icons8)

- Archivos: `res/drawable-nodpi/ic8_*.png` (PNG de 96 px, estilo "Color").
- **La llama de racha NO es de Icons8:** es la ilustración propia de Vitta
  (`assets/vitta/icons/icon_streak_flame.svg`, `VittaIconAssets.streakFlame`).
  Úsala siempre que se muestre una racha.
- Licencia gratuita: **atribución obligatoria** (ya está en Yo → "Iconos por
  Icons8"). **No se pueden modificar**: nada de `tint`, recolorear ni
  editarlos.
- Si la app se publica en Google Play, agregar "Iconos por Icons8
  (https://icons8.com)" en la descripción.

### Agregar un icono

1. Elige uno del **mismo estilo "Color"** en icons8.com (PNG de 96 px, plan
   gratuito). No mezcles estilos.
2. Guárdalo como `res/drawable-nodpi/ic8_<nombre_con_guiones_bajos>.png`.
3. Agrégalo a `HabitIcons.all` en `HabitIconCatalog.kt`, con categoría y
   palabras clave en minúscula y sin tildes (sirven para la búsqueda y para
   `forHabitName`).
4. Añádelo a la lista de "Iconos incluidos" en `README_RECURSOS.md`.

---

## 9. Checklist de pruebas manuales

Con el backend corriendo y los usuarios de la sección 3:

- [ ] **Login** con el teléfono en modo oscuro: todo el texto se lee (tema siempre claro).
- [ ] **Registro**: la lista de requisitos se marca al escribir; el botón ← vuelve a Login.
- [ ] **Login ←** vuelve a la introducción (no cierra la app).
- [ ] **Prueba Claude** → "Elegir mi primer hábito" → seleccionar y deseleccionar todo: **no navega**; "Continuar" queda deshabilitado y legible.
- [ ] **Crear hábito propio**: escribir "Meditar" sugiere el icono de meditación; "Más" abre el catálogo; buscar "agua" filtra.
- [ ] **Meta**: cambiar la cantidad (−/+ o escribiendo) y la unidad (chip o texto).
- [ ] **Frecuencia**: "Días específicos" sin días → "Elige al menos un día".
- [ ] **Hora**: "Cambiar" abre el selector; elegir 07:30 p. m. y verlo en el campo.
- [ ] **Prueba Dos** → editar "Meditar" → cambiar unidad → la tarjeta del Inicio se actualiza.
- [ ] **Yo** → "Cerrar sesión" → Cancelar no hace nada; confirmar lleva a Login; Atrás no vuelve a la app.
- [ ] Girar a horizontal en Login y en un formulario: el contenido se centra y se desplaza.

---

## 10. Emulador

- **RAM:** el AVD `Pixel_9` (Android 15 con Google Play) necesita al menos
  **3 GB** (`hw.ramSize=3072` en `D:\Android\avd\Pixel_9.avd\config.ini`;
  la copia original quedó en `config.ini.bak-2026-10-04`). Con 2 GB, Android
  cerraba procesos, la interfaz del sistema se reiniciaba y la app quedaba
  tapada por una pantalla negra o gris.
- Si aparece "System UI isn't responding" justo después de arrancar, pulsa
  **Esperar**: es el arranque en frío.
- Cierra lo que no uses (otros contenedores de Docker, otros emuladores):
  el PC tiene poca RAM libre con todo abierto.
- Si la pantalla queda negra al abrir: arranca en frío
  (`emulator -avd Pixel_9 -no-snapshot-load`).

### Trucos para automatizar con `adb`

- `adb shell input keyevent 111` (Escape) **escribe un "6"** en los campos
  de texto de este emulador. Para cerrar el teclado usa `keyevent 4` (Atrás).
- Abrir el emulador sin ventana:
  `emulator -avd Pixel_9 -no-window -no-snapshot-save -no-audio`.
- En Git Bash, antepón `MSYS_NO_PATHCONV=1` a los comandos `adb shell` que
  usan rutas como `/sdcard/...`.

---

## 11. Pendientes conocidos

- Backend: columnas `hora_recordatorio` e `icono` (y sus campos en los DTO de Android).
- Notificaciones locales para los recordatorios (WorkManager/AlarmManager).
- Endpoint de perfil (`/users/me`).
- Si se quieren exigir los requisitos de contraseña, validarlos en
  `RegisterViewModel` **y** en `validatePassword` del backend.
- La app siempre arranca en la introducción, aunque haya una sesión
  guardada (no hay inicio de sesión automático).
- Las pestañas Progreso, Logros y Tienda del maquetado aún no existen.

---

## Documentación relacionada

| Archivo | Tema |
|---|---|
| README.md | Visión general del proyecto |
| README_INSTALACION.md | Requisitos, JDK, SDK, emulador |
| README_ESTILOS.md | Tipografía (Nunito), espaciado, formas |
| README_COLORES.md | Paleta y contrastes |
| README_COMPONENTES.md | Componentes del sistema de diseño |
| README_RECURSOS.md | Iconos Icons8 (licencia y atribución), mascota, fuentes, limitaciones del backend |
