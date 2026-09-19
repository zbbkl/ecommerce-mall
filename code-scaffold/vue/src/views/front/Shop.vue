<template>
  <div style="margin: 10px auto;width: 70%;min-height: 90vh">
    <!-- 店铺头部 -->
    <el-card>
      <div style="display: flex; align-items: center">
        <el-image :src="shop.logo || defaultLogo" style="width: 80px; height: 80px; border-radius: 8px" fit="cover"></el-image>
        <div style="margin-left: 20px; flex: 1">
          <div style="font-size: 22px; font-weight: bold">{{ shop.shopName }}</div>
          <div style="font-size: 13px; color: #909399; margin-top: 6px">{{ shop.descr || '这家店主很懒，还没有写店铺简介~' }}</div>
        </div>
        <el-tag :type="shop.state === '已通过' ? 'success' : 'info'" size="medium">{{ shop.state }}</el-tag>
      </div>
    </el-card>

    <!-- 店铺商品 -->
    <el-card style="margin-top: 15px">
      <div slot="header" style="font-weight: bold">全部商品</div>
      <el-row :gutter="20" v-if="goods.length > 0">
        <el-col :span="6" v-for="item in goods" :key="item.id" style="margin-top: 10px; margin-bottom: 10px">
          <el-card :body-style="{ padding: '0px' }" class="card-item">
            <img :src="item.cover" alt="" style="width: 100%;height: 200px;object-fit: cover" @click="goPage('/front/goodsDetail?id='+item.id)">
            <div style="padding: 10px" @click="goPage('/front/goodsDetail?id='+item.id)">
              <div style="margin-top: 3px;font-size: 13px">{{ item.name }}</div>
              <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                {{ item.descr }}
              </div>
              <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                <div class="card-price"><span class="price-symbol">￥</span>{{ item.price }}</div>
                <div class="card-sales">已售 {{ item.sales }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else :image-size="200" description="店铺暂时没有上架商品"></el-empty>
      <div v-if="total > pageSize" style="margin-top: 10px; text-align: right">
        <el-pagination
            background
            layout="prev, pager, next"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "FrontShop",
  data() {
    return {
      shop: {},
      goods: [],
      pageNum: 1,
      pageSize: 12,
      total: 0,
      defaultLogo: require('@/assets/logo.svg')
    }
  },
  created() {
    this.merchantId = this.$route.query.id
    this.loadShop()
    this.loadGoods()
  },
  methods: {
    loadShop() {
      this.$request.get('/merchantShop/info', { params: { merchantId: this.merchantId } }).then(res => {
        if (res.code === '200') {
          this.shop = res.data || {}
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    loadGoods() {
      this.$request.get('/merchantShop/goods', {
        params: { merchantId: this.merchantId, pageNum: this.pageNum, pageSize: this.pageSize }
      }).then(res => {
        this.goods = res.data?.records || []
        this.total = res.data?.total || 0
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.loadGoods()
    },
    goPage(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.card-item {
  cursor: pointer;
  margin-bottom: 8px;
}
</style>
