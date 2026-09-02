/**
 * ${tableComment!} 页 schema：检索列、表格列、表单初始值与校验（codegen）。
 */
export const rowKey = '${pkField!"id"}'
export const permPrefix = '${permissionPrefix}'

export const defaultSearch = {
<#list queryColumns as col>
  ${col.javaField}: ''<#if col_has_next>,</#if>
</#list>
}

export const searchColumns = [
<#list queryColumns as col>
  { prop: '${col.javaField}', label: '${col.columnComment!}', type: 'input', span: 8 }<#if col_has_next>,</#if>
</#list>
]

export const tableColumns = [
<#list listColumns as col>
  { prop: '${col.javaField}', label: '${col.columnComment!}', minWidth: 120, showOverflowTooltip: true },
</#list>
  { prop: 'action', label: '操作', width: 160, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

export function formInitial() {
  return {
<#if pkColumn??>
    ${pkColumn.javaField}: null,
</#if>
<#list editColumns as col>
    ${col.javaField}: ''<#if col_has_next>,</#if>
</#list>
  }
}

export const formRules = {
<#list editColumns as col>
  <#if col.isRequired == "1">
  ${col.javaField}: [{ required: true, message: '必填', trigger: 'blur' }]<#if col_has_next>,</#if>
  </#if>
</#list>
}
