<template>
  <view class="onboarding">
    <swiper class="swiper" :current="currentIndex" @change="handleSwiperChange" :indicator-dots="false"
      :autoplay="false" :circular="false">
      <!-- Step 1: Discover Opportunities -->
      <swiper-item>
        <view class="slide">
          <view class="illustration">
            <view class="scene scene-1">
              <view class="character">👨‍🌾</view>
              <view class="tree">🌳</view>
              <view class="tree tree-2">🌲</view>
              <view class="sun">☀️</view>
              <view class="cloud">☁️</view>
            </view>
          </view>
          <view class="content">
            <text class="title">发现机会</text>
            <text class="subtitle">Discover Opportunities</text>
            <text class="description">探索丰富多彩的志愿活动，找到最适合你的服务机会，让每一份付出都充满意义</text>
          </view>
        </view>
      </swiper-item>

      <!-- Step 2: Track Your Impact -->
      <swiper-item>
        <view class="slide">
          <view class="illustration">
            <view class="scene scene-2">
              <view class="character">👵</view>
              <view class="helper">🧑‍⚕️</view>
              <view class="heart">❤️</view>
              <view class="sparkle sparkle-1">✨</view>
              <view class="sparkle sparkle-2">✨</view>
            </view>
          </view>
          <view class="content">
            <text class="title">记录影响</text>
            <text class="subtitle">Track Your Impact</text>
            <text class="description">记录每一次志愿服务，见证你的成长轨迹，用数据展现你对社区的贡献</text>
          </view>
        </view>
      </swiper-item>

      <!-- Step 3: Join a Community -->
      <swiper-item>
        <view class="slide">
          <view class="illustration">
            <view class="scene scene-3">
              <view class="teacher">👨‍🏫</view>
              <view class="student student-1">👧</view>
              <view class="student student-2">👦</view>
              <view class="book">📚</view>
              <view class="star star-1">⭐</view>
              <view class="star star-2">🌟</view>
            </view>
          </view>
          <view class="content">
            <text class="title">加入社区</text>
            <text class="subtitle">Join a Community</text>
            <text class="description">与志同道合的伙伴一起，分享经验，互相鼓励，共同创造更美好的社区</text>
          </view>
        </view>
      </swiper-item>

      <!-- Step 4: Get Started -->
      <swiper-item>
        <view class="slide slide-final">
          <view class="illustration">
            <view class="scene scene-4">
              <view class="logo-icon">🎓</view>
              <view class="circle circle-1"></view>
              <view class="circle circle-2"></view>
              <view class="circle circle-3"></view>
            </view>
          </view>
          <view class="content">
            <text class="title">开始你的志愿之旅</text>
            <text class="subtitle">Start Your Journey</text>
            <text class="description">加入我们，让每一份善意都被看见，让每一次付出都有回响</text>
          </view>
          <view class="actions">
            <button class="btn-primary" @click="handleLogin">
              <text>登录</text>
            </button>
            <button class="btn-secondary" @click="handleSignup">
              <text>注册账号</text>
            </button>
            <text class="skip-text" @click="handleSkip">跳过引导</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <!-- Indicators (hidden on last page) -->
    <view v-if="currentIndex < 3" class="indicators">
      <view v-for="(item, index) in 3" :key="index" class="indicator" :class="{ active: currentIndex === index }">
      </view>
    </view>

    <!-- Skip Button (except on last screen) -->
    <view v-if="currentIndex < 3" class="skip-btn" @click="handleSkip">
      <text>跳过</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { markOnboardingAsSeen } from '@/utils/onboarding'

const currentIndex = ref(0)

const handleSwiperChange = (e) => {
  currentIndex.value = e.detail.current
}

const handleLogin = () => {
  markOnboardingAsSeen()
  uni.reLaunch({
    url: '/pages/index/index'
  })
}

const handleSignup = () => {
  markOnboardingAsSeen()
  uni.reLaunch({
    url: '/pages/auth/register/register'
  })
}

const handleSkip = () => {
  markOnboardingAsSeen()
  uni.reLaunch({
    url: '/pages/index/index'
  })
}
</script>

<style scoped>
.onboarding {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #FFF5EE 0%, #F7FAFC 100%);
  position: relative;
  overflow: hidden;
}

.swiper {
  width: 100%;
  height: 100%;
}

.slide {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 48rpx 160rpx;
  height: 100%;
}

.slide-final {
  justify-content: flex-start;
  padding-top: 120rpx;
  padding-bottom: 200rpx;
}

/* Illustration Scenes */
.illustration {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 40rpx;
}

.scene {
  position: relative;
  width: 500rpx;
  height: 500rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-final .scene {
  width: 450rpx;
  height: 450rpx;
}

/* Scene 1: Planting Trees */
.scene-1 .character {
  font-size: 160rpx;
  animation: float 3s ease-in-out infinite;
  z-index: 2;
}

.scene-1 .tree {
  position: absolute;
  font-size: 140rpx;
  left: 80rpx;
  bottom: 100rpx;
  animation: grow 2s ease-out;
}

.scene-1 .tree-2 {
  left: auto;
  right: 100rpx;
  bottom: 120rpx;
  animation-delay: 0.3s;
}

.scene-1 .sun {
  position: absolute;
  font-size: 100rpx;
  top: 40rpx;
  right: 60rpx;
  animation: rotate 20s linear infinite;
}

.scene-1 .cloud {
  position: absolute;
  font-size: 80rpx;
  top: 80rpx;
  left: 40rpx;
  animation: drift 15s ease-in-out infinite;
  opacity: 0.8;
}

/* Scene 2: Helping Elderly */
.scene-2 .character {
  font-size: 180rpx;
  position: absolute;
  left: 100rpx;
  animation: float 3s ease-in-out infinite;
}

.scene-2 .helper {
  font-size: 160rpx;
  position: absolute;
  right: 100rpx;
  animation: float 3s ease-in-out infinite 0.5s;
}

.scene-2 .heart {
  font-size: 120rpx;
  animation: heartbeat 1.5s ease-in-out infinite;
  z-index: 3;
}

.scene-2 .sparkle {
  position: absolute;
  font-size: 60rpx;
  animation: twinkle 2s ease-in-out infinite;
}

.scene-2 .sparkle-1 {
  top: 80rpx;
  left: 120rpx;
}

.scene-2 .sparkle-2 {
  bottom: 100rpx;
  right: 140rpx;
  animation-delay: 1s;
}

/* Scene 3: Teaching Kids */
.scene-3 .teacher {
  font-size: 180rpx;
  animation: float 3s ease-in-out infinite;
  z-index: 2;
}

.scene-3 .student {
  position: absolute;
  font-size: 140rpx;
  animation: bounce 2s ease-in-out infinite;
}

.scene-3 .student-1 {
  left: 60rpx;
  bottom: 120rpx;
}

.scene-3 .student-2 {
  right: 60rpx;
  bottom: 100rpx;
  animation-delay: 0.5s;
}

.scene-3 .book {
  position: absolute;
  font-size: 100rpx;
  top: 100rpx;
  animation: float 3s ease-in-out infinite 1s;
}

.scene-3 .star {
  position: absolute;
  font-size: 60rpx;
  animation: twinkle 2s ease-in-out infinite;
}

.scene-3 .star-1 {
  top: 60rpx;
  left: 80rpx;
}

.scene-3 .star-2 {
  top: 80rpx;
  right: 100rpx;
  animation-delay: 1s;
}

/* Scene 4: Logo */
.scene-4 {
  animation: scaleIn 1s ease-out;
}

.scene-4 .logo-icon {
  font-size: 240rpx;
  animation: float 3s ease-in-out infinite;
  z-index: 2;
  filter: drop-shadow(0 8rpx 24rpx rgba(255, 140, 66, 0.3));
}

.scene-4 .circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.scene-4 .circle-1 {
  width: 400rpx;
  height: 400rpx;
  background: linear-gradient(135deg, #FF8C42 0%, #FFB84D 100%);
  animation: pulse 3s ease-in-out infinite;
}

.scene-4 .circle-2 {
  width: 560rpx;
  height: 560rpx;
  background: linear-gradient(135deg, #7BC96F 0%, #A8D99C 100%);
  animation: pulse 3s ease-in-out infinite 0.5s;
}

.scene-4 .circle-3 {
  width: 720rpx;
  height: 720rpx;
  background: linear-gradient(135deg, #FFB84D 0%, #7BC96F 100%);
  animation: pulse 3s ease-in-out infinite 1s;
}

/* Content */
.content {
  text-align: center;
  padding: 0 32rpx;
}

.title {
  display: block;
  font-size: 56rpx;
  font-weight: 700;
  color: #2D3748;
  margin-bottom: 16rpx;
  line-height: 1.3;
}

.subtitle {
  display: block;
  font-size: 32rpx;
  font-weight: 500;
  background: linear-gradient(135deg, #FF8C42 0%, #7BC96F 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 32rpx;
  letter-spacing: 1rpx;
}

.description {
  display: block;
  font-size: 30rpx;
  color: #718096;
  line-height: 1.8;
  font-weight: 400;
}

/* Actions */
.actions {
  width: 100%;
  padding: 0 48rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-top: 60rpx;
}

.btn-primary {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #FF8C42 0%, #FFB84D 100%);
  color: #fff;
  border-radius: 24rpx;
  font-size: 34rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 32rpx rgba(255, 140, 66, 0.35);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 16rpx rgba(255, 140, 66, 0.35);
}

.btn-secondary {
  width: 100%;
  height: 96rpx;
  background: #fff;
  color: #FF8C42;
  border: 3rpx solid #FF8C42;
  border-radius: 24rpx;
  font-size: 34rpx;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-secondary:active {
  background: #FFE5D9;
}

.skip-text {
  text-align: center;
  font-size: 28rpx;
  color: #A0AEC0;
  padding: 24rpx 0 8rpx;
  font-weight: 500;
}

/* Indicators */
.indicators {
  position: absolute;
  bottom: 100rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 16rpx;
  z-index: 10;
}

.indicator {
  width: 16rpx;
  height: 16rpx;
  border-radius: 8rpx;
  background: #E2E8F0;
  transition: all 0.3s ease;
}

.indicator.active {
  width: 48rpx;
  background: linear-gradient(90deg, #FF8C42 0%, #7BC96F 100%);
}

/* Skip Button */
.skip-btn {
  position: absolute;
  top: 60rpx;
  right: 48rpx;
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 48rpx;
  font-size: 28rpx;
  color: #718096;
  font-weight: 500;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  z-index: 10;
}

/* Animations */
@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-20rpx);
  }
}

@keyframes grow {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

@keyframes drift {

  0%,
  100% {
    transform: translateX(0);
  }

  50% {
    transform: translateX(40rpx);
  }
}

@keyframes heartbeat {

  0%,
  100% {
    transform: scale(1);
  }

  25% {
    transform: scale(1.2);
  }

  50% {
    transform: scale(1);
  }
}

@keyframes twinkle {

  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.3);
  }
}

@keyframes bounce {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-30rpx);
  }
}

@keyframes pulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 0.15;
  }

  50% {
    transform: scale(1.1);
    opacity: 0.25;
  }
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
