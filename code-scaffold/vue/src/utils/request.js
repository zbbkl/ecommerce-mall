import axios from 'axios'
import router from "@/router";
import { Notification } from 'element-ui'

// 创建一个新的axios实例
const request = axios.create({
    baseURL: 'http://localhost:9999',   // 后端接口基础地址
    timeout: 30000 // 请求超时时间：30秒（超过该时间未响应则视为请求失败）
})

// request 拦截器，可以自请求发送前对请求做一些处理，比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    let user = JSON.parse(localStorage.getItem("user") || '{}')
    // 将用户token添加到请求头（用于后端身份验证）
    config.headers['token'] = user.token

    return config
}, error => {
    Notification.error({message: '网络连接超时', duration: 3000, showClose: false})
    return Promise.reject(error)
});

// 响应拦截器（接口响应后的处理）作用：统一处理响应数据、拦截错误状态码（如401未登录）等
request.interceptors.response.use(
    response => {
        let res = response.data;

        // 兼容服务端返回的字符串类型数据（如果后端返回JSON字符串，自动解析为对象）
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }

        if (res.code === '401') {
            Notification.error({message: res.msg, duration: 3000, showClose: false})
            localStorage.removeItem('user')
            router.push('/login')
        }
        return res;
    }, error => {
        // 后端现在返回真实 HTTP 状态码：401 未登录、403 无权限、500 系统错误
        const status = error.response ? error.response.status : null
        const data = error.response ? error.response.data : null
        const msg = (data && data.msg) ? data.msg : null

        if (status === 401) {
            Notification.error({message: msg || '登录已失效，请重新登录', duration: 3000, showClose: false})
            localStorage.removeItem('user')
            router.push('/login')
        } else if (status === 403) {
            Notification.error({message: msg || '无权限访问', duration: 3000, showClose: false})
        } else if (status === 500) {
            Notification.error({message: msg || '系统错误', duration: 3000, showClose: false})
        } else {
            Notification.error({message: '网络连接超时', duration: 3000, showClose: false})
        }
        return Promise.reject(error)
    }
)

export default request
