"""Build a self-contained Bioprocess English B2 lesson (HTML with embedded British audio).
Usage: python3 build_lesson.py lesson.json output.html [--no-audio]
See lessons/lesson_02.json for the JSON format."""
import base64, html, json, sys

def e(s):  # escape plain text
    return html.escape(str(s), quote=True)

CSS = r""":root{--ink:#152c3a;--muted:#5b7080;--blue:#126a7d;--blue2:#0c5267;--mist:#edf7f8;--line:#d9e5e9;--paper:#fff;--ground:#f3f7f8;--gold:#f6ba62;--green:#23784b;--red:#af4242;--okbg:#edf8f2;--badbg:#fff1ef}
*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:var(--ground);color:var(--ink);font:16px/1.65 system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif}
header{background:linear-gradient(125deg,#113447,#12647a 60%,#248f93);color:#fff;padding:60px 24px 50px}header .inner{max-width:960px;margin:auto}
.eyebrow{letter-spacing:.15em;font-size:.78rem;text-transform:uppercase;font-weight:750;color:#d4f4ee}h1{font-size:clamp(1.9rem,5vw,3.2rem);line-height:1.16;max-width:800px;margin:16px 0}
header p{max-width:720px;color:#e3f6f5;font-size:1.08rem}.chips{display:flex;gap:10px;flex-wrap:wrap;margin-top:22px}.chip{border:1px solid #ffffff5b;background:#ffffff17;padding:5px 12px;border-radius:24px;font-size:.85rem}
nav{background:#fff;border-bottom:1px solid var(--line);position:sticky;top:0;z-index:5}nav .inner{max-width:1000px;margin:auto;display:flex;gap:18px;overflow:auto;padding:12px 20px;scrollbar-width:thin}
nav a{font-size:.84rem;font-weight:700;color:var(--blue2);white-space:nowrap;text-decoration:none}nav a.done::after{content:" ✓";color:var(--green)}
main{max-width:1000px;margin:0 auto;padding:28px 16px 70px}section{background:var(--paper);border:1px solid var(--line);border-radius:16px;padding:clamp(20px,4vw,38px);margin-bottom:22px;box-shadow:0 8px 30px #14394d08}
h2{font-size:1.6rem;line-height:1.25;color:#103d51;margin:0 0 12px}h3{font-size:1.12rem;color:#174e5f;margin:24px 0 8px}.sub{color:var(--muted);font-size:.93rem;margin:0 0 20px}
.reading{font-size:1.06rem;line-height:1.85;max-width:770px}.reading p{margin:0 0 16px;display:flex;gap:10px;align-items:flex-start}.reading p .play{margin-top:4px}
.quotation{border-left:4px solid var(--blue);background:var(--mist);padding:14px 20px;border-radius:0 9px 9px 0;margin:20px 0;font-weight:600}
.table-wrap{overflow:auto}table{border-collapse:collapse;width:100%;min-width:620px}th,td{border-bottom:1px solid var(--line);padding:11px 10px;text-align:left;vertical-align:top}
th{color:#245b69;background:#f0f7f8;font-size:.82rem;text-transform:uppercase;letter-spacing:.04em}td:first-child{font-weight:700}small,.small{color:var(--muted);font-size:.85rem}
.tip{border-radius:10px;background:#eaf5f3;border:1px solid #cce7e0;padding:14px 18px;margin:18px 0}.tip strong{color:#16596c}
.num{background:var(--blue);color:#fff;display:inline-grid;place-items:center;min-width:28px;height:28px;border-radius:50%;font-weight:700;font-size:.83rem;margin-right:8px}
.phrase{padding:13px 0;border-bottom:1px solid var(--line)}.phrase:last-child{border-bottom:0}.phrase strong{color:#0b6173}.phrase em{display:flex;gap:8px;align-items:center;color:var(--muted);margin-top:4px}
.question{border:1px solid var(--line);border-radius:11px;padding:16px 18px;margin:16px 0}.question>p{font-weight:700;margin:0 0 8px}
.choice{display:block;padding:6px 0;font-weight:400;cursor:pointer}.choice input{margin-right:9px;accent-color:var(--blue)}
.feedback{margin-top:10px;font-size:.93rem}.feedback.correct{color:var(--green)}.feedback.incorrect{color:var(--red)}
button{border:0;border-radius:8px;background:var(--blue);padding:11px 19px;color:#fff;font-weight:700;font-size:.92rem;cursor:pointer;font-family:inherit}button:hover{background:var(--blue2)}
button.secondary{background:#e8f1f3;color:var(--blue2)}button.secondary:hover{background:#d6e8ec}.actions{display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin-top:14px}
.play{flex:none;width:32px;height:32px;padding:0;border-radius:50%;background:#e3f1f4;color:var(--blue2);font-size:.8rem;display:inline-grid;place-items:center;vertical-align:middle}.play:hover{background:#cfe7ec}.play.on{background:var(--blue);color:#fff}
textarea{display:block;width:100%;min-height:180px;border:1px solid #b7cbd1;border-radius:9px;padding:14px;font:inherit;color:var(--ink);resize:vertical}textarea:focus{outline:3px solid #b7e2e5;border-color:var(--blue)}
.model{background:#f3f8f9;border:1px solid var(--line);padding:18px;border-radius:10px;margin:12px 0}.model p{margin:0 0 10px}.model p:last-child{margin:0}
details{margin:14px 0;border:1px solid var(--line);border-radius:10px;padding:12px 16px}summary{font-weight:750;cursor:pointer;color:var(--blue2)}details[open] summary{margin-bottom:12px}
.compare{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin:12px 0}.compare>div{padding:14px;border-radius:10px}.compare .before{background:var(--badbg)}.compare .after{background:var(--okbg)}
.kicker{font-size:.7rem;font-weight:800;letter-spacing:.1em;color:var(--muted);text-transform:uppercase}
.line{display:flex;gap:10px;align-items:flex-start;padding:8px 0;border-bottom:1px dashed var(--line)}.line b{min-width:90px;color:var(--blue2)}.line.now{background:#fff8e6}
.check{list-style:none;padding:0}.check li{padding:4px 0}.check li::before{content:"☐ ";color:var(--blue)}
.score{font-weight:700;color:var(--blue2)}.footer{text-align:center;color:var(--muted);padding:0 16px 34px;font-size:.85rem}
@media(max-width:630px){table.vocab{min-width:0}table.vocab thead{display:none}table.vocab tr{display:block;border-bottom:1px solid var(--line);padding:10px 0}table.vocab td{display:block;border:0;padding:3px 0}table.vocab td:nth-child(2){color:var(--ink)}table.vocab td:nth-child(3){color:var(--muted);font-style:italic}.compare{grid-template-columns:1fr}header{padding:42px 18px 34px}section{padding:22px 16px}.line b{min-width:70px}}
@media print{body{background:#fff}nav,.actions,.play{display:none!important}section{break-inside:avoid;box-shadow:none}}"""

JS = r"""const player=new Audio();let lastBtn=null,url=null,seq=0;
function b64(){if(window._buf)return window._buf;const s=atob(AUDIO_B64),u=new Uint8Array(s.length);for(let i=0;i<s.length;i++)u[i]=s.charCodeAt(i);return window._buf=u.buffer}
function play(key,btn){return new Promise(res=>{const e=AUDIO_IDX[key];if(lastBtn)lastBtn.classList.remove('on');player.pause();if(!e){res();return}
 if(url)URL.revokeObjectURL(url);url=URL.createObjectURL(new Blob([b64().slice(e[0],e[0]+e[1])],{type:'audio/mpeg'}));player.src=url;lastBtn=btn;if(btn)btn.classList.add('on');
 player.onended=player.onerror=()=>{if(btn)btn.classList.remove('on');res()};player.play().catch(()=>res())})}
document.addEventListener('click',ev=>{const b=ev.target.closest('.play');if(b){seq++;play(b.dataset.k,b)}});
async function playAll(sel,btn){const my=++seq;const lines=[...document.querySelectorAll(sel)];btn.textContent='■ Stop';btn.onclick=()=>{seq++;player.pause();reset()};
 for(const l of lines){if(my!==seq)break;l.classList.add('now');await play(l.dataset.k,null);l.classList.remove('now');await new Promise(r=>setTimeout(r,350))}reset();
 function reset(){lines.forEach(l=>l.classList.remove('now'));btn.textContent='▶ Play the whole dialogue';btn.onclick=()=>playAll(sel,btn)}}
function checkQuiz(id){const box=document.getElementById(id),qs=[...box.querySelectorAll('.question')];let s=0;
 qs.forEach(q=>{const c=q.querySelector('input:checked'),fb=q.querySelector('.feedback'),a=+q.dataset.answer,ex=q.dataset.e||'';
  if(!c){fb.textContent='Choose an answer.';fb.className='feedback incorrect';return}
  if(+c.value===a){s++;fb.textContent='✓ Correct. '+ex;fb.className='feedback correct'}
  else{fb.textContent='✗ The answer is: '+q.querySelector('input[value="'+a+'"]').parentElement.textContent.trim()+'. '+ex;fb.className='feedback incorrect'}});
 box.querySelector('.score').textContent='Score: '+s+' / '+qs.length;const n=document.querySelector('nav a[href="#'+box.closest('section').id+'"]');if(n&&s===qs.length)n.classList.add('done')}
const KEY='bpe-'+LESSON_ID;function load(id){try{return localStorage.getItem(KEY+id)||''}catch(e){return ''}}function store(id,v){try{localStorage.setItem(KEY+id,v)}catch(e){}}
function count(id){const t=document.getElementById(id).value.trim();document.getElementById(id+'-n').textContent=(t?t.split(/\s+/).length:0)+' words';store(id,document.getElementById(id).value)}
async function copyText(id){const v=document.getElementById(id).value,st=document.getElementById(id+'-s');if(!v.trim()){st.textContent='Write your answer first.';return}
 const msg='Lesson '+LESSON_NUM+' – please correct my writing:\n\n'+v;try{await navigator.clipboard.writeText(msg);st.textContent='Copied! Now paste it in the chat with Claude.'}catch(e){const el=document.getElementById(id);el.focus();el.select();st.textContent='Text selected: copy it and paste it in the chat with Claude.'}}
document.querySelectorAll('textarea').forEach(t=>{t.value=load(t.id);count(t.id);t.addEventListener('input',()=>count(t.id))});"""

def build(L, audio=True):
    items = []
    def k(text, voice="bf_emma"):
        items.append((text, voice)); return f"{voice}|{text}"
    def pb(text, voice="bf_emma"):
        return f'<button class="play" data-k="{e(k(text, voice))}" aria-label="Listen">▶</button>'
    import random
    rnd = random.Random(L["num"] * 7919)
    def shuffled(q):
        idx = list(range(len(q["o"]))); rnd.shuffle(idx)
        return {**q, "o": [q["o"][i] for i in idx], "a": idx.index(q["a"])}
    def quiz(qid, qs, name):
        out = [f'<div id="{qid}">']
        for i, q in enumerate(map(shuffled, qs)):
            opts = "".join(f'<label class="choice"><input type="radio" name="{name}{i}" value="{j}"> {e(o)}</label>' for j, o in enumerate(q["o"]))
            out.append(f'<div class="question" data-answer="{q["a"]}" data-e="{e(q.get("e", ""))}"><p>{i+1}. {e(q["q"])}</p>{opts}<div class="feedback" aria-live="polite"></div></div>')
        out.append(f'<div class="actions"><button onclick="checkQuiz(\'{qid}\')">Check answers</button><span class="score" role="status"></span></div></div>')
        return "".join(out)
    n = L["num"]
    S = []
    S.append(f'''<section id="goals"><h2>Learning goals</h2><p>By the end of this lesson, you will be able to:</p><ul>{"".join(f"<li>{g}</li>" for g in L["goals"])}</ul>
<div class="tip"><strong>How to study:</strong> learn the vocabulary, read and listen, answer the questions, study the grammar point, and then write your text. Tap <b>▶</b> to hear British English. Your writing is saved in this browser; when you finish, copy it and paste it in the chat with Claude to get your corrections.</div></section>''')
    V = L["vocab"]
    rows = "".join(f'<tr><td>{pb(v["term"])} {e(v["term"])}<br><small>/{e(v["ipa"])}/</small></td><td>{e(v["meaning"])}</td><td>{pb(v["example"])} {e(v["example"])}</td></tr>' for v in V)
    S.append(f'''<section id="vocab"><h2>1. Technical vocabulary</h2><p class="sub">Learn each expression as a complete phrase. Pronunciation: British English.</p>
<div class="table-wrap"><table class="vocab"><thead><tr><th>Term</th><th>Meaning</th><th>Example</th></tr></thead><tbody>{rows}</tbody></table></div>
<h3>Vocabulary practice</h3>{quiz("vq", L["vocab_quiz"], "v")}</section>''')
    R = L["reading"]
    paras = "".join(f'<p>{pb(p_plain)}<span>{p_html}</span></p>' for p_html, p_plain in ((p, html.unescape(__import__("re").sub("<[^>]+>", "", p))) for p in R["paras"]))
    quote = f'<div class="quotation">{e(R["quote"])}</div>' if R.get("quote") else ""
    S.append(f'''<section id="reading"><h2>2. Reading: {e(R["title"])}</h2><p class="sub">B2 reading · about {R["words"]} words · tap ▶ to listen to each paragraph</p>
<div class="reading">{paras}{quote}</div><h3>Comprehension</h3>{quiz("rq", R["questions"], "r")}</section>''')
    LI = L["listening"]
    lines = "".join(f'<div class="line" data-k="{e(k(x["text"], x["voice"]))}"><b>{e(x["spk"])}</b><span>{e(x["text"])}</span></div>' for x in LI["lines"])
    S.append(f'''<section id="listening"><h2>3. Listening: {e(LI["title"])}</h2><p class="sub">{e(LI["intro"])}</p>
<div class="tip"><strong>Step 1:</strong> listen without reading and answer the questions. <strong>Step 2:</strong> open the transcript and listen again.</div>
<div class="actions"><button onclick="playAll('#transcript .line',this)">▶ Play the whole dialogue</button></div>
{quiz("lq", LI["questions"], "l")}<details id="transcript"><summary>Transcript</summary>{lines}</details></section>''')
    EX = L["expressions"]
    S.append(f'''<section id="expressions"><h2>4. Useful professional expressions</h2><p class="sub">Use them in meetings, emails, reports and interviews.</p>
{"".join(f'<div class="phrase"><strong><span class="num">{i+1}</span>{e(x["phrase"])}</strong><em>{pb(x["example"])} {e(x["example"])}</em></div>' for i, x in enumerate(EX))}</section>''')
    G = L["grammar"]
    blocks = []
    for b in G["blocks"]:
        cmp = f'<div class="compare"><div class="before"><div class="kicker">Not correct / less natural</div>{e(b["wrong"])}</div><div class="after"><div class="kicker">Correct</div>{e(b["right"])}</div></div>' if b.get("wrong") else ""
        exs = "".join(f'<div class="phrase"><em>{pb(x)} {e(x)}</em></div>' for x in b.get("examples", []))
        blocks.append(f'<h3>{e(b["h"])}</h3><p>{b["p"]}</p>{cmp}{exs}')
    S.append(f'''<section id="grammar"><h2>5. Grammar for work: {e(G["title"])}</h2>{"".join(blocks)}<h3>Grammar practice</h3>{quiz("gq", G["quiz"], "g")}</section>''')
    W = L["writing"]
    S.append(f'''<section id="writing"><h2>6. Writing task</h2><p><strong>Situation:</strong> {e(W["situation"])}</p><p><strong>Your task:</strong> {e(W["task"])}</p>
<p class="small">Checklist before you send it:</p><ul class="check">{"".join(f"<li>{c}</li>" for c in W["checklist"])}</ul>
<textarea id="w1" placeholder="{e(W["placeholder"])}"></textarea><div class="actions"><span class="small" id="w1-n">0 words</span><button onclick="copyText('w1')">Copy for Claude</button><span class="small" id="w1-s" role="status"></span></div>
<div class="tip"><strong>Get your corrections:</strong> tap <b>Copy for Claude</b> and paste the text in the chat with Claude. You will get every mistake marked, the grammar rule behind it, and a corrected B2 version.</div>
<details><summary>Model answer (open it after writing yours)</summary><div class="model">{"".join(f"<p>{e(p)}</p>" for p in W["model"])}</div></details></section>''')
    SP = L["speaking"]
    S.append(f'''<section id="speaking"><h2>7. Speaking practice</h2><p><strong>{e(SP["prompt"])}</strong></p><ul>{"".join(f"<li>{t}</li>" for t in SP["tips"])}</ul>
<details><summary>Listen to a model answer</summary><div class="model">{"".join(f'<p>{pb(p, "bm_george")} {e(p)}</p>' for p in SP["model"])}</div></details>
<div class="tip"><strong>Key lesson:</strong> {e(L["key_lesson"])}</div></section>''')
    nav = "".join(f'<a href="#{i}">{t}</a>' for i, t in [("goals", "Goals"), ("vocab", "Vocabulary"), ("reading", "Reading"), ("listening", "Listening"), ("expressions", "Expressions"), ("grammar", "Grammar"), ("writing", "Writing"), ("speaking", "Speaking")])
    if audio:
        import os; sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from audio import make_bundle
        idx, buf = make_bundle(items)
    else:
        idx, buf = {}, b""
    chips = "".join(f'<span class="chip">{e(c)}</span>' for c in ["CEFR B2", L["area"], "Vocabulary · Reading · Listening · Grammar · Writing", L.get("time", "40–50 minutes")])
    return f'''<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="{e(L["subtitle"])}"><title>Bioprocess English · Lesson {n:02d}</title><style>{CSS}</style></head><body>
<header><div class="inner"><div class="eyebrow">Bioprocess English · Lesson {n:02d} · {e(L["date"])}</div><h1>{e(L["title"])}</h1><p>{e(L["subtitle"])}</p><div class="chips">{chips}</div></div></header>
<nav aria-label="Lesson navigation"><div class="inner">{nav}</div></nav><main>{"".join(S)}</main>
<div class="footer">Bioprocess English · Professional B2 practice for biotechnology · Lesson {n:02d} · British English audio</div>
<script>const LESSON_ID="{n}",LESSON_NUM="{n:02d}";const AUDIO_IDX={json.dumps(idx, ensure_ascii=False, separators=(",", ":"))};const AUDIO_B64="{base64.b64encode(buf).decode()}";
{JS}</script></body></html>'''

if __name__ == "__main__":
    L = json.load(open(sys.argv[1], encoding="utf-8"))
    out = build(L, audio="--no-audio" not in sys.argv)
    open(sys.argv[2], "w", encoding="utf-8").write(out)
    print("written", sys.argv[2], len(out) // 1024, "KB")
