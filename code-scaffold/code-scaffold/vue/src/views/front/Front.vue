<template>
  <div>
    <div class="header">
      <div class="front-header">
        <a href="/front/home">
          <div class="front-header-left">
            <img src="@/assets/logo.svg" alt="">
            <div class="title">捞宝购物</div>
          </div>
        </a>

        <div class="front-header-center">
          <div @click="goPage(item)" class="menu-item" v-for="item in menuList" :key="item.text" :class="{'menu-item-active' : isActive(item)}">
            {{ item.text }}
            <span v-if="item.badge && badges[item.badge]" class="menu-badge-num">{{ badges[item.badge] }}</span>
          </div>
        </div>

        <div class="front-header-right">
          <div v-if="!user.username" class="front-header-right-button">
            <el-button type="primary" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="success" plain @click="$router.push('/register')">注册</el-button>
          </div>
          <!-- 登录展示 -->
          <div v-else>
            <el-dropdown>
              <div class="front-header-dropdown">
                <img :src="user.avatar" alt="">
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>
                  <div style="color: #333">{{user.name}}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <a style="color: #333" href="/front/person"><div>个人信息</div></a>
                </el-dropdown-item>
                <el-dropdown-item>
                  <a style="color: #333" href="/front/cart"><div>我的购物车<span v-if="badges.cart" class="menu-badge-num">{{ badges.cart }}</span></div></a>
                </el-dropdown-item>
                <el-dropdown-item>
                  <a style="color: #333" href="/front/collect"><div>我的收藏</div></a>
                </el-dropdown-item>
                <el-dropdown-item>
                  <a style="color: #333" href="/front/orders"><div>历史订单<span v-if="badges.pending" class="menu-badge-num">{{ badges.pending }}</span></div></a>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="logout">退出</div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div class="main-body">
      <router-view ref="child" @update:user="updateUser" @update:cart="loadBadges" />
    </div>

    <Footer />
  </div>
</template>

<script>

import Footer from "@/conponents/Footer.vue";
import cart from "@/utils/cart";

export default {
  name: "FrontLayout",
  components: {Footer},
  data () {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      menuList: [
        {text: "首页", path: '/front/home'},
        {text: "全部商品", path: '/front/goods'},
        {text: "购物车", path: '/front/cart', badge: 'cart'},
        {text: "历史订单", path: '/front/orders', badge: 'pending'},
        {text: "个人中心", path: '/front/person'},
      ],
      badges: {cart: 0, pending: 0},
    }
  },
  created() {
    this.loadBadges()
  },
  watch: {
    // 路由变化时刷新角标（加购、结算、支付后都能及时看到数量变化）
    '$route'() {
      this.loadBadges()
    }
  },
  methods: {
    goPage(item) {
      location.href = item.path
    },
    isActive(item) {
      return this.$route.path === item.path
    },
    loadBadges() {
      if (!this.user.id) {
        return
      }
      this.badges.cart = cart.count(this.user.id)   // 购物车保存在本地，不再调接口
      this.$request.get('/orders/count', {params: {state: '待付款'}}).then(res => {
        this.badges.pending = res.code === '200' ? (res.data || 0) : 0
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('user') || '{}')   // 重新获取下用户的最新信息
      this.loadBadges()
    },
    logout() {
      localStorage.removeItem("user");
      location.href = '/front/home'
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/front.css";

.menu-badge-num {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  margin-left: 4px;
  border-radius: 9px;
  background-color: #f56c6c;
  color: #fff;
  font-size: 12px;
  text-align: center;
}
</style>
