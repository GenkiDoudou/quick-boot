declare module '@quickboot/lite-rum' {
  export const SDK_VERSION: string
  export function clearSessionId(): void
  export function getOrCreateSessionId(): string
  export function resetSessionId(): string
  export function configureSessionStorage(adapter: {
    getItem: (k: string) => string | null
    setItem: (k: string, v: string) => void
    removeItem: (k: string) => void
  } | null): void

  export class LiteRum {
    constructor(options?: Record<string, unknown>)
    config: Record<string, unknown>
    SDK_VERSION: string
    start: (extra?: Record<string, unknown>) => unknown
    destroy: () => void
    stop: () => void
    setUin: (uin: string) => void
    setConfig: (partial?: Record<string, unknown>) => void
    trackPv: (page?: string) => void
    trackAction: (action: string, extra?: Record<string, unknown>) => void
    trackApi: (api: Record<string, unknown>) => void
    trackError: (message: string, extra?: Record<string, unknown>) => void
    flush: () => Promise<void>
    bindVueRouter: (router: unknown) => void
    bindRouter: (router: unknown) => void
    getActiveOperation: () => { operationId: string, action: string } | null
    getOrCreateSessionId: () => string
  }

  export function createLiteRum(config: Record<string, unknown>): InstanceType<typeof LiteRum>
  export function normalizeConfig(raw?: Record<string, unknown>): Record<string, unknown>
  export default LiteRum
}
