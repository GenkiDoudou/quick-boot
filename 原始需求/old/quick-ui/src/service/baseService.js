import request from '../utils/request';

// 常用的方法调用

export default {

    /**
     * 删除
     * @param path
     * @param params
     * @returns
     */
    delete(path, params) {
        return request({
            url: path,
            data: params,
            method: "DELETE"
        });
    },

    get(path, params, headers) {
        return new Promise((resolve, reject) => {
            request({
                url: path,
                params,
                headers,
                method: "GET"
            })
                .then(resolve)
                .catch((error) => {
                    if (error !== "-999") {
                        reject(error);
                    }
                });
        });
    },

    getDow(path, params, headers) {
        return new Promise((resolve, reject) => {
            request({
                url: path,
                params,
                headers,
                responseType: "blob",
                method: "GET"
            })
                .then(resolve)
                .catch((error) => {
                    if (error !== "-999") {
                        reject(error);
                    }
                });
        });
    },

    put(path, params, headers) {
        return request({
            url: path,
            data: params,
            headers: {
                "Content-Type": "application/json;charset=UTF-8",
                ...headers
            },
            method: "PUT"
        });
    },

    /**
     * 通用post方法
     * @param path
     * @param body
     * @returns
     */
    post(path, body, headers) {
        return request({
            url: path,
            method: "POST",
            headers: {
                "Content-Type": "application/json;charset=UTF-8",
                ...headers
            },
            data: body
        });
    },

    /**
     * 用于文件上传的post方法
     * @param path
     * @param file
     * @returns
     */
    postfile(path, file, headers) {
        // 文件上传
        let formData = new FormData();
        formData.append("file", file);
        return request({
            url: path,
            method: "POST",
            headers: {
                "Content-Type": "multipart/form-data",
                ...headers
            },
            data: formData
        });
    }
}
