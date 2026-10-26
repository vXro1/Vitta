# Paleta de colores — Vitta

Fuente: `ui/theme/Color.kt`.

Vitta usa la **paleta de la marca** definida en la especificación UX:
cinco colores cálidos y naturales, más los tonos derivados del maquetado
original.

| Color | HEX | Uso conceptual |
|---|---|---|
| Verde oliva | `#778661` | Bienestar, naturaleza, progreso y hábitos saludables |
| Amarillo | `#F1AA52` | Energía amable, recompensas y puntos de atención |
| Verde oscuro | `#2D403E` | Contraste, textos destacados y elementos estructurales |
| Terracota | `#A55729` | Acentos: racha, enlaces, recordatorios, categorías |
| Marfil / crema | `#F9F4EE` | Fondos principales y sensación cálida |

Contrastes calculados con la fórmula de luminancia relativa de WCAG 2.1
sobre el fondo marfil (texto normal ≥ 4.5:1; texto grande en negrita ≥ 3:1).

## Marca — verde oliva

| Nombre | HEX | Uso | Contraste |
|---|---|---|---|
| `GreenPrimary` | `#778661` | Botón primario, selección, progreso | 3.9:1 con texto blanco → solo etiquetas en negrita grande (≥ 14 pt) |
| `GreenHover` | `#66754F` | Hover del botón primario | 5.0:1 con blanco |
| `GreenPressed` | `#56633F` | Presionado; texto verde sobre fondos claros | 5.9:1 sobre marfil |
| `GreenMid` | `#9CAA86` | Barras de progreso secundarias | decorativo |
| `GreenSoft` | `#A9B893` | Relleno terciario | decorativo |
| `GreenPale` | `#C3CDB1` | Relleno claro, texto sobre toast oscuro | decorativo |
| `GreenTrack` | `#D4DCC2` | Riel de progreso sin rellenar | decorativo |

## Acentos

| Nombre | HEX | Uso | Contraste |
|---|---|---|---|
| `OrangeAccent` | `#A55729` | Racha, enlaces, recordatorios (terracota) | 4.8:1 sobre marfil |
| `OrangeAccentHover` | `#7E3F1C` | Hover de enlaces | 7.3:1 |
| `GoldAccent` | `#F1AA52` | Puntos, logros | usar solo con texto oscuro (5.5:1 con `TextPrimary`) |
| `SkyAccent` | = `OrangeAccent` | Recordatorio / hora (nombre conservado; la marca no tiene azul) | |
| `SkySoft` | `#F6E7DC` | Fondo del campo de hora | decorativo |

## Superficies

| Nombre | HEX | Uso |
|---|---|---|
| `Background` | `#F9F4EE` | Fondo de pantalla (marfil) |
| `Surface` | `#FFFCF8` | Tarjetas, campos |
| `SurfaceVariant` | `#EFE6D6` | Burbuja de icono |
| `SurfaceMuted` | `#F4EFE4` | Chips secundarios, controles segmentados |
| `SurfaceTint` | `#EEF0E3` | Estado seleccionado / completado |

## Bordes

| Nombre | HEX |
|---|---|
| `BorderSubtle` | `#DFD4C2` |
| `BorderMuted` | `#E3D9C8` |

## Texto

| Nombre | HEX | Uso | Contraste sobre `Background` |
|---|---|---|---|
| `TextPrimary` | `#2D403E` | Títulos y texto principal | 10.0:1 |
| `TextSecondary` | `#5C6A5B` | Texto de apoyo | 5.2:1 |
| `TextMuted` | `#66705F` | Captions, metadatos | 4.7:1 (el original `#8A9283` daba ≈ 3:1) |
| `TextFaint` | `#A9A493` | Solo deshabilitado/decorativo | 2.3:1 — no usar para texto que deba leerse |

## Overlays y estados

| Nombre | Valor | Uso |
|---|---|---|
| `ScrimBackdrop` | `#A32D403E` | Fondo oscuro detrás de diálogos |
| `ToastSurface` | `#2D403E` | Toast/snackbar |
| `SelectionTint` | `#47A55729` | Selección de texto |
| `Success` | `GreenPrimary` | |
| `Warning` | `GoldAccent` | |
| `Info` | `OrangeAccent` | |
| `Error` | `#B3453A` | 5.0:1 sobre marfil |

## Rellenos con texto blanco

El oliva base (`GreenPrimary`, 3.9:1 con blanco) solo sirve para íconos,
bordes, barras y texto grande en negrita. Todo relleno que lleve **texto
blanco normal** (botón primario, píldora de nivel, chips y días
seleccionados, check de hábito completado) usa `VittaColorRoles.primaryStrong`
(= `GreenHover`, 5.0:1).

## Dónde viven en el código

- Valores crudos: `VittaColors` (`Color.kt`).
- Alias semánticos: `VittaColorRoles` (p. ej. `VittaColorRoles.primary`,
  `VittaColorRoles.primaryStrong`, `VittaColorRoles.reminder`).
- Nombres del maquetado: `Mk` (`VittaMockupTokens.kt`), usado por Progreso,
  Logros y Perfil. Cada valor apunta a `VittaColors`; el gris `#8A9283` del
  maquetado (≈3:1) se sustituyó por `TextMuted`.
- Recursos XML puente (tema del manifest, fondo del ícono):
  `res/values/colors.xml` y `res/values/ic_launcher_background.xml`.
- Material 3: `VittaTheme` (`Theme.kt`). **Por ahora se usa siempre el
  esquema claro**: todas las pantallas pintan colores claros explícitos y
  seguir el modo oscuro del sistema mezclaba componentes oscuros de Material
  (diálogos, campos, selectores) con pantallas claras, y el texto quedaba
  claro sobre fondo claro. Se puede volver a `isSystemInDarkTheme()` cuando
  exista un modo oscuro diseñado.

**Regla del proyecto:** ningún Composable debe escribir `Color(0xFF...)`
directamente; siempre `VittaColors.*` o `VittaColorRoles.*`.
