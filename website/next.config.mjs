import nextra from 'nextra'

const withNextra = nextra({
  theme: 'nextra-theme-docs',
  themeConfig: './theme.config.jsx',
  defaultShowCopyCode: true,
})

// Served from https://androidpoet.github.io/compose-guard/ — a GitHub Pages
// project site lives under a sub-path, so set basePath/assetPrefix accordingly.
const basePath = '/compose-guard'

export default withNextra({
  output: 'export',
  images: { unoptimized: true },
  reactStrictMode: true,
  basePath,
  assetPrefix: basePath,
})
