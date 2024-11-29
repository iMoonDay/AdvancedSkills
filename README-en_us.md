## Advanced Skills

[中文](README.md)

### Required Mods:

1. Fabric Language Kotlin (Fabric) / Kotlin for Forge (Forge)
2. Architectury API

### Gameplay Introduction:

1. Players can gain **Skill Experience** alongside **Vanilla Experience** (viewable in the skill list screen), and every few levels, they can randomly choose to learn a new skill.
2. Open the skill list with the hotkey (default K), equip skills to the skill slots, and press the corresponding hotkey to use the skill. Alternatively, hold the **Quick Cast** hotkey (default R) and move the mouse to select or use the skill. When a skill is selected, pressing the **Quick Cast** hotkey again will instantly release the skill.
3. Players start with 6 skill slots: 3 **Active** slots, 1 **Generic** slot, and 2 **Passive** slots. Except for the Generic slot, other slots can only equip corresponding types of skills. A maximum of 10 skill slots can be active at once.
4. Players can modify the number of skill slots and the types of skills in each slot via commands.
5. Using skills does not consume any resources, but most skills have a cooldown after use.
6. Some skills are continuous and require holding down the button to charge up. However, if the skill is cast using **Quick Cast**, no need to hold the button—it will persist until it ends, unless manually interrupted by using the skill again.

### Sample Skills Introduction:

1. **Reflective Skills**: Skills that have a certain chance of reflecting the first incoming damage within a specific time. Different types of reflective effects exist.
2. **Movement Skills**: Includes various movement abilities, such as dashing, teleporting, dodging, jumping, grappling, etc.
3. **Control Skills**: Includes various control abilities, such as immobilizing, slowing, silencing, etc.
4. **Passive Skills**: All skills that are not passive are considered active. Passive skills include self-healing, passive effects, wall climbing, invisibility, and more.
5. **Enhancement Skills**: Various enhancement skills, such as X-ray vision, water bypassing, and resistance to negative effects.
6. **Summoning Skills**: Includes summoning duplicates, mounts, minions, etc.
7. **Healing Skills**: Different healing skills restore varying amounts of health.
8. **Destructive Skills**: Includes fireballs, TNT, meteorites, etc.

### Command Overview:

#### /skills
- equip \[skill: skill id] \[slot: slot number (1-10)] - Equip a skill to a skill slot
- unequip \[slot: slot number (1-10)] - Unequip the skill from the skill slot
- list - List all learned skills
- learn \[skill: skill id] - Learn a new skill
- learn-all - Learn all skills
- forget \[skill: skill id] - Forget a skill
- forget-all - Forget all skills
- reset - Reset all skill data
- reset-cooldown - Reset the cooldown of all skills
- slot
  - add \[active: active/passive: passive/generic: generic] - Add a new skill slot
  - remove \[slot: slot number (1-10)] - Remove a skill slot
  - reset - Reset to the default skill slots
- xp
  - add \[amount: number] \[points: points/levels: levels] - Add skill experience or levels
  - set \[amount: number] \[points: points/levels: levels] - Set skill experience or levels
  - query \[points: points/levels: levels] - Query current skill experience or levels
  - reset - Reset skill experience and levels
- cooldown - Query global skill cooldown multiplier
  - reset - Reset global skill cooldown multiplier
  - \[multiplier: multiplier] - Set global skill cooldown multiplier
- modify \[skill: skill id]
  - cooldown \[seconds: cooldown time] - Modify the skill cooldown time
  - rarity \[rarity: rarity level] - Modify the skill rarity
  - time \[seconds: duration] - Modify the skill duration

### Useful Tips:

1. In the inventory screen, press the **View Skill List** hotkey (default K) to directly open the skill inventory.
2. In creative mode, you can quickly learn all skills and reset skill cooldowns in the **Skill List** screen (requires permission).
3. In the **Skill List** screen, left-click to double-click a skill to view its detailed description in the skill gallery. Hold Shift to display the full skill description.
4. In the **Skill Inventory** screen, you can quickly equip/swap skills using the hotkeys (1-0 on the top row of the keyboard), and **Shift + Left Click** can quickly equip or unequip skills.
5. In the **Quick Cast** screen, press the **Open/Close Inventory** hotkey (default E) to directly open the skill inventory.
6. In the **Quick Cast** screen, when a skill is selected, press the middle mouse button in the **Cancel** area to deselect the skill.

#### Images:

![Inventory](screenshots/skills.png)
![Skill Inventory](screenshots/inventory.png)
![Quick Cast](screenshots/wheel.png)
![Skill List](screenshots/list.png)
![Skill Gallery](screenshots/gallery.png)