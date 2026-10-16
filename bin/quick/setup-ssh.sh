#!/bin/bash
# ============================================
# SqlParserX 一键初始化环境
#   1. 配置 SSH 免密登录
#   2. 按 deploy.conf 中的路径创建服务器目录
# 使用: ./setup-ssh.sh
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

# 部署根目录下需要维护的子目录
DEPLOY_SUB_DIRS=("lib" "dtstack" "conf/static" "conf/static/front" "conf/static/website" "bin" "logs" "run")

echo "=========================================="
echo "    SqlParserX 环境初始化"
echo "=========================================="
echo "  服务器: $SERVER_USER@$SERVER_IP"
echo "  部署目录: $SERVER_DEPLOY_DIR"
echo "=========================================="
echo ""

# --------------------------------------------
# 1、配置 SSH 免密登录
# --------------------------------------------
echo "正在配置 SSH 免密登录到 $SERVER_USER@$SERVER_IP"

# 生成密钥（如果不存在）
if [ ! -f ~/.ssh/id_rsa ]; then
    echo "生成 SSH 密钥..."
    ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
fi

# 复制公钥到服务器（会提示输入服务器密码）
echo "请稍后输入服务器密码..."
ssh-copy-id "$SERVER_USER@$SERVER_IP"

# 测试连接
echo "测试免密登录..."
if ssh -o ConnectTimeout=5 "$SERVER_USER@$SERVER_IP" "echo '连接成功'"; then
    echo "✅ SSH 免密登录配置成功！"
else
    echo "❌ 配置失败，请检查网络和密码"
    exit 1
fi

# --------------------------------------------
# 2、按配置文件路径创建服务器目录
# --------------------------------------------
echo ""
echo "按 deploy.conf 配置创建服务器目录: $SERVER_DEPLOY_DIR"

REMOTE_MKDIR="mkdir -p '$SERVER_DEPLOY_DIR'"
for d in "${DEPLOY_SUB_DIRS[@]}"; do
    REMOTE_MKDIR="$REMOTE_MKDIR '$SERVER_DEPLOY_DIR/$d'"
done

if ssh "$SERVER_USER@$SERVER_IP" "$REMOTE_MKDIR"; then
    echo "✅ 服务器目录初始化完成"
else
    echo "❌ 服务器目录初始化失败"
    exit 1
fi

echo ""
echo "当前目录结构:"
ssh "$SERVER_USER@$SERVER_IP" "ls -la '$SERVER_DEPLOY_DIR'"
echo ""
echo "✅ 环境初始化全部完成，接下来可以执行: ./build.sh && ./push.sh"
