import request from './request'

export function getProductList(params) {
  return request.get('/products', { params })
}

export function getProductById(id) {
  return request.get(`/products/${id}`)
}

export function createProduct(data) {
  return request.post('/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/products/upload-image', formData)
}
