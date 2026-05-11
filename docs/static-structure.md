# 静的構造

GUI アプリケーションのクラス構成を示す．

## クラス図 1: 全体構造

EditorFrame から Tool と Figure までの関係，および SVG 入出力クラスを示す．
各クラスの詳細は「クラス図 2」を参照．

```plantuml
@startuml static-structure-overview

package view {
    class EditorFrame {
        - canvas : Canvas
        --
        + EditorFrame ()
        - buildMenuBar () : JMenuBar
        - saveFile () : void
        - openFile () : void
    }

    class Canvas {
        - figures : List<Figure>
        - selected : Set<Figure>
        - preview : Figure
        --
        + addFigure (Figure f) : void
        + getFigures () : List<Figure>
        + setFigures (List<Figure> fs) : void
        + setPreview (Figure f) : void
        + figureAt (int x, int y) : Figure
        + select (Figure f, boolean add) : void
        + clearSelection () : void
        + getSelection () : Set<Figure>
        + groupSelected () : void
        + ungroupSelected () : void
        + paintComponent (Graphics g) : void
    }
}

package controller {
    class EditorController {
        - tool : Tool
        --
        + setTool (Tool t) : void
        + mousePressed (MouseEvent e) : void
        + mouseDragged (MouseEvent e) : void
        + mouseReleased (MouseEvent e) : void
        + mouseClicked (MouseEvent e) : void
        + mouseMoved (MouseEvent e) : void
    }

    interface Tool {
        + onPress (int x, int y) : void
        + onDrag (int x, int y) : void
        + onRelease (int x, int y) : void
        + onClick (int x, int y) : void
        + onDoubleClick (int x, int y) : void
        + onMove (int x, int y) : void
        + onPressEvent (MouseEvent e) : void
        + onDragEvent (MouseEvent e) : void
        + onReleaseEvent (MouseEvent e) : void
    }
}

package model {
    abstract class Figure {
        # strokeColor : Color
        # fillColor : Color
        # strokeWidth : float
        # transform : AffineTransform
        --
        + draw (Graphics g) : void
        + move (int dx, int dy) : void
        + moveInScreen (int dx, int dy) : void
        + contains (int x, int y) : boolean
        + getHandles () : List<Handle>
        + getBboxHandles () : List<Handle>
        + toSvg () : String
    }

    class Handle

    interface Rotatable {
        + rotate (theta : double, cx : double, cy : double) : void
    }

    class FigureGroup {
        - children : List<Figure>
    }

    Figure <|-- FigureGroup
    FigureGroup *-- "0..*" Figure
    Figure --> "0..*" Handle
    Rotatable <|.. Figure
}

package io {
    class SvgWriter {
        + {static} write (figures : List<Figure>, path : String, w : int, h : int) : void
    }

    class SvgReader {
        + {static} read (path : String) : List<Figure>
    }

    class SvgColor {
        + {static} parse (s : String) : Color
        + {static} toSvg (c : Color) : String
    }
}

EditorFrame *-- Canvas
EditorFrame *-- EditorController
EditorController --> Canvas
EditorController --> Tool
Canvas --> "0..*" Figure
EditorFrame ..> SvgWriter
EditorFrame ..> SvgReader
SvgWriter ..> Figure
SvgReader ..> Figure
SvgReader ..> SvgColor
SvgWriter ..> SvgColor

@enduml
```

## クラス図 2: 詳細

### Figure 階層

```plantuml
@startuml static-structure-model

interface Rotatable {
    + rotate (theta : double, cx : double, cy : double) : void
}

abstract class Figure {
    # strokeColor : Color
    # fillColor : Color
    # strokeWidth : float
    # transform : AffineTransform
    --
    # applyStroke (Graphics g) : void
    # strokeAttrs () : String
    # transformAttr () : String
    + draw (Graphics g) : void
    + move (int dx, int dy) : void
    + moveInScreen (int dx, int dy) : void
    + contains (int x, int y) : boolean
    + getHandles () : List<Handle>
    + getBboxHandles () : List<Handle>
    + bakeTransform () : Figure
    + toSvg () : String
    + setStrokeWidth (float w) : void
}

class Handle {
    - cx : int
    - cy : int
    - type : Type
    --
    + contains (int x, int y) : boolean
    + drag (int x, int y) : void
}

enum "Handle.Type" as HandleType {
    ENDPOINT
    SCALE_NW
    SCALE_NE
    SCALE_SE
    SCALE_SW
    ROTATE
}

class Line {
    - x1 : double
    - y1 : double
    - x2 : double
    - y2 : double
    --
    + setEndPoint (int x2, int y2) : void
}

class Rectangle {
    - x1 : int
    - y1 : int
    - x2 : int
    - y2 : int
    - rx : int
    - ry : int
    --
    + setEndCorner (int x2, int y2) : void
    + setRoundedCorners (int rx, int ry) : void
    + rotate (theta : double, cx : double, cy : double) : void
}

class Circle {
    - cx : int
    - cy : int
    - radius : int
    --
    + setRadius (int r) : void
    + bakeTransform () : Figure
}

class Ellipse {
    - x1 : int
    - y1 : int
    - x2 : int
    - y2 : int
    --
    + setEndCorner (int x2, int y2) : void
    + {static} fromCenter (cx, cy, rx, ry, ...) : Ellipse
    + {static} fromCenter (cx, cy, r, ...) : Ellipse
    + rotate (theta : double, cx : double, cy : double) : void
}

class Polyline {
    - xs : List<Integer>
    - ys : List<Integer>
    --
    + addPoint (int x, int y) : void
    + setLastPoint (int x, int y) : void
    + removeLastPoint () : void
    + getPointCount () : int
    + rotate (theta : double, cx : double, cy : double) : void
    + bakeTransform () : Figure
}

class Polygon {
    - xs : List<Integer>
    - ys : List<Integer>
    --
    + addPoint (int x, int y) : void
    + setLastPoint (int x, int y) : void
    + removeLastPoint () : void
    + getPointCount () : int
    + rotate (theta : double, cx : double, cy : double) : void
    + bakeTransform () : Figure
}

class FigureGroup {
    - children : List<Figure>
    --
    + add (Figure f) : void
    + getChildren () : List<Figure>
    + scale (sx : double, sy : double, ox : double, oy : double) : void
    + rotate (theta : double, cx : double, cy : double) : void
}

Figure <|-- Line
Figure <|-- Rectangle
Figure <|-- Circle
Figure <|-- Ellipse
Figure <|-- Polyline
Figure <|-- Polygon
Figure <|-- FigureGroup
FigureGroup *-- "0..*" Figure : children

Rotatable <|.. Rectangle
Rotatable <|.. Ellipse
Rotatable <|.. Polyline
Rotatable <|.. Polygon
Rotatable <|.. FigureGroup

Figure --> "0..*" Handle
Handle --> HandleType

@enduml
```

### Tool 階層

```plantuml
@startuml static-structure-tools

interface Tool {
    + onPress (int x, int y) : void
    + onDrag (int x, int y) : void
    + onRelease (int x, int y) : void
    + onClick (int x, int y) : void
    + onDoubleClick (int x, int y) : void
    + onMove (int x, int y) : void
}

class DrawLineTool {
    - startX : int
    - startY : int
}

class DrawRectTool {
    - startX : int
    - startY : int
}

class DrawCircleTool {
    - startX : int
    - startY : int
}

class DrawEllipseTool {
    - startX : int
    - startY : int
}

class DrawPolylineTool {
    - preview : Polyline
}

class DrawPolygonTool {
    - preview : Polygon
}

class SelectTool {
    - dragTarget : Figure
    - activeHandle : Handle
    - activeHandleFigure : Figure
    - lastX : int
    - lastY : int
    - rotateCenterX : double
    - rotateCenterY : double
    - scaleFixedX : double
    - scaleFixedY : double
}

Tool <|.. DrawLineTool
Tool <|.. DrawRectTool
Tool <|.. DrawCircleTool
Tool <|.. DrawEllipseTool
Tool <|.. DrawPolylineTool
Tool <|.. DrawPolygonTool
Tool <|.. SelectTool

@enduml
```

## パッケージ構成

| パッケージ | 役割 |
|---|---|
| `model` | 図形クラスの階層 |
| `view` | Swing コンポーネント (フレーム・キャンバス) |
| `controller` | マウスイベント処理とツール抽象化 |
| `io` | SVG ファイルの読み書き |

## 補足

- `Canvas` は `JPanel` のサブクラスとして実装し，`paintComponent` をオーバーライドして図形を描画する
- `Tool` インタフェースの `onClick`，`onDoubleClick`，`onMove` は `default` メソッドとして空実装を持つ
- `EditorController` は `MouseAdapter` を継承して実装する
- ドラッグ系ツール (`DrawLineTool` 等) は `onPress`/`onDrag`/`onRelease` を使用し，クリック系ツール (`DrawPolylineTool` 等) は `onClick`/`onDoubleClick`/`onMove` を使用する
- `Figure` の `strokeWidth` は `Graphics2D.setStroke(new BasicStroke(...))` で適用し，可変太さの線描画を実現する
- `Rectangle` の `rx`/`ry` は SVG の角丸属性に対応し，`drawRoundRect`/`fillRoundRect` で描画する
- `SvgReader` は `<g>` 要素の `fill`/`stroke`/`stroke-width` と `transform="translate(...)"` を子要素に継承するスタイルコンテキストを持つ
- `FigureGroup` は Composite パターンを適用し，`draw`/`move`/`contains`/`toSvg` を子要素に委譲する．SVG の `<g>` 要素として入出力される
- `SelectTool` は `onPressEvent(MouseEvent)` をオーバーライドして `e.isControlDown()` で Ctrl+クリックによる複数選択を実現する．ドラッグ時は選択図形全体を移動する
- `Figure` は `AffineTransform transform` フィールドを持ち，`draw` 時に座標変換を適用する (テンプレートメソッドパターン)．`moveInScreen` はスクリーン空間での平行移動を transform に合成する
- `Figure.bakeTransform()` はグループ解除時に子図形の transform をローカル座標に焼き込む具象メソッドで，デフォルト実装は何もしない (null を返す)．Line, Polyline, Polygon はオーバーライドして座標を更新し transform をリセットする．`Circle.bakeTransform()` は非均一スケール時に `Ellipse` を返して図形を置き換える
- `Rotatable` インタフェースは `rotate()` を定義する．`FigureGroup`，`Rectangle`，`Ellipse` が実装し，回転ハンドルのドラッグで `SelectTool` から呼び出される
- `Handle` はハンドルの画面座標・種別・ドラッグコールバックを持つ値オブジェクト．`Figure.getHandles()` は端点ハンドル，`Figure.getBboxHandles()` は SCALE / ROTATE ハンドルを返す
