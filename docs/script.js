// 参数类型配置
const paramTemplates = {
    int: {
        name: "整数",
        template: `<input type="number" class="base-value" step="1" placeholder="整数值">`,
        parser: el => {
            const value = parseInt(el.querySelector('.base-value').value);
            return isNaN(value) ? 0 : value;
        }
    },
    float: {
        name: "浮点数",
        template: `<input type="number" class="base-value" step="0.1" placeholder="浮点值">`,
        parser: el => {
            const value = parseFloat(el.querySelector('.base-value').value);
            return isNaN(value) ? 0.0 : value;
        }
    },
    double: {
        name: "双精度",
        template: `<input type="number" class="base-value" step="0.1" placeholder="双精度值">`,
        parser: el => {
            const value = parseFloat(el.querySelector('.base-value').value);
            return isNaN(value) ? 0.0 : value;
        }
    },
    string: {
        name: "字符串",
        template: `<input type="text" class="base-value" placeholder="字符串值">`,
        parser: el => el.querySelector('.base-value').value
    },
    boolean: {
        name: "布尔值",
        template: `
                    <div class="toggle-container">
                        <label class="toggle-switch">
                            <input type="checkbox" class="base-value">
                            <span class="toggle-slider"></span>
                        </label>
                        <span class="toggle-label">切换开关状态</span>
                    </div>`,
        parser: el => el.querySelector('.base-value').checked
    },
    sound: {
        name: "音效",
        template: `
                    <div class="config-group">
                        <div class="form-row">
                            <label>音效ID</label>
                            <input type="text" class="sound_id">
                        </div>
                        <div class="form-row">
                            <label>音效范围 <span style="color:#7f8c8d;font-size:12px">(可选)</span></label>
                            <input type="text" class="range">
                        </div>
                    </div>`,
        parser: el => {
            const soundId = el.querySelector('.sound_id').value.trim();
            if (!soundId) return undefined;

            const range = el.querySelector('.range').value.trim();
            return {
                sound_id: soundId,
                ...(range && { range })
            };
        }
    },
    list: {
        name: "列表",
        template: `
                    <div class="config-group" style="display:flex;flex-direction:column;gap:10px">
                        <div style="display:flex;gap:10px;align-items:center">
                            <label style="flex:0 0 auto">列表类型</label>
                            <div class="option-selector list-type-selector" style="flex:1">
                                <div class="option-item" data-value="int">
                                    <span>整数</span>
                                </div>
                                <div class="option-item" data-value="float">
                                    <span>浮点数</span>
                                </div>
                                <div class="option-item" data-value="double">
                                    <span>双精度</span>
                                </div>
                                <div class="option-item" data-value="boolean">
                                    <span>布尔值</span>
                                </div>
                                <div class="option-item" data-value="string">
                                    <span>字符串</span>
                                </div>
                            </div>
                            <input type="hidden" class="list-type" value="int">
                        </div>
                        <div class="list-items-container"></div>
                        <button type="button" onclick="addListItem(this)" style="align-self:stretch">+ 添加项目</button>
                    </div>`,
        parser: el => {
            const type = el.querySelector('.list-type').value;
            const items = el.querySelectorAll('.list-item input');
            const values = Array.from(items)
                .map(input => input.value.trim())
                .filter(Boolean);

            // 即使列表为空也返回空数组
            if (values.length === 0) return [];

            return values.map(v => {
                if (type === 'int') {
                    const num = parseInt(v);
                    return isNaN(num) ? 0 : num;
                }
                if (type === 'float' || type === 'double') {
                    const num = parseFloat(v);
                    return isNaN(num) ? 0.0 : num;
                }
                if (type === 'boolean') {
                    return v.toLowerCase() === 'true';
                }
                return v;
            });
        }
    }
}

// 完整权重配置
const weightConfigs = {
    fixed: {
        name: "固定权重",
        template: `
                    <div class="form-row">
                        <label>固定值</label>
                        <input type="number" class="fixed-value">
                    </div>`,
        parser: el => parseInt(el.querySelector('.fixed-value').value) || 0
    },
    random: {
        name: "随机权重",
        template: `
                    <div class="config-group">
                        <div class="form-row">
                            <label>最小值</label>
                            <input type="number" class="min">
                        </div>
                        <div class="form-row">
                            <label>最大值</label>
                            <input type="number" class="max">
                        </div>
                    </div>`,
        parser: el => ({
            min: parseInt(el.querySelector('.min').value) || 0,
            max: parseInt(el.querySelector('.max').value) || 0
        })
    },
    level: {
        name: "等级权重",
        template: `
                    <div class="form-row">
                        <label>倍率系数</label>
                        <input type="number" class="multiplier" step="0.01">
                    </div>`,
        parser: el => ({
            multiplier: parseFloat(el.querySelector('.multiplier').value) || 1.0
        })
    },
    custom: {
        name: "自定义权重",
        template: `
                    <div class="config-group">
                        <div class="form-row">
                            <label>默认权重</label>
                            <input type="number" class="default">
                        </div>
                        <button type="button" onclick="addCustomPair(this)" style="width:100%;margin:10px 0">+ 添加等级权重</button>
                        <div class="custom-pairs-container"></div>
                    </div>`,
        parser: el => {
            const pairs = el.querySelectorAll('.custom-pair');
            const map = {};
            pairs.forEach(pair => {
                const key = pair.querySelector('.pair-key').value.trim();
                const value = parseInt(pair.querySelector('.pair-value').value);
                if (key && !isNaN(value)) {
                    map[key] = value;
                }
            });
            return {
                map: map,
                default: parseInt(el.querySelector('.default').value) || 0
            }
        }
    },
    expression: {
        name: "表达式权重",
        template: `
                    <div class="form-row">
                        <label>数学表达式</label>
                        <input type="text" class="expr"
                            placeholder="{level}*2+5">
                    </div>`,
        parser: el => ({
            expression: el.querySelector('.expr').value
        })
    },
    file: {
        name: "文件权重",
        template: `
                    <div class="form-row">
                        <label>文件路径</label>
                        <input type="text" class="file-path"
                            placeholder="path/to/config_{level}.json">
                    </div>`,
        parser: el => ({
            filePath: el.querySelector('.file-path').value
        })
    },
    increment: {
        name: "递增权重",
        template: `
                    <div class="config-group">
                        <div class="form-row">
                            <label>初始权重</label>
                            <input type="number" class="start">
                        </div>
                        <div class="form-row">
                            <label>增量</label>
                            <input type="number" class="increment">
                        </div>
                    </div>`,
        parser: el => ({
            start: parseInt(el.querySelector('.start').value) || 0,
            increment: parseInt(el.querySelector('.increment').value) || 0
        })
    }
}

// 添加防抖函数
const debounce = (func, delay = 300) => {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delay);
    };
};

// 添加检查函数
function isParamNameDuplicate(name, excludeElement = null) {
    const params = document.querySelectorAll('#parameters-container .param-name');
    let firstOccurrence = null;
    for (const input of params) {
        if (input.value.trim() === name) {
            if (!firstOccurrence) {
                firstOccurrence = input;
            } else if (input !== excludeElement) {
                // 如果不是第一次出现且不是当前元素，标记为错误
                showInputError(input, '参数名称已存在！该参数将被忽略');
                return true;
            }
        }
    }
    return false;
}

function isEnhancementIdDuplicate(id, excludeElement = null) {
    const enhancements = document.querySelectorAll('#enhancements-container .enh-id');
    let firstOccurrence = null;
    for (const input of enhancements) {
        if (input.value.trim() === id) {
            if (!firstOccurrence) {
                firstOccurrence = input;
            } else if (input !== excludeElement) {
                // 如果不是第一次出现且不是当前元素，标记为错误
                showInputError(input, '增幅ID已存在！该增幅将被忽略');
                return true;
            }
        }
    }
    return false;
}

// 显示错误提示
function showInputError(input, message) {
    // 移除已有的错误提示
    const existingError = input.parentElement.querySelector('.input-error');
    if (existingError) {
        existingError.remove();
    }

    // 添加新的错误提示
    const errorDiv = document.createElement('div');
    errorDiv.className = 'input-error';
    errorDiv.textContent = message;
    input.parentElement.appendChild(errorDiv);
    input.classList.add('error');
}

// 清除错误提示
function clearInputError(input) {
    const errorDiv = input.parentElement.querySelector('.input-error');
    if (errorDiv) {
        errorDiv.remove();
    }
    input.classList.remove('error');
}

// 添加检查所有重复的函数
function checkAllDuplicateParams() {
    const params = document.querySelectorAll('#parameters-container .param-name');
    const paramValues = new Map(); // 用于存储每个值的出现次数和元素

    // 首先清除所有错误
    params.forEach(input => clearInputError(input));

    // 统计每个值的出现次数和元素
    params.forEach(input => {
        const value = input.value.trim();
        if (!value) return;

        if (!paramValues.has(value)) {
            paramValues.set(value, { count: 1, elements: [input] });
        } else {
            const data = paramValues.get(value);
            data.count++;
            data.elements.push(input);
        }
    });

    // 标记重复的元素
    paramValues.forEach(({ count, elements }) => {
        if (count > 1) {
            // 跳过第一个元素，标记其余的为重复
            elements.slice(1).forEach(input => {
                showInputError(input, '参数名称已存在！该参数将被忽略');
            });
        }
    });
}

function checkAllDuplicateEnhancements() {
    const enhancements = document.querySelectorAll('#enhancements-container .enh-id');
    const enhValues = new Map(); // 用于存储每个值的出现次数和元素

    // 首先清除所有错误
    enhancements.forEach(input => clearInputError(input));

    // 统计每个值的出现次数和元素
    enhancements.forEach(input => {
        const value = input.value.trim();
        if (!value) return;

        if (!enhValues.has(value)) {
            enhValues.set(value, { count: 1, elements: [input] });
        } else {
            const data = enhValues.get(value);
            data.count++;
            data.elements.push(input);
        }
    });

    // 标记重复的元素
    enhValues.forEach(({ count, elements }) => {
        if (count > 1) {
            // 跳过第一个元素，标记其余的为重复
            elements.slice(1).forEach(input => {
                showInputError(input, '增幅ID已存在！该增幅将被忽略');
            });
        }
    });
}

// 修改删除函数
function removeEnhancement(button) {
    button.closest('.dynamic-item').remove();
    checkAllDuplicateEnhancements();
    updateEnhancementSelects();
}

// 修改参数删除事件
document.addEventListener('click', e => {
    if (e.target.matches('#parameters-container .dynamic-item button[onclick="removeParameter(this)"]')) {
        e.target.closest('.dynamic-item').remove();
        checkAllDuplicateParams();
    }
});

// 修改参数名称和增幅ID输入监听
document.addEventListener('input', e => {
    if (e.target.matches('.param-name')) {
        const name = e.target.value.trim();
        if (name) {
            checkAllDuplicateParams();
            updateEnhancementSelects();
        } else {
            clearInputError(e.target);
        }
    }

    if (e.target.matches('.enh-id')) {
        const id = e.target.value.trim();
        if (id) {
            checkAllDuplicateEnhancements();

            const oldId = e.target.dataset.lastValue || '';
            e.target.dataset.lastValue = id;

            // 更新所有使用该增幅的参数
            document.querySelectorAll('.enhancement-ids').forEach(input => {
                const ids = input.value.split(',').filter(Boolean);
                const index = ids.indexOf(oldId);
                if (index !== -1) {
                    ids[index] = id;
                    input.value = ids.join(',');
                }
            });

            updateEnhancementSelects();
        } else {
            clearInputError(e.target);
        }
    }

    if (e.target.matches('.enh-id, .enh-operation')) {
        updateEnhancementSelects();
    }
});

// 添加样式
const style = document.createElement('style');
style.textContent = `
    .input-error {
        color: #e74c3c;
        font-size: 12px;
        margin-top: 5px;
        position: absolute;
        bottom: -20px;
        left: 180px;
    }
    
    input.error {
        border-color: #e74c3c !important;
        background-color: #fef0f0;
    }
    
    .form-row {
        position: relative;
        margin-bottom: 25px;
    }
`;
document.head.appendChild(style);

let selectedTypesOrder = []; // 存储用户选择顺序

// 修改DOMContentLoaded事件监听器
document.addEventListener('DOMContentLoaded', () => {
    // 为整个文档添加拖放事件监听
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        document.body.addEventListener(eventName, preventDefaults, false);
    });

    // 添加拖放效果
    ['dragenter', 'dragover'].forEach(eventName => {
        document.body.addEventListener(eventName, () => {
            document.body.classList.add('drag-over');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        document.body.addEventListener(eventName, () => {
            document.body.classList.remove('drag-over');
        }, false);
    });

    // 处理文件拖放
    document.body.addEventListener('drop', handleDrop, false);

    // 初始化时生成一次
    generateJSON();

    // 监听所有输入变化
    const observer = new MutationObserver(debounce(generateJSON));
    const config = {
        childList: true,
        subtree: true,
        attributes: true,
        characterData: true
    };

    // 观察整个容器
    observer.observe(document.querySelector('.container'), config);

    // 手动监听特殊输入
    document.querySelectorAll('input, select, textarea').forEach(el => {
        el.addEventListener('input', debounce(generateJSON));
    });

    // 技能类型选择器初始化
    const typesSelector = document.querySelector('.types-selector');
    const typesInput = document.getElementById('types');

    typesSelector.addEventListener('click', e => {
        const option = e.target.closest('.type-option');
        if (!option) return;

        const type = option.dataset.value;
        const wasSelected = option.classList.contains('selected');

        // 切换选中状态
        option.classList.toggle('selected');

        if (option.classList.contains('selected')) {
            // 新增选中时添加到数组末尾
            selectedTypesOrder.push(type);
        } else {
            // 取消选中时从数组中移除
            selectedTypesOrder = selectedTypesOrder.filter(t => t !== type);
        }

        // 更新隐藏的input值
        typesInput.value = selectedTypesOrder.join(',');
    });

    // 稀有度选择器初始化
    const raritySelector = document.querySelector('.rarity-selector');
    const rarityInput = document.getElementById('rarity');

    // 设置初始选中状态
    const initialOption = raritySelector.querySelector(`[data-value="${rarityInput.value}"]`);
    if (initialOption) {
        initialOption.classList.add('selected');
    }

    // 处理稀有度点击事件
    raritySelector.addEventListener('click', e => {
        const option = e.target.closest('.rarity-option');
        if (!option) return;

        // 移除其他选项的选中状态
        raritySelector.querySelectorAll('.rarity-option').forEach(opt => {
            opt.classList.remove('selected');
        });

        // 设置当前选项的选中状态
        option.classList.add('selected');

        // 更新隐藏的input值
        rarityInput.value = option.dataset.value;
    });

    // 添加文件输入监听器
    document.getElementById('jsonFileInput').addEventListener('change', function (e) {
        const file = e.target.files[0];
        if (file) {
            loadJSONFile(file);
        }
        this.value = ''; // 清空文件输入，允许重复选择相同文件
    });
});

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;

    if (files.length > 0) {
        const file = files[0];
        if (file.type === 'application/json' || file.name.endsWith('.json')) {
            loadJSONFile(file);
        } else {
            alert('请拖入JSON文件！');
        }
    }
}

// 修改generateJSON函数（移除手动调用）
function generateJSON() {
    const config = {
        version: 3,
        settings: {}
    };

    // 基础属性
    const baseId = document.getElementById('base_id').value.trim();
    if (baseId) config.settings.id = baseId;

    const nameType = document.querySelector('input[name="name_type"]:checked').value;
    const nameValue = document.getElementById('name').value.trim();
    if (nameValue) {
        config.settings.name = nameType === 'translate' ? { translate: nameValue } : nameValue;
    }

    const descType = document.querySelector('input[name="desc_type"]:checked').value;
    const descValue = document.getElementById('description').value.trim();
    if (descValue) {
        config.settings.description = descType === 'translate' ? { translate: descValue } : descValue;
    }

    const icon = document.getElementById('icon').value.trim();
    if (icon) config.settings.icon = icon;

    const types = document.getElementById('types').value.split(',').filter(Boolean);
    if (types.length > 0) config.settings.types = types;

    const cooldown = parseInt(document.getElementById('cooldown').value);
    config.settings.cooldown = !isNaN(cooldown) && cooldown >= 0 ? cooldown : 0;

    const rarity = document.getElementById('rarity').value;
    if (rarity) config.settings.rarity = rarity;

    const weight = parseInt(document.getElementById('weight').value);
    if (!isNaN(weight) && weight > 0) config.settings.weight = weight;

    // 始终包含disabled和drawable字段
    config.settings.disabled = document.getElementById('disabled').checked;
    config.settings.drawable = document.getElementById('drawable').checked;

    // 收集参数配置
    const parameters = {};
    document.querySelectorAll('#parameters-container .dynamic-item').forEach(item => {
        const paramNameInput = item.querySelector('.param-name');
        const paramTypeInput = item.querySelector('.param-type');
        const enhancementsInput = item.querySelector('.enhancement-ids');
        
        if (!paramNameInput || !paramTypeInput) return;
        
        const name = paramNameInput.value.trim();
        const type = paramTypeInput.value;
        
        if (!name || isParamNameDuplicate(name, paramNameInput)) return;
        
        const baseValue = paramTemplates[type]?.parser(item);
        if (baseValue === undefined) return;
        
        const enhancements = enhancementsInput?.value.split(',').filter(Boolean) || [];
        
        parameters[name] = {
            baseValue,
            ...(enhancements.length > 0 && { enhancements })
        };
    });

    // 始终包含parameters字段，即使是空对象
    config.settings.parameters = parameters;

    // 收集增幅配置
    const enhancements = [];
    const processedIds = new Set();
    document.querySelectorAll('#enhancements-container .dynamic-item').forEach(item => {
        const id = item.querySelector('.enh-id').value.trim();
        if (!id || processedIds.has(id) || isEnhancementIdDuplicate(id, item.querySelector('.enh-id'))) return;

        processedIds.add(id);
        const operation = item.querySelector('.enh-operation').value;
        const descArg = item.querySelector('.desc-arg').value;
        const valuePerLvl = parseFloat(item.querySelector('.value-per-lvl').value);
        const maxLevel = parseInt(item.querySelector('.max-level').value);

        // 只有当operation不为none时才检查valuePerLvl和maxLevel
        if (operation !== 'none' && (isNaN(valuePerLvl) || isNaN(maxLevel))) return;

        const weightType = item.querySelector('.weight-type').value;
        const weight = weightConfigs[weightType].parser(item);
        if (!weight && weight !== 0) return;  // 修改条件以允许权重为0

        // 获取name和description
        const nameTypeInput = item.querySelector('input[name^="enh_name_type_"]:checked') || 
                            item.querySelector('input[name^="enh_name_type_"][value="translate"]');
        const nameType = nameTypeInput ? nameTypeInput.value : 'translate';
        const nameInput = item.querySelector('.enh-name');
        const nameValue = nameInput ? nameInput.value.trim() : '';
        const descriptionInput = item.querySelector('.enh-description');
        const description = descriptionInput ? descriptionInput.value.trim() : '';

        if (!nameValue) return; // 如果没有名称，跳过这个增幅

        const enhancementObj = {
            id,
            name: nameType === 'translate' ? { translate: nameValue } : nameValue,
            description,
            operation,
            weight
        };

        // 只有当operation不为none时才添加相关字段
        if (operation !== 'none') {
            enhancementObj.descArg = descArg;
            enhancementObj.valuePerLvl = valuePerLvl;
            enhancementObj.maxLevel = maxLevel;
        }

        enhancements.push(enhancementObj);
    });

    config.settings.enhancements = enhancements;

    // 技能类型
    if (types && Array.isArray(types)) {
        document.getElementById('types').value = types.join(',');
        document.querySelectorAll('.type-option').forEach(opt => {
            opt.classList.toggle('selected', types.includes(opt.dataset.value));
        });
    }

    // 更新增幅选择器
    updateEnhancementSelects();

    // 输出结果
    const output = document.getElementById('output');
    const jsonString = JSON.stringify(config, null, 2);
    output.textContent = jsonString;
    hljs.highlightElement(output);

    return jsonString; // 返回纯文本JSON字符串
}

function showError(element, message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    element.appendChild(errorDiv);
    element.classList.add('error');
    setTimeout(() => {
        errorDiv.remove();
        element.classList.remove('error');
    }, 3000);
}

function exportJSON() {
    const data = document.getElementById('output').textContent;
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'skill_config.json';
    a.click();
}

function resetForm() {
    // 清空所有输入
    document.getElementById('base_id').value = '';
    document.getElementById('name').value = '';
    document.getElementById('description').value = '';
    document.getElementById('icon').value = '';
    document.getElementById('cooldown').value = '';
    document.getElementById('rarity').value = 'common';
    document.getElementById('weight').value = '';
    document.getElementById('disabled').checked = false;
    document.getElementById('drawable').checked = true;
    document.getElementById('types').value = '';
    document.getElementById('parameters-container').innerHTML = '';
    document.getElementById('enhancements-container').innerHTML = '';
    document.getElementById('output').textContent = '';

    // 重置稀有度选择
    document.getElementById('rarity').value = 'common';
    document.querySelectorAll('.rarity-option').forEach(opt => {
        opt.classList.remove('selected');
        if (opt.dataset.value === 'common') {
            opt.classList.add('selected');
        }
    });

    // 重置技能类型选择
    document.querySelectorAll('.type-option').forEach(opt => {
        opt.classList.remove('selected');
    });

    document.querySelector('input[name="name_type"][value="translate"]').checked = true;
    document.querySelector('input[name="desc_type"][value="translate"]').checked = true;
    document.getElementById('name').value = '';
    document.getElementById('description').value = '';

    selectedTypesOrder = [];
}

function loadExample() {
    // 清空现有配置
    resetForm();

    // 基础示例
    document.getElementById('base_id').value = 'advskills_re:absolute_defense';
    document.getElementById('name').value = 'advskills_re.skill.absolute_defense.name';
    document.getElementById('description').value = 'advskills_re.skill.absolute_defense.description';
    document.getElementById('icon').value = 'advskills_re:textures/item/absolute_defense.png';
    document.getElementById('cooldown').value = 600;
    document.getElementById('rarity').value = 'superb';
    document.getElementById('weight').value = 5;
    document.getElementById('disabled').checked = false;
    document.getElementById('drawable').checked = true;

    // 选择多个类型
    const types = ['defense', 'passive', 'enhancement'];
    selectedTypesOrder = [...types]; // 设置示例顺序
    document.getElementById('types').value = types.join(',');
    document.querySelectorAll('.type-option').forEach(opt => {
        opt.classList.remove('selected');
        if (types.includes(opt.dataset.value)) {
            opt.classList.add('selected');
        }
    });

    // 添加增幅配置示例
    addEnhancement();
    const powerEnh = document.querySelector('#enhancements-container .dynamic-item:last-child');
    powerEnh.querySelector('.enh-id').value = 'power';

    // 设置操作类型
    const powerOperation = powerEnh.querySelector('.operation-selector [data-value="multiply_total"]');
    powerOperation.click();

    // 设置描述参数类型
    const powerDescArg = powerEnh.querySelector('.desc-arg-selector [data-value="float_percent"]');
    powerDescArg.click();

    powerEnh.querySelector('.value-per-lvl').value = 0.15;
    powerEnh.querySelector('.max-level').value = 5;

    // 设置权重类型
    const powerWeight = powerEnh.querySelector('.weight-type-selector [data-value="fixed"]');
    powerWeight.click();
    updateWeightConfig(powerEnh.querySelector('.weight-type'));
    powerEnh.querySelector('.fixed-value').value = 10;

    addEnhancement();
    const durationEnh = document.querySelector('#enhancements-container .dynamic-item:last-child');
    durationEnh.querySelector('.enh-id').value = 'duration';

    // 设置操作类型
    const durationOperation = durationEnh.querySelector('.operation-selector [data-value="addition"]');
    durationOperation.click();

    // 设置描述参数类型
    const durationDescArg = durationEnh.querySelector('.desc-arg-selector [data-value="int"]');
    durationDescArg.click();

    durationEnh.querySelector('.value-per-lvl').value = 20;
    durationEnh.querySelector('.max-level').value = 5;

    // 设置权重类型
    const durationWeight = durationEnh.querySelector('.weight-type-selector [data-value="increment"]');
    durationWeight.click();
    updateWeightConfig(durationEnh.querySelector('.weight-type'));
    durationEnh.querySelector('.start').value = 10;
    durationEnh.querySelector('.increment').value = -2;

    // 更新增幅ID选择框
    updateEnhancementSelects();

    // 添加参数示例
    addParameter();
    const intParam = document.querySelector('#parameters-container .dynamic-item:last-child');
    intParam.querySelector('.param-name').value = 'duration';

    // 设置参数类型
    const paramType = intParam.querySelector('.param-type-selector [data-value="int"]');
    paramType.click();
    updateParamConfig(intParam.querySelector('.param-type'));
    intParam.querySelector('.base-value').value = 100;
    intParam.querySelector('.enhancement-ids').value = 'duration,power';
    updateEnhancementSelects();

    // 设置稀有度
    const rarityValue = 'superb';
    document.getElementById('rarity').value = rarityValue;
    document.querySelectorAll('.rarity-option').forEach(opt => {
        opt.classList.remove('selected');
        if (opt.dataset.value === rarityValue) {
            opt.classList.add('selected');
        }
    });
}

// 添加自定义键值对的函数
function addCustomPair(button) {
    const container = button.nextElementSibling;
    const pairDiv = document.createElement('div');
    pairDiv.className = 'form-row custom-pair';
    pairDiv.style.marginBottom = '10px';

    // 获取当前键值对数量，用于自动填充等级
    const currentPairs = container.querySelectorAll('.custom-pair').length;
    const nextLevel = currentPairs + 1;

    pairDiv.innerHTML = `
                <input type="text" class="pair-key" placeholder="等级" value="${nextLevel}" style="flex:1;margin-right:10px">
                <input type="number" class="pair-value" placeholder="权重" style="flex:1;margin-right:10px">
                <button type="button" onclick="this.parentElement.remove()" style="background:#e74c3c;padding:5px 10px">×</button>
            `;
    container.appendChild(pairDiv);
}

// 添加列表项目的函数
function addListItem(button) {
    const container = button.previousElementSibling;
    const itemDiv = document.createElement('div');
    itemDiv.className = 'form-row list-item';
    itemDiv.style.marginBottom = '5px';
    itemDiv.innerHTML = `
                <input type="text" style="flex:1;margin-right:10px">
                <button type="button" onclick="this.parentElement.remove()" style="background:#e74c3c;padding:5px 10px">×</button>
            `;
    container.appendChild(itemDiv);
}

// 修改updateParamConfig函数
function updateParamConfig(select) {
    const type = select.value;
    const container = select.closest('.dynamic-item');
    const area = container.querySelector('.param-config-area');
    area.innerHTML = paramTemplates[type].template;

    // 如果是列表类型，初始化列表类型选择器
    if (type === 'list') {
        initializeOptionSelectors(area);
    }
}

// 修改updateWeightConfig函数
function updateWeightConfig(select) {
    const type = select.value;
    const container = select.closest('.dynamic-item');
    const area = container.querySelector('.weight-config-area');
    area.innerHTML = weightConfigs[type].template;
}

// 动态添加参数
function addParameter() {
    const container = document.createElement('div');
    container.className = 'dynamic-item';
    container.innerHTML = `
                <div class="form-row">
                    <label>参数名称</label>
                    <input type="text" class="param-name" style="flex:1">
                    <button onclick="removeParameter(this)">×</button>
                </div>
                <div class="form-row">
                    <label>参数类型</label>
                    <div class="option-selector param-type-selector" style="flex:1">
                        ${Object.entries(paramTemplates).map(([key, value]) => `
                            <div class="option-item" data-value="${key}">
                                <span>${value.name}</span>
                            </div>
                        `).join('')}
                    </div>
                    <input type="hidden" class="param-type" value="int">
                </div>
                <div class="form-row">
                    <label>参数值</label>
                    <div class="param-config-area" style="flex:1;display:flex"></div>
                </div>
                <div class="form-row">
                    <label>可用增幅</label>
                    <div class="enhancement-selector">
                        <span style="color:#7f8c8d;font-size:12px">暂无可用增幅配置</span>
                    </div>
                    <input type="hidden" class="enhancement-ids">
                </div>
            `;
    document.getElementById('parameters-container').appendChild(container);
    initializeOptionSelectors(container);
    updateParamConfig(container.querySelector('.param-type'));
    updateEnhancementSelects();
}

// 添加参数删除函数
function removeParameter(button) {
    button.closest('.dynamic-item').remove();
    checkAllDuplicateParams();
    updateEnhancementSelects();
}

// 修改更新增幅选择框的函数
function updateEnhancementSelects() {
    // 获取所有增幅配置，但只包含第一次出现的ID
    const processedIds = new Set();
    const enhancements = Array.from(document.querySelectorAll('#enhancements-container .dynamic-item'))
        .map((item, index) => {
            const id = item.querySelector('.enh-id')?.value.trim();
            const operation = item.querySelector('.enh-operation')?.value;
            return { id, operation, position: index, element: item };
        })
        .filter(enh => enh.id)
        .filter(enh => {
            // 检查是否是第一次出现
            if (!processedIds.has(enh.id)) {
                processedIds.add(enh.id);
                return true;
            }
            // 如果不是第一次出现，显示错误
            showInputError(enh.element.querySelector('.enh-id'), '增幅ID已存在！该增幅将被忽略');
            return false;
        });

    // 更新所有参数的增幅选择框
    document.querySelectorAll('.form-row').forEach(row => {
        const selector = row.querySelector('.enhancement-selector');
        if (!selector) return;

        // 如果没有增幅配置，隐藏整个行
        if (enhancements.length === 0) {
            row.style.display = 'none';
            return;
        } else {
            row.style.display = 'flex';
        }

        const hiddenInput = row.querySelector('.enhancement-ids');
        const selectedIds = hiddenInput.value.split(',').filter(Boolean);

        selector.innerHTML = enhancements.map(enh => {
            const isSelected = selectedIds.includes(enh.id);
            return `
                <div class="enhancement-option ${isSelected ? 'selected' : ''}" data-id="${enh.id}">
                    <span class="operation-icon">${getOperationIcon(enh.operation)}</span>
                    <span>${enh.id}</span>
                </div>
            `;
        }).join('');

        // 添加点击事件
        selector.querySelectorAll('.enhancement-option').forEach(option => {
            option.addEventListener('click', () => {
                option.classList.toggle('selected');
                const newSelectedIds = Array.from(selector.querySelectorAll('.enhancement-option.selected'))
                    .map(opt => opt.dataset.id);
                hiddenInput.value = newSelectedIds.join(',');
            });
        });

        // 清理已选择但不再有效的增幅ID
        const validIds = enhancements.map(enh => enh.id);
        const newSelectedIds = selectedIds.filter(id => validIds.includes(id));
        if (newSelectedIds.length !== selectedIds.length) {
            hiddenInput.value = newSelectedIds.join(',');
        }
    });
}

// 添加获取操作类型图标的函数
function getOperationIcon(operation) {
    switch (operation) {
        case 'addition': return '➕';
        case 'multiply_base': return '✖️';
        case 'multiply_total': return '⭐';
        case 'none': return '⚪';
        default: return '❔';
    }
}

// 动态添加增幅
function addEnhancement() {
    const container = document.createElement('div');
    container.className = 'dynamic-item';
    container.innerHTML = `
                <div class="form-row">
                    <label>增幅ID</label>
                    <input type="text" class="enh-id" data-last-value="">
                    <button onclick="removeEnhancement(this)">×</button>
                </div>
                <div class="form-row">
                    <label>名称</label>
                    <div style="flex:1">
                        <div class="input-type-selector" style="margin-bottom:8px;display:flex">
                            <label style="flex:0 0 auto;margin-right:15px">
                                <input type="radio" name="enh_name_type_${Date.now()}" value="translate" checked> 翻译键
                            </label>
                            <label style="flex:0 0 auto">
                                <input type="radio" name="enh_name_type_${Date.now()}" value="text"> 文本
                            </label>
                        </div>
                        <div style="display:flex">
                            <input type="text" class="enh-name" placeholder="advskills_re.skill.example.enhancement.name" style="flex:1"
                                data-translate-ph="advskills_re.skill.example.enhancement.name" data-text-ph="Enhancement Name">
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <label>描述</label>
                    <input type="text" class="enh-description" placeholder="advskills_re.skill.example.enhancement.description">
                </div>
                <div class="form-row">
                    <label>操作类型</label>
                    <div class="option-selector operation-selector">
                        <div class="option-item" data-value="addition">
                            <span class="icon">➕</span>
                            <span>增量增幅</span>
                        </div>
                        <div class="option-item" data-value="multiply_base">
                            <span class="icon">✖️</span>
                            <span>基础乘法</span>
                        </div>
                        <div class="option-item" data-value="multiply_total">
                            <span class="icon">⭐</span>
                            <span>总体乘法</span>
                        </div>
                        <div class="option-item" data-value="none">
                            <span class="icon">⚪</span>
                            <span>无增幅</span>
                        </div>
                    </div>
                    <input type="hidden" class="enh-operation" value="addition">
                </div>
                <div class="form-row">
                    <label>描述参数类型</label>
                    <div class="option-selector desc-arg-selector">
                        <div class="option-item" data-value="int">
                            <span>整数</span>
                        </div>
                        <div class="option-item" data-value="float">
                            <span>浮点</span>
                        </div>
                        <div class="option-item" data-value="int_percent">
                            <span>整数百分比</span>
                        </div>
                        <div class="option-item" data-value="float_percent">
                            <span>浮点百分比</span>
                        </div>
                    </div>
                    <input type="hidden" class="desc-arg" value="int">
                </div>
                <div class="form-row">
                    <label>每级增幅值</label>
                    <input type="number" class="value-per-lvl" step="0.01">
                </div>
                <div class="form-row">
                    <label>最大等级</label>
                    <input type="number" class="max-level" min="1">
                </div>
                <div class="form-row">
                    <label>权重类型</label>
                    <div class="option-selector weight-type-selector">
                        <div class="option-item" data-value="fixed">
                            <span>固定权重</span>
                        </div>
                        <div class="option-item" data-value="random">
                            <span>随机权重</span>
                        </div>
                        <div class="option-item" data-value="level">
                            <span>等级权重</span>
                        </div>
                        <div class="option-item" data-value="custom">
                            <span>自定义权重</span>
                        </div>
                        <div class="option-item" data-value="expression">
                            <span>表达式权重</span>
                        </div>
                        <div class="option-item" data-value="file">
                            <span>文件权重</span>
                        </div>
                        <div class="option-item" data-value="increment">
                            <span>递增权重</span>
                        </div>
                    </div>
                    <input type="hidden" class="weight-type" value="fixed">
                </div>
                <div class="weight-config-area"></div>
            `;

    // 初始化选择器
    initializeOptionSelectors(container);
    document.getElementById('enhancements-container').appendChild(container);
    updateWeightConfig(container.querySelector('.weight-type'));
    updateEnhancementSelects();

    // 监听增幅名称类型切换
    container.querySelectorAll('input[name^="enh_name_type_"]').forEach(radio => {
        radio.addEventListener('change', () => {
            const nameInput = container.querySelector('.enh-name');
            nameInput.placeholder = radio.value === 'text'
                ? nameInput.dataset.textPh
                : nameInput.dataset.translatePh;
        });
    });
}

// 修改初始化选择器函数
function initializeOptionSelectors(container) {
    container.querySelectorAll('.option-selector').forEach(selector => {
        const hiddenInput = selector.nextElementSibling;

        // 设置初始选中状态
        const initialOption = selector.querySelector(`[data-value="${hiddenInput.value}"]`);
        if (initialOption) {
            initialOption.classList.add('selected');
        }

        // 添加点击事件
        selector.addEventListener('click', e => {
            const option = e.target.closest('.option-item');
            if (!option) return;

            // 移除其他选项的选中状态
            selector.querySelectorAll('.option-item').forEach(opt => {
                opt.classList.remove('selected');
            });

            // 设置当前选项的选中状态
            option.classList.add('selected');

            // 更新隐藏的input值
            hiddenInput.value = option.dataset.value;

            // 如果是操作类型选择器，立即更新显示状态
            if (selector.classList.contains('operation-selector')) {
                toggleEnhancementFields(hiddenInput);
                updateEnhancementSelects();
            }

            // 根据选择器类型执行不同的更新操作
            if (selector.classList.contains('operation-selector')) {
                toggleEnhancementFields(hiddenInput);
            } else if (selector.classList.contains('param-type-selector')) {
                updateParamConfig(hiddenInput);
            } else if (selector.classList.contains('weight-type-selector')) {
                updateWeightConfig(hiddenInput);
            }
        });
    });
}

// 修改切换字段显示函数
function toggleEnhancementFields(select) {
    const container = select.closest('.dynamic-item');
    const operation = select.value;

    // 获取相关字段容器
    const descArgRow = container.querySelector('.form-row:nth-child(5)');
    const valuePerLvlRow = container.querySelector('.form-row:nth-child(6)');
    const maxLevelRow = container.querySelector('.form-row:nth-child(7)');

    // 根据操作类型切换显示
    if (operation === 'none') {
        descArgRow.style.display = 'none';
        valuePerLvlRow.style.display = 'none';
        maxLevelRow.style.display = 'none';
    } else {
        descArgRow.style.display = 'flex';
        valuePerLvlRow.style.display = 'flex';
        maxLevelRow.style.display = 'flex';
    }
}

// 从JSON配置填充表单
function fillFormFromJSON(config) {
    // 重置表单
    resetForm();

    const settings = config.settings;
    if (!settings) return;

    // 基础属性
    if (settings.id) document.getElementById('base_id').value = settings.id;

    // 名称
    if (settings.name) {
        if (typeof settings.name === 'object' && settings.name.translate) {
            document.querySelector('input[name="name_type"][value="translate"]').checked = true;
            document.getElementById('name').value = settings.name.translate;
        } else {
            document.querySelector('input[name="name_type"][value="text"]').checked = true;
            document.getElementById('name').value = settings.name;
        }
    }

    // 描述
    if (settings.description) {
        if (typeof settings.description === 'object' && settings.description.translate) {
            document.querySelector('input[name="desc_type"][value="translate"]').checked = true;
            document.getElementById('description').value = settings.description.translate;
        } else {
            document.querySelector('input[name="desc_type"][value="text"]').checked = true;
            document.getElementById('description').value = settings.description;
        }
    }

    if (settings.icon) document.getElementById('icon').value = settings.icon;
    
    var cooldown = parseInt(settings.cooldown);
    if (!isNaN(cooldown) && cooldown >= 0) document.getElementById('cooldown').value = cooldown;
    
    if (settings.weight) document.getElementById('weight').value = settings.weight;

    // 稀有度
    if (settings.rarity) {
        document.getElementById('rarity').value = settings.rarity;
        document.querySelectorAll('.rarity-option').forEach(opt => {
            opt.classList.toggle('selected', opt.dataset.value === settings.rarity);
        });
    }

    // 禁用状态
    document.getElementById('disabled').checked = settings.disabled === true;
    document.getElementById('drawable').checked = settings.drawable !== false;

    // 技能类型
    if (settings.types && Array.isArray(settings.types)) {
        selectedTypesOrder = [...settings.types]; // 保持导入顺序
        document.getElementById('types').value = settings.types.join(',');
        document.querySelectorAll('.type-option').forEach(opt => {
            const shouldSelect = settings.types.includes(opt.dataset.value);
            opt.classList.toggle('selected', shouldSelect);
        });
    }

    // 先添加增幅配置
    if (settings.enhancements) {
        settings.enhancements.forEach(enh => {
            addEnhancement();
            const container = document.querySelector('#enhancements-container .dynamic-item:last-child');

            container.querySelector('.enh-id').value = enh.id;

            // 设置name
            if (enh.name) {
                const nameInput = container.querySelector('.enh-name');
                if (nameInput) {
                    if (typeof enh.name === 'object' && enh.name.translate) {
                        const translateRadio = container.querySelector('input[name^="enh_name_type_"][value="translate"]');
                        if (translateRadio) {
                            translateRadio.checked = true;
                            nameInput.value = enh.name.translate;
                            nameInput.placeholder = nameInput.dataset.translatePh;
                        }
                    } else {
                        const textRadio = container.querySelector('input[name^="enh_name_type_"][value="text"]');
                        if (textRadio) {
                            textRadio.checked = true;
                            nameInput.value = enh.name;
                            nameInput.placeholder = nameInput.dataset.textPh;
                        }
                    }
                }
            }

            // 设置description
            const descInput = container.querySelector('.enh-description');
            if (descInput && enh.description) {
                descInput.value = enh.description;
            }

            // 操作类型
            const operationOption = container.querySelector(`.operation-selector [data-value="${enh.operation}"]`);
            if (operationOption) operationOption.click();

            // 只有当operation不为none时才设置相关字段
            if (enh.operation !== 'none') {
                // 描述参数
                const descArgOption = container.querySelector(`.desc-arg-selector [data-value="${enh.descArg}"]`);
                if (descArgOption) descArgOption.click();

                if (enh.valuePerLvl) container.querySelector('.value-per-lvl').value = enh.valuePerLvl;
                if (enh.maxLevel) container.querySelector('.max-level').value = enh.maxLevel;
            }

            // 权重配置
            if (enh.weight) {
                let weightType = 'fixed';
                if (typeof enh.weight === 'object') {
                    if (enh.weight.min !== undefined) weightType = 'random';
                    else if (enh.weight.multiplier !== undefined) weightType = 'level';
                    else if (enh.weight.map !== undefined) weightType = 'custom';
                    else if (enh.weight.expression !== undefined) weightType = 'expression';
                    else if (enh.weight.filePath !== undefined) weightType = 'file';
                    else if (enh.weight.start !== undefined) weightType = 'increment';
                }

                const weightOption = container.querySelector(`.weight-type-selector [data-value="${weightType}"]`);
                if (weightOption) {
                    weightOption.click();

                    // 填充权重值
                    if (weightType === 'fixed') {
                        container.querySelector('.fixed-value').value = enh.weight;
                    } else if (weightType === 'random') {
                        container.querySelector('.min').value = enh.weight.min;
                        container.querySelector('.max').value = enh.weight.max;
                    } else if (weightType === 'level') {
                        container.querySelector('.multiplier').value = enh.weight.multiplier;
                    } else if (weightType === 'custom') {
                        container.querySelector('.default').value = enh.weight.default;
                        Object.entries(enh.weight.map).forEach(([key, value]) => {
                            const button = container.querySelector('button[onclick="addCustomPair(this)"]');
                            button.click();
                            const lastPair = container.querySelector('.custom-pair:last-child');
                            lastPair.querySelector('.pair-key').value = key;
                            lastPair.querySelector('.pair-value').value = value;
                        });
                    } else if (weightType === 'expression') {
                        container.querySelector('.expr').value = enh.weight.expression;
                    } else if (weightType === 'file') {
                        container.querySelector('.file-path').value = enh.weight.filePath;
                    } else if (weightType === 'increment') {
                        container.querySelector('.start').value = enh.weight.start;
                        container.querySelector('.increment').value = enh.weight.increment;
                    }
                }
            }
        });
    }

    // 更新增幅选择器
    updateEnhancementSelects();

    // 再添加参数配置
    if (settings.parameters) {
        Object.entries(settings.parameters).forEach(([name, param]) => {
            addParameter();
            const container = document.querySelector('#parameters-container .dynamic-item:last-child');

            container.querySelector('.param-name').value = name;

            // 确定参数类型
            let paramType = 'int';
            if (Array.isArray(param.baseValue)) paramType = 'list';
            else if (typeof param.baseValue === 'string') paramType = 'string';
            else if (typeof param.baseValue === 'boolean') paramType = 'boolean';
            else if (typeof param.baseValue === 'object' && param.baseValue.sound_id) paramType = 'sound';
            else if (Number.isInteger(param.baseValue)) paramType = 'int';
            else if (typeof param.baseValue === 'number') paramType = 'float';

            // 设置参数类型
            const typeOption = container.querySelector(`.param-type-selector [data-value="${paramType}"]`);
            if (typeOption) {
                typeOption.click();
                updateParamConfig(container.querySelector('.param-type'));

                // 设置基础值
                if (paramType === 'sound') {
                    container.querySelector('.sound_id').value = param.baseValue.sound_id;
                    if (param.baseValue.range) container.querySelector('.range').value = param.baseValue.range;
                } else if (paramType === 'list') {
                    // 设置列表类型
                    let listType = 'string';
                    if (param.baseValue.length > 0) {
                        const firstItem = param.baseValue[0];
                        if (Number.isInteger(firstItem)) listType = 'int';
                        else if (typeof firstItem === 'number') listType = 'float';
                        else if (typeof firstItem === 'boolean') listType = 'boolean';
                    }
                    container.querySelector(`.list-type-selector [data-value="${listType}"]`).click();

                    // 添加列表项
                    param.baseValue.forEach(value => {
                        const button = container.querySelector('button[onclick="addListItem(this)"]');
                        button.click();
                        const lastItem = container.querySelector('.list-item:last-child input');
                        lastItem.value = value;
                    });
                } else {
                    const baseValueInput = container.querySelector('.base-value');
                    if (baseValueInput) {
                        if (typeof param.baseValue === 'boolean') {
                            baseValueInput.checked = param.baseValue;
                        } else {
                            baseValueInput.value = param.baseValue;
                        }
                    }
                }
            }

            // 设置增幅
            if (param.enhancements) {
                container.querySelector('.enhancement-ids').value = param.enhancements.join(',');
                // 更新增幅选择器的选中状态
                const selector = container.querySelector('.enhancement-selector');
                if (selector) {
                    selector.querySelectorAll('.enhancement-option').forEach(option => {
                        option.classList.toggle('selected', param.enhancements.includes(option.dataset.id));
                    });
                }
            }
        });
    }
}

// 读取JSON文件并填充表单
function loadJSONFile(file) {
    const reader = new FileReader();
    reader.onload = function (e) {
        try {
            const originalConfig = JSON.parse(e.target.result);
            if (originalConfig.version !== 3) {
                alert('不支持的配置文件版本！');
                return;
            }

            // 先填充表单
            fillFormFromJSON(originalConfig);

            // 生成新的JSON并获取纯文本内容
            generateJSON();
            const output = document.getElementById('output');
            const regeneratedConfig = JSON.parse(output.textContent.replace(/\u200B/g, ''));

            // 比较两个JSON
            const differences = compareJSON(originalConfig, regeneratedConfig);

            if (differences.length > 0) {
                // 创建差异显示对话框
                const dialogHtml = `
                    <div id="diff-dialog" style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);
                        background:white;padding:20px;border-radius:10px;box-shadow:0 0 20px rgba(0,0,0,0.3);
                        max-width:90%;max-height:90vh;overflow:auto;z-index:1000">
                        <h3 style="margin-top:0">JSON对比</h3>
                        <div style="margin:10px 0;color:#e74c3c">
                            原始JSON与重新生成的JSON对比：
                        </div>
                        <div style="display:flex;gap:20px;margin-top:15px">
                            <div style="flex:1">
                                <div style="font-weight:bold;margin-bottom:5px">原始JSON：</div>
                                <pre style="background:#f8f9fa;padding:10px;border-radius:5px;max-height:60vh;overflow:auto;margin:0;font-size:12px">
${JSON.stringify(originalConfig, null, 2)}</pre>
                            </div>
                            <div style="flex:1">
                                <div style="font-weight:bold;margin-bottom:5px">重新生成的JSON：</div>
                                <pre style="background:#f8f9fa;padding:10px;border-radius:5px;max-height:60vh;overflow:auto;margin:0;font-size:12px">
${JSON.stringify(regeneratedConfig, null, 2)}</pre>
                            </div>
                        </div>
                        <div style="margin-top:15px;text-align:right">
                            <button onclick="document.getElementById('diff-dialog').remove();document.getElementById('diff-overlay').remove();resetForm()" 
                                style="background:#e74c3c;margin-right:10px">取消导入</button>
                            <button onclick="document.getElementById('diff-dialog').remove();document.getElementById('diff-overlay').remove()" 
                                style="background:#27ae60">继续导入</button>
                        </div>
                    </div>
                    <div id="diff-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;
                        background:rgba(0,0,0,0.5);z-index:999"></div>
                `;

                // 添加对话框到页面
                document.body.insertAdjacentHTML('beforeend', dialogHtml);

                // 点击遮罩层关闭对话框
                document.getElementById('diff-overlay').onclick = () => {
                    document.getElementById('diff-dialog').remove();
                    document.getElementById('diff-overlay').remove();
                    resetForm();
                };
            }
        } catch (error) {
            console.error('JSON解析错误:', error);
            const errorDetails = `
错误类型：${error.name}
错误信息：${error.message}
堆栈跟踪：
${error.stack}

JSON内容：
${e.target.result}`;

            const dialogHtml = `
                <div id="error-dialog" style="position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);
                    background:white;padding:20px;border-radius:10px;box-shadow:0 0 20px rgba(0,0,0,0.3);
                    max-width:80%;max-height:80vh;overflow:auto;z-index:1000">
                    <h3 style="margin-top:0;color:#e74c3c">JSON解析错误</h3>
                    <div style="margin:10px 0">
                        <pre style="background:#f8f9fa;padding:10px;border-radius:5px;max-height:300px;overflow:auto;white-space:pre-wrap;word-break:break-all">${errorDetails}</pre>
                    </div>
                    <div style="margin-top:15px;text-align:right">
                        <button onclick="navigator.clipboard.writeText(document.querySelector('#error-dialog pre').textContent).then(() => alert('错误信息已复制到剪贴板'))" 
                            style="background:#3498db;margin-right:10px">复制错误信息</button>
                        <button onclick="document.getElementById('error-dialog').remove();document.getElementById('error-overlay').remove()" 
                            style="background:#e74c3c">关闭</button>
                    </div>
                </div>
                <div id="error-overlay" style="position:fixed;top:0;left:0;right:0;bottom:0;
                    background:rgba(0,0,0,0.5);z-index:999"></div>
            `;

            // 添加对话框到页面
            document.body.insertAdjacentHTML('beforeend', dialogHtml);

            // 点击遮罩层关闭对话框
            document.getElementById('error-overlay').onclick = () => {
                document.getElementById('error-dialog').remove();
                document.getElementById('error-overlay').remove();
            };
        }
    };
    reader.readAsText(file);
}

// 比较两个JSON对象并返回差异
function compareJSON(original, regenerated) {
    const differences = [];

    function compare(path, obj1, obj2) {
        if (obj1 === obj2) return;

        if (typeof obj1 !== typeof obj2) {
            differences.push(`${path}: 类型不匹配 (原始: ${typeof obj1}, 重新生成: ${typeof obj2})`);
            return;
        }

        if (typeof obj1 === 'object' && obj1 !== null) {
            if (Array.isArray(obj1) !== Array.isArray(obj2)) {
                differences.push(`${path}: 数据结构不匹配 (原始: ${Array.isArray(obj1) ? '数组' : '对象'}, 重新生成: ${Array.isArray(obj2) ? '数组' : '对象'})`);
                return;
            }

            if (Array.isArray(obj1)) {
                if (obj1.length !== obj2.length) {
                    differences.push(`${path}: 数组长度不匹配 (原始: ${obj1.length}, 重新生成: ${obj2.length})`);
                }
                obj1.forEach((item, index) => {
                    compare(`${path}[${index}]`, item, obj2[index]);
                });
            } else {
                const keys1 = Object.keys(obj1);
                const keys2 = Object.keys(obj2);

                keys1.forEach(key => {
                    if (!obj2.hasOwnProperty(key)) {
                        differences.push(`${path}.${key}: 字段在重新生成的JSON中缺失`);
                    } else {
                        compare(`${path}.${key}`, obj1[key], obj2[key]);
                    }
                });

                keys2.forEach(key => {
                    if (!obj1.hasOwnProperty(key)) {
                        differences.push(`${path}.${key}: 字段在原始JSON中缺失`);
                    }
                });
            }
        } else if (obj1 !== obj2) {
            differences.push(`${path}: 值不匹配 (原始: ${obj1}, 重新生成: ${obj2})`);
        }
    }

    compare('root', original, regenerated);
    return differences;
}