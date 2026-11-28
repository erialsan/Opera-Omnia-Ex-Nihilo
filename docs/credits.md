---
type: Credits
title: クレジットとライセンス
description: 同梱テクスチャと参照した実装の出所、クレジット、適用ライセンス。
---
# クレジットとライセンス

## プロジェクト

リポジトリのライセンスは GNU LGPL v3。本文は [LICENSE](../LICENSE) を参照。

## 篩の操作

手動で振るう間隔、メッシュの効率強化による進捗補正、5×5 の篩への素材配布と連鎖処理は **Ex Nihilo: Creatio** の挙動を参照し、Kotlin で実装した。参照元は MIT ライセンスで、著作権表示とライセンス本文を [ExNihiloCreatio-MIT.txt](../src/main/resources/META-INF/licenses/ExNihiloCreatio-MIT.txt) として JAR に同梱する。

- [Creatio の篩の操作と連鎖処理](https://github.com/BloodyMods/ExNihiloCreatio/blob/1.12/src/main/java/exnihilocreatio/blocks/BlockSieve.java)
- [Creatio の篩のクールダウンと進捗処理](https://github.com/BloodyMods/ExNihiloCreatio/blob/1.12/src/main/java/exnihilocreatio/tiles/TileSieve.java)

## テクスチャ

木のクロークと蚕蛾の幼虫の PNG は **Ex Nihilo: Creatio** から画像を変更せずに収録した。上流のライセンスに記載されたテクスチャのクレジットは **© 2019 Wolforce**。MIT ライセンスと上流の著作権表示の全文を [ExNihiloCreatio-MIT.txt](../src/main/resources/META-INF/licenses/ExNihiloCreatio-MIT.txt) として JAR に同梱する。

- 木のクローク → [Creatio の `crook_wood.png`](https://github.com/BloodyMods/ExNihiloCreatio/blob/75f60692a4257a62ba9f6831866e456d8526ec9d/src/main/resources/assets/exnihilocreatio/textures/items/tools/crook/crook_wood.png)
- 蚕蛾の幼虫 → [Creatio の `silkworm.png`](https://github.com/BloodyMods/ExNihiloCreatio/blob/75f60692a4257a62ba9f6831866e456d8526ec9d/src/main/resources/assets/exnihilocreatio/textures/items/silkworm.png)

糸のメッシュはクライアントで白い羊毛のテクスチャから格子を生成する。生成した格子を、アイテムアイコンと篩に張った状態の上面に使う。

木製バレルは Creatio の木製バレルと同じく、木材に対応するバニラの板材テクスチャを使う。[Creatio の木製バレルモデル](https://github.com/BloodyMods/ExNihiloCreatio/blob/75f60692a4257a62ba9f6831866e456d8526ec9d/src/main/resources/assets/exnihilocreatio/models/block/block_barrel0.json)
