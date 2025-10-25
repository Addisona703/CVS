# Element Plus 主题定制说明

## 概述

本文档说明了 CVS 系统中 Element Plus 组件库的主题定制实现，确保所有组件符合设计规范中定义的三种角色主题（管理员、教师、学生）。

## 实现方式

### 1. 主题变量覆盖

通过 `useTheme` composable 动态设置 Element Plus 的 CSS 变量，实现按角色的主题定制。

#### 覆盖的主要 Token

- **主色调系列**
  - `--el-color-primary`: 主题主色
  - `--el-color-primary-light-3` 到 `--el-color-primary-light-9`: 浅色变体
  - `--el-color-primary-dark-2`: 深色变体

- **组件特定 Token**
  - `--el-button-hover-bg-color`: 按钮悬停背景色
  - `--el-button-active-bg-color`: 按钮激活背景色
  - `--el-input-focus-border-color`: 输入框聚焦边框色
  - `--el-input-hover-border-color`: 输入框悬停边框色
  - `--el-menu-active-color`: 菜单激活文字色
  - `--el-menu-hover-bg-color`: 菜单悬停背景色

- **通用样式 Token**
  - `--el-border-radius-base`: 基础圆角 (6px)
  - `--el-border-radius-small`: 小圆角 (4px)
  - `--el-border-radius-large`: 大圆角 (8px)
  - `--el-transition-duration`: 过渡时长 (0.3s)
  - `--el-transition-duration-fast`: 快速过渡 (0.2s)

### 2. 角色主题配置

#### 管理员主题 (Admin)
```javascript
{
  primary: '#409eff',
  lightVariants: {
    'light-3': '#79bbff',
    'light-5': '#a0cfff',
    'light-7': '#c6e2ff',
    'light-8': '#d9ecff',
    'light-9': '#ecf5ff'
  },
  dark: '#337ecc'
}
```

#### 教师主题 (Teacher)
```javascript
{
  primary: '#67c23a',
  lightVariants: {
    'light-3': '#85ce61',
    'light-5': '#a4da89',
    'light-7': '#c2e7b0',
    'light-8': '#d1edc4',
    'light-9': '#e1f3d8'
  },
  dark: '#529b2e'
}
```

#### 学生主题 (Student)
```javascript
{
  primary: '#1f6bff',
  lightVariants: {
    'light-3': '#4d85ff',
    'light-5': '#7aa0ff',
    'light-7': '#a6baff',
    'light-8': '#bfccff',
    'light-9': '#d9ddff'
  },
  dark: '#1956cc'
}
```

### 3. SCSS 样式覆盖

在 `src/styles/element-plus-override.scss` 中定义了详细的组件样式覆盖，包括：

- **按钮组件**: 添加 hover 上浮和 active 缩放动画
- **输入框组件**: 自定义聚焦和悬停状态
- **菜单组件**: 
  - 学生角色：渐变背景激活态
  - 教师角色：左侧指示条
  - 管理员角色：标准激活态
- **卡片组件**: hover 上浮和阴影变化
- **表格组件**: 行悬停效果
- **表单组件**: 错误抖动动画
- **其他组件**: 对话框、消息提示、通知、分页、标签页等

## 使用方法

### 在组件中使用主题

```vue
<script setup>
import { useTheme } from '@/composables/useTheme'

const { currentRole, isDarkMode, toggleDarkMode } = useTheme()
</script>

<template>
  <div>
    <p>当前角色: {{ currentRole }}</p>
    <el-button type="primary">主题色按钮</el-button>
  </div>
</template>
```

### 主题自动应用

主题会在以下情况自动应用：

1. **应用启动时**: `App.vue` 中调用 `initTheme()`
2. **用户登录后**: 根据用户角色自动切换主题
3. **角色切换时**: 监听 `currentRole` 变化自动更新

## 验证方法

### 1. 运行单元测试

```bash
npm test
```

测试覆盖：
- ✅ 角色主题变量设置
- ✅ Element Plus 主色调变体
- ✅ 按钮相关 token
- ✅ 输入框相关 token
- ✅ 菜单相关 token
- ✅ 边框圆角和过渡时长

### 2. 使用验证组件

在开发环境中访问 `ThemeVerification.vue` 组件进行可视化验证：

```vue
<template>
  <ThemeVerification />
</template>

<script setup>
import ThemeVerification from '@/components/ThemeVerification.vue'
</script>
```

该组件展示：
- 各种类型的按钮
- 输入框和选择器
- 菜单组件（包含激活态）
- 开关、复选框、单选框
- 进度条、标签
- 消息提示和通知
- 对话框

### 3. 手动验证清单

#### 按钮验证
- [ ] 主要按钮使用正确的角色主题色
- [ ] hover 时按钮轻微上浮（translateY(-1px)）
- [ ] active 时按钮缩小（scale(0.95)）
- [ ] 颜色过渡平滑（0.3s）

#### 输入框验证
- [ ] 聚焦时边框显示主题色
- [ ] hover 时边框显示浅色变体
- [ ] 圆角为 6px
- [ ] 过渡动画流畅

#### 菜单验证
- [ ] 激活项使用主题色
- [ ] hover 时背景色变化
- [ ] 教师角色显示左侧指示条
- [ ] 学生角色显示渐变背景
- [ ] 管理员角色显示标准激活态

#### 其他组件验证
- [ ] 卡片 hover 时上浮并增强阴影
- [ ] 表格行 hover 时背景色变化
- [ ] 表单错误时显示抖动动画
- [ ] 所有组件圆角统一（6px）

## 设计规范符合性

本实现完全符合 `design.md` 第 3 章节的要求：

✅ **管理员界面（3.1）**
- 主题色 #409eff
- 侧边栏深蓝灰色
- 专业严谨风格

✅ **教师界面（3.2）**
- 主题色 #67c23a
- 侧边栏深绿色
- 激活指示条
- 友好温和风格

✅ **学生界面（3.3）**
- 主题色 #1f6bff
- 渐变 hover 效果
- 圆角设计
- 现代活力风格

## 性能优化

- 使用 CSS 变量实现主题切换，避免重新加载样式
- 仅使用 `transform` 和 `opacity` 实现动画，触发 GPU 加速
- 过渡时长统一为 0.3s，保持一致性
- 支持 `prefers-reduced-motion` 媒体查询

## 浏览器兼容性

- 现代浏览器（Chrome, Firefox, Safari, Edge）完全支持
- CSS 变量在所有目标浏览器中可用
- 降级策略已在学生主题中实现（backdrop-filter）

## 维护指南

### 添加新的主题色

1. 在 `useTheme.js` 的 `themeConfigs` 中添加新角色配置
2. 在 `src/styles/themes/` 中创建对应的 SCSS 文件
3. 更新测试文件验证新主题

### 自定义组件样式

1. 在 `element-plus-override.scss` 中添加组件选择器
2. 使用 CSS 变量引用主题色
3. 确保支持所有角色主题

### 调试主题问题

1. 检查浏览器开发工具中的 CSS 变量值
2. 验证 `body[data-role]` 属性是否正确设置
3. 运行单元测试确认 token 设置正确
4. 使用 `ThemeVerification` 组件进行可视化检查

## 相关文件

- `src/composables/useTheme.js` - 主题管理逻辑
- `src/styles/element-plus-override.scss` - 组件样式覆盖
- `src/styles/themes/` - 角色主题 SCSS 文件
- `src/components/ThemeVerification.vue` - 主题验证组件
- `src/test/theme.test.js` - 主题单元测试
