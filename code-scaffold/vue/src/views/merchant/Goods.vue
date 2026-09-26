<template>
  <div>
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px;margin: 0 5px" placeholder="查询..." v-model="name"></el-input>
        <el-select style="width: 120px;margin: 0 5px" placeholder="状态" v-model="state" clearable>
          <el-option label="上架" value="上架"></el-option>
          <el-option label="下架" value="下架"></el-option>
        </el-select>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
        <el-button type="primary" plain @click="handleAdd">新增商品</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="序号" width="70" align="center" >
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="name" label="名称" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="typeName" label="分类名称"></el-table-column>
        <el-table-column align="center" prop="cover" label="图片">
          <template v-slot="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.cover" :preview-src-list="[scope.row.cover]"></el-image>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="price" label="价格"></el-table-column>
        <el-table-column align="center" prop="store" label="库存">
          <template v-slot="scope">
            <span :style="scope.row.store < 10 ? 'color:#e64340;font-weight:bold' : ''">{{ scope.row.store }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="date" label="上架日期" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="state" label="状态">
          <template v-slot="scope">
            <el-tag :type="scope.row.state === '上架' ? 'success' : 'info'" size="small">{{ scope.row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="sales" label="销量"></el-table-column>

        <el-table-column label="操作" width="300" align="center">
          <template slot-scope="scope">
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">编辑</el-button>
            <el-button v-if="scope.row.state === '上架'" plain type="warning" size="mini" @click="onOff(scope.row, '下架')">下架</el-button>
            <el-button v-else plain type="success" size="mini" @click="onOff(scope.row, '上架')">上架</el-button>
            <el-button plain type="danger" size="mini" @click=del(scope.row.id)>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            style="padding: 0"
            background
            layout="total, prev, pager, next"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="商品信息" :visible.sync="dialogFormVisible" width="50%" :close-on-click-modal="false">
      <el-form label-width="100px" style="padding-right: 40px" :model="form" :rules="rules" ref="ruleForm">
        <el-form-item prop="name" label="名称">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="descr" label="描述">
          <el-input type="textarea" v-model="form.descr" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="content" label="详情介绍">
          <div id="editor"></div>
        </el-form-item>
        <el-form-item prop="cover" label="封面">
          <el-upload :action="$baseUrl +'/file/upload'" :headers="{ token: user.token }" ref="cover" :on-success="handleImgUploadSuccess">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="price" label="价格">
          <el-input v-model="form.price" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="store" label="库存">
          <el-input v-model="form.store" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="date" label="上架日期">
          <el-date-picker style='width:100%' v-model="form.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item prop="typeId" label="分类">
          <el-select v-model="form.typeId" placeholder="请选择分类">
            <el-option v-for="item in types" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
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
import E from "wangeditor"
export default {
  name: "MerchantGoods",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      state: "",
      form: {},
      dialogFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        name: [
          { required: true, message: '请输入必填项', trigger: 'blur'}
        ],
      },
      editor: null,
      types: []
    }
  },
  created() {
    this.loadType()
    this.load()
  },
  methods: {
    setEditor() {
      this.$nextTick(() => {
        if (this.editor) {
          this.editor.destroy()
          this.editor = null
        }
        this.editor = new E(`#editor`);
        this.editor.config.uploadImgHeaders = {token: this.user.token};
        this.editor.config.uploadImgServer = 'http://localhost:9999/file/editor/upload';
        this.editor.config.uploadFileName = 'file';
        this.editor.config.uploadVideoHeaders = {token: this.user.token};
        this.editor.config.uploadVideoServer = 'http://localhost:9999/file/editor/uploadVideo';
        this.editor.config.uploadVideoName = 'file';
        this.editor.create();
      });
    },
    load() {
      this.$request.get("/merchant/goods/selectPage", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          state: this.state,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    loadType(){
      this.$request.get("/type/selectAll").then(res => {
        this.types = res.data
      })
    },
    save() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          this.form.content = this.editor ? this.editor.txt.html() : this.form.content
          this.$request({
            method: this.form.id ? 'PUT' : 'POST',
            url: this.form.id ? '/merchant/goods/update' : '/merchant/goods/add',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
              this.dialogFormVisible = false
              this.load()
            } else {
              this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
      this.$nextTick(() => {
        this.$nextTick(() => {
          this.setEditor()
        })
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
      })
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$nextTick(() => {
          this.setEditor()
          setTimeout(() => {
            this.editor.txt.html(row.content)
          }, 0)
        })
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
      })
    },
    onOff(row, state) {
      this.$request.put('/merchant/goods/onOff', null, {
        params: { id: row.id, state: state }
      }).then(res => {
        if (res.code === '200') {
          this.$notify.success({title: '成功', message: state + '成功', showClose: false, duration: 2000});
          this.load()
        } else {
          this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    del(id) {
      this.$confirm('您确认删除该商品吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete("/merchant/goods/delete?id=" + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load()
          } else {
            this.$notify.error({title: '错误', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {})
    },
    reset() {
      this.name = ""
      this.state = ""
      this.load(1)
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleImgUploadSuccess(res) {
      this.form.cover = res.data
    },
  },
  beforeDestroy() {
    if (this.editor) {
      this.editor.destroy()
      this.editor = null
    }
  },
}
</script>

<style scoped>
</style>
