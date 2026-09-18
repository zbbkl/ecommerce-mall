<template>
  <div>
    <el-card style="width: 50%">
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="user.username" placeholder="用户名" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="user.name" placeholder="姓名"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="user.phone" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="user.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">保 存</el-button>
          <el-button type="success" @click="formDetailVisible = true">修改密码</el-button>
        </div>
      </el-form>
    </el-card>

    <el-drawer :visible.sync="formDetailVisible" title="修改密码" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">修改密码</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <div class="drawer-content" ref="drawerContent">
        <el-form ref="formRef" :model="passForm" :rules="rules" label-width="80px" style="padding-right: 40px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="passForm.username" placeholder="用户名" disabled></el-input>
          </el-form-item>
          <el-form-item label="原始密码" prop="password">
            <el-input show-password v-model="passForm.password" placeholder="原始密码"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input show-password v-model="passForm.newPassword" placeholder="新密码"></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input show-password v-model="passForm.confirmPassword" placeholder="确认密码"></el-input>
          </el-form-item>
        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button type="primary" @click="updatePassword">确认修改</el-button>
        <el-button @click="formDetailVisible = false">关闭</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "Person",
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入确认密码'));
      } else if (value !== this.passForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    const validateNewPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入新密码'));
      } else if (value.length < 8) {
        callback(new Error('密码长度不能少于8位'));
      } else if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
        callback(new Error('密码必须包含字母和数字'));
      } else if (value === this.passForm.password) {
        callback(new Error('新密码不能与原始密码相同'));
      } else {
        callback();
      }
    };
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      passForm: {},
      rules: {
        password: [
          { required: true, message: '请输入原始密码', trigger: 'blur' }
        ],
        newPassword: [
          { validator: validateNewPassword, required: true, trigger: 'blur' }
        ],
        confirmPassword: [
          { validator: validateConfirmPassword, required: true, trigger: 'blur' }
        ]
      },
      formDetailVisible: false,
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {},
  methods: {
    updatePassword() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.post('/admin/password', this.passForm).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '保存成功', showClose: false, duration: 2000});
              this.$router.push('/login')
            } else {
              this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    update() {
      // 只提交资料字段，不动密码
      const data = {
        id: this.user.id,
        username: this.user.username,
        name: this.user.name,
        phone: this.user.phone,
        email: this.user.email
      }
      this.$request.put('/admin', data).then(res => {
        if (res.code === '200') {
          this.$notify.success({title: '成功', message: '保存成功', showClose: false, duration: 2000});
          localStorage.setItem('user', JSON.stringify({ ...this.user, ...data }))
          // 触发父级的数据更新
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
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
/deep/.el-form-item__label {
  font-weight: bold;
}
</style>
