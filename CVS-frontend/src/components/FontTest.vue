<template>
  <div class="font-test">
    <h1 class="test-heading">字体系统测试 Font System Test</h1>
    
    <div class="test-section">
      <h2>字体大小层级 Font Size Hierarchy</h2>
      <div class="text-4xl">超大标题 4XL Heading</div>
      <div class="text-3xl">大标题 3XL Heading</div>
      <div class="text-2xl">标题大 2XL Heading</div>
      <div class="text-xl">标题中 XL Heading</div>
      <div class="text-lg">标题小 LG Heading</div>
      <div class="text-base">正文 Base Text</div>
      <div class="text-sm">正文小 Small Text</div>
      <div class="text-xs">辅助文字 Extra Small Text</div>
    </div>

    <div class="test-section">
      <h2>字重测试 Font Weight Test</h2>
      <div class="font-light">细体 Light Weight</div>
      <div class="font-normal">正常 Normal Weight</div>
      <div class="font-medium">中等 Medium Weight</div>
      <div class="font-semibold">半粗 Semibold Weight</div>
      <div class="font-bold">粗体 Bold Weight</div>
    </div>

    <div class="test-section">
      <h2>语义化文本 Semantic Text</h2>
      <div class="text-primary">主要文本 Primary Text</div>
      <div class="text-regular">常规文本 Regular Text</div>
      <div class="text-secondary">次要文本 Secondary Text</div>
      <div class="text-placeholder">占位文本 Placeholder Text</div>
    </div>

    <div class="test-section">
      <h2>等宽字体 Monospace Font</h2>
      <code class="font-mono">const message = "Hello, 世界!";</code>
    </div>

    <div class="test-section">
      <h2>混合语言测试 Mixed Language Test</h2>
      <p>这是一段包含中英文的测试文本。This is a test paragraph with mixed Chinese and English content. 它用来验证字体在不同语言环境下的显示效果。The font should render consistently across different languages.</p>
    </div>

    <div class="test-info">
      <h3>当前字体信息 Current Font Info</h3>
      <p>语言: {{ currentLanguage }}</p>
      <p>字体加载状态: {{ fontLoadStatus }}</p>
      <p>设备类型: {{ deviceType }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { detectUserLanguage } from '@/utils/language'

const currentLanguage = ref('')
const fontLoadStatus = ref('')
const deviceType = ref('')

onMounted(() => {
  // 获取当前语言
  currentLanguage.value = detectUserLanguage()
  
  // 检查字体加载状态
  fontLoadStatus.value = document.body.classList.contains('fonts-loaded') ? '已加载' : '回退字体'
  
  // 检查设备类型
  if (document.body.classList.contains('device-mobile')) {
    deviceType.value = '移动设备'
  } else if (document.body.classList.contains('device-tablet')) {
    deviceType.value = '平板设备'
  } else {
    deviceType.value = '桌面设备'
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/utilities/typography' as *;

.font-test {
  padding: var(--space-6);
  max-width: 800px;
  margin: 0 auto;
}

.test-heading {
  @include heading-primary;
  text-align: center;
  margin-bottom: var(--space-8);
  border-bottom: 2px solid var(--primary-color);
  padding-bottom: var(--space-4);
}

.test-section {
  margin-bottom: var(--space-6);
  padding: var(--space-4);
  border: 1px solid var(--border-color-base);
  border-radius: var(--border-radius-content);
  background: var(--bg-color-base);

  h2 {
    @include heading-secondary;
    color: var(--primary-color);
  }

  > div, > p, > code {
    margin-bottom: var(--space-2);
    padding: var(--space-1) 0;
  }

  code {
    @include code-text;
    display: block;
    padding: var(--space-2);
    background: var(--bg-color-light);
    border-radius: 4px;
  }
}

.test-info {
  background: var(--bg-color-light);
  padding: var(--space-4);
  border-radius: var(--border-radius-content);
  margin-top: var(--space-6);

  h3 {
    @include heading-tertiary;
    color: var(--text-color-primary);
  }

  p {
    @include body-text;
    margin: var(--space-1) 0;
  }
}
</style>