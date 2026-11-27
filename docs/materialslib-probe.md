---
type: Verification Guide
title: MaterialsLib の鉄登録と取得
description: 検証用 JAR の準備、鉄の登録・取得と名前空間の対照試験。
---
# MaterialsLib の鉄登録と取得

1. 基準構成を `VERSION=0.0.0-dev ./gradlew build` でビルドし、終了を待つ。Gradle の既定キャッシュを使った場合は、ルートで `GRADLE_USER_HOME="$HOME/.gradle" bash probes/materialslib/prepare.sh` を実行する。キャッシュを変更している場合は基準ビルドと同じパスを指定する。初回は HTTPS 接続が必要。スクリプトは Gradle を起動せず、`build/probe-materialslib/` にだけ書く。`JAVA17_HOME` で Jabel 用 JDK 17 を選べる。`clean` で検証 JAR を消した場合は基準構成からやり直す。
2. 検証用プロパティ `ooenProbe` が `materialslib` または `both` の場合**だけ**、ルートプロジェクトから `apply from: 'probes/materialslib/probe.gradle'` する。`devOnlyNonPublishable` に開発名の JAR を二つ追加するだけで、`main` は変更しない。配布・再難読化・公開は行わない。
3. `VERSION=0.0.0-dev ./gradlew -PooenProbe=materialslib runClient25` を実行する。終了して同じコマンドでもう一度起動し、各回の FML ログを上書き前に保存する。下表のイベントと ID を比較する。専用サーバーで比較する際は同じプロパティで `runServer25` を使う。

| ログ（期待値） | 観測内容 |
| --- | --- |
| `MaterialRegistryEvent created registry=ooen_iron_provider` | 外部 MOD が Forge イベントで registry を作成。 |
| `MaterialEvent registered ooen_iron_provider:iron id=26` | 提供 MOD が鉄を登録し、作成時に返った registry で実オブジェクトを検証。`26` は提供側指定の ID（自動採番ではない）。 |
| `MATERIALSLIB_IRON_PASS registry=ooen_iron_provider name=iron id=26 phase=FROZEN` | 別の利用 MOD がロード完了後、名前と数値 ID の両方で同一の鉄を取得し、所有者・名前・ID・フェーズを検証。不一致なら例外。 |
| `MATERIALSLIB_NAMESPACE_CONTROL isolated=false requested=ooen_iron_consumer returned=ooen_iron_provider` | 利用側がイベントの最後に作った**実在する二つ目の registry** を名前で引いた結果。`false` は名前空間の分離失敗であり、上の鉄単体の成功と別に記録する。 |

固定コミットの [`getRegistry`](https://github.com/GTNewHorizons/MaterialsLib/blob/f68a8b09d45d126f5c41a6790dfc53e18be4fb02/src/main/java/com/gtnewhorizons/materialslib/registry/MaterialRegistryManager.java#L56-L65) は、一致する registry があっても最初の registry を返す。したがって、最初に作られた提供側で鉄を取得できても、多数の MOD 間の識別を保証しない。この fixture はアイテム・レシピやワールド保存を検証しない。

## 実測

2026-11-27、Minecraft 1.7.10・Forge 10.13.4.1614 の開発クライアントで記録。GTNH パックと GregTech 本体は未導入。ルートで指定された GTNHLib 0.12.10、FalsePatternLib 1.12.2、Forgelin 2.4.3-2.4.10 を使用し、下記の JAR の SHA-256 は両方とも一致した。

- `MaterialRegistryEvent created`、`MaterialEvent registered` の順に提供側で登録し、利用側で `MATERIALSLIB_IRON_PASS registry=ooen_iron_provider name=iron id=26 phase=FROZEN` を確認した。
- 名前空間の対照試験は `MATERIALSLIB_NAMESPACE_CONTROL isolated=false requested=ooen_iron_consumer returned=ooen_iron_provider`。二つ目のレジストリを名前で取得できなかった。
- 単体構成ではクライアントを終了して起動し直しても、取得した ID と名前空間の対照試験の結果は同じだった。数値 ID は提供側の固定値で、ワールドを保存して再度開く試験は保留。

## 入手・再現性

[固定ソース](https://github.com/GTNewHorizons/MaterialsLib/tree/f68a8b09d45d126f5c41a6790dfc53e18be4fb02) には調査時点で対応する GitHub リリース／タグが見当たらなかった。全 `src/main/java` を RFG の `recompiled_minecraft-1.7.10.jar`、[GTNHLib 0.5.11 dev](https://github.com/GTNewHorizons/GTNHLib/releases/tag/0.5.11)、JetBrains annotations 24.1.0 に対して Jabel でコンパイルした。上流設定相当の版定数と `mcmod.info` を展開し、ライセンスと上流 Java ソースから得たクラスを同梱した。実行時の GTNHLib はルートの開発構成に従う。

取得 URL と SHA-256 の照合は [`prepare.sh`](../probes/materialslib/prepare.sh) にある。JDK 17.0.21.1、Jabel 1.0.2-GTNH、Java 8 バイトコード（major 52）。次は現行の開発 JAR の SHA-256。版 `0.0.0-dev.f68a8b09d45d` は固定コミットに対応するローカル識別子。

| JAR | SHA-256 |
| --- | --- |
| MaterialsLib-f68a8b09d45d-dev.jar | `ed08bfac28b7b62cfae2f3cccca7fe0feefa5174468bda80308152cf6522c98d` |
| materials-probe-dev.jar（提供・利用の 2 MOD） | `2715a91bafbb705a5165bd0fa5eaa2c900a9385bd6609b53395f1ebe56507a8a` |
