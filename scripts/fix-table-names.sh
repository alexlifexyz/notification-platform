#!/bin/bash

# 批量修改数据库表名，去掉后缀s的脚本

echo "开始批量修改数据库表名..."

# 定义表名映射
declare -A table_mappings=(
    ["notification_channels"]="notification_channel"
    ["notification_templates"]="notification_template"
    ["notifications"]="notification"
    ["recipient_groups"]="recipient_group"
    ["recipient_group_members"]="recipient_group_member"
    ["user_in_app_messages"]="user_in_app_message"
)

# 需要修改的文件列表
files=(
    "scripts/database/notification_service_updated.sql"
    "scripts/database/fix_multi_channel_constraint.sql"
)

# 批量替换函数
replace_in_file() {
    local file="$1"
    echo "处理文件: $file"
    
    if [[ ! -f "$file" ]]; then
        echo "文件不存在: $file"
        return 1
    fi
    
    # 为每个表名映射进行替换
    for old_name in "${!table_mappings[@]}"; do
        new_name="${table_mappings[$old_name]}"
        echo "  替换 $old_name -> $new_name"
        
        # 使用sed进行替换，注意处理各种情况
        sed -i.bak \
            -e "s/\`${old_name}\`/\`${new_name}\`/g" \
            -e "s/DROP TABLE IF EXISTS \`${old_name}\`;/DROP TABLE IF EXISTS \`${new_name}\`;/g" \
            -e "s/CREATE TABLE \`${old_name}\`/CREATE TABLE \`${new_name}\`/g" \
            -e "s/INSERT INTO \`${old_name}\`/INSERT INTO \`${new_name}\`/g" \
            -e "s/FROM ${old_name}/FROM ${new_name}/g" \
            -e "s/FROM \`${old_name}\`/FROM \`${new_name}\`/g" \
            -e "s/ALTER TABLE \`${old_name}\`/ALTER TABLE \`${new_name}\`/g" \
            -e "s/INDEX FROM ${old_name}/INDEX FROM ${new_name}/g" \
            -e "s/INDEX FROM \`${old_name}\`/INDEX FROM \`${new_name}\`/g" \
            -e "s/${old_name}:/${new_name}:/g" \
            "$file"
    done
    
    echo "  完成处理: $file"
}

# 处理所有文件
for file in "${files[@]}"; do
    replace_in_file "$file"
done

echo "批量替换完成！"
echo ""
echo "备份文件已创建（.bak后缀），如需回滚可使用备份文件。"
echo "请检查修改结果是否正确。"
