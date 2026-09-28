<template>
  <div class="person-page content-shell">
    <header class="page-heading">
      <h1>个人信息</h1>
      <p>维护联系方式、头像和账户余额，保存后用于订单结算与配送。</p>
    </header>

    <section class="profile-card">
      <header class="profile-summary">
        <el-upload class="avatar-uploader" :action="uploadUrl" :headers="{ token: user.token }" :show-file-list="false" :before-upload="beforeAvatarUpload" :on-success="handleAvatarSuccess">
          <img v-if="user.avatar" :src="user.avatar" class="avatar" :alt="`${user.name || '用户'}头像`" />
          <span v-else class="avatar-placeholder" aria-label="上传头像"><i class="el-icon-plus" aria-hidden="true"></i></span>
        </el-upload>
        <div class="summary-copy"><h2>{{ user.name || user.username || '未命名用户' }}</h2><p>{{ user.infos || '还没有填写个人介绍。' }}</p></div>
        <div class="balance-block"><span>账户余额</span><strong class="tabular-nums">¥{{ money(user.account) }}</strong><button type="button" @click="handleOpen">充值</button></div>
      </header>

      <el-form ref="formRef" class="profile-form" :model="user" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="账号" prop="username"><el-input v-model="user.username" placeholder="用户名" disabled /></el-form-item>
          <el-form-item label="姓名" prop="name"><el-input v-model="user.name" placeholder="请输入姓名" /></el-form-item>
          <el-form-item label="电话" prop="phone"><el-input v-model="user.phone" type="tel" inputmode="tel" autocomplete="tel" placeholder="请输入手机号" /></el-form-item>
          <el-form-item label="邮箱" prop="email"><el-input v-model="user.email" type="email" inputmode="email" autocomplete="email" placeholder="name@example.com" /></el-form-item>
          <el-form-item label="性别" prop="sex"><el-radio-group v-model="user.sex"><el-radio label="男">男</el-radio><el-radio label="女">女</el-radio></el-radio-group></el-form-item>
          <el-form-item label="年龄" prop="age"><el-input v-model="user.age" type="number" inputmode="numeric" placeholder="请输入年龄" /></el-form-item>
          <el-form-item class="form-span" label="地址" prop="address"><el-input v-model="user.address" type="textarea" :rows="3" placeholder="请输入收货地址" /></el-form-item>
          <el-form-item class="form-span" label="个人介绍" prop="infos"><el-input v-model="user.infos" type="textarea" :rows="3" placeholder="简单介绍自己" /></el-form-item>
        </div>
        <div class="form-actions"><el-button class="save-button" type="primary" @click="update">保存信息</el-button><el-button @click="$router.push('/front/password')">修改密码</el-button></div>
      </el-form>
    </section>

    <el-dialog title="账户充值" :visible.sync="dialogFormVisible" width="min(92vw, 420px)" :close-on-click-modal="false">
      <el-form ref="rechargeForm" :model="recharge" :rules="rechargeRules" label-position="top">
        <el-form-item label="充值金额" prop="account"><el-input v-model="recharge.account" type="number" inputmode="decimal" placeholder="请输入大于 0 的金额" /></el-form-item>
        <el-form-item label="支付方式" prop="type"><el-radio-group v-model="recharge.type"><el-radio-button label="微信支付">微信支付</el-radio-button><el-radio-button label="支付宝支付">支付宝支付</el-radio-button></el-radio-group></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button @click="dialogFormVisible = false">取消</el-button><el-button type="primary" @click="save">确认充值</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Person',
  data() {
    const validatePhone = (rule, value, callback) => {
      if (!value) return callback()
      return /^1\d{10}$/.test(value) ? callback() : callback(new Error('请输入 11 位手机号'))
    }
    const validateAge = (rule, value, callback) => {
      if (value === '' || value === null || typeof value === 'undefined') return callback()
      const age = Number(value)
      return Number.isInteger(age) && age >= 0 && age <= 120 ? callback() : callback(new Error('请输入 0–120 之间的整数'))
    }
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      dialogFormVisible: false,
      recharge: { account: '', type: '微信支付' },
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
        age: [{ validator: validateAge, trigger: 'blur' }]
      },
      rechargeRules: {
        account: [{ validator: (rule, value, callback) => {
          const amount = Number(value)
          return Number.isFinite(amount) && amount > 0 ? callback() : callback(new Error('请输入大于 0 的金额'))
        }, trigger: 'blur' }],
        type: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
      }
    }
  },
  computed: {
    uploadUrl() { return this.$baseUrl + '/file/upload' }
  },
  created() { this.loadUser() },
  methods: {
    money(value) { const amount = Number(value); return Number.isFinite(amount) ? amount.toFixed(2) : '0.00' },
    loadUser() {
      if (!this.user.id) { this.$notify.warning({ title: '请登录', message: '登录后查看个人信息', showClose: false, duration: 2000 }); this.$router.push('/login'); return }
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200') this.user = Object.assign({}, res.data, { token: this.user.token })
        else this.$notify.error({ title: '加载失败', message: res.msg, showClose: false, duration: 2000 })
      })
    },
    update() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.$request.put('/user/update', this.user).then(res => {
          if (res.code === '200') { this.$message.success('保存成功'); localStorage.setItem('user', JSON.stringify(this.user)); this.$emit('update:user', this.user) }
          else this.$notify.error({ title: '保存失败', message: res.msg, showClose: false, duration: 2000 })
        })
      })
    },
    beforeAvatarUpload(file) {
      const allowed = ['image/jpeg', 'image/png']
      if (!allowed.includes(file.type)) { this.$message.error('头像仅支持 JPG 或 PNG'); return false }
      if (file.size / 1024 / 1024 > 5) { this.$message.error('头像不能超过 5MB'); return false }
      return true
    },
    handleAvatarSuccess(response) {
      if (response && response.code === '200') { this.user.avatar = response.data; localStorage.setItem('user', JSON.stringify(this.user)); this.$emit('update:user', this.user); this.$notify.success({ title: '成功', message: '头像更新成功', showClose: false, duration: 2000 }) }
      else this.$notify.error({ title: '上传失败', message: (response && response.msg) || '头像上传失败', showClose: false, duration: 2000 })
    },
    handleOpen() { this.recharge = { account: '', type: '微信支付' }; this.dialogFormVisible = true; this.$nextTick(() => this.$refs.rechargeForm && this.$refs.rechargeForm.clearValidate()) },
    save() {
      this.$refs.rechargeForm.validate(valid => {
        if (!valid) return
        const amount = Number(this.recharge.account)
        this.$request.put('/user/recharge', { account: amount }).then(res => {
          if (res.code === '200') { this.user.account = Number(this.user.account) + amount; localStorage.setItem('user', JSON.stringify(this.user)); this.$notify.success({ title: '成功', message: '充值成功', showClose: false, duration: 2000 }); this.dialogFormVisible = false }
          else this.$notify.error({ title: '充值失败', message: res.msg, showClose: false, duration: 2000 })
        })
      })
    }
  }
}
</script>

<style scoped>
.person-page { min-height: 75vh; padding-top: var(--space-8); padding-bottom: var(--space-16); }
.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); line-height: var(--text-display-leading); }
.page-heading p { margin-top: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-body-size); }
.profile-card { margin-top: var(--space-6); padding: var(--space-6); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); }
.profile-summary { display: grid; grid-template-columns: auto minmax(0, 1fr) auto; gap: var(--space-5); align-items: center; padding-bottom: var(--space-6); border-bottom: 1px solid var(--c-line); }
.avatar-uploader :deep(.el-upload) { display: block; width: 88px; height: 88px; overflow: hidden; background: var(--c-surface-sunken); border: 1px dashed var(--c-line-strong); border-radius: var(--radius-full); cursor: pointer; }
.avatar, .avatar-placeholder { display: grid; width: 86px; height: 86px; place-items: center; border-radius: var(--radius-full); }
.avatar { object-fit: cover; }
.avatar-placeholder { color: var(--c-ink-subtle); font-size: var(--text-display-size); }
.summary-copy h2 { font-size: var(--text-title-lg-size); font-weight: var(--text-title-lg-weight); }
.summary-copy p { max-width: 44em; margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.balance-block { min-width: 180px; padding: var(--space-4); background: var(--c-surface-sunken); border-radius: var(--radius-12); text-align: right; }
.balance-block span { display: block; color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.balance-block strong { display: block; margin-top: var(--space-1); color: var(--c-accent); font-size: var(--text-price-lg-size); font-weight: var(--text-price-lg-weight); }
.balance-block button { min-height: 36px; margin-top: var(--space-3); padding: 0 var(--space-4); background: var(--c-brand); border: 1px solid var(--c-brand); border-radius: var(--radius-8); color: var(--c-surface); cursor: pointer; font-weight: 600; }
.profile-form { margin-top: var(--space-6); }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 var(--space-5); }
.form-span { grid-column: 1 / -1; }
.form-actions { display: flex; justify-content: flex-end; gap: var(--space-2); padding-top: var(--space-5); border-top: 1px solid var(--c-line); }
.save-button { min-width: 128px; }
@media (max-width: 760px) { .profile-summary { grid-template-columns: auto minmax(0, 1fr); } .balance-block { grid-column: 1 / -1; min-width: 0; text-align: left; } .form-grid { grid-template-columns: 1fr; } .form-span { grid-column: auto; } }
</style>
