<template>
  <div class="collect-page content-shell">
    <header class="page-heading">
      <h1>我的收藏</h1>
      <p>收藏的商品集中在这里，下架或信息变化以商品详情页为准。</p>
    </header>

    <section class="collect-list" aria-live="polite">
      <div v-if="collects.length" class="product-grid">
        <ProductCard
          v-for="item in collects"
          v-if="item.goods"
          :key="item.id"
          :goods="item.goods"
          :category-name="item.goods && item.goods.typeName"
          :show-add="false"
        >
          <template v-slot:actions>
            <button class="remove-favorite" type="button" @click="del(item)">取消收藏</button>
          </template>
        </ProductCard>
      </div>
      <el-empty v-else :image-size="220" :image="require('@/assets/empty.svg')" description="还没有收藏商品" />
    </section>
  </div>
</template>

<script>
import ProductCard from '@/conponents/ProductCard.vue'

export default {
  name: 'Collect',
  components: { ProductCard },
  data() {
    return { collects: [] }
  },
  created() {
    this.loadCollect()
  },
  methods: {
    loadCollect() {
      this.$request.get('/collect/myCollect').then(res => {
        this.collects = Array.isArray(res.data) ? res.data : []
      })
    },
    del(row) {
      this.$request.delete('/collect/delete?id=' + row.id).then(res => {
        if (res.code === '200') {
          this.$notify.success({ title: '成功', message: '已取消收藏', showClose: false, duration: 2000 })
        } else {
          this.$notify.error({ title: '错误', message: res.msg, showClose: false, duration: 2000 })
        }
        this.loadCollect()
      })
    }
  }
}
</script>

<style scoped>
.collect-page {
  min-height: 70vh;
  padding-top: var(--space-8);
  padding-bottom: var(--space-16);
}

.page-heading h1 {
  font-size: var(--text-display-size);
  font-weight: var(--text-display-weight);
  letter-spacing: var(--text-display-tracking);
  line-height: var(--text-display-leading);
}

.page-heading p {
  margin-top: var(--space-2);
  color: var(--c-ink-muted);
  font-size: var(--text-body-size);
}

.collect-list {
  margin-top: var(--space-8);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-5);
}

.remove-favorite {
  min-height: 40px;
  padding: 0 var(--space-4);
  background: var(--c-accent-soft);
  border: 1px solid var(--c-accent-soft);
  border-radius: var(--radius-8);
  color: var(--c-accent);
  cursor: pointer;
  font-size: var(--text-caption-size);
  font-weight: 600;
}

.remove-favorite:hover {
  background: var(--c-accent);
  border-color: var(--c-accent);
  color: var(--c-surface);
}

@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
