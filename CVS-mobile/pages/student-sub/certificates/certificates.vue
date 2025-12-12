<template>
  <view class="certificates-page">
    <!-- 申请按钮 -->
    <view class="apply-btn-wrapper">
      <button class="apply-btn" @click="showApplyDialog = true">申请证明</button>
    </view>

    <!-- 证明列表 -->
    <scroll-view
      class="scroll-view"
      scroll-y
      enable-back-to-top
      scroll-with-animation
      :lower-threshold="100"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
    >
      <view class="list-container">
        <view v-for="cert in list" :key="cert.id" class="cert-item">
          <view class="cert-header">
            <view class="cert-title">志愿服务证明</view>
            <view class="cert-status" :class="getStatusClass(cert.status)">
              {{ getStatusText(cert.status) }}
            </view>
          </view>
          
          <view class="cert-body">
            <view class="cert-info">
              <text class="label">申请时间：</text>
              <text class="value">{{ formatTime(cert.createTime) }}</text>
            </view>
            <view class="cert-info">
              <text class="label">证明用途：</text>
              <text class="value">{{ cert.purpose }}</text>
            </view>
            <view class="cert-info">
              <text class="label">时间范围：</text>
              <text class="value">{{ formatDate(cert.startDate) }} 至 {{ formatDate(cert.endDate) }}</text>
            </view>
            <view v-if="cert.rejectReason" class="cert-info reject-reason">
              <text class="label">拒绝原因：</text>
              <text class="value">{{ cert.rejectReason }}</text>
            </view>
          </view>

          <view v-if="cert.status === 'APPROVED'" class="cert-footer">
            <button class="preview-btn" @click="handlePreview(cert)">预览证明</button>
            <button class="download-btn" @click="handleDownload(cert)">下载证明</button>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">加载中...</view>
        <view v-else-if="finished && list.length > 0" class="finished">没有更多了</view>
        <view v-else-if="isEmpty" class="empty">
          <text class="empty-icon">📄</text>
          <text class="empty-text">暂无证明记录</text>
        </view>
      </view>
    </scroll-view>

    <!-- 申请弹窗 -->
    <view v-if="showApplyDialog" class="dialog-mask" @click="showApplyDialog = false">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <view class="dialog-title">申请证明</view>
          <view class="dialog-close" @click="showApplyDialog = false">✕</view>
        </view>

        <view class="dialog-body">
          <view class="form-item">
            <view class="form-label">证明用途</view>
            <input
              v-model="applyForm.purpose"
              class="form-input"
              placeholder="请输入证明用途"
            />
          </view>

          <view class="form-item">
            <view class="form-label">时间范围</view>
            <view class="date-range">
              <picker
                mode="date"
                :value="applyForm.startDate"
                @change="handleStartDateChange"
              >
                <view class="date-picker">
                  {{ applyForm.startDate || '开始日期' }}
                </view>
              </picker>
              <text class="date-separator">至</text>
              <picker
                mode="date"
                :value="applyForm.endDate"
                @change="handleEndDateChange"
              >
                <view class="date-picker">
                  {{ applyForm.endDate || '结束日期' }}
                </view>
              </picker>
            </view>
          </view>
        </view>

        <view class="dialog-footer">
          <button class="btn-cancel" @click="showApplyDialog = false">取消</button>
          <button class="btn-confirm" @click="handleApply">提交申请</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { getMyCertificates, createCertificate, previewCertificate, downloadCertificate } from '@/api/certificate'
import { formatDateTime } from '@/utils/format'

const showApplyDialog = ref(false)
const applyForm = ref({
  purpose: '',
  startDate: '',
  endDate: ''
})

const {
  list,
  loading,
  refreshing,
  finished,
  isEmpty,
  refresh,
  loadMore,
  prependItem
} = usePagination(getMyCertificates, {
  params: {}
})

const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected'
  }
  return classMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || '未知'
}

const formatTime = (time) => {
  return formatDateTime(time, 'YYYY-MM-DD HH:mm')
}

const formatDate = (date) => {
  return formatDateTime(date, 'YYYY-MM-DD')
}

const handleStartDateChange = (e) => {
  applyForm.value.startDate = e.detail.value
}

const handleEndDateChange = (e) => {
  applyForm.value.endDate = e.detail.value
}

const handleApply = async () => {
  if (!applyForm.value.purpose) {
    uni.showToast({
      title: '请输入证明用途',
      icon: 'none'
    })
    return
  }

  if (!applyForm.value.startDate || !applyForm.value.endDate) {
    uni.showToast({
      title: '请选择时间范围',
      icon: 'none'
    })
    return
  }

  try {
    const res = await createCertificate(applyForm.value)
    uni.showToast({
      title: '申请成功',
      icon: 'success'
    })
    showApplyDialog.value = false
    prependItem(res)
    
    // 重置表单
    applyForm.value = {
      purpose: '',
      startDate: '',
      endDate: ''
    }
  } catch (error) {
    uni.showToast({
      title: error.message || '申请失败',
      icon: 'none'
    })
  }
}

const handlePreview = (cert) => {
  try {
    // 获取token
    const token = uni.getStorageSync('token')
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
    const previewUrl = `${baseUrl}/certificates/${cert.id}/preview`
    
    console.log('预览证明URL:', previewUrl)
    console.log('Token存在:', !!token)
    
    // #ifdef H5
    // H5环境：直接在新窗口打开预览
    const urlWithToken = `${previewUrl}?token=${token}`
    window.open(urlWithToken, '_blank')
    // #endif
    
    // #ifndef H5
    // 非H5环境：下载后打开预览
    uni.showLoading({
      title: '加载中...'
    })
    
    uni.downloadFile({
      url: previewUrl,
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/pdf'
      },
      success: (downloadRes) => {
        console.log('预览响应状态码:', downloadRes.statusCode)
        
        uni.hideLoading()
        
        if (downloadRes.statusCode === 200) {
          const filePath = downloadRes.tempFilePath
          console.log('文件临时路径:', filePath)
          
          // 打开PDF预览
          uni.openDocument({
            filePath: filePath,
            fileType: 'pdf',
            showMenu: true,
            success: () => {
              console.log('打开PDF预览成功')
            },
            fail: (err) => {
              console.error('打开PDF预览失败:', err)
              uni.showToast({
                title: '预览失败',
                icon: 'none'
              })
            }
          })
        } else {
          console.error('预览失败，状态码:', downloadRes.statusCode)
          uni.showToast({
            title: `预览失败(${downloadRes.statusCode})`,
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        uni.hideLoading()
        console.error('预览失败详情:', err)
        uni.showToast({
          title: err.errMsg || '预览失败，请重试',
          icon: 'none',
          duration: 3000
        })
      }
    })
    // #endif
  } catch (error) {
    console.error('预览证明失败:', error)
    uni.showToast({
      title: '预览失败',
      icon: 'none'
    })
  }
}

const handleDownload = (cert) => {
  try {
    // 获取token
    const token = uni.getStorageSync('token')
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://192.168.155.104:8000/api'
    const downloadUrl = `${baseUrl}/certificates/${cert.id}/download`
    
    console.log('下载证明URL:', downloadUrl)
    console.log('Token存在:', !!token)
    
    // #ifdef H5
    // H5环境：直接打开链接下载（带token）
    const urlWithToken = `${downloadUrl}?token=${token}`
    window.open(urlWithToken, '_blank')
    uni.showToast({
      title: '开始下载',
      icon: 'success'
    })
    // #endif
    
    // #ifndef H5
    // 非H5环境：使用uni.downloadFile，通过header传递token
    uni.showLoading({
      title: '下载中...'
    })
    
    uni.downloadFile({
      url: downloadUrl,
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/pdf'
      },
      success: (downloadRes) => {
        console.log('下载响应状态码:', downloadRes.statusCode)
        console.log('下载响应:', downloadRes)
        
        uni.hideLoading()
        
        if (downloadRes.statusCode === 200) {
          const filePath = downloadRes.tempFilePath
          console.log('文件临时路径:', filePath)
          
          // PDF文件，尝试打开
          uni.openDocument({
            filePath: filePath,
            fileType: 'pdf',
            showMenu: true,
            success: () => {
              console.log('打开PDF成功')
              uni.showToast({
                title: '下载成功',
                icon: 'success'
              })
            },
            fail: (err) => {
              console.error('打开PDF失败:', err)
              // 尝试保存文件
              uni.saveFile({
                tempFilePath: filePath,
                success: (saveRes) => {
                  uni.showToast({
                    title: '文件已保存',
                    icon: 'success'
                  })
                  console.log('文件保存路径:', saveRes.savedFilePath)
                },
                fail: (saveErr) => {
                  console.error('保存文件失败:', saveErr)
                  uni.showToast({
                    title: '文件已下载到临时目录',
                    icon: 'none'
                  })
                }
              })
            }
          })
        } else {
          console.error('下载失败，状态码:', downloadRes.statusCode)
          uni.showToast({
            title: `下载失败(${downloadRes.statusCode})`,
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        uni.hideLoading()
        console.error('下载失败详情:', err)
        uni.showToast({
          title: err.errMsg || '下载失败，请重试',
          icon: 'none',
          duration: 3000
        })
      }
    })
    // #endif
  } catch (error) {
    console.error('下载证明失败:', error)
    uni.showToast({
      title: '下载失败',
      icon: 'none'
    })
  }
}
</script>

<style scoped>
.certificates-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  overflow: hidden;
}

.apply-btn-wrapper {
  padding: 24rpx;
  background: #fff;
}

.apply-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #0052d9;
  color: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}

.scroll-view {
  flex: 1;
  height: 0;
  -webkit-overflow-scrolling: touch;
}

.list-container {
  padding: 24rpx;
}

.cert-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  transform: translateZ(0);
  will-change: transform;
}

.cert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.cert-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.cert-status {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.cert-body {
  margin-bottom: 20rpx;
}

.cert-info {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 28rpx;
}

.cert-info .label {
  color: #999;
  margin-right: 8rpx;
}

.cert-info .value {
  flex: 1;
  color: #333;
}

.cert-info.reject-reason .value {
  color: #ff4d4f;
}

.cert-footer {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  gap: 16rpx;
}

.preview-btn,
.download-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}

.preview-btn {
  background: #fff;
  color: #0052d9;
  border: 1rpx solid #0052d9;
}

.download-btn {
  background: #0052d9;
  color: #fff;
}

.loading,
.finished,
.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.empty-icon {
  font-size: 96rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-content {
  width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.dialog-close {
  font-size: 40rpx;
  color: #999;
}

.dialog-body {
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}

.form-input {
  width: 100%;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.date-picker {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.date-separator {
  font-size: 28rpx;
  color: #999;
}

.dialog-footer {
  display: flex;
  gap: 16rpx;
  padding: 32rpx;
  border-top: 1rpx solid #f0f0f0;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #333;
}

.btn-confirm {
  background: #0052d9;
  color: #fff;
}
</style>
