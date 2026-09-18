<template>
  <div>
    <!-- 表格内容 -->
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px" placeholder="查询用户名" v-model="username"></el-input>
        <el-input style="width: 200px; margin: 0 5px" placeholder="查询姓名" v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
        <el-button type="primary" plain @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="序号" width="70" align="center">
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="手机号"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column label="操作" align="center" width="240">
          <template v-slot="scope">
            <el-button size="mini" type="success" plain @click="detail(scope.row)">详情</el-button>
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
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

    <!-- 新增 | 编辑弹框 -->
    <el-dialog title="信息" :visible.sync="fromVisible" width="30%">
      <el-form :model="form" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名" :disabled="!!form.id"></el-input>
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input show-password v-model="form.password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="姓名"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="邮箱"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="fromVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

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
          <el-form-item label="用户名" prop="username">
            <div class="form_title">{{form.username}}</div>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <div class="form_title">{{form.name}}</div>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <div class="form_title">{{form.phone}}</div>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <div class="form_title">{{form.email}}</div>
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
  name: "Admin",
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      username: '',
      name: '',
      total: 0,
      fromVisible: false,
      formDetailVisible: false,
      form: {},
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right',
      rules: {
        username: [
          {required: true, message: '请输入账号', trigger: 'blur'},
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'},
        ],
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    del(id) {
      this.$confirm('您确认删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/admin/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load(1)
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.formDetailVisible = true
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      delete this.form.password  // 编辑时不回显、不提交密码，保持原密码不变
      this.fromVisible = true
    },
    handleAdd() {
      this.form = {password: '123'}
      this.fromVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: '/admin',
            method: this.form.id ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '保存成功', showClose: false, duration: 2000});
              this.load(1)
              this.fromVisible = false
            } else {
              this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    reset() {
      this.name = ''
      this.username = ''
      this.load()
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/admin/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
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
      // 如果用户通过ESC键退出全屏，同步更新状态
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        // 保存当前抽屉状态以便退出全屏时恢复
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;

        // 进入全屏模式
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';

        // 延迟执行全屏请求，确保DOM已更新
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        // 退出全屏模式
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;

        this.exitFullscreen();
      }
    },
    // 全屏API相关方法
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
  // 监听全屏状态变化
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
