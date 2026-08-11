import request from '@/utils/request'

/** 验证提取码 → 获取分享会话 token（30min） */
export function verifyCodeApi(shareId, shareCode) {
  return request.post('/share/verify', null, {
    params: { shareId, shareCode },
  })
}

/**
 * 获取分享信息 + 浏览子文件夹
 * @param {string} shareId   分享ID
 * @param {string} shareCode 提取码
 * @param {string} shareToken 分享会话 token（验证过一次后可用，避免反复传码）
 * @param {string} parentId  子文件夹ID
 */
export function showShareApi(shareId, shareCode, shareToken, parentId) {
  const params = { shareId }
  if (shareCode) params.shareCode = shareCode
  if (shareToken) params.shareToken = shareToken
  if (parentId) params.parentId = parentId
  return request.post('/share/showshare', null, { params })
}

/** 保存分享文件到自己的网盘 */
export function saveShareApi(fileId, parentId, shareId) {
  return request.post('/share/saveshare', null, {
    params: { fileId, parentId, shareId },
  })
}

/** 创建分享 */
export function createShareApi(fileId, shareType, expire_day, shareCode) {
  return request.post('/share/create', null, {
    params: { fileId, shareType, expire_day, shareCode },
  })
}

/** 我的分享列表 */
export function listShareApi() {
  return request.post('/share/listshare')
}

/** 取消分享 */
export function cancelShareApi(shareId) {
  return request.post('/share/cancel', null, { params: { shareId } })
}
