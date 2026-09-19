# UI 优化任务交接清单

> 记录时间：2026-09-18（接续「测试交接文档.md」之后的第二个任务）
> **当前任务**：优化前端界面，组件来源 https://github.com/uiverse-io/galaxy（Uiverse.io 的开源存档，MIT 协议，单文件 HTML/CSS 片段）。
> 改动范围目前只在**前台（用户端）**，管理端/商户端还没动。

---

## 一、任务进度（已完成 ✅ / 未完成 ⬜）

### ✅ 已完成并已验证

| 改动 | 文件 | 验证方式 |
|---|---|---|
| 商品卡片升级：hover 上浮+橙色投影+图片缩放（galaxy 风格） | `vue/src/assets/css/global.css` 新增 `.card-item` 规则（作用于 Home/Goods/Shop 三个页面的共用类） | CSS 已写入，编译通过 |
| 详情页重构：白底圆角卡片布局、渐变价格面板（大号价格+库存/销量胶囊标签） | `vue/src/views/front/GoodsDetail.vue` template 整体重写 + scoped 样式 | ✅ 截图验证，效果正常 |
| 「加入购物车」动画按钮（购物车图标 hover 从左滑入，点击缩放反馈） | 同上，改编自 galaxy `Buttons/vinodjangid07_brave-goose-29.html`，主题色改为本站橙 #ff6700 | ✅ hover 截图验证，图标滑入正常 |
| 「立即购买」渐变橙按钮（hover 上浮+阴影加深） | 同上 `.buy-btn` | ✅ 截图验证 |
| 加购成功按钮文字短暂变「已加入 ✓」 | 同上（`added` 状态 + setTimeout 1.5s 复位） | 未单独验证（低风险） |
| 回到顶部按钮（hover 展开显示"回到顶部"文字） | `vue/src/views/front/Front.vue` template+script（`showTop`/`onScroll`/`backTop`，滚动>300px 出现）+ 样式在 global.css `.back-to-top`，改编自 galaxy `Buttons/vinodjangid07_afraid-falcon-17.html` | 未单独验证 |
| 删除 Home/Goods 里旧 hover（`transform: scale(1.03)`，会与新样式冲突） | `vue/src/views/front/Home.vue`、`Goods.vue` 末尾 `<style>` | 编译通过 |

前端编译状态：**9 次 Compiled successfully，无报错**。所有改动未提交 git。

### ⬜ 未完成（接手优先级从高到低）

1. **⚠️ 功能回归（最重要）**：GoodsDetail 的三个按钮从 `el-button` 改成了**原生 `<button>`**，`@click` 逻辑（buy/addCart/collect）代码没动，但**没点过**。必须回归：立即购买下单、加入购物车（含未登录拦截提示）、收藏 toggle。购物车页结算流程也建议重走一遍。
2. **卡片 hover 验证**：去 `/front/home`、`/front/goods`、`/front/shop?id=1` 三个页面悬停卡片，确认上浮+图片放大效果生效（Shop.vue 的 `.card-item` scoped 定义只有 cursor 和 margin，全局新样式会叠加，预期正常但未截图）。
3. **回到顶部验证**：滚动页面 >300px 按钮出现、点击平滑回顶、hover 展开文字。
4. **可继续的优化方向**（用户只说"优化前端界面"，可自行取舍）：
   - 购物车页 `Cart.vue` 底部结算栏美化（合计价大号橙字 + 渐变结算按钮，风格与详情页 `.buy-btn` 统一）
   - 首页分类胶囊 `.type-item` 选中态渐变（Home.vue 样式已存在，只差选中高亮）
   - 登录页登录按钮、注册页表单（登录页已有分栏插画布局，底子不错）
   - 商户端/管理端（element-ui 后台风格，动不动影响面大，建议先问用户）
5. 改完把 UI 优化 + 之前的安全修复一起 git commit（目前全部未提交）。

---

## 二、galaxy 组件库使用要点（给接手 agent）

- 仓库结构：`Buttons/`（1231 个）、`Cards/`（726）、`Inputs/`（226）、`Forms/`、`loaders/` 等，每个组件一个 `作者_slug.html` 文件，含 HTML + `<style>`。
- **Tailwind 的组件**（class 里有 `flex`/`bg-` 等）不能直接用，项目是 Vue2 + element-ui 无 Tailwind；**纯 CSS 的直接搬进 scoped 样式即可**。
- 浏览方式：GitHub API 列文件 `https://api.github.com/repos/uiverse-io/galaxy/git/trees/main?recursive=1`，取源码 `https://raw.githubusercontent.com/uiverse-io/galaxy/main/<分类>/<文件名>`。uiverse.io 网站本身是 JS 渲染，webReader 抓不到内容。
- 文件名随机（作者_动物-数字），**没法按效果搜索**，只能按作者采样（vinodjangid07、adamgiebl、Galahhad 是多产作者）或看文件内 Tags 注释。已用过：`vinodjangid07_brave-goose-29`（购物车按钮）、`vinodjangid07_afraid-falcon-17`（回到顶部）。
- 搬运时保留代码注释里的来源标注（`From Uiverse.io by xxx`），仓库虽 MIT 但希望注明出处——本项目已在 CSS 注释里做了。
- 站点主色 **#ff6700（橙）**，组件原色需改造成本站配色。

## 三、环境状态（当前都在跑）

- 后端 9999：后台任务 `exec_ef2408c9-9ec8-47f4-b3c8-a27a60081c9e`，jar 含上一任务的安全修复
- 前端 8080：后台任务 `exec_524eeaaf-e9a2-4f94-adc5-518411676d2b`，热更新已生效
- 浏览器 in-app tab 开在 `/front/home`（已登录用户 `123`）
- 若服务挂了，启动方式见 `测试交接文档.md` 第零节（JDK 必须显式用 `C:\Program Files\Java\jdk-17`）

## 四、相关文档

- `测试交接文档.md`（项目根目录）：上一任务交接——功能验证结果、6 处后端安全修复（未提交 git）、遗留问题（/merchants 页跳登录待排查等）、完整账号表
- 浏览器自动化踩坑记录：见 `测试交接文档.md` 第五节（webpack 浮层遮挡、element-ui 下拉要用事件派发）
