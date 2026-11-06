import re
from pathlib import Path

import yaml

root = Path(__file__).resolve().parents[1]
controller_dir = root / "src" / "main" / "java" / "com" / "joker" / "emp" / "controller"
doc = yaml.safe_load((Path(__file__).with_name("openapi.yaml")).read_text(encoding="utf-8"))

actual = set()
for java_file in controller_dir.glob("*.java"):
    text = java_file.read_text(encoding="utf-8")
    class_match = re.search(r'@RequestMapping\("([^"]*)"\)', text)
    base = class_match.group(1) if class_match else ""
    for match in re.finditer(r'@(Get|Post|Put|Delete)Mapping(?:\(([^)]*)\))?', text):
        method = match.group(1).lower()
        args = match.group(2) or ""
        path_match = re.search(r'"([^"]*)"', args)
        sub = path_match.group(1) if path_match else ""
        full = "/" + "/".join(part.strip("/") for part in (base, sub) if part.strip("/"))
        if full == "/":
            full = "/"
        if full.endswith("/**"):
            full = full[:-3] + "/{objectName}"
        actual.add((method, full))

documented = set()
for path, item in doc["paths"].items():
    for method in ("get", "post", "put", "delete"):
        if method in item:
            documented.add((method, path))

missing = sorted(actual - documented)
extra = sorted(documented - actual)
duplicates = []
for path, item in doc["paths"].items():
    seen = [m for m in ("get", "post", "put", "delete") if m in item]
    if len(seen) > 1:
        duplicates.append((path, seen))

print(f"actual={len(actual)} documented={len(documented)}")
print(f"missing={len(missing)} extra={len(extra)}")
for method, path in missing:
    print("MISSING", method.upper(), path)
for method, path in extra:
    print("EXTRA  ", method.upper(), path)
if duplicates:
    print("DUPLICATE PATHS:")
    for path, methods in duplicates:
        print("-", path, methods)

raise SystemExit(0 if not missing and not extra else 1)
