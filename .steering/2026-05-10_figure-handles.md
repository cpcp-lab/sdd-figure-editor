# 図形ハンドルと Shift 拘束線分

## 要求事項

- 選択中の各図形オブジェクト上にハンドルを表示する．
- ハンドル上でマウス操作すると当該図形の属性 (形状・サイズ・位置など) を変更できる．
- Shift キーを押しながら Line を作成すると，水平・垂直・45° に拘束された線分を作成できる．
- `SvgReader` の transform 対応を汎用 AffineTransform に拡張し，
  W3C SVG 1.1 テストスイートの coords-trans-*.svg の読み込みに対応する．

## 方針

### ハンドル表示

- `Figure` に抽象メソッド `getHandles()` を追加し，`Handle` オブジェクトのリストを返す．
  - `Handle` は位置 (cx, cy) とドラッグ時に図形属性を更新するコールバックを持つ値オブジェクトとする．
  - 各図形クラス (`Line`, `Rectangle`, `Ellipse`, `Circle` など) が自身の制御点に対応した `Handle` を返す．
    - `Line`: 両端点 (x1, y1), (x2, y2)
    - `Rectangle` / `Ellipse`: 四隅 (x1, y1), (x2, y2)
    - `Circle`: 中心 (cx, cy) と半径端点
- `Canvas.paintComponent` で選択中の図形の `getHandles()` を取得し，各ハンドルを小さな正方形 (8x8) として描画する．

### ハンドルによる属性変更

- `SelectTool.onPressEvent` で，選択図形のハンドル一覧を走査し，クリック座標がハンドルに重なる場合はそのハンドルをアクティブハンドルとして記録する．
- `SelectTool.onDrag` でアクティブハンドルがある場合，ハンドルのコールバックにドラッグ先座標を渡して図形属性を更新し `repaint()` する．
- ハンドル操作と図形移動 (既存) は排他的に動作する．

### アフィン変換による座標管理

- `Figure` に `AffineTransform transform` フィールドを追加する (初期値: 単位変換)．
- `draw(Graphics g)` では `Graphics2D.transform(transform)` を適用してから図形を描画する．
- `contains(int x, int y)` では `transform.inverseTransform()` でヒット座標を変換してから判定する．
- SVG 出力では変換が単位変換でない場合に `transform="matrix(a,b,c,d,e,f)"` 属性を付加する．
  - `AffineTransform.getMatrix(double[])` で 6 要素を取り出して文字列化する．
- `scale` / `rotate` メソッドは `Figure` の `transform` に変換を合成する形で実装する (座標フィールドは書き換えない)．
  - `Figure.scale(double sx, double sy, double originX, double originY)`
  - `Figure.rotate(double theta, double cx, double cy)`
  - いずれも `AffineTransform` を生成して `transform.preConcatenate()` で合成する．
- `move(int dx, int dy)` も `transform` への平行移動合成に統一する (各サブクラスの座標フィールドを直接変更する既存実装は維持しつつ，グループ変換と整合させる)．

### FigureGroup のスケーリング・回転ハンドル

- `FigureGroup` は `getHandles()` で通常の端点ハンドルではなく，グループ専用のハンドルセットを返す．
- **バウンディングボックス**: 子図形全体を包む最小矩形 (以下 bbox) を `FigureGroup.getBounds()` で計算する (各子図形の `transform` 適用後の座標を考慮する)．
- **表示**:
  - bbox の周囲を破線 (`BasicStroke` の `DASHES` パターン) で描画する．
  - 四隅 (左上・右上・右下・左下) に 8×8 の小さい正方形ハンドルを描画する．
  - 上辺中央の少し外側に円形の回転ハンドルを描画する (スケールハンドルと視覚的に区別する)．
- **スケーリング操作**:
  - 四隅ハンドルをドラッグすると，ドラッグ前後の bbox サイズ比に基づき `FigureGroup.transform` にスケール変換を合成する．
  - スケーリングは bbox の対角点を固定点として行う．
- **回転操作**:
  - 回転ハンドルをドラッグすると，bbox 中心を回転軸として `FigureGroup.transform` に回転変換を合成する．
  - 回転角は，bbox 中心からドラッグ前後の角度差で決める．
  - Shift を押しながら回転すると 15° 刻みにスナップする．

### SvgReader transform 拡張

- Figure に AffineTransform transform フィールドを追加したことで，SvgReader も汎用 AffineTransform
  に対応できるよう拡張する．
- `SvgReader.parseTransform()` を translate / scale / rotate / skewX / skewY / matrix
  の連結に対応させ，AffineTransform.concatenate() で左→右 (外→内) に合成する．
- 縮退図形 (r=0 の circle，rx/ry=0 の ellipse，width/height=0 の rect) は SVG 仕様に従い描画しない．
- `<path>` 要素の M/L/Z のみで構成される閉じたパスを Polygon に変換する
  (カーブ命令を含む場合はスキップ)．
- 目視確認と自動比較の共通コードを `W3CImageComparator` に集約し，
  `-Dvisual.auto=true` の切り替えを各テストクラスで統一する．

### Shift 拘束 (Line)

- `DrawLineTool.onDrag` (または `onDragEvent`) で `MouseEvent.isShiftDown()` を確認する．
- Shift が押されている場合，始点からの dx, dy の絶対値を比較し，以下のいずれかに終点をスナップする:
  - `|dx| < |dy|` → 垂直線 (x2 = x1)
  - `|dy| < |dx|` → 水平線 (y2 = y1)
  - 差が小さい場合 → 45° 斜線 (|dx| = |dy|，符号は元の方向に合わせる)
- `DrawLineTool` が `onDrag(int x, int y)` ではなく `onDragEvent(MouseEvent e)` をオーバーライドするよう変更し，`e.isShiftDown()` を参照できるようにする．

## タスク

- [x] ステアリング文書を作成
- [x] `Handle` クラスを新規作成
- [x] `Figure` に `getHandles()` 抽象メソッドを追加
- [x] 各図形クラス (`Line`, `Rectangle`, `Ellipse`, `Circle`, `Polyline`, `Polygon`) に `getHandles()` を実装
- [x] `Figure` に `AffineTransform transform` フィールドを追加
- [x] `Figure.draw()` で `transform` を適用するよう各サブクラスを修正 (テンプレートメソッドパターン)
- [x] `Figure.contains()` で逆変換によるヒットテストを実装
- [x] `Figure.scale()` / `Figure.rotate()` を `transform` への合成として実装
- [x] SVG 出力で `transform="matrix(...)"` 属性を付加
- [x] `FigureGroup` に `boundsLocal()` を追加
- [x] `FigureGroup.getHandles()` でバウンディングボックス破線・四隅スケールハンドル・回転ハンドルを返す実装
- [x] `SelectTool` でスケール・回転ハンドルのドラッグ処理を実装 (Shift で 15° スナップ)
- [x] `Canvas.paintComponent` でハンドルを描画
- [x] `SelectTool` でハンドルのヒットテストとドラッグ処理を実装
- [x] `DrawLineTool` に Shift 拘束を実装
- [x] ビルド・テストで確認 (既存テスト全通過; W3C 失敗は変更前から存在)
- [x] `SvgReader.parseTransform()` を汎用 AffineTransform 対応に拡張
- [x] 縮退図形の除外処理を追加
- [x] `<path>` 要素の M/L/Z-only 閉パス → Polygon 変換を実装
- [x] `W3CImageComparator` に共通ユーティリティを集約
- [x] `W3CCoordsTransSvgTest` を新設 (coords-trans-01-b〜14-f，閾値 10%)
- [x] `W3CCoordsTransTest` を新設 (Java コード直接構築，閾値 10%)
- [x] 全テスト通過を確認
