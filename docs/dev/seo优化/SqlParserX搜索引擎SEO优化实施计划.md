# SqlParserX 搜索引擎 SEO 优化实施计划

> 文档类型：产品与工程化调研方案
>
> 版本：V1.0（初版）
>
> 编制日期：2026-10-27
>
> 适用范围：sqlparserx-ui 在线工具站、sqlparserx-web 服务端、website Docusaurus 文档站
>
> 参考资料：搜索引擎SEO优化调研学习02、第一阶段核心改造、服务端直出与站点级文件（BlogLoom 落地篇）

---
## 1. 结论摘要

SqlParserX 可以做 SEO 优化，而且当前最值得做的不是把 SQL 解析结果“伪装成文章”，而是把站点拆成三类页面，分别采用适合的渲染方式：

| 页面类型 | 代表 URL | 页面内容 | 推荐渲染方式 | 是否进入 Sitemap |
|---|---|---|---|---|
| 官网/能力落地页 | /、/sql-lineage.html、/sql-syntax-validator.html | 固定产品介绍、能力说明、示例、FAQ、入口链接 | 静态 HTML 或服务端模板直出 | 是 |
| 在线工具运行页 | /app/lineage.html、/app/syntax-validator.html 等 | 用户输入 SQL 后产生的交互结果 | 保留 Vue SPA | 默认不把运行状态放入 Sitemap |
| 文档与博客页 | /website/docs/**、/website/blog/** | Docusaurus 生成的静态文档和博客 | Docusaurus 静态生成 | 是 |

当前站点的主要 SEO 问题是：

1. sqlparserx-ui 是 Vue 2 SPA，index.html 初始只有空的 div，首页正文在 JavaScript 执行后才出现。
2. Vue Router 已切换为 history 模式，工具页面统一使用 `/app/*.html`；这些运行态页面不作为搜索结果落地页。
3. sqlparserx-web 当前主要提供 /api/** REST 接口，未提供 SEO 页面 Controller、模板目录、robots.txt 或 sitemap.xml。
4. 生产环境的前端静态资源实际部署到外部目录 conf/static/，不是直接从 sqlparserx-web/src/main/resources/static/ 读取。
5. website 已经是 Docusaurus 静态站，具有较好的内容页基础，但当前配置中的 url、baseUrl 和线上域名还需要统一，并需要补齐站点地图配置。

因此，不能简单理解为“把几个 Vue 模板复制到 sqlparserx-web/src/main/resources 就完成 SEO”。更准确的方案是：

> **把可搜索的产品内容做成独立的静态/服务端 HTML 页面，把需要输入 SQL 的交互过程保留为 SPA；再由后端或构建流程统一输出 robots、Sitemap、RSS、canonical、Open Graph 和结构化数据。**

---

## 2. 目标与非目标

### 2.1 目标

第一阶段目标是让搜索引擎能够发现、抓取并理解 SqlParserX 的产品能力，而不是直接对用户临时输入的 SQL 结果做索引。

具体目标：

1. 首页和六个核心解析能力页面拥有独立、稳定、可分享的 URL。
2. 首屏 HTML 源码中直接包含标题、摘要、H1、产品说明、能力关键词和内部链接。
3. 为每个可索引页面输出唯一的 title、description、canonical、Open Graph 和 JSON-LD。
4. 提供根站点 robots.txt、sitemap.xml，并纳入 /website/ 文档站的收录入口。
5. 不把 /api/**、带 SQL 参数的临时结果、重复的 hash 状态和构建资源作为主要索引对象。
6. 保持当前在线解析工具可用，不要求把用户输入的 SQL 提前放进服务端模板。
7. 建立可以通过 curl、XML 校验、源码检查和站长平台验证的验收流程。

### 2.2 非目标

以下事项不纳入第一阶段必须交付范围：

- 不迁移 Vue 2 到 Nuxt、Next 或其它完整 SSR 框架。
- 不把所有 SQL 解析结果持久化成内容页。
- 不通过 User-Agent 给爬虫和用户返回两套不同内容，避免动态渲染造成 cloaking 风险。
- 不为了 SEO 虚构解析器支持能力、数据源数量、性能指标或用户评价。
- 不在第一阶段引入广告、复杂会员体系和大规模内容运营后台。

---

## 3. 当前工程现状评估

### 3.1 当前站点组成

| 模块 | 当前职责 | 当前 SEO 现状 |
|---|---|---|
| sqlparserx-ui | Vue 2 + Vite 在线工具 | CSR，首页和工具页正文主要由 Vue 渲染；index.html 只有基础 title |
| sqlparserx-web | Spring Boot REST 服务，同时托管 UI 静态资源 | 只有 API Controller 和静态资源转发，没有 SEO 页面层 |
| conf/static | 生产环境 UI 构建产物 | 当前包含 index.html、JS、CSS、Logo 和字体，没有 robots/Sitemap |
| website | Docusaurus 文档、博客和社区页面 | 已经是静态 HTML，文档有 front matter 的 title/description；站点地图需专项配置 |
| bin/quick / Dockerfile | 构建和部署 | UI 复制到 conf/static，website 复制到服务根目录 website |

### 3.2 当前前端路由与 SEO 价值

当前 sqlparserx-ui/src/router/index.js 中有以下页面：

| 当前路由 | 页面组件 | SEO 价值 | 建议公开 SEO URL |
|---|---|---|---|
| / | Home.vue | 最高，作为产品首页 | / |
| /lineage | LineageVisualizer.vue | 高，字段/表血缘搜索意图明确 | /sql-lineage.html |
| /sql-splitter.html | SQLSplitterTester.vue | 中高，SQL 切分工具意图明确 | /sql-splitter.html |
| /syntax-validator | SyntaxValidator.vue | 高，在线 SQL 语法校验意图明确 | /sql-syntax-validator.html |
| /table-parser | TableParser.vue | 高，SQL 表解析意图明确 | /sql-table-parser.html |
| /create-table-parser | CreateTableParser.vue | 高，CREATE TABLE 解析意图明确 | /create-table-sql-parser.html |
| /parameter-workbench | ParameterWorkbench.vue | 高，SQL 参数提取/预编译意图明确 | /sql-parameter-parser.html |

建议将“产品介绍页”和“工具运行页”分开理解：

- /sql-lineage.html 等 SEO URL 先输出可阅读的能力介绍、示例、FAQ 和工具入口。
- 用户点击“开始解析”后进入当前 SPA 工具运行态，例如 /app/lineage.html，或后续迁移到 history 路由下的工具页。
- 用户输入的 SQL、解析结果、错误堆栈和临时参数不进入 Sitemap，也不作为可索引内容。

### 3.3 当前服务端静态资源路径

当前 SqlParserXWebApplication 启动时使用外部静态资源目录：

~~~text
file:<deploy-root>/conf/static/
~~~

MyWebMvcConfig 还将以下路径映射为：

~~~text
/                  -> forward:/index.html
/website/**        -> 部署根目录下的 website/**
~~~

这意味着当前生产环境优先使用：

~~~text
/opt/tools/sqlparserx/conf/static/index.html
/opt/tools/sqlparserx/conf/static/assets/...
/opt/tools/sqlparserx/website/...
~~~

而不是直接使用：

~~~text
sqlparserx-web/src/main/resources/static/index.html
~~~

因此，若把 SEO 文件只放进 src/main/resources/static/，在当前 file 静态资源配置下不一定能被访问。需要二选一：

1. **推荐构建产物路线：**在前端构建或部署阶段把生成的 SEO 文件复制到 conf/static/。
2. **服务端资源路线：**把静态资源位置改为同时包含外部目录和 classpath，例如 file:<deploy-root>/conf/static/,classpath:/static/，再将固定 SEO 文件放入 sqlparserx-web/src/main/resources/static/。

若页面需要读取配置、拼接绝对 URL 或未来接入数据库内容，则应使用：

~~~text
sqlparserx-web/src/main/resources/templates/seo/*.html
~~~

并由 Controller 返回 Thymeleaf 视图，而不是把动态内容硬编码在静态文件中。

---

## 4. SEO 页面与索引策略

### 4.1 应该被索引的内容页

这些页面必须具有稳定 URL、服务端可读取正文、唯一 meta 和清晰的内部链接：

- /：SqlParserX 产品主页。
- /sql-lineage.html：字段级/表级 SQL 血缘解析介绍页。
- /sql-syntax-validator.html：多方言 SQL 语法校验介绍页。
- /sql-table-parser.html：真实表、CTE、JOIN、子查询解析介绍页。
- /create-table-sql-parser.html：CREATE TABLE、分区、分桶、键和 properties 解析介绍页。
- /sql-parameter-parser.html：SQL 参数提取与预编译介绍页。
- /sql-splitter.html：多语句 SQL 切分介绍页。
- /website/、/website/docs/**、/website/blog/**：文档和博客内容页。

### 4.2 可以访问但不应作为主要收录对象的页面

- /app/lineage.html 等 SPA 运行态页面。
- 带 ?sql=、?query=、?datasourceType= 等用户输入参数的 URL。
- API 接口返回的 JSON 地址，例如 /api/lineage/query。
- 解析失败详情、临时结果、前端组件状态和全屏模式 URL。

处理原则：

- Sitemap 中只放公开内容页。
- API 路径在 robots.txt 中降低抓取优先级，必要时返回 X-Robots-Tag: noindex, nofollow。
- 对带用户输入的 GET 页面，如果未来开放，应使用 canonical 指向不带参数的能力页，或者直接 noindex。
- 不依赖 robots.txt 保护敏感数据；当前 API 不应把敏感 SQL 持久化到公开 URL。

### 4.3 每个 SEO 能力页的内容模板

每个能力页至少包含以下服务端可见内容：

~~~text
<title>唯一页面标题 - SqlParserX</title>
<meta name="description" ...>
<link rel="canonical" ...>

<header>面包屑、品牌、返回文档或工具入口</header>
<main>
  <h1>页面唯一主标题</h1>
  <p>一句话说明解决的问题</p>
  <a href="...">立即在线体验</a>

  <h2>能力说明</h2>
  <p>输入、输出、适用场景和限制</p>

  <h2>支持的数据源或 SQL 场景</h2>
  <ul>...</ul>

  <h2>示例</h2>
  <pre><code>...</code></pre>

  <h2>常见问题</h2>
  <h3>...</h3>
  <p>...</p>

  <h2>相关文档</h2>
  <a href="/website/docs/...">...</a>
</main>
<footer>GitHub、文档、其它工具入口</footer>
~~~

重要约束：

- 每页只保留一个 h1。
- 内容页必须使用普通 a href 形成 HTML 内链，不能只依赖点击事件或 router.push。
- FAQ 内容必须在页面中真实可见，不能只写在 JSON-LD 中。
- 示例 SQL 只作为静态说明，不应在服务端执行。
- 支持方言、能力数量和功能描述从现有支持矩阵与代码事实中维护，避免文案与实际能力不一致。

### 4.4 推荐的标题和摘要

| 页面 | 推荐 title | 推荐 description 方向 |
|---|---|---|
| 首页 | SqlParserX：多方言 SQL 解析引擎与在线工具 | 介绍 Java 多方言 SQL 解析、表解析、字段/表血缘、建表解析、参数预编译和在线工具 |
| SQL 血缘 | SQL 字段血缘与表血缘解析工具 - SqlParserX | 说明从 SQL 推导来源表、来源字段和目标字段，并提供在线可视化体验 |
| 语法校验 | 在线 SQL 语法校验工具，支持多种数据库方言 - SqlParserX | 说明输入 SQL 和数据源方言后检查语法并展示错误信息 |
| 表解析 | SQL 表解析工具：识别真实表、CTE 与 JOIN - SqlParserX | 说明从 SQL 中提取真实表并处理 CTE、JOIN、子查询等场景 |
| 建表解析 | CREATE TABLE 建表 SQL 解析工具 - SqlParserX | 说明提取表名、字段、分区、分桶、键和表属性 |
| 参数解析 | SQL 参数提取与预编译工具 - SqlParserX | 说明提取输入参数、输出字段并生成预编译 SQL 与绑定值 |
| SQL 切分 | SQL 多语句切分工具 - SqlParserX | 说明按数据库方言将脚本拆分为独立 SQL 语句 |

标题和摘要只是初始版本。上线前应结合 Search Console 的真实搜索词、点击率和收录页表现迭代，不能只追求堆叠关键词。

---

## 5. 渲染方案：静态页、服务端模板与 SPA 的边界

### 5.1 推荐的混合架构

~~~text
浏览器 / 搜索引擎
        │
        ├── GET /                         -> SEO 首页 HTML
        ├── GET /sql-lineage.html              -> 血缘能力 HTML
        ├── GET /sql-syntax-validator.html     -> 语法校验能力 HTML
        ├── GET /sitemap.xml              -> 主站 Sitemap 或 Sitemap Index
        ├── GET /robots.txt               -> 抓取规则
        ├── GET /website/**               -> Docusaurus 静态文档
        │
        └── 用户点击体验工具
                    ↓
             Vue SPA 运行态
             /app/lineage.html、/app/table-parser.html...
                    ↓
             /api/** REST 接口
~~~

该方案有三个优点：

1. 搜索引擎在第一次请求时就能读取产品介绍和链接。
2. SQL 输入框、ECharts 血缘图、Element UI 交互不需要迁移到服务端渲染。
3. 页面内容、工具逻辑和 API 职责清晰，不把用户私有 SQL 混入 SEO 数据。

### 5.2 方案 A：静态 SEO 页面（第一阶段推荐）

适用于当前静态的产品说明页和工具落地页。

推荐源码目录：

~~~text
sqlparserx-web/src/main/resources/static/seo/
├── home.html
├── sql-lineage.html
├── sql-syntax-validator.html
├── sql-table-parser.html
├── create-table-sql-parser.html
├── sql-parameter-parser.html
└── sql-splitter.html
~~~

或将这些文件作为前端构建阶段的产物复制到：

~~~text
conf/static/seo/
~~~

路由通过轻量 SeoPageController 将公开 `.html` URL 映射到对应模板。页面中的“立即体验”链接指向 History 模式工具路径：

~~~html
<a href="/app/lineage.html">立即体验字段血缘解析</a>
~~~

后续如果启用 history 路由，再将链接迁移为 /sql-lineage.html/tool 或 /app/lineage。

适合静态化的内容：标题、产品说明、静态 SQL 示例、支持方言、FAQ、文档入口、GitHub 入口。

不适合静态化的内容：依赖请求参数实时计算的解析结果、用户输入的 SQL、实时接口错误信息。

### 5.3 方案 B：Thymeleaf 服务端模板

适用于需要由服务端读取环境配置、生成绝对 URL 或未来接入数据库内容的页面。

推荐目录：

~~~text
sqlparserx-web/src/main/resources/templates/seo/
├── layout.html
├── home.html
├── tool-page.html
└── fragments/
    ├── seo-head.html
    ├── header.html
    └── footer.html
~~~

需要在 sqlparserx-web/pom.xml 中增加 spring-boot-starter-thymeleaf，并新增：

~~~java
@Controller
public class SeoPageController {

    @GetMapping("/sql-lineage.html")
    public String lineage(Model model) {
        model.addAttribute("seo", seoMetaService.lineagePage());
        return "seo/tool-page";
    }
}
~~~

当前项目没有 BlogLoom 那样的文章数据库、文章详情服务和站点配置表，因此不应该直接复制 BlogPosting 文章模板。SqlParserX 更适合使用 WebApplication、SoftwareApplication、WebSite 和 BreadcrumbList 结构化数据。

### 5.4 resources 目录的正确理解

针对“是不是只能静态化模板页面到 sqlparserx-web/src/main/resources”这个问题，结论如下：

| 需求 | 推荐位置 | 说明 |
|---|---|---|
| robots、静态 Sitemap、共享 CSS | src/main/resources/static/ 或构建到 conf/static/ | 构建期替换公开地址 |
| 带站点域名、环境配置的 SEO HTML | src/main/resources/templates/seo/ | `SeoPageController` 读取模板并在运行时替换公开地址 |
| Vite 构建后的 SPA | sqlparserx-ui/dist → conf/static/ | 由当前部署脚本和 Dockerfile 负责 |
| Docusaurus 文档 | website/build → 部署根目录 website/ | 由 Docusaurus 负责静态生成 |
| Sitemap 文件 | 构建脚本生成并部署到 conf/static/ | 当前无数据库，第一阶段直接使用静态 XML |

所以 resources 是 SEO 模板和站点级文件的源码位置，不是当前运行时唯一的静态资源位置。当前阶段的 robots.txt、sitemap.xml、sitemap-app.xml 仍作为静态文件维护并复制到 conf/static/，不新增数据库型 SeoFileController；6 个能力页由轻量级 SeoPageController 服务端直出，以便运行时读取环境配置。

---

## 6. 页面级 SEO 技术规范

### 6.1 公共 head 最小规范

所有可索引产品页至少输出：

~~~html
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SQL 字段血缘与表血缘解析工具 - SqlParserX</title>
  <meta name="description" content="使用 SqlParserX 从 SQL 中解析字段级和表级血缘关系，支持多种数据库方言并提供在线可视化工具。">
  <link rel="canonical" href="https://sqlparserx.dtstack.com/sql-lineage.html">

  <meta property="og:type" content="website">
  <meta property="og:title" content="SQL 字段血缘与表血缘解析工具 - SqlParserX">
  <meta property="og:description" content="从 SQL 中解析字段级和表级血缘关系。">
  <meta property="og:url" content="https://sqlparserx.dtstack.com/sql-lineage.html">
  <meta property="og:site_name" content="SqlParserX">
  <meta property="og:image" content="https://sqlparserx.dtstack.com/logo.png">

  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:title" content="SQL 字段血缘与表血缘解析工具 - SqlParserX">
  <meta name="twitter:description" content="从 SQL 中解析字段级和表级血缘关系。">
  <meta name="twitter:image" content="https://sqlparserx.dtstack.com/logo.png">

  <link rel="alternate" type="application/rss+xml" title="SqlParserX Blog" href="https://sqlparserx.dtstack.com/website/blog/rss.xml">
</head>
~~~

规则：

- canonical、og:url、Sitemap 中的 URL 必须完全一致，统一使用无 hash、无追踪参数的标准 URL。
- 生产环境域名和可选路径前缀不能硬编码在多个文件中，统一读取 `conf/application.properties` 的 `seo.site-url`。
- 没有合适的公开封面图时可以暂时不输出 og:image，不能输出失效或登录后才能访问的图片。
- title、description、og:title、og:description 应由同一份页面元数据生成，避免多份文案漂移。
- 不为了 SEO 重复堆叠同义词，内容应该首先对开发者有用。

### 6.2 结构化数据

#### 首页：WebSite + Organization + SoftwareApplication

建议首页输出：

~~~json
{
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "WebSite",
      "name": "SqlParserX",
      "url": "https://sqlparserx.dtstack.com/",
      "inLanguage": "zh-CN"
    },
    {
      "@type": "Organization",
      "name": "DTStack",
      "url": "https://github.com/DTStack/SqlParserX",
      "logo": "https://sqlparserx.dtstack.com/logo.png"
    },
    {
      "@type": "SoftwareApplication",
      "name": "SqlParserX 在线 SQL 工具",
      "applicationCategory": "DeveloperApplication",
      "operatingSystem": "Web",
      "url": "https://sqlparserx.dtstack.com/",
      "description": "多方言 SQL 解析引擎与在线工具集"
    }
  ]
}
~~~

#### 工具落地页：SoftwareApplication + BreadcrumbList

工具页不应伪装成文章，建议使用：

- SoftwareApplication：描述在线工具名称、用途、系统平台和入口。
- BreadcrumbList：描述首页 → SQL 工具 → 当前工具的层级。
- FAQPage：只有在页面真实展示 FAQ 问答时使用。

不建议当前阶段使用：

- Review、AggregateRating：没有真实评价数据时不要虚构。
- BlogPosting：工具介绍页不是文章，除非未来新增真正的产品文章。
- HowTo：只有页面确实是步骤教程并且内容完整时使用。

如果未来确认在线工具确实免费且需要表达价格，再补充 Offer；当前不在结构化数据中写死价格，避免产品策略变化后产生错误信息。

### 6.3 图片、链接和可访问性

- Logo、站点封面和能力示意图使用明确的 alt，装饰图使用空 alt。
- 结构化页面的主要标题、正文和链接不依赖 Canvas 或纯 SVG 文本。
- 主要导航使用普通 a href；Vue Router 只负责增强交互。
- 代码示例使用 pre/code，不能只渲染成图片。
- 交互按钮需要有可读文本，图标按钮提供 aria-label。
- 图片设置宽高或占位比例，避免 CLS；正文图使用 loading=lazy。

---

## 7. 站点级文件方案

### 7.1 robots.txt

建议第一版内容：

~~~text
User-agent: *
Allow: /
Disallow: /api/

Sitemap: https://sqlparserx.dtstack.com/sitemap.xml
~~~

注意事项：

- 不要 Disallow: /，否则首页和产品页无法抓取。
- 不要禁止 /website/，文档和博客是重要的内容来源。
- 不要把所有 /sql-* 工具页禁止抓取；应该让搜索引擎读取介绍页。
- /api/ 是 JSON 接口，不是内容页，可以降低无意义抓取。
- robots.txt 不是权限控制。接口仍需要做好请求限制、异常信息脱敏和必要的访问控制。
- 不要盲目禁止 /assets/、/static/。如果 CSS、JS 或字体位于这些目录，禁止抓取可能影响搜索引擎渲染判断。

### 7.2 Sitemap 组织方式

SqlParserX 当前页面规模较小，采用单一 Sitemap，不使用 Sitemap Index：

~~~text
/robots.txt
    └── Sitemap: https://sqlparserx.dtstack.com/sitemap.xml

/sitemap.xml
    └── 主站首页、六个 SEO 页面、Docusaurus 文档和博客页面
~~~

构建流程读取主站固定页面清单和 Docusaurus 生成的 `website/build/sitemap.xml`，使用 `merge-sitemaps.mjs` 合并并去重后输出唯一的 `/sitemap.xml`。

#### 7.2.1 /sitemap.xml：唯一公开 Sitemap

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>https://sqlparserx.dtstack.com/</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-lineage.html</loc></url>
  <!-- 其余 SEO 页面以及 Docusaurus 文档、博客 URL -->
</urlset>
~~~

根节点固定为 `urlset`。robots 和站长平台只声明、提交 `/sitemap.xml`，不再公开 `/sitemap-app.xml`。

#### 7.2.2 主站页面清单

第一版建议包含：

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>https://sqlparserx.dtstack.com/</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-lineage.html</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-syntax-validator.html</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-table-parser.html</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/create-table-sql-parser.html</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-parameter-parser.html</loc></url>
  <url><loc>https://sqlparserx.dtstack.com/sql-splitter.html</loc></url>
</urlset>
~~~

文档和博客 URL 由 Docusaurus 自动生成，再与上述主站 URL 合并到同一个文件。

#### 7.2.3 Sitemap 中必须排除的 URL

- /app/lineage.html、/app/table-parser.html 等 History 运行态 URL。
- /api/** 接口。
- 带 SQL、错误堆栈、临时参数的 URL。
- 构建后的 /assets/*.js、/assets/*.css、字体和图片文件。
- 404、重定向、登录页、内部管理页和调试页。
- 同一内容的带参数副本，例如 ?utm_source=github。

#### 7.2.4 lastmod 策略

主站产品页是代码和文案页面，lastmod 可以使用最近一次发布版本时间，但不能每次请求动态伪造。文档页使用 Docusaurus 的构建能力或 Git 文件修改时间；如果无法获得可靠时间，可以省略 lastmod，不要填一个不真实的值。

Sitemap 不需要使用 changefreq 和 priority。这两个字段不能替代高质量内容、内部链接和稳定的抓取入口。

### 7.3 Docusaurus 文档 Sitemap

当前 website/docusaurus.config.js：

- url 固定为 https://dtstack.github.io；
- baseUrl 默认是 /SqlParserX/，Docker 构建时通过 DOCUSAURUS_BASE_URL=/website/ 覆盖；
- 已配置博客 RSS/Atom feed；
- 尚未显式配置 sitemap 插件。

需要统一成部署域名可配置：

~~~js
url: process.env.DOCUSAURUS_URL || 'https://sqlparserx.dtstack.com',
baseUrl: process.env.DOCUSAURUS_BASE_URL || '/website/',
~~~

然后接入 Docusaurus sitemap 生成能力，确保产物位于：

~~~text
website/build/sitemap.xml
~~~

运行时对外地址为：

~~~text
https://sqlparserx.dtstack.com/website/sitemap.xml
~~~

Docker 和本地部署必须传入同一套公开站点域名，否则文档 Sitemap 可能继续生成 dtstack.github.io 的 URL。

### 7.4 RSS/Atom

SqlParserX 不是以用户文章为主的博客，因此 RSS 不应成为第一阶段阻塞项。推荐顺序：

1. 先启用 Docusaurus 已有的博客 RSS/Atom feed。
2. 若希望根域名统一发现，可以在主站 head 中添加指向 /website/blog/rss.xml 的 alternate。
3. 只有未来主站有产品更新、版本发布或技术文章内容时，才考虑增加根路径 /rss.xml，不要为了 SEO 生成没有内容价值的空 RSS。

---

## 8. Sitemap 生成与部署设计

### 8.1 第一阶段唯一推荐方案：生成并部署静态站点文件

由于当前 SqlParserX 的 SEO 页面是固定产品页面，不依赖数据库文章数据，第一阶段无需引入数据库查询型 Sitemap Controller。建议在构建阶段生成：

~~~text
conf/static/robots.txt
conf/static/sitemap.xml
conf/static/sitemap-app.xml
~~~

生成输入使用一个受版本控制的页面清单，例如：

~~~text
seo/public-routes.json
~~~

示例：

~~~json
[
  {"path": "/", "indexable": true},
  {"path": "/sql-lineage.html", "indexable": true},
  {"path": "/sql-syntax-validator.html", "indexable": true},
  {"path": "/sql-table-parser.html", "indexable": true},
  {"path": "/create-table-sql-parser.html", "indexable": true},
  {"path": "/sql-parameter-parser.html", "indexable": true},
  {"path": "/sql-splitter.html", "indexable": true}
]
~~~

构建脚本读取 `conf/application.properties` 的 `seo.site-url`，输出绝对 URL：

~~~text
seo.site-url=https://sqlparserx.dtstack.com
~~~

该值表示站点的完整公开根地址，也可以包含反向代理路径前缀，例如 `https://example.com/sqlparserx`。末尾不写 `/`。构建流程使用 `__SEO_SITE_URL__` 占位符统一渲染 robots、Sitemap、首页 meta、静态落地页 canonical、资源地址、导航地址和 SPA 工具入口，因此后续切换域名或部署前缀时只修改这一项。

环境约定：仓库中的本地默认值为 `http://localhost:16758`；服务器 `/opt/tools/sqlparserx/conf/application.properties` 使用 `https://sqlparserx.dtstack.com`。部署脚本通过私有 `deploy.conf` 的 `SEO_SITE_URL` 更新服务器配置，避免把线上域名硬编码为本地启动默认值。

若未配置生产域名，构建可以失败或明确生成开发环境标记，不能默默把 localhost 写入正式 Sitemap。

当前阶段不增加数据库查询型 SeoFileController。SqlParserX 暂无文章数据库、发布状态、后台 SEO 配置或动态页面集合；仅增加 SeoPageController 读取固定模板，并通过 EnvironmentContext 注入公开站点地址。

静态文件的推荐来源为：

1. 页面清单和域名配置；
2. 构建脚本生成 XML 和 robots.txt；
3. 复制到 conf/static/；
4. 由现有 Spring Boot 静态资源机制直接返回。

若页面数量很少，也可以直接人工维护 XML 文件，但每次新增、删除或改名页面必须同步更新页面清单和 Sitemap。

### 8.2 后续阶段再考虑：运行时 Controller 生成 Sitemap

只有出现以下需求时，才考虑增加 SeoFileController：

- 页面列表来自数据库或后台配置。
- SEO 域名可以在线修改。
- 版本、案例或产品页面频繁增删。
- 需要根据发布状态过滤内容。

当前阶段不实现下面的 Controller；它仅作为后续动态化路线的设计参考：

~~~java
@RestController
public class SeoFileController {

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public String robots() { ... }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public String sitemapIndex() { ... }

    @GetMapping(value = "/sitemap-app.xml", produces = "application/xml;charset=UTF-8")
    public String appSitemap() { ... }
}
~~~

后续真正采用 Controller 时需要注意：

- 使用 XML 序列化工具或安全的 XML Builder，不手工拼接未转义的 URL。
- URL 统一从 SeoUrlResolver 获取，不在 Controller 中散落域名字符串。
- 对 Sitemap 做缓存，避免每个爬虫请求都重复生成。
- Controller 端点必须先于 SPA fallback 命中；带 .xml 的路径也不能被 SPA fallback 覆盖。
- 如果返回 500，不要返回 200 的空 Sitemap；让监控能发现错误。

### 8.3 构建和部署流程

建议把 SEO 文件纳入现有 bin/quick/build.sh 和 Docker 流程：

~~~text
1. 构建 sqlparserx-ui
2. 生成主站静态 SEO 页面和 sitemap-app.xml
3. 构建 website，并生成 website/build/sitemap.xml
4. 生成 robots.txt 或 sitemap index
5. 将 UI、SEO 文件同步到 conf/static
6. 将 website/build 同步到服务根目录 website
7. 启动 sqlparserx-web
8. 用 curl 检查首页、Sitemap、robots 和文档站
~~~

Docker 中需要保持：

- VITE_API_URL=/，让交互工具继续同源调用 API。
- DOCUSAURUS_URL 使用正式公开域名。
- DOCUSAURUS_BASE_URL=/website/，与 Spring Boot 的 /website/** 映射一致。
- `seo.site-url` 是主站、canonical、静态页面链接和 Sitemap 的唯一公开地址来源。

---

## 9. 后端路由和资源改造清单

### 9.1 第一阶段必须修改的文件

| 文件 | 改造内容 | 优先级 |
|---|---|---|
| sqlparserx-web/.../MyWebMvcConfig.java | 增加 SEO 页面、静态资源和错误路由映射；确认 /website/** 不被 SPA 影响 | P0 |
| sqlparserx-web/.../SqlParserXWebApplication.java | 评估 file 与 classpath 静态资源是否同时启用 | P0 |
| sqlparserx-ui/index.html | 补充基础 meta、Open Graph、无 JS fallback 或首页静态说明 | P0 |
| sqlparserx-ui/src/router/index.js | 启用 History 模式，工具页统一使用 `/app/*.html` | P0 |
| website/docusaurus.config.js | 公开域名和 baseUrl 参数化；启用文档 Sitemap | P0 |
| Dockerfile | 从 `conf/application.properties` 读取 `seo.site-url`，传递 DOCUSAURUS_URL 并复制 SEO 产物 | P0 |
| bin/quick/build.sh | 本地构建时生成/复制 SEO 文件和 Sitemap | P0 |
| bin/quick/push.sh | 部署时同步新的 SEO 文件 | P0 |

### 9.2 使用 Thymeleaf 时新增的文件

| 文件 | 职责 | 优先级 |
|---|---|---|
| sqlparserx-web/pom.xml | 增加 Thymeleaf 依赖 | P1 |
| sqlparserx-web/.../controller/SeoPageController.java | 服务端直出六个固定 SEO 模板页面 | P0 |
| sqlparserx-web/.../env/EnvironmentContext.java | 从 application.properties 读取并规范化公开地址 | P0 |
| sqlparserx-web/.../seo/SeoFileController.java | 后续动态内容阶段的 robots/Sitemap/RSS；第一阶段不新增 | P2 |
| sqlparserx-web/.../seo/SeoMetaService.java | 统一生成 title、description、canonical、JSON-LD | P1 |
| sqlparserx-web/src/main/resources/templates/seo/layout.html | 公共页面骨架 | P1 |
| sqlparserx-web/src/main/resources/templates/seo/tool-page.html | 工具能力落地页 | P1 |
| sqlparserx-web/src/main/resources/templates/seo/fragments/seo-head.html | 公共 head 元信息 | P1 |

### 9.3 不建议第一阶段直接改动的部分

- 不要将 Home.vue 的全部交互逻辑搬进 Java 模板。
- 不要把 ECharts 血缘图服务端渲染成图片作为主要内容。
- 不要让后端执行用户从网页输入的 SQL 来生成 SEO 页面。
- 不要为了让 /lineage 可抓取而把用户的 SQL、解析结果放进 query string。
- 不要复制一套与 website 文档内容不一致的产品介绍；应通过内部链接复用已有文档。

---

## 10. 推荐的分阶段实施计划

### M0：基线与 URL 决策

目标：固定公开域名、页面 URL、渲染边界和 Sitemap 归属。

交付物：

- `seo.site-url` / DOCUSAURUS_URL 的部署约定。
- 公共页面清单和 route manifest。
- 首页及六个工具页的 title、description、H1、canonical 初稿。
- 确认旧 `/#/xxx` 仅用于兼容迁移，不进入 Sitemap。

### M1：首页和站点级文件

目标：先让主站能够被抓取和理解。

交付物：

- 首页首屏可见的静态 SEO 内容。
- robots.txt。
- /sitemap.xml 或 /sitemap-app.xml。
- 首页 canonical、OG、WebSite、SoftwareApplication JSON-LD。
- curl 验收脚本。

验收：

~~~bash
curl -L -sS -H 'Accept: text/html' https://sqlparserx.dtstack.com/ \
  | rg '<h1>|<title>|description|canonical|application/ld\+json'

curl -L -sS https://sqlparserx.dtstack.com/robots.txt
curl -L -sS https://sqlparserx.dtstack.com/sitemap.xml
~~~

### M2：六个工具能力落地页

目标：覆盖核心搜索意图，同时保留现有在线工具。

交付物：

- 六个静态/服务端 SEO 页面。
- 每页至少一个真实 SQL 示例、能力边界、支持方言和 FAQ。
- 页面到 `/app/*.html` SPA 工具的明确 CTA。
- 页面到对应 website/docs 文档的内部链接。
- 每页 SoftwareApplication 和 BreadcrumbList JSON-LD。

### M3：文档站 SEO 与 Sitemap Index

目标：把官网、文档和博客统一到同一公开域名体系。

交付物：

- Docusaurus url、baseUrl 参数化。
- website/sitemap.xml。
- 根 /sitemap.xml 作为 Sitemap Index，指向主站和文档站 Sitemap。
- RSS/Atom 自动发现链接。
- 文档页的 canonical 与实际访问 URL 一致。

### M4：路由和资源工程化

目标：消除静态页面、SPA fallback 和部署目录之间的冲突。

交付物：

- 明确 /、SEO 页面、SPA fallback、/website/** 和 /api/** 的优先级。
- 必要时引入 Thymeleaf 公共模板。
- bin/quick、Docker 和手工部署使用同一份公开域名参数。
- 404、405、500 页面不进入 Sitemap。

### M5：数据监测和持续优化

目标：判断收录和搜索需求，而不是只检查源码。

交付物：

- Google Search Console、Bing Webmaster Tools、百度搜索资源平台（按实际目标市场选择）。
- Sitemap 提交和抓取错误监控。
- GA4、Umami 或现有统计方案，脚本异步加载。
- 记录 impressions、clicks、CTR、平均排名、已发现未编入索引 URL。

### M6：性能与内容迭代

目标：提高页面体验和长期搜索表现。

交付物：

- 图片尺寸、懒加载、缓存和压缩。
- CSS/JS 不阻塞 SEO 页面正文。
- 为高价值能力补充案例、教程和版本更新内容。
- 根据 Search Console 搜索词扩展页面，而不是无依据地批量生成页面。

---

## 11. 验收测试方案

### 11.1 页面源码验收

每个可索引 URL 都应满足：

~~~bash
URL='https://sqlparserx.dtstack.com/sql-lineage.html'
curl -L -sS -H 'Accept: text/html' "$URL" > /tmp/sqlparserx-seo-page.html

rg -n '<title>|meta name="description"|rel="canonical"|og:title|og:description|og:url|application/ld\+json|<h1>' \
  /tmp/sqlparserx-seo-page.html
~~~

验收条件：

- HTTP 状态为 200。
- 首屏源码包含一个 h1 和正文说明。
- title 唯一且与页面能力对应。
- description 非空。
- canonical 为绝对 URL、无 hash、无追踪参数。
- og:url 与 canonical 相同。
- JSON-LD 可被 JSON 解析器解析。
- 禁用 JavaScript 后，页面仍能阅读并能看到主要链接。

### 11.2 robots 和 Sitemap 验收

~~~bash
curl -i https://sqlparserx.dtstack.com/robots.txt
curl -i https://sqlparserx.dtstack.com/sitemap.xml
curl -i https://sqlparserx.dtstack.com/sitemap-app.xml
curl -i https://sqlparserx.dtstack.com/website/sitemap.xml
~~~

检查：

- robots.txt 返回 200、Content-Type 为 text/plain，包含 Sitemap 地址。
- Sitemap 返回 200、Content-Type 为 application/xml。
- XML 能被 xmllint 或等价解析器解析。
- loc 全部为绝对 URL。
- loc 不含 #，不含 /api/，不含 SQL 输入，不含 localhost。
- Sitemap 中的 URL 都返回 200 或可接受的稳定页面，不含 404/500。
- URL 集合与 canonical 集合一致，不产生不同域名、不同尾斜杠或重复参数。

示例校验：

~~~bash
curl -sS https://sqlparserx.dtstack.com/sitemap-app.xml > /tmp/sqlparserx-sitemap.xml
xmllint --noout /tmp/sqlparserx-sitemap.xml
if rg -n '<loc>.*(#|/api/|\?|localhost)' /tmp/sqlparserx-sitemap.xml; then
  exit 1
fi
~~~

### 11.3 结构化数据验收

- JSON-LD 使用合法 JSON，不手工拼接未转义的文本。
- 首页使用 WebSite、Organization、SoftwareApplication。
- 工具页使用 SoftwareApplication、BreadcrumbList，FAQ 必须与可见页面一致。
- 不出现虚构的 rating、review、用户数量或支持能力。
- 使用 Google Rich Results Test、Schema Markup Validator 或等价工具验证。

### 11.4 SPA 回归验收

SEO 改造不应破坏现有工具：

1. 首页仍能进入各个工具。
2. /app/lineage.html、/app/sql-splitter.html 等现有运行态可以加载。
3. /api/datasource/types、/api/lineage/**、/api/sql/**、/api/parameter/** 不被页面 Controller 截获。
4. /website/、文档静态资源和博客 feed 正常访问。
5. Docker 和 bin/quick 部署结果一致。

---

## 12. 风险与处理原则

| 风险 | 影响 | 处理方案 |
|---|---|---|
| 只改 title，不补首屏正文 | 搜索引擎仍只能看到空壳 | 首页和工具页必须有服务端/静态 HTML 正文 |
| 只把文件放进 src/main/resources/static | 生产环境仍可能访问不到 | 处理 file 静态路径，或复制到 conf/static |
| SPA fallback 抢走 SEO URL | Controller/静态页面永远不命中 | 先定义 SEO 路由优先级，再实现 fallback |
| hash URL 进入 Sitemap | 搜索引擎看到重复或无意义页面 | Sitemap 只放无 hash 的公开页面 |
| Sitemap 写入 localhost | 正式站点的收录地址失效 | 构建时强校验 `seo.site-url` |
| Docusaurus URL 仍是 GitHub Pages 域名 | 文档 canonical 和 Sitemap 指错域名 | DOCUSAURUS_URL、DOCUSAURUS_BASE_URL 环境化 |
| robots 错误禁止 CSS/JS | 搜索引擎无法正确渲染页面 | 只禁止 API 和内部路径，静态资源按实际情况处理 |
| FAQ/JSON-LD 与页面不一致 | 结构化数据失去可信度 | JSON-LD 只能描述页面真实可见内容 |
| 把用户 SQL 写入 URL | 泄露隐私、生成海量重复页面 | 运行态只保留在客户端，不做 URL 索引 |
| 首页静态页与 Vue Home.vue 重复维护 | 文案和布局发生漂移 | 先确定单一内容源，后续再将首页拆成可复用模板/构建产物 |
| 为收录而虚构“支持 15 种方言”等信息 | 用户体验和搜索质量下降 | 从枚举、测试和能力矩阵自动核对文案 |

---

## 13. 首期建议的最小交付清单

如果要控制改造成本，建议首期只做下面 10 项：

1. 在 `conf/application.properties` 中明确正式公开根地址 `seo.site-url`。
2. 将首页静态 SEO 内容补入首屏 HTML，并保留 Vue 交互入口。
3. 新增六个工具能力落地页，至少提供文本说明、示例和 CTA。
4. 为首页和六个工具页生成唯一 title、description、canonical、OG。
5. 首页增加 WebSite、Organization、SoftwareApplication JSON-LD。
6. 工具页增加 SoftwareApplication、BreadcrumbList JSON-LD。
7. 生成 robots.txt，至少禁止 /api/ 的无意义抓取。
8. 生成 /sitemap-app.xml，列出首页和六个工具落地页。
9. 为 Docusaurus 配置真实 url、baseUrl 并生成 /website/sitemap.xml。
10. 根 /sitemap.xml 统一作为 Sitemap Index，并完成 curl/XML/源码验收。

首期完成后，SqlParserX 就具备“可发现、可抓取、可理解、可验证”的 SEO 技术底座。此时再根据实际搜索词决定是否投入更多时间改造 history 路由、增加 FAQ/教程页、接入统计和扩充内容，而不是一开始就迁移整套前端框架。

---

## 14. 后续可选增强

### 14.1 history 路由与工具页统一

如果希望 /sql-lineage.html 页面同时承载 SEO 内容和交互工具，可后续将 Vue Router 改为 history 模式，并设计：

~~~text
/sql-lineage.html             -> 页面介绍 + 工具入口/工具组件
/sql-lineage.html/tool        -> 交互式运行页
/sql-lineage.html?sql=...     -> 不进入索引，必要时 noindex
~~~

这需要后端对未知前端路由提供安全 fallback，但必须保证 /api/**、/website/**、/sitemap*.xml、/robots.txt 和静态资源优先命中。

### 14.2 内容与版本页

可以在 website/blog 中持续发布：

- 多方言 SQL 解析实践。
- 字段血缘、表血缘和 CTE 解析原理。
- Druid 与 SqlParserX 的能力对比。
- SQL 参数预编译和数据平台接入案例。
- 版本更新与支持矩阵。

这些内容天然适合 Docusaurus 静态生成，比把工具运行结果做成动态 SEO 页面更有长期价值。

### 14.3 多语言

website 已有 zh-Hans 和 en 配置。后续应确保：

- 中文和英文页面各自有 canonical。
- hreflang 只指向真实存在的对应语言页面。
- 英文 Sitemap URL 与中文 Sitemap URL 不混淆。
- 不把机器翻译的重复页面批量放入索引。

---

## 15. 最终工程决策

本项目的 SEO 改造采用以下决策：

1. **不把整个 Vue 在线工具改造成 SSR 应用。** 用户输入和解析结果属于交互运行态，继续由 SPA 负责。
2. **把产品能力说明做成可独立抓取的静态/服务端页面。** 这是当前投入产出比最高的改造。
3. **sqlparserx-web/src/main/resources 可以承载模板和固定资源，但不是当前唯一运行时目录。** 生产部署必须同步考虑 conf/static。
4. **第一阶段使用构建期固定 Sitemap，后续内容动态化后再引入 Controller 生成。** 当前没有文章数据库，不需要复制 BlogLoom 的文章 Sitemap 方案。
5. **主站 Sitemap 和 Docusaurus Sitemap 使用 Sitemap Index 统一收口。** 根 robots 只需要声明根 Sitemap。
6. **所有绝对 URL、canonical、OG、Sitemap 和 Docusaurus URL 使用同一套公开域名配置。** 不允许出现 localhost 或 GitHub Pages 域名混入生产产物。
7. **先完成源码级和协议级验收，再提交搜索引擎平台。** 能被抓取不等于一定收录，收录和排名需要后续内容质量与数据复盘。

这套方案与当前 SqlParserX 的 Java 8 + Spring Boot 2.3 + Vue 2 + Vite + Docusaurus 架构兼容，能够以较小增量完成第一阶段 SEO 基础建设，并为后续 SSR、history 路由、内容运营和多语言扩展保留空间。

---

## 16. 前端页面与 SEO 模板的同步策略

### 16.1 推荐结论

sqlparserx-ui 可以作为前端页面的主开发和视觉基准，SEO 页面按照它进行稳定内容和视觉结构的一比一还原。后续每次前端页面优化完成后，再同步更新 SEO 模板、页面清单和 Sitemap。

但这里的一比一只针对“页面表达”，不要求复制 Vue 的全部交互实现：

| 需要保持一致 | 不需要复制 |
|---|---|
| Logo、导航、页脚、颜色、字体和间距 | Vue 组件状态 |
| Hero、能力卡片、示例区、CTA 和 FAQ | SQL 输入框实时状态 |
| 标题、摘要、功能描述和支持范围 | API 请求、加载和错误状态 |
| 静态 SQL 示例和解析结果示意 | ECharts 缩放、拖拽和全屏 |
| 响应式布局和移动端信息层级 | 用户输入 SQL 后的动态解析结果 |

SEO 模板只负责让搜索引擎和分享平台第一次请求时拿到完整 HTML；真正的输入 SQL、调用 API 和展示解析结果继续由 Vue SPA 负责。

### 16.2 当前阶段的单一事实来源

当前不引入数据库，也不引入动态 SEO 后台。建议以文件作为内容事实来源：

~~~text
seo/
├── public-routes.json
└── page-content/
    ├── home.json
    ├── sql-lineage.json
    ├── sql-syntax-validator.json
    ├── sql-table-parser.json
    ├── create-table-sql-parser.json
    ├── sql-parameter-parser.json
    └── sql-splitter.json
~~~

其中：

- public-routes.json 维护可索引 URL，并作为 sitemap-app.xml 的生成输入。
- page-content/*.json 维护 title、description、H1、静态说明、文档链接和 SPA 工具入口。
- SEO 模板读取或参考这些稳定内容。
- Vue 页面后续逐步改为引用同一份内容，减少前端和 SEO 页面文案漂移。
- 用户输入、解析结果和运行时错误不写入这些文件。

第一阶段如果不方便让 Vue 和 SEO 模板共用 JSON，也可以先采用人工同步，但必须把这份清单作为 Pull Request 的检查项。

### 16.3 每次前端变更后的同步流程

~~~text
1. 在 sqlparserx-ui 中完成页面开发和浏览器验证
2. 判断变更是稳定文案/页面结构，还是纯交互运行态
3. 稳定内容变更时，更新 page-content/*.json
4. 按照 sqlparserx-ui 的最新布局同步 SEO 模板
5. 检查 title、description、canonical、OG、JSON-LD 和唯一 h1
6. 检查普通 href 是否能进入对应 SPA 工具和 website 文档
7. 更新 public-routes.json，并重新生成静态 Sitemap
8. 检查 robots.txt 是否仍声明正确的 Sitemap 地址
9. 本地用 curl 检查 SEO 页面源代码
10. 浏览器检查桌面端和移动端视觉一致性
~~~

### 16.4 第一阶段的文件职责

~~~text
sqlparserx-ui/
└── 本地开发、交互页面、SPA 工具运行态

sqlparserx-web/src/main/resources/
├── templates/seo/       六个服务端直出的固定 SEO 页面模板
└── static/seo/          SEO 页面共享 CSS

conf/static/
├── robots.txt           静态抓取规则
├── sitemap.xml          主站与文档合并后的唯一 Sitemap urlset
└── seo/seo.css          SEO 页面共享样式；HTML 不直接暴露在 static 下

website/build/
└── sitemap.xml          Docusaurus 生成的文档 Sitemap
~~~

第一阶段不新增数据库型 SeoFileController，不新增数据库表，不新增 SEO 后台配置。六个固定能力页由 SeoPageController 服务端渲染；robots 和 Sitemap 仍由构建脚本生成或直接维护后部署。

### 16.5 该方案的边界

这种方案适合当前页面数量少、内容固定、没有数据库的 SqlParserX 阶段。它可以保证：

- SEO 页面与在线工具保持统一视觉。
- 搜索引擎获得服务端可读的正文和链接。
- Sitemap 内容透明、可审查、可复现。
- 不为简单站点引入不必要的运行时复杂度。
- 前端可以继续独立开发和迭代。

当未来出现数据库内容、版本页、案例页、动态发布状态或后台 SEO 配置时，再将静态文件升级为 Controller/Service 动态生成；在此之前不提前设计复杂的动态 SEO 系统。

---

## 17. 2026-10-27 第一阶段实际落地记录

本节记录本方案第一次真正落地后的代码结构和验证结论，后续排查问题时应优先对照这里，而不是只看早期方案草稿。

### 17.1 最终采用的渲染边界

~~~text
GET /
  -> MyWebMvcConfig forward:/index.html
  -> conf/static/index.html
  -> 包含可抓取首页正文，随后 Vue 挂载

GET /sql-lineage.html/ 等六个能力 URL
  -> SeoPageController
  -> classpath:templates/seo/*.html
  -> EnvironmentContext.getSeoSiteUrl()
  -> 替换 __SEO_SITE_URL__ 后返回 UTF-8 HTML

GET /robots.txt、/sitemap.xml
  -> conf/static 下的静态站点级文件

GET /website/**
  -> Docusaurus 构建产物
~~~

该边界保留了原有 `/` 首页，并将 Vue 工具入口迁移为 `/app/*.html` History 路由，同时让六个能力页具备独立、稳定、可抓取的 HTML。没有引入数据库、SEO 后台和动态内容表。

### 17.2 已落地的代码与资源

| 文件 | 实际职责 |
|---|---|
| `conf/application.properties` | 本地默认 `seo.site-url=http://localhost:16758` |
| `EnvironmentContext.java` | 读取 `server.port` 与 `seo.site-url`，去掉末尾斜杠并校验协议 |
| `SeoPageController.java` | 映射六个干净 URL，读取固定 HTML 模板并替换公开地址 |
| `src/main/resources/templates/seo/*.html` | 六个能力页的标题、描述、正文、示例、FAQ、canonical 和工具入口 |
| `src/main/resources/static/seo/seo.css` | 六个模板共享的响应式视觉样式 |
| `src/main/resources/static/robots.txt` | 允许公开页面、屏蔽 `/api/`、声明 Sitemap |
| `sitemap.xml` | 收录根首页、六个能力页和 Docusaurus 页面，不收录 History 工具、API、查询参数和资源文件 |
| `merge-sitemaps.mjs` | 合并主站页面与 Docusaurus Sitemap，并按 loc 去重 |
| `sqlparserx-ui/index.html` | 首页 meta、canonical、OG、JSON-LD 和 Vue 挂载前的可抓取正文 |
| `website/docusaurus.config.js` | 可配置公开域名，并由 Docusaurus 生成文档 Sitemap |
| `bin/quick/site/render-seo-files.sh` | 构建期替换静态产物中的 `__SEO_SITE_URL__` |
| `bin/quick/build.sh` | 构建 UI、叠加站点级静态文件、渲染公开地址并构建 website |
| `bin/quick/push.sh` | 部署时更新服务器 `seo.site-url`，再推送后端和静态产物 |

### 17.3 当前公开 URL 与运行态入口

| SEO URL | 对应 Vue 工具入口 |
|---|---|
| `/sql-lineage.html/` | `/app/lineage.html` |
| `/sql-syntax-validator.html/` | `/app/syntax-validator.html` |
| `/sql-table-parser.html/` | `/app/table-parser.html` |
| `/create-table-sql-parser.html/` | `/app/create-table-parser.html` |
| `/sql-parameter-parser.html/` | `/app/parameter-workbench.html` |
| `/sql-splitter.html/` | `/app/sql-splitter.html` |

SEO URL 用于产品能力说明和搜索收录，`/app/*.html` 用于运行交互工具；两者职责不同，不应互相替代。旧 hash 地址由首页兼容脚本迁移到新的 History 地址。

### 17.4 环境配置和部署约定

~~~properties
# 本地 conf/application.properties
seo.site-url=http://localhost:16758

# 服务器 /opt/tools/sqlparserx/conf/application.properties
seo.site-url=https://sqlparserx.dtstack.com
~~~

服务器私有 `bin/quick/deploy.conf` 中维护：

~~~bash
SEO_SITE_URL="https://sqlparserx.dtstack.com"
~~~

`push.sh` 部署后端时会同步更新服务器配置。`SEO_SITE_URL_OVERRIDE` 仅用于需要生成指定环境静态产物的构建任务，不替代服务器运行时配置。

### 17.5 本次构建与部署验证

本次实际执行并通过：

1. `./bin/quick/build.sh -f`：Vite 生产构建成功，首页静态 SEO 内容进入 `conf/static/index.html`。
2. `./bin/quick/build.sh -wf`：中英文 Docusaurus 构建成功，生成 `website/build/sitemap.xml`。
3. `mvn -B package -DskipTests -Dspotless.check.skip=true`：后端 17 个源码文件编译和打包成功。
4. 本地启动后验证 `/` 与 `/sql-lineage.html/` 均返回 `200 text/html;charset=UTF-8`，模板 canonical 正确替换且不残留占位符。
5. 后端、UI 和 website 已部署到 `/opt/tools/sqlparserx`，服务器配置已设置真实公开域名并完成服务重启。
6. 通过 `http://172.16.114.23:16758` 验证首页、六个能力页、robots、唯一 Sitemap 和文档站，全部返回 200。

公开域名在验收时仍由外层网关返回 404；这属于反向代理/DNS 接入范围，不影响 IP 直连服务的部署结果。正式提交搜索引擎前必须确保 `https://sqlparserx.dtstack.com` 已转发到该服务。

---

## 18. 后续 SEO 迭代操作指南

### 18.1 UI 页面改版后的同步流程

`sqlparserx-ui` 是视觉与交互的开发源，SEO 模板是搜索引擎和首访用户看到的稳定内容版本。每次首页或工具页面发生明显改版时：

1. 先在 `sqlparserx-ui` 完成交互和视觉开发。
2. 判断变更是否影响产品定位、能力名称、截图/示例、入口路径、FAQ 或主色样式。
3. 同步更新对应的 `templates/seo/*.html`；共性视觉修改放入 `static/seo/seo.css`。
4. 首页稳定文案变化时同步更新 `sqlparserx-ui/index.html` 的静态首屏内容、meta 和 JSON-LD。
5. 不把编辑器、结果画布等重交互组件复制到 SEO 模板；模板只复刻可稳定表达的布局、内容和入口。
6. 用源码请求验证 HTML，再用浏览器验证 Vue 挂载后的页面，避免只检查其中一层。

### 18.2 新增一个 SEO 能力页

假设新增 `/sql-format/`：

1. 在 `src/main/resources/templates/seo/sql-format.html` 创建模板，至少包含唯一 title、description、canonical、H1、能力说明、示例、FAQ 和工具入口。
2. 在 `SeoPageController.PAGE_TEMPLATES` 注册 `/sql-format -> sql-format`。
3. 在 `@GetMapping` 中同时加入 `/sql-format` 和 `/sql-format/`。
4. 在首页普通链接区域增加该能力页入口。
5. 在主站 Sitemap 页面清单中增加标准 `.html` URL，并重新执行合并构建。
6. 如有 Vue 交互工具，在 router 中新增运行态路由，并从 SEO 页面 CTA 指向它。
7. 编译后请求 `.html` URL，并确认旧无后缀 URL 只重定向到唯一 canonical。

### 18.3 修改域名或增加路径前缀

1. 本地开发只修改本地 `conf/application.properties`。
2. 服务器修改私有 `deploy.conf` 的 `SEO_SITE_URL`，再执行后端部署同步配置。
3. 若地址包含路径前缀，例如 `https://example.com/sqlparserx`，还必须同步配置 Spring context path、Vite base、API 基础路径、Docusaurus baseUrl 和反向代理 location。
4. 重建 UI 与 website，不能只修改服务器配置；首页和 Sitemap 属于构建期静态产物。
5. 全站搜索旧域名、localhost、GitHub Pages 域名和 `__SEO_SITE_URL__`，结果应为空。

### 18.4 文案和元数据维护规则

- 每页只保留一个 H1，title 和 description 必须描述该页独有能力。
- canonical、og:url、Sitemap URL 必须使用同一标准地址和尾斜杠策略。
- 示例 SQL 必须可读、无敏感数据，不虚构未支持的数据库能力。
- JSON-LD 仅声明真实、可验证的信息，不填写虚构评分、价格或用户数量。
- 页面正文优先服务开发者理解，不做关键词堆砌。
- 能力名称或路径变化时同时检查首页、模板、Controller、Sitemap、文档和 Vue router。

### 18.5 每次发布前的最小验证清单

~~~bash
./bin/quick/build.sh -f
./bin/quick/build.sh -wf
mvn -B package -DskipTests -Dspotless.check.skip=true

curl -i http://localhost:16758/
curl -i http://localhost:16758/sql-lineage.html/
curl -i http://localhost:16758/robots.txt
curl -i http://localhost:16758/sitemap.xml
curl -i http://localhost:16758/website/sitemap.xml
~~~

同时执行以下静态检查：

~~~bash
rg -n '__SEO_SITE_URL__|http://localhost|dtstack.github.io' conf/static website/build
xmllint --noout conf/static/sitemap.xml website/build/sitemap.xml
git diff --check
~~~

生产部署后将主机替换为服务器 IP 或正式域名，逐项确认状态码、Content-Type、canonical 和页面正文。正式域名未接通时可以先用 IP 验证应用，但不能向搜索引擎提交尚未可访问的域名 Sitemap。

### 18.6 回滚和故障定位顺序

1. `/` 异常：检查 `conf/static/index.html`、Vite assets 和根路径 forward。
2. 六个能力页异常：检查新后端 jar、SeoPageController 路由、模板是否进入 jar、`seo.site-url` 是否有效。
3. 页面无样式：检查 `/seo/seo.css` 是否已随 UI 静态产物部署。
4. robots/Sitemap 异常：检查 `conf/static` 文件是否部署、是否残留占位符、XML 是否有效。
5. `/website/` 异常：检查服务根目录 `website/`、Docusaurus baseUrl 和 Spring 资源映射。
6. IP 正常但域名异常：检查 DNS、证书、Nginx/网关 upstream、Host 规则和路径转发，不要先回滚应用代码。
7. 回滚时后端 jar、`conf/static` 和 website 应按同一版本成组回退，避免 Controller、模板和 Sitemap 版本不一致。

---

## 19. 根路径兼容性约束

### 19.1 根路径必须保持为首页

SEO 改造不能改变现有用户访问习惯。以下约束属于必须满足的兼容性要求：

1. 公开首页继续使用根路径 /。
2. 用户访问 https://sqlparserx.dtstack.com/ 时，仍然直接进入 SqlParserX 首页。
3. 不新增 /home、/seo/home 或 /app 作为首页替代入口。
4. 不要求用户修改书签、外部链接或已有宣传链接。
5. Sitemap 只收录根路径 /，不额外收录多个首页副本。
6. 根路径的 canonical 必须指向根路径本身。

### 19.2 推荐的根路径实现方式

当前阶段不建议把根路径迁移到独立的 SEO Controller。推荐保持现有链路：

~~~text
GET /
  ↓
sqlparserx-web 转发到 conf/static/index.html
  ↓
index.html 首屏包含可抓取的首页 SEO 内容
  ↓
Vue 应用正常挂载并继续提供原有首页视觉和交互
~~~

首页 SEO 内容应融入当前首页的 HTML 表达中，至少包含：

- SqlParserX 品牌和产品定位。
- 首页唯一 H1。
- 多方言 SQL 解析能力说明。
- 表解析、建表解析、字段/表血缘、参数预编译和 SQL 切分等稳定内容。
- 指向六个能力页或在线工具的普通链接。
- 指向 website 文档和 GitHub 的普通链接。

实现时可以选择：

1. 在 sqlparserx-ui 的 index.html 中加入静态 SEO 首页壳，再由 Vue 挂载后接管首页内容；
2. 在构建阶段将 Home.vue 的稳定内容预渲染为 index.html；
3. 后续确有服务端变量需求时，才使用同样视觉结构的服务端模板返回根路径。

无论采用哪种方式，最终浏览器访问 / 的视觉和导航都应与当前首页保持一致。SEO 模板不应把用户带到另一个新的首页地址。

### 19.3 根路径验收

~~~bash
curl -L -sS -H 'Accept: text/html' https://sqlparserx.dtstack.com/ \
  | rg '<title>|<meta name="description"|<link rel="canonical"|<h1>|sqlparser'
~~~

验收条件：

- 返回 200。
- 页面源码包含首页 SEO 文本，而不是只有空的 app 容器。
- title、description、canonical 和 JSON-LD 属于首页。
- canonical 为站点根路径，不包含 hash 或查询参数。
- 浏览器访问 / 后，原有首页布局、滚动导航、工具入口和 GitHub 链接仍然可用。
- Sitemap 中首页只出现一次。

### 19.4 与工具运行态的关系

根路径保持首页不变，并不要求把六个交互工具全部改造成根路径下的服务端页面：

- /：产品首页和 SEO 入口。
- /app/lineage.html、/app/sql-splitter.html 等：现有 Vue 工具运行态，继续保持兼容。
- /sql-lineage.html 等：后续新增的静态能力介绍页，可通过普通链接进入对应 SPA 工具。
- /api/**：后端解析接口，不进入 Sitemap。

这样可以同时满足“访问根路径仍然是原来的首页”和“搜索引擎能读取首页核心内容”两个要求。
