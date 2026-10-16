# SqlParserX 打包部署脚本使用文档

`bin/quick/` 下提供了一套轻量的本地打包 / 推送部署脚本，用于把 SqlParserX 一键部署到测试或演示服务器。

> `deploy.conf` 包含本地服务器配置，已在 `.gitignore` 中单独忽略；脚本、示例配置和文档可提交到仓库。

## 一、核心功能

| 脚本 | 功能 | 说明 |
|------|------|------|
| `setup-ssh.sh` | 一键初始化环境 | 配置 SSH 免密登录，并按配置创建服务器目录 |
| `build.sh` | 本地打包 | 后端 Maven + UI 前端 Vite；可用 `-wf` 单独打包 website |
| `push.sh` | 一键推送部署 | 推送后端、UI 前端或 website，支持重启服务 |
| `deploy.conf` | 统一配置 | 服务器 IP、用户、部署目录（本地私有，不提交） |
| `deploy.conf.example` | 配置示例 | 复制为 `deploy.conf` 后修改 |

## 二、快速开始

### 2.1 首次使用（三步完成）

```bash
# 步骤 1：配置服务器信息
cp deploy.conf.example deploy.conf
vim deploy.conf   # 修改 SERVER_IP、SERVER_USER、SERVER_DEPLOY_DIR

# 步骤 2：初始化环境（配置 SSH 免密 + 创建服务器目录，只需一次）
./setup-ssh.sh

# 步骤 3：打包并部署
./build.sh && ./push.sh
./build.sh -wf && ./push.sh -wf
```

### 2.2 日常使用

```bash
# 完整流程：打包全部 → 部署全部 → 重启服务
./build.sh && ./build.sh -wf && ./push.sh -ba && ./push.sh -f && ./push.sh -wf && ./push.sh -r

# Website 文档站点
./build.sh -wf && ./push.sh -wf

# 1、前后端部署
# 仅打包前端 → 仅部署前端（重新部署前端最快方式）
./build.sh -f && ./push.sh -f
# 仅打包后端 → 仅部署后端
./build.sh -b && ./push.sh -b
# 仅打包后端 → 仅部署后端（lib + 业务 jar）
./build.sh -b && ./push.sh -ba

# 仅打包 website → 仅部署 website
./build.sh -wf && ./push.sh -wf

# 2、重启后端服务
./push.sh -r

# 3、全量部署（带 lib 和 website）
./build.sh && ./build.sh -wf && ./push.sh -ba && ./push.sh -f && ./push.sh -wf && ./push.sh -r

# 查看帮助
./build.sh -h
./push.sh -h
```

## 三、执行顺序

```
┌─────────────────────────────────────────────────────────────┐
│                      完整部署流程                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   1. 配置 deploy.conf          ← 首次执行，设置服务器信息    │
│           ↓                                                │
│   2. ./setup-ssh.sh            ← 首次执行，免密 + 建目录     │
│           ↓                                                │
│   3. ./build.sh                ← 打包后端 + 前端            │
│           ↓                                                │
│   4. ./push.sh                 ← 推送到服务器               │
│   5. ./build.sh -wf && ./push.sh -wf ← 推送 website         │
│           ↓                                                │
│   6. ./push.sh -r              ← 重启服务                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 各脚本执行内容

| 步骤 | 脚本 | 执行内容 |
|------|------|----------|
| 1 | `deploy.conf` | 配置服务器 IP、用户名、部署目录 |
| 2 | `setup-ssh.sh` | 生成 SSH 密钥 → 复制公钥到服务器 → 按配置创建部署目录 |
| 3 | `build.sh` | `mvn clean package -U -DskipTests` → UI 构建到 `conf/static/front`；`-wf` 构建 website |
| 4 | `push.sh` | 推送 jar / lib / `conf/static/front`；`-wf` 推送 website 到 `conf/static/website` |
| 5 | `push.sh -r` | 调用 `$SERVER_DEPLOY_DIR/bin/centos-sqlparserx.sh restart` 重启服务 |

## 四、配置文件说明

**文件位置**：`bin/quick/deploy.conf`

```bash
# 服务器配置
SERVER_IP="172.16.114.23"                 # 服务器 IP
SERVER_USER="root"                        # SSH 登录用户
SERVER_DEPLOY_DIR="/opt/tools/sqlparserx" # 部署根目录
```

> 脚本会自动在部署根目录下维护 `lib/`（第三方依赖）、`dtstack/`（业务 jar）、`conf/static/`（前端静态资源）、`bin/`（启停脚本）、`logs/`、`run/`。

## 五、产物说明

### 打包输出

| 类型 | 路径 | 说明 |
|------|------|------|
| 后端业务 jar | `sqlparserx-web/target/dtstack-platform/dtstack-platform/dtstack/*.jar` | 可执行业务 jar |
| 后端依赖 lib | `sqlparserx-web/target/dtstack-platform/dtstack-platform/lib/` | 第三方依赖 jar |
| UI 前端产物 | `conf/static/front/` | 由 `sqlparserx-ui/dist` 同步而来 |
| Website 产物 | `website/build/` | 由 `./build.sh -wf` 生成，部署到服务器 `conf/static/website/` |

### 部署后服务器目录结构

```
{SERVER_DEPLOY_DIR}/
├── bin/                    # 启停脚本（centos-sqlparserx.sh / base.sh）
├── conf/
│   ├── application.properties   # 后端配置（server.port=16758）
│   ├── logback.xml
│   └── static/
│       ├── front/          # 解析 UI 前端
│       └── website/        # Docusaurus 文档网站
├── dtstack/                # 业务 jar
├── lib/                    # 第三方依赖 jar
├── logs/                   # 日志
└── run/                    # 运行 pid
```

## 六、端口与前端地址

| 项 | 值 | 配置位置 |
|----|----|----------|
| 后端服务端口 | `16758` | `conf/application.properties` 的 `server.port` |
| UI 前端访问地址 | `http://172.16.114.23:16758/front/` | 静态目录 `conf/static/front/` |
| Website 文档地址 | `http://172.16.114.23:16758/website/` | 静态目录 `conf/static/website/` |
| 前端 API 地址 | `http://172.16.114.23:16758` | `sqlparserx-ui/.env.production` 的 `VITE_API_URL` |

> 说明：后端服务读取的是部署目录下 `conf/application.properties`；前端在 `build:prod` 打包时会读取 `.env.production` 中的 `VITE_API_URL`，请确保两者与目标服务器地址、端口保持一致。

## 七、常用命令速查

```bash
# 查看服务器当前部署的后端 jar
ssh $SERVER_USER@$SERVER_IP "ls -la $SERVER_DEPLOY_DIR/dtstack/*.jar"

# 手动备份服务器 jar 包
ssh $SERVER_USER@$SERVER_IP "cp $SERVER_DEPLOY_DIR/dtstack/xxx.jar $SERVER_DEPLOY_DIR/dtstack/xxx.jar.backup"

# 回滚到上一个版本（先确认备份文件名）
ssh $SERVER_USER@$SERVER_IP "cd $SERVER_DEPLOY_DIR/dtstack && mv *.backup_* xxx.jar"

# 查看服务状态 / 重启
ssh $SERVER_USER@$SERVER_IP "cd $SERVER_DEPLOY_DIR/bin && sudo -u admin ./centos-sqlparserx.sh status"
ssh $SERVER_USER@$SERVER_IP "cd $SERVER_DEPLOY_DIR/bin && sudo -u admin ./centos-sqlparserx.sh restart"
```

## 八、常见问题

| 问题 | 解决方案 |
|------|----------|
| `配置文件不存在: deploy.conf` | 执行 `cp deploy.conf.example deploy.conf` 并修改配置 |
| `找不到业务 jar` | 先执行 `./build.sh -b` 完成打包 |
| `找不到前端产物` | 先执行 `./build.sh -f` 完成打包 |
| `找不到 website 构建产物` | 先执行 `./build.sh -wf` 完成打包 |
| `Permission denied` | 执行 `chmod +x build.sh push.sh setup-ssh.sh` |
| `rsync: command not found` | 安装 rsync：`yum install rsync` 或 `apt install rsync` |
| SSH 连接失败 | 检查 `deploy.conf` 中的 IP 和用户名，重新执行 `./setup-ssh.sh` |
| 服务重启失败（用户校验） | `centos-sqlparserx.sh` 要求以 `admin` 用户运行，脚本会自动尝试 `sudo -u admin` |
| 前端接口 404 / 跨域 | 检查 `.env.production` 的 `VITE_API_URL` 与后端实际地址、端口是否一致 |

## 九、脚本依赖

| 依赖 | 版本 | 验证命令 |
|------|------|----------|
| Java | JDK 17（本地打包推荐） | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| Node.js | >= 20 | `node -v` |
| npm | 6+ | `npm -v` |
| rsync | 任意 | `rsync --version` |
| ssh | 任意 | `ssh -V` |

## 十、文件结构

```
SqlParserX/bin/quick/
├── build.sh              # 本地打包脚本
├── push.sh               # 一键推送部署脚本
├── setup-ssh.sh          # 一键初始化环境（免密 + 建目录）
├── deploy.conf           # 用户配置（不提交 git）
├── deploy.conf.example   # 配置示例（可提交）
└── README.md             # 本文档
```
