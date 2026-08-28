<template>
  <div class="syntax-validator">
    <div class="header">
      <h2>在线语法解析验证工具</h2>
    </div>

    <div class="main-container">
      <div class="sql-panel">
        <div class="panel-header">
          <div class="panel-title">SQL输入</div>
          <div class="data-source-selector">
            <label for="data-source">数据源:</label>
            <select id="data-source" v-model="selectedDataSource">
              <option v-for="source in dataSources"
                      :key="source.value"
                      :value="source.value">
                {{ source.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入SQL语句进行语法验证..."
          ></textarea>
        </div>

        <div class="sql-actions">
          <button id="validate-btn" @click="validateSQL">验证语法</button>
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
import { fetchDataSourceTypes } from '@/api/DatasourceApi'

export default {
  name: 'SyntaxValidator',
  data() {
    return {
      sqlInput: '',
      selectedDataSource: 1, // 默认选中ORACLE
      dataSources: [], // 初始化为空数组
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
        const response = await fetchDataSourceTypes()
        this.dataSources = response.data
      } catch (error) {
        console.error('Error fetching data source types:', error)
        // 设置默认值作为回退
        this.dataSources = [
          { value: 3, label: 'MYSQL' },
          { value: 1, label: 'ORACLE' },
          { value: 11, label: 'POSTGRESQL' },
          { value: 17, label: 'SQLSERVER' },
          { value: 25, label: 'HIVE' }
        ]
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
  color: #e2e8f0;
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
  background-color: #141b33;
  border-radius: 14px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.4);
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
  gap: 8px;
}

.data-source-selector label {
  font-size: 14px;
  color: #cbd5e1;
}

.data-source-selector select {
  padding: 6px 12px;
  border: 1px solid rgba(99, 102, 241, 0.35);
  border-radius: 4px;
  font-size: 14px;
  color: #cbd5e1;
  background-color: #1a2342;
  transition: border-color 0.3s;
}

.data-source-selector select:focus {
  outline: none;
  border-color: #22d3ee;
}

.sql-input-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border: 1px solid rgba(99, 102, 241, 0.35);
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
  border-top: 1px solid rgba(99, 102, 241, 0.2);
}

#sql-input:focus {
  outline: none;
  border-color: #22d3ee;
}

#validate-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #6366f1, #22d3ee);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#validate-btn:hover {
  background: linear-gradient(135deg, #818cf8, #67e8f9); box-shadow: 0 4px 18px rgba(99, 102, 241, 0.5);
}

.result-panel {
  flex: 1;
  background-color: #141b33;
  border-radius: 14px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.4);
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
  color: #34d399;
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
  color: #cbd5e1;
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
  color: #cbd5e1;
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
  color: #64748b;
  font-size: 16px;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #e2e8f0;
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
  background: linear-gradient(120deg, #a5b4fc, #22d3ee);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  display: inline-block;
}
.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #e2e8f0;
}
.panel-title::before {
  content: '';
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, #6366f1, #22d3ee);
  flex: none;
}
#sql-input {
  color: #dbeafe;
  caret-color: #22d3ee;
}
#sql-input::placeholder {
  color: #64748b;
}
.result-sql {
  color: #dbeafe;
}
.json-result {
  color: #dbeafe;
}
</style>