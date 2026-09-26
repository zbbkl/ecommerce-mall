# 前台 UI 优化方案

> 制定时间：2026-09-19
> 依据：`UI优化交接文档.md` 未完成项 + 本次功能回归新发现的问题
> 组件来源：https://github.com/uiverse-io/galaxy（MIT，单文件 HTML+CSS）
> 站点主色：**#ff6700（橙）**；技术栈 Vue2 + element-ui（无 Tailwind）

---

## 一、现状盘点

### 已完成（代码在工作区，**全部未提交 git**）

| 改动 | 文件 |
|---|---|
| 商品卡片 hover 上浮 + 橙色投影 + 图片缩放 | `assets/css/global.css`（`.card-item`） |
| 详情页白底圆角卡片重构 + 渐变价格面板 | `views/front/GoodsDetail.vue` |
| 「加入购物车」图标滑入动画按钮 / 「立即购买」渐变橙按钮 | 同上 |
| 回到顶部按钮（hover 展开文字） | `views/front/Front.vue` + `global.css` |
| 清理 Home/Goods 旧 hover（与新样式冲突） | `views/front/Home.vue`、`Goods.vue` |

### 本次功能回归结果（2026-09-19，✅ 全部通过并已提交）

| 场景 | 结果 |
|---|---|
| 未登录点「加入购物车」 | ✅ 弹「请先登录」并跳 `/login` |
| 登录后加购 → 落 localStorage `cart_4` | ✅ `[{goodsId:1,nums:1}]` |
| 购物车勾选 → 结算 → 跳待付款页 + 移除已结算项 | ✅ 全链通过 |
| 立即购买 | ✅ 下单成功跳待付款页 |
| 收藏 | ✅ **已修复**（见下）：接口 `test_collect.py` 10/10 全绿，UI 点击后按钮稳定保持「已收藏」 |
| 回到顶部、卡片 hover（三页） | ✅ 已验证：初始隐藏→滚动出现→hover 展开→点击回顶；三页 -6px 上浮+橙投影+图 scale1.06 |

### ✅ 已修复的真 bug：收藏状态回退（2026-09-19，commit d6bba97）

- **现象**：详情页点收藏，notify 提示成功、DB 已写入，但按钮状态立刻回退为未收藏（刷新后也是未收藏）。
- **根因链**：
  1. `/goods/selectById` 标注了 `@AuthAccess`（匿名可浏览）；
  2. `JwtInterceptor.preHandle` 对 `@AuthAccess` 端点**直接 `return true`，不写入 `LOGIN_ID/LOGIN_ROLE`**；
  3. `TokenUtils.getCurrentUser()` 因此恒为 null；
  4. `GoodsServiceImpl.selectById` 里填充 `isCollect` 的分支整体被跳过 → 接口返回 `isCollect: null`；
  5. 前端 `collect()` 成功后先 `isCollect = true`，紧接着 `loadGoods()` 回来把 `null` 覆盖上去 → 按钮回退。
- **修复**：`JwtInterceptor` 新增 `tryResolveAnonymous()`——`@AuthAccess` 端点携带合法 token 时**仍解析登录态并写入上下文**，解析失败静默按匿名处理（不影响匿名浏览）。
- **状态**：✅ 已编译重启、接口级（`test_collect.py` 10/10）+ UI 双层验证通过，commit `d6bba97`。

---

## 二、优化目标与设计原则

1. **只改外观与交互反馈，不改业务逻辑**；每处改动都要回归对应功能。
2. 视觉统一到主色 **#ff6700**，圆角 12–16px，hover 有位移/阴影反馈，动效控制在 200–300ms。
3. 优先搬运 galaxy 的**纯 CSS 组件**（含 `flex`/`bg-` 等 Tailwind 类的不可直接使用），搬运时保留 `From Uiverse.io by xxx` 出处注释。
4. 管理端 / 商户端 element-ui 后台风格**先不动**（影响面大），待你单独确认。
5. 每一步做完立即截图验证 + 功能回归，通过后再进入下一步。

---

## 三、分阶段方案

### 阶段 0：收尾回归 + 修复 isCollect bug（✅ 2026-09-19 已完成，commit d6bba97 + 823fcef）

| 步骤 | 结果 |
|---|---|
| 0.1 停后端解锁 jar | ✅ taskkill PID 38032 |
| 0.2 重新编译 | ✅ MVN_EXIT=0 |
| 0.3 重启后端 | ✅ 9999 就绪 |
| 0.4 接口验证 | ✅ `test_collect.py` 10/10 全绿（匿名 null / 登录布尔 / toggle 翻转 / 数据还原） |
| 0.5 UI 收藏验证 | ✅ 点收藏 → 按钮保持「已收藏」（loadGoods 不再覆盖回退） |
| 0.6 回到顶部 | ✅ 初始 display:none → 滚动后 flex → hover 展开文字 → 点击回顶 scrollY 0 |
| 0.7 卡片 hover 三页 | ✅ home/goods/shop 全部 -6px 上浮 + 橙投影 + 封面图 scale(1.06) |

> 测试数据已清理（goods 3/5 收藏已还原）；`mvn_out.txt` 等临时文件未入库。

### 阶段 1：购物链路视觉统一（✅ 2026-09-19 结算栏部分完成）

- `Cart.vue` 底部结算栏：✅ 已完成——浅橙渐变结算条、合计金额大号橙字（￥符号小号+数字大号）、渐变「结 算」按钮（改编自 galaxy `vinodjangid07_clever-bird-35`，箭头旋转为右向 hover 滑入，配色与详情页 `.buy-btn` 统一，禁用态浅橙）。
- 回归：加购→勾选→合计实时更新→hover 箭头滑入→结算跳待付款页+购物车清空，全链通过。
- 购物车表格 hover 行高亮、删除按钮危险色统一：⬜ 待做（可与阶段 2 合并）。

### 阶段 2：首页与商品列表（✅ 2026-09-19 完成）

- 首页分类侧栏 `.type-item`：✅ hover 橙色渐变 + 圆角 + 箭头右移动效（点击本就跳转列表页，无需选中态）。
- 三页卡片信息层次统一：✅ 价格主色 #FFA500→#ff6700、￥ 小号(13px)+数字大号(20px)、销量「累计热销：X」→「已售 X」——抽为 global.css 公共类 `.card-price`/`.card-sales`，Home(2处)/Goods/Shop 四处模板统一替换；Shop 封面图补 object-fit:cover。
- Goods 搜索区：✅ 输入框圆角胶囊+聚焦橙色描边（10px→13px 字号），搜索按钮渐变拼接；分类胶囊选中态橙色渐变+阴影、未选中 hover 浅橙上浮。
- 回归：✅ 分类点击（selectedCategoryId 切换+选中态）、搜索「猫咪」（2 条正确结果）、三页截图验证。

### 阶段 3：登录 / 注册页（✅ 2026-09-19 完成）

- 两页品牌区：✅ 深灰底 → 主色橙渐变（160deg #ff9a3d→#ff6700→#e85500），插画白底圆角卡 + 阴影。
- 按钮：✅ 登录/注册按钮橙色渐变 + hover 上浮（与 .buy-btn/.settle-btn 同源）；注册按钮原 darkseagreen 内联样式已移除。
- 输入框：✅ 聚焦态 element 默认蓝 → 主橙描边 + 橙色光晕（::v-deep 覆盖）。
- 链接：✅ 注册账号/返回登录 #409eff → #ff6700。
- 回归：✅ 真实表单登录流程（填表→角色下拉→登录→跳 /front/home + token）；注册页空表单校验正常触发；两页截图。

### 阶段 4：个人中心与订单页（✅ 2026-09-19 完成）

- 订单状态标签：✅ 从 el-tag 深色底改为圆角胶囊 + 语义配色（待付款橙/已支付绿/已发货蓝/已完成深绿/已取消灰）。
- 按钮统一：✅ 查询/支付按钮渐变橙（.query-btn/.pay-btn），取消支付/确认收货/合并支付/删除统一 plain 化。
- 个人中心：✅ 卡片标题橙色左边框，保存按钮渐变橙，修改密码/充值改 plain。
- 收藏列表：✅ 价格统一 .card-price、取消收藏按钮 orangered→#ff6700、封面补 object-fit。
- 回归：✅ 状态机三项流转（取消支付：待付款4→3/已取消+1；支付：已支付+1；确认收货：已发货-1/已完成+1），Person/Collect computed style + 截图验证。

### 阶段 5：商户端 / 管理端 / 全站一致性（✅ 2026-09-24 ~ 09-26 完成）

| 批次 | 内容 | commit |
|---|---|---|
| 批次 1 | 商户端主题化：顶部橙渐变、侧边栏浅橙选中态、卡片 12px 圆角 | `dece784` |
| 批次 2 | 商户端表格表头浅橙、primary 按钮/订单状态胶囊/分页/输入框聚焦橙、弹窗圆角 | `fd837f7` |
| 管理端 | 新增 `admin.css`（作用域 `.admin-container`），与商户端同套规则 | `5ee9650` |
| 第 3 轮 | 全站一致性收口（见下） | 本轮 |

#### 第 3 轮：全站一致性收口（2026-09-26）

**问题**：前面几轮的主题覆盖写在 `merchant.css` / `admin.css` 里（容器作用域），**前台（含登录/注册）没跟上**，仍是 Element 默认蓝；表格行 hover 是 Element 的冷蓝灰 `#F5F7FA`，压在暖色卡片里发脏。

**做法**：把 Element 覆盖提升到 `assets/css/global.css`（`main.js` 引入 = 全站生效），成为唯一定义；两个容器文件里的同名规则值相同，保留为容器级兜底。

| 项 | 改前 | 改后 |
|---|---|---|
| 表格行 hover | `#F5F7FA`（冷蓝灰） | `#fff3e8`（暖橙，三端统一） |
| 表格表头 | 前台是白底 | `#fff7f0`（与商户/管理端一致） |
| 表格分隔线 / 斑马纹 | `#EBEEF5` / `#FAFAFA` | `#f5ece4` / `#fffdfb` |
| 选中行 / 当前行 | `#ecf5ff`（蓝） | `#ffe9d6` |
| 分页当前页、状态筛选单选 | `#409EFF`（蓝） | `#ff6700` |
| 危险按钮（删除/清空/停用） | `#f56c6c`（偏粉） | `#e64340`（暖红，**保留红=危险语义**） |
| 危险 tag（库存预警） | Element 默认粉红 | `#e64340` + 暖底 |
| 输入框聚焦 | 部分页面仍是蓝 | 全站橙描边 + 浅橙光晕 |
| 弹窗 | 直角 | 12px 圆角 + 标题加粗 |
| `prefers-reduced-motion` | 未处理 | 全站降级动效（无障碍） |

**同轮顺带修正的语义色误用**（均为 1 行改动，可单独回退）：

- 购物车页「待支付订单」：`success`（绿）→ `primary`（橙）——入口级动作不该穿 success 语义色
- 前台头部「注册」：`success`（绿）→ `primary`（实心橙），与「登录」（描边橙）成对
- 注册页主按钮：`type="success"` → `type="primary"`（原绿色只是被 `.login-btn` 渐变盖住，**禁用态会露绿**）
- 商户端工作台图标、库存预警数字、店铺驳回原因：`#f56c6c` → `#e64340`
- 购物车/登录/注册按钮的 `transition: all` → 显式属性（`transform, box-shadow, filter`）

**验证**：逐项断言 computed style（改前/改后对照）+ 截图像素采样（`#FFF3E8` 行 hover、`#FFF7F0` 表头、`#FFF1F0` 删除按钮）+ 功能回归（加购 → 删除确认 → 通知 → 行数变化 → 重新加购）+ 生产构建通过。
截图：`.workbuddy/ui-round3-cart-{before,before-hover,hover,after}.png`、`ui-round3-cart-confirm.png`。

#### 阶段 5 遗留（待拍板，本轮未做）

| 项 | 规模 | 说明 |
|---|---|---|
| 后台绿色「查询 / 详情 / 预览 / 修改密码」按钮 | 12 个文件、**20 处** | `type="success"` 被当通用按钮色用（`Admin/Carousel/Collect/Goods/Merchant/Orders/Person/Type/User.vue` + 商户端 `Goods/Orders/Person.vue` + 前台 `Orders.vue`）。改法：`type="success"` → 默认灰（次要动作）或 `primary plain`。**会改变所有 CRUD 页观感，需你确认再动**。注：另 3 处 success 用法语义正确（`Merchant.vue` 审核「通过」「恢复」、`merchant/Goods.vue`「上架」），不动 |
| 前台其余 `transition: all` | 8 处 | `front/Goods.vue`(3)、`Orders.vue`(2)、`GoodsDetail.vue`、`Home.vue`、`Person.vue`。纯代码质量，无视觉差异 |
| 订单状态 tag「已发货」用蓝 `#409eff` | 1 处 | 属**状态语义色**（待付款橙/已支付绿/已发货蓝/已完成深绿/已取消灰），本轮按「保留语义色」未改 |

---

## 四、执行流程约定（按你的要求）

每一项严格按这个循环走，不再跳步：

```
列改动点 → 你确认（或按已批准方案） → 改代码 → 截图 + 功能回归 → 汇报结果 → git commit
```

**节奏**：一次只推进 1–2 项，做完汇报，其余留待下一轮。

---

## 五、验证手段

1. **UI 交互**：agent-browser，**每条 bash 调用必须以 `open` 开头**（localStorage 不跨调用持久），单条链内完成「open → 登录 → 导航 → 操作 → 断言」，用 `ping -n N` 等待；地址用 `127.0.0.1:8080`（避开 localhost 的 localStorage SecurityError）。
2. **接口级**：curl 直连 `localhost:9999`，作为 UI 断言的交叉验证。
3. **证据留存**：每步截图存 `.workbuddy/`，关键断言输出贴进汇报。

---

## 六、风险与回滚

| 风险 | 应对 |
|---|---|
| 改样式误伤共用类（如 `.card-item` 被多页复用） | 逐个页面截图确认；必要时新增独立类名而非改共用类 |
| 编译被 jar 文件锁挡住 | 先停后端进程再编译（阶段 0 已列入） |
| 前端热更新失败 | 检查 `serve_out.txt` 是否有 Compiled successfully |
| 改动过多难以回滚 | 每个阶段一个 commit，粒度可回退 |

---

## 七、待你确认

1. **优化范围**：本轮只做前台（阶段 0–4），还是也要把商户端/管理端（阶段 5）纳入？
2. **优先级**：按上述顺序（先修 bug 再美化），还是你希望先看某个具体页面的效果？
