<template>
  <div class="landing-page">
    <!-- 导航栏 -->
    <header class="landing-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="32" :color="isScrolled ? '#409EFF' : '#FFFFFF'">
            <Trophy />
          </el-icon>
          <span class="logo-text">志愿服务平台</span>
        </div>
        <nav class="nav-menu">
          <a href="#features" class="nav-link">功能特色</a>
          <a href="#about" class="nav-link">关于我们</a>
          <el-button @click="goToLogin">登录</el-button>
          <el-button @click="goToRegister">注册</el-button>
        </nav>
      </div>
    </header>

    <!-- 主横幅轮播 -->
    <section class="hero-section">
      <el-carousel :interval="5000" height="100vh" arrow="always" indicator-position="outside">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <div class="carousel-slide" :style="{ backgroundImage: `url(${item.backgroundImage})` }">
            <div class="hero-container">
              <div class="hero-content">
                <h1 class="hero-title">{{ item.title }}</h1>
                <p class="hero-subtitle">{{ item.subtitle }}</p>
                <div class="hero-actions">
                  <el-button type="primary" size="large" @click="goToRegister" class="btn-primary">
                    立即开始
                    <el-icon class="ml-2">
                      <Right />
                    </el-icon>
                  </el-button>
                  <el-button size="large" @click="scrollToFeatures" class="btn-secondary">
                    了解更多
                  </el-button>
                </div>
              </div>
              <div class="hero-illustration">
                <img :src="item.image" :alt="item.title" class="illustration-image" />
              </div>
            </div>
            <!-- 向下滚动提示 -->
            <div class="scroll-indicator" @click="scrollToFeatures">
              <el-icon :size="24" color="#FFFFFF">
                <ArrowDown />
              </el-icon>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 功能特色 -->
    <section id="features" class="features-section">
      <h2 class="section-title">功能特色</h2>
      <div class="features-grid">
        <div class="feature-card">
          <el-icon :size="48" color="#409EFF">
            <Calendar />
          </el-icon>
          <h3>活动管理</h3>
          <p>便捷的活动创建、发布和管理功能，让志愿活动组织更轻松</p>
        </div>
        <div class="feature-card">
          <el-icon :size="48" color="#67C23A">
            <UserFilled />
          </el-icon>
          <h3>报名签到</h3>
          <p>在线报名、二维码签到签退，全流程数字化管理</p>
        </div>
        <div class="feature-card">
          <el-icon :size="48" color="#E6A23C">
            <Document />
          </el-icon>
          <h3>记录管理</h3>
          <p>完整的服务记录追踪，自动生成志愿服务证明</p>
        </div>
        <div class="feature-card">
          <el-icon :size="48" color="#F56C6C">
            <Star />
          </el-icon>
          <h3>积分系统</h3>
          <p>志愿服务积分累积，可兑换精美礼品</p>
        </div>
        <div class="feature-card">
          <el-icon :size="48" color="#909399">
            <DataAnalysis />
          </el-icon>
          <h3>数据统计</h3>
          <p>多维度数据分析，直观展示志愿服务成果</p>
        </div>
        <div class="feature-card">
          <el-icon :size="48" color="#409EFF">
            <Medal />
          </el-icon>
          <h3>证书管理</h3>
          <p>在线申请志愿服务证明，快速审核与发放</p>
        </div>
      </div>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="about-section">
      <div class="about-container">
        <h2 class="section-title">关于我们</h2>
        <div class="about-content">
          <p class="about-intro">志愿服务数字化管理平台致力于为学校、社区和组织提供专业的志愿服务管理解决方案。</p>
          <p class="about-intro">我们通过数字化手段，简化志愿活动的组织流程，提升管理效率，让更多人参与到志愿服务中来。</p>
        </div>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">10,000+</div>
            <div class="stat-label">注册志愿者</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">5,000+</div>
            <div class="stat-label">志愿活动</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">50,000+</div>
            <div class="stat-label">服务时长（小时）</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">100+</div>
            <div class="stat-label">合作机构</div>
          </div>
        </div>

        <div class="mission-vision">
          <div class="mission-item">
            <h3>我们的使命</h3>
            <p>通过技术创新推动志愿服务发展，让每一份爱心都能被记录和传递</p>
          </div>
          <div class="mission-item">
            <h3>我们的愿景</h3>
            <p>成为最受信赖的志愿服务管理平台，连接更多志愿者与社会需求</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="landing-footer">
      <p>&copy; 2025 志愿服务平台. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Trophy, Right, Calendar, UserFilled, Document, Star, DataAnalysis, Medal, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

// 导入图片
import plantTreeImg from '@/assets/images/hands1.jpg'
import human1Img from '@/assets/images/human1.jpg'
import human2Img from '@/assets/images/plant_tree.jpg'
import wallpaper1 from '@/assets/images/Wallpaper8.jpg'
import wallpaper2 from '@/assets/images/Wallpaper6.jpg'
import wallpaper3 from '@/assets/images/Wallpaper4.jpg'

// 控制header样式
const isScrolled = ref(false)

// 轮播图数据
const carouselItems = ref([
  {
    title: '让志愿服务更简单',
    subtitle: '数字化管理平台，连接志愿者与社区需求',
    backgroundImage: wallpaper1,
    image: plantTreeImg
  },
  {
    title: '智能活动管理',
    subtitle: '从创建到签到，全流程数字化管理',
    backgroundImage: wallpaper2,
    image: human1Img
  },
  {
    title: '温暖社区服务',
    subtitle: '陪伴老人、关爱儿童、美化环境',
    backgroundImage: wallpaper3,
    image: human2Img
  }
])

const goToLogin = () => {
  router.push('/auth/login')
}

const goToRegister = () => {
  router.push('/auth/register')
}

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}

// 监听滚动
const handleScroll = () => {
  const scrollTop = document.querySelector('.landing-page')?.scrollTop || 0
  isScrolled.value = scrollTop > window.innerHeight * 0.8
}

onMounted(() => {
  const landingPage = document.querySelector('.landing-page')
  if (landingPage) {
    landingPage.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  const landingPage = document.querySelector('.landing-page')
  if (landingPage) {
    landingPage.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  background: #FFFFFF;
  scroll-snap-type: y proximity;
  overflow-y: scroll;
  height: 100vh;
}

/* 导航栏 */
.landing-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  transition: all 0.3s ease;
}

.landing-header.scrolled {
  background: #FFFFFF;
  box-shadow: 0 2px 8px rgba(90, 141, 238, 0.08);
}

.landing-header.scrolled .logo-text {
  color: #5A8DEE;
  text-shadow: none;
}

.landing-header.scrolled .nav-link {
  color: #5F6368;
  text-shadow: none;
}

.landing-header.scrolled .nav-link:hover {
  color: #5A8DEE;
  text-shadow: none;
}

.landing-header.scrolled .nav-menu :deep(.el-button) {
  background: #FFFFFF;
  border: 1px solid #DCDFE6;
  color: #606266;
  backdrop-filter: none;
}

.landing-header.scrolled .nav-menu :deep(.el-button:hover) {
  background: #F5F7FA;
  border-color: #C0C4CC;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: -0.01em;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.3);
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  color: #FFFFFF;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.9375rem;
  transition: all 0.2s ease;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

.nav-link:hover {
  color: #5A8DEE;
  text-shadow: 1px 1px 6px rgba(90, 141, 238, 0.6);
}

.nav-menu :deep(.el-button) {
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #FFFFFF;
}

.nav-menu :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 主横幅轮播 */
.hero-section {
  scroll-snap-align: start;
}

.hero-section :deep(.el-carousel__indicator) {
  background-color: rgba(90, 141, 238, 0.3);
}

.hero-section :deep(.el-carousel__indicator.is-active) {
  background-color: #5A8DEE;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rem 2rem 3rem;
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.carousel-slide::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 0;
}

.hero-container {
  max-width: 1200px;
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  position: relative;
  z-index: 1;
}

.hero-content {
  text-align: left;
}

.hero-title {
  font-size: 3.25rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  line-height: 1.2;
  color: #FFFFFF;
  letter-spacing: -0.02em;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.7);
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 2.5rem;
  color: #FFFFFF;
  font-weight: 400;
  line-height: 1.7;
  text-shadow: 1px 1px 6px rgba(0, 0, 0, 0.6);
}

.hero-actions {
  display: flex;
  gap: 1rem;
}

.btn-primary {
  background: #5A8DEE !important;
  border-color: #5A8DEE !important;
  border-radius: 24px !important;
  padding: 14px 32px !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 12px rgba(90, 141, 238, 0.3);
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: #4A7DD8 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(90, 141, 238, 0.4);
}

.btn-secondary {
  background: #FFFFFF !important;
  border: 2px solid #5A8DEE !important;
  color: #5A8DEE !important;
  border-radius: 24px !important;
  padding: 14px 32px !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #F0F5FF !important;
  transform: translateY(-2px);
}

.hero-illustration {
  display: flex;
  align-items: center;
  justify-content: center;
}

.illustration-image {
  width: 100%;
  max-width: 480px;
  height: auto;
  border-radius: 24px;
  object-fit: cover;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.ml-2 {
  margin-left: 0.5rem;
}

/* 向下滚动提示 */
.scroll-indicator {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  animation: bounce 2s infinite;
  cursor: pointer;
  z-index: 1;
  transition: all 0.3s ease;
}

.scroll-indicator:hover {
  transform: translateX(-50%) scale(1.2);
}

@keyframes bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateX(-50%) translateY(0);
  }

  40% {
    transform: translateX(-50%) translateY(-10px);
  }

  60% {
    transform: translateX(-50%) translateY(-5px);
  }
}

/* 功能特色 */
.features-section {
  background: #FAFBFC;
  padding: 3.5rem 2.5rem;
  scroll-snap-align: start;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  color: #2C3E50;
  letter-spacing: -0.02em;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #5A8DEE 0%, #FFA726 100%);
  margin: 1rem auto 2rem;
  border-radius: 2px;
}

.features-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.feature-card {
  padding: 1.75rem 1.5rem;
  text-align: center;
  background: #FFFFFF;
  border-radius: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(90, 141, 238, 0.15);
  border-color: rgba(90, 141, 238, 0.2);
}

.feature-card :deep(.el-icon) {
  margin-bottom: 1rem;
  transition: transform 0.3s ease;
}

.feature-card:hover :deep(.el-icon) {
  transform: scale(1.1);
}

.feature-card h3 {
  font-size: 1.15rem;
  margin: 0.75rem 0 0.5rem;
  color: #2C3E50;
  font-weight: 600;
}

.feature-card p {
  color: #5F6368;
  line-height: 1.6;
  font-size: 0.875rem;
}

/* 关于我们 */
.about-section {
  background: #1A1A1A;
  padding: 5rem 2.5rem;
  scroll-snap-align: start;
}

.about-section .section-title {
  color: #FFFFFF;
}

.about-section .section-title::after {
  background: linear-gradient(90deg, #5A8DEE 0%, #67C23A 100%);
}

.about-container {
  max-width: 1200px;
  margin: 0 auto;
}

.about-content {
  max-width: 800px;
  margin: 0 auto 3rem;
  text-align: center;
}

.about-intro {
  font-size: 1rem;
  line-height: 1.8;
  margin-bottom: 1rem;
  color: #D1D5DB;
  font-weight: 400;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
  padding: 0 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: #5A8DEE;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #5A8DEE 0%, #67C23A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 0.9rem;
  color: #9CA3AF;
  font-weight: 500;
}

.mission-vision {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.mission-item {
  text-align: center;
  padding: 2rem 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.mission-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-5px);
}

.mission-item h3 {
  font-size: 1.25rem;
  color: #FFFFFF;
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.mission-item p {
  font-size: 0.95rem;
  color: #D1D5DB;
  line-height: 1.6;
}

/* 页脚 */
.landing-footer {
  background: #2A2A2A;
  color: #9CA3AF;
  text-align: center;
  padding: 2.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 0.9375rem;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .hero-content {
    text-align: center;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-illustration {
    order: -1;
  }

  .illustration-image {
    max-width: 320px;
  }
}

@media (max-width: 768px) {
  .hero-section :deep(.el-carousel) {
    height: calc(100vh - 118px) !important;
    min-height: 500px;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .hero-actions {
    flex-direction: column;
    gap: 0.75rem;
  }

  .hero-actions :deep(.el-button) {
    width: 100%;
  }

  .nav-menu {
    gap: 1rem;
  }

  .nav-link {
    display: none;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .section-title {
    font-size: 2rem;
  }

  .features-section,
  .about-section {
    padding: 3rem 1.5rem;
  }
}
</style>
