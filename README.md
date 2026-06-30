# Sistema de Seguridad Ciudadana Arequipa

Sistema móvil desarrollado para apoyar la gestión de incidencias de seguridad ciudadana en Arequipa.  
La aplicación permite que los ciudadanos registren reportes con ubicación, que la inteligencia artificial clasifique el riesgo y la prioridad, y que un administrador asigne incidencias a policías o personal operativo.

El objetivo del proyecto es ofrecer una solución móvil para entidades públicas como municipalidades, serenazgo o comisarías, mejorando el registro, monitoreo y atención de incidencias ciudadanas.

---

## Integrantes

- César Augusto Kana Huillcapacco

---

## Stack tecnológico

- Kotlin
- Jetpack Compose
- Material Design 3
- Arquitectura MVVM
- StateFlow
- Coroutines
- Firebase Authentication
- Cloud Firestore
- Gemini API
- OpenStreetMap / osmdroid
- Android Studio

---

## Arquitectura MVVM

El proyecto está organizado siguiendo el patrón MVVM:

- **Model:** contiene las entidades principales como usuarios, incidencias, reportes y análisis IA.
- **Repository:** gestiona la comunicación con Firebase, Firestore, Gemini y servicios externos.
- **ViewModel:** contiene la lógica de negocio y estados de pantalla usando StateFlow.
- **View / Screens:** pantallas hechas con Jetpack Compose, observando estados del ViewModel.

Estructura principal:

```txt
data/
  model/
  repository/

viewmodel/

ui/
  screens/
  navigation/
  theme/