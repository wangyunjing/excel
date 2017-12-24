# 使用限制
- 不支持中国式表格(支持简单的表格:一行表示一条数据)
- 不支持基本类型(例如: int)
- 支持xls和xlsx格式的Excel

# 字段标注注解
`@Excel` 用于标示导出/导入类中的字段

`@Nesting` 用于表示嵌套 
如果2个注解同时出现，Excel不会起作用

属性:

- `name` : 表示标题名称
- `order` : 表示标题的顺序

# 类型转换
`ConverterSupport` : 全局(有一些默认配置, 可以替换)

通过 `addConvert` 方法可以加入自定义的转换类型 
# 导出Excel
## 同步
`ExportExcel.syncExport`
## 异步
`ExportExcel.asyncExport`
# 导入Excel

## 同步
`ImportExcel.syncImport`
## 异步
`ImportExcel.asyncImport`
# 使用方式
参考 单元测试