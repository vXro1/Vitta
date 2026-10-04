# Vitta — Android (Kotlin + Jetpack Compose)

Vitta es una aplicación de hábitos y gamificación: registra hábitos diarios
(agua, yoga, lectura, caminar…), premia la constancia con VitaPuntos, rachas
e insignias, y acompaña todo con una mascota ilustrada.

Este proyecto es la **migración del maquetado HTML/CSS/JS** (carpeta
`../prototipo/Vitta App.html`) a un proyecto nativo de Android Studio. El
maquetado no se modificó ni se borró, pero desde octubre de 2026 la app
ya no lo sigue al pie de la letra: la paleta, la tipografía (Nunito) y los
iconos (Icons8) se renovaron (ver README_COLORES.md y README_RECURSOS.md).

> **¿Vas a desarrollar o probar la app?** Empieza por
> **[README_DESARROLLO.md](README_DESARROLLO.md)**: qué cambió, cómo
> correrla contra el backend, usuarios de prueba, arquitectura,
> navegación, reglas de código y checklist de pruebas.

## Estado actual (octubre 2026)

La app ya no es solo el sistema de diseño: tiene pantallas reales
conectadas al backend (`../Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n`):

- Onboarding, Login y Registro (JWT).
- Selección de hábitos predefinidos, creación y **edición** de hábitos
  (meta con cantidad + unidad, frecuencia, selector de hora, catálogo de
  iconos de Icons8 con búsqueda).
- Inicio (dashboard con progreso, rachas y registro diario) y **Yo**
  (perfil y cerrar sesión).
- Tipografía **Nunito** en toda la app y paleta clara y viva.

Limitaciones del backend (hora del recordatorio e icono no se guardan,
sin endpoint de perfil): ver README_DESARROLLO.md §6.

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

Ver **README_INSTALACION.md** para el paso a paso completo (requisitos,
JDK, SDK, emulador, dispositivo físico y problemas comunes).

Resumen rápido:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # con un emulador/dispositivo conectado
```

O simplemente abrir la carpeta `Vita App/` en Android Studio y presionar
Run ▶ sobre el módulo `app`.
