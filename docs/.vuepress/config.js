const { defaultTheme } = require('vuepress')

module.exports = {
    head: [['link', { rel: 'icon', href: 'favicon.svg' }]],
    title: "彗星 Bot",
    description: "多功能跨 IM 平台机器人",
    base: "/",
    theme: defaultTheme({
        repo: 'StarWishsama/Comet-Bot',
        docsDir: 'docs',
        docsBranch: 'main',
        repoLabel: 'GitHub',
        editLinks: true,
        editLinkText: '在 GitHub 上编辑此页',
        lastUpdatedText: '上次更新',
        contributorsText: '贡献者',
        sidebar: 'auto',
        navbar: [
            { text: "首页", link: "/" },
            { text: "快速上手", link: "/getting-started/" },
            { text: "命令列表", link: "/commands/" },
        ],
    }),
};
