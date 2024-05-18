## 进阶技能

[English(AI Translation)](README-en_us.md)

### **需要Fabric Language Kotlin前置模组**

### 玩法介绍:

1. 玩家可以在获取**原版经验**的同时增加**技能经验**，每几级可以随机学习一个新技能。
2. 通过快捷键打开技能背包，装备技能到技能槽上，按下对应的快捷键进行使用技能，或者按住**快捷施法**快捷键并拖动鼠标使用技能。
3. 玩家默认有6个技能槽，其中3个**主动**技能槽，1个**通用**技能槽和2个**被动**技能槽。除了通用技能槽外，其他技能槽只能装备对应类型的技能。最高允许同时存在10个技能槽。
4. 玩家可以通过指令来修改技能槽的数量，以及技能槽的技能类型。
5. 使用技能无需消耗任何资源，但是大部分技能使用后有一个冷却时间。
6. 有些技能是持续的，需要长按以蓄力使用。但是如果是通过快捷施法使用的技能，无需长按，它会持续到结束，除非玩家手动再次使用技能以中断。

### 部分技能介绍:
1. **反弹类技能**: 以一定概率在一定时间内反弹首个任意伤害，不同类型的反弹效果不同。
2. **位移技能**: 有多种位移技能，包括冲刺、传送、闪避、跳跃、抓钩等。
3. **控制类技能**: 有多种控制类技能，包括禁锢、减速、沉默等。
4. **被动技能**: 除了被动技能以外的技能，均为主动技能。被动技能包括自愈、被动效果、爬墙、隐身等。
5. **强化类技能**: 有多种强化类技能，包括透视、无视液体、负面抵抗等。
6. **召唤类技能**: 例如分身、坐骑、仆从等。
7. **治疗类技能**: 不同等级的治疗技能可以恢复不同血量。
8. **破坏类技能**: 包括火球、TNT、陨石等。

### 指令介绍:
#### /skills
* equip [skill: 技能] [slot: 技能槽位] - 装备技能到技能槽
* unequip [slot: 技能槽] - 卸下技能槽上的技能
* list - 列出所有已学习技能
* learn [skill: 技能] - 学习新技能
* learn-all - 学习所有技能
* forget [skill: 技能] - 忘记技能
* forget-all - 忘记所有技能
* reset - 重置所有技能数据
* reset-cooldown - 重置所有技能的冷却时间
* slot
  * add [active: 主动/passive: 被动/generic: 通用] - 增加一个技能槽
  * remove [slot: 技能槽位] - 移除技能槽
  * reset - 重置至默认技能槽

#### /skill-xp
* add [amount: 数量] [points: 点数/levels: 等级] - 增加技能经验/等级
* set [amount: 数量] [points: 点数/levels: 等级] - 设置技能经验/等级
* query [points: 点数/levels: 等级] - 查询当前技能经验/等级

### 实用小技巧:
1. 在背包界面中，按下**查看技能列表**快捷键可以直接打开技能背包。
2. 创造模式可以在**技能列表**界面中快速学习所有技能和重置技能冷却(需要权限)。
3. 在**技能列表**界面中，左键双击技能可以跳转至图鉴的详细介绍。
4. **技能背包**界面可以通过快捷键(键盘上方的1-0)快速装备/交换技能，**shift+鼠标左键**可以快速装备/卸下技能。
5. 待补充。

#### 图片显示:

![](https://i.postimg.cc/8P7kHf9w/2024-05-18-19-36-03.png)
![](https://i.postimg.cc/25Dzrc6X/2024-05-18-19-36-08.png)

![](https://i.postimg.cc/wTnqbTdz/2024-05-18-19-36-27.png)
![](https://i.postimg.cc/XYFjMSVC/2024-05-18-19-36-34.png)

![](https://i.postimg.cc/yNy7dm36/2024-05-18-19-36-41.png)
![](https://i.postimg.cc/XJN4yr9f/2024-05-18-19-37-15.png)