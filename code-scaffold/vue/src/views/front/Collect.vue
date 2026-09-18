<template>
  <div style="margin: 10px auto;min-height: 90vh;width: 70%">
    <!--顶部+搜索框-->
    <div style="display: flex;justify-content: space-between;align-items: center">
      <div>
        <h1 style="border-left: 5px solid #ff6700;padding-left: 7px;font-size: 22px;color:#303133;">我的收藏</h1>
      </div>
    </div>

    <div>
      <el-row :gutter="20" v-if="collects.length > 0">
        <el-col :span="6" v-for="(item,index) in collects" :key="index" style="margin-top: 10px">
          <el-card :body-style="{ padding: '0px' }" class="card-item">
            <img :src="item.goods.cover" alt="" style="width: 100%;height: 200px" @click="goPage('/front/goodsDetail?id=' + item.goodsId)">
            <div style="padding: 10px" >
              <div style="margin-top: 3px;font-size: 13px" @click="goPage('/front/goodsDetail?id=' + item.goodsId)">
                {{item.goods.name}}
              </div>
              <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" @click="goPage('/front/goodsDetail?id=' + item.goodsId)">
                {{item.goods.descr}}
              </div>
              <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                <div style="font-size: 20px;color: #FFA500;font-weight: 600">
                  ￥{{item.goods.price}}
                </div>

                <div>
                  <el-button type="text" style="color: orangered" @click="del(item)">取消收藏</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="collects.length == 0">
      <el-empty :image-size="300" :image="require('@/assets/empty.svg')" description="您还未收藏哟~"></el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: "Goods",
  data(){
    return{
      collects: [],
    }
  },
  created() {
    this.loadCollect()
  },
  methods:{
    loadCollect(){
      this.$request.get('/collect/myCollect').then(res => {
        this.collects = res.data
      })
    },
    goPage(url){
      location.href = url
    },
    del(row){
      this.$request.delete('/collect/delete?id=' + row.id).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '成功', message: '已取消收藏', showClose: false, duration: 2000});
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
        this.loadCollect()
      })
    }
  }
}
</script>

<style scoped>
.card-item:hover{
  cursor: pointer;
  transform: scale(1.03);
}
</style>