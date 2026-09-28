<template>
  <div class="detail-page content-shell">
    <router-link class="back-link" to="/front/goods">
      <i class="el-icon-arrow-left" aria-hidden="true"></i>
      返回全部商品
    </router-link>

    <div v-if="goods.id" class="detail-layout">
      <article class="detail-card">
        <div class="detail-media">
          <span class="image-tray">
            <el-image
              class="detail-image"
              :src="goods.cover"
              :alt="goods.name || '商品图片'"
              fit="cover"
              :preview-src-list="[goods.cover]"
            />
          </span>
        </div>

        <div class="detail-info">
          <div class="detail-heading">
            <span class="category-pill">{{ displayCategory }}</span>
            <h1>{{ goods.name || '未命名商品' }}</h1>
          </div>

          <router-link
            v-if="goods.merchantId"
            class="shop-link"
            :to="{ path: '/front/shop', query: { id: goods.merchantId } }"
          >
            <i class="el-icon-shop" aria-hidden="true"></i>
            {{ goods.merchantName || '平台自营' }}
          </router-link>

          <p class="detail-description">{{ goods.descr }}</p>

          <div class="price-panel">
            <div class="current-price">
              <span>¥</span>
              <strong>{{ formattedPrice }}</strong>
            </div>
            <div class="stock-summary">
              <span>库存 <b class="tabular-nums">{{ availableStock }}</b></span>
              <span>累计热销 <b class="tabular-nums">{{ salesCount }}</b></span>
            </div>
          </div>

          <dl class="recipe-label">
            <div>
              <dt>分类</dt>
              <dd>{{ displayCategory }}</dd>
            </div>
            <div>
              <dt>库存</dt>
              <dd class="tabular-nums">{{ availableStock }}</dd>
            </div>
            <div>
              <dt>已售</dt>
              <dd class="tabular-nums">{{ salesCount }}</dd>
            </div>
            <div>
              <dt>上架</dt>
              <dd>{{ goods.date || '未标注' }}</dd>
            </div>
          </dl>

          <div class="action-row">
            <el-input-number
              v-model="num"
              :min="1"
              :max="purchaseLimit"
              :disabled="!inStock"
              label="购买数量"
            />
            <button class="buy-button" type="button" :disabled="!inStock" @click="buy">
              立即购买
            </button>
            <button class="cart-button" type="button" :disabled="!inStock" @click="addCart">
              <i class="el-icon-shopping-cart-2" aria-hidden="true"></i>
              {{ added ? '已加入' : '加入购物车' }}
            </button>
            <button class="collect-button" type="button" @click="collect">
              <i :class="isCollect ? 'el-icon-star-on' : 'el-icon-star-off'" aria-hidden="true"></i>
              {{ isCollect ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </article>

      <section class="detail-tabs" aria-label="商品详细信息">
        <el-tabs v-model="activeName">
          <el-tab-pane label="详细介绍" name="goods">
            <div class="rich-content w-e-text" v-html="sanitizeHtml(goods.content)"></div>
          </el-tab-pane>
          <el-tab-pane label="购买须知" name="notice">
            <div class="notice-content">
              <h2>购买说明</h2>
              <ul>
                <li>商品信息以页面展示和订单结算结果为准。</li>
                <li>支持 7 天无理由退货，具体以平台规则为准。</li>
                <li>订单发货后可在历史订单中查看状态。</li>
                <li>如有售后问题，请先联系所属店铺。</li>
              </ul>
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>

      <div class="mobile-purchase-bar">
        <div>
          <span>合计</span>
          <strong>¥{{ formattedPrice }}</strong>
        </div>
        <button type="button" :disabled="!inStock" @click="addCart">加入购物车</button>
        <button type="button" :disabled="!inStock" @click="buy">立即购买</button>
      </div>
    </div>

    <el-empty v-else description="没有找到该商品" />
  </div>
</template>

<script>
import cartMixin from '@/mixins/cartMixin'
import { sanitizeHtml } from '@/utils/sanitize'

export default {
  name: 'GoodsDetail',
  mixins: [cartMixin],
  data() {
    return {
      id: this.$route.query.id,
      goods: {},
      types: [],
      num: 1,
      activeName: 'goods',
      user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : {},
      isCollect: false,
      added: false
    }
  },
  computed: {
    availableStock() {
      const value = Number(this.goods.store)
      return Number.isFinite(value) ? Math.max(0, value) : 0
    },
    salesCount() {
      const value = Number(this.goods.sales)
      return Number.isFinite(value) ? Math.max(0, value) : 0
    },
    purchaseLimit() {
      return Math.max(1, Math.min(this.availableStock || 1, 10))
    },
    inStock() {
      return this.availableStock > 0
    },
    formattedPrice() {
      const value = Number(this.goods.price)
      return Number.isFinite(value) ? value.toFixed(2) : '0.00'
    },
    displayCategory() {
      if (this.goods.typeName) return this.goods.typeName
      const type = this.types.find(item => item.id === this.goods.typeId)
      return type ? type.name : '宠物用品'
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  methods: {
    sanitizeHtml,
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadGoods() {
      this.$request.get('/goods/selectById?id=' + this.id).then(res => {
        this.goods = res.data || {}
        this.isCollect = Boolean(this.goods.isCollect)
      })
    },
    collect() {
      if (!this.user.id) {
        this.$message.warning('请先登录后收藏')
        this.$router.push('/login')
        return
      }
      this.$request.post('/collect/add', { goodsId: this.goods.id }).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '成功', message: '收藏成功', showClose: false, duration: 2000 })
          this.isCollect = true
        } else {
          this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          this.isCollect = false
        }
      })
    },
    addCart() {
      if (!this.goods.id) return
      const success = this.addToCart(this.goods, this.num)
      if (success) {
        this.added = true
        setTimeout(() => { this.added = false }, 1500)
      }
    },
    buy() {
      if (!this.user.id) {
        this.$notify.error({ title: '错误', message: '请先登录', showClose: false, duration: 2000 })
        this.$router.push('/login')
        return
      }
      if (!this.inStock) return
      const items = [{ goodsId: this.goods.id, nums: this.num }]
      this.$request.post('/orders/settle', items).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '成功', message: '下单成功，请尽快支付', showClose: false, duration: 2000 })
          this.$router.push('/front/orders?state=待付款')
        } else {
          this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
        }
      })
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 80vh;
  padding-top: var(--space-6);
  padding-bottom: var(--space-16);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.back-link:hover {
  color: var(--c-brand);
}

.detail-card {
  display: grid;
  grid-template-columns: minmax(340px, .9fr) minmax(0, 1.1fr);
  gap: var(--space-8);
  margin-top: var(--space-4);
  padding: var(--space-6);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
}

.detail-media,
.detail-info {
  min-width: 0;
}

.image-tray {
  display: block;
  height: 100%;
  min-height: 420px;
  padding: var(--space-4);
  background: var(--c-surface-sunken);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-12);
}

.detail-image {
  width: 100%;
  height: 100%;
  min-height: 388px;
  border-radius: var(--radius-8);
  outline: 1px solid rgba(20, 21, 15, .08);
  outline-offset: -1px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.detail-heading {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  gap: var(--space-3);
}

.category-pill {
  padding: var(--space-1) var(--space-3);
  background: var(--c-brand-soft);
  border-radius: var(--radius-full);
  color: var(--c-brand);
  font-size: var(--text-micro-size);
  font-weight: 600;
}

.detail-heading h1 {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
  line-height: var(--text-display-leading);
}

.shop-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-3);
  color: var(--c-brand);
  font-size: var(--text-caption-size);
}

.detail-description {
  margin-top: var(--space-4);
  color: var(--c-ink-muted);
  font-size: var(--text-body-size);
  line-height: var(--text-body-leading);
}

.price-panel {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-4);
  margin-top: var(--space-6);
  padding: var(--space-4);
  background: var(--c-surface-sunken);
  border-radius: var(--radius-12);
}

.current-price {
  display: flex;
  align-items: baseline;
  color: var(--c-accent);
  font-variant-numeric: tabular-nums;
}

.current-price span {
  margin-right: 2px;
  font-size: var(--text-title-size);
  font-weight: 600;
}

.current-price strong {
  font-size: var(--text-price-lg-size);
  font-weight: var(--text-price-lg-weight);
  line-height: var(--text-price-lg-leading);
}

.stock-summary {
  display: flex;
  gap: var(--space-4);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.stock-summary b {
  color: var(--c-ink);
}

.recipe-label {
  margin-top: var(--space-5);
  border-top: 1px solid var(--c-line);
}

.recipe-label > div {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--c-line);
}

.recipe-label dt,
.recipe-label dd {
  min-width: 0;
  margin: 0;
}

.recipe-label dt {
  color: var(--c-ink-subtle);
  font-size: var(--text-caption-size);
}

.recipe-label dd {
  color: var(--c-ink-body);
  font-size: var(--text-body-size);
}

.action-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-6);
}

.buy-button,
.cart-button,
.collect-button {
  min-height: 40px;
  padding: 0 var(--space-5);
  border-radius: var(--radius-8);
  cursor: pointer;
  font-size: var(--text-body-size);
  font-weight: 600;
  transition: background-color var(--duration-state) var(--ease-out), border-color var(--duration-state) var(--ease-out), color var(--duration-state) var(--ease-out), transform var(--duration-state) var(--ease-out);
}

.buy-button {
  background: var(--c-brand);
  border: 1px solid var(--c-brand);
  color: var(--c-surface);
}

.buy-button:hover:not(:disabled),
.cart-button:hover:not(:disabled) {
  background: var(--c-brand-hover);
  border-color: var(--c-brand-hover);
  color: var(--c-surface);
}

.cart-button {
  background: var(--c-brand-soft);
  border: 1px solid var(--c-brand-soft);
  color: var(--c-brand);
}

.collect-button {
  background: var(--c-surface);
  border: 1px solid var(--c-line-strong);
  color: var(--c-ink-body);
}

.collect-button:hover {
  border-color: var(--c-accent);
  color: var(--c-accent);
}

.action-row button:active:not(:disabled) {
  transform: scale(.96);
}

.action-row button:disabled {
  background: var(--c-surface-sunken);
  border-color: var(--c-line);
  color: var(--c-ink-subtle);
  cursor: not-allowed;
}

.detail-tabs {
  margin-top: var(--space-6);
  padding: var(--space-6);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
}

.rich-content {
  color: var(--c-ink-body);
  font-size: var(--text-body-size);
  line-height: var(--text-body-leading);
}

.notice-content h2 {
  font-size: var(--text-title-lg-size);
}

.notice-content ul {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding-left: 1.25em;
  color: var(--c-ink-body);
  font-size: var(--text-body-size);
}

.mobile-purchase-bar {
  display: none;
}

@media (max-width: 900px) {
  .detail-card {
    grid-template-columns: 1fr;
  }

  .image-tray {
    min-height: 320px;
  }

  .detail-image {
    min-height: 288px;
  }
}

@media (max-width: 640px) {
  .detail-page {
    padding-bottom: 96px;
  }

  .detail-card {
    gap: var(--space-5);
    padding: var(--space-4);
  }

  .image-tray {
    min-height: 270px;
    padding: var(--space-3);
  }

  .detail-image {
    min-height: 244px;
  }

  .price-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .action-row {
    display: none;
  }

  .mobile-purchase-bar {
    position: fixed;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: var(--z-sticky);
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto auto;
    gap: var(--space-2);
    align-items: center;
    padding: var(--space-3) var(--space-4);
    background: var(--c-surface);
    border-top: 1px solid var(--c-line);
    box-shadow: var(--shadow-e3);
  }

  .mobile-purchase-bar > div {
    display: flex;
    align-items: baseline;
    flex-direction: column;
    color: var(--c-ink-muted);
    font-size: var(--text-micro-size);
  }

  .mobile-purchase-bar strong {
    color: var(--c-accent);
    font-size: var(--text-price-size);
    font-variant-numeric: tabular-nums;
  }

  .mobile-purchase-bar button {
    min-height: 40px;
    padding: 0 var(--space-3);
    border-radius: var(--radius-8);
    font-size: var(--text-caption-size);
    font-weight: 600;
  }

  .mobile-purchase-bar button:first-of-type {
    background: var(--c-brand-soft);
    border: 1px solid var(--c-brand-soft);
    color: var(--c-brand);
  }

  .mobile-purchase-bar button:last-of-type {
    background: var(--c-brand);
    border: 1px solid var(--c-brand);
    color: var(--c-surface);
  }
}
</style>
