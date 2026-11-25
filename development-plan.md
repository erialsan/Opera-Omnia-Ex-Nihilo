---
type: Project Plan
title: Opera Omnia Ex Nihilo 開発計画書 v0.2
description: OOEN の確定事項、現行のモジュール構成案、素材設計の未決事項、実装順をまとめる。
status: draft
x-ooen-plan-version: "0.2"
sources:
  - id: discussion
    resource: "chatgpt-conversation://6b0438a8-b918-83ee-a9cd-1bd97d04ead7"
    title: Exnihilo内部刷新比較
  - id: materialslib
    resource: "https://github.com/GTNewHorizons/MaterialsLib"
    title: GTNewHorizons MaterialsLib
  - id: jaopca
    resource: "https://www.curseforge.com/minecraft/mc-mods/jaopca"
    title: JAOPCA 公式配布ページ
  - id: okf
    resource: "https://github.com/open-knowledge-format/open-knowledge-format/blob/main/SPEC.md"
    title: Open Knowledge Format v0.3 仕様
---
# Opera Omnia Ex Nihilo 開発計画書 v0.2

2026-11-25 · レビュー用

Ex Nihilo 系の SkyBlock 向け資源獲得・加工をモジュール単位で刷新する。最初は、篩で得た素材を粉砕・加工して使えるところまでを一つの動作経路として検証する。

## 確定事項

- 正式名称は **Opera Omnia Ex Nihilo**。
- 外部 MOD 連携モジュールの名称は **Nexus**。候補の Concordia から変更する。[^discussion]

## 現行のモジュール構成案

参照チャットの最終一覧を作業上の基準とする。略称は **OOEN**、個別表示名は `Opera Omnia Ex Nihilo: Nexus` のように統一する案。表の MOD ID と担当は実装に向けた設計案で、各 MOD の単独導入可否や配布単位は依存関係を検証して決める。[^discussion]

| モジュール | MOD ID 案 | 担当 |
| --- | --- | --- |
| Fundamentum | `ooen_fundamentum` | 最小限の共通基盤。複数モジュールで実際に共有する処理を置く |
| Materia | `ooen_materia` | 素材の対応付け、鉱石加工形態、生成対象、グレースケールのテクスチャ原型 |
| Cribrum | `ooen_cribrum` | 篩、メッシュ、メッシュ別ドロップと篩のエンチャント |
| Malleus | `ooen_malleus` | ハンマー、Ore Smasher、粉砕と Fortune による増産 |
| Transmutatio | `ooen_transmutatio` | 樽、坩堝、液体・物質変換、ウィッチウォーター |
| Industria | `ooen_industria` | 歯車式自動化、電動設備、電動ツール |
| Compressio | `ooen_compressio` | 圧縮ハンマー、圧縮篩 |
| Vita | `ooen_vita` | 動物の餌、人形による召喚、Mob 変換、じょうろ |
| Genesis | `ooen_genesis` | SkyBlock ワールド生成 |
| Nexus | `ooen_nexus` | 外部 MOD 連携。GregTech 固有の処理の置き場 |

## 素材設計の有力案

GTNH 側の正式表記は **MaterialsLib**。同プロジェクトは素材関連処理を扱う。JAOPCA はアイテムとレシピの動的追加を目的とし、作者は Minecraft 1.7.10 向けの W.0 系を実験的な AOBD 代替と位置づけている。OOEN から使える API、外部からの登録、生成対象の制御、両者の併用は採用前に検証する。[^materialslib][^jaopca]

| 責務 | 担当案 |
| --- | --- |
| 素材 ID と属性の基準 | MaterialsLib。対象環境で外部 MOD から参照・登録できることを確認する |
| OreDictionary からの検出、アイテム・レシピ生成 | JAOPCA。鉱石砂利・鉱石砂・鉱石粉を OOEN 用に登録できるか試す |
| 素材の照合と生成方針 | Materia。素材 ID と OreDictionary／JAOPCA 側の識別子を対応付け、加工形態、主要素材の個別定義、生成対象、表示用の原型を管理する |
| 遊び方と連携ルール | 篩のドロップは Cribrum、粉砕と増産は Malleus、GregTech 固有処理は Nexus が持つ |

同一素材を二重管理しないよう、MaterialsLib を使う場合はその素材 ID を基準に照合する。加工形態ごとのアイテムとレシピは、既存の登録を再利用するか、OOEN 用に生成するかを Materia で判断し、登録元を一つにする。[^discussion]

## 未決事項と判断条件

| 未決事項 | 決めるために確認すること |
| --- | --- |
| 対象環境 | 会話で想定された Minecraft 1.7.10 を起点に、Forge、GTNH、Java、Gradle と両ライブラリの対象版を固定する。GTNH なしでの導入をサポートするか決める |
| MaterialsLib の採用 | 外部素材の登録・取得 API、ID の安定性、初期化順、GTNH／GregTech 依存を確認する。形式の参考に留めるか、実行時依存にするか決める |
| JAOPCA の採用 | 対象版で OOEN 独自の加工形態、生成の許可・除外、既存アイテムの再利用、レシピ登録の制御を実証する。遅れて登録される OreDictionary 項目の扱いも確認する |
| 併用と代替 | 別名、同一素材を提供する複数 MOD、未対応素材の扱いを決める。連携が難しい場合は、対象素材を絞った Materia の直接登録を比較する |
| モジュール境界と配布 | Fundamentum の共有範囲、Materia と Cribrum・Malleus の必須依存、Compressio と Industria の依存、Nexus の連携先別の読み込み条件を確定する。個別 JAR と一括配布の範囲も決める |
| 既存環境との互換性 | 継承対象の Ex Nihilo 系実装と機能一覧を照合し、既存ワールド、アイテム ID、設定、レシピ、MOD パックのスクリプトの移行対象を決める |
| ゲームバランスと連携先 | メッシュ別ドロップ率、Fortune、圧縮・自動化の歩留まりを定める。Nexus の初期候補として GregTech、Tinkers’ Construct、AE2、Thermal、Et Futurum の優先順位と対応版を決める |

## 実装順の提案

1. **依存を実証する。** 対象版を仮固定し、代表素材一種で素材 ID の取得・登録と、JAOPCA による検出から鉱石砂利・砂・粉とレシピの登録までを試す。既存品があるケース、外部 MOD が素材を追加するケース、遅延登録を確認する。結果をもとにライブラリの採否と必須／任意依存を決める。
2. **手動の資源獲得を通す。** Fundamentum、Materia、Cribrum、Malleus で、篩のドロップから粉砕を経て利用可能な素材を得るところまでを実装する。クライアントと専用サーバーで動作し、同一素材のアイテム・レシピが重複登録されず、再起動後も既存アイテムが読めることを確認する。
3. **処理系を拡張する。** Transmutatio の変換を追加し、Compressio と Industria が手動処理と同じ素材・レシピ定義を参照できるようにする。圧縮や機械化での歩留まりは個別に検証する。
4. **任意機能と外部連携を追加する。** Vita、Genesis、Nexus を機能単位で追加する。対応先 MOD の有無で起動と登録を確認し、決定した移行範囲に沿って配布前の互換性を検証する。

## 更新範囲と文書形式

前回の計画書本文は参照できなかったため、確認できた会話の命名一覧と素材設計の議論から v0.2 を再構成した。実装順、完了条件、依存が成立しない場合の代替は今回の提案。前回版が入手できた段階で、継承対象の機能と互換性要件を照合する。

計画書の版は **v0.2**、バンドルの形式は **OKF v0.3**。[バンドルの目次](index.md) の YAML に形式の版を記し、計画書は `type` を持つ UTF-8 Markdown のコンセプト文書とした。[^okf]

[^discussion]: [Exnihilo内部刷新比較](chatgpt-conversation://6b0438a8-b918-83ee-a9cd-1bd97d04ead7)
[^materialslib]: [GTNewHorizons/MaterialsLib](https://github.com/GTNewHorizons/MaterialsLib)
[^jaopca]: [JAOPCA 公式配布ページ](https://www.curseforge.com/minecraft/mc-mods/jaopca)
[^okf]: [OKF v0.3 仕様](https://github.com/open-knowledge-format/open-knowledge-format/blob/main/SPEC.md)
