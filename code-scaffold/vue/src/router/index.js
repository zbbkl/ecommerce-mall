import Vue from 'vue'
import VueRouter from 'vue-router'

// 解决重复点击路由报错问题
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/home',
    children: [
      { path: '403', name: 'Auth', meta: { name: '无权限' }, component: () => import('../views/manager/Auth.vue') },
      { path: 'home', name: 'ManagerHome', meta: { name: '系统首页' }, component: () => import('../views/manager/Home.vue') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员信息', requireAdmin: true }, component: () => import('../views/manager/Admin.vue') },
      { path: 'user', name: 'User', meta: { name: '用户信息', requireAdmin: true }, component: () => import('../views/manager/User.vue') },
      { path: 'person', name: 'ManagerPerson', meta: { name: '个人信息' }, component: () => import('../views/manager/Person.vue') },
      { path: 'type', name: 'Type', meta: { name: '商品分类信息' }, component: () => import('../views/manager/Type.vue') },
      { path: 'goods', name: 'ManagerGoods', meta: { name: '商品信息' }, component: () => import('../views/manager/Goods.vue') },

      { path: 'carousel', name: 'Carousel', meta: { name: '轮播图信息' }, component: () => import('../views/manager/Carousel.vue') },
      { path: 'collect', name: 'ManagerCollect', meta: { name: '收藏信息' }, component: () => import('../views/manager/Collect.vue') },
      { path: 'orders', name: 'ManagerOrders', meta: { name: '订单信息' }, component: () => import('../views/manager/Orders.vue') },
      { path: 'merchants', name: 'ManagerMerchants', meta: { name: '商户管理' }, component: () => import('../views/manager/Merchant.vue') },
    ]
  },
  {
    // 商户端（/merchant/**）：路由守卫限制仅 MERCHANT 角色进入
    path: '/merchant',
    name: 'Merchant',
    component: () => import('../views/Merchant.vue'),
    redirect: '/merchant/home',
    children: [
      { path: 'home', name: 'MerchantHome', meta: { name: '工作台' }, component: () => import('../views/merchant/Home.vue') },
      { path: 'goods', name: 'MerchantGoods', meta: { name: '商品管理' }, component: () => import('../views/merchant/Goods.vue') },
      { path: 'orders', name: 'MerchantOrders', meta: { name: '订单管理' }, component: () => import('../views/merchant/Orders.vue') },
      { path: 'shop', name: 'MerchantShop', meta: { name: '店铺信息' }, component: () => import('../views/merchant/Shop.vue') },
      { path: 'person', name: 'MerchantPerson', meta: { name: '个人信息' }, component: () => import('../views/merchant/Person.vue') },
    ]
  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/front/Front.vue'),
    redirect: '/front/home',
    children: [
      { path: 'home', name: 'FrontHome', meta: { name: '首页信息' }, component: () => import('../views/front/Home.vue') },
      { path: 'person', name: 'FrontPerson', meta: { name: '个人信息' }, component: () => import('../views/front/Person.vue') },
      { path: 'password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../views/front/Password.vue') },
      { path: 'goods', name: 'FrontGoods', meta: { name: '全部商品' }, component: () => import('../views/front/Goods.vue') },
      { path: 'goodsDetail', name: 'GoodsDetail', meta: { name: '商品详情' }, component: () => import('../views/front/GoodsDetail.vue') },
      { path: 'shop', name: 'FrontShop', meta: { name: '店铺' }, component: () => import('../views/front/Shop.vue') },
      { path: 'cart', name: 'Cart', meta: { name: '购物车' }, component: () => import('../views/front/Cart.vue') },
      { path: 'collect', name: 'FrontCollect', meta: { name: '收藏' }, component: () => import('../views/front/Collect.vue') },
      { path: 'orders', name: 'FrontOrders', meta: { name: '我的订单' }, component: () => import('../views/front/Orders.vue') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'page-404', meta: { name: '无法访问' }, component: () => import('../views/404.vue')},
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 全局前置守卫：登录校验 + 角色隔离
// - /merchant/** 仅 MERCHANT
// - 管理后台区域（非 /front、/login、/register、/merchant）仅 ADMIN
// - 首页 / 按角色分流
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const role = user.role || ''
  if (to.path === '/'){
    if (role === 'ADMIN'){
      next('/home')
    } else if (role === 'MERCHANT'){
      next('/merchant/home')
    } else if (role === 'USER'){
      next('/front/home')
    } else {
      next('/login')
    }
    return
  }
  if (to.matched.length === 0){
    next('/404')
    return
  }
  // 区域匹配必须带路径边界：startsWith('/merchant') 会误伤 /merchants（商户管理页）
  const inArea = (p) => to.path === p || to.path.startsWith(p + '/')
  if (inArea('/merchant') && role !== 'MERCHANT'){
    next('/login')
    return
  }
  // /merchant 已在上方放行 MERCHANT，这里只管"管理后台区域仅 ADMIN"
  const isPublic = ['/login', '/register', '/front', '/merchant'].some(inArea)
  if (!isPublic && role !== 'ADMIN'){
    next('/login')
    return
  }
  next()
})

const DEFAULT_DESCRIPTION = '捞宝购物宠物电商系统，提供宠物商品浏览、购物车、订单、商户与平台管理。'
const PAGE_DESCRIPTIONS = {
  '/login': '登录捞宝购物，管理购物车、订单和账户信息。',
  '/register': '注册捞宝购物账号；商户可同时提交入驻申请。',
  '/front/home': '浏览宠物主粮、护理、玩具和零食。',
  '/front/goods': '按分类和关键词查找宠物商品。',
  '/front/goodsDetail': '查看商品价格、库存、分类和购买入口。',
  '/front/cart': '管理购物车商品和结算信息。',
  '/front/orders': '查看订单状态并完成支付、确认收货或取消操作。',
  '/front/person': '维护个人资料、头像和账户余额。',
  '/front/password': '修改账号密码。',
  '/front/collect': '查看和管理收藏商品。',
  '/front/shop': '查看店铺信息和店内商品。'
}

router.afterEach(to => {
  const pageName = to.meta && to.meta.name
  document.title = pageName ? `${pageName} | 捞宝购物` : '捞宝购物'
  const description = (to.meta && to.meta.description) || PAGE_DESCRIPTIONS[to.path] || DEFAULT_DESCRIPTION
  const meta = document.querySelector('meta[name="description"]')
  if (meta) meta.setAttribute('content', description)
})

export default router
