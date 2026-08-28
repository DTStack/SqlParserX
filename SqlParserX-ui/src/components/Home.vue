<template>
  <div class="home">
    <!-- 顶部导航 -->
    <nav class="topbar">
      <div class="topbar-inner">
        <a class="brand" href="#top">
          <svg class="brand-icon" viewBox="0 0 48 48" width="32" height="32" fill="none">
            <defs>
              <linearGradient id="brand-grad" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#22d3ee"/>
                <stop offset="100%" stop-color="#8b5cf6"/>
              </linearGradient>
            </defs>
            <rect x="6" y="8" width="36" height="32" rx="7" stroke="url(#brand-grad)" stroke-width="3"/>
            <path d="M14 20h20M14 28h12" stroke="url(#brand-grad)" stroke-width="3" stroke-linecap="round"/>
            <circle cx="34" cy="28" r="2.5" fill="#22d3ee"/>
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
          <a class="btn btn-ghost" href="#docs">📘 文档</a>
          <a class="btn btn-primary" href="#start">🚀 立即体验</a>
        </div>
      </div>
    </nav>

    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <main class="container">
      <!-- HERO 区 -->
      <section id="top" class="hero">
        <div class="hero-left">
          <div class="pill-badges">
            <span class="pill"><span class="dot-pulse"></span>v1.0 · 稳定可用</span>
            <span class="pill">🔓 MIT 协议</span>
            <span class="pill">⚡ Druid 1.2.28</span>
          </div>

          <h1 class="hero-title">
            <span class="title-gradient">SqlParserX</span>
          </h1>
          <h2 class="hero-subtitle">多数据库方言 <span class="text-gradient">SQL 解析引擎</span></h2>

          <p class="hero-desc">
            一站式 <strong>表/字段血缘解析</strong>、<strong>SQL 切分</strong>、<strong>建表解析</strong>、<strong>语法校验</strong>
            与 <strong>行级权限注入</strong>，并提供 Web 可视化与 MCP Server，
            接入 AI Agent 与大模型应用开箱即用。
          </p>

          <div class="hero-cta">
            <router-link to="/lineage" class="btn btn-primary btn-lg">
              🚀 立即体验
            </router-link>
            <a class="btn btn-ghost btn-lg" href="https://gitlab.prod.dtstack.cn/changlu/SqlParserX" target="_blank" rel="noopener">
              <span class="btn-icon">⭐</span> GitHub
            </a>
          </div>

          <div class="trust-row">
            <span class="trust-item">✅ 15+ 方言解析器</span>
            <span class="trust-item">✅ 70+ 单元测试</span>
            <span class="trust-item">✅ Spring AI MCP</span>
            <span class="trust-item">✅ 活跃维护中</span>
          </div>
        </div>

        <div class="hero-right">
          <mockup-terminal></mockup-terminal>
        </div>
      </section>

      <!-- 客户/技术栈横条 -->
      <section class="logo-strip">
        <span class="logo-strip-label">支持方言</span>
        <div class="logo-list">
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
          <h2 class="section-title">为 <span class="text-gradient">数据血缘</span> 而生的解析能力</h2>
          <p class="section-sub">从单条 SQL 到多语句脚本，覆盖解析、可视化、接入的全链路能力</p>
        </div>

        <div class="features-grid">
          <router-link to="/lineage" class="feature-card">
            <div class="card-icon icon-lineage"><i class="el-icon-share"></i></div>
            <div class="card-body">
              <h3>血缘解析可视化</h3>
              <p>字段级与表级血缘双向切换，支持缩放、拖拽与全屏交互式探索。</p>
              <span class="card-tag">字段级 / 表级</span>
            </div>
            <div class="card-num">01</div>
          </router-link>

          <router-link to="/syntax-validator" class="feature-card">
            <div class="card-icon icon-validate"><i class="el-icon-circle-check"></i></div>
            <div class="card-body">
              <h3>语法解析验证</h3>
              <p>多方言语法校验，错误时输出精准错误信息与完整堆栈定位。</p>
              <span class="card-tag">多方言校验</span>
            </div>
            <div class="card-num">02</div>
          </router-link>

          <router-link to="/table-parser" class="feature-card">
            <div class="card-icon icon-table"><i class="el-icon-collection"></i></div>
            <div class="card-body">
              <h3>SQL 表解析</h3>
              <p>解析主表与全部真实表，自动展开 WITH AS / CTE / JOIN / UNION。</p>
              <span class="card-tag">真实表提取</span>
            </div>
            <div class="card-num">03</div>
          </router-link>

          <router-link to="/create-table-parser" class="feature-card">
            <div class="card-icon icon-create"><i class="el-icon-plus"></i></div>
            <div class="card-body">
              <h3>建表 SQL 解析</h3>
              <p>表名、字段、分区、分桶、键与 properties 全维度解析。</p>
              <span class="card-tag">结构级解析</span>
            </div>
            <div class="card-num">04</div>
          </router-link>

          <router-link to="/sql-splitter" class="feature-card">
            <div class="card-icon icon-split"><i class="el-icon-sort"></i></div>
            <div class="card-body">
              <h3>SQL 切分测试</h3>
              <p>将多语句脚本按语句智能切分，支持自定义与原生两种策略。</p>
              <span class="card-tag">脚本拆分</span>
            </div>
            <div class="card-num">05</div>
          </router-link>

          <div id="mcp" class="feature-card card-mcp">
            <div class="card-icon icon-mcp"><i class="el-icon-connection"></i></div>
            <div class="card-body">
              <h3>MCP Server 接入</h3>
              <p>基于 SSE 将解析能力注册为 MCP 工具，AI Agent 直接调用。</p>
              <span class="card-tag">LLM 生态</span>
            </div>
            <div class="card-num">✦</div>
          </div>
        </div>
      </section>

      <!-- 数据展示行 -->
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
            <div class="stat-num">MIT</div>
            <div class="stat-label">开源协议</div>
          </div>
        </div>
      </section>

      <!-- 最终 CTA -->
      <section id="start" class="final-cta">
        <div class="final-cta-inner">
          <div class="final-orb"></div>
          <h2>准备好开始解析你的 SQL 了吗？</h2>
          <p>一键启动 Web 服务，或通过 MCP 把能力嵌入到 AI Agent</p>
          <div class="hero-cta" style="justify-content:center;">
            <router-link to="/lineage" class="btn btn-primary btn-lg">🚀 进入体验</router-link>
            <a class="btn btn-ghost btn-lg" href="https://gitlab.prod.dtstack.cn/changlu/SqlParserX" target="_blank" rel="noopener">⭐ Star & Fork</a>
          </div>
        </div>
      </section>
    </main>

    <!-- Footer -->
    <footer id="community" class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="brand">
            <svg class="brand-icon" viewBox="0 0 48 48" width="28" height="28" fill="none">
              <rect x="6" y="8" width="36" height="32" rx="7" stroke="url(#brand-grad)" stroke-width="3"/>
              <path d="M14 20h20M14 28h12" stroke="url(#brand-grad)" stroke-width="3" stroke-linecap="round"/>
              <circle cx="34" cy="28" r="2.5" fill="#22d3ee"/>
            </svg>
            <span class="brand-name">SqlParserX</span>
          </div>
          <p class="footer-tag">多数据库方言 SQL 解析引擎</p>
          <p class="footer-copy">MIT License · Copyright © 2026 changlu</p>
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
  min-height: 100vh;
  overflow-x: hidden;
}

/* ===== 顶部导航 ===== */
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(11, 16, 32, 0.7);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(99, 102, 241, 0.18);
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 28px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}
.brand-icon { filter: drop-shadow(0 0 8px rgba(139,92,246,0.5)); }
.brand-name {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(120deg, #a5b4fc, #22d3ee);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 24px;
  list-style: none;
  margin: 0;
  padding: 0;
  margin-left: 8px;
}
.nav-links a {
  color: #cbd5e1;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-links a:hover { color: #fff; }

.nav-cta {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

/* ===== 背景装饰 ===== */
.bg-decor { position: absolute; inset: 0; z-index: -1; pointer-events: none; overflow: hidden; }
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.55;
  animation: float 14s ease-in-out infinite;
}
.orb-1 { width: 460px; height: 460px; background: rgba(99,102,241,0.55); top: -160px; right: -100px; }
.orb-2 { width: 420px; height: 420px; background: rgba(34,211,238,0.35); top: 380px; left: -160px; animation-delay: -5s; }
.orb-3 { width: 340px; height: 340px; background: rgba(139,92,246,0.4); top: 1100px; right: 12%; animation-delay: -9s; }
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(28px, -32px) scale(1.06); }
}
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99,102,241,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99,102,241,0.06) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse at 50% 0%, black 35%, transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at 50% 0%, black 35%, transparent 75%);
}

/* ===== 通用按钮 ===== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s, border-color 0.2s;
  color: #e2e8f0;
}
.btn-lg { padding: 12px 24px; font-size: 15px; border-radius: 12px; }
.btn-primary {
  background: linear-gradient(135deg, #6366f1, #22d3ee);
  color: #fff;
  box-shadow: 0 8px 24px rgba(99,102,241,0.4);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(99,102,241,0.55);
}
.btn-ghost {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.4);
  color: #e2e8f0;
}
.btn-ghost:hover {
  background: rgba(99, 102, 241, 0.22);
  border-color: rgba(34, 211, 238, 0.6);
  color: #fff;
}

/* ===== 容器 ===== */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}

/* ===== HERO ===== */
.hero {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 56px;
  align-items: center;
  padding: 56px 0 72px;
}
.pill-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}
.pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: #c7d2fe;
  background: rgba(99, 102, 241, 0.14);
  border: 1px solid rgba(99, 102, 241, 0.4);
}
.dot-pulse {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.7);
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.7); }
  70% { box-shadow: 0 0 0 8px rgba(52, 211, 153, 0); }
  100% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0); }
}

.hero-title {
  font-size: clamp(52px, 8vw, 88px);
  font-weight: 900;
  letter-spacing: -1px;
  margin: 0 0 12px;
  line-height: 1;
}
.title-gradient {
  background: linear-gradient(110deg, #a5b4fc 0%, #22d3ee 40%, #8b5cf6 75%, #c4b5fd 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 7s linear infinite;
  filter: drop-shadow(0 0 28px rgba(99,102,241,0.45));
}
@keyframes shine { to { background-position: 200% center; } }

.hero-subtitle {
  font-size: clamp(20px, 2.4vw, 28px);
  font-weight: 700;
  color: #e2e8f0;
  margin: 0 0 18px;
  letter-spacing: 0.5px;
}
.text-gradient {
  background: linear-gradient(120deg, #22d3ee, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: 16px;
  color: #94a3b8;
  line-height: 1.75;
  max-width: 560px;
  margin: 0 0 28px;
}
.hero-desc strong {
  color: #e2e8f0;
  font-weight: 600;
}

.hero-cta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 28px;
}

.trust-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: #94a3b8;
  font-size: 13.5px;
}
.trust-item {
  position: relative;
  padding-left: 18px;
}
.trust-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 8px;
  height: 8px;
  margin-top: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #22d3ee);
}

.hero-right { display: flex; justify-content: center; }

/* ===== 客户横条 ===== */
.logo-strip {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(99, 102, 241, 0.18);
  backdrop-filter: blur(8px);
  margin: 24px 0 56px;
  flex-wrap: wrap;
}
.logo-strip-label {
  font-size: 13px;
  color: #94a3b8;
  letter-spacing: 1px;
  font-weight: 600;
  flex: none;
}
.logo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.28);
}

/* ===== Section Head ===== */
.section-head {
  text-align: center;
  margin: 56px 0 32px;
}
.section-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 12px;
  color: #22d3ee;
  background: rgba(34, 211, 238, 0.1);
  border: 1px solid rgba(34, 211, 238, 0.35);
  letter-spacing: 1.5px;
  font-weight: 700;
  margin-bottom: 14px;
}
.section-title {
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 800;
  margin: 0 0 10px;
  color: #f1f5f9;
  letter-spacing: 0.5px;
}
.section-sub {
  color: #94a3b8;
  font-size: 15px;
  margin: 0;
}

/* ===== 特性卡片 ===== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 22px;
}
.feature-card {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 26px 22px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(255,255,255,0.06), rgba(255,255,255,0.02));
  border: 1px solid rgba(99, 102, 241, 0.28);
  backdrop-filter: blur(10px);
  text-decoration: none;
  color: var(--px-text);
  overflow: hidden;
  transition: transform 0.35s ease, box-shadow 0.35s ease, border-color 0.35s ease;
}
.feature-card::before {
  content: '';
  position: absolute;
  top: 0; left: -60%;
  width: 50%; height: 100%;
  background: linear-gradient(100deg, transparent, rgba(255,255,255,0.12), transparent);
  transform: skewX(-20deg);
  transition: left 0.6s ease;
}
.feature-card:hover {
  transform: translateY(-6px);
  border-color: rgba(34, 211, 238, 0.5);
  box-shadow: 0 16px 40px rgba(99,102,241,0.3), 0 0 24px rgba(34,211,238,0.15);
}
.feature-card:hover::before { left: 120%; }

.card-icon {
  flex: 0 0 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
}
.icon-lineage { background: linear-gradient(135deg, #6366f1, #22d3ee); box-shadow: 0 6px 18px rgba(99,102,241,0.4); }
.icon-validate { background: linear-gradient(135deg, #10b981, #34d399); box-shadow: 0 6px 18px rgba(16,185,129,0.4); }
.icon-table { background: linear-gradient(135deg, #f59e0b, #fbbf24); box-shadow: 0 6px 18px rgba(245,158,11,0.4); }
.icon-create { background: linear-gradient(135deg, #ec4899, #f472b6); box-shadow: 0 6px 18px rgba(236,72,153,0.4); }
.icon-split { background: linear-gradient(135deg, #8b5cf6, #a78bfa); box-shadow: 0 6px 18px rgba(139,92,246,0.4); }
.icon-mcp { background: linear-gradient(135deg, #06b6d4, #22d3ee); box-shadow: 0 6px 18px rgba(6,182,212,0.4); }

.card-body { flex: 1; }
.card-body h3 { margin: 2px 0 8px; font-size: 18px; color: #f1f5f9; }
.card-body p { margin: 0 0 12px; color: #94a3b8; font-size: 13.5px; line-height: 1.65; }
.card-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #a5b4fc;
  background: rgba(99,102,241,0.12);
  border: 1px solid rgba(99,102,241,0.35);
}
.card-num {
  position: absolute;
  top: 14px;
  right: 18px;
  font-size: 30px;
  font-weight: 800;
  color: rgba(148,163,184,0.14);
  letter-spacing: 1px;
}
.card-mcp { cursor: default; }

/* ===== 数据展示 ===== */
.stats-section { margin: 80px 0 60px; }
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  padding: 32px;
  border-radius: 20px;
  background: linear-gradient(160deg, rgba(255,255,255,0.05), rgba(255,255,255,0.02));
  border: 1px solid rgba(99, 102, 241, 0.28);
  backdrop-filter: blur(10px);
}
.stat-item { text-align: center; }
.stat-num {
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 900;
  background: linear-gradient(120deg, #22d3ee, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 6px;
}
.stat-label { color: #94a3b8; font-size: 14px; }

/* ===== 最终 CTA ===== */
.final-cta {
  position: relative;
  margin: 60px 0 40px;
  padding: 60px 24px;
  border-radius: 24px;
  background: linear-gradient(160deg, rgba(99,102,241,0.18), rgba(139,92,246,0.12));
  border: 1px solid rgba(99, 102, 241, 0.35);
  overflow: hidden;
}
.final-cta-inner { position: relative; text-align: center; }
.final-orb {
  position: absolute;
  inset: -40px;
  background: radial-gradient(closest-side, rgba(34,211,238,0.25), transparent 70%);
  z-index: 0;
  pointer-events: none;
}
.final-cta h2 {
  position: relative;
  font-size: clamp(24px, 3.4vw, 34px);
  margin: 0 0 8px;
  color: #f8fafc;
}
.final-cta p { position: relative; color: #cbd5e1; margin: 0 0 24px; }
.final-cta .hero-cta { position: relative; }

/* ===== Footer ===== */
.site-footer {
  border-top: 1px solid rgba(99, 102, 241, 0.2);
  margin-top: 60px;
  padding: 48px 24px 32px;
  background: rgba(8, 12, 28, 0.6);
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 40px;
}
.footer-brand .brand { margin-bottom: 12px; }
.footer-tag { color: #94a3b8; font-size: 14px; margin: 0 0 8px; }
.footer-copy { color: #64748b; font-size: 13px; margin: 0; }

.footer-cols {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.footer-col h4 {
  font-size: 14px;
  margin: 0 0 12px;
  color: #f1f5f9;
  letter-spacing: 0.5px;
}
.footer-col a {
  display: block;
  color: #94a3b8;
  font-size: 13.5px;
  text-decoration: none;
  padding: 4px 0;
  transition: color 0.2s;
}
.footer-col a:hover { color: #67e8f9; }

/* ===== 响应式 ===== */
@media (max-width: 960px) {
  .hero { grid-template-columns: 1fr; gap: 36px; padding: 40px 0 48px; }
  .nav-links { display: none; }
  .footer-inner { grid-template-columns: 1fr; }
  .footer-cols { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 640px) {
  .topbar-inner { gap: 12px; padding: 0 14px; }
  .container { padding: 24px 14px 56px; }
  .nav-cta .btn-ghost { display: none; }
  .footer-cols { grid-template-columns: 1fr 1fr; }
  .pill-badges { gap: 6px; }
  .pill { padding: 4px 10px; font-size: 12px; }
  .hero-cta .btn-lg { flex: 1; }
}
</style>