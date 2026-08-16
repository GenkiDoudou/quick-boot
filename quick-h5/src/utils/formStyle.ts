/**
 * 表单输入框统一外观（传给 u-input :custom-style）。
 * 集中改这里即可同步所有 CRUD 表单。
 */
export const qbInputStyle: Record<string, string> = {
  background: '#f1f5f9',
  borderRadius: '16rpx',
  paddingLeft: '8rpx',
  paddingRight: '8rpx',
  minHeight: '80rpx',
}

/** 主操作按钮统一高度与圆角 */
export const qbPrimaryBtnStyle: Record<string, string> = {
  marginTop: '16rpx',
  height: '92rpx',
  borderRadius: '20rpx',
  fontWeight: '600',
}
