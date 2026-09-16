<template>
  <div style="margin: 10px auto;width: 70%;min-height: 90vh">
    <el-card>
      <div slot="header" style="display: flex; align-items: center; justify-content: space-between">
        <span style="font-weight: bold; font-size: 16px">我的购物车</span>
        <div>
          <el-button type="success" plain size="small" @click="goPending">待支付订单</el-button>
          <el-button type="info" plain size="small" @click="goHistory">历史订单</el-button>
          <el-button type="danger" plain size="small" :disabled="!tableData.length" @click="clear">清空购物车</el-button>
        </div>
      </div>

      <el-table ref="cartTable" :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" :selectable="row => !!row.goods"></el-table-column>
        <el-table-column label="商品" :show-overflow-tooltip="true">
          <template v-slot="scope">
            <div style="display: flex; align-items: center">
              <el-image v-if="scope.row.goods" style="width: 50px; height: 50px" :src="scope.row.goods.cover" fit="cover" :preview-src-list="[scope.row.goods.cover]"></el-image>
              <el-link v-if="scope.row.goods" style="margin-left: 10px" :href="'/front/goodsDetail?id=' + scope.row.goodsId" :underline="false">{{scope.row.goods.name}}</el-link>
              <span v-else style="color: #999">商品已下架</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="110">
          <template v-slot="scope">
            <span v-if="scope.row.goods">￥{{scope.row.goods.price}}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="170">
          <template v-slot="scope">
            <el-input-number size="mini" v-model="scope.row.nums" :min="1"
                             :max="scope.row.goods ? scope.row.goods.store : 1"
                             :disabled="!scope.row.goods"
                             @change="changeNums(scope.row)"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template v-slot="scope">
            <span v-if="scope.row.goods" style="color: #ff6700; font-weight: bold">￥{{(scope.row.goods.price * scope.row.nums).toFixed(2)}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="加入时间" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template v-slot="scope">
            <el-button size="mini" type="danger" plain @click="del(scope.row.goodsId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 15px">
        <div style="font-size: 14px; color: #606266">
          已选 <b style="color: #409eff">{{selected.length}}</b> 件商品，合计：
          <b style="color: #ff6700; font-size: 20px">￥{{totalAmount}}</b>
        </div>
        <el-button type="primary" :disabled="!selected.length" @click="settle">结 算</el-button>
      </div>

      <div v-if="!tableData.length" style="text-align: center; color: #999; padding: 30px 0">
        购物车还是空的，去
        <el-link :underline="false" href="/front/goods" style="font-size: 13px; vertical-align: baseline">全部商品</el-link>
        逛逛吧~
      </div>
    </el-card>
  </div>
</template>

<script>
import cart from '@/utils/cart'

export default {
  name: "Cart",
  data() {
    return {
      tableData: [],
      selected: [],
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
    }
  },
  computed: {
    totalAmount() {
      return this.selected
          .filter(row => row.goods)
          .reduce((sum, row) => sum + row.goods.price * row.nums, 0)
          .toFixed(2)
    }
  },
  created() {
    this.load()
  },
  methods: {
    // 购物车数据存本地，商品信息实时从服务端获取，保证价格/库存是最新值
    load() {
      const items = this.user.id ? cart.list(this.user.id) : []
      this.tableData = items.map(item => ({goodsId: item.goodsId, nums: item.nums, time: item.time, goods: null}))
      this.selected = []
      this.tableData.forEach(row => {
        this.$request.get('/goods/selectById?id=' + row.goodsId).then(res => {
          if (res.code === '200' && res.data) {
            row.goods = res.data
          }
        })
      })
    },
    handleSelectionChange(rows) {
      this.selected = rows
    },
    changeNums(row) {
      cart.updateNums(this.user.id, row.goodsId, row.nums)
      this.$emit('update:cart')
    },
    del(goodsId) {
      this.$confirm('您确认从购物车中删除该商品吗？', '确认删除', {type: "warning"}).then(() => {
        cart.remove(this.user.id, goodsId)
        this.$notify.success({title: '成功', message: '已删除', showClose: false, duration: 2000});
        this.load()
        this.$emit('update:cart')
      }).catch(() => {})
    },
    clear() {
      this.$confirm('您确认清空购物车吗？', '确认清空', {type: "warning"}).then(() => {
        cart.clear(this.user.id)
        this.$notify.success({title: '成功', message: '已清空', showClose: false, duration: 2000});
        this.load()
        this.$emit('update:cart')
      }).catch(() => {})
    },
    settle() {
      const items = this.selected.map(row => ({goodsId: row.goodsId, nums: row.nums}))
      this.$request.post('/orders/settle', items).then(res => {
        if (res.code === '200') {
          // 下单成功后移除已结算的本地购物车项（与原结算清空已下单记录的行为一致）
          cart.removeByIds(this.user.id, this.selected.map(row => row.goodsId))
          this.$notify.success({title: '成功', message: '下单成功，请尽快支付', showClose: false, duration: 2000});
          this.$emit('update:cart')
          this.$router.push('/front/orders?state=待付款')
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
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

</style>
