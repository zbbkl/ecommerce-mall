---
version: alpha
name: Laobao Larder
description: A warm, legible pet-food marketplace and compact operations system built around a hand-drawn cat mascot, paper-toned surfaces, olive-green actions, brick-red emphasis, and product imagery treated as physical trays rather than full-bleed hero art.
colors:
  canvas: "#FBFAF3"
  surface: "#FFFFFF"
  surface-sunken: "#F4F2E7"
  ink: "#14150F"
  ink-body: "#3A3D33"
  ink-muted: "#6B6F63"
  ink-subtle: "#9AA093"
  line: "#E6E4D8"
  line-strong: "#CFCDBF"
  brand: "#3E5A20"
  brand-hover: "#4C6B2A"
  brand-soft: "#EAF0DC"
  accent: "#B4432F"
  accent-soft: "#F7E7E2"
  flag: "#F2D25C"
  success: "#2F7D4F"
  warning: "#C98A16"
  info: "#4A6B7C"
typography:
  font-ui:
    fontFamily: "'PingFang SC', 'HarmonyOS Sans SC', 'MiSans', 'Microsoft YaHei', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif"
  display-lg:
    fontSize: 32px
    lineHeight: 1.2
    fontWeight: 600
    letterSpacing: -0.6px
  display:
    fontSize: 26px
    lineHeight: 1.25
    fontWeight: 600
    letterSpacing: -0.4px
  title-lg:
    fontSize: 20px
    lineHeight: 1.3
    fontWeight: 600
    letterSpacing: -0.2px
  title:
    fontSize: 16px
    lineHeight: 1.4
    fontWeight: 600
  body:
    fontSize: 14px
    lineHeight: 1.6
    fontWeight: 400
  body-strong:
    fontSize: 14px
    lineHeight: 1.6
    fontWeight: 500
  caption:
    fontSize: 13px
    lineHeight: 1.5
    fontWeight: 400
  micro:
    fontSize: 12px
    lineHeight: 1.4
    fontWeight: 500
  price-lg:
    fontSize: 28px
    lineHeight: 1.1
    fontWeight: 700
  price:
    fontSize: 20px
    lineHeight: 1.1
    fontWeight: 700
rounded:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  full: 999px
spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 20px
  xl: 24px
  xxl: 32px
  3xl: 40px
  4xl: 48px
  5xl: 64px
  6xl: 80px
  7xl: 96px
components:
  button-primary:
    backgroundColor: "{colors.brand}"
    textColor: "{colors.surface}"
    rounded: "{rounded.sm}"
    height: 40px
  product-card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.line}"
    rounded: "{rounded.md}"
    shadow: "{shadow.e1}"
  image-tray:
    backgroundColor: "{colors.surface-sunken}"
    borderColor: "{colors.line}"
    rounded: "{rounded.md}"
  recipe-label:
    textColor: "{colors.ink-body}"
    dividerColor: "{colors.line}"
    numericFeature: tabular-nums
  table-header:
    backgroundColor: "{colors.surface-sunken}"
    textColor: "{colors.ink}"
  status-warning:
    backgroundColor: "{colors.flag}"
    textColor: "{colors.ink}"
---

# 捞宝购物 · 鲜食铺系统

## Direction

The interface should feel like a carefully run neighborhood pet-food shop: bright paper-toned canvas, useful labels, clear ingredients, calm olive actions, and brick-red attention only where price, stock pressure, or irreversible action needs emphasis.

The hand-drawn cat logo is the brand anchor. Orange was scaffolding and is not a brand color.

## Signature Elements

### Recipe Label

A three-row, hairline-separated information strip on product cards and detail pages. Use real product data only.

Preferred mapping for the current schema:

- `分类`: `typeName`
- `库存`: `store`
- `热度`: `sales`

Never invent ingredients, pet age, or package size. Those require real schema fields.

### Image Tray

Small 7–53 KB product images sit inside a shallow `surface-sunken` tray with an inner hairline. The image remains the subject; the tray supplies depth. Do not use full-bleed product photography or oversized gradient panels.

## Layout

- Front storefront: maximum content width `1280px`, 24px side gutters.
- Product grid: `repeat(auto-fill, minmax(240px, 1fr))`.
- Admin and merchant consoles: 200px fixed sidebar, fluid content, 16px page padding, 40px table rows.
- Mobile content collapses to one column without overlapping negative margins.
- Use `min-height: 100dvh`, not `100vh`, for full-height layouts.

## Motion

- State color: 120ms.
- Button and card response: 200ms.
- Overlays: 320ms.
- Entry sequences: 500ms, only when sequencing communicates hierarchy.
- Use `--ease-out` from `tokens.css`.
- Animate only transform, opacity, and color.
- Honor `prefers-reduced-motion`.

## Accessibility

- Body text is at least 14px.
- `#6B6F63` on canvas is approved for text at 4.92:1.
- `#9AA093` is decorative or disabled only and must not carry essential text.
- `#C98A16` is an icon or border color; warning text uses ink on the yellow flag surface.
- Every interactive control needs a visible `:focus-visible` state.
- Never use color as the only status signal.
- All meaningful images need accurate `alt` text.
- Icon-only buttons need an accessible name.

## Do

- Use olive green for primary actions and selected states.
- Use brick red for price emphasis, stock pressure, and destructive actions.
- Use yellow sparingly for new or limited flags.
- Use tabular numerals for prices, stock, counts, and statistics.
- Keep cards quiet; only interactive cards receive improved border and shadow.

## Don't

- Do not use `#ff6700` or orange gradients.
- Do not use full-width gradient headers.
- Do not add glowing colored shadows.
- Do not float every card on hover.
- Do not add uppercase eyebrow labels above headings.
- Do not use more than one primary CTA style in one action group.
- Do not introduce external fonts, icon packs, Tailwind, or animation dependencies.
