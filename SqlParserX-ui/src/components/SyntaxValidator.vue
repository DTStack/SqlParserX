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
  color: #333;
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
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.sql-input-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  border: 1px solid #dcdfe6;
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
  border-top: 1px solid #f0f0f0;
}

#sql-input:focus {
  outline: none;
  border-color: #409EFF;
}

#validate-btn {
  padding: 8px 20px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#validate-btn:hover {
  background-color: #66b1ff;
}

.result-panel {
  flex: 1;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
  color: #67C23A;
  font-size: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.success-details {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  width: 100%;
  max-height: 300px;
  overflow: auto;
}

.error-message {
  color: #F56C6C;
  font-size: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.error-details {
  margin-top: 15px;
  padding: 15px;
  background-color: #fef0f0;
  border-radius: 4px;
  color: #606266;
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
  color: #909399;
  font-size: 16px;
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

  .sql-panel {
    flex: 0 0 auto;
    max-height: 50vh;
  }
}
</style>