<template>
  <AuthLayout
    heading="注册账号"
    description="普通用户注册后即可购物；商户注册后需要平台审核。"
    intro-title="先找到合适的，再慢慢回购"
    intro-text="收藏、购物车和订单会跟着账号保留，下一次回来不用重新找。"
  >
    <el-form
      ref="registerRef"
      class="auth-form"
      :model="user"
      :rules="rules"
      label-position="top"
      @submit.native.prevent="register"
    >
      <el-form-item label="账号" prop="username">
        <el-input
          v-model="user.username"
          autocomplete="username"
          placeholder="请输入账号"
          prefix-icon="el-icon-user"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="user.password"
          autocomplete="new-password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="el-icon-lock"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPass">
        <el-input
          v-model="user.confirmPass"
          autocomplete="new-password"
          type="password"
          placeholder="请再次输入密码"
          prefix-icon="el-icon-lock"
          show-password
        />
      </el-form-item>

      <el-form-item label="注册身份" prop="role">
        <el-select v-model="user.role" placeholder="请选择角色">
          <el-option label="普通用户" value="USER" />
          <el-option label="商户入驻" value="MERCHANT" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="user.role === 'MERCHANT'" label="店铺名称" prop="shopName">
        <el-input
          v-model="user.shopName"
          placeholder="请输入店铺名称"
          prefix-icon="el-icon-office-building"
        />
      </el-form-item>

      <el-button class="auth-submit" type="primary" native-type="submit">注册</el-button>

      <div class="form-links">
        <span>已经有账号？</span>
        <router-link to="/login">返回登录</router-link>
      </div>
    </el-form>
  </AuthLayout>
</template>

<script>
import AuthLayout from '@/conponents/AuthLayout.vue'

export default {
  name: 'Register',
  components: { AuthLayout },
  data() {
    const validatePassword = (rule, confirmPass, callback) => {
      if (!confirmPass) {
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
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        confirmPass: [{ validator: validatePassword, trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }]
      }
    }
  },
  methods: {
    register() {
      this.$refs.registerRef.validate(valid => {
        if (!valid) return

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
            })
          } else {
            this.$notify.error({ title: '注册失败', message: res.msg, showClose: false, duration: 2000 })
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.auth-form {
  margin-top: var(--space-8);
}

.auth-form :deep(.el-form-item__label) {
  color: var(--c-ink-body);
  font-weight: 500;
}

.auth-form :deep(.el-select),
.auth-form :deep(.el-input) {
  width: 100%;
}

.auth-submit {
  width: 100%;
  height: 44px;
  margin-top: var(--space-2);
  font-size: var(--text-body-size);
  font-weight: 600;
}

.form-links {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  margin-top: var(--space-5);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}
</style>
