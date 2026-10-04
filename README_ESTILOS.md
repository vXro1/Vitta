# Sistema de diseño — Vitta

Documentación del Design System implementado en `ui/theme/` y
`ui/components/`. Fuente de verdad: el maquetado `../prototipo/Vitta App.html`.

## Filosofía visual

Vitta se ve como una libreta cálida, no como una app "Material" genérica:
fondos claros blanco-menta (`#F6FBF8`/`#FFFFFF`); un verde esmeralda como
color de acción; naranja y ámbar como acentos de logro/racha (ver
README_COLORES.md); una sola tipografía redondeada y amigable (Nunito)
en toda la app; esquinas muy redondeadas (botones e íconos en
"pastilla", tarjetas con radios grandes); sombras muy suaves y bajas en
opacidad, casi nunca duras.

## Tipografía

Una sola familia en **toda la app, desde el Login**: **Nunito**
(Google Fonts, SIL Open Font License 1.1), pesos 400/500/600/700/800.

- Títulos, números grandes y botones primarios: ExtraBold (800) / Bold (700).
- Cuerpo, labels y captions: Regular (400), Medium (500) y SemiBold (600).
- Los estilos de Material 3 que Vitta no define también usan Nunito, así
  los componentes estándar (selector de hora, chips, diálogos) no caen en
  Roboto.

Jerarquía (`VittaTextStyles` en `Type.kt`): `displayLarge` → `heading` →
`title` → `subtitle` → `body` → `bodySmall` → `label` → `caption` →
`button`/`buttonDisplay`. Cada uno corresponde a un tamaño realmente usado
en el HTML (por ejemplo `title` = 19px, tal como el título del
diálogo "Diez desayunos sencillos").

## Espaciado

El HTML usa una mezcla de valores (4, 6, 8, 9, 10, 12, 14, 16, 18, 20, 22,
24, 26, 30px…). Se consolidaron en una escala regular de 4dp
(`VittaSpacing`: `Xxs=4, Xs=6, Sm=8, Md=12, Lg=16, Xl=20, Xxl=24, Xxxl=32`)
redondeando cada valor original al escalón más cercano, en vez de mantener
un valor suelto por componente. Esto es una decisión documentada, no un
dato inventado: el espaciado *visual* se mantiene, pero ahora es
reutilizable y consistente entre componentes.

## Bordes y radios

`VittaShapes` (`Shape.kt`) mapea directamente los `border-radius` vistos en
el HTML: `10px→RadiusXs`, `14px`, `18px`, `20px`, `22px`, `24px`,
`30px→RadiusHuge`, `34px→RadiusSheet` (hojas/diálogos), y `999px→Pill`
(todos los botones y chips).

## Sombras

El HTML usa `box-shadow` muy suaves (`0 3px 14px rgba(45,64,62,.07)` en
tarjetas, `0 20px 50px rgba(45,64,62,.3)` en diálogos). Material 3 en
Compose expresa elevación con `tonalElevation`/`shadowElevation` en vez de
un `box-shadow` CSS; `VittaElevation` (`Dimensions.kt`) da los niveles
equivalentes (`Card`, `CardLarge`, `Modal`, `Toast`) para usarlos donde un
componente necesite elevación real en vez de solo color plano — en esta
etapa, la mayoría de las tarjetas usan elevación 0 y se apoyan en el
contraste de color (igual que el mockup, donde la sombra es casi
imperceptible).

## Botones

Todos los botones son "pastilla" (radio 999px), con altura mínima táctil
de 48–56dp. Cuatro variantes (`ui/components/buttons/VittaButtons.kt`):
`VittaPrimaryButton`, `VittaSecondaryButton`, `VittaOutlinedButton`,
`VittaTextButton`; todas soportan `enabled` y `loading`. Ver
`README_COMPONENTES.md` para el detalle de cada uno.

## Cards

Fondo `Surface` (`#FFFCF8`), radio grande, sin sombra dura. Variantes:
`VittaSurfaceCard` (base), `VittaStatTile` (par ícono+valor+label),
`VittaProgressCard` (card de progreso con ilustración opcional).

## Responsive / adaptativo

Ningún componente usa un ancho fijo en `dp` cuando puede adaptarse:

- Filas de hábitos, botones y cards usan `fillMaxWidth()` + `padding`, no
  anchos absolutos.
- El selector de días (`VittaDaySelector`) y las barras del gráfico semanal
  (`VittaWeeklyBar`) usan `Modifier.weight(1f)` dentro de una `Row`, así se
  reparten el ancho disponible sin importar si la pantalla es un teléfono
  angosto o uno grande.
- Listas largas (`DesignSystemScreen`) usan `LazyColumn`/`LazyRow` en vez
  de `Column`/`Row` con muchos hijos fijos.
- Tipografía en `sp` (escala con la configuración de accesibilidad del
  usuario), espaciado y tamaños de ícono en `dp`.
- No se fijó ningún layout pensando en un solo tamaño de teléfono; se
  probó visualmente en un emulador de 1080×2400 (Pixel-like, densidad
  alta) y el mismo árbol de Composables se adapta por diseño a anchos
  menores/mayores gracias a lo anterior.

## Estados visuales

Los componentes exponen los estados que el mockup realmente distingue:
`enabled/disabled/loading` en botones, `unlocked/locked` (con atenuación
`alpha`) en insignias y iconos bloqueados, `selected/unselected` en el
selector de días y la navegación inferior, `visible` (con
`AnimatedVisibility`) en toast/diálogo/hoja de recompensa.
