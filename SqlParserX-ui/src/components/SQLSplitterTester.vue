<template>
  <div class="sql-splitter-tester">
    <div class="header">
      <h1>SQL切分测试工具</h1>
    </div>

    <div class="main-container">
      <div class="input-panel">
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
        <div class="sql-input-container">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入多条SQL语句..."
          ></textarea>
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
        alert('请输入SQL语句');
        return;
      }

      try {
        const response = await fetch(
            `${this.API_BASE_URL}/api/sql/split?datasourceType=${this.selectedDataSource}`,
            {
              method: 'POST',
              headers: {
                'Content-Type': 'text/plain'
              },
              body: sql
            }
        );

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.splitResults = await response.json();
      } catch (error) {
        console.error('切分失败:', error);
        alert('切分失败: ' + error.message);
      }
    },

    async fetchDataSourceTypes() {
      try {
        const response = await fetch(`${this.API_BASE_URL}/api/datasource/types`);
        if (!response.ok) {
          throw new Error('Failed to fetch data source types');
        }
        this.dataSources = await response.json();
      } catch (error) {
        console.error('Error fetching data source types:', error);
        // 设置默认值作为回退
        this.dataSources = [
          { value: 3, label: 'MYSQL' },
          { value: 1, label: 'ORACLE' },
          { value: 11, label: 'POSTGRESQL' },
          { value: 17, label: 'SQLSERVER' },
          { value: 25, label: 'HIVE' }
        ];
      }
    }
  }
};
</script>

<style scoped>
.sql-splitter-tester {
  font-family: 'Arial', sans-serif;
  margin: 0;
  padding: 0 20px;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  margin-bottom: -10px;
  color: #333;
}

.main-container {
  display: flex;
  flex: 1;
  gap: 20px;
}

.input-panel, .result-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
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
  color: #606266;
}

.data-source-selector select {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  background-color: #fff;
  transition: border-color 0.3s;
}

.data-source-selector select:focus {
  outline: none;
  border-color: #409EFF;
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
  padding: 12px 1px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  resize: none;
  transition: border-color 0.3s;
}

#sql-input:focus {
  outline: none;
  border-color: #409EFF;
}

#split-btn {
  align-self: flex-end;
  padding: 8px 20px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#split-btn:hover {
  background-color: #66b1ff;
}

.result-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.sql-item {
  margin-bottom: 15px;
}

.sql-index {
  font-weight: bold;
  margin-bottom: 5px;
  color: #409EFF;
}

.result-sql {
  width: 100%;
  height: 100px;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  resize: none;
  background-color: #f9f9f9;
}

.panel-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
  margin-top: 7px;
}

@media (max-width: 992px) {
  .main-container {
    flex-direction: column;
  }
}
</style>