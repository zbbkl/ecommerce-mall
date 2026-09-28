import cart from '@/utils/cart'

export default {
  methods: {
    addToCart(goods, quantity = 1) {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      if (!user.id) {
        this.$message.warning('请先登录后加入购物车')
        this.$router.push('/login')
        return false
      }

      const amount = Math.max(1, Number(quantity) || 1)
      const stock = Number(goods.store)
      if (!Number.isFinite(stock) || stock <= 0) {
        this.$message.warning('商品暂时缺货')
        return false
      }

      const existing = cart.list(user.id).find(item => item.goodsId === goods.id)
      const current = existing ? Number(existing.nums) || 0 : 0
      if (current + amount > stock) {
        this.$message.warning('已达到当前库存上限')
        return false
      }

      cart.add(user.id, goods.id, amount)
      this.$message.success('已加入购物车')
      this.$emit('update:cart')
      return true
    }
  }
}
