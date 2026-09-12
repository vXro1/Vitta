# Instalación y ejecución

Esta guía documenta exactamente las versiones usadas y verificadas al
construir este proyecto (compilación real con `gradlew`, ejecución real en
un emulador). No se suponen versiones no verificadas.

## 1. Requisitos

| Herramienta | Versión usada/verificada en este proyecto |
|---|---|
| Android Studio | 2026.1.1 (build `AI-261.23567.138.2611.15613797`) o cualquier versión que soporte AGP 8.7 / Compose Compiler de Kotlin 2.0 |
| JDK | 21 (se usó el JBR — JetBrains Runtime — que trae incluido Android Studio) |
| Android Gradle Plugin (AGP) | 8.7.3 |
| Gradle | 8.13 (vía el wrapper, `gradlew`/`gradlew.bat`) |
| Kotlin | 2.0.21 |
| Compile/Target SDK | 35 |
| Min SDK | 26 (Android 8.0) |
| Android SDK Build-Tools | 35.0.0 (o el más reciente instalado) |

No es necesario instalar Gradle ni Kotlin por separado: el *wrapper*
(`gradlew`/`gradlew.bat`) descarga Gradle 8.13 automáticamente, y Android
Studio/AGP resuelven Kotlin y el Compose Compiler como dependencias del
proyecto.

## 2. Abrir el proyecto

1. Abre Android Studio.
2. `File > Open...` y selecciona la carpeta `Vita App/` (la carpeta que
   contiene `settings.gradle.kts`, **no** la carpeta `app/` por dentro).
3. Android Studio detecta el proyecto Gradle y ofrece sincronizar
   automáticamente.

## 3. Sincronizar Gradle

- Si no se sincroniza solo: botón **"Sync Project with Gradle Files"**
  (ícono del elefante con la flecha) en la barra de herramientas, o
  `File > Sync Project with Gradle Files`.
- La primera sincronización descarga dependencias (Compose BOM, Coil,
  Navigation, etc.) — requiere conexión a internet.
- Si Android Studio pregunta por la ubicación del SDK, señala tu carpeta
  del Android SDK (por ejemplo `C:\Users\<usuario>\AppData\Local\Android\Sdk`
  o donde lo tengas instalado). Esto se guarda en `local.properties`, un
  archivo **local** que no se versiona (cada máquina tiene el suyo).

## 4. Compilar desde la línea de comandos (opcional)

Desde la carpeta `Vita App/`:

```bash
# Windows (cmd/PowerShell)
gradlew.bat :app:assembleDebug

# Con Git Bash / WSL
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew.bat :app:assembleDebug
```

El APK de depuración queda en:
`app/build/outputs/apk/debug/app-debug.apk`.

Este comando fue verificado y produce `BUILD SUCCESSFUL` en este proyecto.

## 5. Ejecutar en un emulador

1. `Tools > Device Manager` en Android Studio, crea un dispositivo virtual
   (AVD) si no tienes uno — cualquier teléfono con API 26+ sirve; se probó
   con **API 35 (Medium Phone)**.
2. Inícialo y presiona **Run ▶** sobre la configuración `app` en Android
   Studio.
3. Por línea de comandos (con el emulador ya iniciado):
   ```bash
   gradlew.bat :app:installDebug
   adb shell am start -n com.vitta.app/.MainActivity
   ```

## 6. Ejecutar en un dispositivo físico

1. En el teléfono: `Ajustes > Acerca del teléfono`, toca 7 veces
   "Número de compilación" para activar **Opciones de desarrollador**.
2. `Ajustes > Opciones de desarrollador`, activa **Depuración USB**.
3. Conecta el teléfono por USB, acepta el diálogo de autorización de
   depuración que aparece en el teléfono.
4. Verifica que Android Studio (o `adb devices`) lo detecte, y presiona
   Run ▶ igual que con el emulador.

## 7. Problemas comunes

**"SDK location not found" / Gradle no encuentra el SDK**
Crea (o corrige) `local.properties` en la raíz del proyecto con:
```
sdk.dir=C:/ruta/a/tu/Android/Sdk
```
Usa **barras `/`**, no `\`; una barra invertida sin escapar rompe el
parseo de este archivo (`.properties`) y produce `Invalid file path`.

**Falla la sincronización por falta de red**
La primera sincronización necesita descargar dependencias de Google/Maven
Central. Verifica conexión a internet; si usas una red corporativa con
proxy, configúralo en `Android Studio > Settings > Appearance & Behavior >
System Settings > HTTP Proxy`.

**El emulador no arranca / muy lento**
Activa la virtualización de hardware (Intel HAXM / Hyper-V / AMD SVM)
en la BIOS y en Windows Features, o usa un dispositivo físico.

**Las fuentes o íconos no se ven en el `DesignSystemScreen`**
Asegúrate de que la sincronización de Gradle terminó sin errores (los
`.ttf` de `res/font/` y los `.svg` de `assets/vitta/` deben empaquetarse;
si faltan, `assembleDebug` fallará explícitamente, no en silencio).
