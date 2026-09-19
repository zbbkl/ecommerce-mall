<template>
  <div style="width: 62%;min-height: 90vh;margin: 20px auto">
    <div class="detail-card">
      <div class="detail-left">
        <el-image v-if="goods.cover" class="detail-img" :src="goods.cover" fit="cover" :preview-src-list="[goods.cover]"></el-image>
      </div>
      <div class="detail-right">
        <b class="detail-name">{{goods.name}}</b>
        <div style="margin-top: 8px;">
          <el-link type="primary" :underline="false" @click="$router.push('/front/shop?id=' + goods.merchantId)">
            <i class="el-icon-shop"></i> {{ goods.merchantName || '平台自营' }} <span style="color:#909399">进店 ></span>
          </el-link>
          <p class="detail-descr">{{goods.descr}}</p>
        </div>
        <div class="price-panel">
          <span class="price-symbol">￥</span><span class="price-value">{{goods.price}}</span>
          <span class="price-tag">库存 {{goods.store}}</span>
          <span class="price-tag">累计热销 {{goods.sales}}</span>
        </div>
        <div class="detail-meta">上架时间：{{goods.date}}</div>
        <div class="action-row">
          <el-input-number v-model="num" @change="handleChange" :min="1" :max="10" label="描述文字"></el-input-number>
          <button class="buy-btn" @click="buy">
            <span>立即购买</span>
          </button>
          <!-- 加入购物车：改编自 Uiverse.io by vinodjangid07（galaxy/Buttons/brave-goose-29） -->
          <button class="CartBtn" @click="addCart">
            <span class="IconContainer">
              <svg xmlns="http://www.w3.org/2000/svg" height="1em" viewBox="0 0 576 512" class="cart"><path d="M0 24C0 10.7 10.7 0 24 0H69.5c22 0 41.5 12.8 50.6 32h411c26.3 0 45.5 25 38.6 50.4l-41 152.3c-8.5 31.4-37 53.3-69.5 53.3H170.7l5.4 28.5c2.2 11.3 12.1 19.5 23.6 19.5H488c13.3 0 24 10.7 24 24s-10.7 24-24 24H199.7c-34.6 0-64.3-24.6-70.7-58.5L77.4 54.5c-.7-3.8-4-6.5-7.9-6.5H24C10.7 48 0 37.3 0 24zM128 464a48 48 0 1 1 96 0 48 48 0 1 1 -96 0zm336-48a48 48 0 1 1 0 96 48 48 0 1 1 0-96z"/></svg>
            </span>
            <p class="text">{{ added ? '已加入 ✓' : '加入购物车' }}</p>
          </button>
          <el-button round :type="isCollect ? 'danger' : 'warning'" plain icon="el-icon-star-off" @click="collect">{{isCollect ? '已收藏' : '收藏'}}</el-button>
        </div>
      </div>
    </div>

    <div style="margin-top: 24px">
      <el-card>
        <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
          <el-tab-pane label="详细介绍" name="goods">
            <div class="w-e-text" v-html="sanitizeHtml(goods.content)"></div>
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
import { sanitizeHtml } from '@/utils/sanitize'

export default {
  name: "GoodsDetail",
  data(){
    return{
      id: this.$route.query.id,
      goods: {},
      num: 1,
      activeName: 'goods',
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      isCollect: false,
      added: false
    }
  },
  created() {
    this.loadGoods()
  },
  methods:{
    sanitizeHtml,
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
      this.added = true
      setTimeout(() => { this.added = false }, 1500)
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
/* ============ 详情页卡片布局 ============ */
.detail-card {
  display: flex;
  gap: 28px;
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.detail-left {
  flex: 4;
  min-width: 0;
}

.detail-img {
  width: 100%;
  height: 320px;
  border-radius: 12px;
}

.detail-right {
  flex: 6;
  min-width: 0;
  padding-top: 4px;
}

.detail-name {
  font-size: 22px;
  color: #303133;
  line-height: 1.4;
}

.detail-descr {
  font-size: 12px;
  color: #909399;
  line-height: 18px;
  margin-top: 6px;
}

/* 价格面板：浅橙渐变底（galaxy 卡片渐变思路的低饱和版本） */
.price-panel {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 18px;
  padding: 14px 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff7f0, #ffefdf);
}

.price-symbol {
  font-size: 16px;
  color: #ff6700;
  font-weight: bold;
}

.price-value {
  font-size: 30px;
  color: #ff6700;
  font-weight: bold;
  letter-spacing: -0.5px;
}

.price-tag {
  font-size: 12px;
  color: #a2673f;
  background: rgba(255, 103, 0, 0.1);
  border-radius: 20px;
  padding: 3px 10px;
}

.detail-meta {
  margin-top: 14px;
  font-size: 12px;
  color: #909399;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 22px;
  flex-wrap: wrap;
}

/* 立即购买：橙色渐变 + hover 上浮（galaxy 渐变按钮风格） */
.buy-btn {
  width: 150px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background-image: linear-gradient(to right, #ff8a2b, #ff6700 55%, #f25600);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(255, 103, 0, 0.35);
  transition: all 0.25s ease;
}

.buy-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 103, 0, 0.45);
  filter: brightness(1.05);
}

.buy-btn:active {
  transform: scale(0.96);
}

/* 加入购物车：改编自 Uiverse.io by vinodjangid07（galaxy/Buttons/brave-goose-29）
   主题色改为本站橙，购物车图标 hover 时从左滑入 */
.CartBtn {
  width: 150px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background-color: #fff0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition-duration: 0.5s;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.12);
  position: relative;
}

.CartBtn .IconContainer {
  position: absolute;
  left: -45px;
  width: 30px;
  height: 30px;
  background-color: #ff6700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  z-index: 2;
  transition-duration: 0.5s;
}

.CartBtn .cart {
  color: #fff;
  font-size: 15px;
}

.CartBtn .cart path {
  fill: #fff;
}

.CartBtn .text {
  height: 100%;
  width: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6700;
  z-index: 1;
  transition-duration: 0.5s;
  font-size: 14px;
  font-weight: 600;
}

.CartBtn:hover .IconContainer {
  transform: translateX(58px);
  border-radius: 40px;
  transition-duration: 0.5s;
}

.CartBtn:hover .text {
  transform: translate(10px, 0);
  transition-duration: 0.5s;
}

.CartBtn:active {
  transform: scale(0.95);
  transition-duration: 0.5s;
}
</style>