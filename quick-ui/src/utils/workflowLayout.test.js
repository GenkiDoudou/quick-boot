import { describe, expect, it } from 'vitest'
import { optimizeWorkflowLayout } from '@/views/workflow/design/utils/workflowLayout'

describe('optimizeWorkflowLayout', () => {
  it('主图节点按 LR 方向展开', () => {
    const nodes = [
      { id: 'start_1', data: { wfType: 'start' }, position: { x: 0, y: 0 } },
      { id: 'llm_1', data: { wfType: 'llm' }, position: { x: 0, y: 200 } },
      { id: 'end_1', data: { wfType: 'end' }, position: { x: 0, y: 400 } }
    ]
    const edges = [
      { id: 'e1', source: 'start_1', target: 'llm_1' },
      { id: 'e2', source: 'llm_1', target: 'end_1' }
    ]

    const { nodes: laidOut } = optimizeWorkflowLayout(nodes, edges)
    const start = laidOut.find((n) => n.id === 'start_1')
    const llm = laidOut.find((n) => n.id === 'llm_1')
    const end = laidOut.find((n) => n.id === 'end_1')

    expect(llm.position.x).toBeGreaterThan(start.position.x)
    expect(end.position.x).toBeGreaterThan(llm.position.x)
  })

  it('循环体定位在循环头下方且体内子节点有坐标', () => {
    const nodes = [
      { id: 'loop_1', data: { wfType: 'loop', bodyId: 'loop_body_1' }, position: { x: 100, y: 80 } },
      {
        id: 'loop_body_1',
        data: { wfType: 'loop-body', loopNodeId: 'loop_1' },
        position: { x: 60, y: 200 },
        style: { width: '380px', height: '260px' }
      },
      {
        id: 'tmpl_1',
        data: { wfType: 'template-transform' },
        parentNode: 'loop_body_1',
        position: { x: 100, y: 10 }
      },
      {
        id: 'assign_1',
        data: { wfType: 'loop-set-variable' },
        parentNode: 'loop_body_1',
        position: { x: 200, y: 10 }
      }
    ]
    const edges = [
      { id: 'eb', source: 'loop_1', target: 'loop_body_1', sourceHandle: 'body', targetHandle: 'body-in' },
      { id: 'e1', source: 'loop_body_1', target: 'tmpl_1', sourceHandle: 'body-entry' },
      { id: 'e2', source: 'tmpl_1', target: 'assign_1' },
      { id: 'e3', source: 'assign_1', target: 'loop_body_1', targetHandle: 'body-exit' }
    ]

    const { nodes: laidOut } = optimizeWorkflowLayout(nodes, edges)
    const loop = laidOut.find((n) => n.id === 'loop_1')
    const body = laidOut.find((n) => n.id === 'loop_body_1')
    const tmpl = laidOut.find((n) => n.id === 'tmpl_1')

    expect(body.position.y).toBeGreaterThan(loop.position.y)
    expect(tmpl.position.x).toBeGreaterThan(0)
    expect(parseInt(String(body.style.width), 10)).toBeGreaterThanOrEqual(380)
  })
})
