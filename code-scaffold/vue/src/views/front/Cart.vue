<template>
  <div style="margin: 10px auto;width: 70%;min-height: 90vh">
    <el-card>
      <div slot="header" style="display: flex; align-items: center; justify-content: space-between">
        <span style="font-weight: bold; font-size: 16px">我的购物车</span>
        <div>
          <!-- 待支付订单是主路径入口，用主色；历史订单中性；清空是危险操作 -->
          <el-button type="primary" plain size="small" @click="goPending">待支付订单</el-button>
          <el-button type="info" plain size="small" @click="goHistory">历史订单</el-button>
          <el-button type="danger" plain size="small" :disabled="!tableData.length" @click="clear">清空购物车</el-button>
        </div>
      </div>

      <!-- 表格的表头底色、行 hover 高亮、暖色分隔线与删除按钮配色统一由 global.css 提供 -->
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

      <div class="settle-bar">
        <div class="settle-info">
          <span class="settle-count">已选 <b>{{selected.length}}</b> 件商品</span>
          <span class="settle-divider"></span>
          <span class="settle-total">
            合计
            <span class="total-price"><span class="total-symbol">￥</span>{{totalAmount}}</span>
          </span>
        </div>
        <button class="settle-btn" :disabled="!selected.length" @click="settle">
          <span class="settle-text">结 算</span>
          <svg class="settle-arrow" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512">
            <path d="M169.4 470.6c12.5 12.5 32.8 12.5 45.3 0l160-160c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L224 370.8 224 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 306.7L54.6 265.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l160 160z"></path>
          </svg>
        </button>
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
/* ============ 底部结算栏 ============ */
.settle-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18px;
  padding: 14px 20px;
  background: linear-gradient(to right, #fff7f2, #fffdfb 60%, #fff);
  border: 1px solid #ffe3d1;
  border-radius: 12px;
}

.settle-info {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  color: #606266;
}

.settle-count b {
  color: #ff6700;
  font-size: 16px;
}

.settle-divider {
  width: 1px;
  height: 14px;
  background: #f0d8c8;
}

.settle-total {
  display: flex;
  align-items: baseline;
}

/* 合计金额：大号橙字，数字用等宽感字体 */
.total-price {
  margin-left: 6px;
  color: #ff6700;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.5px;
  font-family: 'DIN Alternate', 'Bahnschrift', 'Helvetica Neue', Arial, sans-serif;
}

.total-symbol {
  font-size: 15px;
  font-weight: bold;
  margin-right: 1px;
}

/* ============ 结算按钮 ============
   改编自 Uiverse.io by vinodjangid07（galaxy/Buttons/clever-bird-35）：
   原为向下箭头的下载按钮，箭头旋转为右向、hover 滑入；
   渐变配色与详情页 .buy-btn 统一 */
.settle-btn {
  display: flex;
  align-items: center;
  height: 42px;
  padding: 0 24px;
  border: none;
  border-radius: 10px;
  background-image: linear-gradient(to right, #ff8a2b, #ff6700 55%, #f25600);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(255, 103, 0, 0.35);
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
}

.settle-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 103, 0, 0.45);
  filter: brightness(1.05);
}

.settle-btn:active:not(:disabled) {
  transform: scale(0.96);
}

.settle-btn:disabled {
  background-image: linear-gradient(to right, #ffc9a3, #ffb184);
  box-shadow: none;
  color: rgba(255, 255, 255, 0.85);
  cursor: not-allowed;
}

/* 箭头：默认收起（宽度0），hover 展开滑入 */
.settle-arrow {
  width: 0;
  height: 15px;
  fill: #fff;
  transform: rotate(-90deg);   /* 原组件箭头向下，转为右向 */
  opacity: 0;
  transition: width 0.3s ease, margin-left 0.3s ease, opacity 0.3s ease;
}

.settle-btn:hover:not(:disabled) .settle-arrow {
  width: 15px;
  margin-left: 4px;
  opacity: 1;
}
</style>
