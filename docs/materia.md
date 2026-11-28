---
type: Module Guide
title: Materia の金属カタログ
description: 金属の検出、ホワイトリスト、鉱石素材の登録と加工。
---
# Materia の金属カタログ

初回起動で生成される `config/ooen_materia.cfg` の `materials.metalWhitelist` で対象を指定する。JAOPCA の素材名を大文字・小文字も含めて指定し、編集後は再起動する。合金や別名で検出された素材にも個別の許可が必要。空のリストでは全素材を無効にできる。

初期値は [同梱ホワイトリスト](../src/main/resources/ooen/materia/metal_whitelist.json) を参照。既存の設定ファイルを使う場合は、必要な素材名を手動で追記する。

Materia はホワイトリストにあり、OreDictionary に具体的なインゴットが登録された金属を使う。JAOPCA がある場合は、JAOPCA の金属判定と別名も使う。Forge がインゴット扱いするバニラのレンガは対象外。

同じ金属・形態の小片アイテム4個を 2×2 に並べると、Materia の鉱石砂利・鉱石砂・鉱石塵ブロックになる。ブロックは設置でき、かまどで対応するインゴット1個に精錬できる。テクスチャは砂利・砂・塵を金属ごとに着色する。小片は OreDictionary に対応するアイテム登録があれば再利用する。

`MetalCatalog.candidates("Iron", MetalForm.GRAVEL)` のように素材名と形態を指定して候補を取得できる。鉱石ブロックの場合は Materia のブロックが返る。ブロックは `ooenOreGravel`、`ooenOreSand`、`ooenOrePowder`、小片は `ooenSmallOreGravel`、`ooenSmallOreSand`、`ooenSmallOrePowder` を接頭辞に使う。

JAOPCA だけを追加して確認するには `./gradlew runClient25 -PooenProbe=jaopca-baseline` を使う。JAOPCA の JAR は [素材ライブラリの依存検証](dependency-tests.md) と同じ場所に置く。この構成から提供側を追加したときだけ、追加した金属がカタログに現れることを確認できる。

標準構成では鉄と金を扱える。銅などの追加金属は、導入 MOD が対応インゴットを登録した環境で利用できる。ネザライトは `ingotNetherite` と `NetheriteIngot` に対応する。既存のホワイトリストで使う場合は `Netherite` を追加する。

ドラコニウムはティア4で、環境に登録された `ingotDraconium` を使う。既存のホワイトリストには `Draconium` を追加する。覚醒ドラコニウムブロックは Transmutatio のるつぼの熱源として使える。
