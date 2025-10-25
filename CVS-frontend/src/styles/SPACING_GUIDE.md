# 间距系统使用指南

## 概述

CVS 系统采用基于 8px 基准的间距系统，确保界面元素之间的间距保持一致性和视觉和谐。

## 间距变量

### 基础间距变量

```scss
--space-0: 0;
--space-1: 4px;   // 0.5 * 8px
--space-2: 8px;   // 1 * 8px
--space-3: 12px;  // 1.5 * 8px
--space-4: 16px;  // 2 * 8px
--space-5: 20px;  // 2.5 * 8px
--space-6: 24px;  // 3 * 8px
--space-8: 32px;  // 4 * 8px
--space-10: 40px; // 5 * 8px
--space-12: 48px; // 6 * 8px
--space-15: 60px; // 7.5 * 8px
--space-16: 64px; // 8 * 8px
--space-20: 80px; // 10 * 8px
--space-24: 96px; // 12 * 8px
```

### 响应式间距变量

```scss
--padding-mobile: 16px;   // 移动端内边距
--padding-tablet: 24px;   // 平板端内边距
--padding-desktop: 36px;  // 桌面端内边距
```

### 语义化间距变量

```scss
--gap-xs: var(--space-1);    // 4px
--gap-sm: var(--space-2);    // 8px
--gap-md: var(--space-4);    // 16px
--gap-lg: var(--space-6);    // 24px
--gap-xl: var(--space-8);    // 32px

--layout-gap: var(--space-6);    // 24px - 布局间距
--section-gap: var(--space-8);   // 32px - 章节间距
--page-gap: var(--space-12);     // 48px - 页面间距
```

## 使用方法

### 1. 在 SCSS 中使用 CSS 变量

```scss
.my-component {
  padding: var(--space-4);           // 16px
  margin-bottom: var(--space-6);     // 24px
  gap: var(--gap-md);                // 16px
}
```

### 2. 使用 Mixins

```scss
@use '@/styles/utilities/mixins' as *;

.my-component {
  @include padding(4);               // padding: var(--space-4)
  @include margin-bottom(6);         // margin-bottom: var(--space-6)
  @include gap(4);                   // gap: var(--space-4)
  @include padding-responsive;       // 响应式 padding
}
```

### 3. 使用工具类

```html
<!-- 在模板中直接使用 -->
<div class="p-4 mb-6 gap-4">
  <!-- padding: var(--space-4), margin-bottom: var(--space-6), gap: var(--space-4) -->
</div>

<div class="padding-responsive">
  <!-- 响应式 padding -->
</div>

<div class="section-spacing">
  <!-- margin-bottom: var(--section-gap) -->
</div>
```

## 工具类命名规范

### Margin 工具类
- `.m-{size}` - 全方向 margin
- `.mt-{size}` - margin-top
- `.mb-{size}` - margin-bottom
- `.ml-{size}` - margin-left
- `.mr-{size}` - margin-right
- `.mx-{size}` - 水平方向 margin
- `.my-{size}` - 垂直方向 margin

### Padding 工具类
- `.p-{size}` - 全方向 padding
- `.pt-{size}` - padding-top
- `.pb-{size}` - padding-bottom
- `.pl-{size}` - padding-left
- `.pr-{size}` - padding-right
- `.px-{size}` - 水平方向 padding
- `.py-{size}` - 垂直方向 padding

### Gap 工具类
- `.gap-{size}` - flex/grid gap

### 自动间距
- `.m-auto` - margin: auto
- `.mx-auto` - 水平居中
- `.my-auto` - 垂直居中

## 最佳实践

### 1. 优先使用间距变量
```scss
// ✅ 推荐
.component {
  padding: var(--space-4);
  margin-bottom: var(--space-6);
}

// ❌ 避免
.component {
  padding: 16px;
  margin-bottom: 24px;
}
```

### 2. 使用语义化变量
```scss
// ✅ 推荐 - 语义化
.section {
  margin-bottom: var(--section-gap);
}

// ✅ 可以 - 直接使用
.section {
  margin-bottom: var(--space-8);
}
```

### 3. 响应式间距
```scss
// ✅ 推荐 - 使用响应式 mixin
.container {
  @include padding-responsive;
}

// ✅ 可以 - 手动响应式
.container {
  padding: var(--padding-desktop);
  
  @media (max-width: 1024px) {
    padding: var(--padding-tablet);
  }
  
  @media (max-width: 768px) {
    padding: var(--padding-mobile);
  }
}
```

### 4. 组件间距
```scss
// ✅ 推荐 - 使用 gap
.card-grid {
  display: grid;
  gap: var(--space-5);
}

// ✅ 推荐 - 使用工具类
```

```html
<div class="d-flex gap-4">
  <div>Item 1</div>
  <div>Item 2</div>
</div>
```

## 常用间距场景

### 卡片内边距
- 小卡片: `var(--space-4)` (16px)
- 中等卡片: `var(--space-6)` (24px)
- 大卡片: `var(--space-8)` (32px)

### 元素间距
- 紧密元素: `var(--space-2)` (8px)
- 普通元素: `var(--space-4)` (16px)
- 松散元素: `var(--space-6)` (24px)

### 章节间距
- 小章节: `var(--space-6)` (24px)
- 中等章节: `var(--space-8)` (32px)
- 大章节: `var(--space-12)` (48px)

### 页面边距
- 移动端: `var(--padding-mobile)` (16px)
- 平板端: `var(--padding-tablet)` (24px)
- 桌面端: `var(--padding-desktop)` (36px)

## 迁移指南

### 替换硬编码间距值

1. 找到硬编码的像素值
2. 根据 8px 基准选择最接近的间距变量
3. 替换为对应的 CSS 变量或工具类

```scss
// 迁移前
.old-component {
  padding: 16px;
  margin: 24px 0;
  gap: 12px;
}

// 迁移后
.new-component {
  padding: var(--space-4);
  margin: var(--space-6) 0;
  gap: var(--space-3);
}
```

### 常见替换映射

| 硬编码值 | 间距变量 | 说明 |
|---------|---------|------|
| 4px | `var(--space-1)` | 最小间距 |
| 8px | `var(--space-2)` | 小间距 |
| 12px | `var(--space-3)` | 紧密间距 |
| 16px | `var(--space-4)` | 标准间距 |
| 20px | `var(--space-5)` | 中等间距 |
| 24px | `var(--space-6)` | 大间距 |
| 32px | `var(--space-8)` | 章节间距 |
| 40px | `var(--space-10)` | 大章节间距 |
| 48px | `var(--space-12)` | 页面间距 |

这样可以确保整个应用的间距保持一致性，并且便于后续维护和主题切换。