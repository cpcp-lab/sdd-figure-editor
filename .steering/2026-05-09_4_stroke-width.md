# 描画パスの太さ・色変更機能

## 要求事項

- ツールバーにドロップダウンメニューを追加し，線幅を 3 種から選択できるようにする．
- 描画時は選択中の線幅で図形を作成する．
- 図形選択中に線幅を変更すると，選択中の図形の線幅がその値に更新される．
- ツールバーにストローク色・塗り色のドロップダウンメニューを追加し，複数色から選択できるようにする．
- 描画時は選択中の色で図形を作成する．
- 図形選択中に色を変更すると，選択中の図形の色がその値に更新される．

## 方針

### 線幅

- `Canvas` に `currentStrokeWidth` フィールドを追加し，ゲッタ・セッタを提供する．
  - セッタ内で選択図形がある場合はそれらの `strokeWidth` を更新する処理を担う `setStrokeWidthForSelected` メソッドを別途追加する．
- 各描画ツール (`DrawLineTool` など) は図形作成時に `canvas.getCurrentStrokeWidth()` を取得し `setStrokeWidth()` を呼ぶ．
- `EditorFrame` のツールバーに `JComboBox<String>` で「Thin (1px)」「Normal (3px)」「Thick (6px)」を追加する．
  - 選択時に `canvas.setCurrentStrokeWidth()` を呼び，さらに選択中の図形に適用する．

### 色

- `Figure` に `strokeColor`，`fillColor` フィールドを追加し，ゲッタ・セッタを提供する．
- `Canvas` に `currentStrokeColor` (初期値: `Color.BLACK`)，`currentFillColor` (初期値: `null`) フィールドを追加し，ゲッタ・セッタを提供する．
  - 各セッタは選択中の図形にも即座に適用する．
- 各描画ツールは図形作成時に `canvas.getCurrentStrokeColor()` / `canvas.getCurrentFillColor()` を渡す．
- `EditorFrame` のツールバーに，ストローク色・塗り色それぞれ `JComboBox<Integer>` (色見本アイコン付き) を追加する．
  - 色見本は 16x16 の塗りつぶし矩形アイコンで表示する．塗り色には「塗りなし」選択肢も設ける．

## タスク

- [x] ステアリング文書を作成
- [x] `Canvas` に `currentStrokeWidth` フィールドと関連メソッドを追加
- [x] 全描画ツールで `canvas.getCurrentStrokeWidth()` を使用
- [x] `EditorFrame` に線幅ドロップダウンを追加
- [x] `Figure` に `strokeColor`，`fillColor` フィールドを追加
- [x] `Canvas` に `currentStrokeColor`，`currentFillColor` フィールドと関連メソッドを追加
- [x] 全描画ツールで `canvas.getCurrentStrokeColor()` / `getCurrentFillColor()` を使用
- [x] `EditorFrame` にストローク色・塗り色ドロップダウンを追加 (色見本アイコン付き)
- [x] ビルド・テストで確認
