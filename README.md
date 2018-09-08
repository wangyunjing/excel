# 使用限制
- 不支持中国式表格(支持简单的表格:一行表示一条数据)
- 支持xls和xlsx格式的Excel
- 类的字段必须要存在get和set方法

# 字段标注注解
## `@Excel` 
用于标示导出/导入类中的字段

### 属性:
- `name` : 表示标题名称
- `order` : 表示标题的顺序
- `emptyToNull` : 默认为true; 如果是空字符串"", 则该属性为null

## `@Nesting` 
用于表示嵌套 

## 提示
如果2个注解同时出现，Excel不会起作用



# 类型转换
ConverterService

通过 `addConvert` 方法可以加入自定义的转换类型 

如果没有对应的某一类型转换成String, 那么默认使用toString()

# 导出Excel
## 同步
`ExportExcel.syncExport`
## 异步
`ExportExcel.asyncExport`
# 导入Excel
`ImportExcel.execute`
## 导入配置选项 
`ImportExcelOptions`

# 使用方式
参考 单元测试
