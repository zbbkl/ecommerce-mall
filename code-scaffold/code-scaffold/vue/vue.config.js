// 导入Vue CLI的配置函数
const { defineConfig } = require('@vue/cli-service')

// 导出Vue项目的配置对象
module.exports = defineConfig({
    //  transpileDependencies：是否对node_modules中的依赖进行Babel转译
    //  设置为true时，会将依赖中不兼容的ES6+语法转换为ES5，确保低版本浏览器兼容
    transpileDependencies: true,

    // 开发服务器配置（npm run serve时生效）
    devServer: {
        port: 8080,  // 设置开发环境启动的端口号为8080（默认8080，可根据需要修改）

        // 路由使用 history 模式（见 src/router/index.js），必须开启回退：
        // 否则直接访问 /login、/register 或刷新任意子路由，dev server 都会返回 404。
        // 开启后所有未命中静态文件的请求都会回退到 index.html，交给 vue-router 处理。
        historyApiFallback: true
    },

    // 链式操作Webpack配置（更精细地定制Webpack行为）
    chainWebpack: config => {
        // 配置标题
        config.plugin('html').tap(args => {
                args[0].title = "捞宝购物";
                return args;
            })
    }
})

