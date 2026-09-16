<template>
  <div>
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询店铺名称" v-model="shopName"></el-input>
        <el-select style="width: 140px;margin: 0 5px" placeholder="入驻状态" v-model="state" clearable>
          <el-option label="待审核" value="待审核"></el-option>
          <el-option label="已通过" value="已通过"></el-option>
          <el-option label="已驳回" value="已驳回"></el-option>
          <el-option label="已停用" value="已停用"></el-option>
        </el-select>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
        <el-table-column prop="shopName" label="店铺名称" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="username" label="登录账号" width="120"></el-table-column>
        <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
        <el-table-column prop="address" label="经营地址" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="营业执照" width="90" align="center">
          <template v-slot="scope">
            <el-image v-if="scope.row.license" style="width: 40px; height: 40px" :src="scope.row.license" fit="cover" :preview-src-list="[scope.row.license]"></el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态" width="90" align="center">
          <template v-slot="scope">
            <el-tag :type="stateTagType(scope.row.state)" size="small">{{ scope.row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button v-if="scope.row.state === '待审核'" size="mini" type="success" plain @click="audit(scope.row, '已通过')">通过</el-button>
            <el-button v-if="scope.row.state === '待审核'" size="mini" type="warning" plain @click="audit(scope.row, '已驳回')">驳回</el-button>
            <el-button v-if="scope.row.state === '已通过'" size="mini" type="danger" plain @click="audit(scope.row, '已停用')">停用</el-button>
            <el-button v-if="scope.row.state === '已停用'" size="mini" type="success" plain @click="audit(scope.row, '已通过')">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            background
            @current-change="handleCurrentChange"
            :current-page="pageNum"
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
  name: "ManagerMerchants",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      shopName: '',
      state: '',
      total: 0,
    }
  },
  created() {
    this.load()
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/admin/merchant/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          shopName: this.shopName,
          state: this.state,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    stateTagType(state) {
      switch (state) {
        case '已通过': return 'success'
        case '待审核': return 'warning'
        case '已驳回': return 'danger'
        default: return 'info'
      }
    },
    audit(row, state) {
      if (state === '已驳回') {
        this.$prompt('请输入驳回原因', '驳回申请', {type: 'warning'}).then(({ value }) => {
          this.doAudit(row.id, state, value)
        }).catch(() => {})
        return
      }
      const text = state === '已通过' ? '通过该商户的入驻申请' : '停用该商户账号'
      this.$confirm('确认' + text + '吗？', '确认操作', {type: 'warning'}).then(() => {
        this.doAudit(row.id, state, '')
      }).catch(() => {})
    },
    doAudit(id, state, rejectReason) {
      this.$request.put('/admin/merchant/audit', { id: id, state: state, rejectReason: rejectReason }).then(res => {
        if (res.code === '200') {
          this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
          this.load()
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    reset() {
      this.shopName = ''
      this.state = ''
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
  },
}
</script>

<style scoped>

</style>
