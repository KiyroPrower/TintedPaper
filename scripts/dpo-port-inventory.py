"""Read-only inventory of active DPO injections and their native target sources."""
import argparse
import json
import re
from collections import Counter
from pathlib import Path


def inventory(source, fork):
    roots = [fork / "tintedpaper-server/src/minecraft/java", fork / "paper-server/src/main/java",
             fork / "tintedpaper-server/src/main/java", fork / "paper-api/src/main/java"]
    rows = []
    seen = set()
    for manifest in sorted((source / "src/main/resources").glob("datapackoptimizer*.mixins.json")):
        config = json.loads(manifest.read_text(encoding="utf-8-sig"))
        for section in ("mixins", "server", "client"):
            for name in config.get(section, []):
                qualified = config["package"] + "." + name
                if qualified in seen:
                    raise ValueError("Duplicate active injection: " + qualified)
                seen.add(qualified)
                path = source / "src/main/java" / (qualified.replace(".", "/") + ".java")
                text = path.read_text(encoding="utf-8-sig")
                imports = {value.rsplit(".", 1)[-1]: value
                           for value in re.findall(r"^import ([\w.]+);", text, re.M)}
                annotation = re.search(r"^\s*@Mixin\((.*?)\)", text, re.S | re.M)
                if annotation is None:
                    raise ValueError("Missing mixin annotation: " + qualified)
                body = annotation.group(1)
                targets = []
                for value in re.findall(r"([\w.]+)\.class", body):
                    outer, separator, inner = value.partition(".")
                    targets.append(imports.get(outer, outer) + (separator + inner if separator else ""))
                targets.extend(value.replace("/", ".") for value in re.findall(r'"([\w.$/]+)"', body))
                if not targets:
                    raise ValueError("No targets parsed from the real annotation: " + qualified)
                native = []
                for target in targets:
                    enclosing = target.split("$", 1)[0]
                    while "." in enclosing:
                        relative = enclosing.replace(".", "/") + ".java"
                        found = [(root / relative) for root in roots if (root / relative).is_file()]
                        if found:
                            native.extend(path.relative_to(fork).as_posix() for path in found)
                            break
                        enclosing = enclosing.rsplit(".", 1)[0]
                rows.append({"mixin": qualified, "manifest": manifest.name, "section": section,
                             "source": path.relative_to(source).as_posix(), "targets": targets,
                             "native_sources": native,
                             "review": "pending-semantic-comparison" if native else "pending-target-resolution"})
    if not rows:
        raise ValueError("No active DPO injections found")
    return rows


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--fork", type=Path, default=Path(__file__).resolve().parent.parent)
    parser.add_argument("--summary", action="store_true")
    parser.add_argument("--unresolved", action="store_true", help="Show only unresolved native targets")
    args = parser.parse_args()
    rows = inventory(args.source.resolve(), args.fork.resolve())
    decisions = args.fork / "docs/dpo-port-decisions.json"
    if decisions.is_file():
        reviewed = set()
        for group in json.loads(decisions.read_text(encoding="utf-8-sig"))["groups"]:
            for suffix in group["mixins"]:
                matches = [row for row in rows if row["mixin"].endswith("." + suffix)]
                if len(matches) != 1:
                    raise ValueError("Decision must identify exactly one active injection: " + suffix)
                row = matches[0]
                if row["mixin"] in reviewed:
                    raise ValueError("Duplicate migration decision: " + suffix)
                reviewed.add(row["mixin"])
                row.update(review=group["status"], reason=group["reason"], evidence=group["evidence"])
                if "native_sources" in group:
                    for native in group["native_sources"]:
                        if not (args.fork / native).is_file():
                            raise ValueError("Missing native source in migration decision: " + native)
                    row["native_sources"] = group["native_sources"]
    if args.unresolved:
        rows = [row for row in rows if row["review"] == "pending-target-resolution"]
    result = {"total": len(rows), "manifests": dict(Counter(row["manifest"] for row in rows)),
              "status": dict(Counter(row["review"] for row in rows))}
    if not args.summary:
        result["entries"] = rows
    print(json.dumps(result, indent=2))
