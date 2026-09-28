<template>
  <main class="not-found-page">
    <section class="not-found-card">
      <img :src="image" alt="404 页面插画" />
      <h1>没有找到这个页面</h1>
      <p>地址可能已经变化，返回首页或去商品页继续浏览。</p>
      <div class="not-found-actions"><router-link class="primary-link" to="/">返回首页</router-link><router-link class="secondary-link" to="/front/goods">浏览商品</router-link></div>
    </section>
  </main>
</template>

<script>
export default { name: 'page-404', data() { return { image: require('@/assets/404.svg') } } }
</script>

<style scoped>
.not-found-page { display: grid; min-height: 100dvh; place-items: center; padding: var(--space-6); background: var(--c-canvas); }
.not-found-card { width: min(100%, 620px); padding: var(--space-10); background: var(--c-surface); border: 1px solid var(--c-line); border-radius: var(--radius-16); box-shadow: var(--shadow-e1); text-align: center; }
.not-found-card img { width: min(100%, 320px); max-height: 260px; margin: 0 auto var(--space-6); object-fit: contain; }
.not-found-card h1 { font-size: var(--text-display-size); font-weight: var(--text-display-weight); letter-spacing: var(--text-display-tracking); }
.not-found-card p { margin-top: var(--space-2); color: var(--c-ink-muted); font-size: var(--text-body-size); }
.not-found-actions { display: flex; justify-content: center; flex-wrap: wrap; gap: var(--space-3); margin-top: var(--space-8); }
.primary-link, .secondary-link { display: inline-flex; min-height: 44px; align-items: center; justify-content: center; padding: 0 var(--space-5); border-radius: var(--radius-8); font-weight: 600; }
.primary-link { background: var(--c-brand); border: 1px solid var(--c-brand); color: var(--c-surface); }
.primary-link:hover { background: var(--c-brand-hover); border-color: var(--c-brand-hover); color: var(--c-surface); }
.secondary-link { background: var(--c-surface); border: 1px solid var(--c-line-strong); color: var(--c-ink-body); }
.secondary-link:hover { border-color: var(--c-brand); color: var(--c-brand); }
</style>
