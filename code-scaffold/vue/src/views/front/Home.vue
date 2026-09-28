<template>
  <div class="home-page">
    <section class="home-hero content-shell">
      <div class="hero-copy">
        <h1>给毛孩子挑一口放心的好粮</h1>
        <p>从主粮、护理到玩具和零食，商品信息清楚写，价格和库存直接看。</p>
        <div class="hero-actions">
          <router-link class="hero-primary" to="/front/goods">开始选购</router-link>
          <router-link class="hero-link" to="/front/cart">查看购物车</router-link>
        </div>
      </div>

      <ProductCard
        v-if="featuredGoods"
        class="hero-featured"
        :goods="featuredGoods"
        :category-name="categoryName(featuredGoods)"
        featured
        @add="addToCart"
      />
      <div v-else class="hero-placeholder">
        <span class="hero-placeholder-mark" aria-hidden="true">粮</span>
        <h2>今日推荐正在准备</h2>
        <p>先去全部商品页，按分类找需要的用品。</p>
        <router-link class="hero-primary" to="/front/goods">浏览商品</router-link>
      </div>
    </section>

    <section v-if="types.length" class="category-section content-shell" aria-labelledby="category-heading">
      <div class="section-heading">
        <div>
          <h2 id="category-heading">按需要逛</h2>
          <p>分类保持简单，想买什么直接进入对应商品。</p>
        </div>
      </div>
      <div class="category-grid">
        <button
          v-for="item in types"
          :key="item.id"
          class="category-card"
          type="button"
          @click="goCategory(item)"
        >
          <span class="category-icon" aria-hidden="true">{{ categoryGlyph(item.name) }}</span>
          <span class="category-name">{{ item.name }}</span>
          <span class="category-action">查看商品</span>
        </button>
      </div>
    </section>

    <section v-if="carousels.length" class="editorial-section content-shell" aria-labelledby="editorial-heading">
      <div class="section-heading">
        <div>
          <h2 id="editorial-heading">编辑推荐</h2>
          <p>从当前上架商品里挑出的今日橱窗。</p>
        </div>
      </div>
      <div class="editorial-rail" role="list">
        <router-link
          v-for="(item, index) in carousels"
          :key="item.id"
          class="editorial-card"
          role="listitem"
          :to="{ path: '/front/goodsDetail', query: { id: item.goodsId } }"
          :aria-label="`查看编辑推荐商品 ${index + 1}`"
        >
          <span class="editorial-media">
            <img :src="item.cover" :alt="`编辑推荐商品 ${index + 1}`" loading="lazy" />
          </span>
          <span class="editorial-label">编辑推荐</span>
        </router-link>
      </div>
    </section>

    <section class="content-section content-shell" aria-labelledby="new-heading">
      <div class="section-heading">
        <div>
          <h2 id="new-heading">新品上架</h2>
          <p>按上架时间整理，横向浏览最近的新商品。</p>
        </div>
        <router-link class="section-link" to="/front/goods">查看全部商品</router-link>
      </div>

      <div v-if="timeGoods.length" class="product-rail">
        <ProductCard
          v-for="item in timeGoods"
          :key="item.id"
          class="rail-card"
          :goods="item"
          :category-name="categoryName(item)"
          @add="addToCart"
        />
      </div>
      <div v-else class="inline-empty">
        <h3>暂时没有新品</h3>
        <p>新商品上架后会显示在这里。</p>
      </div>
    </section>

    <section class="content-section content-shell" aria-labelledby="sales-heading">
      <div class="section-heading">
        <div>
          <h2 id="sales-heading">热销商品</h2>
          <p>按当前累计销量排序，查看真实热度。</p>
        </div>
        <router-link class="section-link" to="/front/goods">查看全部商品</router-link>
      </div>

      <div v-if="salesGoods.length" class="ranking-grid">
        <ProductCard
          v-for="(item, index) in salesGoods"
          :key="item.id"
          :goods="item"
          :category-name="categoryName(item)"
          :rank="index + 1"
          @add="addToCart"
        />
      </div>
      <div v-else class="inline-empty">
        <h3>暂时没有热销数据</h3>
        <p>产生销量后会按真实订单更新榜单。</p>
      </div>
    </section>
  </div>
</template>

<script>
import ProductCard from '@/conponents/ProductCard.vue'
import cartMixin from '@/mixins/cartMixin'

export default {
  name: 'Home',
  components: {
    ProductCard
  },
  mixins: [cartMixin],
  data() {
    return {
      carousels: [],
      types: [],
      timeGoods: [],
      salesGoods: []
    }
  },
  computed: {
    featuredGoods() {
      return this.timeGoods.length ? this.timeGoods[0] : null
    }
  },
  created() {
    this.loadType()
    this.loadCarousel()
    this.loadTimeGoods()
    this.loadSaleGoods()
  },
  methods: {
    loadCarousel() {
      this.$request.get('/carousel/selectAll').then(res => {
        this.carousels = Array.isArray(res.data) ? res.data : []
      })
    },
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadTimeGoods() {
      this.$request.get('/goods/times').then(res => {
        this.timeGoods = Array.isArray(res.data) ? res.data : []
      })
    },
    loadSaleGoods() {
      this.$request.get('/goods/sales').then(res => {
        this.salesGoods = Array.isArray(res.data) ? res.data : []
      })
    },
    categoryName(goods) {
      if (!goods) return ''
      if (goods.typeName) return goods.typeName
      const type = this.types.find(item => item.id === goods.typeId)
      return type ? type.name : ''
    },
    categoryGlyph(name) {
      const value = name || ''
      const glyphMap = {
        '宠物主粮': '粮',
        '宠物护理': '护',
        '宠物玩具': '玩',
        '宠物零食': '零'
      }
      return glyphMap[value] || value.slice(0, 1) || '宠'
    },
    goCategory(type) {
      this.$router.push({
        path: '/front/goods',
        query: { selectedCategoryId: type.id }
      })
    },
  }
}
</script>

<style scoped>
.home-page {
  padding: var(--space-8) 0 var(--space-16);
}

.home-hero {
  display: grid;
  min-height: 440px;
  grid-template-columns: minmax(0, .9fr) minmax(440px, 1.1fr);
  gap: var(--space-12);
  align-items: center;
  padding-top: var(--space-8);
  padding-bottom: var(--space-16);
}

.hero-copy h1 {
  max-width: 12ch;
  color: var(--c-ink);
  font-size: var(--text-display-lg-size);
  font-weight: var(--text-display-lg-weight);
  letter-spacing: var(--text-display-lg-tracking);
  line-height: var(--text-display-lg-leading);
}

.hero-copy p {
  max-width: 34em;
  margin-top: var(--space-4);
  color: var(--c-ink-muted);
  font-size: var(--text-body-size);
  line-height: var(--text-body-leading);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-8);
}

.hero-primary,
.hero-link {
  display: inline-flex;
  min-height: 44px;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-5);
  border-radius: var(--radius-8);
  font-size: var(--text-body-size);
  font-weight: 600;
}

.hero-primary {
  background: var(--c-brand);
  color: var(--c-surface);
}

.hero-primary:hover {
  background: var(--c-brand-hover);
  color: var(--c-surface);
}

.hero-link {
  border: 1px solid var(--c-line-strong);
  color: var(--c-ink-body);
}

.hero-link:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.hero-placeholder {
  display: grid;
  min-height: 380px;
  place-items: center;
  align-content: center;
  padding: var(--space-10);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
  text-align: center;
}

.hero-placeholder-mark {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  background: var(--c-brand-soft);
  border-radius: var(--radius-16);
  color: var(--c-brand);
  font-size: var(--text-display-size);
  font-weight: 600;
}

.hero-placeholder h2 {
  margin-top: var(--space-5);
}

.hero-placeholder p {
  margin: var(--space-2) 0 var(--space-6);
  color: var(--c-ink-muted);
}

.category-section,
.editorial-section,
.content-section {
  margin-top: var(--space-16);
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.section-heading h2 {
  font-size: var(--text-title-lg-size);
  font-weight: var(--text-title-lg-weight);
  letter-spacing: var(--text-title-lg-tracking);
  line-height: var(--text-title-lg-leading);
}

.section-heading p {
  margin-top: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.section-link {
  flex: 0 0 auto;
  color: var(--c-brand);
  font-size: var(--text-body-size);
  font-weight: 500;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.category-card {
  display: grid;
  min-height: 132px;
  grid-template-columns: 48px minmax(0, 1fr);
  grid-template-rows: auto auto;
  gap: var(--space-1) var(--space-4);
  align-items: center;
  padding: var(--space-4);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
  color: var(--c-ink-body);
  cursor: pointer;
  text-align: left;
  transition: border-color var(--duration-state) var(--ease-out), box-shadow var(--duration-control) var(--ease-out);
}

.category-card:hover {
  border-color: var(--c-brand);
  box-shadow: var(--shadow-e2);
}

.category-icon {
  display: grid;
  width: 48px;
  height: 48px;
  grid-row: span 2;
  place-items: center;
  background: var(--c-brand-soft);
  border-radius: var(--radius-12);
  color: var(--c-brand);
  font-size: var(--text-title-size);
  font-weight: 600;
}

.category-name {
  align-self: end;
  color: var(--c-ink);
  font-size: var(--text-title-size);
  font-weight: 600;
}

.category-action {
  align-self: start;
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.editorial-rail,
.product-rail {
  display: flex;
  gap: var(--space-4);
  overflow-x: auto;
  overscroll-behavior-inline: contain;
  padding-bottom: var(--space-3);
  scroll-snap-type: x mandatory;
  scrollbar-width: thin;
}

.editorial-card {
  position: relative;
  display: block;
  min-width: 280px;
  flex: 0 0 min(360px, 78vw);
  scroll-snap-align: start;
}

.editorial-media {
  display: block;
  overflow: hidden;
  height: 220px;
  background: var(--c-surface-sunken);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
}

.editorial-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--duration-control) var(--ease-out);
}

.editorial-card:hover .editorial-media img {
  transform: scale(1.02);
}

.editorial-label {
  position: absolute;
  bottom: var(--space-3);
  left: var(--space-3);
  padding: var(--space-1) var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-full);
  color: var(--c-ink);
  font-size: var(--text-micro-size);
  font-weight: var(--text-micro-weight);
}

.rail-card {
  min-width: 280px;
  flex: 0 0 280px;
  scroll-snap-align: start;
}

.ranking-grid {
  display: grid;
  gap: var(--space-4);
}

.inline-empty {
  padding: var(--space-10);
  background: var(--c-surface);
  border: 1px dashed var(--c-line-strong);
  border-radius: var(--radius-16);
  color: var(--c-ink-muted);
  text-align: center;
}

.inline-empty h3 {
  color: var(--c-ink-body);
  font-size: var(--text-title-size);
}

.inline-empty p {
  margin-top: var(--space-1);
  font-size: var(--text-caption-size);
}

@media (max-width: 1024px) {
  .home-hero {
    grid-template-columns: 1fr;
  }

  .hero-copy h1 {
    max-width: 18ch;
  }

  .category-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home-page {
    padding-top: var(--space-4);
  }

  .home-hero {
    gap: var(--space-8);
    padding-bottom: var(--space-10);
  }

  .category-grid {
    grid-template-columns: 1fr;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .editorial-media {
    height: 190px;
  }
}
</style>
