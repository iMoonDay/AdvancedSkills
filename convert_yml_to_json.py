import json
import yaml


# 递归函数，将嵌套字典扁平化
def flatten_dict(d, parent_key='', sep='.'):
    items = []
    for k, v in d.items():
        new_key = f"{parent_key}{sep}{k}" if parent_key else k
        if isinstance(v, dict):
            items.extend(flatten_dict(v, new_key, sep=sep).items())
        else:
            items.append((new_key, v))
    return dict(items)


# 加载 YAML 文件
with open("zh_cn.yml", "r", encoding="utf-8") as file:
    yaml_content = yaml.safe_load(file)

# 扁平化字典
flattened_content = flatten_dict(yaml_content)

# 转换为 JSON 格式
json_content = json.dumps(flattened_content, indent=2, ensure_ascii=False)

# 保存到文件或打印
with open("zh_cn.json", "w", encoding="utf-8") as file:
    file.write(json_content)

print(json_content)
