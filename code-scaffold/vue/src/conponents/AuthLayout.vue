<template>
  <main class="auth-layout">
    <section class="auth-brand">
      <router-link class="brand-link" to="/front/home" aria-label="返回捞宝购物首页">
        <img src="@/assets/logo.svg" alt="捞宝购物手绘猫标志" />
        <span>捞宝购物</span>
      </router-link>

      <div class="brand-copy">
        <h1>{{ introTitle }}</h1>
        <p>{{ introText }}</p>
      </div>

      <div class="illustration-tray">
        <img src="@/assets/login.svg" alt="宠物主人与狗狗的插画" />
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-form-wrap">
        <header class="auth-heading">
          <h2>{{ heading }}</h2>
          <p>{{ description }}</p>
        </header>
        <slot></slot>
      </div>
    </section>
  </main>
</template>

<script>
export default {
  name: 'AuthLayout',
  props: {
    heading: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    },
    introTitle: {
      type: String,
      default: '给毛孩子挑好日常'
    },
    introText: {
      type: String,
      default: '商品、订单和账户信息放在清楚的位置，需要时直接处理。'
    }
  }
}
</script>

<style scoped>
.auth-layout {
  display: grid;
  min-height: 100dvh;
  grid-template-columns: minmax(0, 1.08fr) minmax(420px, .92fr);
  background: var(--c-canvas);
}

.auth-brand {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-8) var(--space-10);
  background: var(--c-brand);
  color: var(--c-surface);
}

.brand-link {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  gap: var(--space-3);
  color: var(--c-surface);
  font-size: var(--text-title-lg-size);
  font-weight: 600;
}

.brand-link:hover {
  color: var(--c-surface);
}

.brand-link img {
  width: 44px;
  height: 44px;
  padding: var(--space-1);
  background: var(--c-surface);
  border-radius: var(--radius-12);
}

.brand-copy {
  max-width: 30em;
  margin: var(--space-10) 0;
}

.brand-copy h1 {
  color: var(--c-surface);
  font-size: var(--text-display-lg-size);
  font-weight: var(--text-display-lg-weight);
  letter-spacing: var(--text-display-lg-tracking);
  line-height: var(--text-display-lg-leading);
}

.brand-copy p {
  max-width: 34em;
  margin-top: var(--space-4);
  color: var(--c-brand-soft);
  font-size: var(--text-body-size);
  line-height: var(--text-body-leading);
}

.illustration-tray {
  max-width: 560px;
  padding: var(--space-3);
  background: var(--c-surface-warm);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e2);
}

.illustration-tray img {
  width: 100%;
  max-height: 300px;
  border-radius: var(--radius-12);
  object-fit: contain;
}

.auth-panel {
  display: grid;
  place-items: center;
  padding: var(--space-10);
  background: var(--c-surface);
}

.auth-form-wrap {
  width: min(100%, 420px);
}

.auth-heading h2 {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
  line-height: var(--text-display-leading);
}

.auth-heading p {
  margin-top: var(--space-2);
  color: var(--c-ink-muted);
  font-size: var(--text-body-size);
  line-height: var(--text-body-leading);
}

@media (max-width: 900px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    min-height: 260px;
    padding: var(--space-6);
  }

  .illustration-tray {
    display: none;
  }

  .brand-copy {
    margin: var(--space-6) 0 0;
  }

  .auth-panel {
    padding: var(--space-8) var(--space-5);
  }
}
</style>
