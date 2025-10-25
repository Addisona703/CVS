# CVS 样式系统文档

基于 `design.md` 规范构建的完整设计系统，支持三种角色主题（管理员、教师、学生）。

## 📁 目录结构

```
src/styles/
├── tokens/              # 设计 tokens（设计变量）
│   ├── _breakpoints.scss   # 响应式断点
│   ├── _colors.scss        # 颜色系统
│   ├── _spacing.scss       # 间距系统（8px 基准）
│   ├── _typography.scss    # 字体系统
│   ├── _shadows.scss       # 阴影系统
│   ├── _borders.scss       # 边框和圆角
│   ├── _transitions.scss   # 过渡和动画时长
│   └── _index.scss         # Tokens 入口
│
├── themes/              # 角色主题
│   ├── admin.scss          # 管理员主题（专业、严谨）
│   ├── teacher.scss        # 教师主题（友好、温和）
│   ├── student.scss        # 学生主题（现代、活力）
│   └── index.scss          # 主题入口
│
├── utilities/           # 工具类和 Mixins
│   ├── _mixins.scss        # 通用 mixins
│   ├── _animations.scss    # 动画定义
│   ├── _helpers.scss       # 工具类
│   └── _index.scss         # Utilities 入口
│
├── element-plus-override.scss  # Element Plus 主题覆盖
└── index.scss           # 样式系统主入口
```

## 🎨 使用方法

### 1. 在 Vue 组件中使用 CSS 变量

```vue
<template>
  <div class="my-card">
    <h2>标题</h2>
    <p>内容</p>
  </div>
</template>

<style scoped>
.my-card {
  background: var(--bg-color-base);
  padding: var(--space-6);
  border-radius: var(--border-radius-card);
  box-shadow: var(--card-shadow);
  color: var(--text-color-primary);
}

.my-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-4px);
  transition: var(--transition-card);
}
</style>
```

### 2. 使用 Mixins

```vue
<style lang="scss" scoped>
@use '@/styles/utilities/mixins' as *;

.my-card {
  @include card-base;
  @include card-hover;
}

.my-button {
  @include button-base;
  @include button-primary;
  @include button-press;
}

// 响应式布局
.container {
  @include padding-responsive;
  
  @include mobile {
    flex-direction: column;
  }
  
  @include desktop {
    flex-direction: row;
  }
}
</style>
```

### 3. 使用工具类

```vue
<template>
  <!-- 间距工具类 -->
  <div class="p-4 mb-6">
    <!-- Flex 布局 -->
    <div class="d-flex justify-between align-center">
      <!-- 文本样式 -->
      <h3 class="text-lg font-semibold">标题</h3>
      <button class="cursor-pointer">按钮</button>
    </div>
  </div>
  
  <!-- 响应式显示 -->
  <div class="hide-mobile show-desktop">
    桌面端显示
  </div>
</template>
```

## 📐 设计 Tokens

### 间距系统（8px 基准）

```scss
--space-0: 0
--space-1: 4px    // 0.5 * 8px
--space-2: 8px    // 1 * 8px
--space-3: 12px   // 1.5 * 8px
--space-4: 16px   // 2 * 8px
--space-5: 20px   // 2.5 * 8px
--space-6: 24px   // 3 * 8px
--space-8: 32px   // 4 * 8px
--space-10: 40px  // 5 * 8px
--space-12: 48px  // 6 * 8px
```

### 字体系统

```scss
// 字体大小
--font-size-xs: 12px    // 辅助文字
--font-size-sm: 14px    // 正文小
--font-size-base: 16px  // 正文
--font-size-lg: 18px    // 标题小
--font-size-xl: 20px    // 标题中
--font-size-2xl: 24px   // 标题大

// 字重
--font-weight-normal: 400
--font-weight-medium: 500
--font-weight-semibold: 600
--font-weight-bold: 700
```

### 阴影系统

```scss
--shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05)
--shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.08)
--shadow-base: 0 4px 12px rgba(0, 0, 0, 0.08)
--shadow-md: 0 6px 16px rgba(0, 0, 0, 0.12)
--shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.15)
--card-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)
--card-shadow-hover: 0 8px 24px rgba(0, 0, 0, 0.15)
```

### 响应式断点

```scss
Mobile:  < 768px
Tablet:  768px - 1023px
Desktop: ≥ 1024px
```

## 🎭 角色主题

### 管理员主题（专业、严谨）

- **主题色**: `#409eff` (蓝色)
- **侧边栏**: `#304156` (深蓝灰)
- **特点**: 深色调、专业感、数据密集型界面

### 教师主题（友好、温和）

- **主题色**: `#67c23a` (绿色)
- **侧边栏**: `#2d5a27` (深绿)
- **特点**: 柔和色调、友好感、工作台风格

### 学生主题（现代、活力）

- **主题色**: `#1f6bff` (鲜艳蓝)
- **侧边栏**: `rgba(255, 255, 255, 0.92)` (半透明白)
- **特点**: 毛玻璃效果、圆角设计、渐变动效

## 🔧 常用 Mixins

### 响应式布局

```scss
@include mobile { /* 移动端样式 */ }
@include tablet { /* 平板样式 */ }
@include desktop { /* 桌面端样式 */ }
@include padding-responsive; // 自动响应式内边距
```

### 动画效果

```scss
@include smooth-transition(all, 0.3s, ease);
@include hover-lift(-4px);  // 悬停上浮
@include button-press;       // 按钮按压效果
@include glass-effect;       // 毛玻璃效果（学生主题）
```

### 卡片和按钮

```scss
@include card-base;
@include card-hover;
@include button-base;
@include button-primary;
```

### 文本处理

```scss
@include text-ellipsis;      // 单行截断
@include text-clamp(2);      // 多行截断
```

### 可访问性

```scss
@include focus-visible;      // 焦点样式
@include touch-target;       // 移动端触摸目标
@include sr-only;            // 屏幕阅读器专用
```

## 🎬 动画类

```html
<!-- 淡入淡出 -->
<div class="fade-in">内容</div>

<!-- 滑入动画 -->
<div class="slide-in-up">内容</div>
<div class="slide-in-down">内容</div>

<!-- 缩放动画 -->
<div class="scale-in">内容</div>

<!-- 加载动画 -->
<div class="spin">加载中</div>
<div class="pulse">脉冲</div>
```

## 🛠️ 工具类

### 间距

```html
<div class="m-4">margin: 16px</div>
<div class="p-6">padding: 24px</div>
<div class="mt-2">margin-top: 8px</div>
<div class="px-4">padding-left & right: 16px</div>
```

### 布局

```html
<div class="d-flex justify-between align-center">
  Flex 布局
</div>
```

### 文本

```html
<p class="text-lg font-semibold text-center">
  大号、半粗体、居中文本
</p>
```

### 响应式显示

```html
<div class="hide-mobile">移动端隐藏</div>
<div class="show-desktop">仅桌面端显示</div>
```

## ♿ 可访问性

### 对比度要求

- 主文本: ≥ 7:1
- 辅助文本: ≥ 4.5:1

### 键盘导航

所有交互元素支持键盘导航，使用 `@include focus-visible` 提供清晰的焦点指示。

### 动效降级

系统自动检测 `prefers-reduced-motion`，为需要低动效的用户禁用复杂动画。

## 🚀 性能优化

### GPU 加速

仅使用 `transform` 和 `opacity` 实现动画，避免触发重排：

```scss
// ✅ 推荐
transform: translateY(-4px);
opacity: 0.8;

// ❌ 避免
top: -4px;
height: 200px;
```

### 动效降级

```scss
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01s !important;
    transition-duration: 0.01s !important;
  }
}
```

## 📝 最佳实践

1. **优先使用 CSS 变量**，而不是硬编码值
2. **使用间距系统**（8px 基准），避免随意间距
3. **使用 mixins** 封装重复样式逻辑
4. **使用工具类** 处理简单的样式需求
5. **遵循响应式设计**，使用提供的断点 mixins
6. **确保可访问性**，使用焦点样式和语义化标签
7. **性能优先**，使用 GPU 加速的动画属性

## 🔗 相关文档

- [design.md](../../.kiro/specs/ui-redesign/design.md) - 完整设计规范
- [requirements.md](../../.kiro/specs/ui-redesign/requirements.md) - 需求文档
- [Element Plus 文档](https://element-plus.org/) - 组件库文档
