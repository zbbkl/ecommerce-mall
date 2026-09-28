<template>
  <div class="front-layout">
    <a class="skip-link" href="#main-content">跳到主要内容</a>

    <header class="header">
      <div class="front-header">
        <router-link class="front-header-left" to="/front/home" aria-label="捞宝购物首页">
          <img src="@/assets/logo.svg" alt="捞宝购物手绘猫标志" />
          <span class="title">捞宝购物</span>
        </router-link>

        <nav class="front-header-center" aria-label="主导航">
          <router-link
            v-for="item in menuList"
            :key="item.text"
            class="menu-item"
            :class="{'menu-item-active': isActive(item)}"
            :to="item.path"
          >
            {{ item.text }}
            <span v-if="item.badge && badges[item.badge]" class="menu-badge-num">{{ badges[item.badge] }}</span>
          </router-link>
        </nav>

        <div class="front-header-right">
          <div v-if="!user.username" class="front-header-right-button">
            <el-button type="primary" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </div>

          <el-dropdown v-else trigger="click" placement="bottom-end">
            <button class="profile-trigger" type="button" aria-label="打开用户菜单">
              <img :src="user.avatar || defaultAvatar" alt="用户头像" />
              <span>{{ user.name || '个人中心' }}</span>
              <i class="el-icon-arrow-down" aria-hidden="true"></i>
            </button>

            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>
                <span class="dropdown-user">{{ user.name || '已登录用户' }}</span>
              </el-dropdown-item>
              <el-dropdown-item>
                <router-link class="dropdown-link" to="/front/person">个人信息</router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <router-link class="dropdown-link" to="/front/cart">
                  我的购物车
                  <span v-if="badges.cart" class="menu-badge-num">{{ badges.cart }}</span>
                </router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <router-link class="dropdown-link" to="/front/collect">我的收藏</router-link>
              </el-dropdown-item>
              <el-dropdown-item>
                <router-link class="dropdown-link" to="/front/orders">
                  历史订单
                  <span v-if="badges.pending" class="menu-badge-num">{{ badges.pending }}</span>
                </router-link>
              </el-dropdown-item>
              <el-dropdown-item divided>
                <button class="dropdown-action" type="button" @click="logout">退出登录</button>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main id="main-content" class="main-body">
      <router-view ref="child" @update:user="updateUser" @update:cart="loadBadges" />
    </main>

    <Footer />

    <button class="back-to-top" v-show="showTop" type="button" aria-label="回到顶部" @click="backTop">
      <svg class="svgIcon" viewBox="0 0 384 512" aria-hidden="true">
        <path d="M214.6 41.4c-12.5-12.5-32.8-12.5-45.3 0l-160 160c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L160 141.2V448c0 17.7 14.3 32 32 32s32-14.3 32-32V141.2L329.4 246.6c12.5 12.5 32 32 45.3 0s12.5-32.8 0-45.3l-160-160z"></path>
      </svg>
    </button>
  </div>
</template>

<script>
import Footer from '@/conponents/Footer.vue'
import cart from '@/utils/cart'

export default {
  name: 'FrontLayout',
  components: { Footer },
  data() {
    return {
      user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : {},
      defaultAvatar: require('@/assets/logo.svg'),
      menuList: [
        { text: '首页', path: '/front/home' },
        { text: '全部商品', path: '/front/goods' },
        { text: '购物车', path: '/front/cart', badge: 'cart' },
        { text: '历史订单', path: '/front/orders', badge: 'pending' },
        { text: '个人中心', path: '/front/person' }
      ],
      badges: { cart: 0, pending: 0 },
      showTop: false
    }
  },
  created() {
    this.loadBadges()
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll, { passive: true })
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.onScroll)
  },
  watch: {
    '$route'() {
      this.loadBadges()
    }
  },
  methods: {
    onScroll() {
      this.showTop = window.pageYOffset > 300
    },
    backTop() {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    isActive(item) {
      if (this.$route.path === item.path) return true
      return item.path === '/front/goods' && this.$route.path === '/front/goodsDetail'
    },
    loadBadges() {
      if (!this.user.id) return
      this.badges.cart = cart.count(this.user.id)
      this.$request.get('/orders/count', { params: { state: '待付款' } }).then(res => {
        this.badges.pending = res.code === '200' ? (res.data || 0) : 0
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('user') || '{}')
      this.loadBadges()
    },
    logout() {
      localStorage.removeItem('user')
      this.user = {}
      this.badges = { cart: 0, pending: 0 }
      this.$router.push('/front/home')
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/front.css";

.skip-link {
  position: fixed;
  top: var(--space-2);
  left: var(--space-2);
  z-index: var(--z-toast);
  padding: var(--space-2) var(--space-4);
  background: var(--c-ink);
  border-radius: var(--radius-8);
  color: var(--c-surface);
  transform: translateY(-160%);
  transition: transform var(--duration-state) var(--ease-out);
}

.skip-link:focus {
  color: var(--c-surface);
  transform: translateY(0);
}

.main-body {
  min-height: 60vh;
}

.profile-trigger {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  gap: var(--space-2);
  padding: 3px var(--space-2) 3px 3px;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-full);
  color: var(--c-ink-body);
  cursor: pointer;
}

.profile-trigger:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.profile-trigger img {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.profile-trigger span {
  max-width: 96px;
  overflow: hidden;
  font-size: var(--text-caption-size);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-user {
  color: var(--c-ink);
  font-weight: 600;
}

.dropdown-link,
.dropdown-action {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  color: var(--c-ink-body);
  font-size: var(--text-caption-size);
  text-align: left;
}

.dropdown-link:hover,
.dropdown-action:hover {
  color: var(--c-brand);
}

.dropdown-action {
  padding: 0;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.menu-badge-num {
  display: inline-flex;
  min-width: 18px;
  height: 18px;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  margin-left: var(--space-1);
  background-color: var(--c-accent);
  border-radius: var(--radius-full);
  color: var(--c-surface);
  font-size: var(--text-micro-size);
  line-height: 1;
}
</style>
