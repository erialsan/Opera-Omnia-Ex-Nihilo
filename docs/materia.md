---
type: Module Guide
title: Materia の金属カタログ
description: JAOPCA を使った金属の検出、ホワイトリスト、加工形態の候補取得。
---
# Materia の金属カタログ

初回起動で生成される `config/ooen_materia.cfg` の `materials.metalWhitelist` で対象を指定する。JAOPCA の素材名を大文字・小文字も含めて指定し、編集後は再起動する。合金や別名で検出された素材にも個別の許可が必要。空のリストでは全素材を無効にできる。

初期値は `Iron`、`Gold`、`Copper`、`Tin`、`Aluminum`、`Aluminium`、`Lead`、`Nickel`、`Silver`、`Cobalt`、`Ardite`、`Osmium`、`Iridium`、`Tungsten`、`Boron`、`Uranium`、`Platinum`、`Manganese`、`Titanium`、`Mithril`、`Thorium`。既存の設定ファイルを使う場合は、必要な素材名を手動で追記する。

ロード完了後の `MetalCatalog.names()` は、ホワイトリストにあり、JAOPCA がインゴット系として認識し、OreDictionary に具体的なインゴットが登録された素材の名前を返す。Forge がインゴット扱いするバニラのレンガは対象外。`MetalCatalog.candidates("Iron", MetalForm.GRAVEL)` のように素材名と形態を指定すると、`ingot`、`ooenOreGravel`、`ooenOreSand`、`ooenOrePowder` の各接頭辞で登録済みアイテムを照合する。許可されていても環境に銅が無ければ、候補は空になる。Materia の照合は読み取り専用。

JAOPCA だけを追加して確認するには `./gradlew runClient25 -PooenProbe=jaopca-baseline` を使う。JAOPCA の JAR は [素材ライブラリの依存検証](dependency-tests.md) と同じ場所に置く。この構成から提供側を追加したときだけ、追加した金属がカタログに現れることを確認できる。

初期ホワイトリストでの実測では、JAOPCA 未導入時は `[]`、JAOPCA 単体では `[Gold, Iron]`、銅と青銅の検証用 MOD を追加した場合は `[Copper, Gold, Iron]` だった。
