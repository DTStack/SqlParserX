import axios from 'axios'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 5000
})

// request 拦截器
service.interceptors.request.use(
    config => {
        // 在这里可以设置请求头、请求参数等return config
        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)

// response 拦截器
service.interceptors.response.use(
    response => {
        // 在这里处理返回数据const { data } = response
        if (response.data.code !== 200) {
            const message = response.data.msg || '请求失败'
            console.error('Error:', message)
            return Promise.reject(new Error(message))
        } else {
            return response.data
        }
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)
export default service;
