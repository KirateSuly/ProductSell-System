import request from './request'

export function getCustomerList(params) {
  return request.get('/customers', { params })
}

export function getAllCustomers() {
  return request.get('/customers/all')
}

export function getCustomerById(id) {
  return request.get(`/customers/${id}`)
}

export function createCustomer(data) {
  return request.post('/customers', data)
}

export function updateCustomer(id, data) {
  return request.put(`/customers/${id}`, data)
}

export function deleteCustomer(id) {
  return request.delete(`/customers/${id}`)
}
