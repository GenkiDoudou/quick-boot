## Context

See `proposal.md` for motivation. Product decisions are fixed in `docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md` §5（控制台）. This change only materializes that console as a **static mock prototype**, following the repo’s existing `docs/demo/*.html` + `create-html` CDN pattern.

## Goals / Non-Goals

**Goals:**

- One HTML file covering four console sections with realistic mock data.
- Interaction fidelity high enough to review IA (navigation, filters, detail drawer, TOP → jump).
- Charts via lightweight CDN chart lib (e.g. ECharts) or simple SVG/CSS if chart CDN is undesirable; prefer ECharts for trend readability.

**Non-Goals:**

- Real ingest, aggregation, persistence, or Webhook.
- Pixel-perfect production theming beyond a clean admin console look.
- Separate HTML files per section.
- Implementing Web SDK or backend tables in this change.

## Decisions

1. **Single-page section switcher (not multi-file)**  
   - Rationale: matches “一页看齐一期控制台” review flow; TOP jump can switch `activeMenu` + set filter state.  
   - Alternative: four HTML files — harder to demo cross-page jumps.

2. **Vue 3 + Element Plus via jsDelivr CDN**  
   - Rationale: aligns with `create-html` skill and other demos; no Node build.  
   - Alternative: plain HTML — weaker tables/drawers.

3. **ECharts for trends**  
   - Rationale: overview + API trends need multi-series lines; Element Plus has no chart.  
   - Alternative: fake sparklines — poorer review signal.

4. **Admin light theme (not ops dark wall)**  
   - Rationale: this is a product console prototype, not a NOC 大屏; keep Element Plus default-ish layout.  
   - Alternative: reuse `system-monitor-dashboard` dark style — visually conflates with现有态势大屏.

5. **Mock-only appId `web-admin`**  
   - Rationale: design reserves multi-app but phase-1 is single app; show read-only tag.

## Risks / Trade-offs

- [CDN offline] → Mitigation: document “需能访问 jsDelivr”；不打包 vendor。  
- [原型被误当已实现] → Mitigation: page banner 标明「静态 Mock / 非现网」。  
- [图表数据与色块阈值不一致] → Mitigation: mock 常量集中定义，KPI 色块按设计阈值计算。

## Migration Plan

- Add file under `docs/demo/`; no deploy/rollback for production apps.  
- Future real console may reference this HTML as UI reference only; no runtime dependency.
