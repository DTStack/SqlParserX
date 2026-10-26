<template>
  <div class="home">
    <!-- 顶部导航 -->
    <nav class="topbar">
      <div class="topbar-inner">
        <a class="brand" href="#top" @click.prevent="scrollToSection('top')">
          <img class="brand-icon" :src="logoUrl" alt="" width="44" height="44" />
          <span class="brand-name"><span>SqlParser</span><span class="brand-x">X</span></span>
        </a>
        <ul class="nav-links">
          <li><a href="#features" @click.prevent="scrollToSection('features')">功能特性</a></li>
          <li><a href="#capabilities" @click.prevent="scrollToSection('capabilities')">能力支持</a></li>
          <li><a href="#docs" @click.prevent="scrollToSection('docs')">项目概览</a></li>
          <li><a href="#community" @click.prevent="scrollToSection('community')">社区</a></li>
        </ul>
        <div class="nav-cta">
          <a class="btn btn-ghost" href="https://github.com/DTStack/SqlParserX" target="_blank" rel="noopener">源码</a>
          <a class="btn btn-primary" href="#start" @click.prevent="scrollToSection('start')">立即体验</a>
        </div>
      </div>
    </nav>

    <main class="container">
      <!-- HERO 区 -->
      <section id="top" class="hero">
        <div class="hero-left">
          <div class="pill-badges">
            <span class="pill"><span class="dot-live"></span> SQL PARSING ENGINE</span>
            <span class="pill">Apache 2.0 协议</span>
            <span class="pill">Druid 1.2.28</span>
          </div>

          <h1 class="hero-title">SqlParser<span>X</span><span class="hero-title-rule"></span></h1>
          <h2 class="hero-subtitle">多数据库方言 SQL 解析引擎</h2>

          <p class="hero-desc">
            覆盖<strong>SQL 类型识别</strong>、<strong>真实表与建表结构解析</strong>、<strong>表 / 字段血缘</strong>、
            <strong>函数名提取</strong>及<strong>参数预编译</strong>，并提供 Web 工具进行交互验证。
          </p>

          <div class="hero-cta">
            <router-link to="/lineage" class="btn btn-primary btn-lg">立即体验</router-link>
            <a class="btn btn-ghost btn-lg" href="https://github.com/DTStack/SqlParserX" target="_blank" rel="noopener">查看源码</a>
          </div>

          <div class="trust-row">
            <span class="trust-item">15 种方言解析器</span>
            <span class="trust-item">6 类通用解析能力</span>
            <span class="trust-item">活跃维护中</span>
          </div>
        </div>

        <div class="hero-right">
          <div class="sql-preview">
            <div class="preview-header"><span class="preview-label"><i class="el-icon-document"></i> SQL 血缘解析</span><span class="preview-dialect">MySQL · 示例</span></div>
            <div class="preview-code"><div class="line-numbers" aria-hidden="true">01<br>02<br>03<br>04<br>05<br>06</div><pre><span class="sql-keyword">INSERT INTO</span> analytics.order_summary
  (user_id, total_amount)
<span class="sql-keyword">SELECT</span> u.id, <span class="sql-function">SUM</span>(o.amount)
<span class="sql-keyword">FROM</span> users u <span class="sql-keyword">JOIN</span> orders o
  <span class="sql-keyword">ON</span> u.id = o.user_id
<span class="sql-keyword">GROUP BY</span> u.id;</pre></div>
            <div class="preview-result">
              <div class="preview-result-title"><span>字段血缘</span><span class="preview-status"><i class="el-icon-circle-check"></i> 解析结果示例</span></div>
              <div class="lineage-example"><div class="lineage-sources"><div><span class="node-label">来源字段</span><code>users.id</code></div><div><span class="node-label">来源字段</span><code>orders.amount</code></div></div><div class="lineage-arrows" aria-hidden="true"><span>→</span><span>→</span></div><div class="lineage-target"><span class="node-label">analytics.order_summary</span><code>user_id</code><code>total_amount</code></div></div>
            </div>
            <div class="preview-footer"><span>从 SQL 语句到清晰的数据关系</span><router-link to="/lineage">试试这项工具 <i class="el-icon-top-right"></i></router-link></div>
          </div>
        </div>
      </section>

      <!-- 方言横条 -->
      <section class="dialect-strip">
        <span class="strip-label">支持方言</span>
        <div class="chip-list">
          <span class="chip">MySQL</span>
          <span class="chip">Hive</span>
          <span class="chip">Doris</span>
          <span class="chip">StarRocks</span>
          <span class="chip">ClickHouse</span>
          <span class="chip">Oracle</span>
          <span class="chip">PostgreSQL</span>
          <span class="chip">Spark</span>
          <span class="chip">Trino</span>
          <span class="chip">DM</span>
          <span class="chip">OceanBase</span>
          <span class="chip">OceanBase Oracle</span>
          <span class="chip">SQL Server</span>
          <span class="chip">Greenplum</span>
          <span class="chip">TiDB</span>
        </div>
      </section>

      <!-- 通用解析能力 -->
      <section id="capabilities" class="capabilities-section">
        <div class="section-head">
          <span class="section-tag">解析能力</span>
          <h2 class="section-title">通用解析能力</h2>
          <p class="section-sub">六类通用解析能力，覆盖语句识别、结构提取、血缘追溯与参数化查询。</p>
        </div>
        <div class="capabilities-grid">
          <article v-for="(capability, index) in capabilities" :key="capability.title" :class="['capability-card', capability.tone]">
            <div class="capability-top">
              <span class="capability-icon">
                <svg v-if="capability.iconType === 'type'" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M5 3h10l4 4v14H5zM15 3v5h4M8 12h8M8 16h5"/><path class="icon-accent" d="m8 8 1.5 1.5L12 7"/>
                </svg>
                <svg v-else-if="capability.iconType === 'structure'" viewBox="0 0 24 24" aria-hidden="true">
                  <ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v6c0 1.7 3.6 3 8 3s8-1.3 8-3V5M4 11v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6"/><path class="icon-accent" d="M8 9h8"/>
                </svg>
                <svg v-else-if="capability.iconType === 'table-lineage'" viewBox="0 0 24 24" aria-hidden="true">
                  <rect x="2.5" y="8.5" width="6" height="7" rx="1.5"/><rect x="15.5" y="3" width="6" height="7" rx="1.5"/><rect x="15.5" y="14" width="6" height="7" rx="1.5"/><path class="icon-accent" d="M8.5 12H12V6.5h3.5M12 12v5.5h3.5"/>
                </svg>
                <svg v-else-if="capability.iconType === 'column-lineage'" viewBox="0 0 24 24" aria-hidden="true">
                  <rect x="2" y="3" width="7" height="18" rx="1.5"/><rect x="15" y="3" width="7" height="18" rx="1.5"/><path d="M4.5 7h2M4.5 11h2M17.5 7h2M17.5 11h2M17.5 15h2"/><path class="icon-accent" d="M6.5 15h4l3-6h3M14 7l2.5 2L14 11"/>
                </svg>
                <svg v-else-if="capability.iconType === 'function'" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M10.5 4.5c-2.2 0-3.4 1.25-3.85 3.75L5.3 16c-.42 2.35-1.45 3.5-3.3 3.5"/>
                  <path d="M4.5 11.5h6"/>
                  <path class="icon-accent" d="M14 8.5l3 3.5-3 3.5M22 8.5l-3 3.5 3 3.5"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M7 3H5a2 2 0 0 0-2 2v4l-2 3 2 3v4a2 2 0 0 0 2 2h2M17 3h2a2 2 0 0 1 2 2v4l2 3-2 3v4a2 2 0 0 1-2 2h-2"/><path class="icon-accent" d="M9 8h6M9 12h6M9 16h6"/>
                </svg>
              </span>
              <span class="capability-index">0{{ index + 1 }}</span>
            </div>
            <h3>{{ capability.title }}</h3>
            <p>{{ capability.description }}</p>
            <div class="capability-tags"><span v-for="tag in capability.tags" :key="tag">{{ tag }}</span></div>
            <div class="capability-example"><code>{{ capability.example }}</code><i class="el-icon-right" aria-hidden="true"></i><span>{{ capability.result }}</span></div>
          </article>
        </div>
      </section>

      <!-- 特性区 -->
      <section id="features" class="features-section">
        <div class="section-head">
          <span class="section-tag">在线工具</span>
          <h2 class="section-title">可直接使用的 SQL 工具</h2>
          <p class="section-sub">围绕 SQL 解析、验证和结构查看提供的在线入口</p>
        </div>

        <div class="features-grid">
          <router-link to="/lineage" class="feature-card">
            <div class="card-icon icon-lineage">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="9" width="6" height="6" rx="1.5"/><rect x="16" y="2" width="6" height="6" rx="1.5"/><rect x="16" y="16" width="6" height="6" rx="1.5"/><path d="M8 12h4V5h4M12 12v7h4"/></svg>
            </div>
            <div class="card-body">
              <h3>血缘解析可视化</h3>
              <p>字段级与表级血缘双向切换，支持缩放、拖拽与全屏交互式探索。</p>
              <span class="card-tag">字段级 / 表级</span>
            </div>
          </router-link>

          <router-link to="/parameter-workbench" class="feature-card">
            <div class="card-icon icon-parameter">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 3H5a2 2 0 0 0-2 2v4l-2 3 2 3v4a2 2 0 0 0 2 2h2M17 3h2a2 2 0 0 1 2 2v4l2 3-2 3v4a2 2 0 0 1-2 2h-2M9 8h6M9 12h6M9 16h6"/></svg>
            </div>
            <div class="card-body">
              <h3>SQL 参数提取与预编译</h3>
              <p>提取动态入参和结果字段，编辑参数值并生成预编译 SQL 与有序绑定值。</p>
              <span class="card-tag">参数化查询</span>
            </div>
          </router-link>

          <router-link to="/syntax-validator" class="feature-card">
            <div class="card-icon icon-validate">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M14 2H5a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9l-7-7Z"/><path d="M14 2v7h7M8 15l3 3 5-6"/></svg>
            </div>
            <div class="card-body">
              <h3>语法解析验证</h3>
              <p>多方言语法校验，查看解析错误信息，快速定位语句中的语法问题。</p>
              <span class="card-tag">多方言校验</span>
            </div>
          </router-link>

          <router-link to="/table-parser" class="feature-card">
            <div class="card-icon icon-table">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M3 15h18M9 9v12M15 9v12"/></svg>
            </div>
            <div class="card-body">
              <h3>SQL 表解析</h3>
              <p>解析主表与全部真实表，自动展开 WITH AS / CTE / JOIN / UNION。</p>
              <span class="card-tag">真实表提取</span>
            </div>
          </router-link>

          <router-link to="/create-table-parser" class="feature-card">
            <div class="card-icon icon-create">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="3" width="15" height="18" rx="2"/><path d="M3 9h15M3 15h7M9 9v12M18 13v8M14 17h8"/></svg>
            </div>
            <div class="card-body">
              <h3>建表 SQL 解析</h3>
              <p>表名、字段、分区、分桶、键与 properties 全维度解析。</p>
              <span class="card-tag">结构级解析</span>
            </div>
          </router-link>

          <router-link to="/sql-splitter" class="feature-card">
            <div class="card-icon icon-split">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4h7M4 8h5M4 16h5M4 20h7M2 12h20M16 3l3 3 3-3M19 6V1M16 21l3-3 3 3M19 18v5"/></svg>
            </div>
            <div class="card-body">
              <h3>SQL 切分测试</h3>
              <p>将多语句脚本按语句智能切分，支持自定义与原生两种策略。</p>
              <span class="card-tag">脚本拆分</span>
            </div>
          </router-link>


        </div>
      </section>

      <!-- 统计区 -->
      <section id="docs" class="stats-section" aria-label="项目概览">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-num">15</div>
            <div class="stat-label">数据库方言</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">114</div>
            <div class="stat-label">单元测试用例</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">6</div>
            <div class="stat-label">通用解析能力</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">Apache 2.0</div>
            <div class="stat-label">开源协议</div>
          </div>
        </div>
      </section>

      <!-- 最终 CTA -->
      <section id="start" class="final-cta">
        <h2>开始解析你的 SQL</h2>
        <p>选择数据源，输入 SQL，即可探索结构、血缘与参数。</p>
        <div class="hero-cta center">
          <router-link to="/lineage" class="btn btn-primary btn-lg">进入体验</router-link>
          <a class="btn btn-ghost btn-lg" href="https://github.com/DTStack/SqlParserX" target="_blank" rel="noopener">查看项目源码</a>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer id="community" class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand">
            <img class="brand-icon" :src="logoUrl" alt="" width="44" height="44" />
            <span class="brand-name"><span>SqlParser</span><span class="brand-x">X</span></span>
          </div>
          <p class="footer-tag">多数据库方言 SQL 解析引擎</p>
          <p class="footer-intro">SqlParserX 是来自袋鼠云数据中台的开源项目，致力于提供统一、可扩展的多数据库 SQL 解析能力。</p>
          <p class="footer-copy">Apache License 2.0 · Copyright © 2026 dtstack</p>
        </div>
        <div class="footer-cols">
          <div class="footer-col">
            <h4>产品</h4>
            <a href="#features" @click.prevent="scrollToSection('features')">核心功能</a>
            <a href="#capabilities" @click.prevent="scrollToSection('capabilities')">解析能力</a>
            <a href="#start" @click.prevent="scrollToSection('start')">立即体验</a>
          </div>
          <div class="footer-col">
            <h4>开发</h4>
            <a href="https://github.com/DTStack/SqlParserX" target="_blank" rel="noopener">源码仓库</a>
            <a href="#docs" @click.prevent="scrollToSection('docs')">项目概览</a>
            <router-link to="/parameter-workbench">参数预编译</router-link>
          </div>
          <div class="footer-col">
            <h4>社区</h4>
            <a href="#community" @click.prevent="scrollToSection('community')">Issue</a>
            <a href="#community" @click.prevent="scrollToSection('community')">贡献指南</a>
            <a href="#community" @click.prevent="scrollToSection('community')">更新日志</a>
          </div>
          <div class="footer-col">
            <h4>数据中台</h4>
            <a href="https://www.dtstack.com/" target="_blank" rel="noopener">袋鼠云官网</a>
            <a href="https://github.com/DTStack/SqlParserX" target="_blank" rel="noopener">SqlParserX 开源项目</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    scrollToSection(id) {
      const section = this.$el.querySelector('#' + id)
      if (section) {
        const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        section.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' })
      }
    }
  },
  data() {
    return {
      logoUrl: `${import.meta.env.BASE_URL}logo.png`,
      capabilities: [
        { title: 'SQL 类型识别', iconType: 'type', tone: 'tone-blue', description: '通过语法树识别查询、写入、建表与管理语句，为不同 SQL 找到对应的处理入口。', tags: ['SELECT / WITH', 'DML / DDL', 'SHOW / EXPLAIN'], example: 'SELECT …', result: '查询语句' },
        { title: '真实表与建表结构', iconType: 'structure', tone: 'tone-slate', description: '穿透别名与嵌套查询找到真实表，解析建表语句中的字段、键、分区与分桶结构。', tags: ['JOIN / CTE', '字段与键', '分区 / 分桶'], example: 'CREATE TABLE …', result: '结构化表定义' },
        { title: '表级血缘', iconType: 'table-lineage', tone: 'tone-teal', description: '从 INSERT 和 CTAS 语句追溯来源表与目标表，呈现数据在表之间的流转关系。', tags: ['INSERT / CTAS', '子查询', 'UNION'], example: 'source → target', result: '表依赖关系' },
        { title: '字段级血缘', iconType: 'column-lineage', tone: 'tone-teal', description: '追溯来源字段与目标字段的映射，分析表达式、函数与 CASE 中的字段依赖。', tags: ['表达式 / CASE', 'CTE / UNION', '字段映射'], example: 'SUM(o.amount)', result: '来源字段追溯' },
        { title: '函数名提取', iconType: 'function', tone: 'tone-slate', description: '从 SQL 中收集普通函数、聚合函数和窗口函数，支持多语句提取与函数名去重。', tags: ['普通函数', '聚合函数', '窗口函数'], example: 'SUM · ROW_NUMBER', result: '函数名称列表' },
        { title: '参数解析与预编译', iconType: 'parameter', tone: 'tone-blue', description: '面向 SELECT 提取动态入参与结果字段，生成 JDBC 占位符 SQL 和按顺序排列的绑定值。', tags: ['SELECT', '动态入参', 'JDBC 预编译'], example: 'id = ${id}', result: 'id = ?' }
      ]
    }
  }
}
</script>

<style scoped>
.home { --home-ink: #182b49; --home-accent: #2563eb; --home-line: #e2e8f0; position: relative; overflow-x: hidden; background: #f8fafc; color: var(--home-ink); }
.home a:focus-visible { outline: 2px solid #3b82f6; outline-offset: 4px; }
.home section[id], .site-footer { scroll-margin-top: 88px; }
.topbar { position: sticky; top: 0; z-index: 50; background: rgba(255, 255, 255, .95); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border-bottom: 1px solid var(--home-line); }
.topbar-inner { max-width: 1240px; margin: 0 auto; height: 68px; padding: 0 32px; display: flex; align-items: center; gap: 34px; }
.brand { display: flex; align-items: center; gap: 8px; text-decoration: none; flex: none; }
.brand-icon { display: block; flex: none; width: 44px; height: 44px; object-fit: contain; }
.brand-name { font-size: 19px; font-weight: 800; color: #172b44; letter-spacing: -.55px; -webkit-text-stroke: .25px currentColor; }
.brand-x { margin-left: 1px; color: #2563eb; -webkit-text-stroke: .3px currentColor; }
.nav-links { display: flex; gap: 26px; list-style: none; margin: 0; padding: 0; }
.nav-links a { color: #54616c; font-size: 13px; font-weight: 600; transition: color .2s; }
.nav-links a:hover { color: #2563eb; }
.nav-cta { margin-left: auto; display: flex; gap: 8px; }
.btn { display: inline-flex; align-items: center; justify-content: center; gap: 8px; padding: 9px 17px; border-radius: 6px; font-size: 13px; font-weight: 650; text-decoration: none; border: 1px solid transparent; cursor: pointer; transition: background .2s, border-color .2s, box-shadow .2s; color: var(--home-ink); }
.btn-lg { padding: 12px 22px; font-size: 14px; }
.btn-primary { background: #2563eb; border-color: #2563eb; color: #fff; box-shadow: 0 3px 10px rgba(37, 99, 235, .18); }
.btn-primary:hover { background: #1d4ed8; border-color: #1d4ed8; color: #fff; box-shadow: 0 5px 15px rgba(37, 99, 235, .22); }
.btn-ghost { background: #fff; border-color: #d8e2ef; color: #405778; }
.btn-ghost:hover { background: #eff6ff; border-color: #93c5fd; color: #2563eb; }
.container { max-width: 1240px; margin: 0 auto; padding: 0 32px 48px; }
.hero { display: grid; grid-template-columns: 1fr 1.07fr; gap: 64px; align-items: center; padding: 82px 0 66px; }
.hero-left { min-width: 0; }
.pill-badges { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 28px; align-items: center; }
.pill { display: inline-flex; align-items: center; gap: 7px; font-size: 11px; color: #697784; font-weight: 600; }
.pill:first-child { color: #2563eb; letter-spacing: 1px; }
.pill + .pill { border-left: 1px solid #ccd4da; padding-left: 12px; }
.dot-live { width: 6px; height: 6px; border-radius: 50%; background: #06b6d4; }
.hero-title { display: inline-flex; position: relative; font-size: clamp(48px, 5.4vw, 70px); font-weight: 800; letter-spacing: -3px; margin: 0 0 20px; color: var(--home-ink); line-height: 1.08; }
.hero-title > span:first-child { color: var(--home-accent); }
.brand-x, .hero-title > span:first-child { background: linear-gradient(180deg, #11c9e8 0%, #1684ff 55%, #073de1 100%); background-clip: text; -webkit-background-clip: text; color: transparent; -webkit-text-fill-color: transparent; -webkit-text-stroke: 0; }
.hero-title-rule { position: absolute; left: 0; bottom: -9px; width: 44px; height: 3px; background: #06b6d4; }
.hero-subtitle { font-size: clamp(19px, 2vw, 25px); font-weight: 650; color: #2563eb; margin: 11px 0 18px; letter-spacing: .2px; }
.hero-desc { font-size: 14px; color: #647380; line-height: 1.95; max-width: 460px; margin: 0 0 28px; }
.hero-desc strong { color: #405566; font-weight: 600; }
.hero-cta { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 24px; }
.hero-cta.center { justify-content: center; }
.trust-row { display: flex; flex-wrap: wrap; gap: 16px; color: #6d7b87; font-size: 11px; }
.trust-item { position: relative; padding-left: 11px; }
.trust-item::before { content: ''; position: absolute; left: 0; top: 50%; width: 4px; height: 4px; margin-top: -2px; border-radius: 50%; background: #38bdf8; }
.hero-right { min-width: 0; }
.sql-preview { overflow: hidden; border: 1px solid #d6e5f7; border-radius: 12px; background: #fff; box-shadow: 0 22px 50px -18px rgba(37, 99, 235, .2), 0 1px 3px rgba(37, 99, 235, .04); }
.preview-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 16px 20px; background: #eff6ff; border-bottom: 1px solid #dce9fa; }
.preview-label { display: flex; align-items: center; gap: 8px; color: #244975; font-size: 12px; font-weight: 600; }
.preview-label i { color: #3b82f6; font-size: 16px; }
.preview-dialect { color: #6384ad; font-size: 10px; }
.preview-code { display: flex; gap: 15px; padding: 22px 20px; overflow: auto; background: #f8fbff; }
.preview-code pre { margin: 0; color: #344966; }
.preview-code pre, .line-numbers { font: 12px/1.9 'SFMono-Regular', Consolas, monospace; }
.line-numbers { color: #9aadc6; text-align: right; user-select: none; }
.sql-keyword { color: #2563eb; font-weight: 600; }
.sql-function { color: #7c3aed; }
.preview-result { padding: 18px 20px; background: #fff; }
.preview-result-title { display: flex; justify-content: space-between; align-items: center; gap: 10px; margin-bottom: 14px; font-size: 12px; color: #334b5e; font-weight: 700; }
.preview-status { color: #0891b2; font-size: 10px; font-weight: 500; }
.lineage-example { display: flex; align-items: stretch; gap: 12px; }
.lineage-sources { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.lineage-sources > div { padding: 8px 11px; background: #eff6ff; border: 1px solid #dbeafe; border-radius: 5px; }
.node-label { display: block; margin-bottom: 3px; color: #7b8a94; font-size: 9px; }
.lineage-example code { display: block; color: #324e65; font: 600 11px/1.7 'SFMono-Regular', Consolas, monospace; overflow-wrap: anywhere; }
.lineage-arrows { display: flex; flex-direction: column; justify-content: space-around; color: #60a5fa; font-size: 23px; }
.lineage-target { flex: 1.2; min-width: 0; padding: 10px 12px; border: 1px solid #bae6fd; border-radius: 5px; background: #ecfeff; }
.lineage-target .node-label { color: #0e7490; margin-bottom: 9px; }
.lineage-target code + code { margin-top: 14px; }
.preview-footer { display: flex; justify-content: space-between; align-items: center; gap: 10px; padding: 11px 20px; background: #f0f7ff; border-top: 1px solid #dce9fa; font-size: 10px; color: #778aa2; }
.preview-footer a { color: #2563eb; font-weight: 650; white-space: nowrap; }
.dialect-strip { display: flex; align-items: flex-start; gap: 26px; padding: 24px 0; border-top: 1px solid var(--home-line); border-bottom: 1px solid var(--home-line); margin: 0 0 68px; }
.strip-label { padding-top: 5px; font-size: 12px; color: #4f626f; font-weight: 700; flex: none; }
.chip-list { display: flex; flex-wrap: wrap; gap: 7px; }
.chip { padding: 4px 10px; border-radius: 4px; font-size: 11px; color: #4d6585; background: #fff; border: 1px solid #e2e8f0; font-weight: 550; }
.section-head { text-align: left; margin: 0 0 28px; }
.section-tag { display: inline-block; color: #2563eb; font-size: 11px; font-weight: 700; letter-spacing: 1px; margin-bottom: 11px; }
.section-title { font-size: clamp(24px, 3vw, 32px); font-weight: 750; margin: 0 0 10px; color: var(--home-ink); letter-spacing: -.6px; line-height: 1.4; }
.section-sub { color: #70808c; font-size: 13px; margin: 0; line-height: 1.8; }
.capabilities-section { margin: 0 0 76px; }
.capabilities-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.capability-card { --card-accent: #2563eb; --card-fill: #eff6ff; display: flex; flex-direction: column; min-width: 0; padding: 24px 23px 0; background: #fff; border: 1px solid #e0e8f2; border-radius: 9px; overflow: hidden; box-shadow: 0 3px 10px rgba(37, 99, 235, .035); }
.tone-teal { --card-accent: #0891b2; --card-fill: #ecfeff; }
.tone-slate { --card-accent: #7c3aed; --card-fill: #f5f3ff; }
.capability-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 17px; }
.capability-icon { display: grid; place-items: center; width: 40px; height: 40px; color: var(--card-accent); background: var(--card-fill); border-radius: 8px; font-size: 21px; }
.capability-icon svg { display: block; width: 25px; height: 25px; fill: none; stroke: currentColor; stroke-width: 1.65; stroke-linecap: round; stroke-linejoin: round; }
.capability-icon .icon-accent { stroke-width: 2.1; }
.capability-index { color: #a2afb8; font: 500 12px/1 Consolas, monospace; }
.capability-card h3 { margin: 0 0 9px; color: #263f52; font-size: 16px; font-weight: 750; }
.capability-card p { margin: 0 0 18px; color: #687a88; font-size: 12px; line-height: 1.85; }
.capability-tags { display: flex; flex-wrap: wrap; gap: 7px; margin-top: auto; padding-bottom: 20px; }
.capability-tags span { color: var(--card-accent); font-size: 10px; padding: 3px 7px; background: var(--card-fill); border-radius: 3px; }
.capability-example { display: flex; align-items: center; gap: 8px; margin: 0 -23px; padding: 12px 23px; border-top: 1px solid #e6edf6; background: #f8fbff; min-width: 0; }
.capability-example code { font: 11px/1.5 'SFMono-Regular', Consolas, monospace; color: #3563a0; overflow-wrap: anywhere; }
.capability-example i { color: #96a6b1; font-size: 12px; flex: none; }
.capability-example > span { font-size: 10px; color: #7a8a96; white-space: nowrap; }
.features-section { margin-bottom: 56px; }
.features-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.feature-card { position: relative; display: flex; gap: 14px; padding: 24px 20px; border-radius: 8px; background: #fff; border: 1px solid #e0e8f2; text-decoration: none; color: var(--home-ink); transition: border-color .2s, background .2s, box-shadow .2s; }
.feature-card::after { content: '↗'; position: absolute; top: 14px; right: 14px; color: #7aa6dc; font-size: 15px; }
.feature-card:hover { background: #fafdff; border-color: #93c5fd; box-shadow: 0 8px 20px rgba(37, 99, 235, .08); }
.card-icon { flex: 0 0 40px; height: 40px; border-radius: 9px; display: flex; align-items: center; justify-content: center; color: #2563eb; background: #eff6ff; }
.card-icon svg { display: block; width: 23px; height: 23px; }
.icon-lineage, .icon-table { color: #0891b2; background: #ecfeff; }
.icon-create, .icon-split { color: #7c3aed; background: #f5f3ff; }
.card-body { flex: 1; min-width: 0; }
.card-body h3 { margin: 2px 12px 9px 0; font-size: 14px; font-weight: 750; color: #263f52; }
.card-body p { margin: 0 0 12px; color: #6a7c8a; font-size: 12px; line-height: 1.8; }
.card-tag { display: inline-block; color: #2563eb; font-size: 10px; font-weight: 600; }
.stats-section { margin: 0 0 52px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); padding: 26px 0; border-top: 1px solid var(--home-line); border-bottom: 1px solid var(--home-line); }
.stat-item { text-align: center; padding: 5px 16px; }
.stat-item + .stat-item { border-left: 1px solid var(--home-line); }
.stat-num { font-size: clamp(25px, 3vw, 36px); font-weight: 750; letter-spacing: -1px; color: #2563eb; margin-bottom: 4px; }
.stat-label { color: #7a8894; font-size: 11px; }
.final-cta { position: relative; margin: 0; padding: 44px 24px; border-radius: 10px; background: linear-gradient(120deg, #eff6ff, #ecfeff); border: 1px solid #d6e8fb; text-align: center; }
.final-cta h2 { font-size: clamp(22px, 3vw, 30px); font-weight: 700; margin: 0 0 10px; color: #1e3a5f; letter-spacing: -.5px; }
.final-cta p { color: #6380a0; margin: 0 0 25px; font-size: 13px; }
.final-cta .hero-cta { margin-bottom: 0; }
.final-cta .btn-primary { color: #fff; background: #2563eb; border-color: #2563eb; }
.final-cta .btn-primary:hover { background: #1d4ed8; border-color: #1d4ed8; }
.final-cta .btn-ghost { color: #3563a0; border-color: #bfd6f3; background: #fff; }
.final-cta .btn-ghost:hover { color: #2563eb; background: #eff6ff; border-color: #93c5fd; }
.site-footer { border-top: 1px solid var(--home-line); padding: 36px 32px 28px; background: #fff; }
.footer-inner { max-width: 1176px; margin: 0 auto; display: grid; grid-template-columns: 1.2fr 1fr; gap: 60px; }
.footer-brand .brand { margin-bottom: 12px; }
.footer-brand .brand-icon { width: 36px; height: 36px; }
.footer-brand .brand-name { font-size: 16px; }
.footer-tag { color: #6b7c88; font-size: 12px; margin: 0 0 9px; }
.footer-intro { max-width: 360px; color: #86939d; font-size: 11px; line-height: 1.7; margin: 0 0 10px; }
.footer-copy { color: #86939d; font-size: 11px; margin: 0; }
.footer-cols { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.footer-col h4 { font-size: 12px; margin: 0 0 12px; color: #3a5366; font-weight: 700; }
.footer-col a { display: block; color: #74838f; font-size: 11px; text-decoration: none; padding: 4px 0; transition: color .2s; }
.footer-col a:hover { color: #2563eb; }
@media (max-width: 1080px) {
  .hero { gap: 32px; }
  .nav-links { gap: 18px; }
  .topbar-inner { gap: 24px; }
  .features-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 860px) {
  .hero { grid-template-columns: 1fr; gap: 32px; padding: 48px 0 40px; }
  .hero-left { max-width: 620px; }
  .hero-desc { max-width: 560px; }
  .hero-right { width: 100%; max-width: 620px; }
  .nav-links { display: none; }
  .capabilities-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .dialect-strip { margin-bottom: 48px; }
  .footer-inner { grid-template-columns: 1fr; gap: 28px; }
}
@media (max-width: 540px) {
  .topbar-inner { height: 60px; gap: 10px; padding: 0 16px; }
  .brand-name { font-size: 17px; }
  .nav-cta .btn-ghost { display: none; }
  .nav-cta .btn { padding: 8px 12px; font-size: 12px; }
  .container { padding: 0 16px 32px; }
  .hero { padding-top: 36px; }
  .hero-title { font-size: 48px; letter-spacing: -2px; }
  .pill-badges { gap: 8px; }
  .pill { font-size: 9px; }
  .pill + .pill { padding-left: 8px; }
  .trust-row { gap: 10px; font-size: 10px; }
  .hero-cta .btn-lg { flex: 1; }
  .preview-header, .preview-result { padding: 14px; }
  .preview-code { padding: 18px 14px; gap: 10px; }
  .preview-code pre, .line-numbers { font-size: 10px; }
  .preview-footer { padding: 10px 14px; flex-wrap: wrap; }
  .lineage-example { gap: 7px; }
  .lineage-example code { font-size: 10px; }
  .dialect-strip { flex-direction: column; gap: 12px; padding: 20px 0; }
  .capabilities-grid, .features-grid { grid-template-columns: 1fr; }
  .capabilities-section { margin-bottom: 48px; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); row-gap: 20px; }
  .stat-item:nth-child(3) { border-left: 0; }
  .final-cta { padding: 32px 18px; }
  .site-footer { padding: 28px 16px; }
  .footer-cols { grid-template-columns: repeat(2, 1fr); gap: 16px; }
}
</style>
