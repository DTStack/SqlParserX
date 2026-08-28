<template>
  <div class="home">
    <!-- 背景装饰光斑 -->
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    <div class="grid-overlay"></div>

    <div class="hero">
      <div class="logo-badge">
        <svg class="logo-icon" viewBox="0 0 48 48" width="64" height="64" fill="none">
          <defs>
            <linearGradient id="lg" x1="0" y1="0" x2="48" y2="48">
              <stop offset="0%" stop-color="#22d3ee"/>
              <stop offset="100%" stop-color="#8b5cf6"/>
            </linearGradient>
          </defs>
          <rect x="6" y="8" width="36" height="32" rx="7" stroke="url(#lg)" stroke-width="3"/>
          <path d="M14 20h20M14 28h12" stroke="url(#lg)" stroke-width="3" stroke-linecap="round"/>
          <circle cx="34" cy="28" r="2.5" fill="#22d3ee"/>
        </svg>
      </div>
      <h1 class="logo-title">SqlParserX</h1>
      <p class="tagline">多数据库方言 SQL 解析引擎 · 表 / 血缘 / 建表 / 切分 / 校验</p>
      <div class="hero-stats">
        <span class="stat-pill">15+ 方言解析器</span>
        <span class="stat-pill">表 · 字段血缘</span>
        <span class="stat-pill">MCP Server</span>
      </div>
    </div>

    <div class="features">
      <router-link to="/lineage" class="feature-card">
        <div class="card-icon icon-lineage">
          <i class="el-icon-share"></i>
        </div>
        <div class="card-body">
          <h3>血缘解析可视化</h3>
          <p>可视化展示 SQL 中表与字段的血缘关系，支持缩放、拖拽与全屏。</p>
          <span class="card-tag">字段级 / 表级</span>
        </div>
        <div class="card-num">01</div>
      </router-link>

      <router-link to="/syntax-validator" class="feature-card">
        <div class="card-icon icon-validate">
          <i class="el-icon-circle-check"></i>
        </div>
        <div class="card-body">
          <h3>语法解析验证</h3>
          <p>快速校验 SQL 语法正确性，错误时返回详细定位与堆栈信息。</p>
          <span class="card-tag">多方言校验</span>
        </div>
        <div class="card-num">02</div>
      </router-link>

      <router-link to="/table-parser" class="feature-card">
        <div class="card-icon icon-table">
          <i class="el-icon-collection"></i>
        </div>
        <div class="card-body">
          <h3>SQL 表解析</h3>
          <p>解析 SQL 中的主表与全部真实表，支持 WITH AS / CTE / JOIN / UNION。</p>
          <span class="card-tag">真实表提取</span>
        </div>
        <div class="card-num">03</div>
      </router-link>

      <router-link to="/create-table-parser" class="feature-card">
        <div class="card-icon icon-create">
          <i class="el-icon-plus"></i>
        </div>
        <div class="card-body">
          <h3>建表 SQL 解析</h3>
          <p>解析 CREATE TABLE 的表名、字段、分区、分桶、键与 properties。</p>
          <span class="card-tag">结构级解析</span>
        </div>
        <div class="card-num">04</div>
      </router-link>

      <router-link to="/sql-splitter" class="feature-card">
        <div class="card-icon icon-split">
          <i class="el-icon-sort"></i>
        </div>
        <div class="card-body">
          <h3>SQL 切分测试</h3>
          <p>将含多条语句的 SQL 脚本按语句智能切分为独立 SQL。</p>
          <span class="card-tag">脚本拆分</span>
        </div>
        <div class="card-num">05</div>
      </router-link>

      <div class="feature-card card-about">
        <div class="card-icon icon-mcp">
          <i class="el-icon-connection"></i>
        </div>
        <div class="card-body">
          <h3>MCP Server 接入</h3>
          <p>基于 SSE 将 SQL 解析能力注册为 MCP 工具，供 AI Agent 直接调用。</p>
          <span class="card-tag">LLM 生态</span>
        </div>
        <div class="card-num">✦</div>
      </div>
    </div>

    <footer class="home-footer">SqlParserX · 基于官方 Druid 1.2.28 构建</footer>
  </div>
</template>

<script>
export default {
  name: 'Home',
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
  padding: 72px 24px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 背景装饰 */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.55;
  pointer-events: none;
  animation: float 12s ease-in-out infinite;
}
.orb-1 { width: 420px; height: 420px; background: rgba(99,102,241,0.5); top: -120px; right: -80px; }
.orb-2 { width: 380px; height: 380px; background: rgba(34,211,238,0.35); bottom: 8%; left: -120px; animation-delay: -4s; }
.orb-3 { width: 300px; height: 300px; background: rgba(139,92,246,0.4); top: 45%; right: 12%; animation-delay: -8s; }
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(24px, -28px) scale(1.08); }
}
.grid-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(99,102,241,0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99,102,241,0.07) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse at 50% 0%, black 30%, transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at 50% 0%, black 30%, transparent 75%);
}

/* Hero 区 */
.hero {
  position: relative;
  z-index: 2;
  text-align: center;
  margin-top: 24px;
}
.logo-badge {
  display: inline-flex;
  padding: 14px;
  border-radius: 24px;
  background: rgba(255,255,255,0.04);
  border: 1px solid var(--px-border);
  box-shadow: 0 0 42px rgba(99,102,241,0.35), inset 0 0 22px rgba(34,211,238,0.12);
  margin-bottom: 18px;
}
.logo-icon {
  filter: drop-shadow(0 0 14px rgba(139,92,246,0.6));
}
.logo-title {
  font-size: clamp(44px, 7vw, 72px);
  font-weight: 800;
  letter-spacing: 2px;
  margin: 0;
  background: linear-gradient(120deg, #a5b4fc 0%, #22d3ee 40%, #8b5cf6 80%, #c4b5fd 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 6s linear infinite;
}
@keyframes shine {
  to { background-position: 200% center; }
}
.tagline {
  margin: 16px auto 20px;
  color: var(--px-text-dim);
  font-size: 17px;
  letter-spacing: 0.5px;
}
.hero-stats {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}
.stat-pill {
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 13px;
  color: #c7d2fe;
  background: rgba(99,102,241,0.14);
  border: 1px solid rgba(99,102,241,0.4);
  backdrop-filter: blur(6px);
}

/* 功能卡片 */
.features {
  position: relative;
  z-index: 2;
  margin-top: 56px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 22px;
  max-width: 1080px;
  width: 100%;
}
.feature-card {
  position: relative;
  display: flex;
  gap: 16px;
  padding: 24px 22px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(255,255,255,0.07), rgba(255,255,255,0.02));
  border: 1px solid var(--px-border);
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
  border-color: var(--px-border-light);
  box-shadow: 0 16px 40px rgba(99,102,241,0.28), 0 0 24px rgba(34,211,238,0.14);
}
.feature-card:hover::before {
  left: 120%;
}
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
.card-body h3 {
  margin: 2px 0 8px;
  font-size: 18px;
  color: #f1f5f9;
}
.card-body p {
  margin: 0 0 12px;
  color: var(--px-text-dim);
  font-size: 13.5px;
  line-height: 1.65;
}
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
.card-about { cursor: default; }

.home-footer {
  position: relative;
  z-index: 2;
  margin-top: 56px;
  color: rgba(148,163,184,0.7);
  font-size: 13px;
}

@media (max-width: 768px) {
  .home { padding: 48px 16px 32px; }
  .features { grid-template-columns: 1fr; margin-top: 40px; }
}
</style>
