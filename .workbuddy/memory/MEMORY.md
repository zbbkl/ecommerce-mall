# 电商平台项目 — 长期项目笔记

## 项目性质（2026-09-16 确认）
- **这是学生的面试项目**，用于求职演示/面试，不是生产系统。
- 用户明确要求：**方案从简**，不要过度工程化。安全修复够讲出亮点即可，不必上企业级方案
  （例：DOMPurify 被砍掉；BCrypt 保留因为是经典面试话术；短信验证码、单测基建不做）。
- 面试价值点（可在回复里帮用户提炼，但别长篇大论）：JWT 三角色统一验签、按商户拆单、
  原子扣库存防超卖、BCrypt 密码、支付服务端重算金额防刷钱。

## 技术栈与结构
- `code-scaffold/code-scaffold/`（双层嵌套目录）：`springboot/`（Spring Boot 3.5.7, JDK17, MyBatis-Plus, 端口 9999）+ `vue/`（Vue 2.6 + Element UI 2.15 + wangeditor, 端口 8080）+ `sql/`
- 三角色：ADMIN（admin 表）、USER（user 表）、MERCHANT（merchant 表）；单一 `jwt.secret` + role 声明
- 商户演示账号 `shop_demo / 123`；管理员 `admin / 123`；用户 `tom / 123`（密码现为 BCrypt 兼容登录，存量明文登录时自动升级散列）
- 购物车在前端 localStorage，结算走 `/orders/settle` 按商户拆单（parent_no 批次号）

## 关键命令与环境坑
- git 仓库在 `D:\workspace\电商平台项目`（2026-09-16 init，baseline → merchant 改造 → 安全修复）
- 后端编译（Bash 里 mvn 坏，用 classworlds launcher 直调）：
  `"/c/Program Files/Java/jdk-17/bin/java" -classpath "D:\DevTools\maven\apache-maven-3.9.10\boot\plexus-classworlds-2.9.0.jar" -Dclassworlds.conf="D:\DevTools\maven\apache-maven-3.9.10\bin\m2.conf" -Dmaven.home="D:\DevTools\maven\apache-maven-3.9.10" -Dmaven.multiModuleProjectDirectory="D:\workspace\电商平台项目\code-scaffold\code-scaffold\springboot" org.codehaus.plexus.classworlds.launcher.Launcher -q compile`
  （multiModuleProjectDirectory 必须设；mvn 位于 D:\DevTools\maven）
- 前端构建：PATH 加 `/d/DevTools/nodejs` 后 `npm run build`（约 35s）
- Bash 前缀：`export PATH="/c/Users/18789/.workbuddy/binaries/PortableGit/versions/1.2.0/usr/bin:/c/Windows/System32:$PATH"`
- `start-*.bat` 是 GBK 编码，别用 VS Code 编辑

## 文档约定
- `待修复与待办.md`：问题清单（P0/P1/P2 编号），修复后更新对应表格
- `商户端开发计划.md`：商户端改造的设计决策记录（D1/D2/D3）
- 存量库迁移：`sql/migration_merchant.sql`；新库直接 `code_scaffold.sql`

## 遗留（用户接受不做）
- v-html 未过滤（跳过）、CORS 全放开、resetPassword 弱校验、无单测、未做动态运行验证
