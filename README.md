# Figure Editor

Java/Swing で実装した図形エディタ．
キャンバス上でマウスをドラッグして線分を描画できる．

本プロジェクトは仕様駆動開発 (Specification-Driven Development) アプローチで実施している．
詳細な仕様は `docs/` 以下の設計文書を参照のこと．

## ライセンス

[MIT License](LICENSE)

## 想定環境

| 項目 | バージョン |
|---|---|
| Java | 21 (OpenJDK Temurin 21 推奨) |
| Maven | 3.x |
| OS | macOS / Linux / Windows |

## テストスイートの準備

一部のテストは W3C SVG 1.1 Test Suite の SVG ファイルおよび参照 PNG を使用する．
テスト実行前に以下の手順でダウンロード・展開しておく必要がある．

```bash
# プロジェクトルートで実行
curl -O https://www.w3.org/Graphics/SVG/Test/20110816/archives/W3C_SVG_11_TestSuite.tar.gz
tar xzf W3C_SVG_11_TestSuite.tar.gz
```

展開後，プロジェクトルートに `W3C_SVG_11_TestSuite/` ディレクトリが作成される．

## ビルドと実行

```bash
# ビルド
mvn compile

# 実行
mvn exec:java

# テスト (全件)
mvn test

# 特定テストのみ
mvn test -Dtest=LineTest#containsPointOnLine

# 目視確認テスト (ウィンドウ表示, W3C テストスイート必要)
mvn test -Dgroups=visual

# 目視確認テスト・自動比較モード (W3C テストスイート必要)
mvn test -Dgroups=visual -Dvisual.auto=true
```

## 使い方

1. `mvn exec:java` でウィンドウを起動する
2. キャンバス上でマウスボタンを押したままドラッグすると線分が描かれる
3. ボタンを離すと線分が確定する
4. 連続して複数の線分を描ける

## プロジェクト構成

```
src/
  main/java/       メインソース (デフォルトパッケージ)
    Figure.java    図形の抽象クラス
    Line.java
    Canvas.java    描画キャンバス (JPanel サブクラス)
    EditorFrame.java  メインウィンドウ (JFrame サブクラス)
    Tool.java      ツールインタフェース
    DrawLineTool.java
    EditorController.java  マウスイベント処理
  test/java/       テストソース (デフォルトパッケージ)
    LineTest.java
docs/              設計文書
  requirements.md
  domain.md
  static-structure.md
```
