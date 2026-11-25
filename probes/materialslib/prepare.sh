#!/usr/bin/env bash
# Run from the project root after its baseline build. No root Gradle invocation.
set -euo pipefail
export LC_ALL=C TZ=UTC
shopt -s globstar nullglob
sha=f68a8b09d45d126f5c41a6790dfc53e18be4fb02
out=build/probe-materialslib
home=${GRADLE_USER_HOME:-.gradle/user-home}
cache=$home/caches/modules-2/files-2.1
jdk=${JAVA17_HOME:-$home/jdks/eclipse_adoptium-17-amd64-linux.2}
mc=build/rfg/recompiled_minecraft-1.7.10.jar
[[ -x $jdk/bin/javac && -f $mc ]] || { echo 'Baseline build and JAVA17_HOME (JDK 17) required' >&2; exit 1; }
mkdir -p "$out"
work=$(mktemp -d "$out/compile.XXXXXXXX")
# Retain the isolated work directory for build-input and bytecode inspection.
fetch() {
    if [[ ! -f $out/$1 ]]; then curl -fLsS "$3" -o "$out/$1"; fi
    printf '%s  %s\n' "$2" "$out/$1" | sha256sum --check
}
fetch source.tar.gz 7ac9cb8770c52242aedd04e67df21dfa9f20f7191f4ad06692088aad5aeacef1 "https://codeload.github.com/GTNewHorizons/MaterialsLib/tar.gz/$sha"
fetch gtnhlib-0.5.11-dev.jar 08df36978628367adeb77a69fb3516e791db40f2fca7fd7445a6a4bc1d3a1c1a https://github.com/GTNewHorizons/GTNHLib/releases/download/0.5.11/gtnhlib-0.5.11-dev.jar
fetch annotations-24.1.0.jar 27a770dc7ce50500918bb8c3c0660c98290630ec796b5e3cf6b90f403b3033c6 https://repo.maven.apache.org/maven2/org/jetbrains/annotations/24.1.0/annotations-24.1.0.jar

tar xzf "$out/source.tar.gz" -C "$work"
src=$work/MaterialsLib-$sha
lib=$work/lib
fixture=$work/fixture
mkdir "$lib" "$fixture"
version=0.0.0-dev.${sha:0:12}
printf 'package com.gtnewhorizons.materialslib; public final class Tags { public static final String VERSION = "%s"; }\n' "$version" > "$work/Tags.java"

cached() {
    local matches=("$cache/$1"/*/"$2")
    [[ ${#matches[@]} == 1 ]] || { echo "Need exactly one $1/$2 under $cache" >&2; exit 1; }
    printf '%s' "${matches[0]}"
}
cp="$mc:$out/gtnhlib-0.5.11-dev.jar:$out/annotations-24.1.0.jar"
cp+=:$(cached com.google.guava/guava/17.0 guava-17.0.jar)
for log in log4j-api log4j-core; do cp+=:$(cached "org.apache.logging.log4j/$log/2.0-beta9-fixed" "$log-2.0-beta9-fixed.jar"); done
jabel=$(cached com.github.GTNewHorizons/jabel-javac-plugin/1.0.2-GTNH jabel-javac-plugin-1.0.2-GTNH.jar)
for bb in byte-buddy byte-buddy-agent; do jabel+=:$(cached "net.bytebuddy/$bb/1.14.9" "$bb-1.14.9.jar"); done
jabel+=:$(cached net.java.dev.jna/jna/5.19.1 jna-5.19.1.jar)
"$jdk/bin/javac" --release 8 -encoding UTF-8 -J-Djdk.attach.allowAttachSelf=true -Xplugin:jabel -processorpath "$jabel" -cp "$cp" -d "$lib" "$work/Tags.java" "$src"/src/main/java/**/*.java
sed -e 's/${modId}/materialslib/g' -e 's/${modName}/MaterialsLib/g' -e "s/\${modVersion}/$version/g" -e 's/${minecraftVersion}/1.7.10/g' "$src/src/main/resources/mcmod.info" > "$lib/mcmod.info"
cp "$src/LICENSE.txt" "$lib/LICENSE.txt"
libjar=$out/MaterialsLib-${sha:0:12}-dev.jar
"$jdk/bin/jar" --create --no-manifest --date=2000-01-01T00:00:00Z --file="$libjar" -C "$lib" .
"$jdk/bin/javac" --release 8 -encoding UTF-8 -proc:none -cp "$libjar:$cp" -d "$fixture" probes/materialslib/src/**/*.java
"$jdk/bin/jar" --create --no-manifest --date=2000-01-01T00:00:00Z --file="$out/materials-probe-dev.jar" -C "$fixture" .
sha256sum "$libjar" "$out/materials-probe-dev.jar"
