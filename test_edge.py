# -*- coding: utf-8 -*-
"""Edge-case API tests: stock-insufficient rollback, duplicate state transitions, pageSize>100 cap."""
import json, urllib.request

BASE = "http://127.0.0.1:9999"
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))

def req(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(BASE + path, data=data, method=method)
    r.add_header("Content-Type", "application/json")
    if token:
        r.add_header("token", token)
    try:
        resp = opener.open(r, timeout=10)
        return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}

results = []
def check(name, cond, detail=""):
    results.append((name, "PASS" if cond else "FAIL", detail))
    print(("PASS" if cond else "FAIL"), "|", name, "|", detail)

# --- login ---
_, r = req("POST", "/login", {"username": "123", "password": "123", "role": "USER"})
user_token = r.get("data") if isinstance(r.get("data"), str) else (r.get("data") or {}).get("token") if isinstance(r.get("data"), dict) else None
if user_token is None: user_token = r.get("data")
check("user login", bool(user_token), str(r)[:120])

_, r = req("POST", "/merchant/login", {"username": "shop_demo", "password": "123", "role": "MERCHANT"})
m_token = r.get("data") if isinstance(r.get("data"), str) else (r.get("data") or {}).get("token") if isinstance(r.get("data"), dict) else None
if m_token is None: m_token = r.get("data")
check("merchant login", bool(m_token), str(r)[:120])

_, r = req("POST", "/admin/login", {"username": "admin", "password": "123", "role": "ADMIN"})
a_token = r.get("data") if isinstance(r.get("data"), str) else (r.get("data") or {}).get("token") if isinstance(r.get("data"), dict) else None
if a_token is None: a_token = r.get("data")
check("admin login", bool(a_token), str(r)[:120])

# --- goods store snapshot ---
_, r = req("GET", "/goods/selectById?id=3")
g3_store = r["data"]["store"]
print("goods3 store =", g3_store)

# --- 1. stock insufficient: settle more than store ---
s, r = req("POST", "/orders/settle", [{"goodsId": 3, "nums": g3_store + 5}], user_token)
ok = (s in (200, 500)) and r.get("code") != "200"
check("settle over-stock rejected", ok, "http=%s code=%s msg=%s" % (s, r.get("code"), str(r.get("msg"))[:60]))
_, r = req("GET", "/goods/selectById?id=3")
check("stock unchanged after rejected settle", r["data"]["store"] == g3_store, "store=%s" % r["data"]["store"])

# --- 2. state machine duplicates ---
s, r = req("POST", "/orders/settle", [{"goodsId": 3, "nums": 1}], user_token)
d0 = r.get("data")
parent = d0.get("parentNo") if isinstance(d0, dict) else d0
_, r = req("GET", "/orders/selectPage?pageNum=1&pageSize=50&state=", token=user_token)
recs = (r.get("data") or {}).get("records") or []
orders = [o["id"] for o in recs if str(o.get("parentNo")) == str(parent)]
check("settle -> order visible (by parentNo)", len(orders) >= 1, "parentNo=%s orders=%s" % (parent, orders))
oid = orders[0]

s, r = req("POST", "/orders/pay", {"id": oid}, user_token)
check("pay once ok", s == 200 and r.get("code") == "200", str(r)[:80])
s, r = req("POST", "/orders/pay", {"id": oid}, user_token)
dup_pay = r.get("code") != "200"
check("duplicate pay rejected", dup_pay, "code=%s msg=%s" % (r.get("code"), str(r.get("msg"))[:50]))

s, r = req("POST", "/merchant/orders/ship?id=%d" % oid, {}, m_token)
check("ship ok", s == 200 and r.get("code") == "200", str(r)[:80])
s, r = req("POST", "/merchant/orders/ship?id=%d" % oid, {}, m_token)
check("duplicate ship rejected", r.get("code") != "200", "code=%s msg=%s" % (r.get("code"), str(r.get("msg"))[:50]))

s, r = req("POST", "/orders/confirm", {"id": oid}, user_token)
check("confirm ok", s == 200 and r.get("code") == "200", str(r)[:80])
s, r = req("POST", "/orders/confirm", {"id": oid}, user_token)
check("duplicate confirm rejected", r.get("code") != "200", "code=%s msg=%s" % (r.get("code"), str(r.get("msg"))[:50]))

s, r = req("DELETE", "/orders/delete?id=%d" % oid, None, user_token)
check("cleanup delete order", s == 200 and r.get("code") == "200", str(r)[:60])

# --- 3. pageSize > 100 cap ---
_, r = req("GET", "/goods/selectPage?pageNum=1&pageSize=500")
d = r.get("data") or {}
gsize = d.get("size"); grec = len(d.get("records") or [])
check("goods pageSize=500 capped <=100", (gsize or 0) <= 100 and grec <= 100, "page.size=%s records=%s total=%s" % (gsize, grec, d.get("total")))

_, r = req("GET", "/orders/selectPage?pageNum=1&pageSize=500&state=", a_token)
d = r.get("data") or {}
osize = d.get("size"); orec = len(d.get("records") or [])
check("orders pageSize=500 capped <=100 (admin)", (osize or 0) <= 100 and orec <= 100, "page.size=%s records=%s" % (osize, orec))

print("\n==== SUMMARY ====")
fails = [x for x in results if x[1] == "FAIL"]
print("total=%d pass=%d fail=%d" % (len(results), len(results) - len(fails), len(fails)))
for n, st, d in fails:
    print("FAILED:", n, d)
