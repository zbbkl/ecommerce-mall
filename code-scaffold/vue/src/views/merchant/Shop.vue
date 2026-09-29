<template>
  <div class="merchant-page">
    <header class="page-heading"><div><h1>店铺信息</h1><p>维护店铺名称、简介、联系方式和经营资质。</p></div></header>
    <section class="form-panel">
      <div class="panel-head"><div><h2>基础资料</h2><p>店铺信息会展示在前台店铺主页。</p></div><span class="state-pill" :class="stateClass">{{ user.state || '未知' }}</span></div>
      <el-alert v-if="user.state === '已驳回'" class="reject-alert" type="error" :closable="false" show-icon :title="`审核驳回：${user.rejectReason || '未填写原因'}`" />
      <el-form ref="shopForm" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="登录账号" prop="username"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="店铺名称" prop="shopName"><el-input v-model="form.shopName" placeholder="请输入店铺名称" /></el-form-item>
          <el-form-item class="form-span" label="店铺简介" prop="descr"><el-input v-model="form.descr" type="textarea" :rows="3" placeholder="介绍主营商品和服务" /></el-form-item>
          <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" type="tel" inputmode="tel" autocomplete="tel" placeholder="请输入联系电话" /></el-form-item>
          <el-form-item label="经营地址" prop="address"><el-input v-model="form.address" placeholder="请输入经营地址" /></el-form-item>
        </div>
        <div class="upload-grid">
          <el-form-item label="店铺 Logo" prop="logo"><el-upload class="asset-uploader" :action="uploadUrl" :headers="{ token: user.token }" :show-file-list="false" :before-upload="beforeImageUpload" :on-success="handleLogoSuccess"><el-button>上传 Logo</el-button></el-upload><el-image v-if="form.logo" class="asset-preview" :src="form.logo" alt="店铺 Logo" fit="cover" :preview-src-list="[form.logo]" /></el-form-item>
          <el-form-item label="营业执照" prop="license"><el-upload class="asset-uploader" :action="uploadUrl" :headers="{ token: user.token }" :show-file-list="false" :before-upload="beforeImageUpload" :on-success="handleLicenseSuccess"><el-button>上传营业执照</el-button></el-upload><el-image v-if="form.license" class="asset-preview" :src="form.license" alt="营业执照" fit="cover" :preview-src-list="[form.license]" /></el-form-item>
        </div>
      </el-form>
      <div class="form-actions"><el-button type="primary" @click="save">保存店铺信息</el-button></div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'MerchantShop',
  data() {
    return { user: JSON.parse(localStorage.getItem('user') || '{}'), form: {}, rules: { shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }], phone: [{ validator: (rule, value, callback) => !value || /^1\d{10}$/.test(value) ? callback() : callback(new Error('请输入 11 位手机号')), trigger: 'blur' }] } }
  },
  computed: {
    uploadUrl() { return this.$baseUrl + '/file/upload' },
    stateClass() { return { 'state-pill--success': this.user.state === '已通过', 'state-pill--warning': this.user.state === '待审核', 'state-pill--danger': this.user.state === '已驳回' || this.user.state === '已停用' } }
  },
  created() { this.load() },
  methods: {
    load() { this.$request.get('/merchant/profile').then(res => { if (res.code === '200') this.form = res.data || {}; else this.$notify.error({ title: '加载失败', message: res.msg, showClose: false, duration: 2000 }) }) },
    beforeImageUpload(file) { if (!['image/jpeg', 'image/png'].includes(file.type)) { this.$message.error('仅支持 JPG 或 PNG'); return false } if (file.size / 1024 / 1024 > 5) { this.$message.error('图片不能超过 5MB'); return false } return true },
    handleLogoSuccess(res) { if (res && res.code === '200') this.form.logo = res.data; else this.$message.error((res && res.msg) || 'Logo 上传失败') },
    handleLicenseSuccess(res) { if (res && res.code === '200') this.form.license = res.data; else this.$message.error((res && res.msg) || '营业执照上传失败') },
    save() { this.$refs.shopForm.validate(valid => { if (!valid) return; this.$request.put('/merchant/profile', this.form).then(res => { if (res.code === '200') { this.$notify.success({ title: '成功', message: '保存成功', showClose: false, duration: 2000 }); const user = { ...this.user, shopName: this.form.shopName, logo: this.form.logo, descr: this.form.descr }; localStorage.setItem('user', JSON.stringify(user)); this.$emit('update:user', user) } else this.$notify.error({ title: '保存失败', message: res.msg, showClose: false, duration: 2000 }) }) }) }
  }
}
</script>

<style scoped>
.merchant-page { min-height: calc(100dvh - var(--header-height) - 32px); }
.page-heading { margin-bottom: var(--space-5); }.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }.page-heading p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.form-panel { max-width: 860px; padding: var(--space-6); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-12); box-shadow: var(--shadow-e1); }
.panel-head { display: flex; align-items: flex-start; justify-content: space-between; gap: var(--space-4); padding-bottom: var(--space-4); border-bottom: 1px solid var(--c-line); }.panel-head h2 { font-size: var(--text-title-size); }.panel-head p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.state-pill { flex: 0 0 auto; padding: var(--space-1) var(--space-3); border-radius: var(--radius-full); font-size: var(--text-micro-size); font-weight: 600; }.state-pill--success { background: var(--c-brand-soft); color: var(--c-success); }.state-pill--warning { background: var(--c-flag); color: var(--c-ink); }.state-pill--danger { background: var(--c-accent-soft); color: var(--c-accent); }
.reject-alert { margin-top: var(--space-4); }
.form-grid,.upload-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 0 var(--space-5); margin-top: var(--space-5); }.form-span { grid-column: 1/-1; }
.asset-preview { display: block; width: 96px; height: 96px; margin-top: var(--space-3); border: 1px solid var(--c-line); border-radius: var(--radius-8); object-fit: cover; }
.form-actions { display: flex; justify-content: flex-end; padding-top: var(--space-5); border-top: 1px solid var(--c-line); }
@media (max-width:760px){.form-panel{padding:var(--space-4)}.form-grid,.upload-grid{grid-template-columns:1fr}.form-span{grid-column:auto}}
</style>
