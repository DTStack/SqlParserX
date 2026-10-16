#!/bin/bash
# ============================================
# SqlParserX 一键推送部署脚本
#   前端: conf/static/front -> 服务器 conf/static/front
#   Website: website/build -> 服务器 conf/static/website
#   后端: 业务 jar -> 服务器 dtstack/，完整包再带 lib/
# 使用: ./push.sh [选项]
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载配置文件
if [ -f "$SCRIPT_DIR/deploy.conf" ]; then
    source "$SCRIPT_DIR/deploy.conf"
else
    echo -e "\033[0;31m[ERROR]\033[0m 配置文件不存在: $SCRIPT_DIR/deploy.conf"
    echo "请复制 deploy.conf.example 为 deploy.conf 并修改配置"
    exit 1
fi

# 项目根目录自动识别（包含 sqlparserx-web）
PROJECT_ROOT="$SCRIPT_DIR"
while [ ! -d "$PROJECT_ROOT/sqlparserx-web" ] && [ "$PROJECT_ROOT" != "/" ]; do
    PROJECT_ROOT="$(dirname "$PROJECT_ROOT")"
done

# 本地路径
BACKEND_PACKAGE_DIR="$PROJECT_ROOT/sqlparserx-web/target/dtstack-platform/dtstack-platform"
BACKEND_JAR_DIR="$BACKEND_PACKAGE_DIR/dtstack"
BACKEND_LIB_DIR="$BACKEND_PACKAGE_DIR/lib"
FRONTEND_STATIC_DIR="$PROJECT_ROOT/conf/static/front"
WEBSITE_BUILD_DIR="$PROJECT_ROOT/website/build"

# 服务器路径
SERVER_JAR_DIR="$SERVER_DEPLOY_DIR/dtstack"
SERVER_LIB_DIR="$SERVER_DEPLOY_DIR/lib"
SERVER_STATIC_DIR="$SERVER_DEPLOY_DIR/conf/static"
SERVER_FRONTEND_DIR="$SERVER_STATIC_DIR/front"
SERVER_WEBSITE_DIR="$SERVER_STATIC_DIR/website"
LOCAL_APPLICATION_CONFIG="$PROJECT_ROOT/conf/application.properties"

# 服务启停脚本（位于 $SERVER_DEPLOY_DIR/bin 下，脚本内部要求以 admin 用户运行）
SERVICE_SCRIPT="centos-sqlparserx.sh"
SERVICE_USER="admin"

# SSH 选项
SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=10"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ============================================
# 解析命令行参数
# ============================================
ONLY_FRONTEND=false
ONLY_BACKEND=false
ONLY_BACKEND_FULL=false
ONLY_RESTART=false
ONLY_WEBSITE=false

show_usage() {
    echo "用法: $0 [选项]"
    echo "选项:"
    echo "  -f, --frontend    仅部署 UI 前端（conf/static/front）"
    echo "  -b, --backend     仅部署后端（业务 jar）"
    echo "  -ba              部署后端完整包（业务 jar + lib）"
    echo "  -wf, --website    仅部署 website 文档站点"
    echo "  -r, --restart     仅重启服务"
    echo "  -h, --help        显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0              # 部署全部（前端 + 后端）"
    echo "  $0 -f           # 仅部署前端"
    echo "  $0 -b           # 仅部署后端"
    echo "  $0 -ba          # 部署后端完整包（jar + lib）"
    echo "  $0 -wf          # 部署 website 到 conf/static/website"
    echo "  $0 -r           # 仅重启服务"
}

while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--frontend)
            ONLY_FRONTEND=true
            shift
            ;;
        -b|--backend)
            ONLY_BACKEND=true
            shift
            ;;
        -ba)
            ONLY_BACKEND_FULL=true
            shift
            ;;
        -wf|--website)
            ONLY_WEBSITE=true
            shift
            ;;
        -r|--restart)
            ONLY_RESTART=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            echo -e "${RED}[ERROR]${NC} 未知参数: $1"
            show_usage
            exit 1
            ;;
    esac
done

# 只能选择一种部署模式
MODE_COUNT=0
[ "$ONLY_FRONTEND" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_BACKEND" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_BACKEND_FULL" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_RESTART" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_WEBSITE" = true ] && MODE_COUNT=$((MODE_COUNT + 1))

if [ "$MODE_COUNT" -gt 1 ]; then
    echo -e "${RED}[ERROR]${NC} 不能同时指定多个部署模式"
    exit 1
fi

# ============================================
# 推送后端（业务 jar）
# ============================================
deploy_backend() {
    echo -e "${YELLOW}[1/2] 检查本地后端文件...${NC}"
    JAR_FILE=$(find "$BACKEND_JAR_DIR" -maxdepth 1 -name "*.jar" -type f 2>/dev/null | head -1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}✗ 找不到业务 jar，请先执行 ./build.sh -b${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ 找到业务 jar: $(basename "$JAR_FILE")${NC}"
    echo ""

    local server_port
    server_port=$(awk -F= '$1 ~ /^[[:space:]]*server[.]port[[:space:]]*$/ {gsub(/[[:space:]]/, "", $2); print $2; exit}' "$LOCAL_APPLICATION_CONFIG")
    if [[ ! "$server_port" =~ ^[0-9]+$ ]]; then
        echo -e "${RED}✗ 无法从 $LOCAL_APPLICATION_CONFIG 读取有效的 server.port${NC}"
        return 1
    fi

    echo -e "${YELLOW}[同步] 服务器后端端口设为 $server_port...${NC}"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "mkdir -p '$SERVER_DEPLOY_DIR/conf' && if grep -Eq '^[[:space:]]*server\\.port[[:space:]]*=' '$SERVER_DEPLOY_DIR/conf/application.properties'; then sed -i -E 's/^[[:space:]]*server\\.port[[:space:]]*=.*/server.port=$server_port/' '$SERVER_DEPLOY_DIR/conf/application.properties'; else printf '\\nserver.port=$server_port\\n' >> '$SERVER_DEPLOY_DIR/conf/application.properties'; fi" || return 1
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "if [ -f '$SERVER_DEPLOY_DIR/bin/centos-sqlparserx.sh' ]; then sed -i -E 's/^REMOTE_PORT=.*/REMOTE_PORT=$server_port/' '$SERVER_DEPLOY_DIR/bin/centos-sqlparserx.sh'; fi" || return 1
    rsync -avz -e "ssh $SSH_OPTS" \
        "$PROJECT_ROOT/bin/base.sh" "$PROJECT_ROOT/bin/centos-sqlparserx.sh" \
        "$SERVER_USER@$SERVER_IP:$SERVER_DEPLOY_DIR/bin/" || return 1

    echo -e "${YELLOW}[2/2] 推送业务 jar（备份服务器旧 jar）...${NC}"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "mkdir -p '$SERVER_JAR_DIR'; cd '$SERVER_JAR_DIR' && for jar in *.jar; do [ -f \"\$jar\" ] && mv \"\$jar\" \"\${jar}.backup_\$(date +%Y%m%d_%H%M%S)\"; done" || return 1

    echo -e "${BLUE}[执行] rsync -avz --progress \"$JAR_FILE\" \"$SERVER_USER@$SERVER_IP:$SERVER_JAR_DIR/\"${NC}"
    rsync -avz --progress -e "ssh $SSH_OPTS" "$JAR_FILE" "$SERVER_USER@$SERVER_IP:$SERVER_JAR_DIR/" || return 1
    echo -e "${GREEN}✓ 后端业务 jar 推送完成${NC}"
}

# ============================================
# 推送后端完整包（业务 jar + lib）
# ============================================
deploy_backend_full() {
    deploy_backend || return 1
    echo ""
    echo -e "${YELLOW}[补充] 推送 lib 目录（第三方依赖，直接覆盖）...${NC}"

    if [ ! -d "$BACKEND_LIB_DIR" ]; then
        echo -e "${RED}✗ 找不到 lib 目录: $BACKEND_LIB_DIR${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ 找到 $(find "$BACKEND_LIB_DIR" -name "*.jar" -type f | wc -l | tr -d ' ') 个依赖 jar${NC}"

    local lib_backup_dir="${SERVER_LIB_DIR}.backup_$(date +%Y%m%d_%H%M%S)"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "if [ -d '$SERVER_LIB_DIR' ]; then mv '$SERVER_LIB_DIR' '$lib_backup_dir'; fi; mkdir -p '$SERVER_LIB_DIR'" || return 1
    echo -e "${BLUE}[执行] rsync -avz --progress \"$BACKEND_LIB_DIR/\" \"$SERVER_USER@$SERVER_IP:$SERVER_LIB_DIR/\"${NC}"
    rsync -avz --progress -e "ssh $SSH_OPTS" "$BACKEND_LIB_DIR/" "$SERVER_USER@$SERVER_IP:$SERVER_LIB_DIR/" || return 1
    echo -e "${GREEN}✓ lib 目录推送完成${NC}"
}

# ============================================
# 推送 UI 前端（conf/static/front）
# ============================================
deploy_frontend() {
    echo -e "${YELLOW}[1/2] 检查本地前端文件...${NC}"
    if [ ! -d "$FRONTEND_STATIC_DIR" ] || [ -z "$(ls -A "$FRONTEND_STATIC_DIR" 2>/dev/null)" ]; then
        echo -e "${RED}✗ 找不到前端产物 $FRONTEND_STATIC_DIR，请先执行 ./build.sh -f${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ 找到前端产物: $FRONTEND_STATIC_DIR${NC}"
    echo ""

    echo -e "${YELLOW}[2/2] 清理服务器旧 UI 文件并推送...${NC}"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "mkdir -p '$SERVER_FRONTEND_DIR' && find '$SERVER_FRONTEND_DIR' -mindepth 1 -maxdepth 1 -exec rm -rf {} +" || return 1

    echo -e "${BLUE}[执行] rsync -avz --progress \"$FRONTEND_STATIC_DIR/\" \"$SERVER_USER@$SERVER_IP:$SERVER_FRONTEND_DIR/\"${NC}"
    rsync -avz --progress -e "ssh $SSH_OPTS" "$FRONTEND_STATIC_DIR/" "$SERVER_USER@$SERVER_IP:$SERVER_FRONTEND_DIR/" || return 1
    echo -e "${GREEN}✓ 前端推送完成${NC}"
}

# ============================================
# 推送 website 文档站点
# ============================================
deploy_website() {
    echo -e "${YELLOW}[1/2] 检查本地 website 构建产物...${NC}"
    if [ ! -f "$WEBSITE_BUILD_DIR/index.html" ]; then
        echo -e "${RED}✗ 找不到 website 构建产物，请先执行 ./build.sh -wf${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ 找到 website 构建产物: $WEBSITE_BUILD_DIR${NC}"
    echo ""

    echo -e "${YELLOW}[2/2] 清理服务器旧 website 文件并推送...${NC}"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" \
        "mkdir -p '$SERVER_WEBSITE_DIR' && find '$SERVER_WEBSITE_DIR' -mindepth 1 -maxdepth 1 -exec rm -rf {} +" || return 1

    echo -e "${BLUE}[执行] rsync -avz --progress \"$WEBSITE_BUILD_DIR/\" \"$SERVER_USER@$SERVER_IP:$SERVER_WEBSITE_DIR/\"${NC}"
    rsync -avz --progress -e "ssh $SSH_OPTS" "$WEBSITE_BUILD_DIR/" "$SERVER_USER@$SERVER_IP:$SERVER_WEBSITE_DIR/" || return 1
    echo -e "${GREEN}✓ website 文档站点推送完成${NC}"
}

# ============================================
# 重启服务
# ============================================
deploy_restart() {
    echo -e "${YELLOW}[1/1] 重启服务...${NC}"
    local cmd="cd '$SERVER_DEPLOY_DIR/bin' && ./$SERVICE_SCRIPT restart"

    if [ "$SERVER_USER" != "$SERVICE_USER" ]; then
        cmd="cd '$SERVER_DEPLOY_DIR/bin' && sudo -u $SERVICE_USER ./$SERVICE_SCRIPT restart"
    fi

    echo -e "${BLUE}[执行] ssh \"$SERVER_USER@$SERVER_IP\" \"$cmd\"${NC}"
    ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" "$cmd" || return 1
    echo -e "${GREEN}✓ 服务重启完成${NC}"
}

# ============================================
# 主流程
# ============================================
echo ""
echo "=========================================="
if [ "$ONLY_FRONTEND" = true ]; then
    echo "    SqlParserX 推送部署（仅前端）"
elif [ "$ONLY_BACKEND" = true ]; then
    echo "    SqlParserX 推送部署（仅后端）"
elif [ "$ONLY_BACKEND_FULL" = true ]; then
    echo "    SqlParserX 推送部署（后端完整包）"
elif [ "$ONLY_RESTART" = true ]; then
    echo "    SqlParserX 服务重启"
elif [ "$ONLY_WEBSITE" = true ]; then
    echo "    SqlParserX 推送部署（仅 website）"
else
    echo "    SqlParserX 推送部署（全部）"
fi
echo "=========================================="
echo ""
echo -e "${BLUE}[配置信息]${NC}"
echo "  服务器: $SERVER_USER@$SERVER_IP"
echo "  部署目录: $SERVER_DEPLOY_DIR"
echo ""

DEPLOY_FAILED=false

if [ "$ONLY_FRONTEND" = true ]; then
    deploy_frontend || DEPLOY_FAILED=true
elif [ "$ONLY_BACKEND" = true ]; then
    deploy_backend || DEPLOY_FAILED=true
elif [ "$ONLY_BACKEND_FULL" = true ]; then
    deploy_backend_full || DEPLOY_FAILED=true
elif [ "$ONLY_RESTART" = true ]; then
    deploy_restart || DEPLOY_FAILED=true
elif [ "$ONLY_WEBSITE" = true ]; then
    deploy_website || DEPLOY_FAILED=true
else
    deploy_backend || DEPLOY_FAILED=true
    echo ""
    deploy_frontend || DEPLOY_FAILED=true
fi

echo ""
if [ "$DEPLOY_FAILED" = true ]; then
    echo "=========================================="
    echo -e "${RED}✗ 部署失败${NC}"
    echo "=========================================="
    exit 1
fi

echo "=========================================="
echo -e "${GREEN}✓ 部署成功${NC}"
echo "=========================================="
echo ""
