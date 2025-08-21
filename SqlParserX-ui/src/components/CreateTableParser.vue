<template>
  <div class="create-table-parser">
    <div class="header">
      <h2>建表SQL解析工具</h2>
    </div>

    <div class="main-container">
      <div class="input-panel">
        <div class="panel-header">
          <div class="panel-title">建表SQL输入</div>
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
              placeholder="请输入建表SQL语句..."
          ></textarea>
        </div>
        <div class="sql-actions">
          <button id="parse-btn" @click="parseCreateTable">解析建表SQL</button>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-title">解析结果</div>
        <div class="result-container">
          <!-- 只保留解析成功时的结果展示 -->
          <div v-if="parseResult" class="result-content">
            <div class="result-info">
              <div class="result-section">
                <h3>完整JSON结果</h3>
                <pre class="json-result">{{ JSON.stringify(parseResult, null, 2) }}</pre>
              </div>
            </div>
<!--              <div class="result-item">-->
<!--                <span class="label">数据库:</span>-->
<!--                <span class="value">{{ parseResult.db || 'N/A' }}</span>-->
<!--              </div>-->
<!--              <div class="result-item">-->
<!--                <span class="label">表名:</span>-->
<!--                <span class="value">{{ parseResult.tableName || 'N/A' }}</span>-->
<!--              </div>-->
<!--              <div class="result-item">-->
<!--                <span class="label">表注释:</span>-->
<!--                <span class="value">{{ parseResult.tableComment || 'N/A' }}</span>-->
<!--              </div>-->
<!--              <div class="result-item">-->
<!--                <span class="label">IF NOT EXISTS:</span>-->
<!--                <span class="value">{{ parseResult.notExists ? '是' : '否' }}</span>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.tableElementList && parseResult.tableElementList.length">-->
<!--              <h3>表字段 ({{ parseResult.tableElementList.length }} 个)</h3>-->
<!--              <div class="table-container">-->
<!--                <table class="result-table">-->
<!--                  <thead>-->
<!--                  <tr>-->
<!--                    <th>字段名</th>-->
<!--                    <th>类型</th>-->
<!--                    <th>参数</th>-->
<!--                    <th>聚合类型</th>-->
<!--                    <th>注释</th>-->
<!--                    <th>策略</th>-->
<!--                    <th>精度</th>-->
<!--                    <th>标度</th>-->
<!--                  </tr>-->
<!--                  </thead>-->
<!--                  <tbody>-->
<!--                  <tr v-for="(column, index) in parseResult.tableElementList" :key="index">-->
<!--                    <td>{{ column.name }}</td>-->
<!--                    <td>{{ column.type }}</td>-->
<!--                    <td>{{ column.arguments && column.arguments.length ? column.arguments.join(', ') : 'N/A' }}</td>-->
<!--                    <td>{{ column.polymerizeType || 'N/A' }}</td>-->
<!--                    <td>{{ column.comment || 'N/A' }}</td>-->
<!--                    <td>{{ column.columnStrategy || 'N/A' }}</td>-->
<!--                    <td>{{ column.precision !== null ? column.precision : 'N/A' }}</td>-->
<!--                    <td>{{ column.scale !== null ? column.scale : 'N/A' }}</td>-->
<!--                  </tr>-->
<!--                  </tbody>-->
<!--                </table>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.partitionedByList && parseResult.partitionedByList.length">-->
<!--              <h3>分区字段 ({{ parseResult.partitionedByList.length }} 个)</h3>-->
<!--              <div class="table-container">-->
<!--                <table class="result-table">-->
<!--                  <thead>-->
<!--                  <tr>-->
<!--                    <th>字段名</th>-->
<!--                    <th>类型</th>-->
<!--                    <th>参数</th>-->
<!--                    <th>注释</th>-->
<!--                  </tr>-->
<!--                  </thead>-->
<!--                  <tbody>-->
<!--                  <tr v-for="(partition, index) in parseResult.partitionedByList" :key="index">-->
<!--                    <td>{{ partition.name }}</td>-->
<!--                    <td>{{ partition.type }}</td>-->
<!--                    <td>{{ partition.arguments && partition.arguments.length ? partition.arguments.join(', ') : 'N/A' }}</td>-->
<!--                    <td>{{ partition.comment || 'N/A' }}</td>-->
<!--                  </tr>-->
<!--                  </tbody>-->
<!--                </table>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.tableKey">-->
<!--              <h3>表键信息</h3>-->
<!--              <div class="key-info">-->
<!--                <div class="key-item">-->
<!--                  <span class="key-label">键类型:</span>-->
<!--                  <span class="key-value">{{ parseResult.tableKey.tableKeyEnum || 'N/A' }}</span>-->
<!--                </div>-->
<!--                <div class="key-item" v-if="parseResult.tableKey.columnName && parseResult.tableKey.columnName.length">-->
<!--                  <span class="key-label">字段名:</span>-->
<!--                  <span class="key-value">{{ parseResult.tableKey.columnName.join(', ') }}</span>-->
<!--                </div>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.tableDistributedBuckets">-->
<!--              <h3>分桶信息</h3>-->
<!--              <div class="bucket-info">-->
<!--                <div class="bucket-item">-->
<!--                  <span class="bucket-label">分桶类型:</span>-->
<!--                  <span class="bucket-value">{{ parseResult.tableDistributedBuckets.distributedByTypeEnum || 'N/A' }}</span>-->
<!--                </div>-->
<!--                <div class="bucket-item">-->
<!--                  <span class="bucket-label">分桶数量:</span>-->
<!--                  <span class="bucket-value">{{ parseResult.tableDistributedBuckets.bucketNum !== null ? parseResult.tableDistributedBuckets.bucketNum : 'N/A' }}</span>-->
<!--                </div>-->
<!--                <div class="bucket-item" v-if="parseResult.tableDistributedBuckets.columnNames && parseResult.tableDistributedBuckets.columnNames.length">-->
<!--                  <span class="bucket-label">分桶字段:</span>-->
<!--                  <span class="bucket-value">{{ parseResult.tableDistributedBuckets.columnNames.join(', ') }}</span>-->
<!--                </div>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.tablePartition">-->
<!--              <h3>分区信息</h3>-->
<!--              <div class="partition-info">-->
<!--                <div class="partition-item">-->
<!--                  <span class="partition-label">分区类型:</span>-->
<!--                  <span class="partition-value">{{ parseResult.tablePartition.tablePartitionEnum || 'N/A' }}</span>-->
<!--                </div>-->
<!--                <div class="partition-item">-->
<!--                  <span class="partition-label">分区字段:</span>-->
<!--                  <span class="partition-value">{{ parseResult.tablePartition.columnName || 'N/A' }}</span>-->
<!--                </div>-->
<!--                <div class="partition-item" v-if="parseResult.tablePartition.tablePartitionElement">-->
<!--                  <span class="partition-label">分区元素:</span>-->
<!--                  <pre class="json-result">{{ JSON.stringify(parseResult.tablePartition.tablePartitionElement, null, 2) }}</pre>-->
<!--                </div>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="result-section" v-if="parseResult.properties && Object.keys(parseResult.properties).length">-->
<!--              <h3>Properties ({{ Object.keys(parseResult.properties).length }} 个)</h3>-->
<!--              <div class="properties-grid">-->
<!--                <div v-for="(value, key) in parseResult.properties" :key="key" class="property-item">-->
<!--                  <span class="property-key">{{ key }}:</span>-->
<!--                  <span class="property-value">{{ value }}</span>-->
<!--                </div>-->
<!--              </div>-->
<!--            </div>-->


          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchDataSourceTypes, parseCreateTable } from '@/api/DatasourceApi'

export default {
  name: 'CreateTableParser',
  data() {
    return {
      sqlInput: `CREATE TABLE IF NOT EXISTS example_db.example_tbl_unique
      (
        user_id LARGEINT NOT NULL,
        user_name VARCHAR(50) NOT NULL,
        city VARCHAR(20),
        age SMALLINT,
        sex TINYINT
        )
        UNIQUE KEY(user_id, user_name)
        DISTRIBUTED BY HASH(user_id) BUCKETS 10
        PROPERTIES (
                     "enable_unique_key_merge_on_write" = "true"
                   );`,
      selectedDataSource: 36,
      dataSources: [],
      parseResult: null
    };
  },
  mounted() {
    this.fetchDataSourceTypes();
  },
  methods: {
    async parseCreateTable() {
      const sql = this.sqlInput.trim();
      if (!sql) {
        this.$message.error('请输入建表SQL语句');
        return;
      }

      if (!sql.toLowerCase().includes('create table')) {
        this.$message.warning('请输入有效的建表SQL语句');
        return;
      }

      try {
        const form = {
          datasourceType: this.selectedDataSource,
          sql: sql
        };
        console.log("form:", form)
        const response = await parseCreateTable(form);
        if (response.code === 200) {
          this.parseResult = response.data;
          this.$message.success('建表SQL解析成功');
        } else {
          this.$message.error(response.msg || '建表SQL解析失败');
        }
      } catch (error) {
        console.error('解析失败:', error);
        this.$message.error('解析失败: ' + (error.response?.data?.msg || error.message));
      }
    },

    async fetchDataSourceTypes() {
      try {
        const response = await fetchDataSourceTypes()
        this.dataSources = response.data
      } catch (error) {
        console.error('Error fetching data source types:', error)
        this.dataSources = [
          { value: 3, label: 'MYSQL' },
          {value: 1, label: 'ORACLE' },
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
.create-table-parser {
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

.input-panel {
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

#sql-input:focus {
  outline: none;
}

.sql-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

#parse-btn {
  padding: 8px 20px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#parse-btn:hover {
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
  gap: 20px;
}

.result-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.result-item .label {
  font-weight: bold;
  color: #606266;
  font-size: 12px;
}

.result-item .value {
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}

.result-section {
  border: 1px solid #e6e8eb;
  border-radius: 8px;
  padding: 15px;
  background-color: #fafafa;
}

.result-section h3 {
  margin: 0 0 15px 0;
  color: #409EFF;
  font-size: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e6e8eb;
}

.table-container {
  overflow-x: auto;
}

.result-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  font-size: 12px;
}

.result-table th,
.result-table td {
  padding: 8px 12px;
  border: 1px solid #e6e8eb;
  text-align: left;
}

.result-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #606266;
  white-space: nowrap;
}

.result-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.result-table tr:hover {
  background-color: #f0f7ff;
}

.key-info,
.bucket-info,
.partition-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.key-item,
.bucket-item,
.partition-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.key-label,
.bucket-label,
.partition-label {
  font-weight: bold;
  color: #606266;
  min-width: 80px;
}

.key-value,
.bucket-value,
.partition-value {
  color: #303133;
  word-break: break-all;
}

.properties-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 10px;
}

.property-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e6e8eb;
}

.property-key {
  font-weight: bold;
  color: #409EFF;
  min-width: 120px;
}

.property-value {
  color: #303133;
  word-break: break-all;
}

.json-result {
  background-color: #2d2d2d;
  color: #f8f8f2;
  padding: 15px;
  border-radius: 4px;
  overflow: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  max-height: 500px;
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

  .input-panel {
    flex: 0 0 auto;
    max-height: 50vh;
  }

  .result-info {
    grid-template-columns: 1fr;
  }

  .properties-grid {
    grid-template-columns: 1fr;
  }

  .result-table {
    font-size: 11px;
  }

  .result-table th,
  .result-table td {
    padding: 6px 8px;
  }
}
</style>