<template>
  <div class="sql-splitter-tester">
    <div class="header">
      <h2>SQL切分测试工具</h2>
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
              placeholder="请输入多条SQL语句..."
          ></textarea>
        </div>

        <div class="sql-actions">
          <button id="split-btn" @click="splitSQL">切分SQL</button>
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
import { fetchDataSourceTypes } from '@/api/DatasourceApi'

export default {
  name: 'SQLSplitterTester',
  data() {
    return {
      sqlInput: `CREATE OR REPLACE FUNCTION add_numbers(
    p_num1 IN NUMBER,
    p_num2 IN NUMBER
) RETURN NUMBER IS
BEGIN
    RETURN p_num1 + p_num2;
END add_numbers;

select * from WWV_FLOW_FILE_OBJECTS$;`,
      selectedDataSource: 1, // 默认选中ORACLE
      dataSources: [], // 初始化为空数组
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
  gap: 8px;
}

.data-source-selector label {
  font-size: 14px;
  color: #4b5563;
}

.data-source-selector select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  color: #4b5563;
  background-color: #ffffff;
  transition: border-color 0.3s;
}

.data-source-selector select:focus {
  outline: none;
  border-color: #2563eb;
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