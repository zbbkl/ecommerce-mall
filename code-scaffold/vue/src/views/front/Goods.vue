<template>
  <div class="goods-page content-shell">
    <header class="page-heading">
      <div>
        <h1>全部商品</h1>
        <p>按分类或关键词查找宠物主粮、护理、玩具和零食。</p>
      </div>
      <form class="search-form" role="search" @submit.prevent="loadGoods(1)">
        <label class="sr-only" for="goods-search">搜索商品</label>
        <input
          id="goods-search"
          v-model.trim="keyboard"
          class="search-input"
          type="search"
          autocomplete="off"
          placeholder="输入商品名称…"
        />
        <button class="search-submit" type="submit">
          <i class="el-icon-search" aria-hidden="true"></i>
          搜索
        </button>
      </form>
    </header>

    <section class="filter-panel" aria-label="商品分类">
      <button
        class="category-filter"
        :class="{ 'category-filter--active': selectedCategoryId === 0 }"
        type="button"
        :aria-pressed="selectedCategoryId === 0"
        @click="handleAllClick"
      >
        全部
      </button>
      <button
        v-for="category in types"
        :key="category.id"
        class="category-filter"
        :class="{ 'category-filter--active': selectedCategoryId === category.id }"
        type="button"
        :aria-pressed="selectedCategoryId === category.id"
        @click="handleCategoryClick(category)"
      >
        {{ category.name }}
      </button>
    </section>

    <section class="result-panel" aria-live="polite">
      <div class="result-meta">
        <p>共找到 <strong>{{ total }}</strong> 件商品</p>
      </div>

      <div v-if="goods.length" class="product-grid">
        <ProductCard
          v-for="item in goods"
          :key="item.id"
          :goods="item"
          :category-name="item.typeName"
          @add="addToCart"
        />
      </div>

      <el-empty
        v-else
        :image-size="220"
        :image="require('@/assets/empty.svg')"
        description="没有找到符合条件的商品"
      />

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handleCurrentChange"
        />
      </div>
    </section>
  </div>
</template>

<script>
import ProductCard from '@/conponents/ProductCard.vue'
import cartMixin from '@/mixins/cartMixin'

export default {
  name: 'Goods',
  components: { ProductCard },
  mixins: [cartMixin],
  data() {
    return {
      types: [],
      selectedCategoryId: parseInt(this.$route.query.selectedCategoryId) || 0,
      total: 0,
      pageNum: 1,
      pageSize: 8,
      keyboard: '',
      goods: []
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  watch: {
    '$route.query.selectedCategoryId'(value) {
      this.selectedCategoryId = parseInt(value) || 0
      this.loadGoods(1)
    }
  },
  methods: {
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadGoods(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/goods/selectPage/type', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.keyboard,
          typeId: this.selectedCategoryId
        }
      }).then(res => {
        this.goods = Array.isArray(res.data?.records) ? res.data.records : []
        this.total = res.data?.total || 0
      })
    },
    handleAllClick() {
      this.selectedCategoryId = 0
      this.syncCategoryQuery(0)
    },
    handleCategoryClick(category) {
      this.selectedCategoryId = category.id
      this.syncCategoryQuery(category.id)
    },
    syncCategoryQuery(value) {
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: value }
      })
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.loadGoods()
    }
  }
}
</script>

<style scoped>
.goods-page {
  min-height: 70vh;
  padding-top: var(--space-8);
  padding-bottom: var(--space-16);
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-8);
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

.search-form {
  display: flex;
  min-width: min(100%, 360px);
}

.search-input {
  min-width: 0;
  height: 44px;
  flex: 1;
  padding: 0 var(--space-4);
  background: var(--c-surface);
  border: 1px solid var(--c-line-strong);
  border-right: 0;
  border-radius: var(--radius-8) 0 0 var(--radius-8);
  color: var(--c-ink-body);
  outline: none;
  transition: border-color var(--duration-state) var(--ease-out), box-shadow var(--duration-state) var(--ease-out);
}

.search-input:focus {
  border-color: var(--c-brand);
  box-shadow: var(--focus-ring);
}

.search-input::placeholder {
  color: var(--c-ink-subtle);
}

.search-submit {
  display: inline-flex;
  min-width: 96px;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: 0 var(--space-5);
  background: var(--c-brand);
  border: 1px solid var(--c-brand);
  border-radius: 0 var(--radius-8) var(--radius-8) 0;
  color: var(--c-surface);
  cursor: pointer;
  font-weight: 600;
}

.search-submit:hover {
  background: var(--c-brand-hover);
  border-color: var(--c-brand-hover);
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-8);
  padding: var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-line);
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-e1);
}

.category-filter {
  min-height: 36px;
  padding: 0 var(--space-4);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  color: var(--c-ink-body);
  cursor: pointer;
  font-size: var(--text-caption-size);
  font-weight: 500;
  transition: background-color var(--duration-state) var(--ease-out), border-color var(--duration-state) var(--ease-out), color var(--duration-state) var(--ease-out);
}

.category-filter:hover {
  background: var(--c-surface-sunken);
  color: var(--c-brand);
}

.category-filter--active {
  background: var(--c-brand-soft);
  border-color: var(--c-brand-soft);
  color: var(--c-brand);
}

.result-panel {
  margin-top: var(--space-6);
}

.result-meta {
  margin-bottom: var(--space-4);
  color: var(--c-ink-muted);
  font-size: var(--text-caption-size);
}

.result-meta strong {
  color: var(--c-ink);
  font-size: var(--text-title-size);
  font-variant-numeric: tabular-nums;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-5);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-8);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 760px) {
  .page-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .search-form {
    width: 100%;
    min-width: 0;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
