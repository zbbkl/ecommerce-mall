<template>
  <div class="admin-page">
    <header class="page-heading"><div><h1>系统首页</h1><p>管理用户、商品、订单、轮播图与商户入驻。</p></div></header>
    <section class="welcome-panel"><span class="welcome-mark" aria-hidden="true">捞</span><div><h2>欢迎使用捞宝购物后台</h2><p>左侧导航按用户、信息、商户和系统管理分组。</p></div></section>
  </div>
</template>

<script>
export default { name: 'Home', data() { return { user: JSON.parse(localStorage.getItem('user') || '{}') } } }
</script>

<style scoped>
.welcome-panel { display: flex; align-items: center; gap: var(--space-5); padding: var(--space-8); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-12); box-shadow: var(--shadow-e1); }.welcome-mark { display: grid; width: 64px; height: 64px; place-items: center; background: var(--c-brand-soft); border-radius: var(--radius-16); color: var(--c-brand); font-size: var(--text-display-size); font-weight: 600; }.welcome-panel h2 { font-size: var(--text-title-lg-size); }.welcome-panel p { margin-top: var(--space-1); color: var(--c-ink-muted); font-size: var(--text-body-size); }
</style>
