# Componentes reutilizables — Vitta

Cada componente está en `app/src/main/java/com/vitta/app/ui/components/`,
agrupado por carpeta. Todos tienen un `@Preview` en su mismo archivo.
Todos aparecen catalogados y en vivo en `DesignSystemScreen`.

## Marca (`components/brand/VittaBrandMark.kt`)

| Composable | Propósito |
|---|---|
| `VittaLogo` | El símbolo/isotipo de Vitta |
| `VittaWordmark` | El logotipo de texto "VITTA" |
| `VittaBrandLockup` | Logo + wordmark juntos (usado en la barra superior del `DesignSystemScreen`) |

## Buttons (`components/buttons/VittaButtons.kt`)

| Composable | Propósito | Parámetros principales |
|---|---|---|
| `VittaPrimaryButton` | Acción principal ("Guardar hábito") | `text`, `onClick`, `enabled`, `loading`, `icon`, `fillWidth` |
| `VittaSecondaryButton` | Acción secundaria sobre fondo suave ("Ver mis logros") | igual que el primario |
| `VittaOutlinedButton` | Acción terciaria con borde ("Omitir") | igual que el primario |
| `VittaTextButton` | Acción mínima ("Cancelar") | `text`, `onClick`, `enabled` |
| `VittaIconCircleButton` | Botón circular de un solo ícono (la "X" de cerrar) | `icon`, `contentDescription`, `onClick` |

Ejemplo:
```kotlin
VittaPrimaryButton(text = "Guardar hábito", onClick = { ... })
VittaPrimaryButton(text = "Guardando…", onClick = { }, loading = true)
```
Úsalos cuando necesites *cualquier* botón — nunca un `Button()` de Material
3 "a pelo" en una pantalla nueva.

## Cards (`components/cards/VittaCards.kt` y `VittaRewardCards.kt`)

| Composable | Propósito |
|---|---|
| `VittaSurfaceCard` | Contenedor base (fondo, radio, padding) para una card a medida |
| `VittaStatTile` | Par ícono + valor + etiqueta (ej. "🔥 12 · días de racha") |
| `VittaProgressCard` | Card con eyebrow + título + descripción + ilustración opcional (ej. la card de bienvenida de Home) |
| `VittaBalanceCard` | La card oscura de "Tu saldo" (Vitta Store) |
| `VittaHighlightCard` | Card oscura para destacar un logro puntual ("Última medalla") |
| `VittaCenteredInfoCard` | Card clara centrada con ilustración ("La más cercana") |
| `VittaEmptyStateCard` | Card de borde punteado para un estado vacío, con la mascota ("Todavía no has canjeado nada") |

## Hábitos (`components/habits/VittaHabitComponents.kt`)

| Composable | Propósito |
|---|---|
| `HabitIcon` (enum) | Los 7 íconos de hábito disponibles (agua, caminar, comer, dormir, leer, yoga + variante "agua vacía") |
| `VittaHabitIconBubble` | La burbuja circular con el ícono de un hábito |
| `VittaHabitRow` | Fila de hábito completa: ícono + título + subtítulo + control final (`trailing`) |
| `VittaHabitDoneIndicator` | El check verde de "completado" para usar como `trailing` |
| `VittaWaterTracker` | La fila de 7–8 vasos de agua (lleno/vacío, tocable) |

Ejemplo:
```kotlin
VittaHabitRow(
    icon = HabitIcon.Water,
    title = "Tomar agua",
    subtitle = "Vas 3 de 8 vasos",
    trailing = { VittaHabitDoneIndicator() }
)
```

## Progreso y logros (`components/progress/VittaProgressComponents.kt`)

| Composable | Propósito |
|---|---|
| `Insignia` (enum) | Las 10 insignias del mockup (rachas de 7/30/50/100 días + 6 de área) |
| `VittaInsigniaBadge` | Ilustración de una insignia, atenuada si `unlocked = false` |
| `VittaStreakBadge` | Pastilla "🔥 N · días de racha" |
| `VittaPointsBadge` | Pastilla "● N · VitaPuntos" |
| `VittaLevelProgressBar` | Barra de progreso de nivel |
| `VittaWeeklyBar` | Una barra del gráfico semanal (úsalas dentro de una `Row` con 7, una por día) |
| `VittaCalendarHeatmap` (`VittaCalendarHeatmap.kt`) | El mapa de calor de 5 semanas × 7 días ("Últimas cinco semanas"), recibe una lista de intensidades `0..3` |

```kotlin
VittaCalendarHeatmap(intensities = mockMonthHeatmap) // 35 valores, 0..3
```

## Vitta Store (`components/store/VittaStoreComponents.kt`)

| Composable | Propósito |
|---|---|
| `StoreItemState` (sealed interface) | Los 3 estados de un artículo de tienda: `Unlockable`, `Locked(requirement)`, `MissingPoints(message)` |
| `VittaStoreItemCard` | Card de un artículo de la tienda; su fila inferior cambia según `state` |
| `VittaRewardListItem` | Fila de "Mis recompensas" (una recompensa ya desbloqueada, tocable) |
| `VittaFilterChipsRow` | Fila horizontal de chips de categoría ("Todo", "Bienestar", …) |

```kotlin
VittaStoreItemCard(
    mascot = MascotState.Strong, area = "MOVIMIENTO",
    name = "Caminatas de 20 minutos", description = "...",
    state = StoreItemState.MissingPoints("Faltan 40 VitaPuntos")
)
```

## Mascota (`components/mascot/VittaMascot.kt`)

| Composable | Propósito |
|---|---|
| `MascotState` (enum) | Los 11 estados/ilustraciones de la mascota que existen como archivo: 6 recompensas (`Strong`, `GoodHabits`, `Reader`, `NoGym`, `Evening`, `Yoga`) + 5 recuperadas del bundle HTML (`OnboardingWelcome`, `OnboardingHabits`, `Peek`, `EmptyState`, `Wave`) |
| `MascotSize` (enum) | Tamaños preset: `Small` (72dp), `Medium` (118dp), `Large` (196dp), `ExtraLarge` (220dp) |
| `VittaMascot` | Pinta la mascota en el estado/tamaño pedido |

```kotlin
VittaMascot(state = MascotState.Yoga, size = MascotSize.Small)
```

Ver `README_RECURSOS.md` para el origen exacto de cada uno de los 11 estados.

## Formularios (`components/forms/VittaFormFields.kt`)

| Composable | Propósito |
|---|---|
| `VittaTextField` | Campo de texto genérico |
| `VittaPasswordField` | Campo de contraseña con alternar mostrar/ocultar |
| `VittaDaySelector` | Selector de 7 días ("L M X J V S D") para programar un hábito |
| `VittaSettingRow` | Fila de configuración: título + descripción + control a la derecha |
| `VittaSwitchRow` | `VittaSettingRow` con un `Switch` como control (ej. "Recordatorio") |

## Navegación (`components/navigation/VittaNavigation.kt`)

| Composable | Propósito |
|---|---|
| `VittaDestination` (enum) | Los 5 destinos de la barra inferior: Inicio, Hoy, Progreso, Logros, Perfil |
| `VittaBottomNavBar` | La barra de navegación inferior completa |
| `VittaSegmentedControl` | Control segmentado de 2+ opciones (ej. "Semana · Mes" en Progreso) |

Es puramente visual en esta etapa — `onSelect` no navega todavía a
ninguna pantalla real (no existen aún). Ver `navigation/VittaNavHost.kt`
para cómo conectarla cuando existan.

## Modales y notificaciones (`components/feedback/VittaFeedback.kt`)

| Composable | Propósito |
|---|---|
| `VittaToast` | Notificación tipo snackbar anclada abajo (ej. "Hábito completado · Racha de 7 días") |
| `VittaConfirmDialog` | Diálogo de confirmación para canjear puntos por una recompensa |
| `VittaRewardSheet` | Hoja con el detalle de una recompensa ya desbloqueada |

**Importante:** estos tres son overlays de pantalla completa (usan un
scrim). Cada uno debe colocarse como **el último hijo del `Box` de la
pantalla que lo muestra** (no en un `Box` raíz compartido por toda la
app) — así el fondo oscuro y el modal quedan confinados a esa pantalla.
Esto replica, y corrige, un bug real que tuvo el maquetado HTML original
(el modal se salía del "celular" simulado porque su contenedor no tenía
`position: relative`); en Compose el equivalente es simplemente respetar
esta regla de anidamiento.

## Comunes (`components/common/VittaAssetImage.kt`)

| Composable | Propósito |
|---|---|
| `VittaAssetImage` | Pinta un SVG desde `assets/vitta/...` vía Coil |
| `VittaIconAssets` | Helpers para construir la ruta de un ícono/insignia por nombre |

Uso interno de `VittaHabitIconBubble`/`VittaInsigniaBadge` — rara vez se
usa directo desde una pantalla.
