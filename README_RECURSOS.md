# Recursos visuales — Vitta

Inventario de todo lo migrado desde `../prototipo/`, y de las decisiones
tomadas para cada tipo de archivo.

## Marca — logo y wordmark

Ubicación: `app/src/main/assets/vitta/logo.svg` y `.../vitta/wordmark.svg`.

| Archivo | Qué es | Cómo se usa |
|---|---|---|
| `logo.svg` | El símbolo/isotipo de Vitta (una hoja/brote), 5 paths planos de un solo color | `VittaLogo` (`ui/components/brand/VittaBrandMark.kt`) |
| `wordmark.svg` | El logotipo "VITTA" (texto de marca como ilustración, no como fuente) | `VittaWordmark` |

Ambos se muestran juntos con `VittaBrandLockup` — usado en la barra
superior del `DesignSystemScreen` y en su propia sección "Marca" del
catálogo.

`logo.svg` es lo bastante simple (5 `<path>` de un solo color, sin
degradados) para convertirse a mano a `VectorDrawable` con fidelidad
1:1 — por eso el **ícono de lanzador** (`res/drawable/ic_launcher_foreground.xml`)
ahora es el logo real de Vitta, no un marcador de posición inventado. `wordmark.svg`,
en cambio, se carga con Coil como el resto de ilustraciones (ver más abajo).

## SVG — íconos de hábitos e insignias

Ubicación: `app/src/main/assets/vitta/icons/` y `.../vitta/insignias/`.

| Origen (`prototipo/assets/`) | Destino | Contenido |
|---|---|---|
| `icons/agua-lleno.svg`, `agua-vacio.svg`, `caminar.svg`, `comer.svg`, `dormir.svg`, `leer.svg`, `yoga.svg` | `assets/vitta/icons/` | 7 ilustraciones de hábito |
| `insignia-*.svg` (10 archivos) | `assets/vitta/insignias/` | 10 insignias de racha/área |
| `icon_streak_flame.svg` (extraído del bundle, ver más abajo) | `assets/vitta/icons/` | Ilustración de la llama de racha (`VittaIconAssets.streakFlame`) |

**Por qué Coil y no `VectorDrawable`:** se inspeccionó el contenido real de
estos SVG (no solo su extensión) y **no son íconos de línea simples** —
son ilustraciones a todo color generadas por IA, con cientos de `<path>`
superpuestos y degradados de color (por ejemplo, `yoga.svg` tiene más de
200 `<path>` distintos). Convertir esto a mano a `VectorDrawable` XML:

1. produciría un archivo enorme y frágil de mantener, o
2. perdería matices de color al simplificarlo.

Por eso se cargan tal cual con **Coil + `coil-svg`**
(`VittaAssetImage`, registrado en `VittaApplication` como
`ImageLoaderFactory` con `SvgDecoder.Factory()`), preservando el diseño
original al 100%. Se verificó renderizando la app en un emulador: los 7
íconos y las 10 insignias se ven idénticos al mockup.

## PNG — ilustraciones de la mascota

Ubicación: `app/src/main/res/drawable-nodpi/` (nombres con `_` en vez de
`-` porque los recursos de Android no admiten guiones).

| Origen | Destino | Estado enum (`MascotState`) |
|---|---|---|
| `assets/png/vitta-fuerte.png` | `vitta_fuerte.png` | `Strong` |
| `assets/png/vitta-buenoshabitos.png` | `vitta_buenoshabitos.png` | `GoodHabits` |
| `assets/png/vitta-lector.png` | `vitta_lector.png` | `Reader` |
| `assets/png/vitta-singym.png` | `vitta_singym.png` | `NoGym` |
| `assets/png/vitta-tarde.png` | `vitta_tarde.png` | `Evening` |
| `assets/png/vitta-yoga.png` | `vitta_yoga.png` | `Yoga` |
| *(extraído del bundle HTML, ver abajo)* | `mascot_onboarding_welcome.png` | `OnboardingWelcome` |
| *(extraído del bundle HTML, ver abajo)* | `mascot_onboarding_habits.png` | `OnboardingHabits` |
| *(extraído del bundle HTML, ver abajo)* | `mascot_peek.png` | `Peek` |
| *(extraído del bundle HTML, ver abajo)* | `mascot_empty_state.png` | `EmptyState` |
| *(extraído del bundle HTML, ver abajo)* | `mascot_wave.png` | `Wave` |

Se dejaron como PNG (no existe un `.svg` equivalente para estas
ilustraciones de mascota en el proyecto de origen — son renders/fotos de
la mascota, no vectores). `drawable-nodpi` evita que Android las reescale
por densidad, ya que son ilustraciones de alta resolución pensadas para
verse a un tamaño fijo razonable.

**Nota visual conocida:** `vitta_lector.png` trae un fondo blanco/claro
"horneado" en el propio archivo (a diferencia de las otras 5, que tienen
fondo transparente). Se conservó el archivo tal cual — no se recortó ni
regeneró — porque no hay forma de "arreglarlo" sin inventar contenido que
no existe en el original; se ve como un pequeño rectángulo blanco detrás
de la mascota en la galería de `MascotSection`. Si se quiere corregir,
habría que pedir/generar una versión con fondo transparente.

## Ilustraciones recuperadas del bundle HTML

El maquetado exportado (`Vitta App.html`) lleva 14 recursos adicionales
**embebidos como base64** dentro de su propio bundle (un
`<script type="__bundler/manifest">`), sin existir como archivo suelto en
ningún punto del proyecto. Se escribió un script puntual
(`zlib.gunzip` + decodificación base64 sobre el manifest) para extraerlos
todos a `prototipo/assets/embedded/` como fuente de verdad, y de ahí
migrar los que aportan algo nuevo:

| Resultado de la extracción | Qué se hizo |
|---|---|
| 5 imágenes PNG (poses reales de la mascota, alt="Vitta" o animación `vFloat` en el HTML) | Migradas — ver tabla de arriba (`OnboardingWelcome`, `OnboardingHabits`, `Peek`, `EmptyState`, `Wave`) |
| 1 SVG de la llama de racha (animación `vFlame` en el HTML) | Migrado como `icon_streak_flame.svg` |
| 1 SVG del logo (antes llamado `icon_brand_mark`) | Migrado y renombrado a `logo.svg` — ver sección "Marca" |
| **7 SVG "genéricos"** (usados en el HTML como decoración de logros bloqueados) | **Descartados** — se verificó por hash (SHA-256) que son copias byte a byte de 7 insignias que ya existen en `assets/vitta/insignias/`. No se duplicó nada. |

`wordmark.svg` no vino de esta extracción: es el logotipo de texto
"VITTA" provisto directamente para este proyecto (no estaba embebido en
el HTML ni suelto en el maquetado original).

## Tipografías

El HTML declara `Caprasimo` (peso 400) y `Figtree` (400/500/600/700) vía
Google Fonts (`@font-face` con URLs de `fonts.gstatic.com`). Se
descargaron los 5 archivos `.ttf` reales (no se generaron ni inventaron)
y se incorporaron como fuentes locales del proyecto:

```
app/src/main/res/font/
├── caprasimo_regular.ttf
├── caprasimo.xml            (font-family con el peso 400)
├── figtree_regular.ttf
├── figtree_medium.ttf
├── figtree_semibold.ttf
├── figtree_bold.ttf
└── figtree.xml              (font-family con los 4 pesos)
```

Se usaron como fuentes **locales empaquetadas** (no como "Downloadable
Fonts" vía Google Play Services) para que la app no dependa de una
descarga en el primer arranque ni de Google Play Services estar
disponible en el dispositivo/emulador — más simple y más confiable para
esta etapa. `ui/theme/Type.kt` las expone como `CaprasimoFamily` y
`FigtreeFamily`.

## Ícono de lanzador (launcher icon)

`res/drawable/ic_launcher_foreground.xml` es el **logo real de Vitta**
(`assets/vitta/logo.svg`), convertido a mano a `VectorDrawable` — se pudo
hacer con fidelidad 1:1 porque, a diferencia de los íconos/insignias, este
archivo es solo 5 `<path>` planos de un color. El ícono adaptativo
(`mipmap-anydpi-v26/ic_launcher.xml`) lo combina con el fondo
`ic_launcher_background` (el crema de marca, `#F9F4EE`).
