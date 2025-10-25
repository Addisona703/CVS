/**
 * 语言检测和字体优化工具
 * 确保在不同语言环境下的最佳字体显示
 */

/**
 * 检测用户的首选语言
 * @returns {string} 语言代码 (zh-CN, en, etc.)
 */
export function detectUserLanguage() {
  // 优先使用用户设置的语言
  const savedLanguage = localStorage.getItem('cvs-language')
  if (savedLanguage) {
    return savedLanguage
  }

  // 检测浏览器语言
  const browserLanguage = navigator.language || navigator.userLanguage
  
  // 标准化语言代码
  if (browserLanguage.startsWith('zh')) {
    return 'zh-CN'
  } else if (browserLanguage.startsWith('en')) {
    return 'en'
  }
  
  // 默认返回中文
  return 'zh-CN'
}

/**
 * 设置文档语言属性
 * @param {string} language - 语言代码
 */
export function setDocumentLanguage(language = null) {
  const lang = language || detectUserLanguage()
  
  // 设置 HTML 元素的 lang 属性
  document.documentElement.lang = lang
  
  // 保存用户语言偏好
  localStorage.setItem('cvs-language', lang)
  
  // 触发字体重新渲染（如果需要）
  document.body.style.fontFamily = 'var(--font-family-base)'
  
  return lang
}

/**
 * 检测文本是否包含中文字符
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含中文
 */
export function containsChinese(text) {
  return /[\u4e00-\u9fff]/.test(text)
}

/**
 * 根据文本内容动态调整字体样式
 * @param {HTMLElement} element - 目标元素
 * @param {string} text - 文本内容
 */
export function optimizeFontForText(element, text) {
  if (!element || !text) return
  
  const hasChinese = containsChinese(text)
  
  if (hasChinese) {
    // 中文文本优化
    element.style.letterSpacing = 'var(--letter-spacing-normal)'
    element.style.lineHeight = 'var(--line-height-relaxed)'
  } else {
    // 英文文本优化
    element.style.letterSpacing = 'var(--letter-spacing-wide)'
    element.style.lineHeight = 'var(--line-height-normal)'
  }
}

/**
 * 字体加载状态检测
 * @returns {Promise<boolean>} 字体是否加载完成
 */
export async function checkFontLoading() {
  if (!document.fonts) {
    // 不支持 Font Loading API 的浏览器
    return true
  }
  
  try {
    // 检查 Inter 字体是否加载
    const interFont = new FontFace('Inter', 'url(https://fonts.gstatic.com/s/inter/v12/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiA.woff2)')
    await interFont.load()
    document.fonts.add(interFont)
    
    // 等待所有字体加载完成
    await document.fonts.ready
    return true
  } catch (error) {
    console.warn('字体加载失败，使用系统默认字体:', error)
    return false
  }
}

/**
 * 初始化字体系统
 * 在应用启动时调用
 */
export async function initFontSystem() {
  // 设置文档语言
  const language = setDocumentLanguage()
  
  // 检查字体加载状态
  const fontLoaded = await checkFontLoading()
  
  // 添加字体加载状态类
  if (fontLoaded) {
    document.body.classList.add('fonts-loaded')
  } else {
    document.body.classList.add('fonts-fallback')
  }
  
  // 返回初始化结果
  return {
    language,
    fontLoaded
  }
}

/**
 * 响应式字体大小调整
 * 根据屏幕尺寸和设备类型调整字体
 */
export function adjustFontForDevice() {
  const isMobile = window.innerWidth < 768
  const isTablet = window.innerWidth >= 768 && window.innerWidth < 1024
  
  if (isMobile) {
    document.body.classList.add('device-mobile')
    document.body.classList.remove('device-tablet', 'device-desktop')
  } else if (isTablet) {
    document.body.classList.add('device-tablet')
    document.body.classList.remove('device-mobile', 'device-desktop')
  } else {
    document.body.classList.add('device-desktop')
    document.body.classList.remove('device-mobile', 'device-tablet')
  }
}

// 监听窗口大小变化
if (typeof window !== 'undefined') {
  window.addEventListener('resize', adjustFontForDevice)
  // 初始调用
  adjustFontForDevice()
}