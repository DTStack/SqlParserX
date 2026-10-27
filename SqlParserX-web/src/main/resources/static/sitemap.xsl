<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:s="http://www.sitemaps.org/schemas/sitemap/0.9">
  <xsl:output method="html" encoding="UTF-8"/>
  <xsl:template match="/">
    <html lang="zh-CN">
      <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <title>SqlParserX Sitemap</title>
        <style>
          body { margin: 0; background: #f5f7fa; color: #1f2937; font: 14px/1.6 -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
          main { max-width: 1120px; margin: 40px auto; padding: 0 20px; }
          h1 { margin-bottom: 4px; font-size: 28px; }
          p { margin-top: 0; color: #64748b; }
          table { width: 100%; border-collapse: collapse; overflow: hidden; background: #fff; border-radius: 10px; box-shadow: 0 4px 18px rgba(15, 23, 42, .08); }
          th, td { padding: 12px 16px; border-bottom: 1px solid #e5e7eb; text-align: left; }
          th { background: #eef2ff; color: #3730a3; }
          tr:last-child td { border-bottom: 0; }
          tr:hover td { background: #f8fafc; }
          a { color: #2563eb; text-decoration: none; overflow-wrap: anywhere; }
          a:hover { text-decoration: underline; }
          .number { width: 48px; color: #64748b; }
          @media (max-width: 720px) { .optional { display: none; } th, td { padding: 10px; } }
        </style>
      </head>
      <body>
        <main>
          <h1>SqlParserX Sitemap</h1>
          <p>共 <xsl:value-of select="count(s:urlset/s:url)"/> 个可收录页面</p>
          <table>
            <thead>
              <tr>
                <th class="number">#</th>
                <th>页面地址</th>
                <th class="optional">最后更新</th>
                <th class="optional">更新频率</th>
                <th class="optional">优先级</th>
              </tr>
            </thead>
            <tbody>
              <xsl:for-each select="s:urlset/s:url">
                <tr>
                  <td class="number"><xsl:value-of select="position()"/></td>
                  <td><a href="{s:loc}"><xsl:value-of select="s:loc"/></a></td>
                  <td class="optional"><xsl:value-of select="s:lastmod"/></td>
                  <td class="optional"><xsl:value-of select="s:changefreq"/></td>
                  <td class="optional"><xsl:value-of select="s:priority"/></td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
        </main>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
