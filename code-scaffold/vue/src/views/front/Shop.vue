<template>
  <div class="shop-page content-shell">
    <header class="shop-header">
      <div class="shop-identity">
        <img :src="shop.logo || defaultLogo" :alt="`${shop.shopName || '店铺'}标志`" />
        <div><h1>{{ shop.shopName || '店铺主页' }}</h1><p>{{ shop.descr || '这家店还没有填写简介。' }}</p></div>
      </div>
      <span v-if="shop.state" class="shop-state">{{ shop.state }}</span>
    </header>
    <section class="shop-products" aria-labelledby="shop-goods-heading">
      <div class="section-heading"><h2 id="shop-goods-heading">店内商品</h2><p>共 {{ total }} 件已上架商品。</p></div>
      <div v-if="goods.length" class="product-grid">
        <ProductCard v-for="item in goods" :key="item.id" :goods="item" :category-name="item.typeName" @add="addToCart" />
      </div>
      <el-empty v-else description="店铺暂时没有上架商品" />
      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination background layout="prev, pager, next" :current-page="pageNum" :page-size="pageSize" :total="total" @current-change="handleCurrentChange" />
      </div>
    </section>
  </div>
</template>

<script>
import ProductCard from '@/conponents/ProductCard.vue'
import cartMixin from '@/mixins/cartMixin'

export default {
  name: 'FrontShop',
  components: { ProductCard },
  mixins: [cartMixin],
  data() {
    return { shop: {}, goods: [], pageNum: 1, pageSize: 12, total: 0, defaultLogo: require('@/assets/logo.svg') }
  },
  created() {
    this.merchantId = this.$route.query.id
    this.loadShop()
    this.loadGoods()
  },
  methods: {
    loadShop() {
      this.$request.get('/merchantShop/info', { params: { merchantId: this.merchantId } }).then(res => {
        if (res.code === '200') this.shop = res.data || {}
        else this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
      })
    },
    loadGoods() {
      this.$request.get('/merchantShop/goods', { params: { merchantId: this.merchantId, pageNum: this.pageNum, pageSize: this.pageSize } }).then(res => {
        this.goods = Array.isArray(res.data?.records) ? res.data.records : []
        this.total = res.data?.total || 0
      })
    },
    handleCurrentChange(pageNum) { this.pageNum = pageNum; this.loadGoods() }
  }
}
</script>

<style scoped>
.shop-page { min-height: 75vh; padding-top: var(--space-8); padding-bottom: var(--space-16); }
.shop-header { display: flex; align-items: center; justify-content: space-between; gap: var(--space-6); padding: var(--space-6); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); }
.shop-identity { display: flex; min-width: 0; align-items: center; gap: var(--space-5); }
.shop-identity img { width: 80px; height: 80px; flex: 0 0 80px; padding: var(--space-2); background: var(--c-surface-sunken); border: 1px solid var(--c-line); border-radius: var(--radius-12); object-fit: contain; }
.shop-identity h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }
.shop-identity p { max-width: 46em; margin-top: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-body-size); }
.shop-state { flex: 0 0 auto; padding: var(--space-1) var(--space-3); background: var(--c-brand-soft); border-radius: var(--radius-full); color: var(--c-success); font-size: var(--text-caption-size); font-weight: 600; }
.shop-products { margin-top: var(--space-10); }
.section-heading { margin-bottom: var(--space-6); }
.section-heading h2 { font-size: var(--text-title-lg-size); font-weight: var(--text-title-lg-weight); }
.section-heading p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: var(--space-5); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-8); }
@media (max-width: 640px) { .shop-header, .shop-identity { align-items: flex-start; flex-direction: column; } .product-grid { grid-template-columns: 1fr; } }
</style>
