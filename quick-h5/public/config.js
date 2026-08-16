/*
 * H5 运行时配置（部署可改，不需要重新打包）
 *
 * 约束与目的：
 * 1) 该文件位于 public 目录，构建时会被原样拷贝到 dist 根目录，不参与打包压缩；
 * 2) 部署后可仅修改 baseUrl 来切换后端地址（开发/测试/生产）；
 * 3) 仅放“非敏感配置”，不要在这里放 token、密钥等敏感信息。
 */
window.__APP_CONFIG__ = {
  // 后端 API 网关地址（示例：'https://api.example.com'）
  baseUrl: 'http://localhost:8080',
}

