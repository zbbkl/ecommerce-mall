<template>
  <div class="manager-container">
    <div class="manager-header">
      <div class="manager-header-left clickable" @click="$router.push('/home')">
        <img src="@/assets/logo.svg" />
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
          <div class="avatar">
            <img :src="user.avatar || defaultAvatar" />
            <div style="cursor: pointer;font-weight: bold">{{ user.name ||  '管理员' }}<!--<i class="el-icon-arrow-down" style="margin-left: 5px"></i>--></div>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="$router.push('/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <div class="manager-main">
      <div class="manager-main-left">
        <el-menu :default-openeds="['info', 'user', 'system']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-submenu index="user" v-if="user.role == 'ADMIN'">
            <template slot="title">
              <i class="el-icon-s-custom"></i>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin"><i class="el-icon-user-solid"></i><span>管理员信息</span></el-menu-item>
            <el-menu-item index="/user"><i class="el-icon-user"></i><span>用户信息</span></el-menu-item>
          </el-submenu>
          <el-submenu index="info" v-if="user.role == 'ADMIN'">
            <template slot="title">
              <i class="el-icon-s-data"></i>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/type"><i class="el-icon-menu"></i><span>商品分类信息</span></el-menu-item>
            <el-menu-item index="/goods"><i class="el-icon-menu"></i><span>商品信息</span></el-menu-item>
            <el-menu-item index="/orders"><i class="el-icon-menu"></i><span>订单信息</span></el-menu-item>
            <el-menu-item index="/carousel"><i class="el-icon-menu"></i><span>轮播图信息</span></el-menu-item>
            <el-menu-item index="/collect"><i class="el-icon-menu"></i><span>收藏信息</span></el-menu-item>
          </el-submenu>
          <el-submenu index="merchant" v-if="user.role == 'ADMIN'">
            <template slot="title">
              <i class="el-icon-s-shop"></i>
              <span>商户管理</span>
            </template>
            <el-menu-item index="/merchants"><i class="el-icon-s-check"></i><span>入驻审核</span></el-menu-item>
          </el-submenu>
          <el-submenu index="system">
            <template slot="title">
              <i class="el-icon-s-tools"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/person"><i class="el-icon-s-custom"></i><span slot="title">个人信息</span></el-menu-item>
          </el-submenu>
        </el-menu>
      </div>

      <div class="manager-main-right">
        <router-view @update:user="updateUser" />
      </div>
    </div>
  </div>
</template>
<script>

export default {
  name: 'HomeView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      defaultAvatar: require('@/assets/logo.svg')
    }
  },
  mounted() {
    if (!this.user.id) {
      this.$router.push('/login')
    }
  },
  methods: {
    updateUser(user) {
      this.user = JSON.parse(JSON.stringify(user))
    },
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    },
  }
}
</script>

<style>
@import "@/assets/css/manager.css";

</style>
