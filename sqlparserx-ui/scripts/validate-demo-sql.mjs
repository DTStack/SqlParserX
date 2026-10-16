import { readdir, readFile } from 'node:fs/promises'
import { join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const demoRoot = fileURLToPath(new URL('../src/demos/sql/', import.meta.url))
const baseUrl = (process.env.DEMO_SQL_API_BASE_URL || 'http://127.0.0.1:16758').replace(/\/$/, '')
const tools = {
  'lineage': { path: '/api/lineage/column/query', bodyType: 'text' },
  'sql-splitter': { path: '/api/sql/split', bodyType: 'text' },
  'syntax-validator': { path: '/api/datasource/grammarCheck', bodyType: 'text' },
  'table-parser': { path: '/api/datasource/parseTables', bodyType: 'json' },
  'create-table-parser': { path: '/api/datasource/parseCreateTable', bodyType: 'json' },
  'parameter-workbench': { path: '/api/parameter/parse', bodyType: 'json' }
}

async function listSqlFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const nested = await Promise.all(entries.map(entry => {
    const path = join(directory, entry.name)
    return entry.isDirectory() ? listSqlFiles(path) : entry.name.endsWith('.sql') ? [path] : []
  }))
  return nested.flat()
}

function makeRequest(tool, datasourceType, sql) {
  const definition = tools[tool]
  const query = definition.bodyType === 'text' ? `?datasourceType=${datasourceType}` : ''
  const url = `${baseUrl}${definition.path}${query}`
  const headers = { 'Content-Type': definition.bodyType === 'text' ? 'text/plain' : 'application/json' }
  let body = sql

  if (tool === 'table-parser' || tool === 'create-table-parser') {
    body = JSON.stringify({ datasourceType, sql })
  } else if (tool === 'parameter-workbench') {
    body = JSON.stringify({
      datasourceType,
      sql,
      ignoreSyntaxCheck: false,
      tableColumnsMap: {
        users: [
          { name: 'id', type: 'BIGINT' },
          { name: 'name', type: 'VARCHAR(64)' },
          { name: 'status', type: 'VARCHAR(32)' },
          { name: 'tenant_id', type: 'BIGINT' }
        ]
      }
    })
  }

  return fetch(url, { method: 'POST', headers, body })
}

function parseDemoContent(rawSql) {
  const lines = rawSql.split(/\r?\n/)
  const metadata = {}
  let firstSqlLine = 0
  while (firstSqlLine < lines.length) {
    const entry = lines[firstSqlLine].match(/^\s*--\s*demo-(title|min-relations):\s*(.*?)\s*$/i)
    if (!entry) break
    metadata[entry[1].toLowerCase()] = entry[2]
    firstSqlLine += 1
  }
  return {
    sql: lines.slice(firstSqlLine).join('\n').trim(),
    minRelations: Number(metadata['min-relations']) || 0
  }
}

const typesResponse = await fetch(`${baseUrl}/api/datasource/types`)
if (!typesResponse.ok) throw new Error(`Cannot read data-source types from ${baseUrl}: HTTP ${typesResponse.status}`)
const typesPayload = await typesResponse.json()
const dataSources = typesPayload.data || []
if (!dataSources.length) throw new Error('The API returned no data-source types.')

const demos = new Map()
for (const file of await listSqlFiles(demoRoot)) {
  const parts = relative(demoRoot, file).split(/[\\/]/)
  if (parts.length !== 3) continue
  const [tool, dialect, filename] = parts
  const { sql, minRelations } = parseDemoContent(await readFile(file, 'utf8'))
  if (!sql) throw new Error(`Empty demo SQL: ${file}`)
  if (!demos.has(tool)) demos.set(tool, new Map())
  const dialectDemos = demos.get(tool)
  if (!dialectDemos.has(dialect.toUpperCase())) dialectDemos.set(dialect.toUpperCase(), new Map())
  dialectDemos.get(dialect.toUpperCase()).set(filename.replace(/\.sql$/i, ''), { file, sql, minRelations })
}

let passed = 0
const failures = []
for (const [tool, dialectDemos] of demos) {
  for (const source of dataSources) {
    const common = dialectDemos.get('COMMON') || new Map()
    const specific = dialectDemos.get(String(source.label).toUpperCase()) || new Map()
    const scenes = new Set([...common.keys(), ...specific.keys()])
    for (const scene of scenes) {
      const demo = specific.get(scene) || common.get(scene)
      try {
        const response = await makeRequest(tool, source.value, demo.sql)
        const payload = await response.json()
        if (!response.ok || payload.code !== 200) {
          failures.push(`${tool}/${source.label}/${scene}: ${payload.msg || `HTTP ${response.status}`}`)
        } else if (tool === 'lineage' && (!payload.data?.relationData?.length
          || payload.data.relationData.length < demo.minRelations)) {
          failures.push(`${tool}/${source.label}/${scene}: expected at least ${demo.minRelations || 1} lineage relations, got ${payload.data?.relationData?.length || 0}`)
        } else {
          passed += 1
          console.log(`PASS ${tool}/${source.label}/${scene}`)
        }
      } catch (error) {
        failures.push(`${tool}/${source.label}/${scene}: ${error.message}`)
      }
    }
  }
}

console.log(`\nValidated ${passed} demo/source combinations.`)
if (failures.length) {
  console.error(`\n${failures.length} validation failure(s):\n${failures.join('\n')}`)
  process.exitCode = 1
}
