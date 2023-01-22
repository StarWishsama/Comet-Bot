module.exports = {
    head: [['link', { rel: 'icon', href: 'favicon.svg' }]],
    title: "彗星 Bot",
    description: "多功能跨 IM 平台机器人",
    base: "/",
    themeConfig: {
        repo: 'StarWishsama/Comet-Bot',
        repoLabel: 'GitHub',
        docsDir: 'docs',
        docsBranch: 'dev',
        editLinks: true,
        editLinkText: '在 GitHub 上编辑此页',
        lastUpdated: '上次更新',
        sidebar: {
            'getting-started': [
                '',
                'comet-module'
            ],
            '/commands/': [
                '',
                'project-sekai',
                'sign-in'
            ],
            '/compile': [
                ''
            ],
            '/': [
                ''
            ]
        },
        nav: [
            { text: "首页", link: "/" },
            { text: "快速上手", link: "/getting-started/" },
            { text: "命令列表", link: "/commands/" },
        ],
    }
};
