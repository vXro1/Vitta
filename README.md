# Vitta — Android (Kotlin + Jetpack Compose)

Vitta es una aplicación de hábitos y gamificación: registra hábitos diarios
(agua, yoga, lectura, caminar…), premia la constancia con VitaPuntos, rachas
e insignias, y acompaña todo con una mascota ilustrada.

Este proyecto es la **migración del maquetado HTML/CSS/JS** (carpeta
`../prototipo/Vitta App.html`) a un proyecto nativo de Android Studio. El
maquetado no se modificó ni se borró, pero desde octubre de 2026 la app
ya no lo sigue al pie de la letra: la tipografía (Nunito) y los iconos
(Icons8) se renovaron (ver README_ESTILOS.md y README_RECURSOS.md). La
paleta es la de la marca (verde oliva, amarillo, verde oscuro, terracota y
marfil; ver README_COLORES.md).

> **¿Vas a desarrollar o probar la app?** Empieza por
> **[README_DESARROLLO.md](README_DESARROLLO.md)**: qué cambió, cómo
> correrla contra el backend, usuarios de prueba, arquitectura,
> navegación, reglas de código y checklist de pruebas.

## Pantallas (noviembre 2026)

Todas usan datos reales del backend (`../Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n`).

| Pantalla | Dónde está | Qué hace |
|---|---|---|
| Onboarding, Login, Registro | Al abrir sin sesión | Sesión con JWT |
| Selección y configuración de hábitos | Inicio → agregar | Hábitos predefinidos o personalizados, meta, frecuencia y hora del recordatorio |
| Inicio y Hoy | Barra inferior | Checklist del día, avance parcial, notas, puntos y racha que se actualizan al marcar |
| Detalle de hábito | Tocar un hábito | Avance, racha y registros del hábito; editar |
| Progreso | Barra inferior | Gráficas semanal/mensual y mapa de calor |
| Logros | Barra inferior | Las 14 medallas con su avance |
| Perfil (Yo) | Barra inferior | Nivel, VitaPuntos, estadísticas y fecha de registro |
| Cuenta | Yo → Cuenta | Cambiar nombre, correo y contraseña |
| Mis hábitos | Yo → Mis hábitos | Activos y archivados; archivar y restaurar |
| Bitácora (diario) | Yo → Bitácora | Notas por hábito y generales |
| Notificaciones | Yo → Notificaciones | Recordatorios (cambiar hora, pausar, borrar) e historial de puntos y medallas |
| Ayuda | Yo → Ayuda | Cómo crear hábitos, avance parcial, puntos y logros |

Los recordatorios llegan como notificación del teléfono (AlarmManager) aunque la app esté cerrada.

## Tecnologías

- **Kotlin** + **Jetpack Compose** (interfaz 100% declarativa; XML solo
  donde Android lo exige: manifest, theme puente, recursos de fuente/ícono).
- **Material 3**, personalizado con los colores/tipografía/formas de Vitta.
- **Coil** (`coil-compose` + `coil-svg`) para renderizar los SVG ricos de
  íconos e insignias sin perder detalle (ver `README_RECURSOS.md`).
- **Navigation Compose** (`navigation/VittaNavHost.kt`).
- **Retrofit + OkHttp + Gson** contra la API REST del backend.
- **DataStore** para guardar la sesión (token, nombre y correo).
- Gradle con **version catalog** (`gradle/libs.versions.toml`).

## Estructura

```text
Vita App/
├── app/src/main/
│   ├── java/com/vitta/app/
│   │   ├── MainActivity.kt
│   │   ├── VittaApplication.kt        (registra el decoder SVG de Coil)
│   │   ├── navigation/                (VittaNavHost)
│   │   ├── data/                      (local: sesión · remote: API · repository · mock)
│   │   └── ui/
│   │       ├── theme/                 (Color, Type, Shape, Dimensions, Theme)
│   │       ├── components/            (brand, buttons, cards, habits,
│   │       │                           progress, store, mascot, forms,
│   │       │                           navigation, feedback, common)
│   │       └── screens/               (auth, habits, home, profile, onboarding, design)
│   ├── assets/vitta/                  (logo, wordmark, SVG de íconos e insignias)
│   └── res/                           (fuentes, drawables PNG, XML de Android)
├── README.md                          (este archivo)
├── README_DESARROLLO.md               (guía para desarrolladores + usuarios de prueba)
├── README_INSTALACION.md
├── README_ESTILOS.md
├── README_COLORES.md
├── README_COMPONENTES.md
└── README_RECURSOS.md
```

## Cómo ejecutar

### 1. Levantar el backend

En el repo del backend (detalles en su README):

```bash
cp .env.example .env     # poner DATABASE_URL, JWT_SECRET y NODE_ENV=development
npm install
npm run migrate          # tablas + 14 logros + plantillas
npm run seed             # opcional: usuarios demo
npm run dev              # http://localhost:3000
```

### 2. URL del servidor

La URL sale de `BuildConfig.BASE_URL` (se arma en `app/build.gradle.kts`). Se cambia en
`local.properties` (no se sube a git) o con `-P` al compilar:

| Build | Propiedad | Por defecto |
|---|---|---|
| debug | `vitta.debugBaseUrl` | `http://10.0.2.2:3000/api/` (emulador) |
| release | `vitta.baseUrl` | `https://CAMBIAR-URL/api/` (hay que cambiarla) |

Ejemplo de `local.properties` para probar en un celular conectado al mismo wifi que el PC:

```properties
sdk.dir=C:/Users/TU-USUARIO/AppData/Local/Android/Sdk
vitta.debugBaseUrl=http://192.168.1.10:3000/api/
```

(la IP es la del PC, se ve con `ipconfig`). En debug se permite HTTP; en release solo HTTPS.

### 3. Compilar e instalar

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # con un emulador o celular conectado
```

O abrir la carpeta en Android Studio y presionar Run ▶ sobre `app`.

Para instalar el APK a mano en un celular: copiar el `.apk`, abrirlo y permitir
"instalar apps de origen desconocido". El APK de release firmado se explica en
**README_INSTALACION.md** (sección 7).

### Cuenta demo (solo debug)

El botón "Entrar con la cuenta demo" solo aparece en debug y si en `local.properties`
está `vitta.demoPassword=...` (la contraseña no está en el código). La primera vez crea
`demo@vitta.test` con esa contraseña y le carga hábitos de ejemplo. En release el botón no existe.

Requisitos y problemas comunes: **README_INSTALACION.md**.
