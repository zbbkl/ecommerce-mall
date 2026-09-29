<template>
  <div class="merchant-page">
    <header class="page-heading"><div><h1>订单管理</h1><p>查看订单详情，并对已支付订单执行发货。</p></div></header>
    <section class="panel">
      <div class="state-filter" role="tablist" aria-label="订单状态筛选"><button v-for="option in stateOptions" :key="option.value || 'all'" type="button" role="tab" :aria-selected="state === option.value" :class="{ 'state-filter--active': state === option.value }" @click="changeState(option.value)">{{ option.label }}</button></div>
      <form class="filter-bar" @submit.prevent="load(1)"><el-input v-model.trim="orderNo" clearable placeholder="查询订单号" aria-label="查询订单号" /><el-button type="primary" native-type="submit">查询</el-button><el-button @click="reset">重置</el-button></form>
      <el-table :data="tableData" stripe>
        <el-table-column prop="orderNo" label="订单号" width="170" :show-overflow-tooltip="true" />
        <el-table-column label="商品" width="190" :show-overflow-tooltip="true"><template v-slot="scope"><router-link v-if="scope.row.goods" class="table-action" :to="{ path: '/front/goodsDetail', query: { id: scope.row.goodsId } }">{{ scope.row.name }}</router-link><span v-else>{{ scope.row.name }}</span></template></el-table-column>
        <el-table-column label="封面" width="80" align="center"><template v-slot="scope"><el-image v-if="scope.row.goods && scope.row.goods.cover" class="table-image" :src="scope.row.goods.cover" :alt="scope.row.name" fit="cover" :preview-src-list="[scope.row.goods.cover]" /><span v-else>-</span></template></el-table-column>
        <el-table-column label="金额" width="100" align="right"><template v-slot="scope"><span class="money tabular-nums">¥{{ money(scope.row.price) }}</span></template></el-table-column>
        <el-table-column prop="nums" label="件数" width="65" align="center" />
        <el-table-column label="买家" width="90"><template v-slot="scope">{{ scope.row.user ? scope.row.user.name : '-' }}</template></el-table-column>
        <el-table-column prop="userPhone" label="收货电话" width="125" :show-overflow-tooltip="true" />
        <el-table-column prop="userAddress" label="收货地址" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column prop="time" label="下单时间" width="155" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="95" align="center"><template v-slot="scope"><span class="order-state" :class="stateClass(scope.row.state)">{{ scope.row.state }}</span></template></el-table-column>
        <el-table-column label="操作" width="145" align="center" fixed="right"><template v-slot="scope"><button class="table-action" type="button" @click="detail(scope.row)">详情</button><button v-if="scope.row.state === '已支付'" class="table-action" type="button" @click="ship(scope.row)">发货</button></template></el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination background layout="total, prev, pager, next" :current-page="pageNum" :page-size="pageSize" :total="total" @current-change="handleCurrentChange" /></div>
    </section>

    <el-drawer :visible.sync="detailVisible" title="订单详情" :with-header="false" size="min(92vw, 560px)">
      <div class="drawer-header"><span class="drawer-title">订单详情</span><button class="drawer-close" type="button" aria-label="关闭订单详情" @click="detailVisible = false"><i class="el-icon-close" aria-hidden="true"></i></button></div>
      <div class="drawer-content">
        <el-descriptions :column="1" border size="small"><el-descriptions-item label="订单号">{{ form.orderNo }}</el-descriptions-item><el-descriptions-item label="批次号">{{ form.parentNo }}</el-descriptions-item><el-descriptions-item label="买家">{{ form.user ? form.user.name : '-' }}</el-descriptions-item><el-descriptions-item label="收货电话">{{ form.userPhone }}</el-descriptions-item><el-descriptions-item label="收货地址">{{ form.userAddress }}</el-descriptions-item><el-descriptions-item label="下单时间">{{ form.time }}</el-descriptions-item><el-descriptions-item label="状态">{{ form.state }}</el-descriptions-item></el-descriptions>
        <h3 class="detail-heading">商品明细</h3>
        <el-table :data="form.items || []" stripe size="small"><el-table-column prop="goodsName" label="商品名称" :show-overflow-tooltip="true" /><el-table-column label="单价" width="90"><template v-slot="scope">¥{{ money(scope.row.price) }}</template></el-table-column><el-table-column prop="nums" label="数量" width="70" align="center" /><el-table-column label="小计" width="100" align="right"><template v-slot="scope"><span class="tabular-nums">¥{{ money(scope.row.price * scope.row.nums) }}</span></template></el-table-column></el-table>
      </div>
      <div class="drawer-footer"><el-button v-if="form.state === '已支付'" type="primary" @click="ship(form)">发货</el-button><el-button @click="detailVisible = false">关闭</el-button></div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: 'MerchantOrders',
  data() { return { tableData: [], pageNum: 1, pageSize: 10, orderNo: '', state: '', total: 0, detailVisible: false, form: {}, stateOptions: [{ label: '全部', value: '' }, { label: '待付款', value: '待付款' }, { label: '已支付', value: '已支付' }, { label: '已发货', value: '已发货' }, { label: '已完成', value: '已完成' }, { label: '已取消', value: '已取消' }] } },
  created() { this.load() },
  methods: {
    money(value) { const amount = Number(value); return Number.isFinite(amount) ? amount.toFixed(2) : '0.00' },
    stateClass(state) { return { 'order-state--pending': state === '待付款', 'order-state--paid': state === '已支付', 'order-state--shipped': state === '已发货', 'order-state--done': state === '已完成', 'order-state--cancelled': state === '已取消' } },
    load(pageNum) { if (pageNum) this.pageNum = pageNum; this.$request.get('/merchant/orders/selectPage', { params: { pageNum: this.pageNum, pageSize: this.pageSize, orderNo: this.orderNo, state: this.state } }).then(res => { this.tableData = Array.isArray(res.data?.records) ? res.data.records : []; this.total = res.data?.total || 0 }) },
    changeState(value) { this.state = value; this.load(1) },
    detail(row) { this.$request.get('/merchant/orders/detail', { params: { id: row.id } }).then(res => { if (res.code === '200') { this.form = res.data || {}; this.detailVisible = true } else this.$notify.error({ title: '加载失败', message: res.msg, showClose: false, duration: 2000 }) }) },
    ship(row) { this.$confirm('确认对该订单发货吗？', '确认发货', { type: 'warning' }).then(() => { this.$request.post('/merchant/orders/ship?id=' + row.id).then(res => { if (res.code === '200') { this.$notify.success({ title: '成功', message: '发货成功', showClose: false, duration: 2000 }); this.detailVisible = false; this.load() } else this.$notify.error({ title: '发货失败', message: res.msg, showClose: false, duration: 2000 }) }) }).catch(() => {}) },
    reset() { this.orderNo = ''; this.state = ''; this.load(1) },
    handleCurrentChange(pageNum) { this.pageNum = pageNum; this.load() }
  }
}
</script>

<style scoped>
.merchant-page { min-height: calc(100dvh - var(--header-height) - 32px); }
.page-heading { margin-bottom: var(--space-5); }.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }.page-heading p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.panel { padding: var(--space-5); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-12); box-shadow: var(--shadow-e1); }
.state-filter { display: flex; flex-wrap: wrap; gap: var(--space-2); }.state-filter button { min-height: 36px; padding: 0 var(--space-4); background: transparent; border: 1px solid transparent; border-radius: var(--radius-full); color: var(--c-ink-body); cursor: pointer; font-size: var(--text-caption-size); }.state-filter button:hover,.state-filter--active { background: var(--c-brand-soft); border-color: var(--c-brand-soft); color: var(--c-brand); }
.filter-bar { display: grid; grid-template-columns: minmax(200px,300px) auto auto; gap: var(--space-3); justify-content: start; margin: var(--space-4) 0; }.table-image { width: 48px; height: 48px; border-radius: var(--radius-8); }.money { color: var(--c-accent); font-weight: 600; }
.table-action { padding: 4px var(--space-2); background: transparent; border: 0; color: var(--c-brand); cursor: pointer; font-size: var(--text-caption-size); }.table-action:hover { color: var(--c-brand-hover); }
.order-state { display: inline-flex; padding: 3px var(--space-2); border-radius: var(--radius-full); font-size: var(--text-micro-size); font-weight: 600; }.order-state--pending { background: var(--c-accent-soft); color: var(--c-accent); }.order-state--paid,.order-state--done { background: var(--c-brand-soft); color: var(--c-success); }.order-state--shipped { background: var(--c-surface-sunken); color: var(--c-info); }.order-state--cancelled { background: var(--c-surface-sunken); color: var(--c-ink-muted); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-4); }.detail-heading { margin: var(--space-5) 0 var(--space-3); font-size: var(--text-title-size); }.drawer-close { width: 32px; height: 32px; background: var(--c-surface-sunken); border: 0; border-radius: var(--radius-full); color: var(--c-ink-body); cursor: pointer; }
@media (max-width: 700px) { .filter-bar { grid-template-columns: 1fr; } }
</style>
