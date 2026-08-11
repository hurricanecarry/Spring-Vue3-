import request from '@/utils/request'

/** 文件列表（status: 0=正常, 1=回收站） */
export function listFileApi(parentId = '0', status = '0') {
  return request.get('/file/list', { params: { parentId, status } })
}

/** 上传文件 */
export function uploadFileApi(file, parentId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('parentId', parentId)
  return request.post('/file/upload', formData)
}

/** 上传文件夹（保留内部目录结构） */
export function uploadFolderApi(files, parentId) {
  const formData = new FormData()
  const relativePaths = []
  for (const f of files) {
    formData.append('files', f)
    relativePaths.push(f.webkitRelativePath || f.name)
  }
  formData.append('relativePaths', JSON.stringify(relativePaths))
  formData.append('parentId', parentId)
  return request.post('/file/uploadFolder', formData)
}

/** 新建文件夹 */
export function newFolderApi(parentId, fileName) {
  return request.put('/file/NewFolder', null, { params: { parentId, fileName } })
}

/** 重命名 */
export function renameApi(fileId, newName) {
  return request.put('/file/Rename', null, { params: { newName, fileId } })
}

/** 回收 */
export function recycleApi(fileId) {
  return request.put('/file/Recyle', null, { params: { fileId } })
}

/** 恢复 */
export function recoverApi(fileId) {
  return request.put('/file/Recover', null, { params: { fileId } })
}

/** 彻底删除 */
export function deleteApi(fileId) {
  return request.put('/file/Delete', null, { params: { fileId } })
}

/** 复制 */
export function copyApi(fileId, desParentId) {
  return request.put('/file/Copyto', null, { params: { fileId, desParentId } })
}

/** 移动 */
export function moveApi(fileId, desParentId) {
  return request.put('/file/Cutto', null, { params: { fileId, desParentId } })
}

/** 下载链接（直接用 a 标签跳转，不走 axios） */
/*一般都是在页面内，所以可以获取token*/
export function getdownloadUrl(fileId) {
  const token = localStorage.getItem('token')
  return `/backend/file/download?fileId=${fileId}&token=${token}`
}

/*根据Id获取文件名*/
export function getfileNamebyId(fileId){
   return request.post('/file/getfileName',null,{params:{fileId}})
}