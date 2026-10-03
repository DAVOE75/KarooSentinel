<p align="center">
  <img src="assets/logo.jpg" alt="Karoo Sentinel Logo" width="300"/>
</p>

# 🛡️ Karoo Sentinel (v0.0.5)

**Karoo Sentinel** es una extensión de seguridad y anti-robo de código abierto desarrollada específicamente para los ciclocomputadores **Hammerhead Karoo (2 y superior)**. Utiliza los sensores integrados del dispositivo para detectar movimientos no autorizados de la bicicleta, haciendo sonar una alarma física y alertando al usuario en su teléfono a través de Telegram.

---

## ✨ Características Principales

* **Detección de Movimiento Acelerométrica:** Utiliza el acelerómetro interno del Karoo para detectar la más mínima vibración o movimiento de la bicicleta cuando la alarma está armada.
* **Alerta Sonora Nativa (Hardware Beeper):** Envía patrones de pitidos de alta frecuencia directamente al "beeper" interno del dispositivo para disuadir posibles robos.
* **Notificaciones Instantáneas por Telegram:** Avisa en tiempo real directamente a tu smartphone a través de un bot de Telegram cuando se detecta movimiento.
* **Integración con SRAM AXS / Shimano Di2:** Permite armar y desarmar el sistema cómodamente desde los "Bonus Buttons" ocultos de tus manetas de cambio electrónico.
* **Interfaz de Usuario Premium:** Pantallas en modo oscuro optimizadas para la visibilidad al sol y ahorro de batería, con animaciones 3D, respuestas inmediatas sin latencia e indicadores de estado claros.
* **Modo "Destello de Sirena":** Al detectar intrusión, la pantalla del Karoo se enciende por encima de la pantalla de bloqueo y emite un destello pulsante de luz roja para llamar la atención.
* **Soporte Multi-Idioma (i18n):** Se adapta automáticamente al idioma del sistema del Karoo (Soporte nativo para: 🇪🇸 Español, 🇬🇧 Inglés, 🇫🇷 Francés, 🇮🇹 Italiano, 🇩🇪 Alemán, 🇵🇹 Portugués).

---

## 🚀 Cómo funciona

La aplicación funciona como un servicio persistente en segundo plano. Cuando aparcas la bici en la puerta de la cafetería:
1. Armas la alarma (desde la pantalla táctil de la App o tocando el botón de tu cambio Di2/SRAM).
2. La aplicación registrará la posición inercial exacta de la bicicleta.
3. Si alguien toca, mueve o intenta llevarse la bicicleta, los sensores detectarán una discrepancia inercial, haciendo que la pantalla parpadee en rojo, la bocina física empiece a pitar agresivamente y te llegue un mensaje a tu móvil inmediatamente.

---

## 📥 Instalación

Actualmente Karoo Sentinel debe instalarse en tu dispositivo mediante ADB (Android Debug Bridge), ya que aprovecha permisos de nivel de sistema y el SDK de Hammerhead.

1. Activa las **Opciones de Desarrollador** en tu Karoo (`Settings` > `About` > Toca 7 veces sobre `Build Number`).
2. Activa la **Depuración USB** (`Settings` > `Developer Options` > `USB Debugging`).
3. Conecta el Karoo a tu ordenador.
4. Descarga el último archivo `.apk` de la pestaña [Releases](https://github.com/DAVOE75/KarooSentinel/releases).
5. Abre una terminal y ejecuta el siguiente comando:
   ```bash
   adb install -r KarooSentinel-v0.0.5.apk
   ```

---

## ⚙️ Configuración y Uso

### Configurar Notificaciones (Telegram)
Para que las alertas lleguen a tu teléfono móvil, debes vincular tu ID:
1. Abre **Telegram** en tu teléfono y busca al bot `@userinfobot`.
2. Escríbele cualquier cosa. El bot te responderá con tu número de `Id` (Ejemplo: `595159484`).
3. Abre **Karoo Sentinel** en tu ciclocomputador.
4. Escribe ese número en la casilla de configuración y pulsa **GUARDAR**.

### Usar los botones SRAM AXS / Shimano Di2
No necesitas quitarte los guantes ni tocar la pantalla del GPS para armar la alarma al bajarte de la bici.
1. Ve a `Ajustes` (Settings) > `Sensores` (Sensors) en tu Karoo.
2. Selecciona tu grupo de cambios emparejado.
3. Busca el apartado de **Bonus Buttons** (Botones adicionales).
4. Elige si quieres configurar un toque o pulsación larga.
5. En la lista desplegable de acciones, selecciona **Alarma Anti-Robo** bajo el apartado de *Extensiones*.
6. ¡Listo! Pulsar ese botón armará/desarmará el sistema emitiendo un pitido de confirmación.

---

## 🛠️ Tecnologías

* **Kotlin** (Lenguaje principal).
* **Hammerhead Karoo SDK** (Para el control de hardware nativo y mapeo de Bonus Actions).
* **Corrutinas (Coroutines)** (Para la gestión asíncrona de los sensores, peticiones de red y emisión de alarmas de larga duración).
* **API Bot de Telegram** (Para envío asíncrono de alertas push).

---
*Desarrollado para la comunidad ciclista.* 🚴‍♂️💨
