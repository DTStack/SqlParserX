<template>
  <div class="demo-sql-selector">
    <div class="demo-sql-heading">
      <span class="demo-sql-label">场景示例</span>
      <span class="demo-sql-hint">按当前数据源匹配 SQL，载入后会替换编辑器内容</span>
    </div>
    <div class="demo-sql-controls">
      <select v-model="selectedDemoId" :disabled="!demos.length" :aria-label="`${toolLabel}示例场景`">
        <option v-if="!demos.length" value="">当前数据源暂无示例</option>
        <option v-for="demo in demos" :key="demo.id" :value="demo.id">
          {{ demo.title }} · {{ demo.sourceLabel }}
        </option>
      </select>
      <button type="button" :disabled="!selectedDemo" @click="loadDemo">载入示例</button>
    </div>
  </div>
</template>

<script>
import { getSqlDemoOptions, getSqlToolLabel } from '@/demos/sqlDemoCatalog'

export default {
  name: 'DemoSqlSelector',
  props: {
    tool: { type: String, required: true },
    dataSources: { type: Array, default: () => [] },
    selectedDataSource: { type: [Number, String], default: null }
  },
  data() {
    return { selectedDemoId: '' }
  },
  computed: {
    selectedDialect() {
      const source = this.dataSources.find(item => String(item.value) === String(this.selectedDataSource))
      return source ? source.label : ''
    },
    demos() {
      return getSqlDemoOptions(this.tool, this.selectedDialect)
    },
    selectedDemo() {
      return this.demos.find(demo => demo.id === this.selectedDemoId) || this.demos[0] || null
    },
    toolLabel() {
      return getSqlToolLabel(this.tool)
    }
  },
  watch: {
    demos: {
      immediate: true,
      handler(options) {
        if (!options.some(demo => demo.id === this.selectedDemoId)) {
          this.selectedDemoId = options.length ? options[0].id : ''
        }
      }
    }
  },
  methods: {
    loadDemo() {
      if (this.selectedDemo) this.$emit('load', this.selectedDemo.sql)
    }
  }
}
</script>

<style scoped>
.demo-sql-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 0 12px;
  padding: 10px 12px;
  border: 1px solid #e5edf7;
  border-radius: 10px;
  background: #f8fbff;
}

.demo-sql-heading {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.demo-sql-label {
  color: #334155;
  font-size: 13px;
  font-weight: 600;
}

.demo-sql-hint {
  color: #8492a6;
  font-size: 11px;
}

.demo-sql-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: none;
}

.demo-sql-controls select {
  width: min(280px, 32vw);
  min-width: 150px;
  height: 34px;
  padding: 0 9px;
  border: 1px solid #d7e0eb;
  border-radius: 7px;
  color: #334155;
  background: #fff;
  font-size: 12px;
}

.demo-sql-controls button {
  height: 34px;
  padding: 0 13px;
  border: 0;
  border-radius: 7px;
  color: #fff;
  background: #2563eb;
  font-size: 12px;
  cursor: pointer;
}

.demo-sql-controls button:disabled {
  background: #aab6c5;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .demo-sql-selector {
    align-items: stretch;
    flex-direction: column;
  }

  .demo-sql-controls select {
    width: 100%;
    min-width: 0;
    flex: 1;
  }
}
</style>
