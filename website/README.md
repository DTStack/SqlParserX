# SqlParserX 文档站（Docusaurus）

SqlParserX 的**官网 + 文档站**，基于 [Docusaurus 3](https://docusaurus.io/)（classic 预设）+ React 19 构建，参考 [DTStack/Taier](https://github.com/DTStack/Taier) 官网结构。

| 能力 | 说明 |
| --- | --- |
| 🎨 深浅色主题切换 | 跟随系统 / 手动切换，Infima 变量驱动 |
| 📚 文档版本管理 | `v1.0` 与 `Next` 多版本共存 |
| 🌍 多语言 i18n | 简体中文 / English，语言目录化维护 |
| 🚀 一键发布 | 推送 `main` 即自动发布 GitHub Pages |
| 🧜 Mermaid | 架构文档中的流程图直接渲染 |

---

## 快速开始

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器（默认简体中文）
npm start          # http://localhost:3000/SqlParserX/

# 3. 生产构建 + 本地预览（中英双语同时可用）
npm run build
npm run serve

# 4. 只开发英文
npm start -- --locale en
```

> `baseUrl` 为 `/SqlParserX/`（与仓库名大小写一致），本地访问必须带该前缀。

---

## 站点结构

导航栏：**首页 / 文档 / 博客 / 社区**，右侧为版本切换与语言切换。

```
website/
├── docusaurus.config.js      # 站点总配置（品牌/导航/页脚/i18n/版本/主题）
├── sidebars.js               # 文档侧边栏结构
├── docs/                     # 当前（Next）文档
│   ├── intro.md
│   ├── about/                # SqlParserX介绍、架构设计
│   ├── quickstart/           # 环境准备与构建
│   ├── core/                 # 字段血缘、表血缘、表解析、函数解析
│   └── contributing.md       # 贡献指南
├── versioned_docs/           # v1.0 历史版本文档快照
├── versioned_sidebars/       # v1.0 侧边栏
├── versions.json             # 版本清单
├── blog/                     # 博客（含 2026-09-28 起源文章与 assets）
├── i18n/en/                  # 英文翻译
│   ├── docusaurus-plugin-content-docs/{current,version-1.0}/  # 文档
│   ├── docusaurus-plugin-content-blog/                        # 博客
│   └── docusaurus-theme-classic/{navbar,footer}.json          # 导航/页脚
├── src/
│   ├── pages/index.js        # 首页入口
│   ├── pages/community.js    # 社区页
│   ├── components/           # Intro / Features / Case 首页组件
│   └── css/custom.css        # 全局主题（深浅色变量）
├── static/img/               # 静态资源（含 architecture/ 架构图）
└── .github/workflows/deploy.yml  # GitHub Pages 自动发布
```

---

## 内容维护

### 新增一篇文档

1. 在 `docs/` 对应目录新建 `xxx.md`，补 `title` / `sidebar_label`；
2. 在 `sidebars.js` 对应分类的 `items` 中注册 `'路径/xxx'`；
3. 图片放 `static/img/`，正文用 `/img/...` 引用；
4. `npm start` 预览，`npm run build` 校验死链。

> **链接约定**：正文站内链接一律用**相对文件路径**（`./x.md`、`../y/z.md`），Docusaurus 会自动补 `baseUrl` 与语言前缀；不要写死 `/docs/...`。

### 版本管理

```bash
# 把当前 docs 快照为新版本（例：1.1）
npm run docs:version 1.1
```

生成 `versioned_docs/version-1.1/`、`versioned_sidebars/version-1.1-sidebars.json`，并更新 `versions.json`。当前已发布版本通过 `docusaurus.config.js` 的 `lastVersion: '1.0'` 指定为默认访问版本，`Next` 为开发版。

### 多语言

```bash
npm run write-translations:en          # 生成英文翻译骨架
npm start -- --locale en               # 只开发英文
```

- 文档：`i18n/en/docusaurus-plugin-content-docs/{current,version-1.0}/`
- 导航/页脚：`i18n/en/docusaurus-theme-classic/{navbar,footer}.json`
- 首页组件文案：`i18n/en/code.json`（组件文本用 `<Translate id="...">` 包裹）

> ⚠️ dev server 只服务单一语言，`npm start` 下访问 `/en/` 会 404，属正常现象；本地验证语言/版本切换请用 `npm run build && npm run serve`。

### 博客

文章放 `blog/`，文件名建议 `YYYY-MM-DD-slug.md`，图片放 `blog/assets/` 并以相对路径引用；作者与标签在 `blog/authors.yml`、`blog/tags.yml` 维护。

### 发布 GitHub Pages

CI 工作流位于**仓库根** [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml)：
推送到 `main` 且改动命中 `website/**` 时，自动 `npm ci && npm run build`，
并将 `website/build` 发布到 `gh-pages` 分支。

仓库端需完成 3 项设置：

1. **Settings → Actions → General → Workflow permissions** 选 **Read and write**；
2. **Settings → Pages → Source** 选 **Deploy from a branch** → `gh-pages` / `(root)`；
3. 触发分支为 `main`。

`baseUrl` 必须与仓库名大小写一致（当前仓库为 `SqlParserX` → `/SqlParserX/`）。
也可本地兜底发布：`GIT_USER=<name> npm run deploy`。

---

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `npm start` | 启动开发服务器 |
| `npm run build` | 生产构建（中英双语）→ `build/` |
| `npm run serve` | 本地预览构建产物 |
| `npm run clear` | 清理缓存 |
| `npm run docs:version <v>` | 创建版本文档快照 |
| `npm run write-translations:en` | 生成英文翻译骨架 |
| `npm run deploy` | 发布到 GitHub Pages |

---

## 来源

文档内容同步自项目根目录 [`docs/`](../docs/)，架构图与博客素材同源。项目地址：<https://github.com/DTStack/SqlParserX>。
