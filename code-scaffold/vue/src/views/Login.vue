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
            <el-option label="商户" value="MERCHANT"></el-option>
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
          // 三个角色三个登录入口：管理员走 admin 表，商户走 merchant 表，用户走 user 表
          const role = this.user.role
          const url = role === 'ADMIN' ? '/admin/login'
              : role === 'MERCHANT' ? '/merchant/login' : '/login'
          const payload = role === 'ADMIN'
              ? { username: this.user.username, password: this.user.password }
              : this.user
          this.$request.post(url, payload).then(res => {
            if (res.code === '200') {
              // /admin/login 返回 {token, admin}，铺平成与用户/商户登录一致的 {token, ...资料, role} 结构
              const data = res.data || {}
              const userInfo = role === 'ADMIN'
                  ? { ...data.admin, token: data.token, role: 'ADMIN' }
                  : { ...data, role: role }
              if (role === 'MERCHANT' && data.state === '已停用') {
                this.$notify.error({message: '账号已停用，请联系平台', showClose: false, duration: 2000});
                return
              }
              localStorage.setItem("user", JSON.stringify(userInfo))
              if (role === 'ADMIN') {
                this.$router.push('/')
              } else if (role === 'MERCHANT') {
                this.$router.push('/merchant/home')
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

/* 品牌区：主色橙渐变（与全站视觉统一） */
.left-section {
  flex: 6;
  background-image: linear-gradient(160deg, #ff9a3d 0%, #ff6700 45%, #e85500 100%);
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
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
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
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
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

/* 输入框聚焦态：element-ui 默认蓝改为主橙 */
.login-form ::v-deep .el-input__inner:focus {
  border-color: #ff6700;
  box-shadow: 0 0 0 2px rgba(255, 103, 0, 0.12);
}

/* 登录按钮：橙色渐变 + hover 上浮（与详情页 .buy-btn / 购物车 .settle-btn 同源） */
.login-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border: none;
  background-image: linear-gradient(to right, #ff8a2b, #ff6700 55%, #f25600);
  box-shadow: 0 6px 14px rgba(255, 103, 0, 0.35);
  transition: all 0.25s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 103, 0, 0.45);
  filter: brightness(1.05);
}

.login-btn:active {
  transform: scale(0.97);
}

.links {
  display: flex;
  justify-content: right;
  margin: 20px 0;
  font-size: 14px;
  color: #ff6700;
}

.links a {
  text-decoration: none;
  color: #ff6700;
}

.links a:hover {
  text-decoration: underline;
}
</style>
