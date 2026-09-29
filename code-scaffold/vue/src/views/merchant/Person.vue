<template>
  <div class="merchant-page">
    <header class="page-heading"><div><h1>个人信息</h1><p>查看商户账号信息并修改登录密码。</p></div></header>
    <section class="profile-panel">
      <div class="profile-head"><div class="avatar-mark" aria-hidden="true">{{ (user.shopName || '商').slice(0, 1) }}</div><div><h2>{{ user.shopName || '商户账号' }}</h2><p>{{ user.username || '-' }}</p></div></div>
      <el-descriptions :column="1" border><el-descriptions-item label="登录账号">{{ user.username || '-' }}</el-descriptions-item><el-descriptions-item label="店铺名称">{{ user.shopName || '-' }}</el-descriptions-item><el-descriptions-item label="联系电话">{{ user.phone || '-' }}</el-descriptions-item><el-descriptions-item label="入驻状态">{{ user.state || '-' }}</el-descriptions-item></el-descriptions>
      <div class="form-actions"><el-button type="primary" @click="openPassword">修改密码</el-button></div>
    </section>

    <el-drawer :visible.sync="formDetailVisible" title="修改密码" :with-header="false" size="min(92vw, 480px)">
      <div class="drawer-header"><span class="drawer-title">修改密码</span><button class="drawer-close" type="button" aria-label="关闭修改密码" @click="formDetailVisible = false"><i class="el-icon-close" aria-hidden="true"></i></button></div>
      <div class="drawer-content"><el-form ref="formRef" :model="passForm" :rules="rules" label-position="top"><el-form-item label="账号" prop="username"><el-input v-model="passForm.username" disabled /></el-form-item><el-form-item label="原始密码" prop="password"><el-input v-model="passForm.password" type="password" autocomplete="current-password" show-password placeholder="请输入原始密码" /></el-form-item><el-form-item label="新密码" prop="newPassword"><el-input v-model="passForm.newPassword" type="password" autocomplete="new-password" show-password placeholder="至少 6 位" /></el-form-item><el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="passForm.confirmPassword" type="password" autocomplete="new-password" show-password placeholder="请再次输入新密码" /></el-form-item></el-form></div>
      <div class="drawer-footer"><el-button type="primary" @click="updatePassword">确认修改</el-button><el-button @click="formDetailVisible = false">关闭</el-button></div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: 'MerchantPerson',
  data() {
    const validateConfirmPassword = (rule, value, callback) => { if (!value) callback(new Error('请输入确认密码')); else if (value !== this.passForm.newPassword) callback(new Error('两次输入的密码不一致')); else callback() }
    const validateNewPassword = (rule, value, callback) => { if (!value) callback(new Error('请输入新密码')); else if (value.length < 6) callback(new Error('密码长度不能少于 6 位')); else if (value === this.passForm.password) callback(new Error('新密码不能与原始密码相同')); else callback() }
    return { user: JSON.parse(localStorage.getItem('user') || '{}'), passForm: {}, formDetailVisible: false, rules: { password: [{ required: true, message: '请输入原始密码', trigger: 'blur' }], newPassword: [{ validator: validateNewPassword, required: true, trigger: 'blur' }], confirmPassword: [{ validator: validateConfirmPassword, required: true, trigger: 'blur' }] } }
  },
  created() { this.passForm = { username: this.user.username } },
  methods: {
    openPassword() { this.passForm = { username: this.user.username, password: '', newPassword: '', confirmPassword: '' }; this.formDetailVisible = true; this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate()) },
    updatePassword() { this.$refs.formRef.validate(valid => { if (!valid) return; this.$request.post('/merchant/password', this.passForm).then(res => { if (res.code === '200') { this.$notify.success({ title: '成功', message: '密码已修改，请重新登录', showClose: false, duration: 2000 }); this.formDetailVisible = false; localStorage.removeItem('user'); this.$router.push('/login') } else this.$notify.error({ title: '修改失败', message: res.msg, showClose: false, duration: 2000 }) }) }) }
  }
}
</script>

<style scoped>
.merchant-page { min-height: calc(100dvh - var(--header-height) - 32px); }
.page-heading { margin-bottom: var(--space-5); }.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }.page-heading p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.profile-panel { max-width: 680px; padding: var(--space-6); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-12); box-shadow: var(--shadow-e1); }
.profile-head { display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-5); }.avatar-mark { display: grid; width: 56px; height: 56px; place-items: center; background: var(--c-brand-soft); border-radius: var(--radius-12); color: var(--c-brand); font-size: var(--text-title-lg-size); font-weight: 600; }.profile-head h2 { font-size: var(--text-title-lg-size); }.profile-head p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.form-actions { display: flex; justify-content: flex-end; margin-top: var(--space-5); }.drawer-close { width: 32px; height: 32px; background: var(--c-surface-sunken); border: 0; border-radius: var(--radius-full); color: var(--c-ink-body); cursor: pointer; }
</style>
