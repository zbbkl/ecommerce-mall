<template>
  <div class="cart-page content-shell">
    <header class="page-heading">
      <div>
        <h1>我的购物车</h1>
        <p>商品价格和库存会在结算时重新校验。</p>
      </div>
      <div class="page-actions">
        <button type="button" @click="goPending">待支付订单</button>
        <button type="button" @click="goHistory">历史订单</button>
        <button class="danger-link" type="button" :disabled="!tableData.length" @click="clear">清空购物车</button>
      </div>
    </header>

    <section class="cart-panel" aria-live="polite">
      <template v-if="tableData.length">
        <div class="cart-list-head">
          <el-checkbox
            :value="allSelected"
            :indeterminate="selectionIndeterminate"
            :disabled="!selectableRows.length"
            @change="toggleAll"
          >
            全选
          </el-checkbox>
          <span>商品信息</span>
          <span>单价</span>
          <span>数量</span>
          <span>小计</span>
          <span>操作</span>
        </div>

        <article
          v-for="row in tableData"
          :key="row.goodsId"
          class="cart-row"
          :class="{ 'cart-row--unavailable': !row.goods }"
        >
          <el-checkbox
            v-model="row.selected"
            :disabled="!row.goods"
            :aria-label="`选择${row.goods ? row.goods.name : '已下架商品'}`"
          />

          <div class="product-cell">
            <el-image
              v-if="row.goods"
              class="product-image"
              :src="row.goods.cover"
              :alt="row.goods.name"
              fit="cover"
              :preview-src-list="[row.goods.cover]"
            />
            <span v-else class="product-image product-image--missing">下架</span>
            <div class="product-copy">
              <router-link
                v-if="row.goods"
                :to="{ path: '/front/goodsDetail', query: { id: row.goodsId } }"
              >
                {{ row.goods.name }}
              </router-link>
              <strong v-else>商品已下架</strong>
              <p>{{ row.goods ? row.goods.descr : '请从购物车中删除该商品。' }}</p>
              <time>{{ row.time }}</time>
            </div>
          </div>

          <div class="price-cell">
            <span class="mobile-label">单价</span>
            <strong v-if="row.goods">¥{{ money(row.goods.price) }}</strong>
            <span v-else>-</span>
          </div>

          <div class="quantity-cell">
            <span class="mobile-label">数量</span>
            <el-input-number
              v-if="row.goods"
              v-model="row.nums"
              size="small"
              :min="1"
              :max="Math.max(1, row.goods.store)"
              @change="changeNums(row)"
            />
            <span v-else>-</span>
          </div>

          <div class="subtotal-cell">
            <span class="mobile-label">小计</span>
            <strong v-if="row.goods" class="tabular-nums">¥{{ rowTotal(row) }}</strong>
            <span v-else>-</span>
          </div>

          <button class="remove-button" type="button" @click="del(row.goodsId)">删除</button>
        </article>

        <div class="settle-bar">
          <div class="settle-info">
            <span>已选 <b>{{ selectedRows.length }}</b> 种，共 <b>{{ selectedQuantity }}</b> 件</span>
            <span class="settle-divider" aria-hidden="true"></span>
            <span class="settle-total">
              合计
              <strong class="tabular-nums"><small>¥</small>{{ totalAmount }}</strong>
            </span>
          </div>
          <button class="settle-button" type="button" :disabled="!selectedRows.length" @click="settle">
            结算
          </button>
        </div>
      </template>

      <el-empty v-else description="购物车还是空的">
        <router-link class="empty-action" to="/front/goods">去全部商品看看</router-link>
      </el-empty>
    </section>
  </div>
</template>

<script>
import cart from '@/utils/cart'

export default {
  name: 'Cart',
  data() {
    return {
      tableData: [],
      user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : {}
    }
  },
  computed: {
    selectableRows() {
      return this.tableData.filter(row => row.goods)
    },
    selectedRows() {
      return this.selectableRows.filter(row => row.selected)
    },
    selectedQuantity() {
      return this.selectedRows.reduce((sum, row) => sum + Number(row.nums || 0), 0)
    },
    allSelected() {
      return this.selectableRows.length > 0 && this.selectedRows.length === this.selectableRows.length
    },
    selectionIndeterminate() {
      return this.selectedRows.length > 0 && !this.allSelected
    },
    totalAmount() {
      return this.selectedRows
        .reduce((sum, row) => sum + Number(row.goods.price) * Number(row.nums), 0)
        .toFixed(2)
    }
  },
  created() {
    this.load()
  },
  methods: {
    money(value) {
      const amount = Number(value)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    rowTotal(row) {
      return this.money(Number(row.goods.price) * Number(row.nums))
    },
    load() {
      const items = this.user.id ? cart.list(this.user.id) : []
      this.tableData = items.map(item => ({
        goodsId: item.goodsId,
        nums: item.nums,
        time: item.time,
        goods: null,
        selected: false
      }))

      this.tableData.forEach(row => {
        this.$request.get('/goods/selectById?id=' + row.goodsId).then(res => {
          if (res.code === '200' && res.data) {
            row.goods = res.data
          } else {
            row.selected = false
          }
        })
      })
    },
    toggleAll(checked) {
      this.selectableRows.forEach(row => { row.selected = checked })
    },
    changeNums(row) {
      cart.updateNums(this.user.id, row.goodsId, row.nums)
      this.$emit('update:cart')
    },
    del(goodsId) {
      this.$confirm('确认从购物车中删除该商品吗？', '确认删除', { type: 'warning' }).then(() => {
        cart.remove(this.user.id, goodsId)
        this.$notify.success({ title: '成功', message: '已删除', showClose: false, duration: 2000 })
        this.load()
        this.$emit('update:cart')
      }).catch(() => {})
    },
    clear() {
      this.$confirm('确认清空购物车吗？', '确认清空', { type: 'warning' }).then(() => {
        cart.clear(this.user.id)
        this.$notify.success({ title: '成功', message: '已清空', showClose: false, duration: 2000 })
        this.load()
        this.$emit('update:cart')
      }).catch(() => {})
    },
    settle() {
      const items = this.selectedRows.map(row => ({ goodsId: row.goodsId, nums: row.nums }))
      this.$request.post('/orders/settle', items).then(res => {
        if (res.code === '200') {
          cart.removeByIds(this.user.id, items.map(item => item.goodsId))
          this.$notify.success({ title: '成功', message: '下单成功，请尽快支付', showClose: false, duration: 2000 })
          this.$emit('update:cart')
          this.$router.push('/front/orders?state=待付款')
        } else {
          this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          this.load()
        }
      })
    },
    goPending() {
      this.$router.push('/front/orders?state=待付款')
    },
    goHistory() {
      this.$router.push('/front/orders')
    }
  }
}
</script>

<style scoped>
.cart-page {
  min-height: 75vh;
  padding-top: var(--space-8);
  padding-bottom: var(--space-16);
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-8);
}

.page-heading h1 {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
  line-height: var(--text-display-leading);
}

.page-heading p {
  margin-top: var(--space-2);
  color: var(--c-ink-muted);
  font-size: var(--text-body-size);
}

.page-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.page-actions button,
.empty-action {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-4);
  background: var(--c-surface);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-8);
  color: var(--c-ink-body);
  cursor: pointer;
  font-size: var(--text-caption-size);
  font-weight: 500;
}

.page-actions button:hover,
.empty-action:hover {
  border-color: var(--c-brand);
  color: var(--c-brand);
}

.page-actions button:disabled {
  background: var(--c-surface-sunken);
  border-color: var(--c-line);
  color: var(--c-ink-subtle);
  cursor: not-allowed;
}

.page-actions .danger-link {
  color: var(--c-accent);
}

.cart-panel {
  margin-top: var(--space-6);
  padding: var(--space-5);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
}

.cart-list-head,
.cart-row {
  display: grid;
  grid-template-columns: 32px minmax(240px, 1.8fr) minmax(90px, .6fr) 150px minmax(110px, .7fr) 64px;
  gap: var(--space-4);
  align-items: center;
}

.cart-list-head {
  padding: 0 var(--space-3) var(--space-3);
  border-bottom: 1px solid var(--c-line);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.cart-list-head > span:nth-child(2) {
  grid-column: 2;
}

.cart-row {
  padding: var(--space-4) var(--space-3);
  border-bottom: 1px solid var(--c-line);
}

.cart-row:last-of-type {
  border-bottom: 0;
}

.cart-row--unavailable {
  background: var(--c-surface-sunken);
}

.product-cell {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: var(--space-4);
}

.product-image {
  width: 88px;
  height: 88px;
  flex: 0 0 88px;
  background: var(--c-surface-sunken);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-8);
  object-fit: cover;
}

.product-image--missing {
  display: grid;
  place-items: center;
  color: var(--c-ink-subtle);
  font-size: var(--text-caption-size);
}

.product-copy {
  min-width: 0;
}

.product-copy a,
.product-copy strong {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  color: var(--c-ink);
  font-size: var(--text-body-size);
  font-weight: 600;
}

.product-copy p {
  overflow: hidden;
  margin-top: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-copy time {
  display: block;
  margin-top: var(--space-2);
  color: var(--c-ink-subtle);
  font-size: var(--text-micro-size);
}

.price-cell,
.subtotal-cell {
  color: var(--c-ink-body);
  font-size: var(--text-body-size);
  font-variant-numeric: tabular-nums;
}

.subtotal-cell strong {
  color: var(--c-accent);
}

.quantity-cell {
  display: flex;
  align-items: center;
}

.mobile-label {
  display: none;
}

.remove-button {
  min-height: 36px;
  padding: 0 var(--space-2);
  background: transparent;
  border: 0;
  color: var(--c-accent);
  cursor: pointer;
  font-size: var(--text-caption-size);
}

.remove-button:hover {
  color: var(--c-ink);
}

.settle-bar {
  position: sticky;
  bottom: var(--space-3);
  z-index: var(--z-sticky);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
  margin-top: var(--space-5);
  padding: var(--space-4);
  background: var(--c-surface-sunken);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-12);
}

.settle-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  color: var(--c-ink-body);
  font-size: var(--text-caption-size);
}

.settle-info b {
  color: var(--c-ink);
  font-variant-numeric: tabular-nums;
}

.settle-divider {
  width: 1px;
  height: 20px;
  background: var(--c-line-strong);
}

.settle-total {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
}

.settle-total strong {
  color: var(--c-accent);
  font-size: var(--text-price-lg-size);
  font-weight: var(--text-price-lg-weight);
}

.settle-total small {
  margin-right: 2px;
  font-size: var(--text-title-size);
}

.settle-button {
  min-height: 44px;
  min-width: 128px;
  padding: 0 var(--space-6);
  background: var(--c-brand);
  border: 1px solid var(--c-brand);
  border-radius: var(--radius-8);
  color: var(--c-surface);
  cursor: pointer;
  font-size: var(--text-body-size);
  font-weight: 600;
}

.settle-button:hover:not(:disabled) {
  background: var(--c-brand-hover);
  border-color: var(--c-brand-hover);
}

.settle-button:disabled {
  background: var(--c-surface);
  border-color: var(--c-line);
  color: var(--c-ink-subtle);
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-list-head {
    display: none;
  }

  .cart-row {
    grid-template-columns: 28px minmax(0, 1fr) auto;
    gap: var(--space-3);
  }

  .product-cell {
    grid-column: 2 / 4;
  }

  .price-cell,
  .quantity-cell,
  .subtotal-cell {
    grid-column: 2;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-4);
  }

  .remove-button {
    grid-column: 3;
    grid-row: 2;
  }

  .mobile-label {
    display: inline;
    color: var(--c-ink-muted);
    font-size: var(--text-caption-size);
  }

  .settle-bar,
  .settle-info {
    align-items: flex-start;
    flex-direction: column;
  }

  .settle-divider {
    display: none;
  }

  .settle-button {
    width: 100%;
  }
}
</style>
