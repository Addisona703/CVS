import request from '@/utils/request'

export const checkAPI = {
  generateCheckinToken(activityId) {
    return request.post(`/checkin/token/${activityId}`)
  },
  generateCheckoutToken(activityId) {
    return request.post(`/checkout/token/${activityId}`)
  },
  studentCheckin(payload) {
    return request.post('/checkin', payload)
  },
  studentCheckout(payload) {
    return request.post('/checkout', payload)
  },
  getPendingCheckinStudents(activityId) {
    return request.get(`/checkin/${activityId}/pending`)
  },
  getPendingCheckoutStudents(activityId) {
    return request.get(`/checkout/${activityId}/pending`)
  }
}
