<template>
  <div class="table-parser">
    <div class="header">
      <h2>SQL 表解析工具</h2>
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
              placeholder="请输入SQL语句..."
          ></textarea>
          <button id="parse-btn" @click="parseTables">解析表</button>
        </div>
      </div>

      <div class="result-panel">
        <div class="panel-title">解析结果</div>
        <div class="result-container">
          <div v-if="parsedTables.length === 0" class="empty-result">
            暂无解析结果
          </div>
          <div v-else>
            <div class="result-count">共找到 {{ parsedTables.length }} 个表:</div>
            <div v-for="(table, index) in parsedTables" :key="index" class="table-item">
              <div class="table-index">表 {{ index + 1 }}:</div>
              <div class="table-name">{{ table }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchDataSourceTypes, parseTables } from '@/api/DatasourceApi'

export default {
  name: 'TableParser',
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
      selectedDataSource: 36, // 默认选中ORACLE
      dataSources: [], // 初始化为空数组
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
.table-parser {
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

#parse-btn {
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

#parse-btn:hover {
  background-color: #66b1ff;
}

.result-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.empty-result {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
  font-size: 16px;
}

.result-count {
  font-weight: bold;
  margin-bottom: 15px;
  color: #67C23A;
}

.table-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.table-index {
  font-weight: bold;
  margin-right: 10px;
  color: #409EFF;
}

.table-name {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
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