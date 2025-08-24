<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <div class="logo">
          <el-icon size="32" color="#409eff">
            <School />
          </el-icon>
          <h1>高校志愿服务数字化系统</h1>
        </div>
        <p class="subtitle">College Volunteer Service Digital System</p>
      </div>
      
      <div class="auth-content">
        <router-view />
      </div>
      
      <div class="auth-footer">
        <p>&copy; 2024 CVS系统. All rights reserved.</p>
      </div>
    </div>
    
    <div class="auth-background">
      <div class="bg-pattern"></div>
    </div>
  </div>
</template>

<script setup>
import { School } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  // background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-image: url('@/assets/images/tree.jpg');
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.auth-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 2;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 8px;
    
    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.auth-content {
  margin-bottom: 20px;
}

.auth-footer {
  text-align: center;
  
  p {
    margin: 0;
    color: #909399;
    font-size: 12px;
  }
}

.auth-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

@media (max-width: 768px) {
  .auth-container {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .auth-header .logo h1 {
    font-size: 20px;
  }
}
</style>
