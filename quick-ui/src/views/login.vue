<template>
  <div class="login-container">
    <!-- 背景 -->
    <div class="login-bg"></div>

    <!-- 熊猫登录卡片 -->
    <div class="panda-login-card">
      <!-- 项目名称 -->
      <div class="project-name">Quick UI</div>

      <!-- 熊猫头部 -->
      <div class="panda-head">
        <div class="panda-ears">
          <div class="panda-ear left"></div>
          <div class="panda-ear right"></div>
        </div>
        <div class="panda-face">
          <div class="panda-eyes">
            <div class="panda-eye left">
              <div class="panda-pupil"></div>
            </div>
            <div class="panda-eye right">
              <div class="panda-pupil"></div>
            </div>
          </div>
          <div class="panda-nose"></div>
          <div class="panda-mouth"></div>
        </div>
      </div>

      <!-- 熊猫身体（卡片） -->
      <div class="panda-body">
        <!-- 熊猫手臂 -->
        <div class="panda-arms">
          <div class="panda-arm left"></div>
          <div class="panda-arm right"></div>
        </div>

        <!-- 登录标签页 -->
        <div class="login-tabs">
          <div
              class="tab-item"
              :class="{ active: activeTab === 'password' }"
              @click="activeTab = 'password'"
          >
            账号登录
          </div>
          <div
              class="tab-item"
              :class="{ active: activeTab === 'phone' }"
              @click="activeTab = 'phone'"
          >
            手机登录
          </div>
          <div
              class="tab-item"
              :class="{ active: activeTab === 'qrcode' }"
              @click="activeTab = 'qrcode'"
          >
            扫码登录
          </div>
        </div>

        <!-- 登录表单容器 -->
        <div class="login-form-container">
          <!-- 账号密码登录 -->
          <div v-show="activeTab === 'password'" class="login-form-wrapper">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="login-form">
              <el-form-item prop="username">
                <el-input
                    v-model="passwordForm.username"
                    type="text"
                    size="large"
                    clearable
                    placeholder="用户名"
                    @keyup.enter="handlePasswordLogin"
                >
                  <template #prefix>
                    <svg-icon icon-class="user" class="input-icon"/>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                    v-model="passwordForm.password"
                    type="password"
                    size="large"
                    clearable
                    show-password
                    placeholder="密码"
                    @keyup.enter="handlePasswordLogin"
                >
                  <template #prefix>
                    <svg-icon icon-class="password" class="input-icon"/>
                  </template>
                </el-input>
              </el-form-item>

              <el-checkbox v-model="passwordForm.rememberMe" style="margin: 0 0 20px 0">
                记住密码
              </el-checkbox>

              <el-button
                  :loading="passwordLoading"
                  size="large"
                  type="primary"
                  style="width: 100%"
                  @click="handlePasswordLogin"
                  class="login-btn"
              >
                {{ passwordLoading ? '登录中...' : '登 录' }}
              </el-button>
              <div v-if="oauthProviders.length" class="oauth-provider-list">
                <div class="oauth-divider">第三方登录</div>
                <el-button
                    v-for="p in oauthProviders"
                    :key="p.providerCode"
                    size="default"
                    style="width: 100%; margin-top: 8px"
                    @click="goOauthProvider(p)"
                >
                  {{ p.providerName }}
                </el-button>
              </div>
            </el-form>
          </div>

          <!-- 手机号登录 -->
          <div v-show="activeTab === 'phone'" class="login-form-wrapper">
            <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" class="login-form">
              <el-form-item prop="phone">
                <el-input
                    v-model="phoneForm.phone"
                    type="text"
                    size="large"
                    clearable
                    placeholder="手机号"
                    maxlength="11"
                >
                  <template #prefix>
                    <svg-icon icon-class="user" class="input-icon"/>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="smsCode">
                <el-row :gutter="10" style="width: 100%">
                  <el-col :span="14">
                    <el-input
                        v-model="phoneForm.smsCode"
                        size="large"
                        clearable
                        placeholder="验证码"
                        maxlength="6"
                    >
                      <template #prefix>
                        <svg-icon icon-class="validCode" class="input-icon"/>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :span="10">
                    <el-button
                        :disabled="smsCountdown > 0 || !phoneForm.phone"
                        size="large"
                        style="width: 100%"
                        @click="sendSms"
                    >
                      {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                    </el-button>
                  </el-col>
                </el-row>
              </el-form-item>

              <el-button
                  :loading="phoneLoading"
                  size="large"
                  type="primary"
                  style="width: 100%"
                  @click="handlePhoneLogin"
                  class="login-btn"
              >
                {{ phoneLoading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form>
          </div>

          <!-- 扫码登录 -->
          <div v-show="activeTab === 'qrcode'" class="qrcode-wrapper">
            <div class="qrcode-container">
              <div class="qrcode-placeholder">
                <div class="qrcode-icon">📱</div>
                <p>请使用手机扫描二维码</p>
                <img :src="qrcodeUrl" alt="二维码" class="qrcode-img" v-if="qrcodeUrl"/>
                <div v-else class="qrcode-loading">
                  <p>生成二维码中...</p>
                </div>
              </div>
              <p class="qrcode-tip">扫码后自动登录，请确保手机已安装应用</p>
            </div>
          </div>
        </div>

        <!-- 熊猫脚 -->
        <div class="panda-feet">
          <div class="panda-foot left"></div>
          <div class="panda-foot right"></div>
        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="login-footer">
      <span>{{ appConfig.copyright }}</span>
    </div>

    <!-- 天爱行为验证码 -->
    <div v-if="captchaVisible" class="tianai-captcha-mask" @click.self="closeCaptcha">
      <div class="tianai-captcha-wrapper">
        <div id="tianai-captcha-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'
import { getLoginCaptchaConfig, phoneLogin, sendSms as sendSmsApi, getQRCode } from '@/api/login'
import { listLoginProviders } from '@/api/oauth/authorize'
import { getToken, setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'
import { appConfig } from '@/config/env'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const passwordFormRef = ref(null)
const phoneFormRef = ref(null)

// 标签页状态
const activeTab = ref('password')

// 账号密码登录
const passwordForm = ref({
  username: 'admin',
  password: 'admin',
  rememberMe: false,
  captchaId: '',
})

const passwordRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
}

const passwordLoading = ref(false)
const oauthProviders = ref([])
const captchaVisible = ref(false)
/** 与后端 qc.login.captcha-enabled 一致；未拉取前默认 true，避免短暂放开校验 */
const loginCaptchaEnabled = ref(true)
let tacInstance = null

onMounted(() => {
    const tokenFromQuery = route.query.access_token
    if (tokenFromQuery && !getToken()) {
        setToken(tokenFromQuery)
        router.push({ path: route.query.redirect || '/' })
        return
    }
    getLoginCaptchaConfig()
        .then((body) => {
            const v = body?.data?.captchaEnabled
            loginCaptchaEnabled.value = v !== false
        })
        .catch(() => {
            loginCaptchaEnabled.value = true
        })
    listLoginProviders()
        .then((res) => {
            oauthProviders.value = res.data || []
        })
        .catch(() => {
            oauthProviders.value = []
        })
})

function goOauthProvider(p) {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const prefix = base.endsWith('/') ? base.slice(0, -1) : base
  const path = p.authorizePath || `/oauth2/client/authorize/${p.providerCode}`
  window.location.href = `${prefix.startsWith('http') ? prefix : window.location.origin + (prefix.startsWith('/') ? prefix : '/' + prefix)}${path}`
}

/** 与 axios baseURL 一致，供 TAC 内 fetch 使用绝对地址 */
function captchaApiBase() {
  const base = import.meta.env.VITE_APP_BASE_API || ''
  const normalized = base.endsWith('/') ? base.slice(0, -1) : base
  if (!normalized) {
    return window.location.origin
  }
  if (normalized.startsWith('http')) {
    return normalized
  }
  const prefix = normalized.startsWith('/') ? normalized : `/${normalized}`
  return `${window.location.origin}${prefix}`
}

function closeCaptcha() {
  captchaVisible.value = false
  if (tacInstance) {
    tacInstance.destroyWindow()
    tacInstance = null
  }
}

function openCaptcha() {
  closeCaptcha()
  captchaVisible.value = true
  nextTick(() => {
    const base = captchaApiBase()
    const config = {
      requestCaptchaDataUrl: `${base}/api/captcha/generate`,
      validCaptchaUrl: `${base}/api/captcha/validate`,
      bindEl: '#tianai-captcha-box',
      validSuccess: (res, _c, tac) => {
        tac.destroyWindow()
        tacInstance = null
        captchaVisible.value = false
        const id = res?.data?.id ?? res?.id
        passwordForm.value.captchaId = id || ''
        doLogin()
      },
      validFail: (_res, _c, tac) => {
        tac.reloadCaptcha()
      },
      btnRefreshFun: (_el, tac) => {
        tac.reloadCaptcha()
      },
      btnCloseFun: (_el, tac) => {
        tac.destroyWindow()
        closeCaptcha()
      },
    }
    const style = { logoUrl: null }
    if (typeof window.initTAC !== 'function') {
      ElMessage.error('验证码脚本未加载，请刷新页面重试')
      captchaVisible.value = false
      return
    }
    window
      .initTAC('./tac', config, style)
      .then((tac) => {
        tacInstance = tac
        tac.init()
      })
      .catch(() => {
        ElMessage.error('验证码初始化失败')
        captchaVisible.value = false
      })
  })
}

// 手机号登录
const phoneForm = ref({
  phone: '',
  smsCode: ''
})

const phoneRules = {
  phone: [
    {required: true, trigger: 'blur', message: '请输入手机号'},
    {pattern: /^1[3-9]\d{9}$/, trigger: 'blur', message: '请输入正确的手机号'}
  ],
  smsCode: [{required: true, trigger: 'blur', message: '请输入验证码'}]
}

const phoneLoading = ref(false)
const smsCountdown = ref(0)

// 扫码登录
const qrcodeUrl = ref('')

// 账号密码登录：开启验证码时弹出天爱；关闭时与后端一致直接登录
function handlePasswordLogin() {
  passwordFormRef.value?.validate((valid) => {
    if (!valid) return
    if (!loginCaptchaEnabled.value) {
      passwordForm.value.captchaId = ''
      doLogin()
      return
    }
    openCaptcha()
  })
}

// 实际执行登录
function doLogin() {
  passwordLoading.value = true
  if (passwordForm.value.rememberMe) {
    Cookies.set('username', passwordForm.value.username, { expires: 30 })
    Cookies.set('password', passwordForm.value.password, { expires: 30 })
    Cookies.set('rememberMe', passwordForm.value.rememberMe, { expires: 30 })
  } else {
    Cookies.remove('username')
    Cookies.remove('password')
    Cookies.remove('rememberMe')
  }
  userStore
    .login(passwordForm.value)
    .then(() => {
      passwordLoading.value = false
      const query = route.query
      const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur]
        }
        return acc
      }, {})
      router.push({ path: route.query.redirect || '/', query: otherQueryParams })
    })
    .catch(() => {
      passwordLoading.value = false
      passwordForm.value.captchaId = ''
    })
}

// 发送短信验证码
function sendSms() {
  phoneFormRef.value?.validateField('phone', (valid) => {
    if (!valid) {
      ElMessage.warning('请输入正确的手机号')
      return
    }

    sendSmsApi(phoneForm.value.phone).then(() => {
      ElMessage.success('验证码已发送到您的手机')
      smsCountdown.value = 60

      const timer = setInterval(() => {
        smsCountdown.value--
        if (smsCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }).catch(() => {
      ElMessage.error('发送验证码失败，请重试')
    })
  })
}

// 手机号登录
function handlePhoneLogin() {
  phoneFormRef.value?.validate((valid) => {
    if (!valid) return
    phoneLoading.value = true

    phoneLogin(phoneForm.value.phone, phoneForm.value.smsCode)
      .then(() => {
        ElMessage.success('登录成功')
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== 'redirect') {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: route.query.redirect || '/', query: otherQueryParams })
      })
      .catch(() => {
        phoneLoading.value = false
      })
  })
}

// 生成二维码
function generateQRCode() {
  getQRCode().then(res => {
    qrcodeUrl.value = res.data.img
  }).catch(() => {
    // 如果获取失败，使用默认二维码
    qrcodeUrl.value = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0id2hpdGUiLz48cmVjdCB4PSIxMCIgeT0iMTAiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgZmlsbD0iYmxhY2siLz48cmVjdCB4PSIxNTAiIHk9IjEwIiB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIGZpbGw9ImJsYWNrIi8+PHJlY3QgeD0iMTAiIHk9IjE1MCIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSJibGFjayIvPjwvc3ZnPg=='
  })
}

// 获取保存的账号密码
function getCookie() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')

  if (username) {
    passwordForm.value.username = username
    passwordForm.value.password = password || ''
    passwordForm.value.rememberMe = Boolean(rememberMe)
  }
}

// 初始化
getCookie()
generateQRCode()
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #26c281 0%, #1abc9c 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.login-bg {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: moveBackground 20s linear infinite;
}

@keyframes moveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

// ============ 熊猫登录卡片 ============

.panda-login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
}

// ============ 项目名称 ============

.project-name {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 15px;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

// ============ 熊猫头部 ============

.panda-head {
  position: relative;
  width: 200px;
  height: 180px;
  margin: 0 auto;
  margin-bottom: -40px;
  animation: pandaBounce 3s ease-in-out infinite;
  z-index: 2;
}

@keyframes pandaBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.panda-ears {
  position: absolute;
  width: 100%;
  height: 100%;
}

.panda-ear {
  position: absolute;
  width: 50px;
  height: 50px;
  background: #000;
  border-radius: 50%;
  top: 0;

  &.left {
    left: 20px;
  }

  &.right {
    right: 20px;
  }
}

.panda-face {
  position: absolute;
  width: 140px;
  height: 140px;
  background: #fff;
  border-radius: 50%;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.panda-eyes {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-top: 35px;
  height: 70px;
}

.panda-eye {
  position: relative;
  width: 40px;
  height: 50px;
  background: #000;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panda-pupil {
  width: 16px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    width: 8px;
    height: 10px;
    background: #000;
    border-radius: 50%;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.panda-nose {
  width: 12px;
  height: 12px;
  background: #000;
  border-radius: 50%;
  margin: 0 auto;
}

.panda-mouth {
  width: 30px;
  height: 15px;
  margin: 8px auto 0;
  border: 2px solid #000;
  border-top: none;
  border-radius: 0 0 30px 30px;
}

// ============ 登录标题 ============

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: #26c281;
  margin-bottom: 30px;
  letter-spacing: 2px;
}

// ============ 熊猫身体（卡片） ============

.panda-body {
  background: #fff;
  border-radius: 30px 30px 50px 50px;
  padding: 60px 30px 60px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  position: relative;
}

.panda-arms {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.panda-arm {
  position: absolute;
  width: 50px;
  height: 80px;
  background: #000;
  border-radius: 50%;
  top: 80px;

  &.left {
    left: -25px;
    transform: rotate(-20deg);
  }

  &.right {
    right: -25px;
    transform: rotate(20deg);
  }
}

// ============ 登录表单容器 ============

.login-form-container {
  position: relative;
  z-index: 1;
}

.login-form {
  .el-form-item {
    margin-bottom: 18px;
  }

  .el-input {
    height: 44px;

    :deep(input) {
      height: 44px;
      font-size: 16px;
      border-radius: 8px;
    }
  }

  .input-icon {
    height: 20px;
    width: 20px;
    color: #26c281;
  }

  .el-checkbox {
    margin-bottom: 18px;
    font-size: 14px;
  }

  .el-button {
    font-weight: 600;
    letter-spacing: 1px;
  }
}

.code-img {
  height: 44px;
  width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.05);
  }
}

.login-btn {
  background: linear-gradient(135deg, #26c281 0%, #1abc9c 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
  height: 44px;
  font-size: 16px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(38, 194, 129, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

// ============ 登录标签页 ============

.login-tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  background: #fafafa;
  margin: 0 -30px;
  padding: 0 30px;
  margin-bottom: 20px;
}

.tab-item {
  flex: 1;
  padding: 12px 0;
  text-align: center;
  cursor: pointer;
  font-size: 14px;
  color: #999;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  white-space: nowrap;
  font-weight: 500;

  &:hover {
    color: #26c281;
  }

  &.active {
    color: #26c281;
    border-bottom-color: #26c281;
  }
}

// ============ 登录表单包装 ============

.login-form-wrapper {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ============ 二维码登录 ============

.qrcode-wrapper {
  padding: 20px 0;
  text-align: center;
  animation: fadeIn 0.3s ease;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 180px;
  background: #f5f7fa;
  border-radius: 12px;
  margin-bottom: 15px;
  border: 2px dashed #26c281;
}

.qrcode-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.qrcode-placeholder p {
  color: #666;
  font-size: 12px;
  margin: 0;
}

.qrcode-img {
  width: 100%;
  height: 100%;
  border-radius: 10px;
}

.qrcode-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;

  p {
    color: #666;
    font-size: 12px;
    margin: 0;
  }
}

.qrcode-tip {
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}

// ============ 熊猫脚 ============

.panda-feet {
  position: absolute;
  width: 100%;
  bottom: 0;
  left: 0;
  display: flex;
  justify-content: space-around;
  padding: 0 30px 20px;
}

.panda-foot {
  width: 60px;
  height: 50px;
  background: #000;
  border-radius: 50% 50% 40% 40%;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

// ============ 天爱行为验证码遮罩 ============

.tianai-captcha-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tianai-captcha-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  min-width: 320px;
}

// ============ 底部信息 ============

.login-footer {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  left: 0;
}

// ============ 响应式设计 ============

/* 平板设备 (768px - 1024px) */
@media (max-width: 1024px) and (min-width: 769px) {
  .panda-login-card {
    max-width: 450px;
  }

  .project-name {
    font-size: 22px;
    margin-bottom: 12px;
  }

  .panda-head {
    width: 180px;
    height: 160px;
    margin-bottom: -35px;
  }

  .panda-ear {
    width: 45px;
    height: 45px;

    &.left {
      left: 15px;
    }

    &.right {
      right: 15px;
    }
  }

  .panda-face {
    width: 130px;
    height: 130px;
    top: 25px;
  }

  .panda-eyes {
    padding-top: 30px;
    height: 65px;
  }

  .panda-eye {
    width: 35px;
    height: 45px;
  }

  .panda-body {
    padding: 55px 25px 55px;
  }
}

/* 手机设备 (480px - 768px) */
@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }

  .panda-login-card {
    max-width: 100%;
  }

  .project-name {
    font-size: 20px;
    margin-bottom: 10px;
  }

  .panda-head {
    width: 160px;
    height: 140px;
    margin-bottom: -30px;
  }

  .panda-ear {
    width: 40px;
    height: 40px;

    &.left {
      left: 10px;
    }

    &.right {
      right: 10px;
    }
  }

  .panda-face {
    width: 120px;
    height: 120px;
    top: 20px;
  }

  .panda-eyes {
    padding-top: 25px;
    height: 60px;
  }

  .panda-eye {
    width: 32px;
    height: 42px;
  }

  .panda-pupil {
    width: 14px;
    height: 18px;

    &::after {
      width: 7px;
      height: 9px;
    }
  }

  .panda-nose {
    width: 10px;
    height: 10px;
  }

  .panda-mouth {
    width: 25px;
    height: 12px;
    margin-top: 6px;
  }

  .panda-body {
    padding: 50px 20px 50px;
    border-radius: 25px 25px 45px 45px;
  }

  .panda-arm {
    width: 45px;
    height: 70px;
    top: 70px;

    &.left {
      left: -20px;
    }

    &.right {
      right: -20px;
    }
  }

  .panda-foot {
    width: 50px;
    height: 40px;
  }

  .login-form {
    .el-form-item {
      margin-bottom: 16px;
    }

    .el-input {
      height: 40px;

      :deep(input) {
        height: 40px;
        font-size: 16px;
      }
    }
  }

  .code-img {
    height: 40px;
  }

  .login-btn {
    height: 40px;
    font-size: 14px;
  }

  .login-footer {
    height: 36px;
    line-height: 36px;
    font-size: 11px;
  }
}

/* 小屏手机 (320px - 480px) */
@media (max-width: 480px) {
  .login-container {
    padding: 8px;
  }

  .project-name {
    font-size: 18px;
    margin-bottom: 8px;
  }

  .panda-head {
    width: 140px;
    height: 120px;
    margin-bottom: -25px;
  }

  .panda-ear {
    width: 35px;
    height: 35px;

    &.left {
      left: 5px;
    }

    &.right {
      right: 5px;
    }
  }

  .panda-face {
    width: 105px;
    height: 105px;
    top: 15px;
  }

  .panda-eyes {
    padding-top: 20px;
    height: 55px;
  }

  .panda-eye {
    width: 28px;
    height: 38px;
  }

  .panda-pupil {
    width: 12px;
    height: 16px;

    &::after {
      width: 6px;
      height: 8px;
    }
  }

  .panda-nose {
    width: 8px;
    height: 8px;
  }

  .panda-mouth {
    width: 20px;
    height: 10px;
    margin-top: 5px;
  }

  .panda-body {
    padding: 45px 16px 45px;
    border-radius: 20px 20px 40px 40px;
  }

  .panda-arm {
    width: 40px;
    height: 60px;
    top: 60px;

    &.left {
      left: -18px;
    }

    &.right {
      right: -18px;
    }
  }

  .panda-foot {
    width: 45px;
    height: 35px;
  }

  .login-form {
    .el-form-item {
      margin-bottom: 14px;
    }

    .el-input {
      height: 36px;

      :deep(input) {
        height: 36px;
        font-size: 16px;
        padding: 6px 8px;
      }
    }

    .el-checkbox {
      font-size: 12px;
    }
  }

  .code-img {
    height: 36px;
  }

  .login-btn {
    height: 36px;
    font-size: 13px;
  }

  .login-footer {
    height: 32px;
    line-height: 32px;
    font-size: 10px;
  }
}

/* 横屏模式 */
@media (max-height: 500px) and (orientation: landscape) {
  .login-container {
    height: auto;
    min-height: 100vh;
    padding: 10px;
  }

  .project-name {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .panda-head {
    width: 120px;
    height: 100px;
    margin-bottom: -20px;
  }

  .panda-body {
    padding: 40px 15px 40px;
  }

  .login-form {
    .el-form-item {
      margin-bottom: 12px;
    }

    .el-input {
      height: 32px;

      :deep(input) {
        height: 32px;
      }
    }
  }

  .login-btn {
    height: 32px;
  }

  .login-footer {
    position: relative;
    margin-top: 10px;
  }
}
</style>
