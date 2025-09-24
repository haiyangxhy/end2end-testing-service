#!/bin/bash

# 端到端测试平台数据库部署脚本
# 用于快速部署和初始化数据库

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查PostgreSQL是否安装
check_postgresql() {
    log_info "检查PostgreSQL安装状态..."
    if ! command -v psql &> /dev/null; then
        log_error "PostgreSQL未安装，请先安装PostgreSQL"
        exit 1
    fi
    log_info "PostgreSQL已安装"
}

# 检查数据库是否存在
check_database() {
    local db_name=$1
    log_info "检查数据库是否存在: $db_name"
    
    if psql -h localhost -U postgres -lqt | cut -d \| -f 1 | grep -qw $db_name; then
        log_warn "数据库 $db_name 已存在"
        read -p "是否要重新创建数据库？这将删除所有现有数据！(y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log_info "删除现有数据库..."
            psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS $db_name;"
            return 1
        else
            return 0
        fi
    else
        return 1
    fi
}

# 创建数据库
create_database() {
    local db_name=$1
    local db_user=$2
    local db_password=$3
    
    log_info "创建数据库: $db_name"
    psql -h localhost -U postgres -c "CREATE DATABASE $db_name;"
    
    log_info "创建用户: $db_user"
    psql -h localhost -U postgres -c "CREATE USER $db_user WITH PASSWORD '$db_password';"
    psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $db_name TO $db_user;"
    psql -h localhost -U postgres -c "ALTER USER $db_user CREATEDB;"
    
    log_info "数据库创建完成"
}

# 执行SQL脚本
execute_sql_script() {
    local script_file=$1
    local db_name=$2
    local db_user=$3
    
    if [ ! -f "$script_file" ]; then
        log_error "SQL脚本文件不存在: $script_file"
        exit 1
    fi
    
    log_info "执行SQL脚本: $script_file"
    psql -h localhost -U $db_user -d $db_name -f "$script_file"
    log_info "SQL脚本执行完成"
}

# 验证数据库
verify_database() {
    local db_name=$1
    local db_user=$2
    
    log_info "验证数据库结构..."
    
    # 检查表是否存在
    local table_count=$(psql -h localhost -U $db_user -d $db_name -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
    
    if [ "$table_count" -gt 0 ]; then
        log_info "数据库验证成功，共创建 $table_count 个表"
    else
        log_error "数据库验证失败，没有找到表"
        exit 1
    fi
    
    # 检查数据是否存在
    local user_count=$(psql -h localhost -U $db_user -d $db_name -t -c "SELECT COUNT(*) FROM users;")
    log_info "用户表中有 $user_count 条记录"
}

# 创建备份
create_backup() {
    local db_name=$1
    local backup_dir=$2
    
    if [ ! -d "$backup_dir" ]; then
        mkdir -p "$backup_dir"
    fi
    
    local backup_file="$backup_dir/testplatform_$(date +%Y%m%d_%H%M%S).sql"
    log_info "创建数据库备份: $backup_file"
    
    pg_dump -h localhost -U postgres -d $db_name > "$backup_file"
    log_info "备份创建完成: $backup_file"
}

# 主函数
main() {
    log_info "开始部署端到端测试平台数据库..."
    
    # 配置参数
    DB_NAME=${DB_NAME:-testplatform}
    DB_USER=${DB_USER:-testplatform}
    DB_PASSWORD=${DB_PASSWORD:-testplatform123}
    BACKUP_DIR=${BACKUP_DIR:-./backups}
    
    # 检查PostgreSQL
    check_postgresql
    
    # 检查数据库是否存在
    if check_database $DB_NAME; then
        log_info "使用现有数据库"
    else
        # 创建备份
        create_backup $DB_NAME $BACKUP_DIR
        
        # 创建数据库
        create_database $DB_NAME $DB_USER $DB_PASSWORD
    fi
    
    # 执行初始化脚本
    execute_sql_script "init.sql" $DB_NAME $DB_USER
    
    # 询问是否导入示例数据
    read -p "是否要导入示例数据？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        execute_sql_script "sample-data.sql" $DB_NAME $DB_USER
        log_info "示例数据导入完成"
    fi
    
    # 验证数据库
    verify_database $DB_NAME $DB_USER
    
    log_info "数据库部署完成！"
    log_info "数据库名称: $DB_NAME"
    log_info "用户名: $DB_USER"
    log_info "连接命令: psql -h localhost -U $DB_USER -d $DB_NAME"
}

# 显示帮助信息
show_help() {
    echo "端到端测试平台数据库部署脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示帮助信息"
    echo "  -n, --name NAME         数据库名称 (默认: testplatform)"
    echo "  -u, --user USER         数据库用户名 (默认: testplatform)"
    echo "  -p, --password PASS     数据库密码 (默认: testplatform123)"
    echo "  -b, --backup-dir DIR    备份目录 (默认: ./backups)"
    echo "  --no-sample-data        不导入示例数据"
    echo ""
    echo "环境变量:"
    echo "  DB_NAME                 数据库名称"
    echo "  DB_USER                 数据库用户名"
    echo "  DB_PASSWORD             数据库密码"
    echo "  BACKUP_DIR              备份目录"
    echo ""
    echo "示例:"
    echo "  $0                                    # 使用默认配置"
    echo "  $0 -n mydb -u myuser -p mypass       # 自定义配置"
    echo "  DB_NAME=mydb $0                      # 使用环境变量"
}

# 解析命令行参数
NO_SAMPLE_DATA=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -n|--name)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -p|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -b|--backup-dir)
            BACKUP_DIR="$2"
            shift 2
            ;;
        --no-sample-data)
            NO_SAMPLE_DATA=true
            shift
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main
