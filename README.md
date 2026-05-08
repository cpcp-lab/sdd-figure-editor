# Figure Editor

Java/Swing で実装した図形エディタ．
キャンバス上でマウスをドラッグして線分を描画できる．

## 想定環境

| 項目 | バージョン |
|---|---|
| Java | 21 (OpenJDK Temurin 21 推奨) |
| Maven | 3.x |
| OS | macOS / Linux / Windows |

## ビルドと実行

```bash
# ビルド
mvn compile

# 実行
mvn exec:java

# テスト
mvn test

# 特定テストのみ
mvn test -Dtest=LineTest#containsPointOnLine
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
