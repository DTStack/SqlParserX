#!/bin/bash
# ============================================
# SqlParserX 本地打包脚本
#   后端: Maven 打包（sqlparserx-engine + sqlparserx-web）
#   前端: Vite 打包，产物同步到项目 conf/static
#   文档: Docusaurus 打包，产物保留在 website/build，部署时同步到服务根目录 website
# 使用: 将脚本放在项目任意位置，自动识别项目根路径
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ============================================
# 路径自动识别
# ============================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/sqlparserx-ui"
FRONTEND_STATIC_DIR="$PROJECT_ROOT/conf/static"
BACKEND_STATIC_DIR="$PROJECT_ROOT/sqlparserx-web/src/main/resources/static"
WEBSITE_DIR="$PROJECT_ROOT/website"
DEPLOY_CONF="$SCRIPT_DIR/deploy.conf"

if [ -f "$DEPLOY_CONF" ]; then
    # shellcheck source=/dev/null
    source "$DEPLOY_CONF"
fi

# ============================================
# 解析命令行参数
# ============================================
ONLY_FRONTEND=false
ONLY_BACKEND=false
ONLY_WEBSITE=false

show_usage() {
    echo "用法: $0 [选项]"
    echo "选项:"
    echo "  -f, --frontend    仅打包 UI 前端（产物同步到 conf/static）"
    echo "  -b, --backend     仅打包后端"
    echo "  -wf, --website    仅打包 website 文档站点"
    echo "  -h, --help        显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0              # 打包全部（前端 + 后端）"
    echo "  $0 -f           # 仅打包前端"
    echo "  $0 -b           # 仅打包后端"
    echo "  $0 -wf          # 仅打包 website 文档站点"
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
        -wf|--website)
            ONLY_WEBSITE=true
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

MODE_COUNT=0
[ "$ONLY_FRONTEND" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_BACKEND" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
[ "$ONLY_WEBSITE" = true ] && MODE_COUNT=$((MODE_COUNT + 1))
if [ "$MODE_COUNT" -gt 1 ]; then
    echo -e "${RED}[ERROR]${NC} -f、-b、-wf 只能选择一个"
    exit 1
fi

# ============================================
# 工具函数
# ============================================
print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_separator() { echo "=========================================="; }

# ============================================
# 后端打包（在项目根目录执行）
# ============================================
build_backend() {
    print_info "开始打包后端..."
    print_separator

    # 主工程的编译目标为 Java 8；BUILD_JAVA_HOME 可用于显式覆盖。
    # macOS 优先自动选择 JDK 8，其它环境再沿用 JAVA_HOME。
    local build_java_home="${BUILD_JAVA_HOME:-}"
    if [ -z "$build_java_home" ] && [ -x /usr/libexec/java_home ]; then
        build_java_home="$(/usr/libexec/java_home -v 1.8 2>/dev/null || true)"
    fi
    if [ -z "$build_java_home" ]; then
        build_java_home="${JAVA_HOME:-}"
    fi
    if [ -n "$build_java_home" ]; then
        export JAVA_HOME="$build_java_home"
        export PATH="$JAVA_HOME/bin:$PATH"
        print_info "Maven 使用 Java: $JAVA_HOME"
    fi

    cd "$PROJECT_ROOT" || { print_error "无法进入项目根目录: $PROJECT_ROOT"; exit 1; }
    print_info "当前目录: $(pwd)"

    local mvn_cmd="mvn clean package -U -DskipTests"
    print_info "执行: $mvn_cmd"
    $mvn_cmd

    if [ $? -ne 0 ]; then
        print_error "后端打包失败"
        exit 1
    fi

    print_success "后端打包成功"
    local package_dir="$PROJECT_ROOT/sqlparserx-web/target/dtstack-platform/dtstack-platform"
    if [ -d "$package_dir" ]; then
        print_info "打包产物目录: $package_dir"
        print_info "  业务 jar: $package_dir/dtstack"
        print_info "  依赖 lib: $package_dir/lib ($(ls -1 "$package_dir/lib"/*.jar 2>/dev/null | wc -l | tr -d ' ') 个 jar)"
    fi
    print_separator
}

# ============================================
# 前端打包（产物同步到 conf/static）
# ============================================
build_frontend() {
    print_info "开始打包前端..."
    print_separator

    if [ ! -d "$FRONTEND_DIR" ]; then
        print_error "前端目录不存在: $FRONTEND_DIR"
        exit 1
    fi

    cd "$FRONTEND_DIR" || { print_error "无法进入前端目录: $FRONTEND_DIR"; exit 1; }
    print_info "当前目录: $(pwd)"

    if [ -d "$FRONTEND_DIR/dist" ]; then
        print_info "删除旧的 dist 目录..."
        rm -rf "$FRONTEND_DIR/dist"
    fi

    if [ ! -x "$FRONTEND_DIR/node_modules/.bin/vite" ]; then
        print_warning "未检测到前端依赖，开始执行 npm ci..."
        npm ci || { print_error "前端依赖安装失败"; exit 1; }
    fi

    print_info "执行: npm run build:prod"
    npm run build:prod || { print_error "前端打包失败"; exit 1; }

    if [ ! -d "$FRONTEND_DIR/dist" ]; then
        print_error "前端打包失败：未生成 dist 目录"
        exit 1
    fi

    # UI 产物直接对应后端服务的 conf/static，website 已迁移到服务根目录 website。
    print_info "同步产物到: $FRONTEND_STATIC_DIR"
    rm -rf "$FRONTEND_STATIC_DIR"
    mkdir -p "$FRONTEND_STATIC_DIR"
    cp -r "$FRONTEND_DIR/dist/." "$FRONTEND_STATIC_DIR/"

    # 叠加由后端维护的 SEO 落地页及站点级文件；不包含根 index.html，避免改变 SPA 首页。
    if [ -d "$BACKEND_STATIC_DIR" ]; then
        cp -r "$BACKEND_STATIC_DIR/." "$FRONTEND_STATIC_DIR/"
    fi
    "$SCRIPT_DIR/render-seo-files.sh" "$FRONTEND_STATIC_DIR" "$PROJECT_ROOT/conf/application.properties" || { print_error "SEO 静态文件渲染失败"; exit 1; }

    local seo_site_url
    seo_site_url=${SEO_SITE_URL_OVERRIDE:-$(sed -n 's/^[[:space:]]*seo\.site-url[[:space:]]*=[[:space:]]*//p' "$PROJECT_ROOT/conf/application.properties" | tail -n 1)}
    seo_site_url=${seo_site_url%/}
    node "$SCRIPT_DIR/merge-sitemaps.mjs" \
        "$BACKEND_STATIC_DIR/sitemap.xml" \
        "$WEBSITE_DIR/build/sitemap.xml" \
        "$FRONTEND_STATIC_DIR/sitemap.xml" \
        "$seo_site_url" || { print_error "Sitemap 合并失败"; exit 1; }

    print_success "前端打包成功"
    print_info "conf/static 大小: $(du -sh "$FRONTEND_STATIC_DIR" | cut -f1)，文件数: $(find "$FRONTEND_STATIC_DIR" -type f | wc -l | tr -d ' ')"
    print_separator
}

# ============================================
# Website 文档站点打包
# ============================================
build_website() {
    print_info "开始打包 website 文档站点..."
    print_separator

    if [ ! -d "$WEBSITE_DIR" ]; then
        print_error "website 目录不存在: $WEBSITE_DIR"
        exit 1
    fi

    cd "$WEBSITE_DIR" || { print_error "无法进入 website 目录: $WEBSITE_DIR"; exit 1; }
    print_info "当前目录: $(pwd)"

    if [ ! -x "$WEBSITE_DIR/node_modules/.bin/docusaurus" ]; then
        print_warning "未检测到 website 依赖，开始执行 npm ci..."
        npm ci || { print_error "website 依赖安装失败"; exit 1; }
    fi

    local seo_site_url
    seo_site_url=${SEO_SITE_URL_OVERRIDE:-$(sed -n 's/^[[:space:]]*seo\.site-url[[:space:]]*=[[:space:]]*//p' "$PROJECT_ROOT/conf/application.properties" | tail -n 1)}
    seo_site_url=${seo_site_url%/}
    print_info "执行: DOCUSAURUS_URL=$seo_site_url DOCUSAURUS_BASE_URL=/website/ npm run build"
    DOCUSAURUS_URL="$seo_site_url" DOCUSAURUS_BASE_URL="/website/" npm run build || { print_error "website 打包失败"; exit 1; }

    if [ ! -f "$WEBSITE_DIR/build/index.html" ]; then
        print_error "website 打包失败：未生成 build/index.html"
        exit 1
    fi

    if [ -d "$FRONTEND_STATIC_DIR" ]; then
        node "$SCRIPT_DIR/merge-sitemaps.mjs" \
            "$BACKEND_STATIC_DIR/sitemap.xml" \
            "$WEBSITE_DIR/build/sitemap.xml" \
            "$FRONTEND_STATIC_DIR/sitemap.xml" \
            "$seo_site_url" || { print_error "Sitemap 合并失败"; exit 1; }
    fi

    print_success "website 打包成功"
    print_info "website/build 大小: $(du -sh "$WEBSITE_DIR/build" | cut -f1)，文件数: $(find "$WEBSITE_DIR/build" -type f | wc -l | tr -d ' ')"
    print_separator
}

# ============================================
# 主函数
# ============================================
main() {
    echo ""
    print_separator
    if [ "$ONLY_FRONTEND" = true ]; then
        echo -e "${GREEN}    SqlParserX 打包脚本（仅前端）${NC}"
    elif [ "$ONLY_BACKEND" = true ]; then
        echo -e "${GREEN}    SqlParserX 打包脚本（仅后端）${NC}"
    elif [ "$ONLY_WEBSITE" = true ]; then
        echo -e "${GREEN}    SqlParserX 打包脚本（仅 website）${NC}"
    else
        echo -e "${GREEN}    SqlParserX 打包脚本（全部）${NC}"
    fi
    print_separator
    echo ""
    print_info "项目根目录: $PROJECT_ROOT"
    echo ""

    START_TIME=$(date +%s)

    if [ "$ONLY_FRONTEND" = true ]; then
        build_frontend
    elif [ "$ONLY_BACKEND" = true ]; then
        build_backend
    elif [ "$ONLY_WEBSITE" = true ]; then
        build_website
    else
        build_backend
        echo ""
        build_frontend
    fi

    END_TIME=$(date +%s)
    print_separator
    print_success "打包完成！总耗时: $((END_TIME - START_TIME)) 秒"
    print_separator
    echo ""
    echo "【后续部署提醒】"
    echo "  UI 前端: ./build.sh -f && ./push.sh -f"
    echo "  后端: ./build.sh -b && ./push.sh -b"
    echo "  Website: ./build.sh -wf && ./push.sh -wf"
    echo ""
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main
fi
