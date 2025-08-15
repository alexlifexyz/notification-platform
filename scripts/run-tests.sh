#!/bin/bash

# 通知平台测试脚本统一入口
# 使用方法: ./scripts/run-tests.sh [test-name]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="${SCRIPT_DIR}/tests"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
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

# 显示帮助信息
show_help() {
    echo "通知平台测试脚本"
    echo ""
    echo "使用方法:"
    echo "  ./scripts/run-tests.sh [选项] [测试名称]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -l, --list     列出所有可用的测试"
    echo ""
    echo "可用的测试:"
    echo "  all                    运行所有测试"
    echo "  basic                  基础功能测试"
    echo "  direct-send           直接发送API测试"
    echo "  multi-channel         多渠道发送测试"
    echo "  idempotency           幂等性测试"
    echo "  async-send            异步发送功能测试"
    echo "  simplified-send       简化发送接口测试（多用户+扁平化）"
    echo "  email                 邮件API测试"
    echo "  swagger               Swagger接口测试"
    echo "  startup               简单启动测试"
    echo ""
    echo "示例:"
    echo "  ./scripts/run-tests.sh direct-send"
    echo "  ./scripts/run-tests.sh all"
}

# 列出所有测试
list_tests() {
    print_info "可用的测试脚本:"
    echo ""
    for script in "${TESTS_DIR}"/*.sh; do
        if [[ -f "$script" ]]; then
            basename "$script" .sh | sed 's/test-/  /'
        fi
    done
    echo ""
}

# 运行指定测试
run_test() {
    local test_name="$1"
    local script_path="${TESTS_DIR}/test-${test_name}.sh"
    
    if [[ ! -f "$script_path" ]]; then
        print_error "测试脚本不存在: $script_path"
        return 1
    fi
    
    print_info "运行测试: $test_name"
    echo "========================================"
    
    if bash "$script_path"; then
        print_success "测试完成: $test_name"
    else
        print_error "测试失败: $test_name"
        return 1
    fi
    
    echo ""
}

# 运行所有测试
run_all_tests() {
    print_info "运行所有测试..."
    echo ""
    
    local failed_tests=()
    
    # 按顺序运行测试
    local test_order=(
        "simple-startup"
        "swagger-endpoints"
        "email-api"
        "direct-send-api"
        "multi-channel-send"
        "multi-channel-idempotency"
        "async-send"
        "simplified-send"
        "all-notifications"
    )
    
    for test in "${test_order[@]}"; do
        if ! run_test "$test"; then
            failed_tests+=("$test")
        fi
    done
    
    echo "========================================"
    if [[ ${#failed_tests[@]} -eq 0 ]]; then
        print_success "所有测试通过!"
    else
        print_error "以下测试失败: ${failed_tests[*]}"
        return 1
    fi
}

# 主函数
main() {
    case "${1:-}" in
        -h|--help)
            show_help
            ;;
        -l|--list)
            list_tests
            ;;
        all)
            run_all_tests
            ;;
        "")
            print_warning "请指定要运行的测试，或使用 --help 查看帮助"
            list_tests
            ;;
        *)
            run_test "$1"
            ;;
    esac
}

# 检查测试目录是否存在
if [[ ! -d "$TESTS_DIR" ]]; then
    print_error "测试目录不存在: $TESTS_DIR"
    exit 1
fi

main "$@"
