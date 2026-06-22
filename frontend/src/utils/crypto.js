/**
 * SHA-256 哈希工具
 * 使用浏览器原生 SubtleCrypto API，对密码脱敏后再传输。
 */
export async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message)
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}
