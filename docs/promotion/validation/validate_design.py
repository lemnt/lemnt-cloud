#!/usr/bin/env python3
"""Validate design attachments only. This is NOT the application promotion engine."""
from __future__ import annotations

import copy
import hashlib
import json
import random
import re
from decimal import Decimal, ROUND_DOWN
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker

ROOT = Path(__file__).resolve().parents[1]
REPO = ROOT.parents[1]
MAX_LONG = (1 << 63) - 1
CHECKER = FormatChecker()


@CHECKER.checks("int64-positive")
def positive_long(value):
    return isinstance(value, str) and value.isascii() and value.isdigit() and 0 < int(value) <= MAX_LONG


@CHECKER.checks("int64-nonnegative")
def nonnegative_long(value):
    return isinstance(value, str) and value.isascii() and value.isdigit() and 0 <= int(value) <= MAX_LONG


class DesignError(Exception):
    pass


def money(value, positive=False):
    if not isinstance(value, str) or not value.isascii() or not value.isdigit():
        raise DesignError("AMOUNT_OUT_OF_RANGE")
    number = int(value)
    if str(number) != value or number > MAX_LONG or number < int(positive):
        raise DesignError("AMOUNT_OUT_OF_RANGE")
    return number


def draft_rules(draft):
    if int(draft["endsAtEpochMs"]) <= int(draft["startsAtEpochMs"]):
        raise DesignError("INVALID_PROMOTION_REQUEST")
    total, member = draft["globalUseLimit"], draft["memberUseLimit"]
    if total is not None and member is not None and int(member) > int(total):
        raise DesignError("INVALID_PROMOTION_REQUEST")
    if draft["type"] == "FULL_REDUCTION":
        tiers = draft["rule"]["tiers"]
        thresholds = [int(t["thresholdFen"]) for t in tiers]
        discounts = [int(t["discountFen"]) for t in tiers]
        if thresholds != sorted(set(thresholds)) or discounts != sorted(discounts):
            raise DesignError("INVALID_PROMOTION_REQUEST")


def allocate(discount, weights):
    """Independent arithmetic checker for largest-remainder examples."""
    total = sum(weights.values())
    assert 0 <= discount <= total
    if total == 0:
        return {key: 0 for key in weights}
    allocated = {key: discount * weight // total for key, weight in weights.items()}
    ranked = sorted(
        (key for key in weights if weights[key] > 0),
        key=lambda key: (-(discount * weights[key] % total), int(key)),
    )
    for key in ranked[: discount - sum(allocated.values())]:
        allocated[key] += 1
    assert sum(allocated.values()) == discount
    assert all(0 <= allocated[key] <= weights[key] for key in weights)
    return allocated


def reduction(rule, kind, base):
    if kind == "FULL_REDUCTION":
        tiers = [tier for tier in rule["tiers"] if base >= money(tier["thresholdFen"])]
        return min(base, money(tiers[-1]["discountFen"], True)) if tiers else 0
    if kind in ("PERCENT", "ORDER_DISCOUNT"):
        rate = rule["payRateBps"]
        if not isinstance(rate, int) or isinstance(rate, bool) or not 1 <= rate <= 9999:
            raise DesignError("INVALID_PROMOTION_REQUEST")
        discount = base * (10000 - rate) // 10000
        cap = rule.get("maxDiscountFen")
        return min(base, discount, money(cap, True)) if cap is not None else min(base, discount)
    if kind == "FIXED":
        return min(base, money(rule["fixedAmountFen"], True))
    raise DesignError("INVALID_PROMOTION_REQUEST")


def calculate(data):
    """Design-vector evaluation, no DB/auth/cache/Java integration."""
    lines = sorted(data["lines"], key=lambda line: int(line["productId"]))
    if not 1 <= len(lines) <= 100 or len({line["productId"] for line in lines}) != len(lines):
        raise DesignError("INVALID_PROMOTION_REQUEST")
    gross = {}
    for line in lines:
        quantity = line["quantity"]
        if not isinstance(quantity, int) or isinstance(quantity, bool) or not 1 <= quantity <= 999:
            raise DesignError("INVALID_PROMOTION_REQUEST")
        value = money(line["unitPriceFen"], True) * quantity
        if value > MAX_LONG:
            raise DesignError("AMOUNT_OUT_OF_RANGE")
        gross[line["productId"]] = value
    original = sum(gross.values())
    if original > MAX_LONG:
        raise DesignError("AMOUNT_OUT_OF_RANGE")
    coupon = data.get("coupon")
    if coupon and (original < money(coupon["thresholdFen"]) or reduction(coupon, coupon["kind"], original) == 0):
        raise DesignError("COUPON_NOT_ELIGIBLE")
    if data["promotionMode"] not in ("BEST", "NONE"):
        raise DesignError("INVALID_PROMOTION_REQUEST")
    campaigns = data.get("campaigns", []) if data["promotionMode"] == "BEST" else []
    if len(campaigns) > 100:
        raise DesignError("PROMOTION_CANDIDATE_LIMIT")
    candidates = []
    for campaign in [None, *campaigns]:
        if campaign and coupon and campaign["couponPolicy"] == "EXCLUSIVE":
            continue
        weights = {}
        if campaign:
            scope = campaign["scope"]
            if scope["kind"] not in ("ALL", "SPU_IDS", "SKU_IDS"):
                raise DesignError("INVALID_PROMOTION_REQUEST")
            for line in lines:
                key = line["productId"]
                if scope["kind"] == "ALL" or (
                    key if scope["kind"] == "SKU_IDS" else line["spuId"]
                ) in scope["ids"]:
                    weights[key] = gross[key]
        base = sum(weights.values())
        activity_discount = 0
        if campaign and base >= money(campaign["rule"].get("thresholdFen", "0")):
            activity_discount = reduction(campaign["rule"], campaign["type"], base)
        if campaign and activity_discount == 0:
            continue
        activity_allocation = {key: 0 for key in gross}
        activity_allocation.update(allocate(activity_discount, weights))
        remaining = {key: value - activity_allocation[key] for key, value in gross.items()}
        after_activity = sum(remaining.values())
        if coupon and after_activity < money(coupon["thresholdFen"]):
            continue
        coupon_discount = reduction(coupon, coupon["kind"], after_activity) if coupon else 0
        if coupon and coupon_discount == 0:
            continue
        coupon_allocation = allocate(coupon_discount, remaining)
        payable = after_activity - coupon_discount
        result = {
            "originalFen": str(original),
            "campaignDiscountFen": str(activity_discount),
            "couponDiscountFen": str(coupon_discount),
            "payableFen": str(payable),
            "selectedCampaignId": campaign["id"] if campaign else None,
            "submittable": payable > 0,
            "lines": [
                {
                    "productId": key,
                    "campaignDiscountFen": str(activity_allocation[key]),
                    "couponDiscountFen": str(coupon_allocation[key]),
                    "payableFen": str(remaining[key] - coupon_allocation[key]),
                }
                for key in gross
            ],
        }
        rank = (
            payable,
            1 if campaign else 0,
            -campaign["priority"] if campaign else 0,
            int(campaign["id"]) if campaign else 0,
            campaign["version"] if campaign else 0,
        )
        candidates.append((rank, result))
    assert candidates
    return min(candidates, key=lambda pair: pair[0])[1]


def validate_quote_totals(quote):
    lines = quote["lines"]
    assert sum(int(line["grossFen"]) for line in lines) == int(quote["originalFen"])
    for field in ("campaignDiscountFen", "couponDiscountFen", "payableFen"):
        assert sum(int(line[field]) for line in lines) == int(quote[field])
    assert int(quote["totalDiscountFen"]) == int(quote["campaignDiscountFen"]) + int(quote["couponDiscountFen"])
    assert int(quote["originalFen"]) == int(quote["totalDiscountFen"]) + int(quote["payableFen"])
    for line in lines:
        quantity = line["quantity"]
        net = int(line["payableFen"])
        assert int(line["unitPriceFen"]) * quantity == int(line["grossFen"])
        assert int(line["grossFen"]) == int(line["campaignDiscountFen"]) + int(line["couponDiscountFen"]) + net
        assert int(line["unitBasePayableFen"]) * quantity + line["extraFenUnitCount"] == net
        assert 0 <= line["extraFenUnitCount"] < quantity


def main():
    markdown_examples = 0
    for document in (ROOT / "design.md", ROOT / "api.md"):
        for match in re.finditer(r"```json\s*\n(.*?)\n```", document.read_text(), re.S):
            json.loads(match.group(1))
            markdown_examples += 1
    schemas = {}
    for path in sorted((ROOT / "contracts").glob("*.schema.json")):
        value = json.loads(path.read_text())
        Draft202012Validator.check_schema(value)
        schemas[path.name.removesuffix(".schema.json")] = Draft202012Validator(value, format_checker=CHECKER)
    manifest = json.loads((ROOT / "validation/example-manifest.json").read_text())
    for example, schema in manifest.items():
        value = json.loads((ROOT / "examples" / (example + ".json")).read_text())
        schemas[schema].validate(value)
        if example in ("activity-create", "activity-update"):
            draft_rules(value["draft"])
        if example == "quote-response":
            validate_quote_totals(value)
    negative = []
    q = json.loads((ROOT / "examples/quote-request.json").read_text())
    for field, value in [("memberId", "99"), ("payableFen", "1"), ("campaignId", "9")]:
        bad = {**q, field: value}
        negative.append(("quote-request", bad))
    negative.extend([
        ("quote-request", {**q, "productIds": ["101", "101"]}),
        ("quote-request", {**q, "productIds": ["9223372036854775808"]}),
        ("quote-request", {**q, "productIds": ["0101"]}),
        ("quote-request", {**q, "promotionMode": "AUTO_DROP_COUPON"}),
        ("quote-request", {**q, "couponId": 5}),
        ("submit-request", {"settlementId": "bad", "requestNo": "K"}),
        ("submit-request", {"settlementId": "a8b6e08e-54e8-4da0-81cf-1070171f34af", "requestNo": "K", "priceFen": "1"}),
    ])
    draft = json.loads((ROOT / "examples/activity-create.json").read_text())["draft"]
    for field, value in [("audience", "MEMBER_LEVEL"), ("type", "GROUP_BUY")]:
        negative.append(("activity-draft", {**draft, field: value}))
    zero = json.loads((ROOT / "examples/quote-response.json").read_text())
    zero["payableFen"] = "0"
    negative.append(("quote-response", zero))
    for schema, value in negative:
        assert list(schemas[schema].iter_errors(value)), (schema, value)
    bad_drafts = []
    value = copy.deepcopy(draft); value["endsAtEpochMs"] = value["startsAtEpochMs"]; bad_drafts.append(value)
    value = copy.deepcopy(draft); value["memberUseLimit"] = "10001"; bad_drafts.append(value)
    value = copy.deepcopy(draft); value["rule"]["tiers"] *= 2; bad_drafts.append(value)
    value = copy.deepcopy(draft); value["rule"]["tiers"] = [{"thresholdFen":"100","discountFen":"90"},{"thresholdFen":"200","discountFen":"80"}]; bad_drafts.append(value)
    for value in bad_drafts:
        try:
            draft_rules(value)
        except DesignError:
            pass
        else:
            raise AssertionError("invalid draft accepted")
    vectors = json.loads((ROOT / "pricing-vectors.json").read_text())["vectors"]
    for vector in vectors:
        try:
            actual = calculate(vector["input"])
        except DesignError as error:
            actual = {"errorCode": str(error)}
        assert actual == vector["expected"], (vector["id"], actual, vector["expected"])
    rng = random.Random(314159)
    for _ in range(2000):
        weights = {str(index + 1): rng.randrange(0, 10**10) for index in range(rng.randrange(1, 21))}
        discount = rng.randrange(sum(weights.values()) + 1)
        actual = allocate(discount, weights)
        reverse = allocate(discount, dict(reversed(list(weights.items()))))
        assert actual == reverse
        for net in actual.values():
            quantity = rng.randrange(1, 1000)
            base, extra = divmod(net, quantity)
            split = rng.randrange(quantity + 1)
            prefix = base * split + min(split, extra)
            suffix = base * (quantity - split) + max(0, extra - split)
            assert prefix + suffix == net
    assert allocate(1, {"10":1,"2":1}) == {"10":0,"2":1}
    assert allocate(0, {"1":0}) == {"1":0}
    for _ in range(1500):
        gross = rng.randrange(1, 10**9)
        percent = rng.randrange(1,100)
        legacy_yuan = (Decimal(gross) / 100 * Decimal(100-percent) / 100).quantize(Decimal("0.01"), rounding=ROUND_DOWN)
        adapted = reduction({"payRateBps":percent*100,"maxDiscountFen":None}, "PERCENT", gross)
        assert adapted == int(legacy_yuan * 100)
    sources = json.loads((ROOT / "validation/source-baseline.json").read_text())
    for source in sources["files"]:
        path = REPO / source["path"]
        assert path.is_file(), path
        assert hashlib.sha256(path.read_bytes()).hexdigest() == source["sha256"], source["path"]
    print(json.dumps({
        "status":"PASS_DESIGN_ATTACHMENTS_ONLY",
        "schemas":len(schemas),"validExamples":len(manifest),"markdownJsonExamples":markdown_examples,
        "rejectedSchemaExamples":len(negative),"rejectedCrossFieldDrafts":len(bad_drafts),
        "goldenPricingVectors":len(vectors),"allocationPropertyCases":2000,
        "legacyPercentageParityCases":1500,"sourceFilesChecked":len(sources["files"]),
        "runtimeJavaEngine":"NOT IMPLEMENTED","newSqlMigration":"NOT GENERATED",
        "mavenReactNativeRerun":"NOT RUN: no runtime source change",
        "liveIntegration":"NOT RUN"
    },ensure_ascii=False,indent=2))


if __name__ == "__main__":
    main()
