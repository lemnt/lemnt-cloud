# Test report

v1 · 2026-10-25 · humanflow-code self-review. Input: design v1 and working candidate recorded by this report's commit. Environment: OpenJDK 17.0.20, Maven 3.9.11, local H2 MySQL mode, mocked remote gateways.

| Check | Result | Evidence / limit |
| --- | --- | --- |
| Root reactor verify, all 71 projects | PASS | evidence/maven-reactor.txt |
| All available Maven suites | PASS | 227 tests, 0 failures/errors/skips; evidence/maven-tests.json |
| Dispatcher success/failure/claim arbitration/partial retry/stale acknowledgement | PASS | TradeOutboxDispatchExeTest, 6 cases |
| Payment duplicate/mismatch/status change/event sequencing | PASS | PaymentNotifyCmdExeTest, 4 cases; mock boundaries, not transaction integration |
| Order close rejects unknown/provider failures and sequences close | PASS | TradeOrderCloseCmdExeTest, 4 cases; mock boundaries |
| Retry exhaustion, expired ownership, due queue eligibility, DEAD replay | PASS | TradeOutboxMapperTest, 4 cases; actual production mapper SQL on H2 |
| Missing/ambiguous provider rejects close | PASS | PaymentChannelCloseGatewayImplTest, 2 cases |
| Replay validation and conditional outcome | PASS | TradeOutboxReplayCmdExeTest, 2 cases |
| Whitespace/conflict check | PASS | git diff --check |
| Real MySQL V3 migration and concurrent transactions | NOT RUN | No test MySQL supplied; H2 mapper tests are not MySQL verification |
| Cross-service Dubbo stock RPC failure injection | NOT RUN | No running service stack |
| HTTP authentication/permission and replay endpoint routing | NOT RUN | Source has RequiresPermissions; no deployed gateway verification |
| Real signed payment callback, provider query/close | BLOCKED | Concrete adapter/configuration absent; no public callback exposed |
| React visual QA and end-to-end checkout | NOT RUN | Requested frontend migration still pending |

Self-review is not an independent reviewer or user acceptance. Recommendation: retain Draft, do not deploy as production-ready. No credentials or real payments used. No real channel unpaid guarantee is claimed by mock tests.
