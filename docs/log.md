# OOEN 更新履歴

## 2026-11-27

- README を OKF のプロジェクト概要に整理し、開発ガイド、プレイ手順、検証記録、クレジットを `docs/` に集約した。[ドキュメントの目次](index.md)
- 白い羊毛から糸のメッシュの格子を生成し、アイテムと篩の両方に適用した。木のクロークと蚕蛾の幼虫には Ex Nihilo: Creatio の PNG を採用した。[クレジット](credits.md)
- Fundamentum のクローク、幼虫、虫食いの葉、堆肥バレルと、Cribrum の篩、糸のメッシュを追加した。統合サーバーで木材と葉から小麦の種までの進行を確認した。[テストプレイ](playtest.md)
- Materia に JAOPCA の金属カタログとホワイトリストを追加した。鉄と銅で既存の加工形態の照合と、許可外の合金の除外を確認した。[金属カタログ](materia.md) · [依存検証](dependency-tests.md)

## 2026-11-26

- Materia を Kotlin に移行した。Forgelin と JvmDowngrader を設定し、Java 8 向けバイトコードを含む JAR をビルドした。[開発環境と起動](getting-started.md)
- 開発クライアントで鉄の基準値を採取し、JAOPCA で既存の鉱石砂利を再利用する試験を実施した。[依存検証](dependency-tests.md)

## 2026-11-25

- リポジトリを作成し、モジュール構成、素材設計、実装順を開発計画書にまとめた。[開発計画書 v0.2](development-plan.md)
