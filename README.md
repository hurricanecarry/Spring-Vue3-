# ☁️ Yunpan — 个人云盘

一个基于 **Vue3 + Spring Boot + Element Plus** 的个人云盘系统，支持文件管理、文件夹上传、分享链接、回收站等功能，已部署上线。

## ✨ 功能特性

- 🔐 **用户系统**：注册、登录、JWT 认证、头像上传、个人资料修改
- 📁 **文件管理**：新建文件夹、文件/文件夹上传、下载（文件夹自动打包 ZIP）、重命名、复制、移动、删除
- 🔍 **搜索**：按文件名实时过滤
- 🗑️ **回收站**：删除文件进入回收站、恢复、彻底删除
- 🔗 **分享**：公开/加密分享、提取码验证、分享列表、取消分享
- 🖼️ **图片预览**：图片缩略图、双击大图预览
- 📱 **响应式布局**：侧边栏导航、毛玻璃分享展示页

## 🛠️ 技术栈

| 层 | 技术 |
|------|------|
| 前端 | Vue 3、Vue Router、Pinia、Element Plus、Axios、Vite |
| 后端 | Spring Boot 4.1、Java 21、MyBatis-Plus、Spring Security、JWT |
| 数据库 | MySQL 8.0 |
| 部署 | Nginx + Docker |

## 📂 项目结构

```
full/
├── backend/                    # 后端 Spring Boot
│   ├── src/main/java/com/yunpan/backend/
│   │   ├── controller/         # 控制器（文件、分享、用户）
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # MyBatis-Plus Mapper
│   │   ├── service/            # 服务层
│   │   ├── config/             # 配置（Security、JWT、文件路径）
│   │   └── util/               # 工具类（Token、认证过滤器）
│   └── src/main/resources/
│       ├── application.properties        # 本地开发配置
│       └── application-prod.properties   # 生产环境配置
├── front/yunpan/               # 前端 Vue3
│   └── src/
│       ├── api/                # API 封装
│       ├── views/              # 页面（home、login、share、recycle、setting、sharelist）
│       ├── stores/             # Pinia 状态
│       ├── router/             # 路由
│       └── utils/              # 工具（axios 封装）
└── test/                       # Playwright 自动化测试
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

默认端口 `8980`，配置文件在 `src/main/resources/application.properties`（数据库地址、文件路径需按本地环境修改）。

### 前端启动

```bash
cd front/yunpan
npm install
npm run dev
```

默认端口 `5173`，通过 Vite 代理转发 `/backend` 到后端。

### 数据库

```sql
CREATE DATABASE IF NOT EXISTS yunpan DEFAULT CHARACTER SET utf8mb4;
```

三张表：`user_info`、`file_info`、`share_info`（建表 SQL 见各实体类 `@TableName` 注解）。

## 📦 部署

### 打包

```bash
# 后端
cd backend
mvn clean package -DskipTests
# 生成 target/yunpan-backend.jar

# 前端
cd front/yunpan
npm run build
# 生成 dist/
```

### 服务器部署

1. 上传 `yunpan-backend.jar` 到服务器，前端 `dist/` 内容到 Nginx 站点目录
2. 启动后端：
   ```bash
   java -jar yunpan-backend.jar --spring.profiles.active=prod
   ```
3. Nginx 配置 SPA 回退 + 反向代理：
   ```nginx
   location / {
       root /www/wwwroot/yunpan;
       index index.html;
       try_files $uri $uri/ /index.html;
   }
   location /backend/ {
       proxy_pass http://localhost:8980/backend/;
   }
   ```

## 🧪 测试

使用 Playwright 做端到端测试：

```bash
npx playwright test
```

测试覆盖：注册登录、新建文件夹、搜索过滤、并发创建、上传下载、性能测试。

## 📝 API 概览

| 模块 | 端点 |
|------|------|
| 认证 | `/backend/auth/login`、`/backend/auth/register`、`/backend/auth/avatar/save`、`/backend/auth/setting/basic/change` |
| 文件 | `/backend/file/list`、`/backend/file/upload`、`/backend/file/uploadFolder`、`/backend/file/download`、`/backend/file/NewFolder`、`/backend/file/Rename`、`/backend/file/Copyto`、`/backend/file/Cutto`、`/backend/file/Recyle`、`/backend/file/Recover`、`/backend/file/Delete`、`/backend/file/thumbnail` |
| 分享 | `/backend/share/create`、`/backend/share/verify`、`/backend/share/showshare`、`/backend/share/saveshare`、`/backend/share/listshare`、`/backend/share/cancel` |

## 📄 许可证

MIT License
