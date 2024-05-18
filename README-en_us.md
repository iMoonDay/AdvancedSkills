## Advanced Skills

[中文](README.md)

### **Requires Fabric Language Kotlin pre-module**

### Play Description.

1. Players can increase **Skill Experience** while gaining **Original Experience**, and can randomly learn a new skill every few levels.
2. Open the skill inventory by shortcut key, equip the skill to the skill slot, press the corresponding shortcut key to use the skill, or press and hold the **Quick Casting** shortcut key and drag the mouse to use the skill.
3. Players have 6 skill slots by default, including 3 **active** skill slots, 1 **general** skill slot and 2 **passive** skill slots. Except for the general skill slots, other skill slots can only be equipped with the corresponding type of skill. A maximum of 10 skill slots are allowed to exist at the same time.
4. Players can modify the number of skill slots and the types of skills in the skill slots through commands.
5. Skills do not consume any resources, but most skills have a cooldown after use. 6.
6. Some skills are continuous and require a long press to store power. However, if a skill is used through a quick cast, it does not require a long press and will last until the end of the skill, unless the player manually uses the skill again to interrupt it.

### Skills.
1. **Rebound Skills**: Bounces back the first damage with a certain probability within a certain period of time.
2. **Displacement Skills**: There are many types of displacement skills, including Dash, Teleport, Dodge, Jump, Grappling Hook, etc.
3. **Control Skills**: There are a variety of control skills, including Imprison, Slow, Silence, etc.
4. **Passive Skills**: Skills other than passive skills are active skills. Passive skills include self-healing, passive effects, wall climbing, stealth, etc.
5. **Enhancement Skills**: There are a variety of Enhancement Skills, including Perspective, Ignore Liquid, Negative Resistance, etc.
6. **Summoning Skills**: e.g. Split, Mount, Servant, etc.
7. **Healing Skills**: Different levels of healing skills can restore different amounts of blood.
8. **Destructive Skills**: Including Fireball, TNT, Meteorite, etc.

### Command Description.
#### /skills
* equip [skill] [slot] - equips a skill to a skill slot
* unequip [slot] - Unequip a skill on a skill slot
* list - Lists all learned skills
* learn [skill] - learn a new skill
* learn-all - learn all skills
* forget [skill] - forget a skill
* forget-all - Forget all skills
* reset - Reset all skill data
* reset-cooldown - Reset all skill cooldowns
* slot
  * add [active/passive/generic] - Add a skill slot
  * remove [slot] - removes a skill slot
  * reset - reset to default skill slot

#### /skill-xp
* add [amount] [points/levels] - adds skill experience/levels
* set [amount] [points/levels] - sets skill experience/levels
* query [points/levels] - query current skill experience/level

### Useful tips.
1. In the inventory screen, press **View Skill List** shortcut key to directly open the skill inventory.
2. Creative Mode allows you to quickly learn all skills and reset skill cooldowns in the **Skill List** screen (requires permission).
3. In the **Skill List** screen, left-click and double-click a skill to jump to the detailed description of the icon.
4. **Skill Backpack** screen can quickly equip/exchange skills by shortcut keys (1-0 above keyboard), **shift+left mouse button** can quickly equip/unequip skills.
5. To be added.

#### Image Display.

![](https://i.postimg.cc/SN2CHsHN/2024-05-18-20-44-53.png)
![](https://i.postimg.cc/Ssgcbzyj/2024-05-18-20-44-59.png)

![](https://i.postimg.cc/Kj4BGvzf/2024-05-18-20-45-09.png)
![](https://i.postimg.cc/vTj9cFxs/2024-05-18-20-45-16.png)

![](https://i.postimg.cc/kX0KCCdN/2024-05-18-20-45-20.png)
![](https://i.postimg.cc/XND92FHz/2024-05-18-20-45-53.png)