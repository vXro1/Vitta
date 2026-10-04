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

Toda la app usa **Nunito** (decisión de 2026-10; antes el maquetado usaba
Caprasimo + Figtree, que se retiraron). Se descargaron de Google Fonts los
5 `.ttf` estáticos oficiales. Licencia: SIL Open Font License 1.1, que
permite incluirlas en una app sin atribución visible.

```
app/src/main/res/font/
├── nunito_regular.ttf     (400)
├── nunito_medium.ttf      (500)
├── nunito_semibold.ttf    (600)
├── nunito_bold.ttf        (700)
├── nunito_extrabold.ttf   (800)
└── nunito.xml             (font-family con los 5 pesos)
```

Se usaron como fuentes **locales empaquetadas** (no como "Downloadable
Fonts" vía Google Play Services) para que la app no dependa de una
descarga en el primer arranque ni de Google Play Services estar
disponible en el dispositivo/emulador — más simple y más confiable para
esta etapa. `ui/theme/Type.kt` las expone como `NunitoFamily`.

## Ícono de lanzador (launcher icon)

`res/drawable/ic_launcher_foreground.xml` es el **logo real de Vitta**
(`assets/vitta/logo.svg`), convertido a mano a `VectorDrawable` — se pudo
hacer con fidelidad 1:1 porque, a diferencia de los íconos/insignias, este
archivo es solo 5 `<path>` planos de un color. El ícono adaptativo
(`mipmap-anydpi-v26/ic_launcher.xml`) lo combina con el fondo
`ic_launcher_background` (el fondo de marca, `#F6FBF8`).


## Iconos de hábitos — Icons8 (2026-10)

Los iconos que representan hábitos (selector de icono, tarjetas del
inicio, hábitos predefinidos, indicadores de racha/logros) son de
**[Icons8](https://icons8.com)**, estilo **"Color"**, en PNG de 96 px.
Reemplazan a las 7 ilustraciones SVG anteriores para los hábitos (las SVG
siguen en `assets/` y se usan en el contador de vasos de agua y en el
catálogo de diseño).

- **Ubicación:** `app/src/main/res/drawable-nodpi/ic8_*.png` (167 archivos,
  ~315 KB en total, ~2 KB cada uno).
- **Catálogo en código:** `ui/components/icons/HabitIconCatalog.kt`
  (nombre en español, categoría y palabras clave para la búsqueda).
- **Carga:** `painterResource` (recurso local, sin red ni Coil). La
  cuadrícula del catálogo es perezosa (`LazyVerticalGrid`) y solo decodifica
  los iconos visibles.
- **Por qué PNG y no SVG:** la licencia gratuita de Icons8 solo entrega PNG
  de hasta 100 px; SVG requiere plan de pago. 96 px se ve nítido en los
  tamaños usados (28 a 40 dp).

### Licencia (verificada el 2026-10-04)

Fuente: *Universal Multimedia Licensing Agreement* de Icons8
(<https://icons8.com/vue-static/landings/pricing/icons8-license.pdf>) y su
centro de ayuda.

| Pregunta | Respuesta |
|---|---|
| ¿Se pueden usar gratis? | Sí, para uso personal o comercial, **con atribución** (enlace a icons8.com). |
| ¿En una app Android? | Sí: la licencia permite expresamente *"Use of Licensed Material for Desktop or Mobile Applications"*. |
| ¿En un proyecto académico? | Sí (está cubierto por el uso personal o no comercial). |
| Formato gratuito | PNG hasta 100 × 100 px. SVG/PDF y tamaños mayores son de pago. |
| ¿Se pueden modificar? | **No** con la cuenta gratuita ("must not make derivative copies"). Por eso los iconos se muestran tal cual, **sin recolorear ni aplicar tint**. La recompresión PNG sin pérdida que se aplicó no cambia ningún píxel. |
| Prohibido | Redistribuir los archivos sueltos o permitir que terceros los extraigan como "stand-alone files"; usarlos para crear packs de iconos. El selector de Vitta solo los muestra dentro de la app. |
| Marcas | Se excluyeron iconos de marcas registradas (p. ej. LEGO, Nintendo) y los relacionados con fumar. |

### Cómo se cumple la atribución

1. **Dentro de la app:** sección **Yo → Acerca de Vitta → "Iconos por
   Icons8"**, con enlace a <https://icons8.com> (`ProfileScreen.kt`).
2. **Si la app se publica en Google Play:** agregar en la descripción de
   la ficha: *"Iconos por Icons8 (https://icons8.com)"*.
3. **Si se usan en un sitio web** (p. ej. la plataforma web de Vitta): la
   licencia gratuita pide dos enlaces, uno a icons8.com y otro a la página
   de cada icono usado.
4. Quien reciba el proyecto (equipo, docentes) queda sujeto a la misma
   licencia; este documento sirve como aviso.

Si en algún momento se compra un plan de Icons8, la atribución deja de ser
obligatoria y se podrían usar SVG y recolorear.

### Iconos incluidos (estilo "Color", 96 px)

`alarm-clock`, `apple`, `avocado`, `baby`, `banana`, `bath`, `beach`, `bed`, `bicycle`, `board-game`, `book`, `books`, `bottle-of-water`, `boxing`, `brain`, `breakfast`, `briefcase`, `broccoli`, `broom`, `calculator`, `calendar`, `camera`, `carrot`, `cat`, `cat-footprint`, `checklist`, `climbing`, `code`, `coffee`, `coffee-to-go`, `coins`, `comb`, `computer`, `controller`, `cooking`, `cooking-pot`, `dancing`, `doctors-bag`, `dog`, `drawing`, `dumbbell`, `egg`, `elderly-person`, `email`, `exercise`, `family`, `fire-element`, `fish`, `fishing`, `flower`, `football`, `friends`, `garden`, `graduation-cap`, `guitar`, `guru`, `hair-brush`, `hair-dryer`, `hammer`, `hand-with-pen`, `handshake`, `happy`, `headphones`, `health-checkup`, `healthy-food`, `heart-health`, `heart-with-pulse`, `homework`, `hot-springs`, `housekeeping`, `hug`, `idea`, `iron`, `journal`, `joystick`, `jump-rope`, `kettle`, `kite`, `laptop`, `leaf`, `learning`, `library`, `lotus`, `lunchbox`, `lungs`, `map`, `massage`, `medical-doctor`, `meditation-guru`, `meeting`, `mental-health`, `microphone`, `milk`, `money`, `money-box`, `moon-symbol`, `movie`, `music`, `nail-polish`, `night`, `no-smoking`, `note`, `office`, `open-book`, `paint-brush`, `paint-palette`, `pen`, `pet`, `phone`, `piano`, `pilates`, `pill`, `pills`, `ping-pong`, `planner`, `popcorn`, `positive-dynamic`, `potted-plant`, `prayer`, `presentation`, `puzzle`, `radio`, `reading`, `relax`, `rowing`, `running`, `salad`, `scale`, `shower`, `skateboarding`, `sleeping-in-bed`, `smartphone`, `smiling`, `soap`, `spa`, `stethoscope`, `stretching`, `sun`, `sunrise`, `surfing`, `swimming`, `tasks`, `tea`, `tennis`, `tent`, `theatre-mask`, `thermometer`, `time`, `todo-list`, `tooth`, `toothbrush`, `treadmill`, `trekking`, `trophy`, `tv`, `vacuum-cleaner`, `vegetarian-food`, `video-call`, `volleyball`, `walking`, `wallet`, `washing-machine`, `water`, `water-bottle`, `watering-can`, `workstation`, `yoga`

## Limitaciones conocidas (backend)

Verificado contra `Vitta-Plataforma-Web-de-Gesti-n-de-H-bitos-y-Gamificaci-n/src`
(`db/schema.sql`, `models/habit.ts`, `utils/validation.ts`,
`repositories/habitRepository.ts`). La tabla `habitos` solo tiene
`nombre`, `meta` (TEXT), `frecuencia` (TEXT) y `tipo`.

| Problema | Causa | Archivo / endpoint | Limitación | Solución necesaria | Impacto actual |
|---|---|---|---|---|---|
| La hora del recordatorio no se guarda | No existe columna para la hora | `POST /api/habits`, `PUT /api/habits/:id`; `schema.sql`; `validateCreateHabitInput` / `validateUpdateHabitInput` | El backend ignora cualquier campo extra; solo guarda `nombre`, `meta`, `frecuencia`, `tipo` | Backend: columna `hora_recordatorio TIME NULL` (o `TEXT` "HH:mm" en 24 h), validarla en create/update e incluirla en `SELECT_COLS`. Android: agregar `hora_recordatorio` a `CreateHabitRequest`, `UpdateHabitRequest` y `HabitDto`, y precargarla en la edición | El selector funciona y muestra la hora elegida, pero se pierde al guardar. La app se lo avisa al usuario en pantalla |
| No hay notificaciones | No hay `AlarmManager`/`WorkManager` ni permiso `POST_NOTIFICATIONS` | Android (no existe el código) | Aunque se guardara la hora, nada avisaría | Programar el aviso localmente (WorkManager/AlarmManager) con la hora guardada | El texto anterior "Vitta te avisa a esta hora" era incorrecto; se reemplazó por un aviso honesto |
| El icono elegido no se guarda | No existe columna para el icono | Mismos endpoints que arriba | Igual que la hora | Backend: columna `icono TEXT NULL` con el `id` del catálogo (p. ej. `"yoga"`). Android: enviarlo y leerlo en los DTO y usarlo en vez de deducirlo | El icono se **deduce del nombre** (`HabitIcons.forHabitName`). Si el usuario elige otro, la app le avisa qué icono verá después |
| Unidad de la meta | — | `habitos.meta` es texto libre | **Ninguna**: "30 minutos", "8 vasos", "2 kilómetros" ya caben | No requiere cambios | La unidad es editable sin tocar el backend |
| No hay datos de perfil editables | No existe `GET/PUT /api/users/me` | `routes/auth.routes.ts` solo tiene `register` y `login` | El nombre y el correo solo llegan en la respuesta de login/registro | Un endpoint de perfil si se quiere editar nombre/correo o mostrar la fecha de registro | "Yo" muestra el nombre y el correo guardados localmente al iniciar sesión (quien inició sesión con una versión anterior verá el correo al volver a entrar) |
