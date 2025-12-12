/**
 * uView图标到Phosphor图标的映射表
 * 使用方式: import { getPhosphorIcon } from '@/utils/icon-map'
 */

export const iconMap = {
  // 导航和界面
  'home': 'house',
  'home-fill': 'house',
  'search': 'magnifying-glass',
  'bell': 'bell',
  'setting': 'gear',
  'setting-fill': 'gear',
  'account': 'user',
  'account-fill': 'user',
  'arrow-right': 'caret-right',
  'arrow-left': 'caret-left',
  'arrow-up': 'caret-up',
  'arrow-down': 'caret-down',
  'close': 'x',
  
  // 时间和日期
  'clock': 'clock',
  'calendar': 'calendar',
  'time': 'timer',
  
  // 位置和地图
  'map': 'map-pin',
  'map-pin': 'map-pin',
  
  // 文件和文档
  'file-text': 'file-text',
  'list': 'list',
  'folder': 'folder',
  'order': 'clipboard-text',
  
  // 操作
  'checkmark': 'check',
  'checkmark-circle': 'check-circle',
  'plus': 'plus',
  'scan': 'qr-code',
  'photo': 'image',
  
  // 商业和购物
  'gift': 'gift',
  'star': 'star',
  'medal': 'medal',
  
  // 其他
  'inbox': 'tray',
  'chart-line': 'chart-line'
}

/**
 * 获取Phosphor图标名称
 * @param {string} uviewIconName - uView图标名称
 * @returns {string} Phosphor图标名称
 */
export function getPhosphorIcon(uviewIconName) {
  return iconMap[uviewIconName] || uviewIconName
}

/**
 * 获取图标权重（样式）
 * @param {boolean} isActive - 是否激活状态
 * @returns {string} 图标权重
 */
export function getIconWeight(isActive) {
  return isActive ? 'fill' : 'regular'
}
