<template>
  <div class="merchant-page">
    <header class="page-heading">
      <div>
        <h1>工作台</h1>
        <p>{{ user.shopName || '商户中心' }} 的今日经营概览。</p>
      </div>
      <span class="shop-state">{{ user.state || '状态未知' }}</span>
    </header>

    <section class="metric-grid" aria-label="经营统计">
      <article v-for="card in cards" :key="card.label" class="metric-card">
        <span class="metric-icon" :class="`metric-icon--${card.tone}`">
          <i :class="card.icon" aria-hidden="true"></i>
        </span>
        <div>
          <strong class="metric-value tabular-nums">{{ card.value }}</strong>
          <span class="metric-label">{{ card.label }}</span>
        </div>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel">
        <header class="panel-head">
          <div>
            <h2>库存预警</h2>
            <p>剩余库存低于 10 件。</p>
          </div>
          <router-link to="/merchant/goods">管理商品</router-link>
        </header>

        <el-table v-if="(stats.stockAlerts || []).length" :data="stats.stockAlerts || []" stripe>
          <el-table-column prop="name" label="商品名称" :show-overflow-tooltip="true" />
          <el-table-column prop="store" label="剩余库存" width="110" align="center">
            <template v-slot="scope">
              <span class="stock-low tabular-nums">{{ scope.row.store }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template v-slot="scope">
              <router-link class="table-action" to="/merchant/goods">去补货</router-link>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="库存充足，暂无预警" :image-size="120" />
      </article>

      <article class="panel">
        <header class="panel-head">
          <div>
            <h2>经营提示</h2>
            <p>需要优先处理的订单和商品状态。</p>
          </div>
        </header>

        <ul class="tips">
          <li>
            <i class="el-icon-van" aria-hidden="true"></i>
            <span>待发货订单</span>
            <b class="tabular-nums">{{ stats.pendingShip || 0 }}</b>
          </li>
          <li>
            <i class="el-icon-bank-card" aria-hidden="true"></i>
            <span>待付款订单</span>
            <b class="tabular-nums">{{ stats.pendingPay || 0 }}</b>
          </li>
          <li>
            <i class="el-icon-s-shop" aria-hidden="true"></i>
            <span>在售商品</span>
            <b class="tabular-nums">{{ stats.goodsCount || 0 }}</b>
          </li>
          <li v-if="user.state && user.state !== '已通过'" class="tips-warning">
            <i class="el-icon-warning" aria-hidden="true"></i>
            <span>店铺状态为「{{ user.state }}」，审核通过后才能上架商品。</span>
          </li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script>
export default {
  name: 'MerchantHome',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      stats: {}
    }
  },
  computed: {
    cards() {
      return [
        { label: '待发货订单', value: this.stats.pendingShip || 0, icon: 'el-icon-van', tone: 'warning' },
        { label: '今日订单', value: this.stats.todayOrders || 0, icon: 'el-icon-s-order', tone: 'accent' },
        { label: '今日销售额', value: `¥${Number(this.stats.todaySales || 0).toFixed(2)}`, icon: 'el-icon-money', tone: 'success' },
        { label: '在售商品', value: this.stats.goodsCount || 0, icon: 'el-icon-goods', tone: 'neutral' }
      ]
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.$request.get('/merchant/stats').then(res => {
        if (res.code === '200') this.stats = res.data || {}
      })
    }
  }
}
</script>

<style scoped>
.merchant-page {
  min-height: calc(100dvh - var(--header-height) - 32px);
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-5);
}

.page-heading h1 {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
}

.page-heading p {
  margin-top: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.shop-state {
  padding: var(--space-1) var(--space-3);
  background: var(--c-brand-soft);
  border-radius: var(--radius-full);
  color: var(--c-success);
  font-size: var(--text-caption-size);
  font-weight: 600;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.metric-card {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-12);
  box-shadow: var(--shadow-e1);
}

.metric-icon {
  display: grid;
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  place-items: center;
  border-radius: var(--radius-8);
  font-size: var(--text-title-lg-size);
}

.metric-icon--warning {
  background: var(--c-flag);
  color: var(--c-ink);
}

.metric-icon--accent {
  background: var(--c-accent-soft);
  color: var(--c-accent);
}

.metric-icon--success {
  background: var(--c-brand-soft);
  color: var(--c-success);
}

.metric-icon--neutral {
  background: var(--c-surface-sunken);
  color: var(--c-ink-muted);
}

.metric-value {
  display: block;
  overflow: hidden;
  color: var(--c-ink);
  font-size: var(--text-title-lg-size);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric-label {
  display: block;
  margin-top: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.panel {
  min-width: 0;
  padding: var(--space-5);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-12);
  box-shadow: var(--shadow-e1);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.panel-head h2 {
  font-size: var(--text-title-size);
  font-weight: 600;
}

.panel-head p {
  margin-top: var(--space-1);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.panel-head a {
  flex: 0 0 auto;
  color: var(--c-brand);
  font-size: var(--text-caption-size);
}

.tips {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}

.tips li {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) auto;
  gap: var(--space-2);
  align-items: center;
  color: var(--c-ink-body);
  font-size: var(--text-body-size);
}

.tips i {
  color: var(--c-info);
}

.tips b {
  color: var(--c-ink);
}

.tips-warning i,
.tips-warning span {
  color: var(--c-accent);
}

.stock-low {
  color: var(--c-accent);
  font-weight: 600;
}

.table-action {
  color: var(--c-brand);
  font-size: var(--text-caption-size);
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .metric-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
