# Migration admin UI baseline

v2 · 2026-10-25 · code-software-engineer. Input: development design v2; existing PostPage, DeptPage, AdminModalForm, usePermission and router registry. Backend contract: Catalog Req/Res and docs/mall-catalog-api.md (being synchronized with implementation).

UI Design: COMPLETE as an implementation baseline. Interactive layout reference: `prototype/catalog-admin.html` v1. Production component/spacing/color baseline is the existing Ant Design/ProTable/PostPage/AdminModalForm implementation, not new bespoke styling. The HTML is a labeled design reference with sample/empty states, not live service integration.

- Category/brand pages: PageContainer + ProTable, name/status columns, edit and confirmed delete controls, create toolbar; Category form uses nullable root parent and only root parents as options; parent is immutable while editing. Image URLs are optional text fields with no uploads or generated imagery.
- SPU page: paged ProTable with code/name/category/brand/status; create modal with code/name/category/brand/description/image plus ProFormList of SKUs, decimal yuan price and integer stock. Variant properties are entered as JSON mapping in the form and parsed/validated locally before submission. Detail drawer loads real SPU DTO, displays SKU code/specifications/available inventory and separate metadata/stock forms.
- Stock form: signed delta, reason, stable per-form requestNo. On an uncertain response the same form retains the same requestNo; changing payload requires a new explicit operation. Submit disabled while pending. No absolute-stock overwrite control.
- Permissions: read gates before queries; action gates use shared usePermission. New routes register existing menu component keys; no hardcoded role checks. A reviewed SQL seed for the system menu is required before normal menu access.
- States: initial loading; explicit empty table; request failure with retry/reload; rejected write stays open with user input; successful write closes and invalidates catalog query keys. If refresh fails after write, report success with stale-list warning rather than encourage resubmission. Delete confirmation and shelf confirmation use Popconfirm.
- Keyboard/accessibility: existing labeled Ant Design form fields, buttons for actions, dialog keyboard/focus behavior. No new interactions requiring custom keyboard handling.

Assets: COMPLETE/N/A for new binary assets. Reuse installed Ant Design controls and the existing AdminModalForm EditOutlined icon through its existing import. No new fonts, photos, illustrations, SVGs or third-party assets are introduced; no placeholders count as product data.

Catalog implementation/verification references: 05-development.md and 06-test.md. Actual browser Visual QA remains unverified; UI design and asset baseline completion is not an interaction PASS.


## Checkout / coupon UI baseline v2

Input: design v3, existing catalog/system pages and actual checkout Req/Res. Interactive reference: prototype/checkout-admin.html. No new binary assets, icons, fonts or dependencies; use existing PageContainer/ProTable/AdminModalForm/Ant Design controls and TanStack Query. The existing catalog baseline remains valid.

- Coupon template page: real template list with code/name/type/threshold/discount/validity/quota/enabled columns; create modal with fixed or percent parameters; enable/disable confirmation; issue modal specifying a target member ID and template. No auto-selected consumer identity or role assumptions.
- Member cart/checkout page: explicitly labeled admin operation; positive member-ID search, real cart rows with fresh sellability/stock/price, quantity and selected-state edit modal, confirmed delete. Coupons show owned AVAILABLE instances and template labels where template:list is granted. Selection changes invalidate the preview.
- Quote pane displays server original/discount/payable strings. Submit always uses server-side repricing; this is not a price guarantee. Zero-payable preview disables submission and explains that zero-pay orders are not implemented.
- Before submitting, persist the immutable requestNo/member/SKU/coupon payload in operator-scoped browser storage. Unknown outcomes retain this payload and allow exact retry; edits/member changes are disabled while an intent is unresolved. An explicit confirmed local reset explains it does not cancel a possibly created server order. Storage failure prevents an unsafe untracked submission.
- Receipt pane shows orderNo and actual RESERVING/PENDING_PAYMENT/PAID/CLOSED state with explicit refresh, no fake payment button. During RESERVING, another request is disallowed for the same cart rows. Successful refresh invalidates cart/coupon caches; failures preserve data and report errors.
- Read/action gates use usePermission. Need new system menu entries and pass-through /trade/** gateway route, supplied as reviewed setup inputs without assigning roles.
- Empty/loading/failure states are explicit; save failures preserve form data; write success is separated from refresh failure. Existing labeled forms/modal keyboard/focus rules apply; mobile tables use horizontal scrolling.

UI Design: COMPLETE baseline for implementation. Assets: COMPLETE/N/A (existing library assets). Checkout implementation and verification are recorded in 05-development.md and 06-test.md. Visual QA remains BLOCKED by the previously observed runtime Unix socket restriction; no screenshots or browser interaction PASS are claimed.

## Order fulfillment UI baseline v3

Input: design v4 and actual fulfillment Req/Res. Reference: prototype/fulfillment-admin.html. Reuse PageContainer, ProTable, AdminModalForm, Ant Design Drawer/Descriptions and TanStack Query; no dependencies or binary assets.

- Order page: exact order number and status filters; paged non-PII rows with amount, state, stock-confirmed flag, pending refund flag and tracking number. Detail drawer requires query permission and loads recipient information only on demand. Explicit error/retry/empty/loading states.
- Ship modal: carrier, tracking, recipient/name/phone/address; stable requestNo for the open operation, same request key retained on failure. Do not persist recipient information in local/session storage. Only PAID + confirmed + no active refund rows offer shipping. Server repeats all guards under order lock.
- Receive: SHIPPED rows, Popconfirm, pending indicator; server preserves original received time on retry. List refresh after writes; refresh error must not turn a committed write into apparent failure.
- Refund request: PAID row, reason only, no client amount; explicit warning that recording a request does not execute a payment-channel refund. Queue has REQUESTED/REJECTED filters, full amount and reason, rejection modal with reason. No approve/refund-success control. Active request blocks shipping. Stable requestNo for request modal.
- All reads/actions gated by shared permission hook; menu seed assigns no roles. Unknown server states and malformed amounts rejected by decoders. Existing checkout view accepts SHIPPED/COMPLETED.
- Modal close discards local operation; reopening creates a new key. Server one-shipment and one-active-refund constraints prevent duplicates across reloads; unknown outcomes instruct refresh before opening another operation. No claim of cross-session exact-retry support for PII forms.

UI Design COMPLETE baseline; assets COMPLETE/N/A. Actual browser visual/interaction QA remains blocked by the runtime restriction already recorded in catalog evidence.
