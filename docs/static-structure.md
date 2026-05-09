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
        - canvas : Canvas
        - currentTool : Tool
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
        --
        + draw (Graphics g) : void
        + move (int dx, int dy) : void
        + contains (int x, int y) : boolean
        + toSvg () : String
    }

    class FigureGroup {
        - children : List<Figure>
    }

    Figure <|-- FigureGroup
    FigureGroup *-- "0..*" Figure
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

abstract class Figure {
    # strokeColor : Color
    # fillColor : Color
    # strokeWidth : float
    --
    # applyStroke (Graphics g) : void
    # strokeAttrs () : String
    + draw (Graphics g) : void
    + move (int dx, int dy) : void
    + contains (int x, int y) : boolean
    + toSvg () : String
    + setStrokeWidth (float w) : void
}

class Line {
    - x1 : int
    - y1 : int
    - x2 : int
    - y2 : int
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
}

class Circle {
    - cx : int
    - cy : int
    - radius : int
    --
    + setRadius (int r) : void
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
}

class Polyline {
    - xs : List<Integer>
    - ys : List<Integer>
    --
    + addPoint (int x, int y) : void
    + setLastPoint (int x, int y) : void
    + removeLastPoint () : void
    + getPointCount () : int
}

class Polygon {
    - xs : List<Integer>
    - ys : List<Integer>
    --
    + addPoint (int x, int y) : void
    + setLastPoint (int x, int y) : void
    + removeLastPoint () : void
    + getPointCount () : int
}

class FigureGroup {
    - children : List<Figure>
    --
    + add (Figure f) : void
    + getChildren () : List<Figure>
}

Figure <|-- Line
Figure <|-- Rectangle
Figure <|-- Circle
Figure <|-- Ellipse
Figure <|-- Polyline
Figure <|-- Polygon
Figure <|-- FigureGroup
FigureGroup *-- "0..*" Figure : children

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
    - current : Polyline
}

class DrawPolygonTool {
    - current : Polygon
}

class SelectTool {
    - dragTarget : Figure
    - lastX : int
    - lastY : int
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
