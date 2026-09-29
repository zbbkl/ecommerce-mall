<template>
  <div class="merchant-page">
    <header class="page-heading">
      <div><h1>商品管理</h1><p>维护店内商品、库存、价格和上下架状态。</p></div>
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
    </header>
    <section class="panel">
      <form class="filter-bar" @submit.prevent="load(1)">
        <el-input v-model.trim="name" clearable placeholder="查询商品名称" aria-label="查询商品名称" />
        <el-select v-model="state" clearable placeholder="全部状态" aria-label="筛选商品状态"><el-option label="上架" value="上架" /><el-option label="下架" value="下架" /></el-select>
        <el-button type="primary" native-type="submit">查询</el-button><el-button @click="reset">重置</el-button>
      </form>
      <el-table :data="tableData" stripe>
        <el-table-column label="序号" width="70" align="center"><template v-slot="scope">{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</template></el-table-column>
        <el-table-column prop="name" label="名称" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column prop="typeName" label="分类" width="110" />
        <el-table-column label="图片" width="90" align="center"><template v-slot="scope"><el-image class="table-image" :src="scope.row.cover" :alt="scope.row.name" fit="cover" :preview-src-list="[scope.row.cover]" /></template></el-table-column>
        <el-table-column label="价格" width="100" align="right"><template v-slot="scope"><span class="tabular-nums">¥{{ money(scope.row.price) }}</span></template></el-table-column>
        <el-table-column label="库存" width="90" align="center"><template v-slot="scope"><span class="tabular-nums" :class="{ 'stock-low': Number(scope.row.store) < 10 }">{{ scope.row.store }}</span></template></el-table-column>
        <el-table-column prop="date" label="上架日期" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="90" align="center"><template v-slot="scope"><span class="state-pill" :class="scope.row.state === '上架' ? 'state-pill--on' : 'state-pill--off'">{{ scope.row.state }}</span></template></el-table-column>
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right"><template v-slot="scope"><button class="table-action" type="button" @click="handleEdit(scope.row)">编辑</button><button class="table-action" type="button" @click="onOff(scope.row, scope.row.state === '上架' ? '下架' : '上架')">{{ scope.row.state === '上架' ? '下架' : '上架' }}</button><button class="table-action table-action--danger" type="button" @click="del(scope.row.id)">删除</button></template></el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination background layout="total, prev, pager, next" :current-page="pageNum" :page-size="pageSize" :total="total" @current-change="handleCurrentChange" /></div>
    </section>

    <el-dialog title="商品信息" :visible.sync="dialogFormVisible" width="min(94vw, 880px)" :close-on-click-modal="false" @closed="destroyEditor">
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" autocomplete="off" /></el-form-item>
        <el-form-item label="描述" prop="descr"><el-input v-model="form.descr" type="textarea" :rows="2" autocomplete="off" /></el-form-item>
        <el-form-item label="详情介绍" prop="content"><div id="editor" class="editor-host"></div></el-form-item>
        <el-form-item label="封面" prop="cover"><el-upload :action="uploadUrl" :headers="{ token: user.token }" :show-file-list="false" :before-upload="beforeImageUpload" :on-success="handleImgUploadSuccess"><el-button>上传封面</el-button></el-upload><el-image v-if="form.cover" class="cover-preview" :src="form.cover" fit="cover" :preview-src-list="[form.cover]" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right" /></el-form-item>
        <el-form-item label="库存" prop="store"><el-input-number v-model="form.store" :min="0" :precision="0" controls-position="right" /></el-form-item>
        <el-form-item label="上架日期" prop="date"><el-date-picker v-model="form.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" /></el-form-item>
        <el-form-item label="分类" prop="typeId"><el-select v-model="form.typeId" placeholder="请选择分类"><el-option v-for="item in types" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button @click="dialogFormVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import E from 'wangeditor'
export default {
  name: 'MerchantGoods',
  data() {
    return {
      tableData: [], total: 0, pageNum: 1, pageSize: 10, name: '', state: '', form: {}, dialogFormVisible: false,
      user: JSON.parse(localStorage.getItem('user') || '{}'), editor: null, types: [],
      rules: { name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], descr: [{ required: true, message: '请输入商品描述', trigger: 'blur' }], cover: [{ required: true, message: '请上传商品封面', trigger: 'change' }], price: [{ required: true, message: '请输入价格', trigger: 'change' }], store: [{ required: true, message: '请输入库存', trigger: 'change' }], date: [{ required: true, message: '请选择上架日期', trigger: 'change' }], typeId: [{ required: true, message: '请选择分类', trigger: 'change' }] }
    }
  },
  computed: { uploadUrl() { return this.$baseUrl + '/file/upload' } },
  created() { this.loadType(); this.load() },
  beforeDestroy() { this.destroyEditor() },
  methods: {
    money(value) { const amount = Number(value); return Number.isFinite(amount) ? amount.toFixed(2) : '0.00' },
    setEditor() {
      this.$nextTick(() => {
        this.destroyEditor()
        this.editor = new E('#editor')
        this.editor.config.uploadImgHeaders = { token: this.user.token }
        this.editor.config.uploadImgServer = this.$baseUrl + '/file/editor/upload'
        this.editor.config.uploadFileName = 'file'
        this.editor.config.uploadVideoHeaders = { token: this.user.token }
        this.editor.config.uploadVideoServer = this.$baseUrl + '/file/editor/uploadVideo'
        this.editor.config.uploadVideoName = 'file'
        this.editor.create()
        if (this.form.content) this.editor.txt.html(this.form.content)
      })
    },
    destroyEditor() { if (this.editor) { this.editor.destroy(); this.editor = null } },
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/merchant/goods/selectPage', { params: { pageNum: this.pageNum, pageSize: this.pageSize, name: this.name, state: this.state } }).then(res => {
        this.tableData = Array.isArray(res.data?.records) ? res.data.records : []
        this.total = res.data?.total || 0
      })
    },
    loadType() { this.$request.get('/type/selectAll').then(res => { this.types = Array.isArray(res.data) ? res.data : [] }) },
    save() {
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return
        if (this.editor) this.form.content = this.editor.txt.html()
        this.$request({ method: this.form.id ? 'PUT' : 'POST', url: this.form.id ? '/merchant/goods/update' : '/merchant/goods/add', data: this.form }).then(res => {
          if (res.code === '200') { this.$notify.success({ title: '成功', message: '保存成功', showClose: false, duration: 2000 }); this.dialogFormVisible = false; this.load() }
          else this.$notify.error({ title: '保存失败', message: res.msg, showClose: false, duration: 2000 })
        })
      })
    },
    handleAdd() { this.form = { price: 0, store: 0 }; this.dialogFormVisible = true; this.setEditor() },
    handleEdit(row) { this.form = JSON.parse(JSON.stringify(row)); this.form.price = Number(this.form.price); this.form.store = Number(this.form.store); this.dialogFormVisible = true; this.setEditor() },
    onOff(row, state) { this.$request.put('/merchant/goods/onOff', null, { params: { id: row.id, state } }).then(res => { if (res.code === '200') this.load(); else this.$notify.error({ title: '操作失败', message: res.msg, showClose: false, duration: 2000 }) }) },
    del(id) { this.$confirm('确认删除该商品吗？', '确认删除', { type: 'warning' }).then(() => { this.$request.delete('/merchant/goods/delete?id=' + id).then(res => { if (res.code === '200') { this.$notify.success({ title: '成功', message: '商品已删除', showClose: false, duration: 2000 }); this.load() } else this.$notify.error({ title: '删除失败', message: res.msg, showClose: false, duration: 2000 }) }) }).catch(() => {}) },
    reset() { this.name = ''; this.state = ''; this.load(1) },
    handleCurrentChange(pageNum) { this.pageNum = pageNum; this.load() },
    beforeImageUpload(file) { if (!['image/jpeg', 'image/png'].includes(file.type)) { this.$message.error('仅支持 JPG 或 PNG'); return false } if (file.size / 1024 / 1024 > 5) { this.$message.error('图片不能超过 5MB'); return false } return true },
    handleImgUploadSuccess(res) { if (res && res.code === '200') { this.form.cover = res.data; this.$nextTick(() => this.$refs.ruleForm.clearValidate('cover')) } else this.$notify.error({ title: '上传失败', message: (res && res.msg) || '图片上传失败', showClose: false, duration: 2000 }) }
  }
}
</script>

<style scoped>
.merchant-page { min-height: calc(100dvh - var(--header-height) - 32px); }
.page-heading { display: flex; align-items: center; justify-content: space-between; gap: var(--space-5); margin-bottom: var(--space-5); }
.page-heading h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }
.page-heading p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-caption-size); }
.panel { padding: var(--space-5); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-12); box-shadow: var(--shadow-e1); }
.filter-bar { display: grid; grid-template-columns: minmax(180px, 260px) 140px auto auto; gap: var(--space-3); justify-content: start; margin-bottom: var(--space-4); }
.table-image { width: 48px; height: 48px; border-radius: var(--radius-8); }
.stock-low { color: var(--c-accent); font-weight: 600; }
.state-pill { display: inline-flex; padding: 3px var(--space-2); border-radius: var(--radius-full); font-size: var(--text-micro-size); font-weight: 600; }
.state-pill--on { background: var(--c-brand-soft); color: var(--c-success); }
.state-pill--off { background: var(--c-surface-sunken); color: var(--c-ink-muted); }
.table-action { padding: 4px var(--space-2); background: transparent; border: 0; color: var(--c-brand); cursor: pointer; font-size: var(--text-caption-size); }
.table-action:hover { color: var(--c-brand-hover); }
.table-action--danger { color: var(--c-accent); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
.editor-host { width: 100%; min-height: 260px; }
.cover-preview { display: block; width: 80px; height: 80px; margin-top: var(--space-2); border: 1px solid var(--c-line); border-radius: var(--radius-8); }
@media (max-width: 900px) { .filter-bar { grid-template-columns: 1fr 1fr; } .page-heading { align-items: flex-start; flex-direction: column; } }
@media (max-width: 560px) { .filter-bar { grid-template-columns: 1fr; } }
</style>
