export default [
    {
        url: '/mockapi/crud/list',
        method: 'post',
        response: ({query}) => {
            return {
                code: 200,
                msg: '成功',
                data: {}
            }
        }
    },
    {
        url: '/mockapi/crud/list2',
        method: 'get',
        response: ({query}) => {
            return {
                code: 200,
                msg: '成功',
                data: {}
            }
        }
    }
]
