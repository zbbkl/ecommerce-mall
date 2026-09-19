# -*- coding: utf-8 -*-
"""
接口级回归：商品详情 isCollect 字段与收藏 toggle 的自洽性。

背景：/goods/selectById 标注 @AuthAccess（匿名可浏览），旧版 JwtInterceptor 对
@AuthAccess 端点直接放行且不写登录态，导致 isCollect 恒为 null，前端收藏后
loadGoods() 回填 null 把按钮状态覆盖回「未收藏」。

本脚本断言（不依赖历史数据，纯自洽）：
  1. 匿名调用 selectById/isCollect 字段应为 null（未登录无个性化）
  2. 登录后调用：isCollect 必须是布尔 false/true（不能是 null）
  3. 连续两次 toggle 后 isCollect 必须翻转回去（数据还原，无副作用）

用法：
  python test_collect.py            # 默认 http://localhost:9999
  python test_collect.py 9999       # 指定端口
"""
import json
import sys
import urllib.request

BASE = "http://localhost:%s" % (sys.argv[1] if len(sys.argv) > 1 else "9999")
GOODS_ID = int(sys.argv[2]) if len(sys.argv) > 2 else 3
USER = {"username": "123", "password": "123", "role": "USER"}

passed = []
failed = []


def req(method, path, body=None, token=None):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    r = urllib.request.Request(BASE + path, data=data, method=method)
    r.add_header("Content-Type", "application/json;charset=utf-8")
    if token:
        r.add_header("token", token)
    try:
        with urllib.request.urlopen(r, timeout=15) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": str(e.code), "msg": e.read().decode("utf-8", "ignore")}


def check(name, cond, detail=""):
    (passed if cond else failed).append(name)
    print(("  PASS  " if cond else "  FAIL  ") + name + ("   | " + str(detail) if detail else ""))


print("=" * 62)
print("isCollect 回归 | %s | goodsId=%s" % (BASE, GOODS_ID))
print("=" * 62)

# --- 1. 匿名：不允许出现 true（未登录不应有个性化收藏态）
r = req("GET", "/goods/selectById?id=%d" % GOODS_ID)
anon_val = r.get("data", {}).get("isCollect") if r.get("data") else "NO_DATA"
check("匿名访问可正常拿到商品详情", r.get("code") == "200", "code=%s" % r.get("code"))
check("匿名时 isCollect 不是 true", anon_val is not True, "isCollect=%r" % anon_val)

# --- 2. 登录
login = req("POST", "/login", USER)
check("用户登录成功", login.get("code") == "200", login.get("msg"))
token = (login.get("data") or {}).get("token")
check("拿到 token", bool(token))

# --- 3. 登录后 isCollect 必须是布尔值（本次修复的核心断言）
r = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)
before = (r.get("data") or {}).get("isCollect")
check("登录后 isCollect 是布尔（非 null）", isinstance(before, bool), "isCollect=%r" % before)

# --- 4. toggle 一次 → 必须翻转
# 注：/collect/add 是 toggle 语义，收藏成功返回 200，取消收藏返回 201，均属正常
r1 = req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token)
check("收藏 toggle 接口调用成功", r1.get("code") in ("200", "201"),
      "code=%s msg=%s" % (r1.get("code"), r1.get("msg")))
r = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)
mid = (r.get("data") or {}).get("isCollect")
check("toggle 后 isCollect 翻转", mid is not before, "before=%r after=%r" % (before, mid))

# --- 5. 再 toggle 一次 → 数据还原（无副作用）
r2 = req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token)
check("二次 toggle 调用成功", r2.get("code") in ("200", "201"),
      "code=%s msg=%s" % (r2.get("code"), r2.get("msg")))
r = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)
after = (r.get("data") or {}).get("isCollect")
check("二次 toggle 后回到初始值（数据已还原）", after is before, "before=%r final=%r" % (before, after))

# --- 6. 未登录仍可匿名浏览（回归保护：修复不能破坏匿名放行）
r = req("GET", "/goods/selectById?id=%d" % GOODS_ID)
check("未登录仍能匿名浏览详情（@AuthAccess 未被破坏）", r.get("code") == "200", "code=%s" % r.get("code"))

print("-" * 62)
print("通过 %d 项，失败 %d 项" % (len(passed), len(failed)))
if failed:
    print("失败项：" + "; ".join(failed))
    sys.exit(1)
print("ALL GREEN")
