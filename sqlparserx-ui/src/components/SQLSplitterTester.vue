<template>
  <div class="sql-splitter-tester">
    <div class="header">
      <h2>SQL切分测试工具</h2>
      <tool-capability-tip>依据所选方言将多语句脚本切分为独立 SQL，便于核对语句边界和切分结果。</tool-capability-tip>
    </div>

    <div class="main-container">
      <div class="sql-panel">
        <div class="panel-header">
          <div class="panel-title">SQL输入</div>
          <div class="data-source-selector">
            <label for="data-source">数据源:</label>
            <select id="data-source" v-model.number="selectedDataSource" :disabled="loadingDataSources || !dataSources.length">
              <option v-if="!dataSources.length" disabled value="">{{ loadingDataSources ? '正在加载…' : '暂无可用数据源' }}</option>
              <option v-for="source in dataSources"
                      :key="source.value"
                      :value="source.value">
                {{ source.label }}
              </option>
            </select>
          </div>
        </div>

        <demo-sql-selector
            tool="sql-splitter"
            :data-sources="dataSources"
            :selected-data-source="selectedDataSource"
            @load="sqlInput = $event"
        />

        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入多条SQL语句..."
          ></textarea>
        </div>

        <div class="sql-actions">
          <button id="split-btn" :disabled="loadingDataSources || !dataSources.length" @click="splitSQL">切分SQL</button>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-title">切分结果</div>
        <div class="result-container">
          <div v-for="(sql, index) in splitResults" :key="index" class="sql-item">
            <div class="sql-index">SQL {{ index + 1 }}:</div>
            <textarea
                class="result-sql"
                :value="sql"
                readonly
            ></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { splitSQL } from '@/api/sqlparser'
import { loadDataSourceOptions } from '@/api/DatasourceApi'
import { getSqlDemoOptions } from '@/demos/sqlDemoCatalog'
import ToolCapabilityTip from '@/components/ToolCapabilityTip.vue'
import DemoSqlSelector from '@/components/DemoSqlSelector.vue'

const commonDemo = getSqlDemoOptions('sql-splitter', 'COMMON')[0]

export default {
  name: 'SQLSplitterTester',
  components: { ToolCapabilityTip, DemoSqlSelector },
  data() {
    return {
      sqlInput: commonDemo ? commonDemo.sql : '',
      selectedDataSource: null,
      dataSources: [],
      loadingDataSources: true,
      splitResults: [],
      API_BASE_URL: import.meta.env.VITE_API_URL,
    };
  },
  mounted() {
    this.fetchDataSourceTypes();
  },
  methods: {
    async splitSQL() {
      const sql = this.sqlInput.trim();
      if (!sql) {
        this.$message.error('请输入SQL语句');
        return;
      }

      try {
        const response = await splitSQL(sql, this.selectedDataSource);
        if (response.code === 200) {
          this.splitResults = response.data;
          this.$message.success('SQL切分成功');
        } else {
          this.$message.error(response.msg || 'SQL切分失败');
        }
      } catch (error) {
        console.error('切分失败:', error);
        this.$message.error('切分失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    async fetchDataSourceTypes() {
      try {
        const { options, selectedValue } = await loadDataSourceOptions('ORACLE')
        this.dataSources = options
        this.selectedDataSource = selectedValue
        this.loadDefaultDemo()
      } catch (error) {
        console.error('Error fetching data source types:', error)
        this.dataSources = []
        this.selectedDataSource = null
        this.$message.error('数据源列表加载失败，请检查服务连接')
      } finally {
        this.loadingDataSources = false
      }
    },

    loadDefaultDemo() {
      const source = this.dataSources.find(item => String(item.value) === String(this.selectedDataSource))
      const demo = getSqlDemoOptions('sql-splitter', source ? source.label : 'COMMON')[0]
      if (demo) this.sqlInput = demo.sql
    },
  }
};
</script>

<style scoped>
.sql-splitter-tester {
  height: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sql-panel {
  flex: 0 0 400px;
  background-color: #ffffff;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px;
  overflow: hidden;
  max-height: 77vh;
  display: flex;
  flex-direction: column;
}

.header {
  margin-bottom: -10px;
  color: #1f2937;
  padding: 0 20px;
}

.main-container {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
  padding: 20px;
  box-sizing: border-box;
}

.result-panel {
  flex: 1;
  background-color: #ffffff;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px;
  position: relative;
  overflow: hidden;
  min-height: 0;
  display: flex;
  flex-direction: column;
  max-height: 77vh;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.data-source-selector {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}

.data-source-selector label {
  font-size: 14px;
  color: #4b5563;
}

.data-source-selector select {
  width: 154px;
  max-width: 46vw;
  min-width: 0;
  height: 34px;
  box-sizing: border-box;
  padding: 0 30px 0 10px;
  color: #35465b;
  background-color: #fff;
  background-image: linear-gradient(45deg, transparent 50%, #526b87 50%), linear-gradient(135deg, #526b87 50%, transparent 50%);
  background-position: calc(100% - 12px) 14px, calc(100% - 8px) 14px;
  background-repeat: no-repeat;
  background-size: 4px 4px;
  border: 1px solid #c7d0db;
  border-radius: 6px;
  appearance: none;
  -webkit-appearance: none;
  font: inherit;
  font-size: 13px;
  cursor: pointer;
}

.data-source-selector select:focus {
  outline: none;
  border-color: #496e93;
  box-shadow: 0 0 0 2px rgba(73, 110, 147, .12);
}

.data-source-selector select:disabled {
  color: #8994a2;
  background-color: #f0f2f5;
  cursor: wait;
}

.sql-input-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  flex: 1;
}

#sql-input {
  width: 100%;
  flex: 1;
  padding: 12px;
  border: none;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
  min-height: 150px;
}

#sql-input:focus {
  outline: none;
  border-color: #2563eb;
}

#split-btn {
  align-self: flex-end;
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.sql-input-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

#split-btn:hover {
  background: #1d4ed8;
}

.result-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.sql-item {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f3f4f6;
  border-radius: 4px;
}

.sql-index {
  font-weight: bold;
  margin-bottom: 10px;
  color: #2563eb;
}

.result-sql {
  width: 100%;
  min-height: 100px;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  resize: vertical;
  background-color: #ffffff;
}


.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #1f2937;
  margin-top: 7px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .main-container {
    flex-direction: column;
  }

  .sql-panel {
    flex: 0 0 auto;
    max-height: 50vh;
  }
}


/* ===== SqlParserX 主题增强 ===== */
.header h2 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #111827;
  display: inline-block;
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}
.panel-title::before {
  content: '';
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #2563eb;
  flex: none;
}
#sql-input {
  color: #1f2937;
  caret-color: #2563eb;
}
#sql-input::placeholder {
  color: #9ca3af;
}
.result-sql {
  color: #1f2937;
}
.json-result {
  color: #1f2937;
}
</style>
