<template>
  <div>
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px;margin: 0 5px" placeholder="查询..." v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
        <el-button type="primary" plain @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="序号" width="70" align="center" >
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="name" label="名称" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="cover" label="图片">
          <template slot-scope="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.cover" :preview-src-list="[scope.row.cover]"></el-image>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="goodsName" label="商品名称" :show-overflow-tooltip="true"></el-table-column>

        <el-table-column label="操作" width="240" align="center">
          <template slot-scope="scope">
            <el-button plain type="success" @click="detail(scope.row)" size="mini">详情</el-button>
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">编辑</el-button>
            <el-button plain type="danger" size="mini" @click=del(scope.row.id)>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            style="padding: 0"
            background
            layout="total, prev, pager, next"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="信息" :visible.sync="dialogFormVisible" width="30%" :close-on-click-modal="false">
      <el-form label-width="100px" style="padding-right: 40px" :model="form" :rules="rules" ref="ruleForm">
        <el-form-item prop="name" label="名称">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="cover" label="封面">
          <el-upload :action="$baseUrl +'/file/upload'" :headers="{ token: user.token }" ref="cover" :on-success="handleImgUploadSuccess">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="goodsId" label="商品">
          <el-select v-model="form.goodsId" placeholder="请选择分类">
            <el-option v-for="item in goods" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

    <el-drawer :visible.sync="drawerFormVisible" title="详情" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">详情</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="drawerFormVisible = false"/>
        </div>
      </div>

      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item prop="name" label="名称">
            <div>{{form.name}}</div>
          </el-form-item>
          <el-form-item prop="cover" label="封面">
            <div>
              <el-image style="width: 50px; height: 50px" :src="form.cover" :preview-src-list="[form.cover]"></el-image>
            </div>
          </el-form-item>
          <el-form-item prop="goodsId" label="商品名称">
            <div>{{form.goodsName}}</div>
          </el-form-item>

        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button @click="drawerFormVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "Carousel",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      form: {},
      dialogFormVisible: false,
      drawerFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        name: [
          { required: true, message: '请输入必填项', trigger: 'blur'}
        ],
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right',
      goods: []
    }
  },
  created() {
    this.loadGoods()
    this.load()
  },
  methods: {
    load() {
      this.$request.get("/carousel/selectPage", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    loadGoods(){
      this.$request.get('/goods/selectAll').then(res => {
        this.goods = res.data
      })
    },
    save() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          this.$request({
            method: this.form.id ? 'PUT' : 'POST',
            url: this.form.id ? '/carousel/update' : '/carousel/add',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
              this.dialogFormVisible = false
              this.load()
            } else {
              this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
      this.$nextTick(() => {
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => {
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    del(id) {
      this.$confirm('您确认删除这些数据吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete("/carousel/delete?id=" + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load()
          } else {
            this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {})
    },
    reset() {
      this.name = ""
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleImgUploadSuccess(res) {
      this.form.cover = res.data
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