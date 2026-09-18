<template>
  <div>
    <el-card style="width: 50%">
      <el-form :model="user" label-width="80px" style="padding-right: 20px">
        <el-form-item label="登录账号">
          <el-input v-model="user.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="店铺名称">
          <el-input v-model="user.shopName" disabled></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="user.phone" disabled></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="success" @click="formDetailVisible = true">修改密码</el-button>
        </div>
      </el-form>
    </el-card>

    <el-drawer :visible.sync="formDetailVisible" title="修改密码" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">修改密码</span>
        <div class="drawer-actions">
          <el-button icon="el-icon-close" size="mini" circle @click="formDetailVisible = false"/>
        </div>
      </div>

      <div class="drawer-content">
        <el-form ref="formRef" :model="passForm" :rules="rules" label-width="80px" style="padding-right: 40px">
          <el-form-item label="账号" prop="username">
            <el-input v-model="passForm.username" disabled></el-input>
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
  name: "MerchantPerson",
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
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'));
      } else if (value === this.passForm.password) {
        callback(new Error('新密码不能与原始密码相同'));
      } else {
        callback();
      }
    };
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
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
    }
  },
  created() {
    this.passForm = { username: this.user.username }
  },
  methods: {
    updatePassword() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request.post('/merchant/password', this.passForm).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '密码已修改，请重新登录', showClose: false, duration: 2000});
              this.formDetailVisible = false
              localStorage.removeItem('user')
              this.$router.push('/login')
            } else {
              this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>
/deep/.el-form-item__label {
  font-weight: bold;
}
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
