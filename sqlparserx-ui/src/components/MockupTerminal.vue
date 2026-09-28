<template>
  <div class="mockup-terminal">
    <div class="terminal-glow"></div>
    <div class="terminal-frame">
      <div class="terminal-header">
        <div class="dots">
          <span class="dot dot-red"></span>
          <span class="dot dot-yellow"></span>
          <span class="dot dot-green"></span>
        </div>
        <div class="terminal-title">SqlParserX · Lineage Analyzer</div>
        <div class="terminal-meta">bash — 96×24</div>
      </div>
      <div class="terminal-body">
        <div class="line">
          <span class="prompt">$</span>
          <span class="cmd">parse-lineage</span>
          <span class="flag">--sql</span>
          <span class="string">"INSERT INTO result (id, name) SELECT a.id, a.name FROM users a JOIN orders o ON a.id = o.user_id"</span>
        </div>
        <div class="line line-out">
          <span class="ok">✓</span> Target Table&nbsp;&nbsp;: <span class="hl">result</span>
        </div>
        <div class="line line-out">
          <span class="ok">✓</span> Source Tables: <span class="hl">users</span>, <span class="hl">orders</span>
        </div>
        <div class="line line-out">
          <span class="ok">✓</span> Column Lineage:
        </div>
        <div class="line line-out indent">
          <span class="src">users.id</span>
          <span class="arrow">→</span>
          <span class="dst">result.id</span>
        </div>
        <div class="line line-out indent">
          <span class="src">users.name</span>
          <span class="arrow">→</span>
          <span class="dst">result.name</span>
        </div>
        <div class="line line-out indent">
          <span class="src">orders.user_id</span>
          <span class="arrow">→</span>
          <span class="dst">result.id</span>
        </div>
        <div class="line line-out">
          <span class="ok">✓</span> Generated in <span class="hl-em">12 ms</span>
        </div>
        <div class="line typing">
          <span class="prompt">$</span>
          <span class="cursor">▍</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MockupTerminal'
}
</script>

<style scoped>
.mockup-terminal {
  position: relative;
  width: 100%;
  max-width: 580px;
}

.terminal-glow {
  position: absolute;
  inset: -30px -40px;
  background: radial-gradient(closest-side, rgba(99,102,241,0.25), transparent 70%);
  filter: blur(40px);
  z-index: -1;
  opacity: 0.6;
}

.terminal-frame {
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(180deg, #13132e 0%, #0d0d24 100%);
  border: 1px solid rgba(99, 102, 241, 0.22);
  box-shadow:
    0 24px 64px rgba(17, 24, 39, 0.16),
    0 2px 8px rgba(17, 24, 39, 0.06);
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.7;
  color: #cbd5e1;
  position: relative;
}

.terminal-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.dots {
  display: flex;
  gap: 7px;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.dot-red { background: #f87171; }
.dot-yellow { background: #fbbf24; }
.dot-green { background: #34d399; }

.terminal-title {
  flex: 1;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  letter-spacing: 0.5px;
}
.terminal-meta {
  font-size: 12px;
  color: #64748b;
}

.terminal-body {
  padding: 22px 22px 26px;
  overflow: hidden;
}

.line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.line + .line { margin-top: 4px; }
.line-out { padding-left: 0; }
.line.indent { padding-left: 24px; }

.prompt { color: #22d3ee; margin-right: 10px; font-weight: 700; }
.cmd { color: #c4b5fd; font-weight: 600; }
.flag { color: #a5b4fc; margin-left: 6px; }
.string { color: #f9a8d4; }
.ok { color: #34d399; margin-right: 8px; font-weight: 700; }
.hl { color: #67e8f9; font-weight: 600; }
.hl-em { color: #fbbf24; font-weight: 700; }
.src { color: #93c5fd; }
.arrow { color: #8b5cf6; margin: 0 10px; font-weight: 700; }
.dst { color: #c4b5fd; font-weight: 600; }

.cursor {
  color: #22d3ee;
  animation: blink 1s steps(1) infinite;
  font-weight: 700;
}
@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

@media (max-width: 768px) {
  .terminal-frame { font-size: 12.5px; }
  .terminal-body { padding: 16px; }
}
</style>