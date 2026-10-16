const demoModules = import.meta.glob('./sql/**/*.sql', {
  eager: true,
  query: '?raw',
  import: 'default'
})

const toolLabels = {
  'lineage': '血缘解析',
  'sql-splitter': 'SQL 切分',
  'syntax-validator': '语法验证',
  'table-parser': '表解析',
  'create-table-parser': '建表解析',
  'parameter-workbench': '参数解析与预编译'
}

function formatSceneName(scene) {
  return scene
    .split('-')
    .filter(Boolean)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

function parseDemo(path, rawSql) {
  const [, tool, dialect, filename] = path.match(/^\.\/sql\/([^/]+)\/([^/]+)\/([^/]+)\.sql$/) || []
  if (!tool || !dialect || !filename) return null

  const lines = rawSql.split(/\r?\n/)
  const metadata = {}
  let firstSqlLine = 0
  while (firstSqlLine < lines.length) {
    const entry = lines[firstSqlLine].match(/^\s*--\s*demo-(title|min-relations):\s*(.*?)\s*$/i)
    if (!entry) break
    metadata[entry[1].toLowerCase()] = entry[2]
    firstSqlLine += 1
  }
  const sql = lines.slice(firstSqlLine).join('\n').trim()
  if (!sql) return null

  return {
    tool,
    dialect: dialect.toUpperCase(),
    scene: filename,
    id: `${tool}:${filename}`,
    title: metadata.title || formatSceneName(filename),
    minRelations: Number(metadata['min-relations']) || null,
    sql
  }
}

const allDemos = Object.entries(demoModules)
  .map(([path, sql]) => parseDemo(path, sql))
  .filter(Boolean)

/** Return the exact-dialect demo when available, otherwise the common demo. */
export function getSqlDemoOptions(tool, dialect) {
  const common = allDemos.filter(demo => demo.tool === tool && demo.dialect === 'COMMON')
  const exact = allDemos.filter(demo => demo.tool === tool && demo.dialect === String(dialect || '').toUpperCase())
  const byScene = new Map(common.map(demo => [demo.scene, demo]))
  exact.forEach(demo => byScene.set(demo.scene, demo))

  return [...byScene.values()]
    .sort((left, right) => left.title.localeCompare(right.title, 'zh-CN'))
    .map(demo => ({
      ...demo,
      sourceLabel: demo.dialect === 'COMMON' ? '通用示例' : `${demo.dialect} 示例`
    }))
}

export function getSqlToolLabel(tool) {
  return toolLabels[tool] || tool
}
