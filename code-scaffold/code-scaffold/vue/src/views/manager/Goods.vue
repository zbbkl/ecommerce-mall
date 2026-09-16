<template>
  <div>
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px;margin: 0 5px" placeholder="查询..." v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">查询</el-button>
        <el-button type="info" plain @click="reset">重置</el-button>
        <el-button type="primary" plain @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="序号" width="70" align="center" >
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="name" label="名称" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="typeName" label="分类名称"></el-table-column>
        <el-table-column align="center" prop="descr" label="描述" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="content" label="详情介绍">
          <template slot-scope="scope">
            <el-button plain type="success" @click="view(scope.row)" size="mini">预览</el-button>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="cover" label="图片">
          <template v-slot="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.cover" :preview-src-list="[scope.row.cover]"></el-image>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="price" label="价格"></el-table-column>
        <el-table-column align="center" prop="store" label="库存"></el-table-column>
        <el-table-column align="center" prop="userName" label="添加人"></el-table-column>
        <el-table-column align="center" prop="date" label="上架日期" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column align="center" prop="state" label="状态"></el-table-column>
        <el-table-column align="center" prop="sales" label="销量"></el-table-column>

        <el-table-column label="操作" width="240" align="center">
          <template slot-scope="scope">
            <el-button plain type="success" @click="detail(scope.row)" size="mini">详情</el-button>
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">编辑</el-button>
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

    <el-dialog title="信息" :visible.sync="dialogFormVisible" width="50%" :close-on-click-modal="false">
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
        <el-form-item prop="state" label="状态">
          <el-radio-group v-model="form.state" size="small">
            <el-radio-button label="上架"></el-radio-button>
            <el-radio-button label="下架"></el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>

    <el-drawer :visible.sync="drawerFormVisible" title="详情" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">详情</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '退出全屏' : '全屏'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="drawerFormVisible = false"/>
        </div>
      </div>

      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item prop="name" label="名称">
            <div>{{form.name}}</div>
          </el-form-item>
          <el-form-item prop="typeId" label="商品分类名称">
            <div>{{form.typeName}}</div>
          </el-form-item>
          <el-form-item prop="descr" label="描述">
            <div>{{form.descr}}</div>
          </el-form-item>
          <el-form-item prop="content" label="详情介绍">
            <div class="w-e-text" v-html="form.content"></div>
          </el-form-item>
          <el-form-item prop="cover" label="封面">
            <div>
              <el-image style="width: 50px; height: 50px" :src="form.cover" :preview-src-list="[form.cover]"></el-image>
            </div>
          </el-form-item>
          <el-form-item prop="price" label="价格">
            <div>{{form.price}}</div>
          </el-form-item>
          <el-form-item prop="store" label="库存">
            <div>{{form.store}}</div>
          </el-form-item>
          <el-form-item prop="userId" label="添加人">
            <div>{{form.userName}}</div>
          </el-form-item>
          <el-form-item prop="date" label="上架日期">
            <div>{{form.date}}</div>
          </el-form-item>
          <el-form-item prop="state" label="状态">
            <div>{{form.state}}</div>
          </el-form-item>
          <el-form-item prop="sales" label="销量">
            <div>{{form.sales}}</div>
          </el-form-item>

        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button @click="drawerFormVisible = false">关闭</el-button>
      </div>
    </el-drawer>

    <el-dialog title="预览信息" :visible.sync="dialogWangeditorVisible" width="60%">
      <div v-html="content"></div>
    </el-dialog>
  </div>
</template>

<script>
import E from "wangeditor"
export default {
  name: "Goods",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      form: {},
      dialogFormVisible: false,
      drawerFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        name: [
          { required: true, message: '请输入必填项', trigger: 'blur'}
        ],
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right',
      editor: null,
      dialogWangeditorVisible: false,
      content: '',
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
        // 关键：创建新实例前，先销毁已存在的实例
        if (this.editor) {
          this.destroy(); // 销毁旧实例
          this.editor = null;    // 置空，避免内存泄漏
        }
        // 重新创建编辑器实例
        this.editor = new E(`#editor`);
        // 编辑器配置（保持原配置不变）
        this.editor.config.uploadImgHeaders = {token: this.user.token};
        this.editor.config.uploadImgServer = 'http://localhost:9999/file/editor/upload';
        this.editor.config.uploadFileName = 'file';
        this.editor.config.uploadVideoHeaders = {token: this.user.token};
        this.editor.config.uploadVideoServer = 'http://localhost:9999/file/editor/uploadVideo';
        this.editor.config.uploadVideoName = 'file';
        this.editor.create(); // 最终创建实例
      });
    },
    destroy() {
      this.editor.destroy()
      this.editor = null
    },
    view(row) {
      this.content = row.content
      this.dialogWangeditorVisible = true
    },
    load() {
      this.$request.get("/goods/selectPage", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
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
          this.form.content = this.editor.txt.html()
          this.$request({
            method: this.form.id ? 'PUT' : 'POST',
            url: this.form.id ? '/goods/update' : '/goods/add',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
              this.dialogFormVisible = false
              this.load()
            } else {
              this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
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
          // 设置富文本
          this.setEditor()
        })
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$nextTick(() => {
          // 设置富文本
          this.setEditor()
          setTimeout(() => {
            this.editor.txt.html(row.content) //设置内容
          }, 0)
        })
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    del(id) {
      this.$confirm('您确认删除这些数据吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete("/goods/delete?id=" + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '成功', message: '操作成功', showClose: false, duration: 2000});
            this.load()
          } else {
            this.$notify.error({title: '成功', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {})
    },
    reset() {
      this.name = ""
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleImgUploadSuccess(res) {
      this.form.cover = res.data
    },
    handleFullscreenChange() {
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
        this.exitFullscreen();
      }
    },
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>
</style>
