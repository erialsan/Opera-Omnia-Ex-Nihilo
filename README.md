---
type: Development Guide
title: Materia 開発環境と依存検証
---
# Opera Omnia Ex Nihilo: Materia

[開発計画](development-plan.md)の最初の問いは、既存の素材から鉱石砂利・鉱石砂・鉱石粉を重複なく用意できるか、という点。まず `ooen_materia` 一つだけをロードし、外部ライブラリを追加する前の比較値を取る。この段階ではアイテムもレシピも登録しない。

## 基準構成を起動する

Minecraft **1.7.10**、Forge **10.13.4.1614**、MCP **stable 12** を使う。ビルドは **JDK 25** と同梱の **Gradle 9.7.1**、GTNH settings convention **2.0.34**。`gradle.properties` の `enableModernJavaSyntax = jvmDowngrader` で **JvmDowngrader** を使用する。生成する MOD のベースクラスは Java 8 向けのバイトコードに変換する。GTNH のビルドツールを使うことと、ゲーム側に GTNH パックを必須にすることは別である。

1. JDK 25 で以下を実行する。初回は Gradle、ツールチェーン、Minecraft、Forge 等の取得にネット接続が必要。Git タグのない作業コピーでは開発用の版を指定する。

   ```sh
   export VERSION=0.0.0-dev
   ./gradlew --version
   ./gradlew build
   ```

2. Wayland 環境に合わせ、クライアントは **`./gradlew runClient25`** でタイトルまで起動する（クライアントの実行環境は Java 25）。FML ログの `OreDictionary baseline: ingotIron entries=N` を探し、`N` が 1 以上であることを記録して終了する。これはロード完了時の OreDictionary エントリ数であり、素材 ID やユニークな提供元の数ではない。
3. `./gradlew runServer` で開発用専用サーバーも起動する。初回に EULA で停止した場合は、内容に同意する場合のみ開発サーバー側で設定して再実行する。サーバーでも同じログを確認し、`stop` で終了する。本番ワールドは使わない。

基準構成の実測（2026-11-25）：`build` 成功。`runClient25` で `ooen_materia@0.0.0-dev` がロードされ、`ingotIron entries=1`、シングルプレイのワールド参加まで確認した。MaterialsLib・JAOPCA は未導入。専用サーバーは未検証。

## 開発用の依存関係

`dependencies.gradle` で **BugTorch 1.3.1** と **ArchaicFix 0.8.0** を Modrinth Maven から、**JarJar 0.4.2-beta** を GTNH Maven から取得する。いずれも `runtimeOnlyNonPublishable` で開発環境の実行時に使う。

JarJar の動作確認には、RetroFuturaBootstrap を使う `./gradlew runClient25` または `./gradlew runServer25` を使用する。

## 最初の依存検証：鉄一種を通す

候補を比較するときだけ、非公開のローカル Gradle 設定 `addon.local.gradle` の `dependencies` に `devOnlyNonPublishable` で検証用 JAR を追加する。難読化された配布 JAR は `rfg.deobf(project.files("…"))` を通す。両方を一度に追加せず、基準 → 各単体 → 併用の順に実行する。

| 試験 | 実施内容と合格条件 |
| --- | --- |
| MaterialsLib 単体 | [固定したソースと外部登録のサンプル](https://github.com/GTNewHorizons/MaterialsLib/tree/f68a8b09d45d126f5c41a6790dfc53e18be4fb02/src/materialsTest/java/com/gtnewhorizons/materialstest)を起点に、提供側で鉄を登録し、別の利用側から同じ ID で取得する。登録イベントと再起動前後の ID を記録する。ソースリビジョンは配布 JAR の版を保証しない。 |
| JAOPCA 単体 | [1.7.10 系 W.0.8.32 のソース](https://github.com/TheLMiffy1111/JAOPCA/tree/ae1abf9c8b4106cced63bb63ad5cad65b6c2b875)を候補に、検出した鉄から三形態と加工レシピを要求する。形態ごとに登録名と件数を記録し、除外した形態には新規品もレシピもないことを確認する。 |
| 既存品・別名・遅延 | 提供側で一形態を先に登録し、同じ鉄を別名／別 MOD からも登録する。次に登録を検出後に遅らせて再試行する。再利用した ID と検出漏れを記録する。形態ごとにアイテムとレシピの重複件数を別々に数え、両方とも 0 件を合格とする。`ingotIron` の件数だけでは判定しない。 |
| 併用・再起動 | 両者の識別子を鉄に対応付け、各形態の登録元を一つにする。専用サーバーとクライアントで加工し、同じ検証ワールドを開き直してスタックとレシピが維持されることを確認する。 |

各試験で **JAR の出所・版・SHA-256、追加の必須 MOD、GTNH パック有無と版、起動側、観測値、ログ箇所** を残す。表は実施手順であり、互換性の確認結果ではない。両ライブラリの必須化、GTNH なしのサポート、JAR の分割は未決。併用で重複や遅延登録を解決できなければ、鉄だけを Materia が直接登録する構成と比較してから篩・粉砕に進む。
