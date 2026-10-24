"""Convert pinned egg rolls text fields only; no audio, examples, fonts or templates.
Run: python scripts/import_jlpt.py --source build/vocabulary-source/notes.csv
Fetch the URL in SOURCE with curl first. Outputs are deterministic and offline.
"""
import argparse, csv, hashlib, html, io, json, re, unicodedata
from pathlib import Path
COMMIT = 'b8a4712fa6e63054eed66564cf9cef9b60d13a2f'
SOURCE = f'https://raw.githubusercontent.com/5mdld/anki-jlpt-decks/{COMMIT}/deck-source/notes.csv'
ROOT = Path(__file__).resolve().parents[1]

def convert(source):
    raw = source.read_bytes()
    assert hashlib.sha256(raw).hexdigest() == '30e9350c92c7bdeaf0f4c0317c9a0f659eb32e606a69870176deff159559eb1a', 'Source differs from pinned snapshot'
    rows = csv.reader(io.StringIO('\n'.join(x for x in raw.decode('utf-8-sig').splitlines() if not x.startswith('#'))), delimiter='\t')
    groups = {level: [] for level in ['N5','N4','N3','N2','N1']}
    seen = {level: set() for level in groups}
    excluded = []
    def clean(s):
        return unicodedata.normalize('NFC', html.unescape(re.sub('<[^>]+>', '', s))).strip()
    for row in rows:
        assert len(row) == 39
        level = re.search(r'N[1-5]', row[1])[0]
        word, reading, meaning = [clean(row[i]) for i in [3,6,7]]
        assert word and reading and meaning and len(word)<=120 and len(reading)<=120 and len(meaning)<=1000
        assert not any(ord(c)<32 for c in word+reading)
        key = word, reading
        if key in seen[level]:
            excluded.append({'id':row[2], 'level':level, 'reason':'duplicate text + reading'})
            continue
        seen[level].add(key)
        groups[level].append((float(row[35] or 'inf'), ['egg-'+row[2],word,reading,meaning,level]))
    out = ROOT/'app/src/main/assets/vocabulary'
    out.mkdir(parents=True, exist_ok=True)
    manifest = {'author':'egg rolls', 'repository':'https://github.com/5mdld/anki-jlpt-decks', 'commit':COMMIT, 'source':SOURCE, 'source_sha256':hashlib.sha256(raw).hexdigest(), 'license':'CC BY-NC 4.0', 'fields_1based':{'id':3,'japanese':4,'reading':7,'meaning_zh':8,'level':2,'sort':36}, 'excluded':excluded, 'levels':{}}
    for level, entries in groups.items():
        entries.sort(key=lambda v:(v[0],v[1][0]))
        path=out/f'{level}.csv'
        with path.open('w',encoding='utf-8',newline='') as f:
            w=csv.writer(f); w.writerow(['id','japanese','reading','meaning_zh','level']); w.writerows(v[1] for v in entries)
        manifest['levels'][level]={'count':len(entries),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()}
    (out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(manifest,ensure_ascii=True,indent=2))
if __name__=='__main__':
    p=argparse.ArgumentParser(); p.add_argument('--source',type=Path,required=True); convert(p.parse_args().source)
