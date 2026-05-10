/**
 * 对应清单：TC_C7SEL_015 - resultKey 与 dataFormatter 解析链
 * 与 TC_C7SEL_005 同源 result-key；独立 formatter 用例待专用 mock。
 */
import {test} from '@playwright/test'

test.describe('TC_C7SEL_015 - resultKey 与 dataFormatter 解析链', () => {
  test('占位-待 formatter 分区', async () => {
    test.skip(true, '待 Dev 页增加 data-formatter 专用分区后实现')
  })
})
