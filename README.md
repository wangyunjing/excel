# 使用限制
- 不支持嵌套类
- 不支持中国式表格(支持简单的表格:一行表示一条数据)
- 只支持基本类型对应的对象(例如: int --> Integer)

# 字段标注注解
`@Excel` 用于标示导出/导入类中的字段

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
参考 `ExportExcel.main` 和 `ImportExcel.main`