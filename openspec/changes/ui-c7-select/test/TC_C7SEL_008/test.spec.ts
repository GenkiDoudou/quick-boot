/**
 * 对应清单：TC_C7SEL_008 - remote 防抖与 last-write-wins（观测）
 * 需人为制造快慢请求或改造 mock 延迟后再启用自动化。
 */
import {test} from '@playwright/test'

test.describe('TC_C7SEL_008 - remote 防抖与 last-write-wins（观测）', () => {
  test('占位-待可控慢请求环境', async () => {
    test.skip(true, '依赖可控慢请求/竞态环境，暂用手测与清单 TC_C7SEL_008 步骤')
  })
})
