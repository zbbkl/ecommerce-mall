<template>
  <div style="margin: 10px auto;min-height: 90vh;width: 70%">
    <!--顶部+搜索框-->
    <div style="display: flex;justify-content: space-between;align-items: center">
      <div>
        <h1 style="border-left: 5px solid #ff6700;padding-left: 7px;font-size: 22px;color:#303133;">热卖商品</h1>
      </div>
      <div>
        <input v-model='keyboard' type="text" placeholder="请输入搜索商品名称" class="search-input" @keyup.enter="loadGoods"/>
        <el-button class="search-button" @click="loadGoods">
          <i class="search-icon">🔍</i>
        </el-button>
      </div>
    </div>

    <!--分类按钮-->
    <div style="margin-top: 15px">
      <div class="type-group">
        <el-button type="primary" :class="{ 'type-selected': selectedCategoryId === 0 }" @click="handleAllClick">全部</el-button>
        <el-button type="primary" v-for="(category,index) in types" :key="index" :class="{ 'type-selected': selectedCategoryId === category.id }" @click="handleCategoryClick(category)">
          {{ category.name }}
        </el-button>
      </div>
    </div>
    <div>
      <el-row :gutter="20" v-if="goods.length > 0">
        <el-col :span="6" v-for="(item,index) in goods" :key="index" style="margin-top: 10px">
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
      <div v-if="total > 0" style="margin-top: 20px; text-align: right;">
        <el-pagination
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[8, 16, 32]"
            :page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
            background
        ></el-pagination>
      </div>
    </div>

    <div v-if="goods.length == 0">
      <el-empty :image-size="300" :image="require('@/assets/empty.svg')" description="没有商品哟~"></el-empty>    </div>
  </div>
</template>

<script>
export default {
  name: "Goods",
  data(){
    return{
      types: [],
      selectedCategoryId: parseInt(this.$route.query.selectedCategoryId) || 0,
      total: 0,
      pageNum: 1,
      pageSize: 8,
      keyboard: '',
      goods: [],
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  methods:{
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = res.data
      })
    },
    loadGoods(){
      this.$request.get("/goods/selectPage/type", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.keyboard,
          typeId: this.selectedCategoryId
        }
      }).then(res => {
        this.goods = res.data?.records
        this.total = res.data?.total
      })
    },
    handleAllClick() {
      this.selectedCategoryId = 0;
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: 0 }
      })
      this.loadGoods()
    },
    handleCategoryClick(category) {
      this.selectedCategoryId = category.id;
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: category.id }
      })
      this.loadGoods()
    },
    handleCurrentChange(pageNum){
      this.pageNum = pageNum;
      this.loadGoods()
    },
    goPage(url){
      location.href=url
    }
  }
}
</script>

<style scoped>
/* 搜索框：圆角胶囊 + 聚焦橙色描边 */
.search-input{
  width: 260px;
  padding: 11px 18px;
  outline: none;
  border: 2px solid transparent;
  border-radius: 10px 0 0 10px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  font-size: 13px;
  color: #303133;
  transition: all 0.25s ease;
}

.search-input::placeholder{
  color: #c0c4cc;
}

.search-input:focus{
  border-color: #ff6700;
  box-shadow: 0 4px 12px rgba(255, 103, 0, 0.18);
}

/* 搜索按钮：与输入框拼接的渐变块 */
.search-button{
  padding: 12px 20px;
  background-image: linear-gradient(135deg, #ff8a2b, #ff6700);
  border: none;
  border-radius: 0 10px 10px 0;
  font-size: 15px;
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.3);
  transition: all 0.25s ease;
}

.search-button:hover{
  filter: brightness(1.06);
  box-shadow: 0 6px 14px rgba(255, 103, 0, 0.4);
}

/* 分类标签组容器 */
.type-group {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

/* 分类胶囊：选中态橙色渐变（与详情页/购物车主按钮同源） */
.type-group .el-button{
  border-radius: 18px;
  transition: all 0.25s ease;
}

.type-selected {
  background-image: linear-gradient(135deg, #ff8a2b, #ff6700) !important;
  border-color: #ff6700 !important;
  color: #fff !important;
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.35);
}

/* 未选中状态 hover效果 */
.type-group .el-button--primary:not(.type-selected):hover {
  background-color: #fff7f2 !important;
  border-color: #ff8a2b !important;
  color: #ff6700 !important;
  transform: translateY(-1px);
}

/* 重置 ElementUI 主按钮默认样式 */
.type-group .el-button--primary {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

/* 卡片 hover 交互统一由 global.css 的 .card-item 提供 */
</style>
