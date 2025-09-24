#!/bin/bash

# 端到端测试平台数据库结构验证脚本
# 用于验证数据库是否正确创建和配置

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认配置
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"5432"}
DB_NAME=${DB_NAME:-"testplatform"}
DB_USER=${DB_USER:-"testplatform"}
DB_PASSWORD=${DB_PASSWORD:-"testplatform123"}

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

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 检查数据库连接
check_connection() {
    log_info "检查数据库连接..."
    
    if ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" > /dev/null 2>&1; then
        log_error "无法连接到数据库 $DB_NAME"
        log_error "请检查数据库配置："
        log_error "  Host: $DB_HOST"
        log_error "  Port: $DB_PORT"
        log_error "  Database: $DB_NAME"
        log_error "  User: $DB_USER"
        exit 1
    fi
    
    log_success "数据库连接成功"
}

# 检查表是否存在
check_tables() {
    log_info "检查核心表是否存在..."
    
    local expected_tables=(
        "users"
        "user_sessions"
        "test_environments"
        "global_variables"
        "test_suites"
        "test_cases"
        "test_suite_cases"
        "test_executions"
        "test_case_executions"
        "test_reports"
        "scheduled_tasks"
        "task_execution_history"
    )
    
    local missing_tables=()
    
    for table in "${expected_tables[@]}"; do
        local exists=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '$table';")
        
        if [ "$exists" -eq 0 ]; then
            missing_tables+=("$table")
        else
            log_success "表 $table 存在"
        fi
    done
    
    if [ ${#missing_tables[@]} -gt 0 ]; then
        log_error "以下表缺失："
        for table in "${missing_tables[@]}"; do
            log_error "  - $table"
        done
        return 1
    fi
    
    log_success "所有核心表都存在"
}

# 检查表结构
check_table_structure() {
    log_info "检查表结构..."
    
    # 检查users表结构
    log_info "检查users表结构..."
    local user_columns=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'users' ORDER BY ordinal_position;")
    local expected_user_columns=("id" "username" "email" "password_hash" "full_name" "role" "is_active" "last_login" "created_at" "updated_at")
    
    for column in "${expected_user_columns[@]}"; do
        if echo "$user_columns" | grep -q "$column"; then
            log_success "  ✓ $column"
        else
            log_error "  ✗ $column 缺失"
        fi
    done
    
    # 检查test_cases表结构
    log_info "检查test_cases表结构..."
    local test_case_columns=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'test_cases' ORDER BY ordinal_position;")
    local expected_test_case_columns=("id" "suite_id" "name" "description" "test_type" "priority" "status" "config" "preconditions" "expected_result" "tags" "created_by" "created_at" "updated_at")
    
    for column in "${expected_test_case_columns[@]}"; do
        if echo "$test_case_columns" | grep -q "$column"; then
            log_success "  ✓ $column"
        else
            log_error "  ✗ $column 缺失"
        fi
    done
}

# 检查索引
check_indexes() {
    log_info "检查索引..."
    
    local expected_indexes=(
        "idx_users_username"
        "idx_users_email"
        "idx_test_cases_suite_id"
        "idx_test_cases_test_type"
        "idx_test_executions_suite_id"
        "idx_test_executions_status"
    )
    
    for index in "${expected_indexes[@]}"; do
        local exists=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM pg_indexes WHERE indexname = '$index';")
        
        if [ "$exists" -gt 0 ]; then
            log_success "索引 $index 存在"
        else
            log_warn "索引 $index 不存在"
        fi
    done
}

# 检查约束
check_constraints() {
    log_info "检查约束..."
    
    # 检查test_cases表的约束
    local constraints=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'test_cases' AND constraint_type = 'CHECK';")
    
    if echo "$constraints" | grep -q "chk_test_cases_priority"; then
        log_success "约束 chk_test_cases_priority 存在"
    else
        log_warn "约束 chk_test_cases_priority 不存在"
    fi
    
    if echo "$constraints" | grep -q "chk_test_cases_status"; then
        log_success "约束 chk_test_cases_status 存在"
    else
        log_warn "约束 chk_test_cases_status 不存在"
    fi
}

# 检查视图
check_views() {
    log_info "检查视图..."
    
    local views=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT viewname FROM pg_views WHERE schemaname = 'public';")
    
    if echo "$views" | grep -q "test_suite_stats"; then
        log_success "视图 test_suite_stats 存在"
    else
        log_warn "视图 test_suite_stats 不存在"
    fi
    
    if echo "$views" | grep -q "test_execution_stats"; then
        log_success "视图 test_execution_stats 存在"
    else
        log_warn "视图 test_execution_stats 不存在"
    fi
}

# 检查函数
check_functions() {
    log_info "检查函数..."
    
    local functions=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT routine_name FROM information_schema.routines WHERE routine_schema = 'public';")
    
    if echo "$functions" | grep -q "update_updated_at_column"; then
        log_success "函数 update_updated_at_column 存在"
    else
        log_warn "函数 update_updated_at_column 不存在"
    fi
    
    if echo "$functions" | grep -q "cleanup_expired_sessions"; then
        log_success "函数 cleanup_expired_sessions 存在"
    else
        log_warn "函数 cleanup_expired_sessions 不存在"
    fi
}

# 检查数据
check_data() {
    log_info "检查示例数据..."
    
    # 检查用户数据
    local user_count=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM users;")
    if [ "$user_count" -gt 0 ]; then
        log_success "用户表中有 $user_count 条记录"
    else
        log_warn "用户表中没有数据"
    fi
    
    # 检查测试环境数据
    local env_count=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM test_environments;")
    if [ "$env_count" -gt 0 ]; then
        log_success "测试环境表中有 $env_count 条记录"
    else
        log_warn "测试环境表中没有数据"
    fi
    
    # 检查测试套件数据
    local suite_count=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM test_suites;")
    if [ "$suite_count" -gt 0 ]; then
        log_success "测试套件表中有 $suite_count 条记录"
    else
        log_warn "测试套件表中没有数据"
    fi
}

# 检查性能
check_performance() {
    log_info "检查数据库性能..."
    
    # 检查表大小
    local table_sizes=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
        SELECT 
            schemaname,
            tablename,
            pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
        FROM pg_tables 
        WHERE schemaname = 'public' 
        ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
    ")
    
    echo -e "${BLUE}表大小统计：${NC}"
    echo "$table_sizes"
    
    # 检查索引使用情况
    local index_usage=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
        SELECT 
            schemaname,
            tablename,
            indexname,
            idx_scan,
            idx_tup_read,
            idx_tup_fetch
        FROM pg_stat_user_indexes 
        WHERE schemaname = 'public'
        ORDER BY idx_scan DESC;
    ")
    
    echo -e "${BLUE}索引使用统计：${NC}"
    echo "$index_usage"
}

# 生成报告
generate_report() {
    log_info "生成验证报告..."
    
    local report_file="database_validation_report_$(date +%Y%m%d_%H%M%S).txt"
    
    {
        echo "端到端测试平台数据库验证报告"
        echo "生成时间: $(date)"
        echo "数据库: $DB_NAME@$DB_HOST:$DB_PORT"
        echo "用户: $DB_USER"
        echo "=========================================="
        echo ""
        
        echo "表结构检查："
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            SELECT 
                table_name,
                column_name,
                data_type,
                is_nullable,
                column_default
            FROM information_schema.columns 
            WHERE table_schema = 'public' 
            ORDER BY table_name, ordinal_position;
        "
        
        echo ""
        echo "索引检查："
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            SELECT 
                schemaname,
                tablename,
                indexname,
                indexdef
            FROM pg_indexes 
            WHERE schemaname = 'public'
            ORDER BY tablename, indexname;
        "
        
        echo ""
        echo "数据统计："
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            SELECT 
                'users' as table_name, COUNT(*) as row_count FROM users
            UNION ALL
            SELECT 'test_environments', COUNT(*) FROM test_environments
            UNION ALL
            SELECT 'test_suites', COUNT(*) FROM test_suites
            UNION ALL
            SELECT 'test_cases', COUNT(*) FROM test_cases
            UNION ALL
            SELECT 'test_executions', COUNT(*) FROM test_executions
            UNION ALL
            SELECT 'test_reports', COUNT(*) FROM test_reports;
        "
        
    } > "$report_file"
    
    log_success "验证报告已生成: $report_file"
}

# 主函数
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  端到端测试平台数据库验证工具${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    # 检查连接
    check_connection
    echo ""
    
    # 检查表
    check_tables
    echo ""
    
    # 检查表结构
    check_table_structure
    echo ""
    
    # 检查索引
    check_indexes
    echo ""
    
    # 检查约束
    check_constraints
    echo ""
    
    # 检查视图
    check_views
    echo ""
    
    # 检查函数
    check_functions
    echo ""
    
    # 检查数据
    check_data
    echo ""
    
    # 检查性能
    check_performance
    echo ""
    
    # 生成报告
    generate_report
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  数据库验证完成${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# 显示帮助信息
show_help() {
    echo "端到端测试平台数据库验证脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示帮助信息"
    echo "  -H, --host HOST         数据库主机 (默认: localhost)"
    echo "  -p, --port PORT         数据库端口 (默认: 5432)"
    echo "  -d, --database DB       数据库名称 (默认: testplatform)"
    echo "  -u, --user USER         数据库用户名 (默认: testplatform)"
    echo "  -P, --password PASS     数据库密码 (默认: testplatform123)"
    echo ""
    echo "环境变量:"
    echo "  DB_HOST                 数据库主机"
    echo "  DB_PORT                 数据库端口"
    echo "  DB_NAME                 数据库名称"
    echo "  DB_USER                 数据库用户名"
    echo "  DB_PASSWORD             数据库密码"
    echo ""
    echo "示例:"
    echo "  $0                                    # 使用默认配置"
    echo "  $0 -H localhost -p 5432 -d mydb      # 自定义配置"
    echo "  DB_NAME=mydb $0                      # 使用环境变量"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -H|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -P|--password)
            DB_PASSWORD="$2"
            shift 2
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
