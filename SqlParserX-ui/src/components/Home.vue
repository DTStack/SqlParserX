<template>
  <div class="home">
    <!-- 顶部导航 -->
    <nav class="topbar">
      <div class="topbar-inner">
        <a class="brand" href="#top">
          <svg class="brand-icon" viewBox="0 0 48 48" width="30" height="30" fill="none">
            <defs>
              <linearGradient id="brand-grad" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#2563eb"/>
                <stop offset="100%" stop-color="#7c3aed"/>
              </linearGradient>
            </defs>
            <rect x="6" y="8" width="36" height="32" rx="7" stroke="url(#brand-grad)" stroke-width="3"/>
            <path d="M14 20h20M14 28h12" stroke="url(#brand-grad)" stroke-width="3" stroke-linecap="round"/>
            <circle cx="34" cy="28" r="2.5" fill="#2563eb"/>
          </svg>
          <span class="brand-name">SqlParserX</span>
        </a>
        <ul class="nav-links">
          <li><a href="#features">功能特性</a></li>
          <li><a href="#mcp">MCP Server</a></li>
          <li><a href="#docs">开发文档</a></li>
          <li><a href="#community">社区</a></li>
        </ul>
        <div class="nav-cta">
          <a class="btn btn-ghost" href="#docs">文档</a>
          <a class="btn btn-primary" href="#start">立即体验</a>
        </div>
      </div>
    </nav>

    <main class="container">
      <!-- HERO 区 -->
      <section id="top" class="hero">
        <div class="hero-left">
          <div class="pill-badges">
            <span class="pill"><span class="dot-live"></span> v1.0 · 稳定可用</span>
            <span class="pill">Apache 2.0 协议</span>
            <span class="pill">Druid 1.2.28</span>
          </div>

          <h1 class="hero-title">SqlParserX</h1>
          <h2 class="hero-subtitle">多数据库方言 SQL 解析引擎</h2>

          <p class="hero-desc">
            一站式<strong>表 / 字段血缘解析</strong>、<strong>SQL 切分</strong>、<strong>建表解析</strong>、
            <strong>语法校验</strong>与<strong>行级权限注入</strong>，并提供 Web 可视化与 MCP
            Server，接入 AI Agent 与大模型应用开箱即用。
          </p>

          <div class="hero-cta">
            <router-link to="/lineage" class="btn btn-primary btn-lg">立即体验</router-link>
            <a class="btn btn-ghost btn-lg" href="https://gitlab.prod.dtstack.cn/changlu/SqlParserX" target="_blank" rel="noopener">在 GitHub 查看</a>
          </div>

          <div class="trust-row">
            <span class="trust-item">15+ 方言解析器</span>
            <span class="trust-item">70+ 单元测试</span>
            <span class="trust-item">Spring AI MCP</span>
            <span class="trust-item">活跃维护中</span>
          </div>
        </div>

        <div class="hero-right">
          <mockup-terminal></mockup-terminal>
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
          <span class="chip">SQL Server</span>
          <span class="chip">Greenplum</span>
          <span class="chip">TiDB</span>
          <span class="chip">+ 更多</span>
        </div>
      </section>

      <!-- 特性区 -->
      <section id="features" class="features-section">
        <div class="section-head">
          <span class="section-tag">Core Features</span>
          <h2 class="section-title">为数据血缘而生的解析能力</h2>
          <p class="section-sub">从单条 SQL 到多语句脚本，覆盖解析、可视化与接入的全链路能力</p>
        </div>

        <div class="features-grid">
          <router-link to="/lineage" class="feature-card">
            <div class="card-icon icon-lineage">
              <i class="el-icon-share"></i>
            </div>
            <div class="card-body">
              <h3>血缘解析可视化</h3>
              <p>字段级与表级血缘双向切换，支持缩放、拖拽与全屏交互式探索。</p>
              <span class="card-tag">字段级 / 表级</span>
            </div>
          </router-link>

          <router-link to="/syntax-validator" class="feature-card">
            <div class="card-icon icon-validate">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="card-body">
              <h3>语法解析验证</h3>
              <p>多方言语法校验，错误时输出精准错误信息与完整堆栈定位。</p>
              <span class="card-tag">多方言校验</span>
            </div>
          </router-link>

          <router-link to="/table-parser" class="feature-card">
            <div class="card-icon icon-table">
              <i class="el-icon-collection"></i>
            </div>
            <div class="card-body">
              <h3>SQL 表解析</h3>
              <p>解析主表与全部真实表，自动展开 WITH AS / CTE / JOIN / UNION。</p>
              <span class="card-tag">真实表提取</span>
            </div>
          </router-link>

          <router-link to="/create-table-parser" class="feature-card">
            <div class="card-icon icon-create">
              <i class="el-icon-plus"></i>
            </div>
            <div class="card-body">
              <h3>建表 SQL 解析</h3>
              <p>表名、字段、分区、分桶、键与 properties 全维度解析。</p>
              <span class="card-tag">结构级解析</span>
            </div>
          </router-link>

          <router-link to="/sql-splitter" class="feature-card">
            <div class="card-icon icon-split">
              <i class="el-icon-sort"></i>
            </div>
            <div class="card-body">
              <h3>SQL 切分测试</h3>
              <p>将多语句脚本按语句智能切分，支持自定义与原生两种策略。</p>
              <span class="card-tag">脚本拆分</span>
            </div>
          </router-link>

          <div id="mcp" class="feature-card card-mcp">
            <div class="card-icon icon-mcp">
              <i class="el-icon-connection"></i>
            </div>
            <div class="card-body">
              <h3>MCP Server 接入</h3>
              <p>基于 SSE 将解析能力注册为 MCP 工具，AI Agent 直接调用。</p>
              <span class="card-tag">LLM 生态</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 统计区 -->
      <section id="docs" class="stats-section">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-num">15+</div>
            <div class="stat-label">数据库方言</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">71</div>
            <div class="stat-label">单元测试用例</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">5</div>
            <div class="stat-label">核心解析能力</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">Apache 2.0</div>
            <div class="stat-label">开源协议</div>
          </div>
        </div>
      </section>

      <!-- 最终 CTA -->
      <section id="start" class="final-cta">
        <h2>准备好开始解析你的 SQL 了吗？</h2>
        <p>一键启动 Web 服务，或通过 MCP 把能力嵌入到 AI Agent</p>
        <div class="hero-cta center">
          <router-link to="/lineage" class="btn btn-primary btn-lg">进入体验</router-link>
          <a class="btn btn-ghost btn-lg" href="https://gitlab.prod.dtstack.cn/changlu/SqlParserX" target="_blank" rel="noopener">Star &amp; Fork</a>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer id="community" class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand">
            <svg class="brand-icon" viewBox="0 0 48 48" width="24" height="24" fill="none">
              <rect x="6" y="8" width="36" height="32" rx="7" stroke="#2563eb" stroke-width="3"/>
              <path d="M14 20h20M14 28h12" stroke="#2563eb" stroke-width="3" stroke-linecap="round"/>
              <circle cx="34" cy="28" r="2.5" fill="#2563eb"/>
            </svg>
            <span class="brand-name">SqlParserX</span>
          </div>
          <p class="footer-tag">多数据库方言 SQL 解析引擎</p>
          <p class="footer-copy">Apache License 2.0 · Copyright © 2026 changlu</p>
        </div>
        <div class="footer-cols">
          <div class="footer-col">
            <h4>产品</h4>
            <a href="#features">核心功能</a>
            <a href="#mcp">MCP Server</a>
            <a href="#start">立即体验</a>
          </div>
          <div class="footer-col">
            <h4>开发</h4>
            <a href="https://gitlab.prod.dtstack.cn/changlu/SqlParserX" target="_blank" rel="noopener">源码仓库</a>
            <a href="#docs">接入文档</a>
            <a href="#docs">Java SDK</a>
          </div>
          <div class="footer-col">
            <h4>社区</h4>
            <a href="#community">Issue</a>
            <a href="#community">贡献指南</a>
            <a href="#community">更新日志</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import MockupTerminal from '@/components/MockupTerminal.vue'
export default {
  name: 'Home',
  components: { MockupTerminal },
  created() {
    console.log("import.meta.env=>", import.meta.env)
    console.log("import.meta.env.VITE_API_URL=>", import.meta.env.VITE_API_URL)
  }
}
</script>

<style scoped>
.home {
  position: relative;
  overflow-x: hidden;
  background: #f7f8fa;
  color: #1f2937;
}

/* ===== 顶部导航 ===== */
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #e5e7eb;
}
.topbar-inner {
  max-width: 1160px;
  margin: 0 auto;
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 28px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  text-decoration: none;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 0.3px;
}

.nav-links {
  display: flex;
  gap: 26px;
  list-style: none;
  margin: 0;
  padding: 0;
  margin-left: 8px;
}
.nav-links a {
  color: #4b5563;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-links a:hover { color: #2563eb; }

.nav-cta {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

/* ===== 按钮 ===== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  color: #1f2937;
}
.btn-lg { padding: 11px 22px; font-size: 15px; }
.btn-primary {
  background: #2563eb;
  color: #fff;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}
.btn-primary:hover { background: #1d4ed8; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35); }
.btn-ghost {
  background: #fff;
  border-color: #d1d5db;
  color: #374151;
}
.btn-ghost:hover { border-color: #2563eb; color: #2563eb; }

/* ===== 容器 ===== */
.container {
  max-width: 1160px;
  margin: 0 auto;
  padding: 40px 24px 56px;
}

/* ===== HERO ===== */
.hero {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 56px;
  align-items: center;
  padding: 64px 0 56px;
}
.pill-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12.5px;
  color: #475569;
  background: #fff;
  border: 1px solid #e5e7eb;
}
.dot-live {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5);
  animation: pulse 2.2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5); }
  70% { box-shadow: 0 0 0 7px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.hero-title {
  font-size: clamp(44px, 6vw, 64px);
  font-weight: 800;
  letter-spacing: -0.5px;
  margin: 0 0 10px;
  color: #111827;
  line-height: 1.1;
}
.hero-subtitle {
  font-size: clamp(18px, 2vw, 22px);
  font-weight: 600;
  color: #2563eb;
  margin: 0 0 16px;
}
.hero-desc {
  font-size: 15.5px;
  color: #6b7280;
  line-height: 1.75;
  max-width: 520px;
  margin: 0 0 26px;
}
.hero-desc strong { color: #374151; font-weight: 600; }

.hero-cta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 26px;
}
.hero-cta.center { justify-content: center; }

.trust-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: #9ca3af;
  font-size: 13px;
}
.trust-item { position: relative; padding-left: 16px; }
.trust-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 6px;
  height: 6px;
  margin-top: -3px;
  border-radius: 50%;
  background: #2563eb;
}

.hero-right { display: flex; justify-content: center; }

/* ===== 方言横条 ===== */
.dialect-strip {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eef0f3;
  margin: 8px 0 48px;
  flex-wrap: wrap;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.strip-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 600;
  flex: none;
}
.chip-list { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  padding: 3px 11px;
  border-radius: 999px;
  font-size: 12.5px;
  color: #4b5563;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
}

/* ===== Section Head ===== */
.section-head {
  text-align: center;
  margin: 48px 0 30px;
}
.section-tag {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  letter-spacing: 1.5px;
  font-weight: 600;
  margin-bottom: 12px;
}
.section-title {
  font-size: clamp(26px, 3.5vw, 36px);
  font-weight: 700;
  margin: 0 0 8px;
  color: #111827;
  letter-spacing: 0.3px;
}
.section-sub { color: #6b7280; font-size: 15px; margin: 0; }

/* ===== 特性卡片 ===== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}
.feature-card {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 24px 22px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eef0f3;
  text-decoration: none;
  color: #1f2937;
  transition: border-color 0.25s, box-shadow 0.25s, transform 0.25s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.feature-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.08);
  transform: translateY(-3px);
}

.card-icon {
  flex: 0 0 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}
.icon-lineage { background: linear-gradient(135deg, #2563eb, #7c3aed); }
.icon-validate { background: linear-gradient(135deg, #059669, #10b981); }
.icon-table { background: linear-gradient(135deg, #d97706, #f59e0b); }
.icon-create { background: linear-gradient(135deg, #db2777, #f472b6); }
.icon-split { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.icon-mcp { background: linear-gradient(135deg, #0891b2, #06b6d4); }

.card-body { flex: 1; }
.card-body h3 { margin: 1px 0 7px; font-size: 16.5px; color: #111827; }
.card-body p { margin: 0 0 11px; color: #6b7280; font-size: 13.5px; line-height: 1.65; }
.card-tag {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  border: 1px solid #dbeafe;
}
.card-mcp { cursor: default; }

/* ===== 统计区 ===== */
.stats-section { margin: 64px 0 56px; }
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  padding: 28px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eef0f3;
}
.stat-item { text-align: center; }
.stat-num {
  font-size: clamp(30px, 3.5vw, 42px);
  font-weight: 800;
  color: #2563eb;
  margin-bottom: 4px;
}
.stat-label { color: #6b7280; font-size: 14px; }

/* ===== 最终 CTA ===== */
.final-cta {
  margin: 48px 0 8px;
  padding: 48px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff 0%, #f5f3ff 100%);
  border: 1px solid #e0e7ff;
  text-align: center;
}
.final-cta h2 {
  font-size: clamp(22px, 3vw, 30px);
  margin: 0 0 6px;
  color: #111827;
}
.final-cta p { color: #6b7280; margin: 0 0 22px; font-size: 15px; }
.final-cta .hero-cta { margin-bottom: 0; }

/* ===== Footer ===== */
.site-footer {
  border-top: 1px solid #e5e7eb;
  margin-top: 48px;
  padding: 40px 24px 20px;
  background: #fff;
}
.footer-inner {
  max-width: 1160px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 40px;
}
.footer-brand .brand { margin-bottom: 10px; }
.footer-brand .brand-name { font-size: 16px; }
.footer-tag { color: #6b7280; font-size: 13.5px; margin: 0 0 6px; }
.footer-copy { color: #9ca3af; font-size: 12.5px; margin: 0; }

.footer-cols { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.footer-col h4 { font-size: 14px; margin: 0 0 10px; color: #111827; }
.footer-col a {
  display: block;
  color: #6b7280;
  font-size: 13px;
  text-decoration: none;
  padding: 3px 0;
  transition: color 0.2s;
}
.footer-col a:hover { color: #2563eb; }

/* ===== 响应式 ===== */
@media (max-width: 960px) {
  .hero { grid-template-columns: 1fr; gap: 36px; padding: 44px 0 36px; }
  .nav-links { display: none; }
  .footer-inner { grid-template-columns: 1fr; }
  .footer-cols { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 640px) {
  .topbar-inner { gap: 10px; padding: 0 14px; }
  .container { padding: 24px 14px 40px; }
  .nav-cta .btn-ghost { display: none; }
  .footer-cols { grid-template-columns: 1fr 1fr; }
  .hero-cta .btn-lg { flex: 1; }
  .stats-section { margin: 40px 0 32px; }
  .final-cta { padding: 32px 16px; }
  .site-footer { padding: 28px 18px 16px; }
}
</style>