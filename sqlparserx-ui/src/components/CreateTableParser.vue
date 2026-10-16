<template>
  <div class="create-table-parser">
    <div class="header">
      <h2>建表SQL解析工具</h2>
      <tool-capability-tip>解析 CREATE TABLE 结构，提取表字段、键、分区、分桶及 properties 等定义。</tool-capability-tip>
    </div>

    <div class="main-container">
      <div class="input-panel">
        <div class="panel-header">
          <div class="panel-title">建表SQL输入</div>
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
            tool="create-table-parser"
            :data-sources="dataSources"
            :selected-data-source="selectedDataSource"
            @load="sqlInput = $event"
        />

        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入建表SQL语句..."
          ></textarea>
        </div>
        <div class="sql-actions">
          <button id="parse-btn" :disabled="loadingDataSources || !dataSources.length" @click="parseCreateTable">解析建表SQL</button>
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
import { loadDataSourceOptions, parseCreateTable } from '@/api/DatasourceApi'
import ToolCapabilityTip from '@/components/ToolCapabilityTip.vue'
import DemoSqlSelector from '@/components/DemoSqlSelector.vue'

export default {
  name: 'CreateTableParser',
  components: { ToolCapabilityTip, DemoSqlSelector },
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
      selectedDataSource: null,
      dataSources: [],
      loadingDataSources: true,
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
        const { options, selectedValue } = await loadDataSourceOptions('DORIS')
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
.create-table-parser {
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

.input-panel {
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

#sql-input:focus {
  outline: none;
}

.sql-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #eef0f3;
}

#parse-btn {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

#parse-btn:hover {
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
  min-height: 0;
}

.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-info {
  min-height: 100%;
  padding: 14px;
  background-color: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.result-item .label {
  font-weight: bold;
  color: #4b5563;
  font-size: 12px;
}

.result-item .value {
  color: #1f2937;
  font-size: 14px;
  word-break: break-all;
}

.result-section {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  background-color: #ffffff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}

.result-section h3 {
  margin: 0 0 15px 0;
  color: #2563eb;
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
  background-color: #ffffff;
  font-size: 12px;
}

.result-table th,
.result-table td {
  padding: 8px 12px;
  border: 1px solid #eef0f3;
  text-align: left;
}

.result-table th {
  background-color: #ffffff;
  font-weight: bold;
  color: #4b5563;
  white-space: nowrap;
}

.result-table tr:nth-child(even) {
  background-color: #f3f4f6;
}

.result-table tr:hover {
  background-color: #eff6ff;
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
  color: #4b5563;
  min-width: 80px;
}

.key-value,
.bucket-value,
.partition-value {
  color: #1f2937;
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
  background-color: #ffffff;
  border-radius: 4px;
  border: 1px solid #eef0f3;
}

.property-key {
  font-weight: bold;
  color: #2563eb;
  min-width: 120px;
}

.property-value {
  color: #1f2937;
  word-break: break-all;
}

.json-result {
  margin: 0;
  padding: 16px 18px;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  background-color: #f8fafc;
  color: #334155;
  overflow: auto;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Monaco, monospace;
  font-size: 13px;
  line-height: 1.65;
  tab-size: 2;
  white-space: pre;
  max-height: calc(77vh - 170px);
  scrollbar-gutter: stable;
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
</style>
