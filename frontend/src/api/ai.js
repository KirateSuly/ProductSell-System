/**
 * AI 智能问答 API 模块
 *
 * 封装与后端 /api/ai 的通信，用于内嵌 AI 对话窗口。
 */
import request from './request'

/**
 * 发送对话消息
 * @param {Object} data - { message: string, history: [{role, content}] }
 * @returns {Promise} 后端响应 { code, message, data: { reply, refused } }
 */
export function sendChatMessage(data) {
  return request.post('/ai/chat', data)
}
