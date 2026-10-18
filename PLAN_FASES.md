# PLAN DE FASES — Reestructuración de TraductorLSV

> **Versión**: 1.0
> **Estado**: Aprobado para ejecución
> **Objetivo general**: Convertir la app actual (una única pantalla de cámara) en una
> aplicación estructurada con **Splash → Onboarding → Home (Dashboard) → Traductor →
> Entrenamiento → Juegos → Estadísticas → Perfil**, sin romper la lógica existente
> de MediaPipe / KNN / DTW.

---

## 0. VISIÓN Y PRINCIPIOS

### 0.1 Estado actual
- `MainActivity` arranca directamente en la cámara.
- Toda la UI vive en un único `Composable` (`CameraScreen`).
- No hay navegación, ni persistencia de progreso, ni gamificación.
- Estilo visual: Material 3 plano (aún no aplicado el rediseño Duolingo).

### 0.2 Estado objetivo
Una app educativa de Lengua de Señas Venezolana con:

1. **Onboarding** la primera vez que se abre.
2. **Dashboard** con progreso del usuario.
3. **Módulo Traductor** (cámara en tiempo real, como hoy).
4. **Módulo Entrenamiento** (crear/gestionar el dataset de señas).
5. **Módulo Juegos** (adivinanzas, memoria, velocidad).
6. **Estadísticas** (racha, aciertos, precisión).
7. **Perfil / Ajustes** (tema, sonido, idioma, reset).
8. **Persistencia local** robusta (Room) de progreso, logros y dataset.

### 0.3 Principios rectores
- **P1 — No romper lo que funciona**: MediaPipe, KNN y DTW se mueven pero no se reescriben.
- **P2 — Cada fase debe compilar, correr y ser testeable**.
- **P3 — Una fase a la vez**, con validación antes de avanzar.
- **P4 — Estilo visual Duolingo** unificado desde Fase 2 en adelante.
- **P5 — Persistencia siempre local** (no backend en esta etapa).
- **P6 — Código limpio**: ViewModel + Repository + UI desacoplada.

---

## 1. ARQUITECTURA OBJETIVO

### 1.1 Estructura de paquetes

```text
com.example.traductorlsv/
├── MainActivity.kt                  # Solo host de NavHost
├── TraductorLSVApp.kt               # Application class (DI manual)
│
├── core/                            # Sin dependencias de UI
│   ├── classifier/                  # ← movido desde raíz
│   │   ├── GestureClassifier.kt
│   │   └── model/GestureSample.kt
│   ├── camera/
│   │   ├── GestureRecognizerHelper.kt
│   │   └── ImageProxyExt.kt
│   ├── data/                        # NUEVO
│   │   ├── local/                   # Room DB
│   │   │   ├── TraductorDatabase.kt
│   │   │   ├── dao/ProgressDao.kt
│   │   │   ├── dao/AchievementDao.kt
│   │   │   └── entity/*.kt
│   │   ├── repository/
│   │   │   ├── ProgressRepository.kt
│   │   │   ├── DatasetRepository.kt
│   │   │   └── PreferencesRepository.kt
│   │   └── preferences/DataStoreKeys.kt
│   └── domain/                      # NUEVO
│       ├── model/Game.kt
│       ├── model/Achievement.kt
│       └── usecase/*.kt
│
├── navigation/                      # NUEVO
│   ├── Routes.kt
│   ├── NavGraph.kt
│   └── BottomNavItem.kt
│
├── ui/
│   ├── theme/                       # Ya existe (Duolingo)
│   ├── components/                  # Ya existe (DuolingoButton, DuoCard…)
│   │   └── … (añadir: DuoProgressBar, DuoTopBar, DuoDialog, DuoBadge)
│   ├── screens/
│   │   ├── splash/SplashScreen.kt
│   │   ├── onboarding/OnboardingScreen.kt
│   │   ├── home/HomeScreen.kt
│   │   ├── home/HomeViewModel.kt
│   │   ├── translator/TranslatorScreen.kt
│   │   ├── translator/TranslatorViewModel.kt
│   │   ├── trainer/TrainerScreen.kt
│   │   ├── trainer/TrainerViewModel.kt
│   │   ├── games/GamesHubScreen.kt
│   │   ├── games/guess/GuessGameScreen.kt
│   │   ├── games/guess/GuessGameViewModel.kt
│   │   ├── games/memory/MemoryGameScreen.kt
│   │   ├── games/memory/MemoryGameViewModel.kt
│   │   ├── games/speed/SpeedGameScreen.kt
│   │   ├── games/speed/SpeedGameViewModel.kt
│   │   ├── stats/StatsScreen.kt
│   │   ├── stats/StatsViewModel.kt
│   │   ├── profile/ProfileScreen.kt
│   │   └── profile/ProfileViewModel.kt
│   └── overlay/
│       └── HandLandmarkOverlay.kt   # Ya existe (mover aquí)
```

### 1.2 Grafo de navegación

```text
[ Splash ]
    │
    ├── (primera vez) ──▶ [ Onboarding ] ──▶ [ Home ]
    └── (recurrente) ───▶ [ Home ]

[ Home ]  ◀─── bottom nav ───▶  [ Juegos ]  [ Stats ]  [ Perfil ]
   │                                │
   │                                ├─▶ [ Guess Game ]
   │                                ├─▶ [ Memory Game ]
   │                                └─▶ [ Speed Game ]
   │
   ├─▶ [ Traductor ]  (cámara live)
   └─▶ [ Entrenador ] (dataset builder)
```

### 1.3 Dependencias nuevas a agregar (`libs.versions.toml`)

| Librería                              | Uso                              |
|---------------------------------------|----------------------------------|
| `androidx.navigation:navigation-compose` | Navegación entre pantallas    |
| `androidx.room:room-runtime` / `-ktx` | Persistencia de progreso         |
| `androidx.room:room-compiler` (KSP)   | Compilador Room                  |
| `androidx.datastore:datastore-preferences` | Preferencias ligeras        |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | ViewModels en Compose |
| `androidx.compose.animation:animation` | Transiciones                    |
| `io.coil-kt:coil-compose`             | Avatares / iconos (opcional)     |

---

## 2. REGLAS GLOBALES DEL PROYECTO

> Estas reglas aplican a **todas las fases**. Cualquier incumplimiento invalida la fase.

### R1 — No romper la lógica existente
`GestureClassifier.kt`, `GestureRecognizerHelper.kt`, `CameraPreview.kt` y
`HandLandmarkOverlay.kt` **solo se mueven de carpeta**. Su contenido lógico
(algoritmos KNN, DTW, extracción de landmarks) **no se altera** en ninguna fase
salvo la Fase 9 (extracción a `core/`).

### R2 — Cada fase debe compilar y ejecutarse
Al terminar cada fase, `./gradlew assembleDebug` debe pasar y la app debe
instalarse y funcionar en un dispositivo/emulador. Si no compila, la fase no está
terminada.

### R3 — Una fase a la vez
No se inicia la Fase N+1 hasta que la Fase N esté **validada y aprobada** por el
responsable del proyecto.

### R4 — Estilo visual unificado (Duolingo)
Toda pantalla nueva **debe** usar:
- `TraductorLSVTheme` (paleta Duolingo, ya definida).
- Componentes reutilizables: `DuolingoButton`, `DuoCard`, `DuoTextField`.
- Tipografía `Nunito` (o SansSerif con pesos altos).
- Animaciones suaves (transiciones de 200–300 ms).

### R5 — Cero lógica en Composables
Toda la lógica de negocio debe vivir en `ViewModel`. Los Composables solo:
- Reciben estado inmutable.
- Emiten eventos (`onClick`, `onValueChange`).
- No acceden a `Context` para lógica (solo para recursos).

### R6 — Persistencia local, no backend
Todo el progreso, dataset y logros se guardan localmente (Room / DataStore).
No se introduce backend remoto en estas fases.

### R7 — Tests mínimos por fase
Cada fase debe incluir **al menos**:
- 1 test unitario de ViewModel (si aplica).
- 1 test de compilación de la navegación (que la ruta exista).

### R8 — Nombres y estructura
- Composable de pantalla: `XxxScreen`.
- ViewModel: `XxxViewModel`.
- Ruta: constante en `Routes.kt` (sealed class o `const val`).
- No usar nombres genéricos como `MainScreen` o `Screen1`.

### R9 — Documentación por fase
Cada fase entrega un archivo `docs/fases/FASE_XX.md` con:
- Objetivo.
- Archivos creados/modificados.
- Capturas o descripción de la UI.
- Cómo probar manualmente.
- Resultado de `./gradlew test` y `assembleDebug`.

### R10 — Control de versiones
Cada fase se desarrolla en su propia rama `feature/fase-XX-nombre`.
Al aprobarse, merge a `develop`. `main` solo recibe releases estables.

---

## 3. FASES

---

### 🟦 FASE 0 — Preparación y análisis

**Objetivo**: Preparar el terreno sin tocar la app aún.

**Duración estimada**: 0.5 día.

**Entregables**:
- `docs/fases/FASE_00.md`
- `docs/PLAN_FASES.md` (este documento)
- `docs/ARQUITECTURA.md` (resumen del árbol de paquetes)
- Inventario del código actual (qué archivos existen, qué hacen).
- Confirmación de versiones (`libs.versions.toml`) y de que el proyecto compila
  tal cual está hoy.

**Criterios de aceptación**:
- [ ] `./gradlew assembleDebug` pasa sin tocar nada.
- [ ] Documento `ARQUITECTURA.md` aprobado.
- [ ] Se identifican explícitamente los archivos "intocables" (R1).

**No incluye**: código nuevo.

---

### 🟦 FASE 1 — Infraestructura de navegación

**Objetivo**: Añadir Navigation-Compose y convertir `MainActivity` en un host de
`NavHost`. La app sigue mostrando la cámara, pero ahora como una ruta más.

**Dependencias**: Fase 0.

**Entregables**:
- `navigation/Routes.kt` (sealed class `Route`).
- `navigation/NavGraph.kt`.
- `MainActivity.kt` refactorizado: solo `setContent { TraductorNavHost() }`.
- Nuevo paquete `ui/screens/translator/TranslatorScreen.kt` que encapsula lo que
  hoy es `CameraScreen`.
- Se elimina `CameraScreen` de `MainActivity.kt`.

**Criterios de aceptación**:
- [ ] Al abrir la app se navega a `Route.Translator` (temporalmente, hasta Fase 3).
- [ ] La cámara y el reconocimiento funcionan **exactamente igual** que antes.
- [ ] No hay regresiones en KNN/DTW/MediaPipe.
- [ ] `./gradlew test` pasa.

**Riesgos**:
- MediaPipe puede quejarse si el ciclo de vida cambia; verificar `DisposableEffect`.

---

### 🟦 FASE 2 — Splash Screen + Onboarding

**Objetivo**: Añadir pantalla de carga y onboarding para usuarios nuevos.

**Dependencias**: Fase 1.

**Entregables**:
- `ui/screens/splash/SplashScreen.kt` (logo + nombre + animación).
- `ui/screens/onboarding/OnboardingScreen.kt` (3–4 slides con `HorizontalPager`).
- `core/data/preferences/` con `DataStore` que guarde `has_seen_onboarding: Boolean`.
- Flujo: `Splash` → (si `!hasSeenOnboarding`) `Onboarding` → `Home`.
- `SplashViewModel` para decidir la ruta siguiente.

**Slides del onboarding** (contenido sugerido):
1. "Bienvenido a TraductorLSV" — presentación.
2. "Aprende el abecedario en señas" — features.
3. "Juega y gana estrellas" — gamificación.
4. "Permite la cámara para comenzar" — solicitud de permiso.

**Criterios de aceptación**:
- [ ] El splash dura mínimo 800 ms para evitar parpadeo.
- [ ] El onboarding solo aparece la primera vez.
- [ ] Se solicita permiso de cámara **en el onboarding**, no en el traductor.
- [ ] Botón "Saltar" y "Siguiente" con `DuolingoButton`.

---

### 🟦 FASE 3 — Home / Dashboard

**Objetivo**: Crear el panel de inicio con `BottomNavigationBar` de 4 items.

**Dependencias**: Fase 2.

**Entregables**:
- `navigation/BottomNavItem.kt` (Home, Juegos, Stats, Perfil).
- `ui/screens/home/HomeScreen.kt` con:
  - Saludo personalizado ("¡Hola, [nombre]!").
  - Barra de progreso del día (ej. "3/5 señas hoy").
  - Tarjetas grandes: **Traductor**, **Entrenador**, **Jugar**.
  - Racha actual (🔥 días).
  - Botón "Continuar lección" destacado.
- `ui/screens/home/HomeViewModel.kt`.
- `ui/components/DuoProgressBar.kt` (barra verde estilo Duolingo).
- `ui/components/DuoTopBar.kt`.
- Modificación de `NavGraph.kt`: `Home` pasa a ser el destino inicial.

**Criterios de aceptación**:
- [ ] La app **ya NO abre en la cámara**, abre en Home.
- [ ] El botón "Traductor" navega a `Route.Translator` y funciona.
- [ ] El botón "Entrenador" navega a `Route.Trainer` (placeholder hasta Fase 5).
- [ ] Los items de bottom nav cambian de color al seleccionarse (verde activo, gris inactivo).
- [ ] Botón atrás desde Traductor vuelve a Home.

---

### 🟦 FASE 4 — Módulo Traductor (refactor visual)

**Objetivo**: Aplicar estilo Duolingo a la pantalla de traducción y limpiar su
ViewModel.

**Dependencias**: Fase 3.

**Entregables**:
- `ui/screens/translator/TranslatorScreen.kt` rediseñado (DuolingoButton, DuoCard).
- `ui/screens/translator/TranslatorViewModel.kt` que:
  - Expone `StateFlow<TranslatorUiState>`.
  - Contiene la lógica que hoy vive en `CameraScreen` (buffers, clasificación).
- `DuoTopBar` con botón "volver a Home".
- Panel inferior con tarjeta blanca tipo Duolingo mostrando la seña detectada.

**Criterios de aceptación**:
- [ ] El reconocimiento funciona idéntico a hoy (misma precisión).
- [ ] El ViewModel no referencia `Context` para lógica (solo para MediaPipe init).
- [ ] La UI no contiene `if (handLandmarks != null)` con lógica — todo via StateFlow.
- [ ] Botón atrás vuelve a Home.

---

### 🟦 FASE 5 — Módulo Entrenador (separar del traductor)

**Objetivo**: Sacar el "modo entrenamiento" del traductor y darle su propia
pantalla con mejor UX.

**Dependencias**: Fase 4.

**Entregables**:
- `ui/screens/trainer/TrainerScreen.kt`:
  - Lista de señas ya grabadas (grid de tarjetas).
  - Botón "+ Nueva seña".
  - Formulario: etiqueta + botón "Grabar 1 s".
  - Barra de progreso animada durante la grabación.
  - Botón "Borrar" por seña con confirmación (`DuoDialog`).
- `ui/screens/trainer/TrainerViewModel.kt`.
- `ui/components/DuoDialog.kt` (alertas estilo Duolingo).
- `core/data/repository/DatasetRepository.kt` (wrapper de `GestureClassifier`).

**Criterios de aceptación**:
- [ ] Se pueden crear, ver y borrar señas.
- [ ] El dataset persiste entre cierres de la app.
- [ ] Se muestra el total de muestras en cada tarjeta.
- [ ] Se confirma antes de borrar (evita accidentes).

---

### 🟦 FASE 6 — Módulo de Juegos (Hub)

**Objetivo**: Crear el hub de juegos y la infraestructura común.

**Dependencias**: Fase 5.

**Entregables**:
- `ui/screens/games/GamesHubScreen.kt`:
  - Grid 2x2 con tarjetas de juegos:
    - 🎯 **Adivinanza** — haz la seña que te pide.
    - 🧠 **Memoria** — repite la secuencia de señas.
    - ⚡ **Velocidad** — cuántas señas aciertas en 30 s.
    - 🏆 **Desafío** — (deshabilitado hasta Fase 10).
  - Cada tarjeta muestra: icono, nombre, mejor puntaje, candado si bloqueado.
- `core/domain/model/Game.kt` (enum con `id`, `title`, `icon`, `unlockedBy`).
- `ui/components/DuoBadge.kt`.
- Navegación desde el hub a placeholders de cada juego.

**Criterios de aceptación**:
- [ ] El hub carga sin errores.
- [ ] Cada tarjeta navega a su pantalla (aunque sea placeholder).
- [ ] El estilo es consistente con el resto (Duolingo).

---

### 🟦 FASE 7 — Juego: Adivinanza

**Objetivo**: El juego estrella. La app muestra una letra/seña objetivo, el
usuario debe hacerla frente a la cámara y el sistema la valida con KNN/DTW.

**Dependencias**: Fase 6.

**Entregables**:
- `ui/screens/games/guess/GuessGameScreen.kt`:
  - Arriba: "Haz la seña: **A**" (letra grande, tipografía display).
  - En medio: cámara en vivo + overlay de landmarks.
  - Abajo: barra de tiempo o contador de intentos.
  - Feedback visual: ✅ acierto (verde + animación), ❌ error (rojo + shake).
- `ui/screens/games/guess/GuessGameViewModel.kt`:
  - Estados: `Idle`, `Playing(target)`, `Success`, `Fail`.
  - Reutiliza `GestureClassifier.classifySequence`.
  - Genera 10 rondas aleatorias del dataset del usuario.
- `core/domain/usecase/PlayGuessRoundUseCase.kt`.

**Criterios de aceptación**:
- [ ] El juego usa **el dataset del usuario** (no uno hardcodeado).
- [ ] Detecta correctamente cuando el usuario hace la seña esperada.
- [ ] Al terminar 10 rondas muestra resumen (aciertos/errores/tiempo).
- [ ] El progreso se guarda (aunque estadísticas completas llegan en Fase 9).

**Riesgos**:
- Umbral de confianza DTW: puede requerir calibración. Documentar parámetros.

---

### 🟦 FASE 8 — Juego: Memoria y Juego: Velocidad

**Objetivo**: Añadir dos juegos más para variedad.

**Dependencias**: Fase 7.

**Entregables**:

**Memoria** (`MemoryGameScreen`):
- Muestra 3–5 señas aleatorias en secuencia (video/imagen pregrabada o icono).
- El usuario debe repetirlas en el mismo orden.
- Cada acierto suma un punto; cada fallo reinicia la ronda.

**Velocidad** (`SpeedGameScreen`):
- 30 segundos en el reloj.
- Aparece una seña aleatoria cada vez que el usuario acierta.
- Contador de aciertos en grande.
- Al terminar, muestra puntaje y "mejor puntaje histórico".

**Criterios de aceptación**:
- [ ] Ambos juegos reutilizan la misma infraestructura del juego Adivinanza.
- [ ] El mejor puntaje se persiste.
- [ ] El juego de velocidad tiene animación de cuenta regresiva.

---

### 🟦 FASE 9 — Persistencia robusta (Room + progreso)

**Objetivo**: Migrar de JSON plano a Room y guardar estadísticas del usuario.

**Dependencias**: Fase 8.

**Entregables**:
- `core/data/local/TraductorDatabase.kt` (Room).
- Entidades:
  - `ProgressEntity` (fecha, aciertos, errores, tiempo).
  - `AchievementEntity` (id, unlockedAt).
  - `StreakEntity` (última vez, días consecutivos).
- DAOs correspondientes.
- `ProgressRepository`, `AchievementRepository`.
- Migrar `GestureClassifier` para leer del `DatasetRepository` (Room) en lugar
  de `dtw_dataset.json` (con migración automática del JSON existente la primera
  vez).
- Registrar eventos desde Traductor y Juegos.

**Criterios de aceptación**:
- [ ] El JSON actual se importa automáticamente la primera vez.
- [ ] Los juegos guardan aciertos/errores.
- [ ] La racha (streak) se actualiza al abrir la app.
- [ ] No hay pérdida de datos al actualizar la app.

**Riesgos**:
- Migración de datos: hacer backup del JSON antes de borrarlo.

---

### 🟦 FASE 10 — Estadísticas y Perfil

**Objetivo**: Pantallas finales del bottom nav.

**Dependencias**: Fase 9.

**Entregables**:

**Stats**:
- `ui/screens/stats/StatsScreen.kt`:
  - Racha actual y mejor racha.
  - Total de señas aprendidas.
  - Gráfico de barras: aciertos por día (últimos 7 días).
  - Logros (grid de badges desbloqueados/bloqueados).

**Perfil**:
- `ui/screens/profile/ProfileScreen.kt`:
  - Avatar + nombre editable.
  - Nivel (basado en XP acumulado).
  - Ajustes: sonido on/off, vibración, tema.
  - Botón "Reiniciar progreso" (con confirmación).
  - Botón "Reentrenar dataset" (borra todo).

**Criterios de aceptación**:
- [ ] Los gráficos usan `Canvas` de Compose (sin librerías externas pesadas).
- [ ] Los ajustes persisten en DataStore.
- [ ] Reiniciar progreso no borra el dataset (son cosas distintas).

---

### 🟦 FASE 11 — Pulido, animaciones y QA

**Objetivo**: Dejar la app lista para release.

**Dependencias**: Fase 10.

**Entregables**:
- Animaciones de transición entre pantallas (`AnimatedNavHost`).
- Animación de éxito en juegos (confetti + sonido).
- Animación de "shake" en errores.
- Accesibilidad: content descriptions, tamaños táctiles ≥ 48 dp.
- Tests de UI (Compose Test) para los flujos principales.
- Manual de usuario en `docs/MANUAL_USUARIO.md`.
- APK firmado de release.

**Criterios de aceptación**:
- [ ] `./gradlew test connectedAndroidTest` pasa.
- [ ] Ninguna pantalla tarda más de 300 ms en transicionar.
- [ ] La app no crashea en rotación de pantalla.
- [ ] Se entrega APK `traductorlsv-v1.0.apk`.

---

## 4. CRITERIOS DE ACEPTACIÓN GLOBALES

Al finalizar **todas** las fases, la app debe cumplir:

- [ ] Abre en Splash → Onboarding (primera vez) → Home.
- [ ] Nunca abre directamente en la cámara.
- [ ] Tiene bottom nav funcional con 4 items.
- [ ] El módulo Traductor funciona igual o mejor que la versión actual.
- [ ] El módulo Entrenador permite gestionar el dataset completo.
- [ ] El módulo Juegos tiene 3 juegos funcionales.
- [ ] El progreso y logros persisten.
- [ ] El estilo visual es coherente (Duolingo) en toda la app.
- [ ] No hay crashes, ANRs ni warnings graves en el log.
- [ ] El tamaño del APK no supera los 30 MB.

---

## 5. ORDEN DE EJECUCIÓN Y DEPENDENCIAS

| Fase | Nombre                        | Depende de | Bloquea a |
|------|-------------------------------|------------|-----------|
| 0    | Preparación y análisis        | —          | 1         |
| 1    | Infraestructura de navegación | 0          | 2         |
| 2    | Splash + Onboarding           | 1          | 3         |
| 3    | Home / Dashboard              | 2          | 4, 5, 6   |
| 4    | Traductor (refactor visual)   | 3          | 5         |
| 5    | Entrenador                    | 3, 4       | 6         |
| 6    | Hub de Juegos                 | 3, 5       | 7         |
| 7    | Juego: Adivinanza             | 6          | 8, 9      |
| 8    | Juegos: Memoria y Velocidad   | 7          | 9         |
| 9    | Persistencia robusta (Room)   | 8          | 10        |
| 10   | Estadísticas y Perfil         | 9          | 11        |
| 11   | Pulido, animaciones y QA      | 10         | release   |

---

## 6. RIESGOS IDENTIFICADOS Y MITIGACIONES

| Riesgo                                              | Probabilidad | Mitigación                                                        |
|-----------------------------------------------------|--------------|-------------------------------------------------------------------|
| MediaPipe crashea al recrear la Activity            | Media        | Usar `DisposableEffect` correcto, no recrear el helper sin motivo |
| Migración de JSON a Room pierde datos               | Baja         | Backup automático del JSON antes de la migración                  |
| El ViewModel retiene `Context` y hay leak           | Media        | Usar `AndroidViewModel` solo si es imprescindible, `Application`  |
| El umbral DTW no calibra bien en juegos             | Alta         | Añadir slider de sensibilidad en Perfil (Fase 10)                 |
| Onboarding molesta a usuarios recurrentes           | Baja         | Bandera en DataStore, se muestra solo una vez                     |
| El APK crece demasiado por Room + Navigation        | Baja         | R8/ProGuard en release, verificar tamaño en Fase 11               |

---

## 7. GLOSARIO

- **Dashboard / Home**: Pantalla principal con acceso a módulos.
- **Hub**: Pantalla intermedia que agrupa opciones (ej. hub de juegos).
- **Onboarding**: Pantalla introductoria para primeros usos.
- **Racha / Streak**: Días consecutivos de uso.
- **XP**: Puntos de experiencia acumulados.
- **Ronda**: Una pregunta/intento dentro de un juego.
- **DTW**: Dynamic Time Warping (algoritmo de comparación de secuencias).
- **KNN**: K-Nearest Neighbors (clasificador de señas estáticas).

---

## 8. CHECKLIST RÁPIDO ANTES DE CERRAR CADA FASE

- [ ] El código compila (`./gradlew assembleDebug`).
- [ ] Los tests pasan (`./gradlew test`).
- [ ] Se probó manualmente en dispositivo/emulador.
- [ ] Se actualizó `docs/fases/FASE_XX.md`.
- [ ] Se revisó que no se rompió nada de fases anteriores.
- [ ] Se siguió el estilo Duolingo.
- [ ] No hay `TODO` sin resolver dejados en el código de la fase.
- [ ] El commit sigue la convención `feat(fase-XX): descripción corta`.

---

**Fin del documento.**