import request from './request'

export function getOverview(params) {
  return request.get('/statistics/overview', { params })
}

export function getSalesTrend(params) {
  return request.get('/statistics/sales-trend', { params })
}

export function getTopProducts(params) {
  return request.get('/statistics/top-products', { params })
}

export function getCategorySales(params) {
  return request.get('/statistics/category-sales', { params })
}

export function getOrderStatus(params) {
  return request.get('/statistics/order-status', { params })
}
