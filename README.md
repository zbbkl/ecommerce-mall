# 🐾 Pet Mall 宠物电商商城

一个前后端分离的宠物电商全栈项目：用户前台逛店下单、商户独立管理店铺、管理员统一运营，包含完整的订单闭环与一套按「面试项目」标准做的安全加固。

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 | Vue 2.6 + Element UI 2.15 + Vue Router + Axios + ECharts + wangEditor/mavon-editor + DOMPurify（Vue CLI 5 构建） |
| 后端 | Spring Boot 3.5.7（JDK 17）+ MyBatis-Plus + Spring Security Crypto（BCrypt）+ JWT |
| 数据库 | MySQL 8.0（库名 `bil_mall`） |
| 端口 | 前端 `8080` / 后端 `9999` |

## 功能一览

### 🛒 前台商城（用户端）
- 商品浏览、分类筛选、关键词搜索、轮播图推荐
- 商品详情（富文本详情页，DOMPurify 过滤防 XSS）、 merchants 店铺主页（点击商品卡片商户名直达）
- 购物车 → 结算下单（**服务端按商品表价格重算金额，防篡改**；事务 + 原子扣减库存）
- 余额支付（服务端校验归属与状态）、余额充值（服务端原子加余额，上限 10 万）
- 订单全流程：待付款 → 待发货 → 待收货 → 已完成 / 取消退款回补库存、确认收货
- 商品收藏、个人资料、修改密码

### 🏪 商户端（MERCHANT）
- 商户注册登录，商品上架/下架/增删改查
- 店铺订单管理与发货操作
- 店铺主页展示

### 🛠️ 管理后台（ADMIN）
- 用户管理、商户入驻审核、商品管理、商品分类管理
- 轮播图配置、订单监控、数据看板（ECharts）

## 安全设计亮点

- **JWT 验签 + 角色鉴权**：全局拦截器统一验签，`/admin/**`→ADMIN、`/merchant/**`→MERCHANT、订单/收藏→本人；匿名接口白名单收敛 + `@AuthAccess` 显式标注
- **BCrypt 密码散列**：登录/注册/改密全链路加密存储，存量明文密码命中登录后自动升级散列
- **资金安全**：支付/下单/充值金额一律取服务端数据，杜绝前端篡改；扣库存、加余额走数据库原子操作 + 事务
- **文件上传下载防护**：扩展名白名单（禁 svg）、下载路径 canonical 规范化防穿越、单文件上限 50MB
- **分页防护**：MyBatis-Plus 分页插件统一 `maxLimit(100)`，覆盖全部 12 个分页接口
- **XSS 防护**：全部 `v-html` 渲染点接入 DOMPurify 白名单过滤
- **错误码规范**：认证失败返回真实 HTTP 401/403，前端统一拦截处理

## 目录结构

```
code-scaffold/
├── vue/                        # 前端（Vue2 + ElementUI）
│   ├── src/views/front/        #   用户前台页面
│   ├── src/views/merchant/     #   商户端页面
│   ├── src/views/manager/      #   管理后台页面
│   └── src/utils/sanitize.js   #   DOMPurify XSS 过滤封装
├── springboot/                 # 后端（Spring Boot 3 + MyBatis-Plus）
│   └── src/main/java/com/example/springboot/
│       ├── controller/         #   15 个控制器（用户/商品/订单/商户/店铺/轮播…）
│       ├── service/            #   业务层（下单重算、原子扣库存、支付校验）
│       ├── common/             #   JWT 拦截器、鉴权配置
│       └── entity / mapper / exception / utils
└── sql/
    ├── code_scaffold.sql            # 纯建库脚本（含演示数据种子，导入即用）
    └── migration_merchant.sql       # 存量库商户化迁移脚本

start-all.bat / start-backend.bat / start-frontend.bat   # Windows 一键启动脚本
docs/                                                    # 过程文档（部署/测试/待办/开发计划/UI优化），索引见 docs/README.md
```

## 快速开始

### 环境要求

| 软件 | 版本 |
|---|---|
| JDK | 17+（Spring Boot 3 不兼容 JDK 8） |
| Node.js | 18+（建议 20/22） |
| MySQL | 8.0 |
| Maven | 3.8+（或用 IDEA 自带） |

### 1️⃣ 导入数据库

```bash
mysql -uroot -p < code-scaffold/sql/code_scaffold.sql
```

建库 `bil_mall` 并创建全部表结构（纯结构，不含演示数据）。

> - 含演示数据的全量导出 `bil_mall_full_*.sql`（6 个商户、40+ 件商品、订单、轮播图）**未入库**，避免业务数据上公开仓库；本机已有该文件的话可直接导入它，开箱即跑。
> - MySQL 密码不是 123456 的话，改 `springboot/src/main/resources/application.yml`，
>   或设置环境变量 `MYSQL_USER` / `MYSQL_PASSWORD`。

### 2️⃣ 启动后端（端口 9999）

双击 `code-scaffold/start-backend.bat`，或手动：

```bash
cd code-scaffold/springboot
mvn -DskipTests package
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

### 3️⃣ 启动前端（端口 8080）

双击 `code-scaffold/start-frontend.bat`，或手动：

```bash
cd code-scaffold/vue
npm install        # 首次必跑
npm run serve
```

### 4️⃣ 访问

- 前台商城：http://localhost:8080/front/home
- 后端接口：http://localhost:9999

## 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123` |
| 普通用户 | `123` | `123` |
| 商户 | `shop_demo` | `123` |
| 其他商户 | `1234` / `maomijia` / `wangwang` / `chongai` / `lingdang` | `123456` |

## 已知限制（Roadmap）

- [ ] CORS 目前 `addAllowedOrigin("*")`，生产环境需改为域名白名单
- [ ] 找回密码仅「用户名 + 手机号」弱校验，生产需接入短信验证码
- [ ] 关键路径（下单/支付/库存）缺自动化单元测试
