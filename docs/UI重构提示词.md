# UI 重构提示词（可直接粘贴给编码/设计 Agent）

> 生成时间：2026-09-26
> 用途：把本项目的 UI 重构需求一次性交代清楚，粘给任意具备文件读写能力的 Agent（dsh / Claude Code / Codex / ZCode / Cursor）即可执行
> 配套事实来源：本机 skill 清单在 `D:\workspace\AI-Fullstack-Knowledge\90-参考资料\09-工具与平台\AI编程Agent本地Skill清单.md`；设计系统参照在 `D:\workspace\awesome-design-md\design-md\`

---

## 一、直接粘贴版（推荐）

```text
你同时是设计总监和前端工程师。任务：把 D:\workspace\ecommerce-mall 这个三端电商演示项目
的 UI 按下面的设计系统重构落地——不是刷配色，是换系统（现状有 63 种颜色 / 15 种字号 /
16 种圆角 / 47 条阴影 / 全站 0 处字体声明，前几轮只把 Element 默认蓝改成了橙）。

【第一步：先读，别急着写】
1. 读这些文件摸清现状：code-scaffold/vue/src/assets/css/{global,merchant,admin,front,manager}.css、
   src/views/front/{Home,Goods,GoodsDetail,Cart,Orders}.vue、src/views/front/Front.vue、
   src/views/Merchant.vue、src/views/Manager.vue、public/index.html
2. 调用这些本机已装的设计 skill（有就显式激活；目标环境若没有，去 ~/.dsh/skills、~/.codex/skills、
   ~/.workbuddy/skills、~/.zcode/skills 里找同名目录读 SKILL.md）：
   frontend-design（艺术方向优先、避开 AI 通病）、redesign-skill（审计+升级现有项目）、
   high-end-visual-design（高端版式与微交互）、web-design-guidelines（无障碍 MUST/NEVER）、
   make-interfaces-feel-better（同心圆角/阴影/动画细节）
3. 读 D:\workspace\awesome-design-md\design-md\airbnb\DESIGN.md 与 notion\DESIGN.md，
   按它们的 token 文档格式（YAML frontmatter: colors/typography/rounded/spacing/components）
   为本项目写一份 DESIGN.md 放在仓库根目录

【项目事实（已审计，别再猜）】
- 技术栈：Vue 2.6 + Element UI 2.15 + vue-cli，后端 Spring Boot；三端：前台 /front、商户端 /merchant、管理端 /
- 品牌资产真相：src/assets/logo.svg 是手绘猫插画，色板 #EAE8B5 奶油黄 / #EFA69B 蜜桃粉 /
  #6A9636 橄榄绿 / #FFF274 亮黄 / #818A8A 灰 / #020202 墨 —— 现在的橙色 #ff6700 不是品牌色，是脚手架遗留
- 商品图 47 张，仅 7–53KB 小图 → 走「卡片 + 托盘」，不要做全出血大图
- 首页现状 = 400px 轮播 + #606266 灰块分类栏 + 「新品上架」「热销商品」两段完全相同的四列卡片行（模板感来源）
- 前台所有页面容器是 width:70%，没有 max-width（宽屏行宽 ~1800px，不可读）
- 卡片上没有加购入口；正文大量 11–13px；200 处行内 style

【已定艺术方向：鲜食铺 · Larder（不要另选风格）】
主张：像一家讲究的宠物鲜食铺——干净、可读、有食欲、可信；信息像食品标签一样被认真排版。
色板（直接落地为 CSS 变量）：
  --c-canvas #FBFAF3   --c-surface #FFFFFF   --c-surface-sunken #F4F2E7
  --c-ink #14150F      --c-ink-body #3A3D33  --c-ink-muted #6B6F63   --c-ink-subtle #9AA093
  --c-line #E6E4D8     --c-line-strong #CFCDBF
  --c-brand #3E5A20（主色）  --c-brand-hover #4C6B2A  --c-brand-soft #EAF0DC
  --c-accent #B4432F（价格/库存紧张/危险）  --c-accent-soft #F7E7E2
  --c-flag #F2D25C（新品/限时旗标）  --c-success #2F7D4F  --c-warning #C98A16  --c-info #4A6B7C
字体：只用系统字体栈，禁止下载/自托管字体（要能离线演示）：
  --font-ui: "PingFang SC","HarmonyOS Sans SC","MiSans","Microsoft YaHei","Noto Sans SC",-apple-system,system-ui,sans-serif
  价格/数量/统计用 font-variant-numeric: tabular-nums
字号阶梯（角色化，禁止自由取值；正文 ≥14px）：
  display-lg 32/1.2/600/-0.6px · display 26/1.25/600/-0.4px · title-lg 20/1.3/600/-0.2px ·
  title 16/1.4/600 · body 14/1.6/400 · body-strong 14/1.6/500 · caption 13/1.5/400 ·
  micro 12/1.4/500 · price-lg 28/1.1/700 · price 20/1.1/700
间距：4/8/12/16/20/24/32/40/48/64/80/96（8px 基线；前台区块 64–96，后台紧凑 8–16）
圆角：4/8/12/16/999；同心规则=外层 16 + 内边距 8 → 内层 8
阴影（暖墨、单一光源自上而下，禁纯黑 0.3、禁彩色发光）：
  e1 0 1px 2px rgba(20,21,15,.04), 0 2px 8px rgba(20,21,15,.04)
  e2 0 2px 4px rgba(20,21,15,.05), 0 10px 24px rgba(20,21,15,.08)
  e3 0 8px 16px rgba(20,21,15,.08), 0 24px 48px rgba(20,21,15,.12)
  e4 0 16px 32px rgba(20,21,15,.12), 0 32px 64px rgba(20,21,15,.16)
动效：--ease-out cubic-bezier(.22,1,.36,1)；120ms 状态色 / 200ms 按钮卡片 / 320ms 浮层 / 500ms 入场；
      只动 transform、opacity、颜色；尊重 prefers-reduced-motion
网格：前台 max-width 1280px + 24px gutter（替换 width:70%）；商品网格 repeat(auto-fill,minmax(240px,1fr))；
      后台 fluid + 侧栏 200px，表格行高 40（紧凑 36），表头用中性底 surface-sunken（不再彩色）
两个签名元素：① 配方标签（商品卡与详情页的「成分/适用/规格」三行细线信息表，数字等宽）
              ② 图片托盘（商品图放在带内发丝线的浅色托盘里，同心圆角）

【硬约束】
- 不换框架、不上 Tailwind、不引任何新依赖、不引外部字体/CDN（离线演示必须可用）
- 单主题（不做暗色模式）；不破坏任何现有功能与接口
- 这是学生面试项目：方案从简，优先 CSS 变量 + 公共组件，禁止为炫技引入构建期工具/动画库
- 三端共用同一套 token；后台不单独设计色板，只出「紧凑模式」

【分批执行，一批一停】
P0 设计系统：tokens.css + base.css + element-override.css（合并现有 global/merchant/admin 三份覆盖）+ DESIGN.md
P1 前台骨架：布局容器/页头/页脚/商品卡（含配方标签与加购）/空态组件 + 首页首屏重做（分类改白卡图标行，
   新品用横向滚动、热销用带排名数字的榜单，两段不再同构）
P2 前台交易链路：Goods 筛选与网格 + GoodsDetail（图库托盘 + sticky 购买栏）+ Cart + Orders（4 步状态进度）
P3 前台次级页：Person / Collect / Shop / Login / Register / 404 + 页脚与 meta
P4 商户端 5 页；P5 管理端 12 页（表格/筛选/操作列降噪：详情/编辑/删除改文字链，不要满屏绿橙红三色按钮）
每批流程：先列改动点 → 停下等我确认 → 改代码 → 自检 → 汇报 → git commit。不要一次做完所有批次。

【每批必须自检并贴出证据】
1. token 收敛断言（用脚本统计 src 下 .vue/.css）：
   唯一 6 位色值 ≤ 20 且全部来自 token 白名单；font-size ⊆ {11,12,13,14,16,18,20,26,28,32}；
   border-radius ⊆ {4,8,12,16,999}；box-shadow 配方 ⊆ 4 层
2. 禁止模式 grep 必须为空：transition: all / rgba(0,0,0,.3) / z-index: 9999 / width: 70% / <html lang="">
3. 对比度 ≥ WCAG AA（正文 4.5:1，大字 3:1）；键盘可达 + focus-visible；图片有 alt；reduced-motion 生效
4. 关键页截图存 .workbuddy/ui-v2-*.png
5. 功能回归：三端登录 / 加购→购物车→结算→支付 / 取消支付 / 确认收货 / 商户发货 / 平台商户审核
   环境：MySQL 自带；后端 cd code-scaffold/springboot && java -jar target/springboot-0.0.1-SNAPSHOT.jar --server.port=9999；
   前端 cd code-scaffold/vue && node node_modules/@vue/cli-service/bin/vue-cli-service.js serve --port 8080
   账号：admin/123（管理端）、shop_demo/123（商户端）、tom/123（前台）

【绝对不要】
- 不要 AI 味文案（Elevate / Seamless / Unleash / Next-Gen / 一站式赋能 / 开启新篇章）
- 不要在标题上方加全大写小标签、不要给每张卡都做 hover 上浮、不要彩色发光阴影、不要渐变铺满顶栏
- 不要 3–4 等分卡片行当作唯一版式、不要 12px 以下正文、不要用颜色作为唯一状态提示
- 不确定的取舍先问我，不要擅自扩大范围

【汇报格式（每批）】
① 改了什么（文件:行）② 证据（断言输出 / 截图路径 / 回归结果）③ 与 token 的偏差及原因 ④ 下一批计划
```

---

## 二、变体提示词

### 变体 A：只做 P0（设计系统落地，最小风险起步）

```text
在 D:\workspace\ecommerce-mall（Vue2 + Element UI 2.15）里落地一套设计 token 系统，只做这一批，不碰页面结构。

产出三个文件：
- src/assets/css/tokens.css：CSS 变量（颜色见下、间距 4/8/12/16/20/24/32/40/48/64、圆角 4/8/12/16/999、
  阴影 4 层 e1–e4、缓动 --ease-out cubic-bezier(.22,1,.36,1)、时长 120/200/320/500、
  层级 --z-sticky 10/--z-drawer 100/--z-modal 1000/--z-toast 2000、字体栈与字号阶梯）
- src/assets/css/base.css：元素基线（盒模型、正文 14px/1.6、标题字重与负字距、tabular-nums、
  链接与 focus-visible 光圈、img 托盘描边、prefers-reduced-motion 降级）
- src/assets/css/element-override.css：Element UI 覆盖（主按钮=--c-brand、输入聚焦主色、
  表格表头 surface-sunken、行 hover 暖墨 4%、分页当前页主色、状态 tag 语义色、弹窗圆角 12px、
  抽屉/模态阴影 e3/e4）

色板（唯一真源，禁止出现其它色值）：
#FBFAF3 #FFFFFF #F4F2E7 #14150F #3A3D33 #6B6F63 #9AA093 #E6E4D8 #CFCDBF
#3E5A20 #4C6B2A #EAF0DC #B4432F #F7E7E2 #F2D25C #2F7D4F #C98A16 #4A6B7C

要求：main.js 只引这三个文件；删除 global.css / merchant.css / admin.css 里的重复覆盖（保留 front.css 的页头布局）；
三端（前台/商户端/管理端）都自动生效，不许再靠容器类各写一份。改完跑一次生产构建，并列出
「唯一色值/字号/圆角/阴影」四个统计数字的前后对比。不要改任何 .vue 的模板结构。
```

### 变体 B：只做某一个页面（举例：首页首屏）

```text
项目 D:\workspace\ecommerce-mall，Vue2 + Element UI 2.15，艺术方向已定：
画布 #FBFAF3 / 墨 #14150F / 主色橄榄绿 #3E5A20 / 强调砖红 #B4432F / 旗标亮黄 #F2D25C；
系统字体栈，价格用 tabular-nums；间距 8px 基线；圆角 4/8/12/16/999；阴影 4 层（暖墨、单一光源）；
只动 transform/opacity/颜色，尊重 prefers-reduced-motion。

只重构 src/views/front/Home.vue 的首屏与两个商品区块：
- 首屏：品牌主张（display-lg 32/600）+ 主推商品大卡；轮播降级为「编辑推荐」位，去掉默认箭头样式
- 分类：现在的 background-color:#606266 灰块改成白卡图标行（选中态用 --c-brand-soft 底 + --c-brand 字）
- 「新品上架」改横向滚动（scroll-snap），「热销商品」改带排名数字的榜单 —— 两段版式不许再同构
- 商品卡按新解剖重做：图片托盘（内发丝线 + 同心圆角）+ 标题两行截断 + 配方标签（成分/适用/规格，数字等宽）
  + 价格 price 20/700 + 加购按钮（缺货置灰）
- 小节标题去掉「左侧 5px 色条 + 查看更多>>」，改成标题 + 文字链接
约束：不改接口调用与数据结构；正文不小于 14px；宽屏 max-width 1280px；写完贴出改动点、截图、以及
「唯一色值/字号/圆角/阴影」统计。不要顺手改其它页面。
```

### 变体 C：出视觉稿（给 Stitch / imagegen / Figma 类工具）

```text
为「宠物食品电商 · 网页端」出一套视觉稿。品牌资产：手绘猫插画（奶油黄 #EAE8B5 / 蜜桃粉 #EFA69B /
橄榄绿 #6A9636 / 亮黄 #FFF274）。
风格：干净讲究的宠物鲜食铺 —— 明亮纸感画布 #FBFAF3、墨色文字 #14150F、橄榄绿主色 #3E5A20、
砖红强调 #B4432F（价格/促销）、亮黄旗标 #F2D25C；系统无衬线中文字体；价格用等宽数字；
圆角 4/8/12/16；阴影极轻（暖墨、单一光源）；大量留白。
需要 5 张独立画面（每张单独出图，不要拼成一张）：
1. 首页首屏（品牌主张 + 主推商品大卡 + 分类图标行）
2. 商品列表页（筛选栏 + 4 列商品网格 + 商品卡特写：图片托盘 + 成分/适用/规格标签 + 价格 + 加购按钮）
3. 商品详情页（图库 + 信息三栏 + 底部固定购买栏）
4. 购物车页（缩略图行 + 底部结算栏）
5. 后台管理表格页（中性表头、40px 行高、状态胶囊、操作列只留文字链）
不要：霓虹渐变、彩色发光阴影、深色背景、衬线大标题、满屏彩色按钮、圆角药丸滥用。
```

---

## 三、使用说明

| 场景 | 用哪版 | 备注 |
|---|---|---|
| 想让它自己分批做完整重构 | 一、直接粘贴版 | 它会先停下来等你确认 P0 改动点 |
| 想先看系统层效果、风险最小 | 变体 A | 不动页面结构，只换 token 与 Element 覆盖，最容易回滚 |
| 只想先改一个页面 | 变体 B | 换掉页面名即可复用（Goods / Detail / Cart / Orders 同理） |
| 想让别的工具出设计图 | 变体 C | 每张画面独立出图，方便对着图验收 |

**粘贴前建议**：确认目标 Agent 能读到这三个位置——项目 `D:\workspace\ecommerce-mall`、设计参照
`D:\workspace\awesome-design-md\design-md`、skill 目录（`~/.dsh/skills`、`~/.codex/skills`、
`~/.workbuddy/skills`、`~/.zcode/skills` 中的任意一个）。读不到设计参照时，把提示词里的
「读 airbnb/notion 的 DESIGN.md」删掉即可，其余内容自洽。
