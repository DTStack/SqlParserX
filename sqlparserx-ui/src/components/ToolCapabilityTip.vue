<template>
  <aside class="tool-capability-tip" role="note">
    <span class="tip-mark" aria-hidden="true">i</span>
    <strong>核心能力</strong>
    <span class="tip-copy"><slot /></span>
  </aside>
</template>

<style scoped>
.tool-capability-tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  margin: 0 0 2px 12px;
  padding: 4px 9px;
  color: #52647a;
  background: #f0f3f6;
  border: 1px solid #dce3ea;
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.45;
  vertical-align: middle;
}

.tip-mark {
  display: inline-grid;
  flex: none;
  place-items: center;
  width: 14px;
  height: 14px;
  color: #fff;
  background: #58718d;
  border-radius: 50%;
  font: 700 9px/1 Georgia, serif;
}

.tool-capability-tip strong {
  flex: none;
  color: #334a63;
  font-weight: 700;
}

.tip-copy { min-width: 0; }

@media (max-width: 760px) {
  .tool-capability-tip {
    margin: 2px 0 0;
    align-items: flex-start;
  }
}
</style>
