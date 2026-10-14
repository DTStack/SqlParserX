<template>
  <main class="parameter-workbench">
    <section class="page-heading">
      <div>
        <div class="eyebrow">SQL 工具集</div>
        <h1>SQL 参数提取与预编译</h1>
        <p class="capability-summary">仅针对 SELECT：提取 ${变量} 入参与结果字段，并生成 JDBC ? 占位符及有序绑定值。</p>
      </div>
      <label class="source-picker">
        <span>数据源</span>
        <select v-model.number="selectedDataSource" :disabled="loadingSources || !dataSources.length">
          <option v-if="!dataSources.length" disabled value="">{{ loadingSources ? '正在加载…' : '暂无可用数据源' }}</option>
          <option v-for="source in dataSources" :key="source.value" :value="source.value">
            {{ source.label }}
          </option>
        </select>
      </label>
    </section>

    <nav class="steps" aria-label="处理步骤">
      <button :class="['step', { active: currentStep === 1, complete: parameterResult }]" @click="currentStep = 1">
        <span class="step-number">1</span>
        <span><strong>参数提取</strong><small>解析输入参数与输出字段</small></span>
      </button>
      <span class="step-connector"></span>
      <button :class="['step', { active: currentStep === 2, complete: compileResult }]" @click="openCompileStep">
        <span class="step-number">2</span>
        <span><strong>预编译</strong><small>生成占位符 SQL 与绑定值</small></span>
      </button>
    </nav>

    <section v-if="currentStep === 1" class="workspace">
      <div class="editor-card">
        <div class="card-heading">
          <div><span class="step-kicker">步骤 1</span><h2>参数提取</h2></div>
          <span class="hint">动态变量格式：<code>${参数名}</code></span>
        </div>

        <label class="field-label" for="parameter-sql">SQL 语句</label>
        <textarea id="parameter-sql" v-model="sqlInput" class="sql-editor" spellcheck="false"
                  placeholder="请输入包含 ${参数名} 的 SELECT SQL"></textarea>

        <div class="field-row">
          <div class="field-copy">
            <label class="field-label" for="metadata-json">表字段元数据</label>
            <p>用于匹配参数字段类型和输出字段信息；没有元数据时可留空。</p>
          </div>
          <label class="check-control">
            <input v-model="ignoreSyntaxCheck" type="checkbox">
            <span>忽略非 SELECT 语句校验</span>
          </label>
        </div>
        <textarea id="metadata-json" v-model="metadataJson" class="json-editor" spellcheck="false"></textarea>

        <div class="editor-footer">
          <span class="subtle-note">元数据格式：表名对应字段数组，每个字段包含 name、type、comment。</span>
          <button class="primary-button" :disabled="loadingParse" @click="extractParameters">
            {{ loadingParse ? '正在提取…' : '提取参数' }} <span aria-hidden="true">→</span>
          </button>
        </div>
      </div>

      <aside class="result-card">
        <div class="card-heading result-heading">
          <div><span class="step-kicker">SQL 分析</span><h2>解析结果</h2></div>
          <button v-if="parameterResult" class="text-button" @click="copyJson(parameterResult)">复制 JSON</button>
        </div>
        <div v-if="parameterResult" class="result-body">
          <div class="summary-grid">
            <div class="summary-item"><strong>{{ inputParams.length }}</strong><span>输入参数</span></div>
            <div class="summary-item"><strong>{{ outputParams.length }}</strong><span>输出字段</span></div>
          </div>

          <div class="result-tables">
            <section class="result-table-section">
              <div class="table-section-heading"><h3>输入参数</h3><span>{{ inputParams.length }} 项</span></div>
              <div class="result-table-scroll">
                <table class="result-table input-params-table">
                  <thead>
                    <tr><th>参数名</th><th>必填</th><th>操作符</th><th>类型</th><th>绑定字段 / 描述</th><th>SQL 片段</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="param in inputParams" :key="param.name">
                      <td><code class="table-code">{{ param.name }}</code></td>
                      <td><span :class="['required-tag', { optional: !param.imperative }]">{{ param.imperative ? '是' : '否' }}</span></td>
                      <td>{{ param.operator || '—' }}</td>
                      <td>{{ param.dataType || '—' }}</td>
                      <td><span>{{ param.fieldName || '—' }}</span><small v-if="param.desc">{{ param.desc }}</small></td>
                      <td><code class="sql-cell">{{ param.sqlPart || '—' }}</code></td>
                    </tr>
                    <tr v-if="!inputParams.length"><td colspan="6" class="table-empty">没有发现输入参数</td></tr>
                  </tbody>
                </table>
              </div>
            </section>

            <section class="result-table-section">
              <div class="table-section-heading"><h3>输出字段</h3><span>{{ outputParams.length }} 项</span></div>
              <div class="result-table-scroll">
                <table class="result-table output-fields-table">
                  <thead>
                    <tr><th>#</th><th>输出表达式</th><th>别名</th><th>绑定字段</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="(output, index) in outputParams" :key="`${output.sqlPart}-${index}`">
                      <td class="table-index-cell">{{ index + 1 }}</td>
                      <td><code class="table-code">{{ output.sqlPart || '—' }}</code></td>
                      <td>{{ output.alias || '—' }}</td>
                      <td>{{ bindColumnNames(output.bindColumns) }}</td>
                    </tr>
                    <tr v-if="!outputParams.length"><td colspan="4" class="table-empty">没有发现输出字段</td></tr>
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          <div class="result-actions">
            <button class="secondary-button" @click="openCompileStep">继续配置预编译 <span aria-hidden="true">→</span></button>
          </div>
        </div>
        <div v-else class="empty-state">
          <strong>等待解析</strong>
          <p>填写 SQL 后提取参数，结果会显示在这里。</p>
        </div>
      </aside>
    </section>

    <section v-else class="workspace compile-workspace">
      <div class="editor-card">
        <div class="card-heading">
          <div><span class="step-kicker">步骤 2</span><h2>预编译配置</h2></div>
          <button class="text-button" @click="currentStep = 1">← 返回参数提取</button>
        </div>
        <div class="sql-summary">
          <div><span class="field-label">当前 SQL</span><p>{{ sqlInput.trim() || '尚未输入 SQL' }}</p></div>
          <button class="text-button" @click="currentStep = 1">编辑 SQL</button>
        </div>

        <div class="compile-parameter-panel">
          <div class="table-section-heading">
            <h3>输入参数值</h3>
            <span v-if="compileParams.length">{{ compileParams.length }} 项</span>
          </div>
          <div v-if="compileParams.length" class="compile-params-scroll">
            <table class="result-table compile-params-table">
              <thead><tr><th>参数</th><th>是否必填</th><th>参数值</th></tr></thead>
              <tbody>
                <tr v-for="param in compileParams" :key="param.name">
                  <td>
                    <code class="table-code">{{ param.name }}</code>
                    <small v-if="param.dataType || param.operator">{{ parameterSummary(param) }}</small>
                  </td>
                  <td>
                    <select v-model="param.imperative" class="required-select" :aria-label="param.name + ' 是否必填'">
                      <option :value="true">必填</option>
                      <option :value="false">选填</option>
                    </select>
                  </td>
                  <td>
                    <input v-model="param.valueText" class="value-input" type="text"
                           :placeholder="parameterPlaceholder(param)">
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="compile-params-empty">
            {{ parameterResult ? '当前 SQL 没有输入参数，可以直接生成预编译 SQL。' : '请先完成第 1 步参数提取。' }}
          </div>
        </div>

        <div class="editor-footer">
          <span class="subtle-note">类型与必填规则沿用第 1 步结果；数组值请使用 JSON 格式。</span>
          <button class="primary-button" :disabled="loadingCompile || !parameterResult" @click="runPrepareCompile">
            {{ loadingCompile ? '正在预编译…' : '生成预编译 SQL' }} <span aria-hidden="true">→</span>
          </button>
        </div>
      </div>

      <aside class="result-card compile-result-card">
        <div class="card-heading result-heading">
          <div><span class="step-kicker">编译 SQL</span><h2>预编译结果</h2></div>
          <button v-if="compileResult" class="text-button" @click="copyJson(compileResult)">复制 JSON</button>
        </div>
        <div v-if="compileResult" class="result-body">
          <div class="compile-stat"><strong>{{ (compileResult.inputFields || []).length }}</strong><span>个绑定值</span></div>
          <div class="result-section">
            <h3>预编译 SQL</h3>
            <pre class="sql-output">{{ compileResult.beautifulSql }}</pre>
          </div>
          <div class="result-section">
            <h3>有序绑定值</h3>
            <pre class="json-preview">{{ pretty(compileResult.inputFields || []) }}</pre>
          </div>
        </div>
        <div v-else class="empty-state">
          <strong>等待预编译</strong>
          <p>配置运行时参数后生成占位符 SQL 和绑定值。</p>
        </div>
      </aside>
    </section>
  </main>
</template>

<script>
import { loadDataSourceOptions, parseParameters, prepareCompile } from '@/api/DatasourceApi'

export default {
  name: 'ParameterWorkbench',
  data() {
    return {
      currentStep: 1,
      selectedDataSource: null,
      dataSources: [],
      loadingSources: true,
      loadingParse: false,
      loadingCompile: false,
      sqlInput: 'SELECT u.id, u.name\nFROM users u\nWHERE u.id = ${userId}\n  AND u.status IN (${statuses})',
      metadataJson: '{\n  "users": [\n    { "name": "id", "type": "BIGINT", "comment": "主键" },\n    { "name": "name", "type": "VARCHAR(64)", "comment": "姓名" },\n    { "name": "status", "type": "VARCHAR(32)", "comment": "状态" }\n  ]\n}',
      ignoreSyntaxCheck: false,
      parameterResult: null,
      compileResult: null,
      compileParams: [
        { name: 'userId', valueText: '1001' },
        { name: 'statuses', valueText: '["ACTIVE", "PENDING"]' }
      ]
    }
  },
  computed: {
    inputParams() {
      return this.parameterResult && Array.isArray(this.parameterResult.inputParams)
        ? this.parameterResult.inputParams
        : []
    },
    outputParams() {
      return this.parameterResult && Array.isArray(this.parameterResult.outputParams)
        ? this.parameterResult.outputParams
        : []
    }
  },
  mounted() {
    this.loadDataSources()
  },
  watch: {
    sqlInput() { this.invalidateParameterResult() },
    metadataJson() { this.invalidateParameterResult() },
    ignoreSyntaxCheck() { this.invalidateParameterResult() },
    selectedDataSource() { this.invalidateParameterResult() },
    compileParams: {
      deep: true,
      handler() { this.compileResult = null }
    }
  },
  methods: {
    async loadDataSources() {
      this.loadingSources = true
      try {
        const { options, selectedValue } = await loadDataSourceOptions('MYSQL')
        this.dataSources = options
        this.selectedDataSource = selectedValue
      } catch (error) {
        this.$message.error('数据源列表加载失败: ' + (error.message || '请检查服务连接'))
        this.dataSources = []
        this.selectedDataSource = null
      } finally {
        this.loadingSources = false
      }
    },
    parseJson(text, label) {
      try {
        return JSON.parse(text || '{}')
      } catch (error) {
        this.$message.error(`${label} JSON 格式错误，请检查后重试`)
        return null
      }
    },
    async extractParameters() {
      const sql = this.sqlInput.trim()
      if (!sql) {
        this.$message.error('请输入 SQL 语句')
        return
      }
      const tableColumnsMap = this.parseJson(this.metadataJson, '表字段元数据')
      if (tableColumnsMap === null) return

      this.loadingParse = true
      try {
        const response = await parseParameters({
          datasourceType: this.selectedDataSource,
          sql,
          ignoreSyntaxCheck: this.ignoreSyntaxCheck,
          tableColumnsMap
        })
        this.parameterResult = response.data
        this.compileResult = null
        this.syncCompileParams(this.parameterResult)
        this.$message.success('参数提取完成')
      } catch (error) {
        this.$message.error('参数提取失败: ' + this.errorMessage(error))
      } finally {
        this.loadingParse = false
      }
    },
    invalidateParameterResult() {
      if (!this.parameterResult) return
      this.parameterResult = null
      this.compileResult = null
      this.currentStep = 1
    },
    openCompileStep() {
      if (!this.parameterResult) {
        this.$message.warning('请先完成第 1 步参数提取')
        this.currentStep = 1
        return
      }
      this.currentStep = 2
    },
    valueText(value) {
      if (value === null || value === undefined) return ''
      return typeof value === 'object' ? JSON.stringify(value) : String(value)
    },
    syncCompileParams(result) {
      const existingValues = {}
      this.compileParams.forEach(param => { existingValues[param.name] = param.valueText })
      const params = result && Array.isArray(result.inputParams) ? result.inputParams : []
      this.compileParams = params.map(param => {
        let valueText = ''
        if (param.value !== null && param.value !== undefined) {
          valueText = this.valueText(param.value)
        } else if (param.defaultValue !== null && param.defaultValue !== undefined && param.defaultValue !== '') {
          valueText = this.valueText(param.defaultValue)
        } else if (Object.prototype.hasOwnProperty.call(existingValues, param.name)) {
          valueText = existingValues[param.name]
        }
        return Object.assign({}, param, { valueText })
      })
    },
    async runPrepareCompile() {
      const sql = this.sqlInput.trim()
      if (!this.parameterResult) {
        this.$message.warning('请先完成第 1 步参数提取')
        this.currentStep = 1
        return
      }
      if (!sql) {
        this.$message.error('请先输入 SQL 语句')
        this.currentStep = 1
        return
      }
      const inParams = {}
      const columns = {}
      this.compileParams.forEach(param => {
        const definition = Object.assign({}, param)
        delete definition.valueText
        columns[param.name] = definition
      })
      try {
        this.compileParams.forEach(param => {
          const rawValue = (param.valueText || '').trim()
          if (!rawValue) {
            inParams[param.name] = null
          } else if (rawValue.startsWith('[') || rawValue.startsWith('{')) {
            inParams[param.name] = JSON.parse(rawValue)
          } else {
            inParams[param.name] = rawValue
          }
        })
      } catch (error) {
        this.$message.error('参数值 JSON 格式错误，请检查数组或对象的写法')
        return
      }

      this.loadingCompile = true
      try {
        const response = await prepareCompile({
          datasourceType: this.selectedDataSource,
          sql,
          inParams,
          columns
        })
        this.compileResult = response.data
        this.$message.success('预编译完成')
      } catch (error) {
        this.$message.error('预编译失败: ' + this.errorMessage(error))
      } finally {
        this.loadingCompile = false
      }
    },
    errorMessage(error) {
      return (error.response && error.response.data && error.response.data.msg) || error.message || '请求失败'
    },
    pretty(value) {
      return JSON.stringify(value, null, 2)
    },
    bindColumnNames(columns) {
      if (!Array.isArray(columns) || !columns.length) return '—'
      return columns.map(column => column.name).filter(Boolean).join(', ') || '—'
    },
    parameterSummary(param) {
      return [param.dataType, param.operator].filter(Boolean).join(' · ')
    },
    parameterPlaceholder(param) {
      if (param.defaultValue) return `默认值：${param.defaultValue}`
      return String(param.operator || '').toLowerCase() === 'in'
        ? '数组用 JSON，如 [1, 2]'
        : '输入参数值'
    },
    async copyJson(value) {
      const text = this.pretty(value)
      try {
        await navigator.clipboard.writeText(text)
        this.$message.success('JSON 已复制')
      } catch (error) {
        this.$message.error('复制失败，请手动复制结果')
      }
    }
  }
}
</script>

<style scoped>
.parameter-workbench { display: flex; flex-direction: column; height: calc(100vh - 72px); min-height: 0; padding: 12px clamp(16px, 3vw, 42px) 14px; overflow: hidden; color: #1e2b3e; background: var(--px-bg-muted, #f7f8fa); }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; width: 100%; max-width: 1500px; margin: 0 auto 10px; }
.eyebrow, .step-kicker { color: #526b87; font-size: 11px; font-weight: 700; letter-spacing: .08em; }
.page-heading h1 { margin: 2px 0; color: #17283e; font-size: clamp(20px, 1.8vw, 25px); line-height: 1.2; }
.page-heading p { margin: 0; color: #687386; font-size: 12px; }
.page-heading .capability-summary { display: inline-block; margin-top: 6px; padding: 4px 8px; color: #52647a; background: #f0f3f6; border-left: 2px solid #58718d; border-radius: 0 5px 5px 0; line-height: 1.45; }
.source-picker { display: flex; align-items: center; gap: 10px; flex: none; color: #596579; font-size: 13px; font-weight: 600; }
.source-picker select { display: block; width: 178px; max-width: 31vw; min-width: 0; height: 32px; box-sizing: border-box; padding: 0 32px 0 11px; color: #243047; background-color: #fff; background-image: linear-gradient(45deg, transparent 50%, #526b87 50%), linear-gradient(135deg, #526b87 50%, transparent 50%); background-position: calc(100% - 13px) 13px, calc(100% - 9px) 13px; background-repeat: no-repeat; background-size: 4px 4px; border: 1px solid #cbd4df; border-radius: 7px; appearance: none; -webkit-appearance: none; font: inherit; cursor: pointer; }
.source-picker select:focus { outline: none; border-color: #496e93; box-shadow: 0 0 0 2px rgba(73, 110, 147, .12); }
.source-picker select:disabled { color: #8994a2; background-color: #f0f2f5; cursor: wait; }
.steps { display: flex; align-items: center; width: 100%; max-width: 1500px; margin: 0 auto 10px; padding: 6px 15px; background: #fff; border: 1px solid #e8ecf3; border-radius: 11px; box-shadow: 0 4px 16px rgba(28, 43, 74, .035); }
.step { display: flex; align-items: center; gap: 11px; padding: 2px 4px; color: #8a94a6; text-align: left; background: transparent; border: 0; cursor: pointer; }
.step-number { display: grid; place-items: center; width: 26px; height: 26px; color: #8993a3; background: #eef1f6; border-radius: 50%; font-size: 12px; font-weight: 700; }
.step strong, .step small { display: block; }
.step strong { color: #344055; font-size: 12px; }
.step small { margin-top: 1px; color: #98a1b0; font-size: 10px; }
.step.active .step-number, .step.complete .step-number { color: #fff; background: #315b83; }
.step.active strong, .step.complete strong { color: #294d70; }
.step-connector { width: clamp(30px, 12vw, 150px); height: 1px; margin: 0 20px; background: #e3e8f0; }
.workspace { display: grid; flex: 1; grid-template-columns: minmax(320px, 390px) minmax(0, 1fr); align-items: stretch; gap: 14px; width: 100%; max-width: 1500px; min-height: 0; margin: 0 auto; }
.compile-workspace { grid-template-columns: minmax(460px, 1.35fr) minmax(320px, .75fr); }
.editor-card, .result-card { min-width: 0; min-height: 0; padding: 16px; background: #fff; border: 1px solid #dce2ea; border-radius: 11px; box-shadow: 0 5px 18px rgba(26, 39, 57, .06); }
.editor-card { display: flex; flex-direction: column; overflow: auto; }
.result-card { display: flex; flex-direction: column; overflow: hidden; }
.card-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.card-heading h2 { margin: 2px 0 0; color: #182238; font-size: 16px; }
.hint { color: #7b8699; font-size: 12px; }
.hint code { color: #354e69; background: #e9eef4; border-radius: 4px; padding: 3px 5px; }
.field-label { display: block; margin-bottom: 7px; color: #354157; font-size: 12px; font-weight: 700; }
.sql-editor, .json-editor { display: block; width: 100%; max-width: 100%; min-width: 0; box-sizing: border-box; color: #dbeafe; background: #111a2b; border: 1px solid #111a2b; border-radius: 9px; outline: none; font: 12px/1.55 'SFMono-Regular', Consolas, 'Liberation Mono', monospace; tab-size: 2; resize: none; overflow: auto; }
.sql-editor { flex: none; height: clamp(100px, 20vh, 155px); min-height: 90px; max-height: 155px; padding: 11px 12px; }
.sql-editor:focus, .json-editor:focus { border-color: #5b8cff; box-shadow: 0 0 0 3px rgba(37, 99, 235, .12); }
.sql-editor::placeholder { color: #70809a; }
.field-row { display: flex; justify-content: space-between; align-items: center; gap: 15px; margin: 12px 0 7px; }
.field-copy p, .json-field p { margin: -3px 0 0; color: #8993a3; font-size: 11px; }
.check-control { display: flex; align-items: center; gap: 7px; flex: none; color: #657187; font-size: 11px; cursor: pointer; }
.check-control input { accent-color: #2563eb; }
.json-editor { flex: none; height: clamp(72px, 14vh, 108px); min-height: 68px; max-height: 108px; padding: 9px 11px; color: #bbf7d0; }
.json-editor.large { flex: 1; height: auto; min-height: 0; max-height: none; }
.editor-footer { display: flex; flex: none; align-items: center; justify-content: space-between; gap: 14px; margin-top: 11px; padding-top: 10px; border-top: 1px solid #edf0f5; }
.subtle-note { color: #929baa; font-size: 11px; line-height: 1.5; }
.primary-button, .secondary-button { display: inline-flex; align-items: center; justify-content: center; gap: 10px; min-height: 39px; padding: 0 16px; color: #fff; background: #294e72; border: 1px solid #294e72; border-radius: 8px; font-size: 12px; font-weight: 700; cursor: pointer; transition: .18s ease; white-space: nowrap; }
.primary-button:hover, .secondary-button:hover { background: #1f3e5b; border-color: #1f3e5b; box-shadow: 0 4px 10px rgba(31, 62, 91, .18); }
.primary-button:disabled { opacity: .65; cursor: wait; box-shadow: none; }
.secondary-button { background: #edf2f6; border-color: #d6dfe8; color: #294e72; }
.secondary-button:hover { color: #fff; }
.text-button { padding: 4px 0; color: #2563eb; background: transparent; border: 0; font-size: 11px; font-weight: 600; cursor: pointer; white-space: nowrap; }
.result-heading { margin-bottom: 12px; }
.result-body { display: flex; flex: 1; flex-direction: column; min-height: 0; overflow: hidden; }
.summary-grid { display: grid; flex: none; grid-template-columns: 1fr 1fr; gap: 8px; margin: 3px 0 10px; }
.summary-item { padding: 7px 10px; background: #f1f4f7; border: 1px solid #e1e6ec; border-radius: 8px; }
.summary-item strong { display: inline-block; margin-right: 7px; color: #294d70; font-size: 17px; line-height: 1.1; }
.summary-item span { color: #7b8699; font-size: 10px; }
.result-section { margin: 17px 0; }
.result-section h3 { margin: 0 0 9px; color: #48546a; font-size: 12px; }
.result-tables { display: grid; flex: 1; grid-template-rows: repeat(2, minmax(0, 1fr)); min-height: 0; gap: 10px; }
.result-table-section { display: flex; flex: 1; flex-direction: column; min-height: 0; overflow: hidden; border: 1px solid #e8ecf3; border-radius: 9px; }
.table-section-heading { display: flex; flex: none; align-items: center; justify-content: space-between; padding: 7px 10px; background: #f0f3f7; border-bottom: 1px solid #dce2ea; }
.table-section-heading h3 { margin: 0; color: #29384c; font-size: 11px; font-weight: 700; }
.table-section-heading span { color: #647286; font-size: 10px; font-weight: 600; }
.result-table-scroll { flex: 1; min-height: 0; overflow: auto; }
.result-table { width: 100%; border-collapse: separate; border-spacing: 0; color: #29364a; font-size: 11px; font-weight: 600; text-align: left; }
.input-params-table { min-width: 700px; }
.output-fields-table { min-width: 480px; }
.compile-params-table { min-width: 330px; table-layout: fixed; }
.compile-params-table th:first-child, .compile-params-table td:first-child { width: 96px; }
.compile-params-table th:nth-child(2), .compile-params-table td:nth-child(2) { width: 76px; }
.result-table th, .result-table td { padding: 7px 9px; border-bottom: 1px solid #e5e9ef; vertical-align: top; }
.result-table th { position: sticky; top: 0; z-index: 1; color: #26374e; background: #f2f5f8; font-size: 10px; font-weight: 700; white-space: nowrap; }
.result-table tbody tr:last-child td { border-bottom: 0; }
.result-table tbody tr:hover td { background: #f7f9fb; }
.result-table td small { display: block; margin-top: 3px; color: #53647b; font-size: 9px; font-weight: 600; }
.table-code { color: #192b42; font: 700 10px/1.45 'SFMono-Regular', Consolas, monospace; overflow-wrap: anywhere; }
.sql-cell { display: block; max-width: 260px; color: #667085; font: 9px/1.4 'SFMono-Regular', Consolas, monospace; overflow-wrap: anywhere; }
.table-index-cell { width: 32px; color: #8a94a6; }
.table-empty { height: 50px; color: #a0a8b5; text-align: center !important; vertical-align: middle !important; }
.required-tag.optional { color: #64748b; background: #f1f5f9; }
.compile-parameter-panel { display: flex; flex: 1; flex-direction: column; min-height: 0; overflow: hidden; border: 1px solid #dce2ea; border-radius: 9px; }
.compile-params-scroll { flex: 1; min-height: 0; overflow: auto; }
.compile-params-table td { vertical-align: middle; }
.required-select { display: block; width: 68px; max-width: 100%; min-width: 0; height: 27px; box-sizing: border-box; padding: 0 18px 0 6px; color: #26374e; background-color: #f7f9fb; background-image: linear-gradient(45deg, transparent 50%, #526b87 50%), linear-gradient(135deg, #526b87 50%, transparent 50%); background-position: calc(100% - 9px) 11px, calc(100% - 5px) 11px; background-repeat: no-repeat; background-size: 4px 4px; border: 1px solid #c7d0db; border-radius: 5px; appearance: none; -webkit-appearance: none; font-family: inherit; font-size: 10px; font-weight: 600; }
.required-select:focus { outline: none; border-color: #315d87; box-shadow: 0 0 0 2px rgba(49, 93, 135, .12); }
.compile-params-empty { display: grid; flex: 1; place-items: center; padding: 20px; color: #69778a; font-size: 12px; text-align: center; }
.value-input { width: 100%; max-width: 280px; min-width: 0; height: 30px; padding: 0 8px; box-sizing: border-box; color: #1e2b3e; background: #fff; border: 1px solid #bdc8d6; border-radius: 5px; font-family: inherit; font-size: 11px; line-height: 1.4; }
.value-input:focus { outline: none; border-color: #315d87; box-shadow: 0 0 0 2px rgba(49, 93, 135, .13); }
.compile-result-card .result-body { overflow: auto; }
.parameter-item { padding: 11px 12px; border: 1px solid #e9edf4; border-radius: 9px; }
.parameter-title { display: flex; align-items: center; gap: 7px; }
.parameter-title code, .output-item code { color: #25334d; font-size: 11px; font-weight: 700; }
.required-tag { padding: 2px 5px; color: #b45309; background: #fff7ed; border-radius: 4px; font-size: 9px; }
.parameter-meta { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 7px; }
.parameter-meta span { padding: 2px 6px; color: #68768e; background: #f1f4f8; border-radius: 4px; font-size: 9px; }
.parameter-desc { margin: 7px 0 0; color: #7f899b; font-size: 10px; }
.sql-part { display: block; margin-top: 7px; padding: 6px 7px; color: #6b7280; background: #f8fafc; border-radius: 5px; font: 10px/1.5 'SFMono-Regular', Consolas, monospace; overflow-wrap: anywhere; }
.output-item { display: flex; align-items: flex-start; gap: 9px; padding: 8px 10px; border: 1px solid #edf0f5; border-radius: 8px; }
.output-index { display: grid; place-items: center; flex: none; width: 19px; height: 19px; color: #2563eb; background: #eff6ff; border-radius: 6px; font-size: 9px; font-weight: 700; }
.output-item small { display: block; margin-top: 3px; color: #8b95a5; font-size: 9px; }
.empty-inline { color: #a0a8b5; font-size: 11px; }
.result-actions { display: flex; flex: none; justify-content: flex-end; margin-top: 9px; padding-top: 8px; border-top: 1px solid #edf0f5; }
.empty-state { display: flex; flex: 1; flex-direction: column; align-items: center; justify-content: center; min-height: 0; color: #7f899b; text-align: center; }
.empty-icon { display: grid; place-items: center; width: 45px; height: 45px; margin-bottom: 13px; color: #6894ee; background: #eff5ff; border-radius: 13px; font: 700 17px monospace; }
.empty-state strong { color: #4b566b; font-size: 13px; }
.empty-state p { max-width: 220px; margin: 6px 0; color: #9aa3b1; font-size: 11px; line-height: 1.6; }
.sql-summary { display: flex; align-items: flex-start; justify-content: space-between; gap: 15px; margin: 1px 0 14px; padding: 9px 11px; background: #f7f9fc; border: 1px solid #edf0f5; border-radius: 9px; }
.sql-summary p { margin: 0; color: #596579; font: 10px/1.6 'SFMono-Regular', Consolas, monospace; white-space: pre-wrap; overflow-wrap: anywhere; }
.sql-summary .field-label { margin-bottom: 5px; }
.json-field-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; flex: 1; min-height: 0; }
.json-field { display: flex; flex-direction: column; min-width: 0; }
.json-field p { min-height: 24px; margin: -3px 0 6px; line-height: 1.45; }
.compile-stat { display: flex; align-items: baseline; gap: 7px; margin: 6px 0 17px; color: #788398; font-size: 11px; }
.compile-stat strong { color: #1d4ed8; font-size: 22px; }
.sql-output, .json-preview { max-height: 260px; overflow: auto; margin: 0; padding: 12px; color: #dbeafe; background: #111a2b; border-radius: 9px; font: 11px/1.65 'SFMono-Regular', Consolas, monospace; white-space: pre-wrap; overflow-wrap: anywhere; }
.json-preview { max-height: 200px; color: #bbf7d0; }

@media (max-width: 1050px) {
  .workspace { grid-template-columns: minmax(0, 1fr); grid-template-rows: minmax(0, 1.1fr) minmax(0, .9fr); }
  .json-editor.large { min-height: 0; }
}
@media (max-width: 680px) {
  .parameter-workbench { padding: 9px 10px; }
  .page-heading { align-items: flex-start; flex-direction: row; gap: 10px; }
  .page-heading p { display: none; }
  .page-heading h1 { font-size: 17px; }
  .eyebrow { font-size: 9px; }
  .source-picker { gap: 6px; font-size: 11px; }
  .source-picker select { width: 145px; max-width: 42vw; height: 29px; }
  .source-picker { width: auto; justify-content: flex-end; }
  .steps { padding: 5px 8px; }
  .step { gap: 7px; }
  .step small { display: none; }
  .step-connector { flex: 1; width: auto; margin: 0 8px; }
  .editor-card, .result-card { padding: 11px; }
  .field-row { align-items: flex-start; flex-direction: column; }
  .editor-footer { align-items: flex-start; flex-direction: column; }
  .editor-footer .primary-button { align-self: flex-end; }
  .json-field-grid { grid-template-columns: 1fr; }
  .json-field p { display: none; }
}
</style>
