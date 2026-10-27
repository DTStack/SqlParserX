# SqlParserX UI

SqlParserX 的 Vue 2 + Vite 前端工具集。生产环境与后端服务同源部署：

```text
页面：https://sqlparserx.dtstack.com/
接口：https://sqlparserx.dtstack.com/api/...
```

`VITE_API_URL` 只在统一请求客户端中配置，业务 API 文件只声明 `api/...` 这样的相对路径。开发环境由 Vite 将 `/api` 转发到 `http://localhost:16758`；Docker 构建时默认注入 `VITE_API_URL=/`。
