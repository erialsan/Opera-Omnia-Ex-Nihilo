---
type: Development Guide
title: 開発環境と起動
description: Minecraft 1.7.10 向けのビルド、開発クライアント・サーバーの起動、依存関係。
---
# 開発環境と起動

## 基準構成を起動する

Minecraft **1.7.10**、Forge **10.13.4.1614**、MCP **stable 12** を使う。ビルドは **JDK 25** と同梱の **Gradle 9.7.1**、GTNH settings convention **2.0.34**。`gradle.properties` の `enableModernJavaSyntax = jvmDowngrader` で **JvmDowngrader** を使用する。生成する MOD のベースクラスは Java 8 向けのバイトコードに変換する。GTNH のビルドツールを使うことと、ゲーム側に GTNH パックを必須にすることは別である。

1. JDK 25 で以下を実行する。初回は Gradle、ツールチェーン、Minecraft、Forge 等の取得にネット接続が必要。Git タグのない作業コピーでは開発用の版を指定する。

   ```sh
   export VERSION=0.0.0-dev
   ./gradlew --version
   ./gradlew build
   ```

2. Wayland 環境に合わせ、クライアントは **`./gradlew runClient25`** でタイトルまで起動する（クライアントの実行環境は Java 25）。JAOPCA を追加していない基準構成では、FML ログに `Materia metals: count=0 names=[]` が出る。
3. `./gradlew runServer25` で開発用専用サーバーも起動する。初回の EULA 確認には、内容に同意する場合のみ応答する。サーバーでも同じログを確認し、`stop` で終了する。本番ワールドは使わない。

Kotlin 移行後の実測（2026-11-26）：`build` と `runClient25` が成功。Forgelin が Kotlin 2.4.10 をロードし、Materia のロード完了時に `ingotIron entries=1` を確認した。MaterialsLib・JAOPCA は未導入。`runServer25` は初回の EULA 確認で停止したため、専用サーバーは未検証。

## 必須依存

配布版 Materia を使う場合は、Minecraft 1.7.10 向けの以下の MOD をクライアント・専用サーバーの両方に入れる（記載は最低バージョン）。開発時は Gradle が dev JAR を取得する。

- [Forgelin 2.4.3-2.4.10](https://github.com/LegacyModdingMC/Forgelin/releases/tag/2.4.3-2.4.10)：Kotlin コンパイラは提供されるランタイムと同じ 2.4.10 を使う。通常版は初回起動時に Kotlin ランタイムを取得する。
- [FalsePatternLib 1.12.2](https://github.com/FalsePattern/FalsePatternLib/releases/tag/1.12.2)
- [GTNHLib 0.12.10](https://github.com/GTNewHorizons/GTNHLib/releases/tag/0.12.10)：Materia のコードから API を使用できるよう、コンパイル時にも追加する。

金属カタログを使う場合は、実行環境に [JAOPCA 1.7.10-W.0.8.32](https://www.curseforge.com/minecraft/mc-mods/jaopca/files/8859967) を追加する。JAOPCA が無い環境ではカタログは空になる。

## 開発用の依存関係

`dependencies.gradle` で **BugTorch 1.3.1** と **ArchaicFix 0.8.0** を Modrinth Maven から、**JarJar 0.4.2-beta** を GTNH Maven から取得する。いずれも `runtimeOnlyNonPublishable` で開発環境の実行時に使う。

EndlessIDs 1.7.1 と ChunkAPI 0.8.1 も開発環境の実行時だけに追加する。この組み合わせは [ArchaicFix 0.8.0 のコンパイル時依存](https://github.com/embeddedt/ArchaicFix/blob/0.8.0/dependencies.gradle) に合わせている。ChunkAPI 0.5.1 には ArchaicFix の照明処理が呼び出す `registerDataManager(DataManager, int)` がなく、起動時に `NoSuchMethodError` になる。EndlessIDs 1.5-beta0003 は ChunkAPI 0.6.0 未満を要求するため、ChunkAPI だけは更新できない。配布版 Materia の必須依存ではない。

JarJar の動作確認には、RetroFuturaBootstrap を使う `./gradlew runClient25` または `./gradlew runServer25` を使用する。

ゲーム内の操作は [テストプレイ](playtest.md)、素材ライブラリの比較は [依存検証](dependency-tests.md) を参照。
