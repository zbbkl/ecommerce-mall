<template>
  <div class="admin-page">
    <el-empty description="当前账号没有权限访问该页面">
      <router-link class="empty-action" to="/home">返回首页</router-link>
    </el-empty>
  </div>
</template>

<script>
export default { name: 'Auth' }
</script>

<style scoped>
.admin-page { min-height: 60vh; padding-top: var(--space-16); }.empty-action { display: inline-flex; min-height: 40px; align-items: center; padding: 0 var(--space-4); background: var(--c-brand); border: 1px solid var(--c-brand); border-radius: var(--radius-8); color: var(--c-surface); font-weight: 600; }
</style>
