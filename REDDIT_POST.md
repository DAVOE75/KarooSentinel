# Reddit Post: Introducing Karoo Sentinel 🚨🚴

---

### **Suggested Title:**
> **[RELEASE] Karoo Sentinel: Free & Open-Source Anti-Theft Alarm & Live GPS Tracker for Hammerhead Karoo (Motion Detection, Telegram Push, Di2/AXS Arming)**

*(Alternative short title: `Introducing Karoo Sentinel – Turn your Hammerhead Karoo into a smart anti-theft bike alarm with live Telegram tracking & Di2 controls!`)*

---

### **Post Body (Markdown for Reddit):**

Hey everyone! 👋

Like many of you, I love mid-ride coffee stops and bakery pastries. But I *hate* that lingering anxiety of leaving a multi-thousand-dollar carbon bike leaning against a terrace or post, even for 60 seconds while paying at the counter. 

That’s why I built **Karoo Sentinel** — a 100% free, open-source security and anti-theft extension built specifically for the **Hammerhead Karoo (Karoo 2 & Karoo 3)** using the official Karoo SDK.

---

### 🛡️ What does Karoo Sentinel do?

It turns your head unit into an active digital sentry. When you hop off your bike to grab an espresso or refill water:
1. **Arm the system** in one tap on your screen — or right from your **SRAM AXS / Shimano Di2 "Bonus Buttons"** on your hoods (no need to take off winter gloves).
2. Sentinel records your bike’s exact 3D inertial orientation and sets an invisible GPS geofence.
3. If anyone touches, moves, or rolls your bike away:
   - 🔊 **The Karoo’s hardware beeper sounds an aggressive alarm siren** to deter the thief.
   - 🚨 **The screen flashes pulsating siren red** over the lock screen.
   - 📱 **You receive an immediate push alert on your smartphone via Telegram** with a direct Google Maps pin showing its live location, updating every 30 seconds if it's moving!

---

### ✨ Key Features at a Glance:

* 🚨 **Advanced Inertial Detection:** Leverages the Karoo's internal accelerometer to detect the slightest nudge, vibration, or lift. Sensitivity is fully adjustable (Low / Normal / High).
* 📍 **GPS Geofencing Fallback:** If the bike is gently loaded into a van or wheeled away, the 20-meter geofence triggers the alarm regardless of terrain vibrations.
* 🛰️ **Real-Time Live Tracking:** Sends instantaneous Telegram alerts and continuous live GPS coordinates so you can track where your bike is heading in real time.
* 🖲️ **Di2 & SRAM AXS Bonus Buttons:** You can map arming/disarming directly to your electronic shifting hood buttons (Single press or Long press with confirmation chirps).
* 🔒 **4-Digit Security PIN:** The thief cannot simply tap "Disarm" on the screen — they need your PIN to silence it.
* 🛑 **Anti-Shutdown Protection:** If a thief tries to hold the power button to turn off the Karoo, Sentinel intercepts the signal, blares the siren, and transmits an emergency alert with the last known GPS coordinate before shutting down.
* 📡 **Anti-Jammer & Sensor Disconnect Guard:** If someone tries to switch off or disconnect your Garmin Varia radar or paired Bluetooth sensors while armed, you receive an immediate critical alert.
* 🥷 **Silent Trap Mode:** Need stealth? Arm in silent mode — zero sound or light on the bike, but you get the instant Telegram alert so you can catch them in the act.
* 🌍 **Native Multi-Language Support:** Fully translated into English 🇬🇧, Spanish 🇪🇸, French 🇫🇷, German 🇩🇪, Italian 🇮🇹, and Portuguese 🇵🇹.

---

### 📥 How to Install (Takes ~2 minutes via ADB)

Because Sentinel interacts deeply with hardware beepers, system sensors, and lock screen services, it sideloads seamlessly via standard ADB:

1. Enable **Developer Options** on your Karoo (`Settings` > `About` > Tap `Build Number` 7 times).
2. Turn on **USB Debugging** (`Settings` > `Developer Options`).
3. Connect your Karoo to your PC/Mac via USB-C.
4. Download the latest `.apk` from GitHub and run:
   ```bash
   adb install KarooSentinel-v0.0.7.apk
   ```
5. Open Sentinel on your device, link your Telegram ID (takes 30 seconds via `@userinfobot`), and you’re ready to roll!

---

### 🔗 Links & Community:

* 💻 **GitHub Repository:** [https://github.com/DAVOE75/KarooSentinel](https://github.com/DAVOE75/KarooSentinel)
* 📦 **Download Latest Release:** [Releases on GitHub](https://github.com/DAVOE75/KarooSentinel/releases)
* ☕ **Support the Project:** If Sentinel gives you peace of mind during your rides, feel free to [Buy Me a Coffee](https://buymeacoffee.com/davoe75) to keep updates coming!

---

This is built for the cycling community, so I’d love to hear your feedback, thoughts, or feature requests! Let me know if you test it out on your weekend coffee ride! ☕🚴‍♂️💨
