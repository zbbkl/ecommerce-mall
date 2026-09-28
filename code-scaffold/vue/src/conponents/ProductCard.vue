<template>
  <article
    class="product-card"
    :class="{
      'product-card--featured': featured,
      'product-card--ranked': rank > 0,
      'product-card--out': !inStock
    }"
  >
    <div v-if="rank" class="product-rank" aria-label="热销排名">
      <span>销量榜</span>
      <strong>{{ String(rank).padStart(2, '0') }}</strong>
    </div>

    <router-link class="product-media" :to="detailRoute" :aria-label="`查看${goods.name || '商品'}详情`">
      <span class="image-tray">
        <img :src="goods.cover" :alt="goods.name || '商品图片'" :loading="featured ? 'eager' : 'lazy'" :fetchpriority="featured ? 'high' : 'auto'" />
      </span>
    </router-link>

    <div class="product-content">
      <div class="product-title-row">
        <h3 class="product-title">
          <router-link :to="detailRoute">{{ goods.name || '未命名商品' }}</router-link>
        </h3>
        <span v-if="!inStock" class="stock-flag">缺货</span>
      </div>

      <p v-if="goods.descr" class="product-description">{{ goods.descr }}</p>

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
      </dl>

      <div class="product-footer">
        <div class="product-price" aria-label="商品价格">
          <span>¥</span>
          <strong>{{ formattedPrice }}</strong>
        </div>
        <button
          class="add-button"
          type="button"
          :disabled="!inStock"
          :aria-label="inStock ? `将${goods.name || '商品'}加入购物车` : `${goods.name || '商品'}暂时缺货`"
          @click.stop="$emit('add', goods)"
        >
          <span aria-hidden="true">+</span>
          {{ inStock ? '加入购物车' : '暂时缺货' }}
        </button>
      </div>
    </div>
  </article>
</template>

<script>
export default {
  name: 'ProductCard',
  props: {
    goods: {
      type: Object,
      required: true
    },
    categoryName: {
      type: String,
      default: ''
    },
    featured: {
      type: Boolean,
      default: false
    },
    rank: {
      type: Number,
      default: 0
    }
  },
  computed: {
    detailRoute() {
      return {
        path: '/front/goodsDetail',
        query: { id: this.goods.id }
      }
    },
    availableStock() {
      const value = Number(this.goods.store)
      return Number.isFinite(value) ? Math.max(0, value) : 0
    },
    salesCount() {
      const value = Number(this.goods.sales)
      return Number.isFinite(value) ? Math.max(0, value) : 0
    },
    inStock() {
      return this.availableStock > 0
    },
    displayCategory() {
      return this.goods.typeName || this.categoryName || '宠物用品'
    },
    formattedPrice() {
      const value = Number(this.goods.price)
      return Number.isFinite(value) ? value.toFixed(2) : '0.00'
    }
  }
}
</script>

<style scoped>
.product-card {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
  transition: border-color var(--duration-state) var(--ease-out), box-shadow var(--duration-control) var(--ease-out);
}

.product-card:hover {
  border-color: var(--c-line-strong);
  box-shadow: var(--shadow-e2);
}

.product-card--featured {
  display: grid;
  min-height: 380px;
  grid-template-columns: minmax(280px, .95fr) minmax(0, 1.05fr);
}

.product-card--ranked {
  display: grid;
  min-height: 214px;
  grid-template-columns: 132px minmax(0, 1fr);
}

.product-media {
  display: block;
  min-width: 0;
  padding: var(--space-2);
  color: inherit;
}

.image-tray {
  display: block;
  overflow: hidden;
  height: 100%;
  min-height: 188px;
  padding: var(--space-2);
  background: var(--c-surface-sunken);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-12);
}

.image-tray img {
  width: 100%;
  height: 100%;
  min-height: 172px;
  border-radius: var(--radius-8);
  object-fit: cover;
  outline: 1px solid rgba(20, 21, 15, .08);
  outline-offset: -1px;
}

.product-content {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  padding: var(--space-4);
}

.product-title-row {
  display: flex;
  min-width: 0;
  align-items: flex-start;
  gap: var(--space-2);
}

.product-title {
  display: -webkit-box;
  min-width: 0;
  flex: 1;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--c-ink);
  font-size: var(--text-title-size);
  font-weight: 600;
  line-height: var(--text-title-leading);
}

.product-title a {
  color: inherit;
}

.product-title a:hover {
  color: var(--c-brand);
}

.stock-flag {
  flex: 0 0 auto;
  padding: 2px var(--space-2);
  background: var(--c-accent-soft);
  border-radius: var(--radius-full);
  color: var(--c-accent);
  font-size: var(--text-micro-size);
  font-weight: var(--text-micro-weight);
}

.product-description {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  margin-top: var(--space-2);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
  line-height: var(--text-caption-leading);
}

.recipe-label {
  display: grid;
  gap: 0;
  margin-top: var(--space-4);
  border-top: 1px solid var(--c-line);
}

.recipe-label > div {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: var(--space-2);
  padding: 7px 0;
  border-bottom: 1px solid var(--c-line);
}

.recipe-label dt,
.recipe-label dd {
  min-width: 0;
  margin: 0;
}

.recipe-label dt {
  color: var(--c-ink-subtle);
  font-size: var(--text-micro-size);
  font-weight: var(--text-micro-weight);
}

.recipe-label dd {
  overflow: hidden;
  color: var(--c-ink-body);
  font-size: var(--text-caption-size);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: auto;
  padding-top: var(--space-4);
}

.product-price {
  display: flex;
  align-items: baseline;
  color: var(--c-accent);
  font-variant-numeric: tabular-nums;
}

.product-price span {
  margin-right: 2px;
  font-size: var(--text-caption-size);
  font-weight: 600;
}

.product-price strong {
  font-size: var(--text-price-size);
  font-weight: var(--text-price-weight);
  line-height: var(--text-price-leading);
}

.add-button {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: 0 var(--space-4);
  background: var(--c-brand);
  border: 1px solid var(--c-brand);
  border-radius: var(--radius-8);
  color: var(--c-surface);
  cursor: pointer;
  font-size: var(--text-caption-size);
  font-weight: 600;
  transition: background-color var(--duration-state) var(--ease-out), border-color var(--duration-state) var(--ease-out), transform var(--duration-state) var(--ease-out);
}

.add-button:hover:not(:disabled) {
  background: var(--c-brand-hover);
  border-color: var(--c-brand-hover);
}

.add-button:active:not(:disabled) {
  transform: scale(.96);
}

.add-button:disabled {
  background: var(--c-surface-sunken);
  border-color: var(--c-line);
  color: var(--c-ink-subtle);
  cursor: not-allowed;
}

.product-card--featured .product-media {
  padding: var(--space-4);
}

.product-card--featured .image-tray {
  min-height: 300px;
  padding: var(--space-3);
}

.product-card--featured .image-tray img {
  min-height: 272px;
}

.product-card--featured .product-content {
  justify-content: center;
  padding: var(--space-8);
}

.product-card--featured .product-title {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
  line-height: var(--text-display-leading);
}

.product-card--ranked .product-media {
  padding: var(--space-2) 0 var(--space-2) var(--space-2);
}

.product-card--ranked .image-tray {
  min-height: 196px;
}

.product-card--ranked .product-content {
  padding: var(--space-3) var(--space-4);
}

.product-card--ranked .product-description {
  display: none;
}

.product-card--ranked .recipe-label {
  margin-top: var(--space-2);
}

.product-card--ranked .product-footer {
  padding-top: var(--space-3);
}

.product-rank {
  position: absolute;
  z-index: 1;
  top: var(--space-4);
  left: var(--space-4);
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  background: var(--c-ink);
  border-radius: var(--radius-12);
  color: var(--c-surface);
  text-align: center;
}

.product-rank span {
  align-self: end;
  font-size: var(--text-micro-size);
  line-height: 1;
}

.product-rank strong {
  align-self: start;
  font-size: var(--text-title-size);
  line-height: 1.1;
}

@media (max-width: 900px) {
  .product-card--featured {
    grid-template-columns: 1fr;
  }

  .product-card--featured .image-tray {
    min-height: 240px;
  }

  .product-card--featured .image-tray img {
    min-height: 212px;
  }
}

@media (max-width: 640px) {
  .product-card--ranked {
    grid-template-columns: 112px minmax(0, 1fr);
  }

  .product-card--ranked .image-tray {
    min-height: 166px;
  }

  .product-card--ranked .recipe-label,
  .product-card--ranked .product-description {
    display: none;
  }

  .product-card--ranked .product-footer {
    display: block;
  }

  .product-card--ranked .add-button {
    width: 100%;
    margin-top: var(--space-2);
  }
}
</style>
