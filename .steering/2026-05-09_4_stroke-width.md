# ワークパッケージ 4: 描画パスの太さ変更機能

## 要求事項

- ツールバーにドロップダウンメニューを追加し，線幅を 3 種から選択できるようにする．
- 描画時は選択中の線幅で図形を作成する．
- 図形選択中に線幅を変更すると，選択中の図形の線幅がその値に更新される．

## 方針

- `Canvas` に `currentStrokeWidth` フィールドを追加し，ゲッタ・セッタを提供する．
  - セッタ内で選択図形がある場合はそれらの `strokeWidth` を更新する処理を担う `setStrokeWidthForSelected` メソッドを別途追加する．
- 各描画ツール (`DrawLineTool` など) は図形作成時に `canvas.getCurrentStrokeWidth()` を取得し `setStrokeWidth()` を呼ぶ．
- `EditorFrame` のツールバーに `JComboBox<String>` で「Thin (1px)」「Normal (3px)」「Thick (6px)」を追加する．
  - 選択時に `canvas.setCurrentStrokeWidth()` を呼び，さらに選択中の図形に適用する．

## タスク

- [x] ステアリング文書を作成
- [x] `Canvas` に `currentStrokeWidth` フィールドと関連メソッドを追加
- [x] 全描画ツールで `canvas.getCurrentStrokeWidth()` を使用
- [x] `EditorFrame` に線幅ドロップダウンを追加
- [x] ビルド・テストで確認
