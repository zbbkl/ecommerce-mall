<template>
  <div class="form-container">
    <el-card class="form-card">
      <div class="card-header">个人信息</div>

      <el-form :model="user" label-width="80px" class="user-form">
        <div class="avatar-section">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:9999/file/upload"
              :headers="{ token: user.token }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="user.avatar" :src="user.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </div>

        <el-form-item label="账号" prop="username">
          <el-input v-model="user.username" placeholder="用户名" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="user.name" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="user.phone" placeholder="电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="user.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" v-model="user.address" placeholder="地址" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio v-model="user.sex" label="男">男</el-radio>
          <el-radio v-model="user.sex" label="女">女</el-radio>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="user.age" placeholder="年龄" />
        </el-form-item>
        <el-form-item label="个人介绍" prop="infos">
          <el-input type="textarea" v-model="user.infos" placeholder="个人介绍" />
        </el-form-item>
        <el-form-item label="余额" prop="account">
          {{user.account}}
        </el-form-item>
        <div class="form-actions">
          <el-button class="main-btn" @click="update">保 存</el-button>
          <el-button type="primary" plain @click="$router.push('/front/password')">修改密码</el-button>
          <el-button type="warning" plain @click="handleOpen">充值</el-button>
        </div>
      </el-form>
    </el-card>

    <el-dialog title="充值" :visible.sync="dialogFormVisible" width="30%" :close-on-click-modal="false">
      <el-form label-width="100px" style="padding-right: 40px" :model="user" ref="ruleForm">
        <el-form-item prop="account" label="金额">
          <el-input v-model="account" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="type" label="支付方式">
          <el-radio-group v-model="type" size="small">
            <el-radio-button label="微信支付"></el-radio-button>
            <el-radio-button label="支付宝支付"></el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "Person",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      dialogFormVisible: false,
      account: 0,
      type: '微信支付'
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser(){
      if (typeof this.user.name === 'undefined'){
        this.$notify.warning({title: '成功', message: '请登录', showClose: false, duration: 2000});
        this.$router.push('/login')
        return;
      }
      this.$request.get('/user/selectById/'+ this.user.id).then(res => {
        if (res.code === '200') {
          // 详情接口不返回 token，保留登录时存的 token，避免保存后丢失导致 401
          this.user = Object.assign({}, res.data, { token: this.user.token })
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    update() {
      this.$request.put('/user/update', this.user).then(res => {
        if (res.code === '200') {
          this.$message.success('保存成功')
          localStorage.setItem('user', JSON.stringify(this.user))
          this.$emit('update:user', this.user)
        } else {
          this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    handleAvatarSuccess(response) {
      if (response && response.code === '200') {
        this.user.avatar = response.data
        localStorage.setItem('user', JSON.stringify(this.user))
        this.$emit('update:user', this.user)
        this.$notify.success({title: '成功', message: '头像更新成功', showClose: false, duration: 2000});
      } else {
        this.$notify.error({title: '错误', message: (response && response.msg) || '头像上传失败', showClose: false, duration: 2000});
      }
    },
    handleOpen(){
      this.dialogFormVisible = true
    },
    save() {
      // 充值走专用接口：服务端在原余额上原子加金额（/user/update 已禁止自改 account）
      this.$request.put('/user/recharge', { account: Number(this.account) }).then(res => {
        if (res.code === '200') {
          this.user.account = Number(this.user.account) + Number(this.account)
          this.$notify.success({title: '成功', message: '充值成功', showClose: false, duration: 2000});
          this.dialogFormVisible = false
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
  }
}
</script>

<style scoped>
.form-container {
  display: flex;
  justify-content: center;
  padding: 10px;
  box-sizing: border-box;
}

.form-card {
  width: 30%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  transition: box-shadow 0.3s ease;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding: 16px 20px;
  border-left: 4px solid #ff6700;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
}

/* 保存按钮：渐变橙主按钮 */
.main-btn {
  background-image: linear-gradient(135deg, #ff8a2b, #ff6700);
  border: none;
  color: #fff;
  transition: all 0.25s ease;
}

.main-btn:hover {
  filter: brightness(1.06);
  box-shadow: 0 4px 10px rgba(255, 103, 0, 0.35);
}

.form-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.user-form {
  padding: 20px 30px;
}

.avatar-section {
  text-align: center;
  margin: 20px 0;
}

:deep(.el-form-item__label) {
  font-weight: bold;
  color: #333;
}

:deep(.avatar-uploader .el-upload) {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
  width: 78px;
  height: 78px;
  display: inline-block;
  line-height: 178px;
  text-align: center;
  transition: border-color 0.3s;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border-radius: 50%;
  background-color: #f8f8f8;
}

/* 已上传头像样式 */
.avatar {
  width: 78px;
  height: 78px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}

.form-actions .el-button {
  margin: 0 10px;
  min-width: 100px;
}

.custom-recharge-btn {
  background-color: #edbf37 !important;
  border-color: #edbf37 !important;
}

.custom-recharge-btn:hover {
  background-color: #f4d782 !important;
  border-color: #edbf37 !important;
}
</style>
