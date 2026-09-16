<template>
  <div>
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询商品名称" v-model="name"></el-input>
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询订单号" v-model="orderNo"></el-input>
        <el-button type="success" plain @click="load(1)">查询</el-button>
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
            <el-image v-if="scope.row.goods.cover" style="width: 50px; height: 50px" :src="scope.row.goods.cover" fit="cover" :preview-src-list="[scope.row.goods.cover]"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" :show-overflow-tooltip="true" width="150">
        </el-table-column>
        <el-table-column prop="price" label="总价" width="50"></el-table-column>
        <el-table-column prop="nums" label="数量" width="50"></el-table-column>
        <el-table-column prop="userName" label="姓名"></el-table-column>
        <el-table-column prop="userPhone" label="联系方式" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="userAddress" label="地址" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="time" label="购买时间" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column prop="state" label="订单状态"></el-table-column>
        <el-table-column prop="user.name" label="用户" width="50"></el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">详情</el-button>
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

    <!-- 详情内容 -->
    <el-drawer :visible.sync="formDetailVisible" title="详情" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">详情</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <!-- 抽屉内容 -->
      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item label="商品名称" prop="name">
            <div>{{form.name}}</div>
          </el-form-item>
          <el-form-item label="商品封面" >
            <div>
              <el-image v-if="form.goods?.cover" style="width: 50px; height: 50px" :src="form.goods?.cover" fit="cover" :preview-src-list="[form.goods?.cover]"></el-image>
            </div>
          </el-form-item>
          <el-form-item label="订单号" prop="name">
            <div>{{form.orderNo}}</div>
          </el-form-item>
          <el-form-item label="总价" prop="name">
            <div>{{form.price}}</div>
          </el-form-item>
          <el-form-item label="数量" prop="name">
            <div>{{form.nums}}</div>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <div>{{form.userName}}</div>
          </el-form-item>
          <el-form-item label="联系方式" prop="name">
            <div>{{form.userPhone}}</div>
          </el-form-item>
          <el-form-item label="地址" prop="name">
            <div>{{form.userAddress}}</div>
          </el-form-item>
          <el-form-item label="购买时间" prop="name">
            <div>{{form.time}}</div>
          </el-form-item>
          <el-form-item label="订单状态" prop="name">
            <div>{{form.state}}</div>
          </el-form-item>
          <el-form-item label="用户" >
            <div>{{form.user?.name}}</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 抽屉底部 -->
      <div class="drawer-footer">
        <el-button @click="formDetailVisible = false">关闭</el-button>
      </div>
    </el-drawer>
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
      total: 0,
      fromVisible: false,
      formDetailVisible: false,
      form: {},
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      rules: {
        username: [
          {required: true, message: '请输入分类名称', trigger: 'blur'},
        ]
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {
    this.load()
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          orderNo: this.orderNo
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formDetailVisible = true
    },
    del(id) {
      this.$confirm('您确认删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/orders/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load(1)
          } else {
            this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {
      })
    },
    reset() {
      this.name = ''
      this.username = ''
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleFullscreenChange() {
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
        this.exitFullscreen();
      }
    },
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>

</style>
