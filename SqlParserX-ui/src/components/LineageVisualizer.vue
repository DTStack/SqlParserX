<template>
  <div class="lineage-visualizer">
    <div class="header">
      <h1>血缘解析可视化工具</h1>
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
        <div class="sql-input-container">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入SQL语句..."
          ></textarea>
          <button id="parse-btn" @click="parseSQL">解析SQL</button>
        </div>
      </div>

      <div class="visualization-panel">
        <div class="panel-title">血缘关系图</div>
        <!-- 开关按钮及文字说明 -->
        <div class="switch-container">
          <span>字段级血缘关系</span>
          <label class="switch">
            <input type="checkbox" id="toggle-switch" v-model="isFieldLevel">
            <span class="slider round"></span>
          </label>
          <span>表级别血缘关系</span>
        </div>
        <div id="container" ref="chartContainer"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'LineageVisualizer',
  data() {
    return {
      sqlInput: `CREATE TABLE user_order_summary AS
    SELECT
        u.id as user_id,
        u.name,
        SUM(o.amount) as total_amount,
        COUNT(o.id) as order_count
    FROM users u JOIN orders o ON u.id = o.user_id
    GROUP BY u.id, u.name`,
      isFieldLevel: true,
      myChart: null,
      tableData: [],
      relationData: [],
      currentGraphicElements: [],
      currentLines: [],
      defaultLineColor: '#66b1ff40',
      activeLineColor: '#66b1ff',
      API_BASE_URL: 'http://127.0.0.1:8090',
      selectedDataSource: 1, // 默认选中MySQL
      dataSources: [], // 初始化为空数组
      fieldRelationsMap: {}, // 存储字段间的映射关系
      highlightedFields: new Set(), // 存储当前高亮的字段集合
    };
  },
  mounted() {
    this.fetchDataSourceTypes(); // 在组件挂载时获取数据源类型
    this.initChart();
    this.loadExampleData();
    this.parseSQL();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.myChart) {
      this.myChart.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initChart() {
      this.myChart = echarts.init(this.$refs.chartContainer);
      this.myChart.on('click', this.handleChartClick);
    },

    async parseSQL() {
      const sql = this.sqlInput.trim();
      if (!sql) {
        alert('请输入SQL语句');
        return;
      }

      try {
        this.myChart.showLoading();
        const { tableData, relationData } = await this.analyzeSQL(sql);
        this.updateTableData(tableData, relationData);
        this.buildFieldRelationsMap(relationData); // 新增：构建字段映射关系
      } catch (error) {
        console.error('解析失败:', error);
        alert('解析失败: ' + error.message);
      } finally {
        this.myChart.hideLoading();
      }
    },


    // 新增方法：构建字段关系映射
    buildFieldRelationsMap(relationData) {
      const map = {};
      relationData.forEach(rel => {
        // 正向关系：来源 -> 目标
        if (!map[rel.from]) map[rel.from] = new Set();
        map[rel.from].add(rel.to);

        // 反向关系：目标 -> 来源
        if (!map[rel.to]) map[rel.to] = new Set();
        map[rel.to].add(rel.from);
      });
      this.fieldRelationsMap = map;
      // console.log('build初始化：', this.fieldRelationsMap);
    },


    async loadExampleData() {
      try {
        this.myChart.showLoading();
        const { tableData, relationData } = await this.getExampleData();
        this.updateTableData(tableData, relationData);
      } catch (error) {
        console.error('加载示例数据失败:', error);
      } finally {
        this.myChart.hideLoading();
      }
    },

    async analyzeSQL(sql) {
      try {
        const encodedSQL = encodeURIComponent(sql);
        const response = await fetch(
            `${this.API_BASE_URL}/api/lineage/query?sql=${encodedSQL}&datasourceType=${this.selectedDataSource}`
        );

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
      } catch (error) {
        console.error('Error in analyzeSQL:', error);
        throw error;
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
    },

    async getExampleData() {
      const exampleSQL = `CREATE TABLE user_order_summary AS
      SELECT
        u.id as user_id,
        u.name,
        SUM(o.amount) as total_amount,
        COUNT(o.id) as order_count
      FROM users u JOIN orders o ON u.id = o.user_id
      GROUP BY u.id, u.name`;

      return this.analyzeSQL(exampleSQL);
    },

    updateTableData(newTableData, newRelationData) {
      this.tableData = [...newTableData];
      this.relationData = [...newRelationData];
      this.renderChart();
    },

    calculatePositions(tables) {
      const positions = {};
      const padding = 50;
      const tableWidth = 140;
      const tableHeightBase = 30;
      const fieldHeight = 24;
      const containerHeight = this.$refs.chartContainer.clientHeight;
      const containerWidth = this.$refs.chartContainer.clientWidth;

      const verticalSpacing = this.isFieldLevel ? 100 : 50;

      const tableHeights = {};
      tables.forEach(table => {
        tableHeights[table.tableName] = this.isFieldLevel
            ? tableHeightBase + (table.fields.length * fieldHeight)
            : tableHeightBase;
      });

      const sourceTables = [];
      const targetTables = [];
      tables.forEach(table => {
        const isSource = this.relationData.some(r => r.from.startsWith(table.tableName + '.'));
        const isTarget = this.relationData.some(r => r.to.startsWith(table.tableName + '.'));

        if (isSource && !isTarget) {
          sourceTables.push(table);
        } else if (!isSource && isTarget) {
          targetTables.push(table);
        } else {
          targetTables.push(table);
        }
      });

      const totalSourceHeight = sourceTables.reduce((sum, table) => sum + tableHeights[table.tableName], 0) +
          (sourceTables.length - 1) * verticalSpacing;
      let startY = (containerHeight - totalSourceHeight) / 2;
      let currentY = startY;

      sourceTables.forEach(table => {
        positions[table.tableName] = {
          x: containerWidth * 0.2,
          y: currentY
        };
        currentY += tableHeights[table.tableName] + verticalSpacing;
      });

      const totalTargetHeight = targetTables.reduce((sum, table) => sum + tableHeights[table.tableName], 0) +
          (targetTables.length - 1) * verticalSpacing;
      startY = (containerHeight - totalTargetHeight) / 2;
      currentY = startY;

      targetTables.forEach(table => {
        positions[table.tableName] = {
          x: containerWidth * 0.7,
          y: currentY
        };
        currentY += tableHeights[table.tableName] + verticalSpacing;
      });

      if (this.isFieldLevel) {
        this.adjustOverlaps(positions, tableHeights, verticalSpacing);
      }

      return positions;
    },

    adjustOverlaps(positions, tableHeights, minSpacing) {
      const tableNames = Object.keys(positions);

      const leftTables = tableNames.filter(name => positions[name].x === this.$refs.chartContainer.clientWidth * 0.2)
          .sort((a, b) => positions[a].y - positions[b].y);

      for (let i = 1; i < leftTables.length; i++) {
        const prevTable = leftTables[i-1];
        const currTable = leftTables[i];
        const prevBottom = positions[prevTable].y + tableHeights[prevTable];
        const currTop = positions[currTable].y;

        if (currTop < prevBottom + minSpacing) {
          const moveDown = (prevBottom + minSpacing) - currTop;
          positions[currTable].y += moveDown;

          for (let j = i+1; j < leftTables.length; j++) {
            positions[leftTables[j]].y += moveDown;
          }
        }
      }

      const rightTables = tableNames.filter(name => positions[name].x === this.$refs.chartContainer.clientWidth * 0.7)
          .sort((a, b) => positions[a].y - positions[b].y);

      for (let i = 1; i < rightTables.length; i++) {
        const prevTable = rightTables[i-1];
        const currTable = rightTables[i];
        const prevBottom = positions[prevTable].y + tableHeights[prevTable];
        const currTop = positions[currTable].y;

        if (currTop < prevBottom + minSpacing) {
          const moveDown = (prevBottom + minSpacing) - currTop;
          positions[currTable].y += moveDown;

          for (let j = i+1; j < rightTables.length; j++) {
            positions[rightTables[j]].y += moveDown;
          }
        }
      }
    },

    getTableRelations() {
      const tableRelations = [];
      this.relationData.forEach(rel => {
        const fromTable = rel.from.split('.')[0];
        const toTable = rel.to.split('.')[0];
        if (!tableRelations.some(r => r.from === fromTable && r.to === toTable)) {
          tableRelations.push({ from: fromTable, to: toTable });
        }
      });
      return tableRelations;
    },

    drawArrowLine(x1, y1, x2, y2, color = this.defaultLineColor, lineWidth = 2) {
      const angle = Math.atan2(y2 - y1, x2 - x1);
      const arrowLength = 10;
      const arrowX1 = x2 - arrowLength * Math.cos(angle - Math.PI / 6);
      const arrowY1 = y2 - arrowLength * Math.sin(angle - Math.PI / 6);
      const arrowX2 = x2 - arrowLength * Math.cos(angle + Math.PI / 6);
      const arrowY2 = y2 - arrowLength * Math.sin(angle + Math.PI / 6);

      return [
        {
          type: 'line',
          shape: { x1, y1, x2, y2 },
          style: { stroke: color, lineWidth },
          z: 5
        },
        {
          type: 'polyline',
          shape: {
            points: [
              [x2, y2],
              [arrowX1, arrowY1],
              [arrowX2, arrowY2],
              [x2, y2]
            ]
          },
          style: {
            fill: color,
            stroke: color,
            lineWidth: 1
          },
          z: 5
        }
      ];
    },

    // 新增方法：正确拆分带schema的表名和字段名
    getTableAndField(fullName) {
      // 处理特殊字段名（如t1.0）
      if (/\.\d+$/.test(fullName)) {
        const lastDotIndex = fullName.lastIndexOf('.');
        return {
          tableName: fullName.slice(0, lastDotIndex),
          fieldName: fullName.slice(lastDotIndex + 1)
        };
      }

      // 处理普通情况（如digua_ceshi.demo_table_digua.id）
      const parts = fullName.split('.');
      if (parts.length < 2) return { tableName: fullName, fieldName: '' };

      const fieldName = parts.pop();
      const tableName = parts.join('.');
      return { tableName, fieldName };
    },

    renderChart() {
      if (!this.myChart) return;

      this.myChart.clear();
      this.currentGraphicElements = [];
      this.currentLines = [];

      const positions = this.calculatePositions(this.tableData);

      this.tableData.forEach(table => {
        const pos = positions[table.tableName];
        if (!pos) return;

        const tableHeight = 30 + (table.fields.length * 24);
        const tableGroup = {
          type: 'group',
          position: [pos.x, pos.y],
          children: [
            {
              type: 'rect',
              shape: {width: 140, height: 30},
              style: {fill: '#409EFF'},
              name: table.tableName,
              data: {isTable: true, tableName: table.tableName}
            },
            {
              type: 'text',
              left: 5,
              top: 5,
              style: {
                text: table.tableName,
                fill: '#fff',
                fontSize: 14
              }
            }
          ]
        };

        if (this.isFieldLevel) {
          table.fields.forEach((field, index) => {
            const fieldFullName = `${table.tableName}.${field}`;
            // console.log("fieldFullName=>", fieldFullName);
            tableGroup.children.push({
              type: 'rect',
              shape: {width: 140, height: 24},
              style: {
                fill: this.highlightedFields.has(fieldFullName) ? '#e6f7ff' : '#fff', // 动态设置背景色
                stroke: '#ccc'
              },
              position: [0, 30 + index * 24],
              name: fieldFullName,
              z: 10,
              // 确保 data 是一个对象且包含 isField 属性
              data: {
                isField: true,  // 明确设置 isField
                source: this.relationData.find(r => r.to === fieldFullName)?.from,
                tableName: table.tableName,
                fieldName: field
              },
              onclick: (params) => {
                this.handleChartClick({
                  componentType: 'graphic',
                  name: fieldFullName,
                  data: { isField: true }
                });
              }
            });
            tableGroup.children.push({
              type: 'text',
              left: 10,
              top: 30 + index * 24 + 7,
              style: {
                text: field,
                fill: '#333',
                fontSize: 12
              },
              z: 11
            });
          });
        }
        this.currentGraphicElements.push(tableGroup);
      });

      const currentRelationData = this.isFieldLevel ? this.relationData : this.getTableRelations();

      console.log("currentRelationData=>", currentRelationData)
      currentRelationData.forEach(rel => {
        console.log("rel => ", rel)
        try {
          const { tableName: fromTable, fieldName: fromField } = this.getTableAndField(rel.from);
          const { tableName: toTable, fieldName: toField } = this.getTableAndField(rel.to);

          const fromPos = positions[fromTable];
          const toPos = positions[toTable];
          console.log("positions => ", positions)
          console.log("fromPos => ", fromPos, "fromTable=>", fromTable)
          console.log("toPos => ", toPos, "toTable=>", toTable)
          if (!fromPos || !toPos) return;

          let x1, y1, x2, y2;

          if (fromPos.x < toPos.x) {
            x1 = fromPos.x + 140;
            x2 = toPos.x;
          } else {
            x1 = fromPos.x;
            x2 = toPos.x + 140;
          }

          console.log(111)
          if (this.isFieldLevel) {
            const fromTableData = this.tableData.find(t => t.tableName === fromTable);
            const toTableData = this.tableData.find(t => t.tableName === toTable);
            const fromField = rel.from.split('.')[1];
            const toField = rel.to.split('.')[1];

            const fromFieldIndex = fromTableData.fields.indexOf(fromField);
            const toFieldIndex = toTableData.fields.indexOf(toField);

            y1 = fromPos.y + 30 + fromFieldIndex * 24 + 12;
            y2 = toPos.y + 30 + toFieldIndex * 24 + 12;
          } else {
            y1 = fromPos.y + 15;
            y2 = toPos.y + 15;
          }

          const arrowLine = this.drawArrowLine(x1, y1, x2, y2, this.defaultLineColor, 2);
          console.log(`Drawing line from (${x1},${y1}) to (${x2},${y2})`)
          arrowLine.forEach(line => {
            line.name = `line_${rel.from}_${rel.to}`;
            line.data = {
              from: rel.from,
              to: rel.to,
              isLine: true,
              defaultColor: this.defaultLineColor,
              activeColor: this.activeLineColor
            };
            line.z = 20; // 确保连线在较高层级
            this.currentLines.push(line);
            this.currentGraphicElements.push(line);
          });
        } catch (error) {
          console.error(`Error processing relation ${JSON.stringify(rel)}:`, error);
        }
      });

      const option = {
        graphic: this.currentGraphicElements,
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            if (params.componentType === 'graphic') {
              if (params.data?.isField) {
                return `字段：${params.name}<br>来源：${params.data.source || '无'}`;
              } else if (params.data?.isTable) {
                const sources = this.isFieldLevel
                    ? [...new Set(this.relationData
                        .filter(r => r.to.startsWith(params.name + '.'))
                        .map(s => s.from.split('.')[0]))]
                    : this.getTableRelations()
                        .filter(r => r.to === params.name)
                        .map(s => s.from);

                return `表：${params.name}<br>来源：${sources.join(', ') || '无'}`;
              }
            }
            return '';
          }
        }
      };

      this.myChart.setOption(option);
    },

    handleChartClick(params) {
      // console.log("handleChartClick=>", params);
      if (params.componentType === 'graphic' && params.name) {
        // 如果是字段点击
        if (params.data?.isField) {
          this.highlightedFields = new Set();

          // 获取所有相关字段
          // console.log(this.fieldRelationsMap)
          // console.log("缓存映射=>", this.fieldRelationsMap, ", filedName:", params.name)
          const collectRelatedFields = (fieldName) => {
            // console.log("获取相关字段=>", this.highlightedFields)
            if (this.highlightedFields.has(fieldName)) return;
            this.highlightedFields.add(fieldName);

            const relatedFields = this.fieldRelationsMap[fieldName] || [];
            relatedFields.forEach(f => collectRelatedFields(f));
          };

          collectRelatedFields(params.name);
        }

        // console.log('当前高亮属性：', this.highlightedFields);
        // console.log('当前高亮属性判断：', this.highlightedFields.has("user_order_summary.order_count"));
        // console.log('当前高亮属性判断：', this.highlightedFields.has("orders.id"));

        // 更新图表高亮
        this.updateChartHighlight();
      }
    },

    // 新增方法：更新图表高亮状态
    updateChartHighlight() {
      const processItem = (item) => {
        const itemData = item.data || {};

        // 处理group类型，递归处理其children
        if (item.type === 'group' && item.children) {
          return {
            ...item,
            children: item.children.map(child => processItem(child))
          };
        }

        // 处理字段高亮
        if (itemData.isField) {
          const isHighlighted = this.highlightedFields.has(item.name);
          return {
            ...item,
            style: {
              ...item.style,
              fill: isHighlighted ? '#e6f7ff' : '#fff'
            }
          };
        }

        // 处理连线高亮
        if (itemData.isLine) {
          const isRelated = [...this.highlightedFields].some(field =>
              itemData.from === field || itemData.to === field
          );
          const newColor = isRelated ? this.activeLineColor : this.defaultLineColor;

          if (item.type === 'polyline') {
            return {
              ...item,
              style: {
                ...item.style,
                fill: newColor,
                stroke: newColor
              }
            };
          } else if (item.type === 'line') {
            return {
              ...item,
              style: {
                ...item.style,
                stroke: newColor
              }
            };
          }
        }

        return item;
      };

      const newGraphicElements = this.currentGraphicElements.map(item => processItem(item));
      this.myChart.setOption({ graphic: newGraphicElements });
    },

    handleResize() {
      if (this.myChart) {
        this.myChart.resize();
        this.renderChart();
      }
    }
  },
  watch: {
    isFieldLevel() {
      this.renderChart();
    }
  }
};
</script>

<style scoped>
/* 新增样式 */
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

.lineage-visualizer {
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

.sql-panel {
  flex: 0 0 400px;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.visualization-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  position: relative;
}

#container {
  width: 100%;
  height: 100%;
  min-height: 600px;
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

.switch-container {
  position: absolute;
  top: 20px;
  left: 130px;
  display: flex;
  align-items: center;
  z-index: 1000;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px 15px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-size: 14px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
  margin: 0 10px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .4s;
}

input:checked + .slider {
  background-color: #409EFF;
}

input:focus + .slider {
  box-shadow: 0 0 1px #409EFF;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.slider.round {
  border-radius: 20px;
}

.slider.round:before {
  border-radius: 50%;
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
  }
}
</style>