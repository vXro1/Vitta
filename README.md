# Vitta — Android (Kotlin + Jetpack Compose)

Vitta es una aplicación de hábitos y gamificación: registra hábitos diarios
(agua, yoga, lectura, caminar…), premia la constancia con VitaPuntos, rachas
e insignias, y acompaña todo con una mascota ilustrada.

Este proyecto es la **migración del maquetado HTML/CSS/JS** (carpeta
`../prototipo/Vitta App.html`) a un proyecto nativo de Android Studio. El
maquetado HTML sigue siendo la fuente de verdad visual — no se modificó ni
se borró.

## Objetivo de esta etapa

Esta primera etapa entrega **frontend y sistema de diseño**, no la app
final:

- Un `Theme` de Compose fiel a los colores, tipografía, radios y sombras del
  maquetado.
- Una biblioteca de componentes reutilizables (botones, cards, hábitos,
  progreso, mascota, formularios, navegación, modales).
- Una pantalla `DesignSystemScreen` que cataloga todo lo anterior en una
  sola vista desplazable.
- Todos los recursos visuales (SVG de íconos/insignias, PNG de la mascota,
  tipografías Caprasimo/Figtree) migrados y organizados.

No incluye backend, autenticación real, base de datos ni lógica de negocio
— todo lo que se muestra usa datos mock (`data/mock/MockData.kt`).

## Tecnologías

- **Kotlin** + **Jetpack Compose** (interfaz 100% declarativa; XML solo
  donde Android lo exige: manifest, theme puente, recursos de fuente/ícono).
- **Material 3**, personalizado con los colores/tipografía/formas de Vitta.
- **Coil** (`coil-compose` + `coil-svg`) para renderizar los SVG ricos de
  íconos e insignias sin perder detalle (ver `README_RECURSOS.md`).
- **Navigation Compose**, con un `NavHost` mínimo listo para crecer.
- Gradle con **version catalog** (`gradle/libs.versions.toml`).

## Estructura

```text
Vita App/
├── app/src/main/
│   ├── java/com/vitta/app/
│   │   ├── MainActivity.kt
│   │   ├── VittaApplication.kt        (registra el decoder SVG de Coil)
│   │   ├── navigation/                (VittaNavHost)
│   │   ├── data/mock/                 (datos de ejemplo)
│   │   └── ui/
│   │       ├── theme/                 (Color, Type, Shape, Dimensions, Theme)
│   │       ├── components/            (brand, buttons, cards, habits,
│   │       │                           progress, store, mascot, forms,
│   │       │                           navigation, feedback, common)
│   │       └── screens/design/        (DesignSystemScreen — el catálogo)
│   ├── assets/vitta/                  (logo, wordmark, SVG de íconos e insignias)
│   └── res/                           (fuentes, drawables PNG, XML de Android)
├── README.md                          (este archivo)
├── README_INSTALACION.md
├── README_ESTILOS.md
├── README_COLORES.md
├── README_COMPONENTES.md
└── README_RECURSOS.md
```

## Estado actual

- ✅ Compila (`./gradlew :app:assembleDebug`) y **corre** — verificado en un
  emulador Android (API 35).
- ✅ Design System completo y catalogado en `DesignSystemScreen`.
- ⏳ Pendiente (próxima etapa): pantallas reales (Onboarding, Home, Hoy,
  Progreso, Logros, Perfil, Tienda) construidas *encima* de estos mismos
  componentes, navegación real entre ellas, y persistencia de datos.

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
