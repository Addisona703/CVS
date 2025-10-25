import request from '@/utils/request'

export const reviewAPI = {
  // 查询待审核的签退列表
  fetchReviewList(params) {
    return request.post('/review/search', params)
  },
  // 教师调整评分与评价
  updateReview(signupId, payload) {
    return request.patch(`/review/${signupId}`, payload)
  }
}

