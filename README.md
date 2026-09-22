# Huey Helper
A premium, all-in-one RuneLite plugin designed to track live fight stats, dynamic session data, and loot for **The Hueycoatl**.

Whether you are soloing, doing small groups, or grinding in a 20-man mass, Huey Helper completely takes the guesswork out of your damage contribution and drop eligibility.

## Features

### ⚔️ Live Fight Stats & Eligibility Tracking
Stop wondering if you've done enough damage. The dynamic side panel tracks exactly how much damage you have dealt to both the Body (Phase 1) and the Head/Tail (Phase 2).
* Automatically calculates if you are eligible for a drop based on the size of your team (Solo, Group, or Mass).
* Instantly calculates your exact rare drop rates based on your personal damage contribution.
* UI text dynamically shifts from Red to Green the moment you hit your damage thresholds.

### 📊 Dynamic InfoBox Overlay
A custom, lag-free InfoBox overlay sits directly on your screen so you never have to look away from the fight.
* Displays a compact `P 1` and `P 2` damage tracker.
* Hover over the InfoBox at any time for a highly detailed tooltip showing your Session Kills, Kills Per Hour (KPH), Average Kill Time, and Fastest Kill Time.

### ⏱️ True-Tick Session & Timer Stats
Built-in stopwatch logic reads RuneScape's official server-tick kill times directly from the chat box to give you 100% accurate session data.
* **Smart Filtering:** The plugin automatically ignores failed attempts (teleporting out early or missing the damage threshold) so your KPH and Average Time are never skewed by bad runs.

### 💰 Automated CSV Boss Logging
Every single kill is automatically parsed and saved to a `.csv` file on your local machine (`.runelite/Huey_Helper_Session_Logs/`).
* Tracks Date, Kill Count, Participants, Damage, exact Kill Times, MVP status, and complete Personal & Group Loot records.
* Easily open your logs directly from the side panel!

### 🎨 Fully Customizable UI
* **Custom Fonts:** Choose from multiple built-in RuneLite fonts or premium custom fonts (like Quill or Bebas Neue) to make your panel look exactly how you want.
* **Collapsible Menus:** Keep your sidebar clean by collapsing the Loot Tracker, Session Stats, or Fight Stats.

---

## 🐉 Drop Mechanics & Eligibility Tracking
Thanks to extensive session logging and research, Huey Helper tracks your drop eligibility using the exact mechanics utilized by the game servers. The requirements for receiving loot change dynamically based on the size of your team:

### Small Groups (1 to 5 Players)
To be eligible for a drop, you must meet **both** damage thresholds:
* Deal **25+ damage** in Phase 1 (Body)
* Deal **75+ damage** in Phase 2 (Head/Tail)

### Masses (6+ Players)
Jagex implements a "mass protection" mechanic for larger groups. To be eligible for a drop, you must meet **either** of the following:
* Deal **25+ damage** in Phase 1 (Body)
* **OR** deal **75+ Total Damage** combined across the entire fight.

### The 95% Penalty
If you reach the 75 Total Damage threshold in a mass, but fail to deal 25 damage to the body in Phase 1, you will still receive a drop! However, your drop quantity and pet chance will be penalized by **95%**, dropping you to the minimum **5% loot floor**. Huey Helper automatically detects this penalty, adjusts your expected reward percentage, and correctly recalculates your rare drop rates to reflect the penalty.

---

## ⚠️ Known Limitations: Mass Lobbies & Overkill Damage
Due to how Old School RuneScape works server side, damage tracking relies on local hitsplats:
* **Server PID Priority:** If your attack lands on the final tick, your client may display a full hitsplat (e.g., 25 damage), but the server may award the remaining boss HP to another player based on PID priority, although the plugin tracks this damage, you'd technically contribute 0 damage server side.
* **Tracking Impact:** In rare instances during mass kills, the overlay may show that you met the Phase 1/Phase 2 threshold even if the server registered your actual contribution as lower, potentially causing you to fail the kill.

> **Note:** This is a standard client-side estimation constraint across OSRS tracking plugins and does not affect your actual server-side loot eligibility or drop rates.

## Support & Feedback
If you encounter any bugs, have feature requests, or want to contribute, feel free to open an issue on the GitHub repository!

**Author:** Jufinda