// @ts-check

/**
 * 侧边栏采用「显式手动定义」，分类、顺序、折叠态完全可控。
 * 新增文档后，记得把它的 docId 加到下面的 items 中。
 *
 * @type {import('@docusaurus/plugin-content-docs').SidebarsConfig}
 */
const sidebars = {
  docs: [
    'intro',
    {
      type: 'category',
      label: '关于 SqlParserX',
      collapsed: false,
      items: ['about/introduction', 'about/architecture'],
    },
    {
      type: 'category',
      label: '快速开始',
      collapsed: false,
      items: ['quickstart/setup'],
    },
    {
      type: 'category',
      label: '核心解析能力使用',
      collapsed: false,
      items: [
        'core/field-lineage',
        'core/table-lineage',
        'core/table-parse',
        'core/function-parse',
      ],
    },
    {
      type: 'category',
      label: '贡献指南',
      collapsed: true,
      items: ['contributing'],
    },
  ],
};

export default sidebars;