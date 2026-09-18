<template>
  <div>
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
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询订单号" v-model="orderNo"></el-input>
        <el-button type="success" @click="load(1)">查询</el-button>
        <el-button type="info" @click="reset">重置</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="orderNo" label="订单号" :show-overflow-tooltip="true" width="170">
        </el-table-column>
        <el-table-column prop="name" label="商品" :show-overflow-tooltip="true" width="200">
          <template v-slot="scope">
            <el-link v-if="scope.row.goods" :href="'/front/goodsDetail?id=' + scope.row.goodsId" :underline="false">{{scope.row.name}}</el-link>
            <span v-else>{{scope.row.name}}</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="80">
          <template v-slot="scope">
            <el-image v-if="scope.row.goods && scope.row.goods.cover" style="width: 50px; height: 50px" :src="scope.row.goods.cover" fit="cover" :preview-src-list="[scope.row.goods.cover]"></el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="金额" width="90">
          <template v-slot="scope">￥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="nums" label="件数" width="60"></el-table-column>
        <el-table-column prop="user.name" label="买家" width="80"></el-table-column>
        <el-table-column prop="userPhone" label="收货电话" :show-overflow-tooltip="true" width="120"></el-table-column>
        <el-table-column prop="userAddress" label="收货地址" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="time" label="下单时间" :show-overflow-tooltip="true" width="150"></el-table-column>
        <el-table-column prop="state" label="状态" width="90">
          <template v-slot="scope">
            <el-tag :type="stateTagType(scope.row.state)" effect="dark" size="small">{{ scope.row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="170">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">详情</el-button>
            <el-button size="mini" type="primary" v-if="scope.row.state === '已支付'" @click="ship(scope.row)">发货</el-button>
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

    <!-- 订单详情抽屉 -->
    <el-drawer :visible.sync="detailVisible" title="订单详情" :with-header="false" size="40%">
      <div class="drawer-header">
        <span class="drawer-title">订单详情</span>
        <div class="drawer-actions">
          <el-button icon="el-icon-close" size="mini" circle @click="detailVisible = false"/>
        </div>
      </div>

      <div class="drawer-content">
        <el-descriptions :column="1" border size="small" style="margin-bottom: 15px">
          <el-descriptions-item label="订单号">{{ form.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ form.parentNo }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ form.user ? form.user.name : '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货电话">{{ form.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ form.userAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ form.time }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ form.state }}</el-descriptions-item>
        </el-descriptions>

        <div style="font-weight: bold; margin-bottom: 8px">商品明细</div>
        <el-table :data="form.items || []" stripe size="small">
          <el-table-column prop="goodsName" label="商品名称" :show-overflow-tooltip="true"></el-table-column>
          <el-table-column prop="price" label="单价" width="90">
            <template v-slot="scope">￥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column prop="nums" label="数量" width="70"></el-table-column>
          <el-table-column label="小计" width="100">
            <template v-slot="scope">￥{{ (scope.row.price * scope.row.nums).toFixed ? (scope.row.price * scope.row.nums).toFixed(2) : scope.row.price * scope.row.nums }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="drawer-footer">
        <el-button v-if="form.state === '已支付'" type="primary" @click="ship(form)">发 货</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "MerchantOrders",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      orderNo: '',
      state: '',
      total: 0,
      detailVisible: false,
      form: {},
    }
  },
  created() {
    this.load()
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/merchant/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          orderNo: this.orderNo,
          state: this.state,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    stateTagType(state) {
      switch (state) {
        case '待付款': return 'danger'
        case '已支付': return 'warning'
        case '已发货': return 'success'
        case '已完成': return 'success'
        default: return 'info'
      }
    },
    detail(row) {
      this.$request.get('/merchant/orders/detail', {params: {id: row.id}}).then(res => {
        if (res.code === '200') {
          this.form = res.data || {}
          this.detailVisible = true
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    ship(row) {
      this.$confirm('确认对该订单发货吗？', '确认发货', {type: "warning"}).then(() => {
        this.$request.post('/merchant/orders/ship?id=' + row.id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '发货成功', showClose: false, duration: 2000});
            this.detailVisible = false
            this.load()
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {})
    },
    reset() {
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
  },
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}
.drawer-title {
  font-weight: bold;
  font-size: 16px;
}
.drawer-content {
  padding: 15px 20px;
  overflow: auto;
}
.drawer-footer {
  padding: 10px 20px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}
</style>
