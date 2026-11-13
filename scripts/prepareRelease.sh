#!/usr/bin/env bash
set -euo pipefail

: "${BUILD_NUMBER:?BUILD_NUMBER must identify the CI build}"
[[ "$BUILD_NUMBER" =~ ^[1-9][0-9]*$ ]] || { echo 'Invalid BUILD_NUMBER' >&2; exit 1; }
mc_version=$(sed -n 's/^mcVersion=//p' gradle.properties | tr -d '\r')
channel=$(sed -n 's/^channel=//p' gradle.properties | tr -d '\r' | tr '[:upper:]' '[:lower:]')
filename="tintedpaper-${mc_version}.build.${BUILD_NUMBER}-${channel}.jar"
test -f "build/dist/$filename"
mkdir -p dist
cp "build/dist/$filename" "dist/$filename"
(cd dist && sha256sum "$filename" > "$filename.sha256")

cat > dist/release-notes.md <<EOF
TintedPaper é um fork pessoal AI-driven do Paper para um servidor privado de Minecraft.

- Minecraft: ${mc_version}; canal: ${channel}; build: ${BUILD_NUMBER}.
- Migração em andamento; esta é uma construção de teste.
- Código: https://github.com/KiyroPrower/TintedPaper/commit/$(git rev-parse HEAD)
- Validação: https://github.com/KiyroPrower/TintedPaper/actions/runs/${GITHUB_RUN_ID:-local}
- Todos os patches continuam sendo de direito autoral de seus respectivos criadores.

O JAR e seu checksum SHA-256 estão anexados. Nenhuma implantação no servidor é realizada por este workflow.
EOF
