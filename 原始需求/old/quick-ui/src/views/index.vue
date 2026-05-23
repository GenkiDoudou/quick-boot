<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="24">
        <div class="welcome-container">
          <h1 class="welcome-title">欢迎进入 {{ appConfig.loginTitle }} 系统</h1>
          <p class="welcome-subtitle">基于 Spring Boot + Vue 3 的快速开发平台</p>
         
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { appConfig } from '@/config/env'

// 页面数据
const version = ref(appConfig.version)

// 页面方法
function goTarget(url) {
  window.open(url, '__blank')
}
</script>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #5e659f 0%, #6a6372 100%);
  
  .welcome-container {
    text-align: center;
    padding: 60px 20px;
    background: transparent;
    border-radius: 12px;
    color: white;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    
      .welcome-title {
        font-size: 2.5rem;
        font-weight: 300;
      margin-bottom: 20px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }
    
    .welcome-subtitle {
      font-size: 1.2rem;
      margin-bottom: 40px;
      opacity: 0.9;
      font-weight: 300;
    }
    
    .welcome-info {
      background: rgba(255, 255, 255, 0.1);
      padding: 30px;
      border-radius: 8px;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      
      p {
        margin: 10px 0;
        font-size: 1rem;
        
        strong {
          font-weight: 600;
          margin-right: 10px;
        }
        
        span {
          color: #f0f0f0;
          font-weight: 500;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .home .welcome-container {
    padding: 40px 15px;
    
    .welcome-title {
      font-size: 2rem;
    }
    
    .welcome-subtitle {
      font-size: 1rem;
    }
  }
}
</style>