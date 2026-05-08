# GUI 上での線分描画

## 要求事項

- Swing ウィンドウ (JFrame) を起動できる
- キャンバス上でマウスをドラッグすると線分が描かれる
- 描いた線分はウィンドウを再描画しても消えない
- 連続して複数の線分を描ける

## 方針

static-structure.md のクラス構成を基本とし，最小限のクラスで実装する．

- パッケージ構成: `model` / `view` / `controller`
- `Figure` (抽象クラス) と `Line` を `model` に実装
- `Canvas` (JPanel サブクラス) と `EditorFrame` (JFrame サブクラス) を `view` に実装
- `EditorController` (MouseListener/MouseMotionListener 実装) と `DrawLineTool` を `controller` に実装
- `Line` および `Figure` 継承関係のユニットテストを作成

## タスク

- [x] Maven プロジェクト構成 (pom.xml) を作成
- [x] `Figure` 抽象クラスを実装 (`draw`, `move`, `contains`)
- [x] `Line` クラスを実装
- [x] `Canvas` クラスを実装 (図形リスト管理・再描画)
- [x] `DrawLineTool` クラスを実装 (マウス操作 → Line 生成)
- [x] `EditorController` クラスを実装 (マウスイベントを Tool に委譲)
- [x] `EditorFrame` クラスを実装 (Canvas・Controller の組み立て)
- [x] ユニットテストを作成・実施 (8 件すべてパス)
- [x] 目視による動作確認
