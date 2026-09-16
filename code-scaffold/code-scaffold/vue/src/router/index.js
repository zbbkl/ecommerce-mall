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

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (to.path === '/'){
    if (user.role){
      if (user.role === 'ADMIN'){
        next('/home')
      } else {
        next('/front/home')
      }
    } else {
      next('/login')
    }
  } else {
    if (to.matched.length === 0){
      next('/404')
      return
    } else {
      next()
    }
  }
})

export default router
