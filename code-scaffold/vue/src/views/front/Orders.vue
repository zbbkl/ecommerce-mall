<template>
  <div style="margin: 10px auto;width: 70%;min-height: 90vh">
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-radio-group v-model="state" size="small" @change="load(1)">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="待付款">待付款</el-radio-button>
          <el-radio-button label="已支付">已支付</el-radio-button>
          <el-radio-button label="已发货">已发货</el-radio-button>
          <el-radio-button label="已完成">已完成</el-radio-button>
          <el-radio-button label="已取消">已取消</el-radio-button>
        </el-radio-group>
      </div>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询商品名称" v-model="name"></el-input>
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询订单号" v-model="orderNo"></el-input>
        <el-button class="query-btn" @click="load(1)">查 询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="name" label="商品名称" :show-overflow-tooltip="true" width="200">
          <template v-slot="scope">
            <el-link :href="'/front/goodsDetail?id=' + scope.row.goodsId" :underline="false">{{scope.row.name}}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品封面" :show-overflow-tooltip="true">
          <template v-slot="scope">
            <el-image v-if="scope.row.goods && scope.row.goods.cover" style="width: 50px; height: 50px" :src="scope.row.goods.cover" fit="cover" :preview-src-list="[scope.row.goods.cover]"></el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户" :show-overflow-tooltip="true" width="120"></el-table-column>
        <el-table-column prop="orderNo" label="订单号" :show-overflow-tooltip="true" width="150">
        </el-table-column>
        <el-table-column prop="price" label="总价" width="50"></el-table-column>
        <el-table-column prop="nums" label="数量" width="50"></el-table-column>
        <el-table-column prop="user.name" label="收货人" width="70"></el-table-column>
        <el-table-column prop="userPhone" label="联系方式" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="userAddress" label="地址" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="time" label="购买时间" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="state" label="订单状态" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.state == '待付款'" class="state-tag tag-pending" effect="plain">待付款</el-tag>
            <el-tag v-else-if="scope.row.state == '已支付'" class="state-tag tag-paid" effect="plain">已支付</el-tag>
            <el-tag v-else-if="scope.row.state == '已发货'" class="state-tag tag-shipped" effect="plain">已发货</el-tag>
            <el-tag v-else-if="scope.row.state == '已完成'" class="state-tag tag-done" effect="plain">已完成</el-tag>
            <el-tag v-else-if="scope.row.state == '已取消'" class="state-tag tag-cancel" effect="plain">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="300">
          <template v-slot="scope">
            <el-button size="mini" type="warning" plain @click="cancel(scope.row)" v-if="scope.row.state == '待付款'">取消支付</el-button>
            <el-button size="mini" class="pay-btn" @click="pay(scope.row)" v-if="scope.row.state == '待付款'">支 付</el-button>
            <el-button size="mini" type="primary" plain @click="confirm(scope.row)" v-if="scope.row.state == '已发货'">确认收货</el-button>
            <el-button size="mini" type="primary" plain @click="payBatch(scope.row)" v-if="scope.row.state == '待付款' && scope.row.parentNo && isBatchPending(scope.row)">本批次合并支付</el-button>
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Type",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      name: '',
      orderNo: '',
      state: this.$route.query.state || '',   // 支持从主界面「待支付订单」入口带状态进来
      total: 0,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
    }
  },
  created() {
    this.load()
  },
  watch: {
    '$route.query.state'(state) {
      this.state = state || ''
      this.load(1)
    }
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          orderNo: this.orderNo,
          state: this.state,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    del(id) {
      this.$confirm('您确认删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/orders/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load(1)
            this.$emit('update:cart')
          } else {
            this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {
      })
    },
    reset() {
      this.name = ''
      this.orderNo = ''
      this.state = ''
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    isBatchPending(row) {
      // 同批次下还有其他待付款订单时才展示「合并支付」
      return this.tableData.some(item => item.parentNo === row.parentNo && item.state === '待付款' && item.id !== row.id)
    },
    cancel(row){
      this.$confirm('您确认取消该订单吗？取消后库存将回补。', '确认取消', {type: "warning"}).then(() => {
        this.$request.post('/orders/cancel', {id: row.id}).then(res => {
          if (res.code == '200'){
            this.$notify.success({title: '成功', message: '已取消支付', showClose: false, duration: 2000});
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
          this.load(1)
          this.$emit('update:cart')
        })
      }).catch(() => {})
    },
    pay(row){
      this.$request.post('/orders/pay', {id: row.id}).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '成功', message: '支付成功', showClose: false, duration: 2000});
          this.$emit('update:cart')
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
        this.load(1)
      })
    },
    confirm(row){
      this.$confirm('确认已收到该订单的商品吗？', '确认收货', {type: "warning"}).then(() => {
        this.$request.post('/orders/confirm', {id: row.id}).then(res => {
          if (res.code == '200'){
            this.$notify.success({title: '成功', message: '已确认收货', showClose: false, duration: 2000});
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
          this.load(1)
        })
      }).catch(() => {})
    },
    payBatch(row){
      this.$confirm('将合并支付该批次下全部待付款订单，确认支付吗？', '批次合并支付', {type: "warning"}).then(() => {
        this.$request.post('/orders/payBatch', {parentNo: row.parentNo}).then(res => {
          if (res.code == '200'){
            this.$notify.success({title: '成功', message: '支付成功', showClose: false, duration: 2000});
            this.$emit('update:cart')
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
          this.load(1)
        })
      }).catch(() => {})
    }
  },
}
</script>

<style scoped>
/* 查询按钮：渐变橙（全站主按钮统一风格） */
.query-btn {
  background-image: linear-gradient(135deg, #ff8a2b, #ff6700);
  border: none;
  color: #fff;
  transition: all 0.25s ease;
}

.query-btn:hover {
  filter: brightness(1.06);
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.35);
}

/* 支付按钮：渐变橙（mini） */
.pay-btn {
  background-image: linear-gradient(135deg, #ff8a2b, #ff6700);
  border: none;
  color: #fff;
  transition: all 0.25s ease;
}

.pay-btn:hover {
  filter: brightness(1.06);
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.35);
}

/* 订单状态标签：按状态语义配色，圆角胶囊 */
.state-tag {
  border-radius: 12px;
  font-weight: 600;
}

.tag-pending {
  background: #fff7f2;
  color: #ff6700;
  border-color: #ffc9a3;
}

.tag-paid {
  background: #f0f9eb;
  color: #67c23a;
  border-color: #c2e7b0;
}

.tag-shipped {
  background: #ecf5ff;
  color: #409eff;
  border-color: #b3d8ff;
}

.tag-done {
  background: #f0f9eb;
  color: #529b2e;
  border-color: #b3e19d;
}

.tag-cancel {
  background: #f4f4f5;
  color: #909399;
  border-color: #d3d4d6;
}
</style>
