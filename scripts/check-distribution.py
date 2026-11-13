"""Check the actual API, server and launcher artifacts before publishing."""
import json
import os
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parent.parent
properties = dict(line.split("=", 1) for line in (root / "gradle.properties").read_text().splitlines()
                  if "=" in line and not line.startswith("#"))
mc = properties["mcVersion"]
build = os.environ.get("BUILD_NUMBER")
version = f"{mc}.build.{build}-{properties['channel'].lower()}" if build else f"{mc}.local-SNAPSHOT"
server = root / "tintedpaper-server/build/libs" / f"tintedpaper-server-{version}.jar"
api = root / "tintedpaper-api/build/libs" / f"tintedpaper-api-{version}.jar"
launcher = root / "build/dist" / f"tintedpaper-{version}.jar"

with zipfile.ZipFile(server) as jar:
    manifest = jar.read("META-INF/MANIFEST.MF").decode().replace("\r\n ", "")
    for attribute in ("Brand-Id: kiyroprower:tintedpaper", "Brand-Name: TintedPaper",
                      f"Specification-Version: {version}", f"Build-Number: {build or ''}"):
        assert attribute in manifest.splitlines(), f"Missing manifest value: {attribute}"
    if os.environ.get("BUILD_BRANCH"):
        assert f"Git-Branch: {os.environ['BUILD_BRANCH']}" in manifest.splitlines()
with zipfile.ZipFile(api) as jar:
    assert json.loads(jar.read("apiVersioning.json"))["version"] == version
with zipfile.ZipFile(launcher) as jar:
    assert jar.testzip() is None, "Corrupt launcher archive"
    assert "Main-Class: io.papermc.paperclip.Main" in jar.read("META-INF/MANIFEST.MF").decode()
print(f"Verified TintedPaper {version}: API, server metadata and Paperclip distribution")
