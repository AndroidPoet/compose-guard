import { useConfig } from 'nextra-theme-docs'
import { useRouter } from 'next/router'

const Logo = () => (
  <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700 }}>
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 2 4 5v6c0 5 3.4 8.7 8 10 4.6-1.3 8-5 8-10V5l-8-3Z"
        fill="#7F52FF"
      />
      <path
        d="m9.2 12.2 1.9 1.9 3.9-4.1"
        stroke="#fff"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
    <span>ComposeGuard</span>
  </span>
)

export default {
  logo: <Logo />,
  project: {
    link: 'https://github.com/AndroidPoet/compose-guard',
  },
  docsRepositoryBase: 'https://github.com/AndroidPoet/compose-guard/tree/main/website',
  color: {
    hue: 255,
    saturation: 100,
  },
  footer: {
    content: (
      <span>
        Apache 2.0 © {new Date().getFullYear()}{' '}
        <a href="https://github.com/AndroidPoet/compose-guard" target="_blank" rel="noreferrer">
          ComposeGuard
        </a>
        . Catch Jetpack Compose mistakes as you type.
      </span>
    ),
  },
  head: function useHead() {
    const { frontMatter } = useConfig()
    const { asPath } = useRouter()
    const pageTitle = frontMatter?.title
    const title = pageTitle ? `${pageTitle} – ComposeGuard` : 'ComposeGuard'
    const description =
      frontMatter?.description ??
      'ComposeGuard — an Android Studio & IntelliJ IDEA plugin that surfaces Jetpack Compose best-practice rule violations live in the editor, with inline highlights, gutter icons, and one-click fixes.'
    const base = 'https://androidpoet.github.io/compose-guard'
    const path = asPath === '/' ? '' : asPath.split('?')[0].split('#')[0]
    const canonical = `${base}${path}`
    const ogImage = `${base}/favicon.svg`
    return (
      <>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>{title}</title>
        <meta name="description" content={description} />
        <link rel="canonical" href={canonical} />
        <link rel="icon" href={`${base}/favicon.svg`} type="image/svg+xml" />
        <meta name="theme-color" content="#7F52FF" />
        <meta property="og:type" content="website" />
        <meta property="og:site_name" content="ComposeGuard" />
        <meta property="og:url" content={canonical} />
        <meta property="og:title" content={pageTitle ?? 'ComposeGuard'} />
        <meta property="og:description" content={description} />
        <meta property="og:image" content={ogImage} />
        <meta name="twitter:card" content="summary_large_image" />
        <meta name="twitter:title" content={pageTitle ?? 'ComposeGuard'} />
        <meta name="twitter:description" content={description} />
        <meta name="twitter:image" content={ogImage} />
      </>
    )
  },
  sidebar: {
    defaultMenuCollapseLevel: 1,
  },
  toc: {
    backToTop: true,
  },
  navigation: {
    prev: true,
    next: true,
  },
  darkMode: true,
}
