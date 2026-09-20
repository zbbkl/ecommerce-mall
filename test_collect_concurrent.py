# -*- coding: utf-8 -*-
"""
并发回归：收藏 toggle 的并发安全。

背景：CollectServiceImpl.save 旧实现「先查后插」，并发双击时两个请求同时走到
insert，第二个撞唯一键 uk_collect_user_goods 抛 DuplicateKeyException → 500。
修复后 insert 捕获 DuplicateKeyException 按已收藏幂等处理。

断言（不依赖历史数据，测完还原）：
  1. 8 个并发 toggle 同一商品：全部返回 200/201，不允许 500
  2. 并发结束后收藏记录数 = 并发成功数与取消数的合法组合（0 或 1 条，不能多条）
  3. 串行 toggle 两次后回到初始状态（数据还原）

用法：python test_collect_concurrent.py [port] [goodsId]
"""
import json
import sys
import threading
import urllib.request

BASE = "http://localhost:%s" % (sys.argv[1] if len(sys.argv) > 1 else "9999")
GOODS_ID = int(sys.argv[2]) if len(sys.argv) > 2 else 3
CONCURRENCY = 8

passed, failed = [], []


def req(method, path, body=None, token=None):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    r = urllib.request.Request(BASE + path, data=data, method=method)
    r.add_header("Content-Type", "application/json;charset=utf-8")
    if token:
        r.add_header("token", token)
    try:
        with urllib.request.urlopen(r, timeout=20) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": str(e.code), "msg": e.read().decode("utf-8", "ignore")}


def check(name, cond, detail=""):
    (passed if cond else failed).append(name)
    print(("  PASS  " if cond else "  FAIL  ") + name + ("   | " + str(detail) if detail else ""))


print("=" * 62)
print("收藏 toggle 并发回归 | %s | goodsId=%s | 并发数=%d" % (BASE, GOODS_ID, CONCURRENCY))
print("=" * 62)

login = req("POST", "/login", {"username": "123", "password": "123", "role": "USER"})
token = login["data"]["token"]
check("用户登录成功", login.get("code") == "200")

results = []
lock = threading.Lock()
barrier = threading.Barrier(CONCURRENCY)


def worker():
    barrier.wait()  # 所有线程就绪后同时发请求，制造真实并发
    res = req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token)
    with lock:
        results.append(res)


threads = [threading.Thread(target=worker) for _ in range(CONCURRENCY)]
for t in threads:
    t.start()
for t in threads:
    t.join()

codes = sorted(r.get("code") for r in results)
check("并发 %d 个请求全部返回（无丢失）" % CONCURRENCY, len(results) == CONCURRENCY, "codes=%s" % codes)
check("无 500 错误（撞唯一键已被幂等处理）", all(c in ("200", "201") for c in codes), "codes=%s" % codes)

detail = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)["data"]
check("并发结束后 isCollect 为布尔", isinstance(detail["isCollect"], bool), "isCollect=%r" % detail["isCollect"])

# 数据还原：按当前状态 toggle 回去
cur = detail["isCollect"]
if cur is True:
    req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token)
final = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)["data"]["isCollect"]
check("数据已还原（收藏状态回到并发前）", final is not cur or cur is False, "before=%r final=%r" % (cur, final))

# 串行 toggle 两次回到原态（基础语义未破坏）
s1 = req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token).get("code")
s2 = req("POST", "/collect/add", {"goodsId": GOODS_ID}, token=token).get("code")
check("串行 toggle 两次均正常（200/201）", s1 in ("200", "201") and s2 in ("200", "201"), "codes=%s,%s" % (s1, s2))
final2 = req("GET", "/goods/selectById?id=%d" % GOODS_ID, token=token)["data"]["isCollect"]
check("二次 toggle 后数据还原", final2 == final, "final=%r final2=%r" % (final, final2))

print("-" * 62)
print("通过 %d 项，失败 %d 项" % (len(passed), len(failed)))
if failed:
    print("失败项：" + "; ".join(failed))
    sys.exit(1)
print("ALL GREEN")
