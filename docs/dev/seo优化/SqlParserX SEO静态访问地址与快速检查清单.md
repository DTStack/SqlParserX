# SqlParserX SEO 静态访问地址与快速检查清单

## 1. 环境

| 环境 | 地址 |
|---|---|
| 本地 | `http://localhost:16758` |
| 服务器 IP | `http://172.16.114.23:16758` |
| 正式域名 | `https://sqlparserx.dtstack.com` |

## 2. 落地服务

全部地址由同一个 `sqlparserx-web` 服务返回，无独立 SEO 服务。

| 项目 | 值 |
|---|---|
| 服务名 | `sqlparserx-web` |
| 启动脚本 | `/opt/tools/sqlparserx/bin/centos-sqlparserx.sh`（`admin` 用户执行） |
| 部署根目录 | `/opt/tools/sqlparserx` |
| 端口 | `16758` |
| 静态资源 | `/opt/tools/sqlparserx/conf/static` |
| 文档站 | `/opt/tools/sqlparserx/website` |
| 公开地址配置 | `conf/application.properties` 的 `seo.site-url` |
| 域名接入 | 外部网关/Nginx 反代到 `172.16.114.23:16758` |

## 3. 当前维护的全部静态地址

### 3.1 可收录页面（进入 sitemap）

| 地址 | 来源 |
|---|---|
| `/` | `conf/static/index.html` |
| `/sql-lineage.html` | `SeoPageController` |
| `/sql-syntax-validator.html` | `SeoPageController` |
| `/sql-table-parser.html` | `SeoPageController` |
| `/create-table-sql-parser.html` | `SeoPageController` |
| `/sql-parameter-parser.html` | `SeoPageController` |
| `/sql-splitter.html` | `SeoPageController` |
| `/website/` | Docusaurus |
| `/website/docs/**` | Docusaurus |
| `/website/docs/**` | Docusaurus 正式版文档（不含 `/website/docs/next/**`） |
| `/website/blog` | Docusaurus 博客首页 |
| `/website/blog/<article>` | Docusaurus 博客正文 |
| `/website/community` | Docusaurus |

### 3.2 交互工具页（不进 sitemap）

| 地址 | 页面 |
|---|---|
| `/app/lineage.html` | SQL 血缘 |
| `/app/syntax-validator.html` | 语法校验 |
| `/app/table-parser.html` | 表解析 |
| `/app/create-table-parser.html` | 建表解析 |
| `/app/parameter-workbench.html` | 参数工作台 |
| `/app/sql-splitter.html` | 脚本切分 |

### 3.3 SEO 基础设施

| 地址 | 来源 |
|---|---|
| `/robots.txt` | `conf/static/robots.txt` |
| `/sitemap.xml` | `conf/static/sitemap.xml`，单一 `urlset`，当前 22 条 |
| `/seo/seo.css` | `conf/static/seo/seo.css` |

### 3.4 重定向

| 请求 | 结果 |
|---|---|
| `/#/` | 前端 `history.replaceState` → `/` |
| `/sql-lineage`、`/sql-lineage/` | 302 → `/sql-lineage.html`（其余 5 个 SEO 页面同规则） |
| `/website` | 302 → `/website/` |

## 4. robots.txt

~~~text
User-agent: *
Allow: /
Disallow: /api/

Sitemap: https://sqlparserx.dtstack.com/sitemap.xml
~~~

## 5. sitemap.xml 规则

| 项 | 要求 |
|---|---|
| 根节点 | `<urlset>`，不是 `<sitemapindex>` |
| 内容 | 首页 + 6 个 SEO 页 + 文档站首页/社区页 + 正式文档 + 博客首页/正文，共 22 条 |
| 必须排除 | `/#/`、`/app/`、`/api/`、`/website/docs/next/`、博客归档/作者/标签聚合页、查询参数、localhost、静态资源、404 页 |
| `<loc>` | 与页面 canonical 完全一致 |
| 已废弃 | `/sitemap-app.xml`（应 404） |

## 6. 快速检查

~~~bash
SITE_URL=https://sqlparserx.dtstack.com

for p in / /sql-lineage.html /sql-syntax-validator.html /sql-table-parser.html \
  /create-table-sql-parser.html /sql-parameter-parser.html /sql-splitter.html \
  /app/lineage.html /app/syntax-validator.html /app/table-parser.html \
  /app/create-table-parser.html /app/parameter-workbench.html /app/sql-splitter.html \
  /robots.txt /sitemap.xml /website/; do
  curl -sS -o /dev/null -w "$p %{http_code}\n" "$SITE_URL$p"
done

curl -sS "$SITE_URL/sitemap.xml" -o /tmp/sitemap.xml
xmllint --noout /tmp/sitemap.xml
rg -c '<url>' /tmp/sitemap.xml
rg -n '<sitemapindex|sitemap-app\.xml|__SEO_SITE_URL__|localhost|/#/|/app/|/api/|/website/docs/next/|/website/blog/(archive|authors|tags)(/|<)|<loc>.*\?' /tmp/sitemap.xml
curl -sS "$SITE_URL/sql-lineage.html" | rg '<title>|canonical|og:url|<h1>|/app/lineage.html'
~~~

预期：全部 `200`；`xmllint` 无输出；URL 数 `22`；排除项检查无匹配。

## 7. 构建与发布

~~~bash
SEO_SITE_URL_OVERRIDE=https://sqlparserx.dtstack.com ./bin/quick/build.sh -f
./bin/quick/build.sh -wf
./bin/quick/push.sh -f     # 仅静态
./bin/quick/push.sh        # 前后端
~~~

构建后：

~~~bash
test ! -e conf/static/sitemap-app.xml
xmllint --noout conf/static/sitemap.xml
rg -c '<url>' conf/static/sitemap.xml
rg -n '__SEO_SITE_URL__|http://localhost|<sitemapindex' conf/static
~~~

## 8. 提交搜索引擎前

| 检查项 | 标准 |
|---|---|
| 域名 HTTPS | 正常 |
| `/` | 200，不跳 `/#/` |
| 6 个 SEO 页 | 200，canonical 为正式域名 |
| robots.txt | 200，Sitemap 地址正确 |
| sitemap.xml | 200，XML 有效，22 条，且每个 `<loc>` 均为真实可访问的正式内容页 |
| 关闭 JS | 仍有标题、正文、链接 |
| 站长平台 | 只提交 `/sitemap.xml` |

## 9. 常见问题

| 问题 | 检查 |
|---|---|
| 首页仍是 `/#/` | 新 `index.html` 是否加载、浏览器缓存 |
| `/app/*.html` 404 | `MyWebMvcConfig` forward、后端是否重启 |
| SEO `.html` 404 | `SeoPageController`、模板是否进 jar |
| canonical 是 localhost | `seo.site-url`、部署 `SEO_SITE_URL` |
| 页面无样式 | `/seo/seo.css` 是否发布 |
| sitemap 仍是 Index | 重新执行 `build.sh -f` 并发布 `conf/static` |
| sitemap 缺文档页 | `website/build/sitemap.xml` 与合并脚本 |
| IP 正常域名 404 | DNS、证书、网关 Host/upstream |
