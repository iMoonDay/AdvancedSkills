import os
import re
import shutil
from pathlib import Path

# 定义路径和不同的文件夹名称
base_path = Path.cwd()
folder_names = ['fabric', 'forge', 'neoforge']
desktop_path = Path.home() / 'Desktop'


# 定义正则表达式
def get_pattern(path_name):
    return re.compile(rf'.*-{path_name}-(\d+)(\.\d+)(\.\d+)?(-\w+\.\d+)?\+(\d+)(\.\d+)(\.\d+)?\.jar')


def find_latest_jar(source_path, path_name):
    if source_path.exists():
        pattern = get_pattern(path_name)
        # 获取符合条件的文件
        jar_files = [f for f in source_path.glob('*') if pattern.match(f.name)]

        if jar_files:
            # 找到最新修改的文件
            latest_file = max(jar_files, key=os.path.getmtime)
            return latest_file
        else:
            print(f"在 {source_path} 中没有找到符合条件的文件.")
    else:
        print(f"路径不存在: {source_path}")

    return None


# 找到所有最新的jar文件
latest_files = []
all_files = {}

for folder in folder_names:
    path = base_path / folder / 'build' / 'libs'
    latest_file = find_latest_jar(path, folder)
    if latest_file:
        latest_files.append(latest_file)

    # 收集当前文件夹内的所有符合条件的文件
    all_files[folder] = [f for f in path.glob('*') if get_pattern(folder).match(f.name)]

# 打印找到的文件并确认复制
if latest_files:
    print("找到以下最新文件:")
    for jar_file in latest_files:
        print(f" - {jar_file.name}")

    confirm = input("确认复制这些文件到桌面? (y/n): ")
    if confirm.lower() == 'y':
        for jar_file in latest_files:
            shutil.copy(jar_file, desktop_path)
        print("文件已复制到桌面.")
    elif confirm.lower() == 'n':
        # 每个文件夹轮流询问
        for folder in folder_names:
            if folder in all_files and all_files[folder]:
                print(f"\n选择 {folder} 中的文件:")
                for idx, jar_file in enumerate(all_files[folder]):
                    print(f"  {idx + 1}: {jar_file.name}")

                selected_index = input("请选择要复制的文件编号 (输入 n 跳过): ")

                if selected_index.lower() == 'n':
                    continue

                try:
                    index = int(selected_index)
                    if 1 <= index <= len(all_files[folder]):
                        shutil.copy(all_files[folder][index - 1], desktop_path)
                        print(f"文件已复制: {all_files[folder][index - 1].name}")
                    else:
                        print("无效的选择，跳过.")
                except ValueError:
                    print("无效的输入，跳过.")

    else:
        print("复制操作已取消.")
else:
    print("没有找到符合条件的文件.")
