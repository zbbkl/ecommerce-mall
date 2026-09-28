<template>
  <div class="orders-page content-shell">
    <header class="page-heading">
      <h1>历史订单</h1>
      <p>查看支付、发货、收货和取消状态，所有操作仍在原订单接口上完成。</p>
    </header>

    <section class="filter-panel">
      <div class="state-filter" role="tablist" aria-label="订单状态筛选">
        <button
          v-for="option in stateOptions"
          :key="option.value || 'all'"
          type="button"
          role="tab"
          :aria-selected="state === option.value"
          :class="{ 'state-filter--active': state === option.value }"
          @click="changeState(option.value)"
        >{{ option.label }}</button>
      </div>

      <form class="search-row" role="search" @submit.prevent="applyFilters">
        <label class="sr-only" for="order-product-name">商品名称</label>
        <input id="order-product-name" v-model.trim="name" type="search" placeholder="商品名称…" />
        <label class="sr-only" for="order-number">订单号</label>
        <input id="order-number" v-model.trim="orderNo" type="search" placeholder="订单号…" />
        <button type="submit">查询</button>
        <button type="button" class="reset-button" @click="reset">重置</button>
      </form>
    </section>

    <section class="orders-list" aria-live="polite">
      <article v-for="order in tableData" :key="order.id" class="order-card">
        <header class="order-head">
          <div class="order-number"><span>订单号</span><strong>{{ order.orderNo || '-' }}</strong></div>
          <div class="order-head-meta">
            <span>{{ order.merchantName || '平台自营' }}</span>
            <span>{{ order.time || '-' }}</span>
            <span class="status-pill" :class="stateClass(order.state)">{{ order.state }}</span>
          </div>
        </header>

        <ol v-if="order.state !== '已取消'" class="order-progress">
          <li
            v-for="(step, index) in steps"
            :key="step"
            :class="{ 'step--done': index < stepIndex(order.state), 'step--current': index === stepIndex(order.state) }"
          >
            <span class="step-dot" aria-hidden="true"><i v-if="index < stepIndex(order.state)" class="el-icon-check"></i><template v-else>{{ index + 1 }}</template></span>
            <span>{{ step }}</span>
          </li>
        </ol>
        <p v-else class="cancelled-note">订单已取消，库存已按原业务逻辑回补。</p>

        <div class="order-body">
          <div class="product-summary">
            <el-image v-if="order.goods && order.goods.cover" class="order-cover" :src="order.goods.cover" :alt="order.name" fit="cover" :preview-src-list="[order.goods.cover]" />
            <span v-else class="order-cover order-cover--missing">无图</span>
            <div>
              <router-link :to="{ path: '/front/goodsDetail', query: { id: order.goodsId } }">{{ order.name || (order.goods && order.goods.name) || '商品信息已更新' }}</router-link>
              <p>数量 {{ order.nums }} · 单价 ¥{{ money(order.price / Math.max(order.nums, 1)) }}</p>
            </div>
          </div>

          <dl class="order-facts">
            <div><dt>收货人</dt><dd>{{ order.user && order.user.name ? order.user.name : '-' }}</dd></div>
            <div><dt>联系方式</dt><dd>{{ order.userPhone || '-' }}</dd></div>
            <div><dt>地址</dt><dd>{{ order.userAddress || '-' }}</dd></div>
          </dl>

          <div class="order-total"><span>订单金额</span><strong class="tabular-nums">¥{{ money(order.price) }}</strong></div>
        </div>

        <footer class="order-actions">
          <button v-if="order.state === '待付款'" class="action-danger" type="button" @click="cancel(order)">取消支付</button>
          <button v-if="order.state === '待付款'" class="action-primary" type="button" @click="pay(order)">支付</button>
          <button v-if="order.state === '已发货'" class="action-secondary" type="button" @click="confirm(order)">确认收货</button>
          <button v-if="order.state === '待付款' && order.parentNo && isBatchPending(order)" class="action-secondary" type="button" @click="payBatch(order)">批次合并支付</button>
          <button class="action-link" type="button" @click="del(order.id)">删除订单</button>
        </footer>
      </article>

      <el-empty v-if="!tableData.length" description="没有找到符合条件的订单" />
      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next" :current-page="pageNum" :page-size="pageSize" :total="total" @current-change="handleCurrentChange" />
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Orders',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      name: '',
      orderNo: '',
      state: this.$route.query.state || '',
      total: 0,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      stateOptions: [
        { label: '全部', value: '' },
        { label: '待付款', value: '待付款' },
        { label: '已支付', value: '已支付' },
        { label: '已发货', value: '已发货' },
        { label: '已完成', value: '已完成' },
        { label: '已取消', value: '已取消' }
      ],
      steps: ['待付款', '已支付', '已发货', '已完成']
    }
  },
  created() { this.load() },
  watch: {
    '$route.query.state'(state) {
      this.state = state || ''
      this.load(1)
    }
  },
  methods: {
    money(value) {
      const amount = Number(value)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    stepIndex(state) { return this.steps.indexOf(state) },
    stateClass(state) {
      return {
        'status--pending': state === '待付款',
        'status--paid': state === '已支付',
        'status--shipped': state === '已发货',
        'status--done': state === '已完成',
        'status--cancelled': state === '已取消'
      }
    },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: { pageNum: this.pageNum, pageSize: this.pageSize, name: this.name, orderNo: this.orderNo, state: this.state }
      }).then(res => {
        this.tableData = Array.isArray(res.data?.records) ? res.data.records : []
        this.total = res.data?.total || 0
      })
    },
    applyFilters() { this.load(1) },
    reset() {
      this.name = ''
      this.orderNo = ''
      this.state = ''
      this.$router.replace({ query: {} })
      this.load(1)
    },
    changeState(value) {
      if (value === this.state && this.$route.query.state === value) {
        this.load(1)
        return
      }
      this.state = value
      const query = { ...this.$route.query }
      if (value) query.state = value
      else delete query.state
      this.$router.replace({ query })
    },
    handleCurrentChange(pageNum) { this.pageNum = pageNum; this.load() },
    isBatchPending(row) {
      return this.tableData.some(item => item.parentNo === row.parentNo && item.state === '待付款' && item.id !== row.id)
    },
    del(id) {
      this.$confirm('确认删除该订单吗？', '确认删除', { type: 'warning' }).then(() => {
        this.$request.delete('/orders/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({ title: '成功', message: '订单已删除', showClose: false, duration: 2000 })
            this.load(1)
            this.$emit('update:cart')
          } else {
            this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          }
        })
      }).catch(() => {})
    },
    cancel(row) {
      this.$confirm('确认取消该订单吗？取消后库存将回补。', '确认取消', { type: 'warning' }).then(() => {
        this.$request.post('/orders/cancel', { id: row.id }).then(res => {
          if (res.code === '200') this.$notify.success({ title: '成功', message: '已取消支付', showClose: false, duration: 2000 })
          else this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          this.load(1)
          this.$emit('update:cart')
        })
      }).catch(() => {})
    },
    pay(row) {
      this.$request.post('/orders/pay', { id: row.id }).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '成功', message: '支付成功', showClose: false, duration: 2000 })
          this.$emit('update:cart')
        } else this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
        this.load(1)
      })
    },
    confirm(row) {
      this.$confirm('确认已收到该订单的商品吗？', '确认收货', { type: 'warning' }).then(() => {
        this.$request.post('/orders/confirm', { id: row.id }).then(res => {
          if (res.code === '200') this.$notify.success({ title: '成功', message: '已确认收货', showClose: false, duration: 2000 })
          else this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          this.load(1)
        })
      }).catch(() => {})
    },
    payBatch(row) {
      this.$confirm('将合并支付该批次下全部待付款订单，确认支付吗？', '批次合并支付', { type: 'warning' }).then(() => {
        this.$request.post('/orders/payBatch', { parentNo: row.parentNo }).then(res => {
          if (res.code === '200') {
            this.$notify.success({ title: '成功', message: '支付成功', showClose: false, duration: 2000 })
            this.$emit('update:cart')
          } else this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
          this.load(1)
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.orders-page { min-height: 75vh; padding-top: var(--space-8); padding-bottom: var(--space-16); }
.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); line-height: var(--text-display-leading); }
.page-heading p { max-width: 44em; margin-top: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-body-size); }
.filter-panel { margin-top: var(--space-6); padding: var(--space-4); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); }
.state-filter { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.state-filter button { min-height: 36px; padding: 0 var(--space-4); background: transparent; border: 1px solid transparent; border-radius: var(--radius-full); color: var(--c-ink-body); cursor: pointer; font-size: var(--text-caption-size); }
.state-filter button:hover, .state-filter--active { background: var(--c-brand-soft); border-color: var(--c-brand-soft); color: var(--c-brand); }
.search-row { display: flex; gap: var(--space-3); margin-top: var(--space-4); }
.search-row input { min-width: 0; height: 40px; flex: 1; padding: 0 var(--space-3); background: var(--c-surface); border: 1px solid var(--c-line-strong); border-radius: var(--radius-8); color: var(--c-ink-body); }
.search-row input:focus { border-color: var(--c-brand); box-shadow: var(--focus-ring); outline: none; }
.search-row button { min-height: 40px; padding: 0 var(--space-5); background: var(--c-brand); border: 1px solid var(--c-brand); border-radius: var(--radius-8); color: var(--c-surface); cursor: pointer; font-weight: 600; }
.search-row .reset-button { background: var(--c-surface); border-color: var(--c-line-strong); color: var(--c-ink-body); }
.orders-list { display: grid; gap: var(--space-5); margin-top: var(--space-6); }
.order-card { padding: var(--space-5); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); }
.order-head { display: flex; align-items: center; justify-content: space-between; gap: var(--space-4); padding-bottom: var(--space-4); border-bottom: 1px solid var(--c-line); }
.order-number { display: flex; align-items: baseline; gap: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.order-number strong { color: var(--c-ink); font-variant-numeric: tabular-nums; }
.order-head-meta { display: flex; align-items: center; gap: var(--space-3); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.status-pill { padding: var(--space-1) var(--space-3); background: var(--c-surface-sunken); border-radius: var(--radius-full); color: var(--c-ink-body); font-weight: 600; }
.status--pending { background: var(--c-accent-soft); color: var(--c-accent); }
.status--paid, .status--done { background: var(--c-brand-soft); color: var(--c-success); }
.status--shipped { background: var(--c-surface-sunken); color: var(--c-info); }
.status--cancelled { color: var(--c-ink-subtle); }
.order-progress { display: grid; grid-template-columns: repeat(4, 1fr); margin: var(--space-5) 0; padding: 0; list-style: none; }
.order-progress li { position: relative; display: grid; gap: var(--space-2); justify-items: center; color: var(--c-ink-subtle); font-size: var(--text-caption-size); }
.order-progress li::before { position: absolute; top: 14px; right: 50%; left: -50%; height: 1px; background: var(--c-line); content: ""; }
.order-progress li:first-child::before { display: none; }
.order-progress .step--done::before, .order-progress .step--current::before { background: var(--c-brand); }
.step-dot { position: relative; z-index: 1; display: grid; width: 28px; height: 28px; place-items: center; background: var(--c-surface-sunken); border: 1px solid var(--c-line); border-radius: var(--radius-full); font-size: var(--text-micro-size); font-weight: 600; }
.step--done, .step--current { color: var(--c-brand); }
.step--done .step-dot, .step--current .step-dot { background: var(--c-brand); border-color: var(--c-brand); color: var(--c-surface); }
.cancelled-note { margin: var(--space-5) 0; padding: var(--space-3) var(--space-4); background: var(--c-surface-sunken); border-radius: var(--radius-8); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.order-body { display: grid; grid-template-columns: minmax(220px, 1.4fr) minmax(260px, 1.2fr) auto; gap: var(--space-4); align-items: center; }
.product-summary { display: flex; min-width: 0; align-items: center; gap: var(--space-3); }
.order-cover { width: 64px; height: 64px; flex: 0 0 64px; border: 1px solid var(--c-line); border-radius: var(--radius-8); object-fit: cover; }
.order-cover--missing { display: grid; place-items: center; background: var(--c-surface-sunken); color: var(--c-ink-subtle); font-size: var(--text-micro-size); }
.product-summary a { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; color: var(--c-ink); font-weight: 600; }
.product-summary p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.order-facts { display: grid; gap: var(--space-2); margin: 0; }
.order-facts > div { display: grid; grid-template-columns: 64px minmax(0, 1fr); gap: var(--space-2); }
.order-facts dt, .order-facts dd { min-width: 0; margin: 0; font-size: var(--text-caption-size); }
.order-facts dt { color: var(--c-ink-subtle); }
.order-facts dd { overflow: hidden; color: var(--c-ink-body); text-overflow: ellipsis; white-space: nowrap; }
.order-total { text-align: right; }
.order-total span { display: block; color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.order-total strong { color: var(--c-accent); font-size: var(--text-price-size); }
.order-actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: var(--space-2); margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--c-line); }
.order-actions button { min-height: 36px; padding: 0 var(--space-4); border-radius: var(--radius-8); cursor: pointer; font-size: var(--text-caption-size); font-weight: 600; }
.action-primary { background: var(--c-brand); border: 1px solid var(--c-brand); color: var(--c-surface); }
.action-secondary { background: var(--c-brand-soft); border: 1px solid var(--c-brand-soft); color: var(--c-brand); }
.action-danger { background: var(--c-accent-soft); border: 1px solid var(--c-accent-soft); color: var(--c-accent); }
.action-link { background: transparent; border: 0; color: var(--c-ink-muted); }
.action-link:hover { color: var(--c-accent); }
.pagination-wrap { display: flex; justify-content: flex-end; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }
@media (max-width: 900px) { .order-head, .order-head-meta { align-items: flex-start; flex-direction: column; } .order-body { grid-template-columns: 1fr; } .order-total { text-align: left; } .order-actions { justify-content: flex-start; } }
@media (max-width: 640px) { .search-row { flex-direction: column; } .order-progress { grid-template-columns: 1fr; gap: var(--space-3); } .order-progress li { grid-template-columns: 28px minmax(0, 1fr); justify-items: start; } .order-progress li::before { display: none; } }
</style>
