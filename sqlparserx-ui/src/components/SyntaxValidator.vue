<template>
  <div class="syntax-validator">
    <div class="header">
      <h2>在线语法解析验证工具</h2>
      <tool-capability-tip>按所选数据源的语法规则解析 SQL，返回验证结果和错误信息；可识别范围随方言而异。</tool-capability-tip>
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
            tool="syntax-validator"
            :data-sources="dataSources"
            :selected-data-source="selectedDataSource"
            @load="sqlInput = $event"
        />

        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入SQL语句进行语法验证..."
          ></textarea>
        </div>

        <div class="sql-actions">
          <button id="validate-btn" :disabled="loadingDataSources || !dataSources.length" @click="validateSQL">验证语法</button>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-title">验证结果</div>
        <div class="result-container">
          <div v-if="validationResult" class="result-content">
            <div v-if="validationResult.code === 200" class="success-message">
              <i class="el-icon-success"></i> SQL语法验证通过
              <div class="success-details" v-if="validationResult.msg">
                {{ validationResult.msg }}
              </div>
            </div>
            <div v-else class="error-message">
              <i class="el-icon-error"></i> SQL语法错误
              <div class="error-details" v-if="validationResult.msg">
                {{ validationResult.msg }}
              </div>
              <!-- 添加堆栈信息显示 -->
              <div class="stack-trace" v-if="validationResult.data">
                <pre>{{ validationResult.data }}</pre>
              </div>
            </div>
          </div>
          <div v-else class="empty-result">
            请先输入SQL并点击验证按钮
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { validateSQL } from '@/api/sqlparser'
import { loadDataSourceOptions } from '@/api/DatasourceApi'
import ToolCapabilityTip from '@/components/ToolCapabilityTip.vue'
import DemoSqlSelector from '@/components/DemoSqlSelector.vue'

export default {
  name: 'SyntaxValidator',
  components: { ToolCapabilityTip, DemoSqlSelector },
  data() {
    return {
      sqlInput: '',
      selectedDataSource: null,
      dataSources: [],
      loadingDataSources: true,
      validationResult: null,
      API_BASE_URL: import.meta.env.VITE_API_URL,
    };
  },
  mounted() {
    this.fetchDataSourceTypes();
  },
  methods: {
    async validateSQL() {
      const sql = this.sqlInput.trim();
      if (!sql) {
        this.$message.error('请输入SQL语句');
        return;
      }

      try {
        this.validationResult = await validateSQL(sql, this.selectedDataSource);
      } catch (error) {
        console.error("validationResult=>", validationResult)
        console.error('验证失败:', error);
        this.$message.error('验证失败: ' + error.message);
        this.validationResult = {
          code: 500,
          msg: error.message,
          data: error.stack
        };
      }
    },

    async fetchDataSourceTypes() {
      try {
        const { options, selectedValue } = await loadDataSourceOptions('ORACLE')
        this.dataSources = options
        this.selectedDataSource = selectedValue
      } catch (error) {
        console.error('Error fetching data source types:', error)
        this.dataSources = []
        this.selectedDataSource = null
        this.$message.error('数据源列表加载失败，请检查服务连接')
      } finally {
        this.loadingDataSources = false
      }
    },
  }
};
</script>

<style scoped>
.syntax-validator {
  height: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

/* 保持与LineageVisualizer相同的面板样式 */
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

.sql-input-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
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


.sql-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #eef0f3;
}

#sql-input:focus {
  outline: none;
  border-color: #2563eb;
}

#validate-btn {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#validate-btn:hover {
  background: #1d4ed8;
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

.result-container {
  flex: 1;
  width: 100%;
  overflow: auto;
}

.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.success-message {
  color: #059669;
  font-size: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.success-details {
  margin-top: 15px;
  padding: 15px;
  background-color: rgba(52, 211, 153, 0.12); border: 1px solid rgba(52, 211, 153, 0.35);
  border-radius: 4px;
  color: #4b5563;
  font-size: 14px;
  width: 100%;
  max-height: 300px;
  overflow: auto;
}

.error-message {
  color: #f87171;
  font-size: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.error-details {
  margin-top: 15px;
  padding: 15px;
  background-color: rgba(248, 113, 113, 0.12); border: 1px solid rgba(248, 113, 113, 0.35);
  border-radius: 4px;
  color: #4b5563;
  font-size: 14px;
  width: 100%;
  max-height: 300px;
  overflow: auto;
}

.empty-result {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
  font-size: 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #1f2937;
  margin-top: 7px;
}

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
