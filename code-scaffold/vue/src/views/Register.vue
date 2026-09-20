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
            <el-option label="商户入驻" value="MERCHANT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="user.role === 'MERCHANT'" prop="shopName">
          <el-input v-model="user.shopName" size="medium" placeholder="请输入店铺名称" prefix-icon="el-icon-office-building"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" class="login-btn" @click="register">注 册</el-button>
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
  name: 'Register',
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
        confirmPass: '',
        role: 'USER',
        shopName: ''
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
        shopName: [
          { required: true, message: '请输入店铺名称', trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          // 商户入驻走 /merchant/register（提交后待平台审核），普通用户走 /register
          const isMerchant = this.user.role === 'MERCHANT'
          const url = isMerchant ? '/merchant/register' : '/register'
          const payload = isMerchant
              ? { username: this.user.username, password: this.user.password, shopName: this.user.shopName }
              : this.user
          this.$request.post(url, payload).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')
              this.$notify.success({
                title: '成功',
                message: isMerchant ? '入驻申请已提交，等待平台审核' : '注册成功',
                showClose: false,
                duration: 3000
              });
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

/* 品牌区：主色橙渐变（与登录页统一） */
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
  font-size: 40px;
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

/* 输入框聚焦态：主橙 */
.login-form ::v-deep .el-input__inner:focus {
  border-color: #ff6700;
  box-shadow: 0 0 0 2px rgba(255, 103, 0, 0.12);
}

/* 注册按钮：橙色渐变（原 darkseagreen 内联样式已移除） */
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
