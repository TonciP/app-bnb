# App BNB — Prueba práctica Android

Flujo de verificación en dos pantallas, hecho en **Java** con arquitectura **MVVM**:

1. **Información (paso 1 / 6)** — celular, carnet y checkbox "¿Tiene complemento?", con validación de longitud y tipo de dato.
2. **Activa tu ubicación** — bottom sheet previo al diálogo de permiso del sistema.
3. **Autenticación (paso 2 / 6)** — carrusel de 4 recomendaciones previas a la prueba, con lectura en voz alta (`TextToSpeech`) desde el ícono de bocina.

La prueba teórica respondida está en [`docs/prueba teorica.docx`](docs/prueba%20teorica.docx).

## Reglas de validación

| Campo | Regla |
|---|---|
| Número de celular | Obligatorio, solo dígitos, **8** caracteres |
| Número de carnet | Obligatorio, solo dígitos, **10** caracteres |
| Complemento | Opcional (se muestra al marcar "¿Tiene complemento?"); si se ingresa, **2** caracteres, solo letras y números (sin símbolos), en mayúsculas |

Se aplican en dos niveles: `InputFilter`s que impiden escribir/pegar caracteres inválidos y superar el máximo, y
`FormValidator` al pulsar *Siguiente*. Las longitudes viven en un solo lugar (`FormValidator`).

## Permiso de ubicación

Antes de consumir el servicio se verifica el permiso (`ACCESS_FINE_LOCATION` o `ACCESS_COARSE_LOCATION`;
basta uno, porque el usuario puede elegir ubicación aproximada):

- **Concedido** → se obtiene la ubicación y se llama al servicio.
- **No concedido** → bottom sheet "Activa tu ubicación" → *Continuar* → diálogo del sistema.
- **Denegado** → mensaje y el servicio **no** se consume. Si se marcó "No volver a preguntar", el mensaje ofrece
  abrir los ajustes de la app. Si el GPS está apagado, ofrece abrir los ajustes de ubicación.

## Arquitectura

Una sola `Activity` (`MainActivity`) que aloja un `NavHostFragment`; cada pantalla es un `Fragment`.

```
ui/            Fragments, ViewModels, Adapter (ViewPager2), bottom sheet     (ViewBinding + LiveData)
domain/        Modelos, validación y contratos (VerificationRepository, LocationProvider) — sin Android
data/          Retrofit/OkHttp (remote), repositorio y proveedor de ubicación
di/            AppContainer: inyección de dependencias manual
```

- **MVVM**: `InformationViewModel` mantiene el estado (`LiveData`) y decide el flujo; no conoce Views ni Context.
  El Fragment solo pinta el estado, consulta/solicita el permiso y se lo informa al ViewModel.
- **Eventos de una sola vez** (navegación, mensajes) con `Event<T>` para que no se repitan al rotar.
- **ViewBinding** en lugar de `findViewById`; los bindings se liberan en `onDestroyView`.
- **Retrofit + OkHttp + Gson** para el servicio REST (JSON). Las llamadas son cancelables y se cancelan en `onCleared()`.
- **Ubicación** con `LocationManager` (sin Google Play Services), con timeout y última ubicación conocida como respaldo.
- **Edge-to-edge** (obligatorio con `targetSdk 35`) resuelto con `InsetsHelper`, incluyendo el teclado.
- **Privacidad**: sin logging de peticiones (llevan datos personales), `allowBackup=false` y sin respaldo en la nube.

## Servicio REST

No se indicó un endpoint, así que se definió uno:

```
POST {BASE_URL}api/v1/verification/start
{ "phoneNumber": "...", "identityCard": "...", "complement": "..."|null, "latitude": 0.0, "longitude": 0.0 }
→ { "sessionId": "..." }
```

En **debug** (`USE_MOCK_API = true`) un interceptor responde localmente para poder recorrer todo el flujo sin backend.
Para usar un backend real, cambia `BASE_URL` y `USE_MOCK_API` en [`app/build.gradle`](app/build.gradle).

## Ejecutar

Requiere JDK 17+ y Android SDK 35.

```bash
./gradlew :app:testDebugUnitTest   # tests unitarios (validador y ViewModel)
./gradlew :app:assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

`minSdk 24` · `targetSdk 35` · Java 17.

## Alcance

El botón *Siguiente* del paso 2 recorre las recomendaciones; los pasos 3 a 6 no forman parte de la prueba. El total de pasos (6) sale del diseño y está en `StepHeader.TOTAL_STEPS`.
