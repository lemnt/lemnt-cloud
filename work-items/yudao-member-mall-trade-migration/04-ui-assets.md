# Catalog admin UI baseline

v1 · 2026-10-25 · code-software-engineer. Input: development design v2; existing PostPage, DeptPage, AdminModalForm, usePermission and router registry. Backend contract: Catalog Req/Res and docs/mall-catalog-api.md (being synchronized with implementation).

UI Design: COMPLETE as an implementation baseline. Interactive layout reference: `prototype/catalog-admin.html` v1. Production component/spacing/color baseline is the existing Ant Design/ProTable/PostPage/AdminModalForm implementation, not new bespoke styling. The HTML is a labeled design reference with sample/empty states, not live service integration.

- Category/brand pages: PageContainer + ProTable, name/status columns, edit and confirmed delete controls, create toolbar; Category form uses nullable root parent and only root parents as options; parent is immutable while editing. Image URLs are optional text fields with no uploads or generated imagery.
- SPU page: paged ProTable with code/name/category/brand/status; create modal with code/name/category/brand/description/image plus ProFormList of SKUs, decimal yuan price and integer stock. Variant properties are entered as JSON mapping in the form and parsed/validated locally before submission. Detail drawer loads real SPU DTO, displays SKU code/specifications/available inventory and separate metadata/stock forms.
- Stock form: signed delta, reason, stable per-form requestNo. On an uncertain response the same form retains the same requestNo; changing payload requires a new explicit operation. Submit disabled while pending. No absolute-stock overwrite control.
- Permissions: read gates before queries; action gates use shared usePermission. New routes register existing menu component keys; no hardcoded role checks. A reviewed SQL seed for the system menu is required before normal menu access.
- States: initial loading; explicit empty table; request failure with retry/reload; rejected write stays open with user input; successful write closes and invalidates catalog query keys. If refresh fails after write, report success with stale-list warning rather than encourage resubmission. Delete confirmation and shelf confirmation use Popconfirm.
- Keyboard/accessibility: existing labeled Ant Design form fields, buttons for actions, dialog keyboard/focus behavior. No new interactions requiring custom keyboard handling.

Assets: COMPLETE/N/A for new binary assets. Reuse installed Ant Design controls and the existing AdminModalForm EditOutlined icon through its existing import. No new fonts, photos, illustrations, SVGs or third-party assets are introduced; no placeholders count as product data.

Implementation: pending. Visual QA: NOT RUN until the UI is built and compared to this baseline. Backend end-to-end service and permissions verification is separately recorded, not inferred from browser mocks.
