# 静的構造

GUI アプリケーションのクラス構成を示す．

## クラス図 1: 全体構造

EditorFrame から Tool と Figure までの関係を示す．
各クラスの詳細は「クラス図 2」を参照．

```plantuml
@startuml static-structure-overview

package view {
    class EditorFrame {
        - canvas : Canvas
        - controller : EditorController
        --
        + EditorFrame ()
    }

    class Canvas {
        - figures : List<Figure>
        - preview : Figure
        --
        + addFigure (Figure f) : void
        + setPreview (Figure f) : void
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
    }
}

package model {
    abstract class Figure {
        # strokeColor : Color
        # fillColor : Color
        --
        + draw (Graphics g) : void
        + move (int dx, int dy) : void
        + contains (int x, int y) : boolean
    }

}

EditorFrame *-- Canvas
EditorFrame *-- EditorController
EditorController --> Canvas
EditorController --> Tool
Canvas --> "0..*" Figure

@enduml
```

## クラス図 2: 詳細

### Figure 階層

```plantuml
@startuml static-structure-model

abstract class Figure {
    # strokeColor : Color
    # fillColor : Color
    --
    + draw (Graphics g) : void
    + move (int dx, int dy) : void
    + contains (int x, int y) : boolean
}

class Line {
    - x1 : int
    - y1 : int
    - x2 : int
    - y2 : int
}

class Rectangle {
    - x1 : int
    - y1 : int
    - x2 : int
    - y2 : int
    --
    + setEndCorner (int x2, int y2) : void
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

Figure <|-- Line
Figure <|-- Rectangle
Figure <|-- Circle
Figure <|-- Ellipse
Figure <|-- Polyline
Figure <|-- Polygon

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

Tool <|.. DrawLineTool
Tool <|.. DrawRectTool
Tool <|.. DrawCircleTool
Tool <|.. DrawEllipseTool
Tool <|.. DrawPolylineTool
Tool <|.. DrawPolygonTool

@enduml
```

## パッケージ構成

| パッケージ | 役割 |
|---|---|
| `model` | 図形クラスの階層 |
| `view` | Swing コンポーネント (フレーム・キャンバス) |
| `controller` | マウスイベント処理とツール抽象化 |

## 補足

- `Canvas` は `JPanel` のサブクラスとして実装し，`paintComponent` をオーバーライドして図形を描画する
- `Tool` インタフェースの `onClick`，`onDoubleClick`，`onMove` は `default` メソッドとして空実装を持つ
- `EditorController` は `MouseAdapter` を継承して実装する
- ドラッグ系ツール (`DrawLineTool` 等) は `onPress`/`onDrag`/`onRelease` を使用し，クリック系ツール (`DrawPolylineTool` 等) は `onClick`/`onDoubleClick`/`onMove` を使用する
