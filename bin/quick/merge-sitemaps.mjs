import fs from 'node:fs'

const [appSitemapPath, websiteSitemapPath, outputPath, siteUrl] = process.argv.slice(2)

if (!appSitemapPath || !websiteSitemapPath || !outputPath || !siteUrl) {
  throw new Error('Usage: node merge-sitemaps.mjs <app-sitemap> <website-sitemap> <output> <site-url>')
}

const readUrls = (file) => {
  if (!fs.existsSync(file)) return []
  const xml = fs.readFileSync(file, 'utf8').replaceAll('__SEO_SITE_URL__', siteUrl.replace(/\/$/, ''))
  return xml.match(/<url>.*?<\/url>/gs) || []
}

const uniqueUrls = new Map()
for (const entry of [...readUrls(appSitemapPath), ...readUrls(websiteSitemapPath)]) {
  const location = entry.match(/<loc>(.*?)<\/loc>/s)?.[1]
  if (location && !uniqueUrls.has(location)) uniqueUrls.set(location, entry)
}

const xml = [
  '<?xml version="1.0" encoding="UTF-8"?>',
  '<?xml-stylesheet type="text/xsl" href="/sitemap.xsl"?>',
  '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
  ...uniqueUrls.values(),
  '</urlset>',
  ''
].join('\n')

fs.writeFileSync(outputPath, xml, 'utf8')
console.log(`Merged ${uniqueUrls.size} URLs into ${outputPath}`)
