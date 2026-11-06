import sys
from pathlib import Path

import yaml


class UniqueKeyLoader(yaml.SafeLoader):
    pass


def construct_mapping(loader, node, deep=False):
    mapping = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in mapping:
            raise ValueError(f"duplicate key: {key!r} at line {key_node.start_mark.line + 1}")
        mapping[key] = loader.construct_object(value_node, deep=deep)
    return mapping


UniqueKeyLoader.add_constructor(
    yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, construct_mapping
)

path = Path(__file__).with_name("openapi.yaml")
doc = yaml.load(path.read_text(encoding="utf-8"), Loader=UniqueKeyLoader)

problems = []

refs = []


def walk(node):
    if isinstance(node, dict):
        for key, value in node.items():
            if key == "$ref" and isinstance(value, str):
                refs.append(value)
            else:
                walk(value)
    elif isinstance(node, list):
        for item in node:
            walk(item)


walk(doc)

for ref in refs:
    if not ref.startswith("#/"):
        problems.append(f"external ref not allowed: {ref}")
        continue
    current = doc
    for part in ref[2:].split("/"):
        part = part.replace("~1", "/").replace("~0", "~")
        if not isinstance(current, dict) or part not in current:
            problems.append(f"missing ref target: {ref}")
            break
        current = current[part]

paths = doc.get("paths", {})
operations = 0
for item in paths.values():
    for method in ("get", "post", "put", "patch", "delete"):
        if method in item:
            operations += 1

print(f"openapi={doc.get('openapi')}")
print(f"paths={len(paths)} operations={operations} refs={len(refs)}")
print(f"schemas={len(doc.get('components', {}).get('schemas', {}))}")
print(f"parameters={len(doc.get('components', {}).get('parameters', {}))}")
print(f"responses={len(doc.get('components', {}).get('responses', {}))}")

if problems:
    print("PROBLEMS:")
    for problem in sorted(set(problems)):
        print("-", problem)
    sys.exit(1)

print("validation=ok")
