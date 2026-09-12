# Paleta de colores — Vitta

Fuente: `ui/theme/Color.kt`. Todos los valores fueron extraídos directamente
de los estilos inline del maquetado (`Vitta App.html`) — ninguno fue
inventado. Donde el HTML no distinguía un rol (p. ej. "error"), se indica
explícitamente cómo se derivó.

## Marca — verde principal

| Nombre | HEX | Uso |
|---|---|---|
| `GreenPrimary` | `#778661` | Botón primario, ítem de navegación activo, relleno de progreso |
| `GreenHover` | `#66754F` | Estado `:hover` del botón primario |
| `GreenPressed` | `#56633F` | Estado `:active`/presionado del botón primario |
| `GreenMid` | `#9CAA86` | Barras de progreso secundarias (p. ej. día 3 de 4 en el gráfico semanal) |
| `GreenSoft` | `#A9B893` | Relleno terciario de progreso |
| `GreenPale` | `#C3CDB1` | Texto secundario sobre el toast oscuro |
| `GreenTrack` | `#D4DCC2` | Riel/track sin rellenar de una barra de progreso |

## Acento — naranja y dorado

| Nombre | HEX | Uso |
|---|---|---|
| `OrangeAccent` | `#A55729` | Texto de racha/fuego, enlaces, hora del recordatorio |
| `OrangeAccentHover` | `#7E3F1C` | Estado `:hover` de enlaces |
| `GoldAccent` | `#F1AA52` | Círculo de puntos, ícono del toast |

## Superficies (cálidas, nunca blanco/gris puro)

| Nombre | HEX | Uso |
|---|---|---|
| `Background` | `#F9F4EE` | Fondo de pantalla |
| `Surface` | `#FFFCF8` | Tarjetas elevadas |
| `SurfaceVariant` | `#EFE6D6` | Burbuja de ícono, chip suave |
| `SurfaceMuted` | `#F4EFE4` | Chip secundario, tile de estadística |
| `SurfaceTint` | `#EEF0E3` | Superficie con tinte verde muy sutil |

## Bordes

| Nombre | HEX | Uso |
|---|---|---|
| `BorderSubtle` | `#DFD4C2` | Borde de botón outlined |
| `BorderMuted` | `#E3D9C8` | Borde de tarjeta bloqueada |

## Texto

| Nombre | HEX | Uso |
|---|---|---|
| `TextPrimary` | `#2D403E` | Títulos, texto principal |
| `TextSecondary` | `#5C6A5B` | Cuerpo de texto de apoyo |
| `TextMuted` | `#8A9283` | Captions, metadatos |
| `TextFaint` | `#A9A493` | Etiquetas deshabilitadas / de menor énfasis |

## Overlays y feedback

| Nombre | Valor | Uso |
|---|---|---|
| `ScrimBackdrop` | `rgba(45,64,62,.62)` → `Color(0xA32D403E)` | Fondo oscuro detrás de diálogos y hojas de recompensa |
| `ToastSurface` | `#2D403E` | Fondo del toast/snackbar oscuro |
| `SelectionTint` | `rgba(165,87,41,.28)` | Color de selección de texto (`::selection` en el HTML) |

## Estados semánticos (no presentes como colores literales en el HTML)

El maquetado no define una paleta de estados (éxito/advertencia/error) —
usa el verde y el naranja de marca para "positivo" en general, y no tiene
ninguna pantalla de error visible. Para tener un `ColorScheme` de Material 3
completo, se documentan así (no se inventó una paleta nueva, se reutilizó
la existente donde tenía sentido):

| Rol | Valor | Origen |
|---|---|---|
| `Success` | `GreenPrimary` | Reutiliza el verde de marca |
| `Warning` | `GoldAccent` | Reutiliza el dorado de puntos/logros |
| `Info` | `OrangeAccent` | Reutiliza el naranja de acento |
| `Error` | `#B3453A` (nuevo) | **Única** adición real: un tono de la misma familia cálida que `OrangeAccent`, para el caso — no presente en el mockup — en que Material 3 necesita un color de error |

## Dónde viven en el código

- Valores crudos: `VittaColors` (objeto en `Color.kt`).
- Alias semánticos para usar en componentes: `VittaColorRoles` (p. ej.
  `VittaColorRoles.primary`, `VittaColorRoles.textSecondary`).
- Integrados a Material 3 vía `VittaTheme` (`Theme.kt`), que además define
  un `ColorScheme` oscuro **derivado** (no diseñado en el mockup original —
  ver el comentario en `Theme.kt`) para que la app respete el modo oscuro
  del sistema en vez de forzar la pantalla clara.

**Regla del proyecto:** ningún Composable debe escribir `Color(0xFF...)`
directamente — siempre `VittaColors.*` o `VittaColorRoles.*`.
