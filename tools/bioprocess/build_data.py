"""Build the data files of one lesson for the Bioprocess English artifact.
Usage: python3 build_data.py lessons/lesson_NN.json OUTDIR [--no-audio]
Writes OUTDIR/lessons/lesson_NN.json, lesson_NN_audio.json, lesson_NN.mp3 and merges OUTDIR/lessons/index.json."""
import json, os, random, re, sys, html
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
src, out = sys.argv[1], sys.argv[2]
L = json.load(open(src, encoding="utf-8")); n = L["num"]; NN = f"{n:02d}"
os.makedirs(f"{out}/lessons", exist_ok=True)
rnd = random.Random(n * 7919)
def shuf(q):
    idx = list(range(len(q["o"]))); rnd.shuffle(idx)
    return {**q, "o": [q["o"][i] for i in idx], "a": idx.index(q["a"])}
items = []
def k(text, voice="bf_emma"):
    items.append((text, voice)); return f"{voice}|{text}"
plain = lambda s: html.unescape(re.sub("<[^>]+>", "", s))
D = dict(L)
D["vocab"] = [{**v, "k1": k(v["term"]), "k2": k(v["example"])} for v in L["vocab"]]
D["vocab_quiz"] = [shuf(q) for q in L["vocab_quiz"]]
R = dict(L["reading"]); R["paras"] = [{"html": p, "k": k(plain(p))} for p in R["paras"]]; R["questions"] = [shuf(q) for q in R["questions"]]; D["reading"] = R
LI = dict(L["listening"]); LI["lines"] = [{**x, "k": k(x["text"], x["voice"])} for x in LI["lines"]]; LI["questions"] = [shuf(q) for q in LI["questions"]]; D["listening"] = LI
D["expressions"] = [{**x, "k": k(x["example"])} for x in L["expressions"]]
G = dict(L["grammar"]); G["blocks"] = [{**b, "examples": [{"t": x, "k": k(x)} for x in b.get("examples", [])]} for b in G["blocks"]]; G["quiz"] = [shuf(q) for q in G["quiz"]]; D["grammar"] = G
SP = dict(L["speaking"]); SP["model"] = [{"t": p, "k": k(p, "bm_george")} for p in SP["model"]]; D["speaking"] = SP
if "--no-audio" not in sys.argv:
    from audio import make_bundle
    idx, buf = make_bundle(items)
    open(f"{out}/lessons/lesson_{NN}.mp3", "wb").write(buf)
    json.dump(idx, open(f"{out}/lessons/lesson_{NN}_audio.json", "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))
    D["audio"] = True
else:
    D["audio"] = False
json.dump(D, open(f"{out}/lessons/lesson_{NN}.json", "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))
mp = f"{out}/lessons/index.json"
M = json.load(open(mp, encoding="utf-8")) if os.path.exists(mp) else []
M = [m for m in M if m["num"] != n] + [{"num": n, "title": L["title"], "area": L["area"], "date": L["date"], "grammar": L["grammar"]["title"], "audio": D["audio"]}]
json.dump(sorted(M, key=lambda m: m["num"]), open(mp, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("lesson", NN, "items", len(items), "manifest", len(M))
