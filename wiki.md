# 技能配置指南

## 配置文件位置
1. 全局配置文件夹: `./config/advskills_re/skills`
2. 存档配置文件夹: `./saves/[存档]/serverconfig/advskills_re/skills`

> 注意：
> - 如果配置文件夹为空或不存在，需先进入一次任意存档
> - 如果要修改存档配置，需从全局配置文件夹复制相应技能配置文件
> - 存档配置将会覆盖全局配置

## 技能参数说明

### 基础属性
| 参数          | 说明    | 可选值        |
|-------------|-------|------------|
| id          | 技能ID  | 不可修改       |
| name        | 技能名称  | 可翻译文本      |
| description | 技能描述  | 可翻译文本      |
| icon        | 技能图标  | 材质路径       |
| cooldown    | 冷却时间  | 整数(tick)   |
| disabled    | 是否禁用  | true/false |
| weight      | 技能权重  | 整数         |
| drawable    | 是否可抽取 | true/false |

### 技能类型 (types)
可多选：
- attack: 攻击类
- defense: 防御类
- utility: 实用类
- control: 控制类
- passive: 被动类
- enhancement: 强化类
- summon: 召唤类
- restoration: 恢复类
- movement: 移动类
- destruction: 破坏类

### 稀有度 (rarity)
- common: 普通
- uncommon: 罕见
- rare: 稀有
- superb: 卓越
- epic: 史诗
- legendary: 传奇
- mythic: 神话
- unique: 无双

### 技能参数 (parameters)
每个参数包含：
- baseValue: 基础值
  - 支持的数据类型：
    - int: 整型
    - float: 浮点型
    - double: 双精度型
    - string: 字符串
    - boolean: 布尔型
    - 音效配置:
      ```json
      {
        "sound_id": "音效ID",
        "range": "音效范围(可选)"
      }
      ```
    - 列表类型: [int/float/double/boolean/string]
- enhancements: 可用增幅ID列表(可选)

### 增幅配置 (enhancements)
每个增幅包含：

| 参数          | 说明                                          |
|-------------|---------------------------------------------|
| id          | 增幅ID                                        |
| name        | 增幅名称                                        |
| description | 增幅描述                                        |
| descArg     | 描述参数类型(float/int/int_percent/float_percent) |
| valuePerLvl | 每级增幅值                                       |
| maxLevel    | 最大等级                                        |
| operation   | 增幅类型                                        |
| weight      | 权重配置                                        |

#### 增幅类型(operation)
- none: 无等级增幅
- addition: 增量增幅
- multiply_base: 乘法增幅(基础值)
- multiply_total: 乘法增幅(总值)

#### 权重配置类型
1. 固定权重: 直接设置整数值
2. 随机权重:
   ```json
   {
     "min": "最小值",
     "max": "最大值"
   }
   ```
3. 等级权重:
   ```json
   {
     "multiplier": "倍率系数"
   }
   ```
4. 自定义权重:
   ```json
   {
     "map": {
       "等级1": "权重值1",
       "等级2": "权重值2"
     },
     "default": "默认权重"
   }
   ```
5. 表达式权重:
   ```json
   {
     "expression": "数学表达式(使用{level}作为等级占位符)"
   }
   ```
6. 文件权重:
   ```json
   {
     "filePath": "外部文件路径(使用{level}作为等级占位符)"
   }
   ```
7. 递增权重:
   ```json
   {
     "start": "初始权重",
     "increment": "增量"
   }
   ```

## 重载配置
在游戏内使用 `/reload` 指令重载配置文件