import { describe, expect, it } from 'vitest'
import {
  buildLlmPromptVariableTree,
  buildUpstreamVariableTree,
  extractTemplatePlaceholderRoots,
  findUndeclaredLlmPromptReferences,
  resolveLoopScopeOutputs
} from '@/views/workflow/design/composables/useUpstreamVariables'

describe('resolveLoopScopeOutputs', () => {
  it('指定次数循环包含 index 与 item', () => {
    const outputs = resolveLoopScopeOutputs({
      data: { loopType: 'count', intermediateVariables: [{ key: 'snap', type: 'string' }] }
    })
    const keys = outputs.map((o) => o.key)
    expect(keys).toContain('index')
    expect(keys).toContain('item')
    expect(keys).toContain('snap')
  })

  it('数组循环包含自定义数组参数名', () => {
    const outputs = resolveLoopScopeOutputs({
      data: {
        loopType: 'array',
        arrayParameters: [{ key: 'row', source: '{{start_1.rows}}' }]
      }
    })
    const keys = outputs.map((o) => o.key)
    expect(keys).toContain('index')
    expect(keys).toContain('row')
    expect(keys).toContain('item')
  })
})

describe('buildUpstreamVariableTree', () => {
  it('循环体内节点可引用 loop 的 index 与 item', () => {
    const nodes = [
      { id: 'start_1', data: { wfType: 'start', inputs: [] } },
      { id: 'loop_1', data: { wfType: 'loop', bodyId: 'loop_body_1', loopType: 'count', count: 3 } },
      { id: 'loop_body_1', data: { wfType: 'loop-body', loopNodeId: 'loop_1' } },
      {
        id: 'tmpl_1',
        data: { wfType: 'template-transform' },
        parentNode: 'loop_body_1',
        data: { wfType: 'template-transform', parentId: 'loop_body_1' }
      }
    ]
    nodes[3] = {
      id: 'tmpl_1',
      parentNode: 'loop_body_1',
      data: { wfType: 'template-transform', parentId: 'loop_body_1' }
    }

    const edges = [
      { source: 'start_1', target: 'loop_1' },
      { source: 'loop_1', target: 'loop_body_1', sourceHandle: 'body' },
      { source: 'loop_body_1', target: 'tmpl_1', sourceHandle: 'body-entry' }
    ]

    const tree = buildUpstreamVariableTree('tmpl_1', nodes, edges)
    const loopGroup = tree.find((g) => g.id === '__loop_scope_loop_1')
    expect(loopGroup).toBeTruthy()
    const inserts = (loopGroup.children || []).map((c) => c.insert)
    expect(inserts).toContain('{{loop_1.index}}')
    expect(inserts).toContain('{{loop_1.item}}')
  })

  it('loopNodeId 缺失时可通过 bodyId 反查循环头', () => {
    const nodes = [
      { id: 'loop_1', data: { wfType: 'loop', bodyId: 'loop_body_1', loopType: 'count' } },
      { id: 'loop_body_1', data: { wfType: 'loop-body' } },
      {
        id: 'tmpl_1',
        parentNode: 'loop_body_1',
        data: { wfType: 'template-transform', parentId: 'loop_body_1' }
      }
    ]
    const tree = buildUpstreamVariableTree('tmpl_1', nodes, [])
    expect(tree.some((g) => g.id === '__loop_scope_loop_1')).toBe(true)
  })

  it('循环体外的节点不可选 loop 的 index/item', () => {
    const nodes = [
      { id: 'start_1', data: { wfType: 'start', inputs: [] } },
      {
        id: 'loop_1',
        data: {
          wfType: 'loop',
          bodyId: 'loop_body_1',
          loopType: 'count',
          arrayParameters: [{ key: 'item', source: '' }]
        }
      },
      { id: 'loop_body_1', data: { wfType: 'loop-body', loopNodeId: 'loop_1' } },
      {
        id: 'tmpl_1',
        parentNode: 'loop_body_1',
        data: { wfType: 'template-transform', parentId: 'loop_body_1' }
      },
      { id: 'answer_main', data: { wfType: 'answer' } }
    ]
    const edges = [
      { source: 'start_1', target: 'loop_1' },
      { source: 'loop_1', target: 'answer_main', sourceHandle: 'flow-out' }
    ]

    const tree = buildUpstreamVariableTree('answer_main', nodes, edges)
    expect(tree.some((g) => g.id === '__loop_scope_loop_1')).toBe(false)

    const loopGroup = tree.find((g) => g.id === 'loop_1')
    expect(loopGroup).toBeTruthy()
    const inserts = (loopGroup.children || []).map((c) => c.insert)
    expect(inserts).not.toContain('{{loop_1.index}}')
    expect(inserts).not.toContain('{{loop_1.item}}')
    expect(inserts).toContain('{{loop_1.results}}')
    expect(inserts).toContain('{{loop_1.count}}')
  })

  it('选中循环体容器本身不展示循环迭代变量', () => {
    const nodes = [
      { id: 'loop_1', data: { wfType: 'loop', bodyId: 'loop_body_1', loopType: 'count' } },
      { id: 'loop_body_1', data: { wfType: 'loop-body', loopNodeId: 'loop_1' } }
    ]
    const tree = buildUpstreamVariableTree('loop_body_1', nodes, [])
    expect(tree.some((g) => g.id === '__loop_scope_loop_1')).toBe(false)
  })
})

describe('buildLlmPromptVariableTree', () => {
  it('builds tree from declared input variables only', () => {
    const tree = buildLlmPromptVariableTree([
      { key: 'question', value: '{{start_1.input}}' },
      { key: 'context', value: '{{kb_1.contextText}}' }
    ])
    expect(tree).toHaveLength(1)
    expect(tree[0].label).toBe('本节点输入参数')
    expect(tree[0].children.map((c) => c.insert)).toEqual(['{{question}}', '{{context}}'])
  })
})

describe('findUndeclaredLlmPromptReferences', () => {
  it('flags upstream refs not declared as input params', () => {
    const undeclared = findUndeclaredLlmPromptReferences(
      'Q: {{question}} 上游: {{start_1.input}}',
      ['question']
    )
    expect(undeclared).toEqual(['start_1'])
  })

  it('extracts nested placeholder roots', () => {
    expect(extractTemplatePlaceholderRoots('{{context.title}} and {{items[0]}}')).toEqual([
      'context',
      'items'
    ])
  })
})
