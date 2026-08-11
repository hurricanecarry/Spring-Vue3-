// ========== UserInfo ==========
export interface UserInfo {
  userId: string
  password?: string
  nickname: string
  email: string
  avatar: string | null
  totalSpace: number        // 字节
  usedSpace: number         // 字节
  registerDate: string
  lastDate: string | null
  status: 0 | 1             // 0=封禁 1=正常
}

// ========== FileInfo ==========
export interface FileInfo {
  fileId: string
  userId: string
  parentId: string
  fileName: string
  fileType: 0 | 1           // 0=文件夹 1=文件
  fileCategory: FileCategory
  filePath: string | null   // 文件夹为 null
  fileSize: number           // 字节
  md5: string | null        // 文件夹为 null
  status: FileStatus
  updateTime: string
  recycleTime: string | null
  createdTime: string
}

export enum FileCategory {
  VIDEO = 1,
  AUDIO = 2,
  DOC = 3,
  ARCHIVE = 4,
  OTHER = 5,
  IMAGE = 6,
}

export type FileStatus = 0 | 1 | 2   // 0=正常 1=回收站 2=删除

// ========== ShareInfo ==========
export interface ShareInfo {
  shareId: string
  userId: string
  fileId: string
  shareCode: string | null
  expireTime: string | null   // null=永不过期
  shareType: 0 | 1            // 0=公开 1=加密
  viewCount: number
  downloadCount: number
  status: ShareStatus
  createdTime: string
}

export type ShareStatus = 0 | 1 | 2  // 0=有效 1=已取消 2=已过期

// ========== showshare 专用返回 ==========
export interface ShowShareResult {
  shareId: string
  shareType: 0 | 1
  expireTime: string | null
  rootFile: FileInfo
  currentFolder: FileInfo
  children: FileInfo[] | null
}

// ========== 登录/注册返回 ==========
export interface LoginRes {
  token: string
  userInfo:UserInfo
}

// ========== 统一响应 ==========
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}
