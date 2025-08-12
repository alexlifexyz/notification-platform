#!/bin/bash

# 通知平台数据库管理脚本
# 使用方法: ./scripts/database-manager.sh [action]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_SCRIPTS_DIR="${SCRIPT_DIR}/database"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 数据库配置
DB_NAME="notification_service"
DB_USER="${DB_USER:-root}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

show_help() {
    echo "通知平台数据库管理脚本"
    echo ""
    echo "使用方法:"
    echo "  ./scripts/database-manager.sh [选项] [操作]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -u, --user     数据库用户名 (默认: root)"
    echo "  -H, --host     数据库主机 (默认: localhost)"
    echo "  -P, --port     数据库端口 (默认: 3306)"
    echo ""
    echo "操作:"
    echo "  init           初始化数据库（全新安装）"
    echo "  update         更新数据库结构"
    echo "  fix-multi      修复多渠道约束问题"
    echo "  backup         备份数据库"
    echo "  status         检查数据库状态"
    echo ""
    echo "环境变量:"
    echo "  DB_USER        数据库用户名"
    echo "  DB_HOST        数据库主机"
    echo "  DB_PORT        数据库端口"
    echo "  MYSQL_PWD      数据库密码（推荐使用此方式传递密码）"
    echo ""
    echo "示例:"
    echo "  MYSQL_PWD=password ./scripts/database-manager.sh init"
    echo "  ./scripts/database-manager.sh -u admin -H 192.168.1.100 status"
}

# 检查MySQL连接
check_mysql_connection() {
    print_info "检查MySQL连接..."
    
    if ! command -v mysql &> /dev/null; then
        print_error "MySQL客户端未安装"
        return 1
    fi
    
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "SELECT 1;" &> /dev/null; then
        print_error "无法连接到MySQL服务器"
        print_info "请检查连接参数: $DB_USER@$DB_HOST:$DB_PORT"
        return 1
    fi
    
    print_success "MySQL连接正常"
}

# 初始化数据库
init_database() {
    print_info "初始化数据库..."
    
    # 创建数据库
    print_info "创建数据库: $DB_NAME"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    
    # 执行初始化脚本
    local init_script="${DB_SCRIPTS_DIR}/notification_service.sql"
    if [[ -f "$init_script" ]]; then
        print_info "执行初始化脚本..."
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" < "$init_script"
        print_success "数据库初始化完成"
    else
        print_error "初始化脚本不存在: $init_script"
        return 1
    fi
}

# 修复多渠道约束
fix_multi_channel() {
    print_info "修复多渠道约束..."
    
    local fix_script="${DB_SCRIPTS_DIR}/fix_multi_channel_constraint.sql"
    if [[ -f "$fix_script" ]]; then
        print_info "执行修复脚本..."
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" < "$fix_script"
        print_success "多渠道约束修复完成"
    else
        print_error "修复脚本不存在: $fix_script"
        return 1
    fi
}

# 检查数据库状态
check_status() {
    print_info "检查数据库状态..."
    
    # 检查数据库是否存在
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "USE $DB_NAME;" &> /dev/null; then
        print_success "数据库存在: $DB_NAME"
        
        # 检查表数量
        local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW TABLES;" | wc -l)
        print_info "表数量: $((table_count - 1))"
        
        # 检查关键表
        local tables=("notification_channels" "notification_templates" "notifications" "recipient_groups" "user_in_app_messages")
        for table in "${tables[@]}"; do
            if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "DESCRIBE $table;" &> /dev/null; then
                local count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SELECT COUNT(*) FROM $table;" | tail -n 1)
                print_info "表 $table: $count 条记录"
            else
                print_warning "表不存在: $table"
            fi
        done
        
        # 检查约束
        print_info "检查约束..."
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" -e "SHOW INDEX FROM notifications WHERE Key_name LIKE 'uk_%';"
        
    else
        print_warning "数据库不存在: $DB_NAME"
    fi
}

# 备份数据库
backup_database() {
    print_info "备份数据库..."
    
    local backup_dir="backups"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_file="${backup_dir}/${DB_NAME}_${timestamp}.sql"
    
    mkdir -p "$backup_dir"
    
    if mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" > "$backup_file"; then
        print_success "数据库备份完成: $backup_file"
    else
        print_error "数据库备份失败"
        return 1
    fi
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -H|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -P|--port)
            DB_PORT="$2"
            shift 2
            ;;
        *)
            ACTION="$1"
            shift
            ;;
    esac
done

# 检查数据库脚本目录
if [[ ! -d "$DB_SCRIPTS_DIR" ]]; then
    print_error "数据库脚本目录不存在: $DB_SCRIPTS_DIR"
    exit 1
fi

# 检查MySQL连接
if ! check_mysql_connection; then
    exit 1
fi

# 执行操作
case "${ACTION:-}" in
    init)
        init_database
        ;;
    fix-multi)
        fix_multi_channel
        ;;
    status)
        check_status
        ;;
    backup)
        backup_database
        ;;
    "")
        print_warning "请指定操作，使用 --help 查看帮助"
        show_help
        ;;
    *)
        print_error "未知操作: $ACTION"
        show_help
        exit 1
        ;;
esac
