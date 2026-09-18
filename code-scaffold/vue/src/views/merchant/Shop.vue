<template>
  <div>
    <el-card style="width: 60%">
      <div slot="header" style="display: flex; align-items: center; justify-content: space-between">
        <span style="font-weight: bold">店铺信息</span>
        <el-tag :type="stateTagType" size="small">{{ user.state || '未知' }}</el-tag>
      </div>
      <el-form :model="form" :rules="rules" ref="shopForm" label-width="100px" style="padding-right: 20px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="店铺名称"></el-input>
        </el-form-item>
        <el-form-item label="店铺简介" prop="descr">
          <el-input type="textarea" v-model="form.descr" placeholder="店铺简介"></el-input>
        </el-form-item>
        <el-form-item label="店铺LOGO" prop="logo">
          <el-upload :action="$baseUrl + '/file/upload'" :headers="{ token: user.token }" :on-success="handleLogoSuccess">
            <el-button size="small" type="primary">上传LOGO</el-button>
          </el-upload>
          <el-image v-if="form.logo" style="width: 80px; height: 80px; margin-top: 8px" :src="form.logo" fit="cover" :preview-src-list="[form.logo]"></el-image>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话"></el-input>
        </el-form-item>
        <el-form-item label="经营地址" prop="address">
          <el-input v-model="form.address" placeholder="经营地址"></el-input>
        </el-form-item>
        <el-form-item label="营业执照" prop="license">
          <el-upload :action="$baseUrl + '/file/upload'" :headers="{ token: user.token }" :on-success="handleLicenseSuccess">
            <el-button size="small" type="primary">上传营业执照</el-button>
          </el-upload>
          <el-image v-if="form.license" style="width: 80px; height: 80px; margin-top: 8px" :src="form.license" fit="cover" :preview-src-list="[form.license]"></el-image>
        </el-form-item>
        <el-form-item v-if="user.state === '已驳回'" label="驳回原因">
          <div style="color: #f56c6c">{{ user.rejectReason || '未填写' }}</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保 存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "MerchantShop",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      form: {},
      rules: {
        shopName: [
          { required: true, message: '请输入店铺名称', trigger: 'blur' }
        ],
      }
    }
  },
  computed: {
    stateTagType() {
      switch (this.user.state) {
        case '已通过': return 'success'
        case '待审核': return 'warning'
        default: return 'danger'
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.$request.get('/merchant/profile').then(res => {
        if (res.code === '200') {
          this.form = res.data || {}
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    handleLogoSuccess(res) {
      this.form.logo = res.data
    },
    handleLicenseSuccess(res) {
      this.form.license = res.data
    },
    save() {
      this.$refs.shopForm.validate((valid) => {
        if (valid) {
          this.$request.put('/merchant/profile', this.form).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '保存成功', showClose: false, duration: 2000});
              // 同步本地缓存的店铺名等资料
              const user = {...this.user, shopName: this.form.shopName, logo: this.form.logo, descr: this.form.descr}
              localStorage.setItem('user', JSON.stringify(user))
              this.$emit('update:user', user)
            } else {
              this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>

</style>
