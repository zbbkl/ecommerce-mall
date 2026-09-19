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
                <div style="font-size: 20px;color: #FFA500;font-weight: 600">
                  ￥{{item.price}}
                </div>
                <div style="font-size:11px;color: #909399;">
                  累计热销：{{item.sales}}
                </div>
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
.search-input{
  padding: 14px 24px;
  outline: none;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-size: 10px;
}

.search-button{
  padding: 14px 24px;
  background: #ff6700;
  border: none;
}

/* 分类标签组容器（可选，优化间距） */
.type-group {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

/* 选中状态样式 */
.type-selected {
  background-color: #ff6700 !important;
  color: #fff !important;
}

/* 未选中状态 hover效果 */
.type-group .el-button--primary:not(.type-selected):hover {
  background-color: #ff6700 !important;
  color: #fff !important;
}

/* 重置 ElementUI 主按钮默认样式 */
.type-group .el-button--primary {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

/* 卡片 hover 交互统一由 global.css 的 .card-item 提供 */
</style>
