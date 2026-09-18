// 购物车本地存储：购物车数据不再入库，按用户区分存放在 localStorage
// 数据结构：[{ goodsId, nums, time }]，同一用户同一商品只有一条记录（与原 cart 表唯一约束行为一致）
const KEY_PREFIX = 'cart_'

function getKey(userId) {
    return KEY_PREFIX + userId
}

function read(userId) {
    try {
        return JSON.parse(localStorage.getItem(getKey(userId)) || '[]')
    } catch (e) {
        return []
    }
}

function write(userId, items) {
    localStorage.setItem(getKey(userId), JSON.stringify(items))
}

function formatTime() {
    const d = new Date()
    const pad = n => (n < 10 ? '0' + n : '' + n)
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
        + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds())
}

export default {
    // 当前用户的全部购物车项
    list(userId) {
        return read(userId)
    },
    // 加入购物车，已存在则累加数量
    add(userId, goodsId, nums) {
        const items = read(userId)
        const exist = items.find(item => item.goodsId === goodsId)
        if (exist) {
            exist.nums += nums
        } else {
            items.push({goodsId: goodsId, nums: nums, time: formatTime()})
        }
        write(userId, items)
    },
    // 修改某条记录的数量
    updateNums(userId, goodsId, nums) {
        const items = read(userId)
        const exist = items.find(item => item.goodsId === goodsId)
        if (exist) {
            exist.nums = nums
            write(userId, items)
        }
    },
    // 删除某条记录
    remove(userId, goodsId) {
        write(userId, read(userId).filter(item => item.goodsId !== goodsId))
    },
    // 批量删除（结算成功后移除已下单的购物车项）
    removeByIds(userId, goodsIds) {
        write(userId, read(userId).filter(item => !goodsIds.includes(item.goodsId)))
    },
    // 清空当前用户的购物车
    clear(userId) {
        localStorage.removeItem(getKey(userId))
    },
    // 购物车总件数（与原 /cart/count 接口口径一致：按数量累加）
    count(userId) {
        return read(userId).reduce((sum, item) => sum + (item.nums || 0), 0)
    },
}
