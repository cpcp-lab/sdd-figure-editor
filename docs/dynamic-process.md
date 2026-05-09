# 動的過程

## Canvas の状態機械図 — Line を 1 本描く場合

DrawLineTool を使ってマウス操作で Line を 1 本描く際の Canvas の状態遷移を示す．
Canvas の状態は `figures` リストと `preview` フィールドの組み合わせで定まる．

```plantuml
@startuml dynamic-process-canvas-draw-line

state "白紙" as Empty
state "プレビュー表示中" as Previewing
state "ドラッグ中" as Dragging
state "図形確定済み" as Committed

[*]        --> Empty     : Canvas 生成
Empty      --> Previewing : mousePressed\n/ preview ← new Line(x,y,x,y) ; repaint()
Previewing --> Dragging   : mouseDragged\n/ preview.setEndPoint(x,y) ; repaint()
Dragging   --> Dragging   : mouseDragged\n/ preview.setEndPoint(x,y) ; repaint()
Dragging   --> Committed  : mouseReleased\n/ figures.add(preview) ; preview ← null ; repaint()
Committed  --> Previewing : mousePressed\n/ preview ← new Line(x,y,x,y) ; repaint()

@enduml
```

遷移のトリガーは `EditorController` がマウスイベントを受けて `DrawLineTool` の各メソッドを呼び出し，
`DrawLineTool` が `Canvas` の `setPreview()`・`addFigure()`・`repaint()` を呼び出すことで生じる．

## W3CShapesTest.testPolylineVisual()

`testPolylineVisual()` は Polyline 図形を Canvas に追加し，参照 PNG と比較して描画の正しさを確認するテストメソッドである．
自動比較モード (`-Dvisual.auto=true`) と目視確認モード (デフォルト) の 2 つの実行経路を持つ．


```plantuml
@startuml dynamic-process-polyline-interactive

actor       JUnit
actor       User
participant W3CShapesTest  as Test
participant Canvas
participant "Polyline\n(pl1..pl6)" as PL
participant JFrame
participant JOptionPane

JUnit -> Test : testPolylineVisual()

Test -> Canvas : new Canvas()
loop pl1 〜 pl6
    Test -> PL    : new Polyline(strokeColor [, fillColor])
    Test -> PL    : addPoint(x, y) × n
    Test -> Canvas : addFigure(pl)
end

Test -> Test : showAndConfirm("Polyline", canvas, pngPath)
Test -> Test : interactiveConfirm(title, canvas, pngPath)

Test -> JFrame : new JFrame("Visual Test: Polyline")
Test -> JFrame : add(canvas + refLabel 並列表示)
Test -> JFrame : setVisible(true)

Test -> JOptionPane : showConfirmDialog(...)
JOptionPane -> User : ウィンドウ表示\n「正しく描画されていますか？」

alt YES 選択
    User -> JOptionPane : YES
    JOptionPane --> Test : YES_OPTION
    Test -> JFrame : dispose()
    Test -> JUnit  : (テスト成功)
else NO 選択
    User -> JOptionPane : NO
    JOptionPane --> Test : NO_OPTION
    Test -> JFrame : dispose()
    Test -> JUnit  : AssertionError (テスト失敗)
end

@enduml
```
