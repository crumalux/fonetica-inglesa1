"""British TTS (Kokoro) for the Bioprocess English lessons.
make_bundle(items) -> (index dict {key: [offset, length]}, bytes). items = list of (text, voice)."""
import hashlib, os, subprocess, numpy as np
MODEL = os.environ.get("KOKORO_DIR", "/tmp/kokoro")
URL = "https://github.com/thewh1teagle/kokoro-onnx/releases/download/model-files-v1.0/"
def ensure_model():
    os.makedirs(MODEL, exist_ok=True)
    for f in ["kokoro-v1.0.int8.onnx", "voices-v1.0.bin"]:
        p = f"{MODEL}/{f}"
        if not os.path.exists(p):
            subprocess.run(["curl", "-sSL", "-o", p, URL + f], check=True)
_k = _tk = None
def tts(text, voice="bf_emma", speed=0.92):
    global _k, _tk
    if _k is None:
        ensure_model()
        from kokoro_onnx import Kokoro
        from kokoro_onnx.tokenizer import Tokenizer
        _k = Kokoro(f"{MODEL}/kokoro-v1.0.int8.onnx", f"{MODEL}/voices-v1.0.bin"); _tk = Tokenizer()
    SR = 24000
    a, _ = _k.create(_tk.phonemize(text.replace("—", ",").replace("–", ","), "en-gb-x-rp"), voice=voice, speed=speed, lang="en-gb", is_phonemes=True)
    z = np.zeros(int(0.06 * SR), dtype=np.float32)
    pcm = (np.clip(np.concatenate([z, a, z]), -1, 1) * 32767).astype("<i2").tobytes()
    return subprocess.run(["ffmpeg", "-loglevel", "error", "-f", "s16le", "-ar", str(SR), "-ac", "1", "-i", "-",
                           "-c:a", "libmp3lame", "-b:a", "48k", "-f", "mp3", "-"], input=pcm, capture_output=True, check=True).stdout
def make_bundle(items, cache="/tmp/kokoro/cache"):
    os.makedirs(cache, exist_ok=True)
    idx, buf = {}, bytearray()
    for text, voice in items:
        key = f"{voice}|{text}"
        if key in idx: continue
        f = f"{cache}/{hashlib.md5(key.encode()).hexdigest()}.mp3"
        if not os.path.exists(f):
            open(f + ".tmp", "wb").write(tts(text, voice)); os.replace(f + ".tmp", f)
        b = open(f, "rb").read(); idx[key] = [len(buf), len(b)]; buf += b
    return idx, bytes(buf)
