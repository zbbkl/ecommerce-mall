<template>
  <div class="login-container">
    <div class="left-section">
      <h1 class="title">捞宝购物平台</h1>
      <p class="description">品类丰富的综合网上购物平台</p>
      <p class="descr">为用户提供商品浏览、在线购买、订单跟踪和账户管理的一站式服务</p>
      <img src="../assets/login.svg" alt="登录插画" class="illustration" />
    </div>
    <div class="right-section">
      <h1 class="welcome-title">欢迎回来</h1>
      <div class="login-type-wrapper">
        <p class="login-type">账号密码登录</p>
      </div>
      <el-form :model="user" :rules="rules" ref="loginRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="user.username" size="medium" placeholder="请输入账号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="user.password" size="medium" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="user.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="用户" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="login">登录</el-button>
        </el-form-item>

        <div class="links">
          <div style="margin-left: 10px"><a href="/register">注册账号</a></div>
        </div>
      </el-form>
    </div>

    <el-dialog title="忘记密码" :visible.sync="forgetPassDialogVis" width="30%">
      <el-form :model="forgetUserForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="请输入手机号"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">取 消</el-button>
        <el-button type="primary" @click="resetPassword">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      forgetUserForm: {},
      forgetPassDialogVis: false,
      user: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    handleForgetPass() {
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    resetPassword() {
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('重置成功')
          this.forgetPassDialogVis = false
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          // 管理员账号在 admin 表中维护，走独立登录接口；普通用户仍走 /login（user 表）
          const isAdmin = this.user.role === 'ADMIN'
          const url = isAdmin ? '/admin/login' : '/login'
          const payload = isAdmin
              ? { username: this.user.username, password: this.user.password }
              : this.user
          this.$request.post(url, payload).then(res => {
            if (res.code === '200') {
              // /admin/login 返回 {token, admin}，铺平成与用户登录一致的 {token, ...资料, role} 结构
              const data = res.data || {}
              const userInfo = isAdmin
                  ? { ...data.admin, token: data.token, role: 'ADMIN' }
                  : data
              localStorage.setItem("user", JSON.stringify(userInfo))
              if (isAdmin) {
                this.$router.push('/')
              } else {
                this.$router.push('/front/home')
              }
              this.$notify.success({title: '成功', message: '登录成功', showClose: false, duration: 2000});
            } else {
              this.$notify.error({message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
}

.left-section {
  flex: 6;
  background-color: #1f2937;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.title {
  font-size:40px;
  font-weight: bold;
  margin-bottom: 20px;
}

.description {
  font-size: 20px;
  margin-bottom: 20px;
  text-align: center;
}

.descr {
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
}

.illustration {
  width: 400px;
  height: auto;
  border-radius: 0;
}

.right-section {
  flex: 4;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #fff;
}

.welcome-title {
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #1e293b;
}

.login-type-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  width: 200px;
}

.login-type-wrapper::before {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #e2e8f0;
  margin-right: 10px;
}

.login-type-wrapper::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #e2e8f0;
  margin-left: 10px;
}

.login-type {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
}

.login-form {
  width: 350px;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
}

.links {
  display: flex;
  justify-content: right;
  margin: 20px 0;
  font-size: 14px;
  color: #409eff;
}

.links a {
  text-decoration: none;
  color: #409eff;
}

.links a:hover {
  text-decoration: underline;
}
</style>
