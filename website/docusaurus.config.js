// @ts-check
// 这是本站（SqlParserX 官网文档模板）的 Docusaurus 总配置。
// 参考 DTStack/Taier 官网结构复刻，并内置：
//   1) 深浅色主题切换  2) 文档版本管理  3) 多语言 i18n  4) GitHub Pages 一键发布
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// 运行在 Node 侧，不要在这里写浏览器代码。
/** @type {import('@docusaurus/types').Config} */
const config = {
  // -------- 品牌信息（改这里即可换品牌） --------
  title: 'SqlParserX',
  tagline: '多方言 SQL 解析引擎与工具包',
  favicon: 'img/logo.png',

  // 兼容未来的 Docusaurus v4
  future: {
    v4: true,
  },

  // -------- 部署信息（GitHub Pages 必改） --------
  // 组织/用户站点：https://<org>.github.io，baseUrl 用 '/'
  // 项目站点：https://<org>.github.io/<repo>/，baseUrl 用 '/<repo>/'
  url: 'https://dtstack.github.io',
  baseUrl: '/SqlParserX/',
  organizationName: 'DTStack', // GitHub 组织/用户名
  projectName: 'SqlParserX', // GitHub 仓库名
  deploymentBranch: 'gh-pages', // docusaurus deploy 推送到的分支
  trailingSlash: false,

  // -------- 质量约束 --------
  onBrokenLinks: 'throw',
  markdown: {
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  themes: ['@docusaurus/theme-mermaid'],

  // -------- 多语言 i18n：能力 3 --------
  i18n: {
    defaultLocale: 'zh-Hans',
    locales: ['zh-Hans', 'en'],
    localeConfigs: {
      'zh-Hans': {label: '简体中文', direction: 'ltr', htmlLang: 'zh-Hans'},
      en: {label: 'English', direction: 'ltr', htmlLang: 'en'},
    },
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          // 每页「编辑此页」链接，替换成你的仓库地址
          editUrl:
            'https://github.com/DTStack/SqlParserX/tree/main/website/',
          // 显示「最后更新时间/作者」需要站点位于 Git 仓库中；
          // 若已纳入 Git 管理，可改为 true 开启。
          showLastUpdateAuthor: false,
          showLastUpdateTime: false,
          // -------- 版本管理：能力 2 --------
          // 新增版本：npm run docs:version 1.0.0
          versions: {
            current: {
              label: 'Next 🚧',
              // 当前开发版（未发布）的访问路径为 /docs/next/
            },
          },
          lastVersion: '1.0',
        },
        blog: {
          showReadingTime: true,
          feedOptions: {
            type: ['rss', 'atom'],
            xslt: true,
          },
          editUrl:
            'https://github.com/DTStack/SqlParserX/tree/main/website/',
          onInlineTags: 'warn',
          onInlineAuthors: 'warn',
          onUntruncatedBlogPosts: 'warn',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
        // 如需接入 Google Analytics，取消下面注释并填入你的 trackingID
        // gtag: {
        //   trackingID: 'G-XXXXXXXXXX',
        //   anonymizeIP: true,
        // },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'img/social-card.svg',

      // -------- 深浅色主题切换：能力 1 --------
      colorMode: {
        defaultMode: 'light',
        disableSwitch: false,
        respectPrefersColorScheme: true, // 跟随系统
      },

      announcementBar: {
        id: 'announcement',
        content: '⭐ 如果你觉得 SqlParserX 有用，欢迎到 GitHub 点个 Star',
        backgroundColor: '#fafbfc',
        textColor: '#091E42',
        isCloseable: true,
      },

      navbar: {
        title: 'SqlParserX',
        logo: {
          alt: 'SqlParserX Logo',
          src: 'img/logo.png',
        },
        items: [
          {to: '/', label: '首页', position: 'left'},
          {
            type: 'docSidebar',
            sidebarId: 'docs',
            position: 'left',
            label: '文档',
          },
          {to: '/blog', label: '博客', position: 'left'},
          {to: '/community', label: '社区', position: 'left'},
          // 版本下拉：多版本切换
          {type: 'docsVersionDropdown', position: 'right'},
          // 语言下拉：多语种切换
          {type: 'localeDropdown', position: 'right'},
          {
            href: 'https://github.com/DTStack/SqlParserX',
            position: 'right',
            className: 'header-github-link',
            'aria-label': 'GitHub repository',
          },
        ],
      },

      footer: {
        style: 'light',
        links: [
          {
            title: '文档',
            items: [
              {label: '介绍', to: '/docs/about/introduction'},
              {label: '快速开始', to: '/docs/quickstart/setup'},
              {label: '参与贡献', to: '/docs/contributing'},
            ],
          },
          {
            title: '社区',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/DTStack/SqlParserX',
              },
              {label: '博客', to: '/blog'},
              {label: '社区', to: '/community'},
            ],
          },
          {
            title: '更多',
            items: [
              {label: '发行版', href: 'https://github.com/DTStack/SqlParserX/releases'},
              {label: '架构设计', to: '/docs/about/architecture'},
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} dtstack · SqlParserX`,
      },

      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['bash', 'json', 'nginx', 'java', 'sql', 'python'],
      },
    }),
};

export default config;
