<template>
  <div class="manager-container merchant-container">
    <header class="manager-header">
      <div class="manager-header-left clickable" @click="$router.push('/merchant/home')">
        <button class="mobile-menu-toggle" type="button" :aria-expanded="String(menuVisible)" aria-label="打开商户菜单" @click.stop="menuVisible = true">
          <i class="el-icon-s-unfold" aria-hidden="true"></i>
        </button>
        <img src="@/assets/logo.svg" alt="捞宝购物标志" />
        <div class="title">商户管理中心</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/merchant/home' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <span v-if="shopState && shopState !== '已通过'" class="shop-state-tag">
          <el-tag :type="shopState === '待审核' ? 'warning' : 'danger'" size="small">{{ shopState }}</el-tag>
        </span>
        <el-dropdown placement="bottom">
          <button class="avatar" type="button" aria-label="打开商户菜单">
            <img :src="user.logo || defaultAvatar" alt="商户头像" />
            <span>{{ user.shopName || '商户' }}</span>
          </button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/merchant/shop')">店铺信息</el-dropdown-item>
            <el-dropdown-item @click.native="$router.push('/merchant/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </header>

    <div class="manager-main">
      <button v-if="menuVisible" class="manager-menu-scrim" type="button" aria-label="关闭商户菜单" @click="menuVisible = false"></button>
      <aside class="manager-main-left" :class="{ 'manager-main-left--open': menuVisible }">
        <el-menu :default-openeds="['operate']" router :default-active="$route.path">
          <el-menu-item index="/merchant/home"><i class="el-icon-s-home"></i><span slot="title">工作台</span></el-menu-item>
          <el-submenu index="operate">
            <template slot="title"><i class="el-icon-s-shop"></i><span>店铺经营</span></template>
            <el-menu-item index="/merchant/goods"><i class="el-icon-goods"></i><span>商品管理</span></el-menu-item>
            <el-menu-item index="/merchant/orders"><i class="el-icon-s-order"></i><span>订单管理</span></el-menu-item>
          </el-submenu>
          <el-submenu index="system">
            <template slot="title"><i class="el-icon-s-tools"></i><span>系统管理</span></template>
            <el-menu-item index="/merchant/shop"><i class="el-icon-office-building"></i><span slot="title">店铺信息</span></el-menu-item>
            <el-menu-item index="/merchant/person"><i class="el-icon-s-custom"></i><span slot="title">个人信息</span></el-menu-item>
          </el-submenu>
        </el-menu>
      </aside>

      <main class="manager-main-right"><router-view @update:user="updateUser" /></main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MerchantView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      defaultAvatar: require('@/assets/logo.svg'),
      menuVisible: false
    }
  },
  computed: {
    shopState() { return this.user.state || '' }
  },
  watch: {
    '$route'() { this.menuVisible = false }
  },
  mounted() {
    if (!this.user.id || this.user.role !== 'MERCHANT') this.$router.push('/login')
  },
  methods: {
    updateUser(user) { this.user = JSON.parse(JSON.stringify(user)) },
    logout() { localStorage.removeItem('user'); this.$router.push('/login') }
  }
}
</script>

<style>
@import "@/assets/css/manager.css";
</style>
