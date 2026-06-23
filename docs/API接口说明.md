# API 接口说明

后端基础地址：`http://localhost:8087/api`

数据库：MySQL `campus_career`

除注册、登录、岗位公开查询外，其他接口需要在请求头携带：

```text
X-Token: 登录后返回的 token
```

## 用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /auth/register | 学生/公司人员注册，管理员不开放注册 |
| POST | /auth/login | 用户登录 |
| POST | /auth/logout | 退出登录 |
| GET | /auth/me | 获取当前用户 |

## 岗位接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /jobs | 岗位列表，支持 keyword、city、status |
| GET | /jobs/{id} | 岗位详情 |
| POST | /jobs | 公司人员/管理员新增岗位 |
| PUT | /jobs/{id} | 公司人员/管理员编辑岗位 |
| DELETE | /jobs/{id} | 公司人员/管理员删除岗位 |

## 简历接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /resume | 获取当前学生简历 |
| PUT | /resume | 保存当前学生简历 |

## 投递接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /applications/jobs/{jobId} | 学生投递岗位 |
| GET | /applications/mine | 学生查看自己的投递 |
| POST | /applications/{id}/cancel | 学生撤回投递 |
| GET | /applications | 公司人员/管理员查看全部投递 |
| PUT | /applications/{id}/status | 公司人员/管理员修改投递状态 |

## 统计接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /dashboard/stats | 公司人员/管理员统计看板 |

## 背景图接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /backgrounds | 获取各页面背景图配置 |
| POST | /backgrounds/{pageKey} | 管理员上传并替换指定页面背景图，表单字段为 file |

## AI 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /ai/prompt-test | 不需要 token，仅用提示词测试百炼 AI 调用 |
| POST | /ai/resume-review | 学生上传简历文件并发送给百炼 AI 生成修改建议，表单字段为 file、prompt |
