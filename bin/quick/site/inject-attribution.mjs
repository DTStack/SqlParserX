import fs from 'node:fs'
import path from 'node:path'

const targetDir = process.argv[2]
const snippet = (process.env.DTSTACK_ATTRIBUTION_HTML || '').trim()

if (!targetDir) {
  console.error('Usage: node inject-attribution.mjs <html-directory>')
  process.exit(1)
}

if (!snippet) {
  console.log('DTSTACK_ATTRIBUTION_HTML is empty; skip attribution injection.')
  process.exit(0)
}

if (!fs.existsSync(targetDir) || !fs.statSync(targetDir).isDirectory()) {
  console.error('HTML directory does not exist: ' + targetDir)
  process.exit(1)
}

const startMarker = '<!-- dtstack-attribution:start -->'
const endMarker = '<!-- dtstack-attribution:end -->'
const managedBlock = /\s*<!-- dtstack-attribution:start -->[\s\S]*?<!-- dtstack-attribution:end -->\s*/gi

function collectHtmlFiles(directory) {
  const files = []
  for (const entry of fs.readdirSync(directory, {withFileTypes: true})) {
    const entryPath = path.join(directory, entry.name)
    if (entry.isDirectory()) {
      files.push(...collectHtmlFiles(entryPath))
    } else if (entry.isFile() && entry.name.endsWith('.html')) {
      files.push(entryPath)
    }
  }
  return files
}

const files = collectHtmlFiles(targetDir)
let injected = 0
let skipped = 0

for (const file of files) {
  const original = fs.readFileSync(file, 'utf8')
  if (!/<\/head>/i.test(original)) {
    skipped += 1
    continue
  }

  const withoutPreviousBlock = original.replace(managedBlock, '\n')
  const block = '\n' + startMarker + '\n' + snippet + '\n' + endMarker + '\n'
  const updated = withoutPreviousBlock.replace(/\s*<\/head>/i, block + '</head>')

  if (updated !== original) {
    fs.writeFileSync(file, updated)
    injected += 1
  }
}

console.log('Attribution injected into ' + injected + ' HTML file(s) under ' + targetDir + '.')
if (skipped > 0) {
  console.log('Skipped ' + skipped + ' HTML file(s) without a </head> tag.')
}
