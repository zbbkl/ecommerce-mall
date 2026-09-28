<template>
  <footer class="footer">
    <div class="footer-inner content-shell">
      <div class="footer-brand">
        <strong translate="no">捞宝购物</strong>
        <p>商品、库存和订单状态以页面实时信息为准。</p>
      </div>
      <div class="footer-meta">
        <p>© 2026 捞宝购物</p>
        <p>京ICP备12345678号</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  margin-top: var(--space-16);
  background: var(--c-surface-sunken);
  border-top: 1px solid var(--c-line);
}

.footer-inner {
  display: flex;
  min-height: 120px;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-8);
  padding-top: var(--space-6);
  padding-bottom: var(--space-6);
}

.footer-brand strong {
  color: var(--c-ink);
  font-size: var(--text-title-size);
}

.footer-brand p,
.footer-meta p {
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.footer-brand p {
  margin-top: var(--space-1);
}

.footer-meta {
  text-align: right;
}

.footer-meta p + p {
  margin-top: var(--space-1);
}

@media (max-width: 640px) {
  .footer-inner {
    align-items: flex-start;
    flex-direction: column;
  }

  .footer-meta {
    text-align: left;
  }
}
</style>
