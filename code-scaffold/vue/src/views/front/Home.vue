<template>
  <div class="homeContainer">
    <div class="carousel-margin">
      <div style="flex: 2;background-color: #606266;box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);height: 400px">
        <div v-for="(item,index) in types" :key="index" class="type-item" @click="goPage('/front/goods')">
          <span>{{item.name}}</span>
          <i class="el-icon-arrow-right type-arrow"></i>
        </div>
      </div>
      <div style="flex: 8">
        <el-carousel height="400px" :interval="10000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <img :src="item.cover" class="carousel-img"  @click="goPage('/front/goodsDetail?id='+item.goodsId)" style="cursor: pointer">
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <div style="margin-top: 30px">
      <div style="display: flex;justify-content: space-between;align-items: center">
        <div style="border-left: 5px solid #ff6700;padding-left: 7px;color:#303133; font-size: 12px;">
          <h1>新品上架</h1>
        </div>
        <div>
          <el-link href="/front/goods" :underline="false">查看更多>></el-link>
        </div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="(item,index) in timeGoods" :key="index" style="margin-top: 10px">
            <el-card :body-style="{ padding: '0px' }" class="card-item">
              <img :src="item.cover" alt="" style="width: 100%;height: 200px;object-fit: cover"@click="goPage('/front/goodsDetail?id='+item.id)">
              <div style="padding: 10px"@click="goPage('/front/goodsDetail?id='+item.id)">
                <div style="margin-top: 3px;font-size: 13px">
                  {{item.name}}
                </div>
                <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                  {{item.descr}}
                </div>
                <div style="margin-top: 5px;font-size: 11px">
                  <el-link type="primary" :underline="false" @click.stop="goPage('/front/shop?id=' + item.merchantId)">
                    <i class="el-icon-shop"></i> {{ item.merchantName || '平台自营' }}
                  </el-link>
                </div>
                <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                  <div class="card-price"><span class="price-symbol">￥</span>{{item.price}}</div>
                  <div class="card-sales">已售 {{item.sales}}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <div style="margin-top: 30px">
      <div style="display: flex;justify-content: space-between;align-items: center">
        <div style="border-left: 5px solid #ff6700;padding-left: 7px;color:#303133; font-size: 12px;">
          <h1>热销商品</h1>
        </div>
        <div>
          <el-link href="/front/goods" :underline="false">查看更多>></el-link>
        </div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="(item,index) in salesGoods" :key="index" style="margin-top: 10px">
            <el-card :body-style="{ padding: '0px' }" class="card-item">
              <img :src="item.cover" alt="" style="width: 100%;height: 200px;object-fit: cover" @click="goPage('/front/goodsDetail?id='+item.id)">
              <div style="padding: 10px" @click="goPage('/front/goodsDetail?id='+item.id)">
                <div style="margin-top: 3px;font-size: 13px">
                  {{item.name}}
                </div>
                <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                  {{item.descr}}
                </div>
                <div style="margin-top: 5px;font-size: 11px">
                  <el-link type="primary" :underline="false" @click.stop="goPage('/front/shop?id=' + item.merchantId)">
                    <i class="el-icon-shop"></i> {{ item.merchantName || '平台自营' }}
                  </el-link>
                </div>
                <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                  <div class="card-price"><span class="price-symbol">￥</span>{{item.price}}</div>
                  <div class="card-sales">已售 {{item.sales}}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      carousels: [],
      types: [],
      timeGoods: [],
      salesGoods: []
    }
  },
  created() {
    this.loadType()
    this.loadCarousel()
    this.loadTimeGoods()
    this.loadSaleGoods()
  },
  methods: {
    loadCarousel(){
      this.$request.get('/carousel/selectAll').then(res => {
        this.carousels = res.data
      })
    },
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = res.data
      })
    },
    loadTimeGoods(){
      this.$request.get('/goods/times').then(res => {
        this.timeGoods = res.data
      })
    },
    loadSaleGoods(){
      this.$request.get('/goods/sales').then(res => {
        this.salesGoods = res.data
      })
    },
    goPage(url){
      location.href = url
    }
  }
}
</script>

<style scoped>
.homeContainer{
  width: 70%;
  margin: 0 auto;
  min-height: 90vh;
}

.carousel-margin{
  margin: 10px 0;
  display: flex;
}

.carousel-img{
  width: 100%;
}

.type-item{
  padding: 0 30px;
  margin: 6px 10px;
  height: 33px;
  line-height: 33px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.type-item:hover{
  background-image: linear-gradient(90deg, #ff8a2b, #ff6700);
  padding-left: 34px;
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.35);
}

.type-arrow{
  color: #fff;
  transition: transform 0.25s ease;
}

.type-item:hover .type-arrow{
  transform: translateX(3px);
}

/* 卡片 hover 交互统一由 global.css 的 .card-item 提供 */
</style>
