<template>
  <div class="lineage-visualizer">
    <div class="header">
      <h2>血缘解析可视化工具</h2>
      <tool-capability-tip>从 INSERT / CTAS 推导表级与字段级来源，支持 JOIN、CTE、子查询和 UNION；纯 SELECT 不产出血缘。</tool-capability-tip>
    </div>

    <div class="main-container">
      <div class="sql-panel">
        <div class="panel-header">
          <div class="panel-title">SQL输入</div>
          <div class="data-source-selector">
            <label for="data-source">数据源:</label>
              <select id="data-source" v-model.number="selectedDataSource" :disabled="loadingLineage || loadingDataSources || !dataSources.length">
              <option v-if="!dataSources.length" disabled value="">{{ loadingDataSources ? '正在加载…' : '暂无可用数据源' }}</option>
              <option v-for="source in dataSources"
                      :key="source.value"
                      :value="source.value">
                {{ source.label }}
              </option>
            </select>
          </div>
        </div>

        <!-- 将文本区域和按钮分离，按钮固定在底部 -->
        <div class="sql-input-wrapper">
          <textarea
              id="sql-input"
              v-model="sqlInput"
              placeholder="请输入SQL语句..."
          ></textarea>
        </div>

        <div class="sql-actions">
            <button id="parse-btn" :disabled="loadingLineage || loadingDataSources || !dataSources.length" @click="parseSQL">解析SQL</button>
        </div>
      </div>

      <div class="visualization-panel">
        <div class="visualization-toolbar">
          <div class="panel-title">血缘关系图</div>
          <div class="lineage-level-switch" role="group" aria-label="血缘关系层级">
            <button
                type="button"
                :class="{ active: isFieldLevel }"
                :aria-pressed="isFieldLevel"
                :disabled="loadingLineage || loadingDataSources || !dataSources.length"
                @click="switchLineageLevel(true)"
            >字段级</button>
            <button
                type="button"
                :class="{ active: !isFieldLevel }"
                :aria-pressed="!isFieldLevel"
                :disabled="loadingLineage || loadingDataSources || !dataSources.length"
                @click="switchLineageLevel(false)"
            >表级</button>
          </div>
        </div>

        <!-- 图表控制按钮 -->
        <div class="chart-controls">
          <el-tooltip content="放大" placement="bottom">
            <el-button circle @click="zoomIn" size="small">
              <i class="el-icon-zoom-in"></i>
            </el-button>
          </el-tooltip>
          <el-tooltip content="缩小" placement="bottom">
            <el-button circle @click="zoomOut" size="small">
              <i class="el-icon-zoom-out"></i>
            </el-button>
          </el-tooltip>
          <el-tooltip content="重置" placement="bottom">
            <el-button circle @click="resetZoom" size="small">
              <i class="el-icon-refresh-left"></i>
            </el-button>
          </el-tooltip>
          <el-tooltip content="全屏" placement="bottom">
            <el-button circle @click="toggleFullscreen" size="small">
              <i class="el-icon-full-screen"></i>
            </el-button>
          </el-tooltip>
        </div>

        <!-- 图表容器 -->
        <div
            id="container"
            ref="chartContainer"
            @wheel.prevent="handleWheel"
            @mousedown="startDrag"
            @mousemove="handleDrag"
            @mouseup="endDrag"
            @mouseleave="endDrag"
        ></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { fullscreen, exitFullscreen } from '@/utils/fullscreen';
import { analyzeColumnLineage, analyzeTableLineage } from '@/api/LineageApi'
import { loadDataSourceOptions } from '@/api/DatasourceApi'
import ToolCapabilityTip from '@/components/ToolCapabilityTip.vue'

export default {
  name: 'LineageVisualizer',
  components: { ToolCapabilityTip },
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
      loadingLineage: false,
      myChart: null,
      tableData: [],
      relationData: [],
      currentGraphicElements: [],
      currentLines: [],
      defaultLineColor: '#66b1ff40',
      activeLineColor: '#66b1ff',
      API_BASE_URL: import.meta.env.VITE_API_URL,
      selectedDataSource: null,
      dataSources: [],
      loadingDataSources: true,
      fieldRelationsMap: {},
      highlightedFields: new Set(),
      tableWidth: 160,
      zoomLevel: 1,
      isFullscreen: false,
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      translateX: 0,
      translateY: 0,
    };
  },
  mounted() {
    this.$refs.chartContainer.style.cursor = 'grab';
    this.initChart();
    this.fetchDataSourceTypes().then(() => {
      if (this.selectedDataSource !== null) {
        this.parseSQL();
      }
    });
    window.addEventListener('resize', this.handleResize);

    // 确保初始化时计算正确高度
    this.$nextTick(() => {
      this.handleResize();
    });
  },
  beforeDestroy() {
    if (this.myChart) {
      this.myChart.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    startDrag(event) {
      this.isDragging = true;
      this.dragStartX = event.clientX - this.translateX;
      this.dragStartY = event.clientY - this.translateY;
      this.$refs.chartContainer.style.cursor = 'grabbing';
    },

    handleDrag(event) {
      if (!this.isDragging) return;

      this.translateX = event.clientX - this.dragStartX;
      this.translateY = event.clientY - this.dragStartY;

      this.applyTransform();
    },

    endDrag() {
      this.isDragging = false;
      this.$refs.chartContainer.style.cursor = 'grab';
    },

    applyTransform() {
      const container = this.$refs.chartContainer;
      container.style.transform = `
        scale(${this.zoomLevel})
        translate(${this.translateX}px, ${this.translateY}px)
      `;
    },

    handleWheel(event) {
      event.preventDefault();
      const delta = event.deltaY > 0 ? -0.1 : 0.1;
      this.zoomLevel = Math.min(Math.max(this.zoomLevel + delta, 0.5), 3);
      this.applyZoom();
    },

    zoomIn() {
      this.zoomLevel = Math.min(this.zoomLevel + 0.1, 2);
      this.applyZoom();
    },

    zoomOut() {
      this.zoomLevel = Math.max(this.zoomLevel - 0.1, 0.5);
      this.applyZoom();
    },

    resetZoom() {
      this.zoomLevel = 1;
      this.translateX = 0;
      this.translateY = 0;
      this.applyTransform();
      this.$nextTick(() => {
        this.myChart.resize();
        this.renderChart();
      });
    },

    applyZoom() {
      this.applyTransform();
      this.$nextTick(() => {
        this.myChart.resize();
      });
    },

    async toggleFullscreen() {
      const container = this.$refs.chartContainer.parentElement;

      if (this.isFullscreen) {
        await exitFullscreen();
        this.isFullscreen = false;
      } else {
        await fullscreen(container);
        this.isFullscreen = true;
      }

      this.$nextTick(() => {
        this.myChart.resize();
      });
    },

    handleResize() {
      if (this.myChart) {
        // 强制计算容器高度
        const container = this.$refs.chartContainer;
        if (container) {
          const parent = container.parentElement;
          if (parent) {
            // 设置图表容器高度为父容器可用高度
            container.style.height = `${parent.clientHeight - 60}px`;
          }
        }
        this.myChart.resize();
        if (!this.isFullscreen) {
          this.renderChart();
        }
      }
    },

    initChart() {
      this.myChart = echarts.init(this.$refs.chartContainer);
      this.myChart.on('click', this.handleChartClick);
    },

    switchLineageLevel(isFieldLevel) {
      if (this.loadingLineage || this.loadingDataSources || !this.dataSources.length) return;
      this.isFieldLevel = isFieldLevel;
      this.updateTableData([], []);
      this.parseSQL();
    },

    async parseSQL() {
      if (this.loadingLineage || this.loadingDataSources || !this.dataSources.length) return;
      const sql = this.sqlInput.trim();
      if (!sql) {
        alert('请输入SQL语句');
        return;
      }

      this.loadingLineage = true;
      try {
        this.myChart.showLoading();
        const { tableData, relationData } = await this.analyzeSQL(sql);
        this.updateTableData(tableData, relationData);
        this.highlightedFields = new Set();
        this.buildFieldRelationsMap(this.isFieldLevel ? relationData : []);
      } catch (error) {
        this.updateTableData([], []);
        this.fieldRelationsMap = {};
        console.error('解析失败:', error);
        alert('解析失败: ' + error.message);
      } finally {
        this.loadingLineage = false;
        this.myChart.hideLoading();
      }
    },

    buildFieldRelationsMap(relationData) {
      const map = {};
      relationData.forEach(rel => {
        if (!map[rel.from]) map[rel.from] = new Set();
        map[rel.from].add(rel.to);

        if (!map[rel.to]) map[rel.to] = new Set();
        map[rel.to].add(rel.from);
      });
      this.fieldRelationsMap = map;
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
        const queryLineage = this.isFieldLevel ? analyzeColumnLineage : analyzeTableLineage
        const response = await queryLineage(sql, this.selectedDataSource)
        return response.data
      } catch (error) {
        console.error('Error in analyzeSQL:', error)
        throw error
      }
    },

    async fetchDataSourceTypes() {
      try {
        const { options, selectedValue } = await loadDataSourceOptions('MYSQL')
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
      const tableHeightBase = 30;
      const fieldHeight = 24;
      // 使用实际计算的容器高度
      const containerHeight = this.$refs.chartContainer.clientHeight;
      const containerWidth = this.$refs.chartContainer.clientWidth;

      const verticalSpacing = this.isFieldLevel ? 60 : 30;

      const tableHeights = {};
      tables.forEach(table => {
        tableHeights[table.tableName] = this.isFieldLevel
            ? tableHeightBase + (table.fields.length * fieldHeight)
            : tableHeightBase;
      });

      const sourceTables = [];
      const targetTables = [];
      tables.forEach(table => {
        const isSource = this.relationData.some(r => this.getTableAndField(r.from).tableName === table.tableName);
        const isTarget = this.relationData.some(r => this.getTableAndField(r.to).tableName === table.tableName);

        if (isSource && !isTarget) {
          sourceTables.push(table);
        } else if (!isSource && isTarget) {
          targetTables.push(table);
        } else {
          targetTables.push(table);
        }
      });

      // 计算源表总高度
      const totalSourceHeight = sourceTables.reduce((sum, table) => sum + tableHeights[table.tableName], 0) +
          (sourceTables.length - 1) * verticalSpacing;
      let startY = (containerHeight - totalSourceHeight) / 2;
      let currentY = startY;

      // 计算源表位置
      sourceTables.forEach(table => {
        positions[table.tableName] = {
          x: containerWidth * 0.2,
          y: currentY
        };
        currentY += tableHeights[table.tableName] + verticalSpacing;
      });

      // 计算目标表总高度
      const totalTargetHeight = targetTables.reduce((sum, table) => sum + tableHeights[table.tableName], 0) +
          (targetTables.length - 1) * verticalSpacing;
      startY = (containerHeight - totalTargetHeight) / 2;
      currentY = startY;

      // 计算目标表位置
      targetTables.forEach(table => {
        positions[table.tableName] = {
          x: containerWidth * 0.7,  // 调整为更靠右的位置，避免拥挤
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
      return this.relationData;
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

    getTableAndField(fullName) {
      if (!this.isFieldLevel) return { tableName: fullName, fieldName: '' };
      if (/\.\d+$/.test(fullName)) {
        const lastDotIndex = fullName.lastIndexOf('.');
        return {
          tableName: fullName.slice(0, lastDotIndex),
          fieldName: fullName.slice(lastDotIndex + 1)
        };
      }

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

      let tableWidth = this.tableWidth
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
              shape: {width: tableWidth, height: 30},
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
            tableGroup.children.push({
              type: 'rect',
              shape: {width: tableWidth, height: 24},
              style: {
                fill: this.highlightedFields.has(fieldFullName) ? '#e6f7ff' : '#fff',
                stroke: '#ccc'
              },
              position: [0, 30 + index * 24],
              name: fieldFullName,
              z: 10,
              data: {
                isField: true,
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

      currentRelationData.forEach(rel => {
        try {
          const { tableName: fromTable, fieldName: fromField } = this.getTableAndField(rel.from);
          const { tableName: toTable, fieldName: toField } = this.getTableAndField(rel.to);

          const fromPos = positions[fromTable];
          const toPos = positions[toTable];
          if (!fromPos || !toPos) return;

          let x1, y1, x2, y2;

          if (fromPos.x < toPos.x) {
            x1 = fromPos.x + tableWidth;
            x2 = toPos.x;
          } else {
            x1 = fromPos.x;
            x2 = toPos.x + tableWidth;
          }

          if (this.isFieldLevel) {
            const fromTableData = this.tableData.find(t => t.tableName === fromTable);
            const toTableData = this.tableData.find(t => t.tableName === toTable);

            if (!fromTableData || !toTableData) {
              console.error(`Table data not found: fromTable=${fromTable}, toTable=${toTable}`);
              return;
            }

            const fromFieldIndex = fromTableData.fields.indexOf(fromField);
            const toFieldIndex = toTableData.fields.indexOf(toField);

            if (fromFieldIndex === -1 || toFieldIndex === -1) {
              console.error(`Field not found: fromField=${fromField}, toField=${toField}`);
              return;
            }

            y1 = fromPos.y + 30 + fromFieldIndex * 24 + 12;
            y2 = toPos.y + 30 + toFieldIndex * 24 + 12;
          } else {
            y1 = fromPos.y + 15;
            y2 = toPos.y + 15;
          }

          const arrowLine = this.drawArrowLine(x1, y1, x2, y2, this.defaultLineColor, 2);
          arrowLine.forEach(line => {
            line.name = `line_${rel.from}_${rel.to}`;
            line.data = {
              from: rel.from,
              to: rel.to,
              isLine: true,
              defaultColor: this.defaultLineColor,
              activeColor: this.activeLineColor
            };
            line.z = 20;
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
                        .map(s => this.getTableAndField(s.from).tableName))]
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
      if (params.componentType === 'graphic' && params.name) {
        if (params.data?.isField) {
          this.highlightedFields = new Set();

          const collectRelatedFields = (fieldName) => {
            if (this.highlightedFields.has(fieldName)) return;
            this.highlightedFields.add(fieldName);

            const relatedFields = this.fieldRelationsMap[fieldName] || [];
            relatedFields.forEach(f => collectRelatedFields(f));
          };

          collectRelatedFields(params.name);
        }

        this.updateChartHighlight();
      }
    },

    updateChartHighlight() {
      const processItem = (item) => {
        const itemData = item.data || {};

        if (item.type === 'group' && item.children) {
          return {
            ...item,
            children: item.children.map(child => processItem(child))
          };
        }

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
    }
  }
};
</script>

<style scoped>
/* 确保所有父容器高度正确传递 */
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}

.header {
  margin-bottom: -10px;
  color: #1f2937;
  padding: 0 20px;
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

/* 确保组件占满父容器高度 */
.lineage-visualizer {
  height: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  margin-bottom: -10px;
  color: #1f2937;
}

/* 主容器使用flex占满空间 */
.main-container {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
  padding: 20px;
  box-sizing: border-box;
}

.sql-panel, .visualization-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sql-panel {
  flex: 0 0 400px;
  background-color: #ffffff;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px;
  overflow: hidden;
  /* 关键修改：限制SQL面板最大高度为视口高度的90% */
  max-height: 77vh;
  display: flex;
  flex-direction: column;
}

/* 关键修改：SQL输入区域包装器，用于控制滚动 */
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
  min-height: 150px; /* 设置最小高度 */
}

#sql-input:focus {
  outline: none;
}

/* 关键修改：按钮区域固定在底部 */
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

/* 可视化面板占满剩余空间 */
.visualization-panel {
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

/* 图表容器强制占满可用高度 */
#container {
  flex: 1;
  width: 100%;
  overflow: hidden;
  transform-origin: 0 0;
  transition: transform 0.15s ease-out;
  user-select: none;
  position: relative;
  min-height: 0;
}

.visualization-toolbar {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 38px;
  padding-right: 190px;
  margin-bottom: 10px;
}

.visualization-toolbar .panel-title {
  margin: 0;
}

.lineage-level-switch {
  display: inline-flex;
  align-items: center;
  padding: 3px;
  border: 1px solid #e2e8f0;
  border-radius: 9px;
  background: #f8fafc;
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.04);
}

.lineage-level-switch button {
  min-width: 64px;
  padding: 5px 14px;
  border: 0;
  border-radius: 6px;
  color: #64748b;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  line-height: 20px;
  white-space: nowrap;
  transition: color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.lineage-level-switch button:hover:not(.active) {
  color: #2563eb;
  background: #eff6ff;
}

.lineage-level-switch button.active {
  color: #1d4ed8;
  background: #ffffff;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.12);
}

.lineage-level-switch button:disabled { opacity: .55; cursor: wait; }

.lineage-level-switch button:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 1px;
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
    padding: 10px;
  }

  .sql-panel {
    flex: 0 0 auto;
    max-height: 50vh;
  }

  #container {
    min-height: 400px;
  }

  .visualization-toolbar {
    padding-right: 0;
  }

  .chart-controls {
    position: static;
    align-self: flex-end;
    margin-top: -38px;
    margin-bottom: 10px;
  }
}

@media (max-width: 640px) {
  .visualization-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .chart-controls {
    align-self: flex-start;
    margin: 0 0 10px;
  }
}

.chart-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 8px;
  z-index: 1000;
  background-color: #ffffff;
  padding: 7px 10px;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.chart-controls button {
  width: 30px;
  height: 30px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.chart-controls button:hover {
  background: #409EFF;
  color: white;
}

.chart-controls i {
  font-size: 16px;
  color: #4b5563;
}

.chart-controls .el-button:hover i {
  color: #2563eb;
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
