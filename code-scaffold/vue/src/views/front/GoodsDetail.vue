<template>
  <div style="width: 60%;min-height: 90vh;margin: 10px auto">
    <div style="display: flex">
      <div style="flex: 4">
        <el-image v-if="goods.cover" style="width: 400px; height: 300px" :src="goods.cover" fit="cover" :preview-src-list="[goods.cover]"></el-image>
      </div>
      <div style="flex: 6">
        <div style="padding: 0 15px">
          <b style="font-size: 20px;color: #303133;">{{goods.name}}</b>
        </div>
        <div style="padding: 0 15px;margin-top: 10px;">
          <el-link type="primary" :underline="false" @click="$router.push('/front/shop?id=' + goods.merchantId)">
            <i class="el-icon-shop"></i> {{ goods.merchantName || '平台自营' }} <span style="color:#909399">进店 ></span>
          </el-link>
          <p style="font-size: 11px;color: #606266;line-height: 17px;">{{goods.descr}}</p>
          <div style="border-bottom: 1px dashed #eaeaea;margin-top: 10px"></div>
        </div>
        <div style="display: flex;padding: 0 15px;margin-top: 20px;align-items: center">
          <div>
            <span style="font-size: 11px;color: #606266">价格：</span>
            <span style="font-size: 17px;color: #ff6700;font-weight: bold">￥{{goods.price}}</span>
          </div>
          <div style="margin-left: 20px">
            <span style="font-size: 11px;color: #606266">库存：{{goods.store}}</span>
          </div>
          <div style="margin-left: 20px">
            <span style="font-size: 11px;color: #606266">累计热销：{{goods.sales}}</span>
          </div>
        </div>
        <div style="padding: 0 15px;margin-top: 20px">
          <span style="font-size: 11px;color: #606266">上架时间：{{goods.date}}</span>
        </div>
        <div style="display: flex;padding: 0 15px;margin-top: 15px;gap: 20px">
          <div>
            <el-input-number v-model="num" @change="handleChange" :min="1" :max="10" label="描述文字"></el-input-number>
          </div>
          <div>
            <el-button type="primary" style="width: 180px" @click="buy">立即购买</el-button>
          </div>
          <div>
            <el-button type="warning" style="width: 180px" @click="addCart">加入购物车</el-button>
          </div>
          <div>
            <el-button :type="isCollect ? 'danger' : 'warning'" style="width: 180px" @click="collect">{{isCollect ? '已收藏' : '收藏'}}</el-button>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 30px">
      <el-card>
        <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
          <el-tab-pane label="详细介绍" name="goods">
            <div class="w-e-text" v-html="goods.content"></div>
          </el-tab-pane>
          <el-tab-pane label="购买须知" name="notice">
            <div style="padding: 25px;">
              <h3 style="color: #333;margin: 15px 0;">购买说明</h3>
              <div>
                <div style="margin: 10px 0;color: #666;">1、正品保证</div>
                <div style="margin: 10px 0;color: #666;">2、7天无理由退货</div>
                <div style="margin: 10px 0;color: #666;">3、全国包邮</div>
                <div style="margin: 10px 0;color: #666;">4、售后无忧</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
import cart from '@/utils/cart'

export default {
  name: "GoodsDetail",
  data(){
    return{
      id: this.$route.query.id,
      goods: {},
      num: 1,
      activeName: 'goods',
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      isCollect: false
    }
  },
  created() {
    this.loadGoods()
  },
  methods:{
    loadGoods(){
      this.$request.get('/goods/selectById?id=' + this.id).then(res => {
        this.goods = res.data
        this.isCollect = this.goods.isCollect
      })
    },
    handleChange(value) {
      this.num = value;
      console.log(this.num)
    },
    handleClick(tab, event) {
      console.log(tab, event);
    },
    collect(){
      const data = {goodsId: this.goods.id}
      this.$request.post('/collect/add',data).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '成功', message: '收藏成功', showClose: false, duration: 2000});
          this.isCollect = true
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          this.isCollect = false
        }
        this.loadGoods()
      })
    },
    addCart(){
      if (!this.user.id){
        this.$notify.error({title: '错误', message: '请先登录', showClose: false, duration: 2000});
        this.$router.push('/login')
        return
      }
      // 购物车保存在本地，同一商品累加数量，不能超过库存
      const exist = cart.list(this.user.id).find(item => item.goodsId === this.goods.id)
      const total = (exist ? exist.nums : 0) + this.num
      if (this.goods.store == null || total > this.goods.store){
        this.$notify.error({title: '错误', message: this.goods.name + '商品库存不足', showClose: false, duration: 2000});
        return
      }
      cart.add(this.user.id, this.goods.id, this.num)
      this.$notify.success({title: '成功', message: '已加入购物车', showClose: false, duration: 2000});
      this.$emit('update:cart')   // 通知顶部刷新购物车角标
    },
    buy(){
      if (!this.user.id){
        this.$notify.error({title: '错误', message: '请先登录', showClose: false, duration: 2000});
        this.$router.push('/login')
        return
      }
      // 立即购买也走统一结算接口：金额由服务端按商品价格重算，库存原子扣减
      const items = [{goodsId: this.goods.id, nums: this.num}]
      this.$request.post('/orders/settle', items).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '成功', message: '下单成功，请尽快支付', showClose: false, duration: 2000});
          this.$router.push('/front/orders?state=待付款')
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    }
  }
}
</script>

<style scoped>

</style>