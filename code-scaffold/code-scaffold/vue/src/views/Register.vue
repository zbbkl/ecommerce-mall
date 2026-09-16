<template>
  <div class="login-container">
    <div class="left-section">
      <h1 class="title">捞宝购物平台</h1>
      <p class="description">品类丰富的综合网上购物平台</p>
      <p class="descr">为用户提供商品浏览、在线购买、订单跟踪和账户管理的一站式服务</p>
      <img src="../assets/login.svg" alt="登录插画" class="illustration" />
    </div>
    <div class="right-section">
      <h1 class="welcome-title">欢迎注册</h1>
      <div class="login-type-wrapper">
        <p class="login-type">账号密码注册</p>
      </div>
      <el-form :model="user" :rules="rules" ref="registerRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="user.username" size="medium" placeholder="请输入账号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="user.password" size="medium" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" show-password></el-input>
        </el-form-item>
        <el-form-item prop="confirmPass">
          <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="请确认密码" v-model="user.confirmPass"></el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="user.role" size="medium" placeholder="请选择角色" style="width: 100%">
            <el-option label="用户" value="USER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" class="login-btn" @click="register" style="background-color: darkseagreen">注 册</el-button>
        </el-form-item>

        <div class="links">
          <div style="margin-left: 10px"><a href="/login">返回登录</a></div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: {
        username: '',
        password: '',
        confirmPass: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          this.$request.post('/register', this.user).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')
              this.$notify.success({title: '成功', message: '注册成功', showClose: false, duration: 2000});
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
  font-size: 40px;
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
