<template>
  <div>
    <el-row :gutter="15">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" :style="{ backgroundColor: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="15" style="margin-top: 15px">
      <el-col :span="12">
        <el-card>
          <div slot="header" style="font-weight: bold">库存预警（低于 10 件）</div>
          <el-table :data="stats.stockAlerts || []" stripe size="small">
            <el-table-column prop="name" label="商品名称" :show-overflow-tooltip="true"></el-table-column>
            <el-table-column prop="store" label="剩余库存" width="100" align="center">
              <template v-slot="scope">
                <el-tag type="danger" size="mini">{{ scope.row.store }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template v-slot="scope">
                <el-button type="text" size="mini" @click="$router.push('/merchant/goods')">去补货</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!(stats.stockAlerts || []).length" style="color: #999; text-align: center; padding: 20px 0">
            库存充足，暂无预警
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header" style="font-weight: bold">经营提示</div>
          <div class="tips">
            <p><i class="el-icon-warning" style="color: #e6a23c"></i> 待发货订单 <b>{{ stats.pendingShip || 0 }}</b> 笔，请及时到「订单管理」发货</p>
            <p><i class="el-icon-bank-card" style="color: #409eff"></i> 待付款订单 <b>{{ stats.pendingPay || 0 }}</b> 笔，买家支付后即可发货</p>
            <p><i class="el-icon-s-shop" style="color: #67c23a"></i> 在售商品 <b>{{ stats.goodsCount || 0 }}</b> 件，可在「商品管理」维护</p>
            <p v-if="user.state && user.state !== '已通过'">
              <i class="el-icon-circle-close" style="color: #f56c6c"></i>
              店铺状态为「{{ user.state }}」，审核通过后才能上架商品
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "MerchantHome",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      stats: {}
    }
  },
  computed: {
    cards() {
      return [
        { label: '待发货订单', value: this.stats.pendingShip || 0, icon: 'el-icon-van', color: '#e6a23c' },
        { label: '今日订单', value: this.stats.todayOrders || 0, icon: 'el-icon-s-order', color: '#409eff' },
        { label: '今日销售额', value: '￥' + (this.stats.todaySales || 0).toFixed ? '￥' + Number(this.stats.todaySales || 0).toFixed(2) : '￥0', icon: 'el-icon-money', color: '#67c23a' },
        { label: '在售商品', value: this.stats.goodsCount || 0, icon: 'el-icon-goods', color: '#909399' },
      ]
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.$request.get('/merchant/stats').then(res => {
        if (res.code === '200') {
          this.stats = res.data || {}
        }
      })
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}
.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}
.tips p {
  line-height: 2;
  margin: 0;
  font-size: 14px;
  color: #606266;
}
</style>
