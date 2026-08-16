/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_BASE_API: string
  readonly VITE_APP_BASE_API_NATIVE?: string
  readonly VITE_OAUTH_CLIENT_ID: string
  readonly VITE_OAUTH_CLIENT_SECRET: string
  readonly VITE_APP_LITE_RUM_ENABLED?: string
  readonly VITE_APP_LITE_RUM_APP_ID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
