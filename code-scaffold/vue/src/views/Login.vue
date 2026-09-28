<template>
  <AuthLayout
    heading="登录"
    description="登录后可管理购物车、订单和账户信息。"
    intro-title="给毛孩子挑好日常"
    intro-text="商品、订单和账户信息放在清楚的位置，需要时直接处理。"
  >
    <el-form
      ref="loginRef"
      class="auth-form"
      :model="user"
      :rules="rules"
      label-position="top"
      @submit.native.prevent="login"
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
          autocomplete="current-password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="el-icon-lock"
          show-password
        />
      </el-form-item>

      <el-form-item label="登录身份" prop="role">
        <el-select v-model="user.role" placeholder="请选择角色">
          <el-option label="普通用户" value="USER" />
          <el-option label="商户" value="MERCHANT" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>

      <el-button class="auth-submit" type="primary" native-type="submit">登录</el-button>

      <div class="form-links">
        <button type="button" @click="handleForgetPass">忘记密码</button>
        <router-link to="/register">没有账号？注册</router-link>
      </div>
    </el-form>

    <el-dialog
      title="忘记密码"
      :visible.sync="forgetPassDialogVis"
      width="min(92vw, 420px)"
      :close-on-click-modal="false"
    >
      <el-form :model="forgetUserForm" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="forgetUserForm.username" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="forgetUserForm.phone" type="tel" inputmode="tel" autocomplete="tel" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确认重置</el-button>
      </div>
    </el-dialog>
  </AuthLayout>
</template>

<script>
import AuthLayout from '@/conponents/AuthLayout.vue'

export default {
  name: 'Login',
  components: { AuthLayout },
  data() {
    return {
      forgetUserForm: {},
      forgetPassDialogVis: false,
      user: {
        username: '',
        password: '',
        role: 'USER'
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
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
          this.$message.success('密码已重置')
          this.forgetPassDialogVis = false
        } else {
          this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
        }
      })
    },
    login() {
      this.$refs.loginRef.validate(valid => {
        if (!valid) return

        const role = this.user.role
        const url = role === 'ADMIN' ? '/admin/login'
          : role === 'MERCHANT' ? '/merchant/login'
            : '/login'
        const payload = role === 'ADMIN'
          ? { username: this.user.username, password: this.user.password }
          : this.user

        this.$request.post(url, payload).then(res => {
          if (res.code !== '200') {
            this.$notify.error({ title: '登录失败', message: res.msg, showClose: false, duration: 2000 })
            return
          }

          const data = res.data || {}
          const userInfo = role === 'ADMIN'
            ? { ...data.admin, token: data.token, role: 'ADMIN' }
            : { ...data, role }
          if (role === 'MERCHANT' && data.state === '已停用') {
            this.$notify.error({ title: '无法登录', message: '账号已停用，请联系平台', showClose: false, duration: 2000 })
            return
          }

          localStorage.setItem('user', JSON.stringify(userInfo))
          if (role === 'ADMIN') this.$router.push('/')
          else if (role === 'MERCHANT') this.$router.push('/merchant/home')
          else this.$router.push('/front/home')
          this.$notify.success({ title: '成功', message: '登录成功', showClose: false, duration: 2000 })
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
  justify-content: space-between;
  gap: var(--space-4);
  margin-top: var(--space-5);
  font-size: var(--text-caption-size);
}

.form-links button {
  padding: 0;
  background: transparent;
  border: 0;
  color: var(--c-ink-muted);
  cursor: pointer;
}

.form-links button:hover {
  color: var(--c-brand);
}
</style>
