---
type: Development Guide
title: Materia 開発環境と依存検証
---
# Opera Omnia Ex Nihilo: Materia

[開発計画](development-plan.md)では、既存の素材から鉱石砂利・鉱石砂・鉱石粉を重複なく用意できるかを検証する。Materia は JAOPCA が認識した金属と、環境に登録済みの加工形態を参照する。

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

## Materia の金属カタログ

初回起動で生成される `config/ooen_materia.cfg` の `materials.metalWhitelist` で対象を指定する。JAOPCA の素材名を大文字・小文字も含めて指定し、編集後は再起動する。合金や別名で検出された素材にも個別の許可が必要。空のリストでは全素材を無効にできる。

初期値は `Iron`、`Gold`、`Copper`、`Tin`、`Aluminum`、`Aluminium`、`Lead`、`Nickel`、`Silver`、`Cobalt`、`Ardite`、`Osmium`、`Iridium`、`Tungsten`、`Boron`、`Uranium`。

ロード完了後の `MetalCatalog.names()` は、ホワイトリストにあり、JAOPCA がインゴット系として認識し、OreDictionary に具体的なインゴットが登録された素材の名前を返す。Forge がインゴット扱いするバニラのレンガは対象外。`MetalCatalog.candidates("Iron", MetalForm.GRAVEL)` のように素材名と形態を指定すると、`ingot`、`ooenOreGravel`、`ooenOreSand`、`ooenOrePowder` の各接頭辞で登録済みアイテムを照合する。許可されていても環境に銅が無ければ、候補は空になる。Materia の照合は読み取り専用。

JAOPCA だけを追加して確認するには `./gradlew runClient25 -PooenProbe=jaopca-baseline` を使う。JAOPCA の JAR は下の依存検証と同じ場所に置く。この構成から提供側を追加したときだけ、追加した金属がカタログに現れることを確認できる。

初期ホワイトリストでの実測では、JAOPCA 未導入時は `[]`、JAOPCA 単体では `[Gold, Iron]`、銅と青銅の検証用 MOD を追加した場合は `[Copper, Gold, Iron]` だった。

## 最初の依存検証：鉄一種を通す

検証用 MOD は通常のビルドに含めない。基準 → 各単体 → 併用の順に、`-PooenProbe=materialslib|jaopca|both` のいずれかを指定して起動する。

- MaterialsLib: [準備と実行の手順](probes/materialslib/README.md)を参照。
- JAOPCA: [配布ファイル 8859967](https://www.curseforge.com/minecraft/mc-mods/jaopca/files/8859967)を `build/probe-jaopca/JAOPCA-1.7.10-W.0.8.32.jar` に置き、`./gradlew runClient25 -PooenProbe=jaopca -PjaopcaCase=reuse` を実行する。Gradle が SHA-256 の照合と逆難読化を行う。`generate` と `exclude` も指定できる。`MATERIA_IRON_CANDIDATES_PASS` は実際の登録品からの候補取得を示す。

JAOPCA `reuse` の実測（2026-11-26、開発クライアント・GTNH パックなし）：鉄の鉱石砂利は既存品を再利用し、砂と粉は各 1 種を生成した。三形態とも対応するレシピは 1 件。ただし対照試験では別フェーズから同じキーで再登録したレシピが 2 件になった。また、早期の別名は別素材になり、遅延した別名は OreDictionary に届いたが素材としては認識されなかった。この実測は JAOPCA 側の試験で、追加した Materia の候補取得アサーションを含まない。Materia 本体は画面を起動せず Forge の OreDictionary を使って確認し、未登録キーを作らないこと、後から登録した複数候補を拾うこと、ワイルドカードを除外し元のスタックや NBT を変更しないことを確認した。

MaterialsLib 単体の実測（2026-11-27、開発クライアント・GTNH パックなし・GTNHLib 0.12.10）は [検証結果](probes/materialslib/README.md#実測) を参照。外部の利用側 MOD が鉄を ID 26 で取得できた。二つ目のレジストリを名前で問い合わせると一つ目が返り、名前空間の分離に失敗した。

同日の再検証では、JAOPCA 単体の `generate`、`reuse`、`exclude` と併用の `reuse` で `MATERIA_IRON_CANDIDATES_PASS` を確認した。生成と再利用の対象形態は各 1 アイテム・1 レシピ。除外した粉は 0 アイテム・0 レシピだった。併用時は `MATERIALSLIB_IRON_PASS` も出た。検証した JAOPCA W.0.8.32 の SHA-256 は `0461f752fa25bb7b1a00d1cee984f0edd0e846408b8d6c579e6c4c62f598bacd`。JAOPCA 用プロファイルは UniMixins 0.3.1 を追加する。

| 試験 | 実施内容と合格条件 |
| --- | --- |
| MaterialsLib 単体 | [固定したソースと外部登録のサンプル](https://github.com/GTNewHorizons/MaterialsLib/tree/f68a8b09d45d126f5c41a6790dfc53e18be4fb02/src/materialsTest/java/com/gtnewhorizons/materialstest)を起点に、提供側で鉄を登録し、別の利用側から同じ ID で取得する。登録イベントと再起動前後の ID を記録する。ソースリビジョンは配布 JAR の版を保証しない。 |
| JAOPCA 単体 | [1.7.10 系 W.0.8.32 のソース](https://github.com/TheLMiffy1111/JAOPCA/tree/ae1abf9c8b4106cced63bb63ad5cad65b6c2b875)を候補に、検出した鉄から三形態と加工レシピを要求する。形態ごとに登録名と件数を記録し、除外した形態には新規品もレシピもないことを確認する。 |
| 既存品・別名・遅延 | 提供側で一形態を先に登録し、同じ鉄を別名／別 MOD からも登録する。次に登録を検出後に遅らせて再試行する。再利用した ID と検出漏れを記録する。形態ごとにアイテムとレシピの重複件数を別々に数え、両方とも 0 件を合格とする。`ingotIron` の件数だけでは判定しない。 |
| 併用・再起動 | 両者の識別子を鉄に対応付け、各形態の登録元を一つにする。専用サーバーとクライアントで加工し、同じ検証ワールドを開き直してスタックとレシピが維持されることを確認する。 |

各試験で **JAR の出所・版・SHA-256、追加の必須 MOD、GTNH パック有無と版、起動側、観測値、ログ箇所** を残す。表は実施手順であり、互換性の確認結果ではない。両ライブラリの必須化、GTNH なしのサポート、JAR の分割は未決。併用で重複や遅延登録を解決できなければ、鉄だけを Materia が直接登録する構成と比較してから篩・粉砕に進む。

## バニラにない銅での依存検証

同じ手順に `-PooenProbeMetal=copper` を加える。JAOPCA の試験では、提供側が登録する前の `ingotCopper` と `oreCopper` が空であることを検証してから、検証用の銅インゴットと銅鉱石アイテムを OreDictionary に追加する。MaterialsLib の試験では、提供側が `copper` を固定 ID 29 で登録し、利用側が名前と数値 ID で同じ素材を取得できることを検証する。

```sh
VERSION=0.0.0-dev ./gradlew runClient25 -PooenProbe=both -PooenProbeMetal=copper -PjaopcaCase=reuse
```

併用時は FML ログの `MATERIALSLIB_COPPER_PASS` と `JAOPCA_COPPER_PASS` を確認する。銅の検証用 MOD は対照として青銅も登録し、JAOPCA が検出した青銅が初期ホワイトリストではカタログと候補の両方から除外されることを確認する。`generate` では加工三形態を生成し、`exclude` では砂利を再利用して砂を生成し、粉を除外する。

2026-11-27 の実測（Minecraft 1.7.10・Forge 10.13.4.1614 の開発クライアント、GTNH パックなし、上記と同じ依存）では、提供前の銅はインゴット・鉱石とも 0 件だった。MaterialsLib 単体では ID 29 を取得し、二つ目のレジストリの名前検索は鉄と同様に失敗した。

| JAOPCA の条件 | 鉱石砂利 | 鉱石砂 | 鉱石粉 |
| --- | --- | --- | --- |
| `generate` | 生成 | 生成 | 生成 |
| `reuse` | 既存品を再利用 | 生成 | 生成 |
| `exclude` | 既存品を再利用 | 生成 | 除外 |

`phase=loadComplete form=` の各行で、生成・再利用した形態は各 1 アイテム・1 レシピ、除外した粉は 0 アイテム・0 レシピを確認した。鉄と同じく、早期の別名は別素材になり、遅延した別名は OreDictionary に届く段階までだった。別フェーズで同じキーを使った対照試験のレシピは 2 件になった。専用サーバーでの加工とワールドの保存・再読み込みは未検証。
