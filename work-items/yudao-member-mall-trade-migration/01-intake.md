# Intake

v1 · 2026-10-25 · humanflow-code. Input: user request, PR #1, head a476b90, server AGENTS.md.

Retain Java 17, Boot 4.1, Dubbo/Nacos and existing six-layer modules. No main edits, merge or deployment. Commit/push to the specified branch and update existing Draft PR. Existing outbox worker has unfenced acknowledgements, unbounded retries and eligibility starvation. Expiry worker closes orders without channel verification. Payment callback adapter and full catalog/React flows are absent.

Java 17 is installed; Maven was absent and a temporary Maven 3.9.11 was downloaded. Initial Maven resolution failed on the configured Aliyun repository; retrying through a temporary Central mirror/proxy without changing dependency versions. No runtime MySQL/Nacos/payment sandbox has been supplied. Continue independent work; record actual test outcomes separately.
