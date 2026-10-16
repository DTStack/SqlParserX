<template>
  <div class="table-parser">
    <div class="header">
      <h2>SQL 表解析工具</h2>
      <tool-capability-tip>提取主表及全部真实表，支持 JOIN、子查询、UNION 和 CTE；本操作不依赖字段元数据。</tool-capability-tip>
    </div>

    <div class="main-container">
      <div class="input-panel">
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
            tool="table-parser"
            :data-sources="dataSources"
            :selected-data-source="selectedDataSource"
            @load="sqlInput = $event"
        />

        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入SQL语句..."
          ></textarea>
        </div>
        <div class="sql-actions">
          <button id="parse-btn" :disabled="loadingDataSources || !dataSources.length" @click="parseTables">解析表</button>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-title">解析结果</div>
        <div class="result-container">
          <div v-if="parsedTables.length === 0" class="empty-result">
            暂无解析结果
          </div>
          <div v-else class="result-content">
            <div class="result-count">共找到 {{ parsedTables.length }} 个表:</div>
            <div class="table-list">
              <div v-for="(table, index) in parsedTables" :key="index" class="table-item">
                <div class="table-index">表 {{ index + 1 }}:</div>
                <div class="table-name">{{ table }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { loadDataSourceOptions, parseTables } from '@/api/DatasourceApi'
import ToolCapabilityTip from '@/components/ToolCapabilityTip.vue'
import DemoSqlSelector from '@/components/DemoSqlSelector.vue'

export default {
  name: 'TableParser',
  components: { ToolCapabilityTip, DemoSqlSelector },
  data() {
    return {
      sqlInput: `-- 完整查询（包含WITH AS、JOIN、子查询、UNION等）
WITH
-- 第一个AS：简单SELECT
t1 AS (
    SELECT id, name, age
    FROM digua_ceshi.demo_table_digua
),

-- 第二个AS：SELECT FROM (SELECT) 嵌套形式
t2 AS (
    SELECT sub.id, sub.name
    FROM (
        SELECT id, name
        FROM digua_ceshi.demo_table_digua
        WHERE id IS NOT NULL
    ) AS sub
),

-- 第三个AS：UNION查询（使用新的表）
t3 AS (
    SELECT id, name, age
    FROM digua_ceshi.new_table
    WHERE age > 18
    UNION
    SELECT id, name, age
    FROM digua_ceshi.new_table
    WHERE age IS NULL
    UNION
    SELECT id, name, age
    FROM digua_ceshi.new_table2
    WHERE age IS NULL
),

-- 第四个AS：LEFT JOIN查询（使用新的表）
t4 AS (
    SELECT t.id, t.name, nt.age AS new_table_age
    FROM digua_ceshi.demo_table_digua t
    LEFT JOIN digua_ceshi.new_table nt ON t.id = nt.id
),

-- 第五个AS：递归CTE（使用新的表）
t5 AS (
    -- 递归CTE的初始查询
    SELECT id, name, manager_id, level
    FROM digua_ceshi.hierarchy_table
    WHERE manager_id IS NULL  -- 假设顶级节点的manager_id为NULL

    UNION ALL

    -- 递归部分
    SELECT h.id, h.name, h.manager_id, t5.level + 1
    FROM digua_ceshi.hierarchy_table h
    INNER JOIN t5 ON h.manager_id = t5.id
)

-- 最终主查询（包含所有JOIN、子查询、UNION操作）
-- digua_ceshi.result_table.*           来源字段为  digua_ceshi.demo_table_digua.* 【见下层】
--     digua_ceshi.result_table.id      来源字段为  digua_ceshi.demo_table_digua.id
-- \t\t digua_ceshi.result_table.name    来源字段为  digua_ceshi.demo_table_digua.name
--     digua_ceshi.result_table.age     来源字段为  digua_ceshi.demo_table_digua.age
-- digua_ceshi.result_table.t2_name  \t\t来源字段为  digua_ceshi.demo_table_digua.name
-- digua_ceshi.result_table.t3_name  \t\t来源字段为  digua_ceshi.new_table.name、digua_ceshi.new_table2.name
-- digua_ceshi.result_table.t4_age   \t\t来源字段为  digua_ceshi.new_table.age
-- digua_ceshi.result_table.t5_level \t\t来源字段为  digua_ceshi.hierarchy_table.level
-- digua_ceshi.result_table.department 来源字段为  digua_ceshi.departments.department
-- digua_ceshi.result_table.older_count 来源字段为  digua_ceshi.demo_table_digua.* 【见下层】
--     digua_ceshi.result_table.older_count      来源字段为  digua_ceshi.demo_table_digua.id
-- \t\t digua_ceshi.result_table.older_count      来源字段为  digua_ceshi.demo_table_digua.name
--     digua_ceshi.result_table.older_count     来源字段为  digua_ceshi.demo_table_digua.age
INSERT INTO digua_ceshi.result_table
SELECT
    t1.*,
    t2.name AS t2_name,
    t3.name AS t3_name,
    t4.new_table_age AS t4_age,
    t5.level AS t5_level, -- 添加递归CTE中的层级字段
    -- 添加与真实表的JOIN
    dt.department,
    -- 添加子查询示例
    (SELECT COUNT(*) FROM digua_ceshi.demo_table_digua WHERE age > t1.age) AS older_count -- 来源字段为 digua_ceshi.demo_table_digua.*
FROM t1
JOIN t2 ON t1.id = t2.id
LEFT JOIN t3 ON t1.id = t3.id
LEFT JOIN t4 ON t1.id = t4.id
LEFT JOIN t5 ON t1.id = t5.id
-- 添加与真实表的JOIN
JOIN digua_ceshi.department dt ON t1.id = dt.employee_id
-- 添加UNION示例
WHERE t1.age > 20

UNION ALL

SELECT
    t1.*,
    t2.name AS t2_name,
    t3.name AS t3_name,
    t4.new_table_age AS t4_age,
    t5.level AS t5_level, -- 添加递归CTE中的层级字段
    dt.department,
    0 AS older_count  -- 默认值
FROM t1
JOIN t2 ON t1.id = t2.id
LEFT JOIN t3 ON t1.id = t3.id
LEFT JOIN t4 ON t1.id = t4.id
LEFT JOIN t5 ON t1.id = t5.id
JOIN digua_ceshi.departments dt ON t1.id = dt.employee_id
WHERE t1.age <= 20;`,
      selectedDataSource: null,
      dataSources: [],
      loadingDataSources: true,
      parsedTables: [],
      API_BASE_URL: import.meta.env.VITE_API_URL,
    };
  },
  mounted() {
    this.fetchDataSourceTypes();
  },
  methods: {
    async parseTables() {
      const sql = this.sqlInput.trim();
      if (!sql) {
        this.$message.error('请输入SQL语句');
        return;
      }

      try {
        const form = {
          datasourceType: this.selectedDataSource,
          sql: sql
        };
        console.log("form:", form)
        const response = await parseTables(form);
        if (response.code === 200) {
          this.parsedTables = response.data;
          this.$message.success('表解析成功');
        } else {
          this.$message.error(response.msg || '表解析失败');
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
.table-parser {
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
}

.empty-result {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
  font-size: 16px;
}

.result-count {
  margin-bottom: 14px;
  color: #059669;
  font-size: 14px;
  font-weight: 600;
}

.table-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 4px;
}

.table-item {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.table-item:hover {
  border-color: #bfdbfe;
  background-color: #eff6ff;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.06);
}

.table-index {
  flex: none;
  min-width: 52px;
  padding: 3px 8px;
  border-radius: 6px;
  color: #1d4ed8;
  background-color: #dbeafe;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
}

.table-name {
  min-width: 0;
  color: #1e293b;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Monaco, monospace;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  overflow-wrap: anywhere;
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
