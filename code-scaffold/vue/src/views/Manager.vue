<template>
  <div class="manager-container admin-container">
    <header class="manager-header">
      <div class="manager-header-left clickable" @click="$router.push('/home')">
        <button class="mobile-menu-toggle" type="button" :aria-expanded="String(menuVisible)" aria-label="打开管理菜单" @click.stop="menuVisible = true"><i class="el-icon-s-unfold" aria-hidden="true"></i></button>
        <img src="@/assets/logo.svg" alt="捞宝购物标志" />
        <div class="title">后台管理系统</div>
      </div>

      <div class="manager-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="manager-header-right">
        <el-dropdown placement="bottom">
          <button class="avatar" type="button" aria-label="打开管理员菜单">
            <img :src="user.avatar || defaultAvatar" alt="管理员头像" />
            <span>{{ user.name || '管理员' }}</span>
          </button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </header>

    <div class="manager-main">
      <button v-if="menuVisible" class="manager-menu-scrim" type="button" aria-label="关闭管理菜单" @click="menuVisible = false"></button>
      <aside class="manager-main-left" :class="{ 'manager-main-left--open': menuVisible }">
        <el-menu :default-openeds="['info', 'user', 'system']" router :default-active="$route.path">
          <el-menu-item index="/home"><i class="el-icon-s-home"></i><span slot="title">首页</span></el-menu-item>
          <el-submenu index="user" v-if="user.role == 'ADMIN'">
            <template slot="title"><i class="el-icon-s-custom"></i><span>用户管理</span></template>
            <el-menu-item index="/admin"><i class="el-icon-user-solid"></i><span>管理员信息</span></el-menu-item>
            <el-menu-item index="/user"><i class="el-icon-user"></i><span>用户信息</span></el-menu-item>
          </el-submenu>
          <el-submenu index="info" v-if="user.role == 'ADMIN'">
            <template slot="title"><i class="el-icon-s-data"></i><span>信息管理</span></template>
            <el-menu-item index="/type"><i class="el-icon-menu"></i><span>商品分类信息</span></el-menu-item>
            <el-menu-item index="/goods"><i class="el-icon-menu"></i><span>商品信息</span></el-menu-item>
            <el-menu-item index="/orders"><i class="el-icon-menu"></i><span>订单信息</span></el-menu-item>
            <el-menu-item index="/carousel"><i class="el-icon-menu"></i><span>轮播图信息</span></el-menu-item>
            <el-menu-item index="/collect"><i class="el-icon-menu"></i><span>收藏信息</span></el-menu-item>
          </el-submenu>
          <el-submenu index="merchant" v-if="user.role == 'ADMIN'">
            <template slot="title"><i class="el-icon-s-shop"></i><span>商户管理</span></template>
            <el-menu-item index="/merchants"><i class="el-icon-s-check"></i><span>入驻审核</span></el-menu-item>
          </el-submenu>
          <el-submenu index="system">
            <template slot="title"><i class="el-icon-s-tools"></i><span>系统管理</span></template>
            <el-menu-item index="/person"><i class="el-icon-s-custom"></i><span slot="title">个人信息</span></el-menu-item>
          </el-submenu>
        </el-menu>
      </aside>

      <main class="manager-main-right"><router-view @update:user="updateUser" /></main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ManagerView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      defaultAvatar: require('@/assets/logo.svg'),
      menuVisible: false
    }
  },
  watch: {
    '$route'() { this.menuVisible = false }
  },
  mounted() {
    if (!this.user.id) this.$router.push('/login')
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
