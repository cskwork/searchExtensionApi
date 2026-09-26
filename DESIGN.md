---
name: 검색 API 진단 콘솔
description: 장애 진단 콘솔
colors:
  ink: '#20252d'
  muted: '#535e6c'
  action: '#252b34'
  action-hover: '#0f1216'
  served: '#1d6b3a'
  outage: '#8a5a00'
  line: '#d7dde5'
  surface: '#ffffff'
  code-ink: '#e9eef6'
  error: '#9a2b1f'
  field: '#8b96a4'
  focus: '#527dde'
typography:
  title:
    fontFamily: system-ui, -apple-system, "Apple SD Gothic Neo", "Noto Sans KR", "Malgun
      Gothic", sans-serif
    fontSize: 28px
    lineHeight: 1.25
  body:
    fontFamily: system-ui, -apple-system, "Apple SD Gothic Neo", "Noto Sans KR", "Malgun
      Gothic", sans-serif
    fontSize: 16px
    lineHeight: 1.55
  code:
    fontFamily: ui-monospace, "SFMono-Regular", Menlo, Consolas, monospace
    fontSize: 13px
    lineHeight: 1.65
rounded:
  control: 6px
  surface: 8px
spacing:
  field: 18px
  code: 20px
  section: 24px
  column: 28px
components:
  button-primary:
    backgroundColor: '{colors.action}'
    textColor: '#ffffff'
    rounded: '{rounded.control}'
    padding: 0.6rem 1.2rem
  button-primary-hover:
    backgroundColor: '{colors.action-hover}'
  input:
    backgroundColor: '#ffffff'
    textColor: '{colors.ink}'
    rounded: '{rounded.control}'
    padding: 0.5rem 0.6rem
---

# Design System: 검색 API 진단 콘솔

## Overview

**Creative North Star: "장애 진단 콘솔"**

White request controls and graphite JSON surfaces distinguish input from response. Since 2026-09-26 the console chrome and actions are graphite (`#1b2027` header band, `#252b34` primary action, ink underline on the selected tab); colour is reserved for response state: green served/HTTP 200, amber for the Naver share in source distribution, red for configured outage or failure. Recent requests and popularity follow the main experiment; contract details remain expandable reference material.

No original frontend exists: this console is newly authored compatibility UI for the original Java endpoints. index.html uses synthetic responses and blocks outbound connections; live.html uses native same-origin endpoints. Keep the environment label and history separation visible.

Evidence: [portfolio-demo/src/styles.css](portfolio-demo/src/styles.css) and [portfolio-demo/src/index.html](portfolio-demo/src/index.html). This document records the implemented cascade on 2026-09-26. The 2026-09-26 workbench refresh was written without browser access; root performs the rendered desktop/mobile review. Screen-reader output and real keyboard behavior remain unverified until then.

## Colors

The frontmatter is the normative inventory: action for primary work, ink for readable content, muted for secondary facts, line for separation, and surface for supporting regions. Errors retain textual feedback alongside color. No new raster assets were added.

## Typography

Use the actual system/Korean sans stack in the frontmatter. Do not require a downloaded font to complete a task. Titles establish hierarchy through size and weight. Monospace is reserved for endpoint parameters, code, response data and hashes.

## Layout

The 1440px console uses a 300–380px request column and flexible response column separated by 28px; request content has 28px inset before its right divider. At 1000px columns tighten and history uses three columns. At 860px request, response, history, popularity and contract stack; at 520px history uses one column. Code scrolls within its own bounded pane.

## Elevation & Depth

Task surfaces use flat fills and dividers. The documented components do not add decorative shadows.

## Shapes

Controls use 6px corners; supporting surfaces use 8px. Rows use a bottom divider. Response tabs have zero radius and a 3px bottom selection line; that is not a rounded card border.

## Components

- Header: one dark console band — title and environment link on one row, lede below — so request and response panels start in the first viewport.
- Request route (`#route-flow`): four joined steps 요청 → 카카오 1순위 → 네이버 대체 → 응답. Before a request it mirrors the simulated provider radios (정상 설정/장애 설정, 요청 대기); after a response it marks the served provider, providers not called, configured outages and the HTTP outcome. Live mode shows only the provider inferred from the returned document format and `응답에 기록 없음` for the rest; no latency, retry or timing is displayed because no response carries them.
- Result documents are bordered cards (title, source · date, expandable detail), clearly separate from the dark JSON pane and the trace list.
- Recent requests start with a response-source distribution (SVG bar + legend) counted from the stored history entries only.
- Primary action: action fill, white label, action-hover state and visible focus ring.
- Inputs: white fill, field border, control radius and accent caret. Keep labels and errors adjacent.
- Navigation: preserve the current-state indication and native links.
- Records: use separators and concrete status text. Empty content has an explicit message.
- Responsive and reduced-motion rules remain in the source stylesheet.

The companion [.impeccable/design.json](.impeccable/design.json) contains 7 isolated HTML/CSS samples of these implemented patterns. They are illustrative samples, not live application controls.

## Do's and Don'ts

- Do preserve original route, field and listener contracts.
- Do identify synthetic data and the selected execution environment.
- Do retain visible focus and reduced-motion treatment.
- Do use source values when extending an existing component.
- Don't claim a native provider succeeded from a synthetic demo result.
- Don't treat this code-only review as visual approval.
