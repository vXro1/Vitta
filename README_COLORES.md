# Paleta de colores — Vitta

Fuente: `ui/theme/Color.kt`.

**2026-10 — paleta renovada.** El verde oliva (`#778661`) y los beige/grises
desaturados del maquetado original (`#EFE6D6`, `#F4EFE4`, `#8A9283`…) se
reemplazaron por una paleta clara, viva y fresca: verde esmeralda, naranja,
ámbar y fondos blanco-menta. Los **nombres** de las constantes no cambiaron,
así todas las pantallas heredan la paleta sin tocar cada una.

Contrastes calculados con la fórmula de luminancia relativa de WCAG 2.1
(texto normal ≥ 4.5:1).

## Marca — verde principal

| Nombre | HEX | Uso | Contraste |
|---|---|---|---|
| `GreenPrimary` | `#0A8754` | Botón primario, selección, progreso | 4.56:1 con texto blanco |
| `GreenHover` | `#08774A` | Hover del botón primario | |
| `GreenPressed` | `#066B42` | Presionado; texto verde sobre fondos claros | 6.6:1 sobre blanco, 5.7:1 sobre `SurfaceTint` |
| `GreenMid` | `#2FB67C` | Barras de progreso secundarias | decorativo |
| `GreenSoft` | `#6ED3A3` | Relleno terciario | decorativo |
| `GreenPale` | `#B5EBD0` | Bordes de tarjetas completadas, texto sobre toast oscuro | decorativo |
| `GreenTrack` | `#D6F2E4` | Riel de progreso sin rellenar | decorativo |

## Acentos

| Nombre | HEX | Uso | Contraste |
|---|---|---|---|
| `OrangeAccent` | `#C2410C` | Racha, enlaces | 5.2:1 sobre blanco |
| `OrangeAccentHover` | `#9A3412` | Hover de enlaces | |
| `GoldAccent` | `#FFB020` | Anillo de progreso, puntos, logros | usar solo con texto oscuro (7.6:1 con `TextPrimary`) |
| `SkyAccent` | `#1C6FC4` | Recordatorio / hora | ≈5:1 sobre blanco |
| `SkySoft` | `#E4F0FB` | Fondo del campo de hora | decorativo |

## Superficies

| Nombre | HEX | Uso |
|---|---|---|
| `Background` | `#F6FBF8` | Fondo de pantalla (blanco con un toque menta) |
| `Surface` | `#FFFFFF` | Tarjetas, campos |
| `SurfaceVariant` | `#E3F5EC` | Burbuja de icono |
| `SurfaceMuted` | `#EEF5F1` | Chips secundarios, controles segmentados |
| `SurfaceTint` | `#DDF3E7` | Estado seleccionado / completado |

## Bordes

| Nombre | HEX |
|---|---|
| `BorderSubtle` | `#C9DDD3` |
| `BorderMuted` | `#DCE9E2` |

## Texto

| Nombre | HEX | Uso | Contraste sobre `Background` |
|---|---|---|---|
| `TextPrimary` | `#14312A` | Títulos y texto principal | 13.4:1 |
| `TextSecondary` | `#3F5C54` | Texto de apoyo | 7.0:1 |
| `TextMuted` | `#5A746C` | Captions, metadatos | 4.8:1 (antes `#8A9283` ≈ 3:1, insuficiente) |
| `TextFaint` | `#93A79F` | Solo deshabilitado/decorativo | 2.5:1 — no usar para texto que deba leerse |

## Overlays y estados

| Nombre | Valor | Uso |
|---|---|---|
| `ScrimBackdrop` | `#A314312A` | Fondo oscuro detrás de diálogos |
| `ToastSurface` | `#14312A` | Toast/snackbar |
| `SelectionTint` | `#470A8754` | Selección de texto |
| `Success` | `GreenPrimary` | |
| `Warning` | `GoldAccent` | |
| `Info` | `SkyAccent` | |
| `Error` | `#C62828` | 5.6:1 sobre blanco |

## Dónde viven en el código

- Valores crudos: `VittaColors` (`Color.kt`).
- Alias semánticos: `VittaColorRoles` (p. ej. `VittaColorRoles.primary`,
  `VittaColorRoles.reminder`).
- Material 3: `VittaTheme` (`Theme.kt`). **Por ahora se usa siempre el
  esquema claro**: todas las pantallas pintan colores claros explícitos y
  seguir el modo oscuro del sistema mezclaba componentes oscuros de Material
  (diálogos, campos, selectores) con pantallas claras, y el texto quedaba
  claro sobre fondo claro. Se puede volver a `isSystemInDarkTheme()` cuando
  exista un modo oscuro diseñado.

**Regla del proyecto:** ningún Composable debe escribir `Color(0xFF...)`
directamente; siempre `VittaColors.*` o `VittaColorRoles.*`.
