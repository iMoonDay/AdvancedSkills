# Skill Configuration Guide

## Configuration File Location
1. Global config folder: `./config/advskills_re/skills`
2. Save-specific config folder: `./saves/[savename]/serverconfig/advskills_re/skills`

> Note:
> - If the config folder is empty or doesn't exist, enter any save first
> - To modify save-specific configs, copy the skill config file from the global folder
> - Save-specific configs override global configs

## Skill Parameters

### Basic Properties
| Parameter   | Description       | Values            |
|-------------|-------------------|-------------------|
| id          | Skill ID          | Non-modifiable    |
| name        | Skill Name        | Translatable text |
| description | Skill Description | Translatable text |
| icon        | Skill Icon        | Texture path      |
| cooldown    | Cooldown Time     | Integer(ticks)    |
| disabled    | Disable Skill     | true/false        |
| weight      | Skill Weight      | Integer           |
| drawable    | Can be Drawn      | true/false        |

### Skill Types (types)
Multiple selections allowed:
- attack: Attack
- defense: Defense
- utility: Utility
- control: Control
- passive: Passive
- enhancement: Enhancement
- summon: Summon
- restoration: Restoration
- movement: Movement
- destruction: Destruction

### Rarity Levels (rarity)
- common: Common
- uncommon: Uncommon
- rare: Rare
- superb: Superb
- epic: Epic
- legendary: Legendary
- mythic: Mythic
- unique: Unique

### Skill Parameters (parameters)
Each parameter includes:
- baseValue: Base value
  - Supported data types:
    - int: Integer
    - float: Float
    - double: Double
    - string: String
    - boolean: Boolean
    - Sound configuration:
      ```json
      {
        "sound_id": "sound ID",
        "range": "sound range(optional)"
      }
      ```
    - Lists: [int/float/double/boolean/string]
- enhancements: Available enhancement ID list(optional)

### Enhancement Configuration (enhancements)
Each enhancement includes:

| Parameter   | Description                                                     |
|-------------|-----------------------------------------------------------------|
| id          | Enhancement ID                                                  |
| name        | Enhancement Name                                                |
| description | Enhancement Description                                         |
| descArg     | Description Parameter Type(float/int/int_percent/float_percent) |
| valuePerLvl | Value Per Level                                                 |
| maxLevel    | Maximum Level                                                   |
| operation   | Enhancement Type                                                |
| weight      | Weight Configuration                                            |

#### Enhancement Types (operation)
- none: No level enhancement
- addition: Additive enhancement
- multiply_base: Multiplicative enhancement (base value)
- multiply_total: Multiplicative enhancement (total value)

#### Weight Configuration Types
1. Fixed Weight: Direct integer value
2. Random Weight:
   ```json
   {
     "min": "minimum value",
     "max": "maximum value"
   }
   ```
3. Level Weight:
   ```json
   {
     "multiplier": "multiplier coefficient"
   }
   ```
4. Custom Weight:
   ```json
   {
     "map": {
       "level1": "weight1",
       "level2": "weight2"
     },
     "default": "default weight"
   }
   ```
5. Expression Weight:
   ```json
   {
     "expression": "math expression(use {level} as level placeholder)"
   }
   ```
6. File Weight:
   ```json
   {
     "filePath": "external file path(use {level} as level placeholder)"
   }
   ```
7. Incremental Weight:
   ```json
   {
     "start": "initial weight",
     "increment": "increment value"
   }
   ```

## Reload Configuration
Use `/reload` command in-game to reload configuration files 