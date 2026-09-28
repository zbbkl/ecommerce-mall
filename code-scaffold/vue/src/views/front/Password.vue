<template>
  <div class="password-page content-shell">
    <header class="page-heading">
      <h1>修改密码</h1>
      <p>新密码至少 8 位，并同时包含字母和数字。</p>
    </header>

    <section class="password-card">
      <el-form ref="formRef" :model="user" :rules="rules" label-position="top" @submit.native.prevent="update">
        <el-form-item label="原始密码" prop="password"><el-input v-model="user.password" type="password" autocomplete="current-password" show-password placeholder="请输入原始密码" clearable /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="user.newPassword" type="password" autocomplete="new-password" show-password placeholder="至少 8 位，包含字母和数字" clearable /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="user.confirmPassword" type="password" autocomplete="new-password" show-password placeholder="请再次输入新密码" clearable /></el-form-item>
        <div class="form-actions"><el-button @click="resetForm">重置</el-button><el-button type="primary" native-type="submit">确认修改</el-button></div>
      </el-form>
    </section>
  </div>
</template>

<script>
const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

export default {
  name: 'Password',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) callback(new Error('请输入确认密码'))
      else if (value !== this.user.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }
    const validateNewPassword = (rule, value, callback) => {
      if (!value) callback(new Error('请输入新密码'))
      else if (value.length < 8) callback(new Error('密码长度不能少于 8 位'))
      else if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) callback(new Error('密码必须包含字母和数字'))
      else if (value === this.user.password) callback(new Error('新密码不能与原始密码相同'))
      else callback()
    }
    return {
      user: { password: '', newPassword: '', confirmPassword: '', id: currentUser.id },
      rules: {
        password: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
        newPassword: [{ validator: validateNewPassword, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
      }
    }
  },
  created() { if (!this.user.id) this.$router.push('/login') },
  methods: {
    resetForm() { this.$refs.formRef.resetFields() },
    update() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        try {
          const res = await this.$request.post('/user/password', { id: this.user.id, newPassword: this.user.newPassword, password: this.user.password })
          if (res.code === '200') {
            this.$notify.success({ title: '成功', message: '密码修改成功，请重新登录', showClose: false, duration: 2000 })
            localStorage.removeItem('user')
            localStorage.removeItem('userId')
            setTimeout(() => this.$router.push('/login'), 1200)
          } else this.$notify.error({ title: '修改失败', message: res.msg || '请稍后重试', showClose: false, duration: 2000 })
        } catch (error) {
          this.$notify.error({ title: '网络异常', message: '请稍后重试', showClose: false, duration: 2000 })
        }
      })
    }
  }
}
</script>

<style scoped>
.password-page { min-height: 75vh; padding-top: var(--space-8); padding-bottom: var(--space-16); }
.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); line-height: var(--text-display-leading); }
.page-heading p { margin-top: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-body-size); }
.password-card { width: min(100%, 560px); margin: var(--space-6) auto 0; padding: var(--space-6); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); }
.form-actions { display: flex; justify-content: flex-end; gap: var(--space-2); padding-top: var(--space-4); border-top: 1px solid var(--c-line); }
</style>
