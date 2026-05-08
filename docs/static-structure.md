# 静的構造

最初に実装する GUI アプリケーションのクラス構成を示す．

## クラス図

```plantuml
@startuml static-structure

package model {
    abstract class Figure {
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
        - x : int
        - y : int
        - width : int
        - height : int
    }

    class FigureGroup {
        - figures : List<Figure>
        --
        + add (Figure f) : void
        + remove (Figure f) : void
    }

    Figure <|-- Line
    Figure <|-- Rectangle
    Figure <|-- FigureGroup
    FigureGroup o-- "0..*" Figure
}

package view {
    class EditorFrame {
        - canvas : Canvas
        - toolbar : ToolBar
        --
        + EditorFrame ()
    }

    class Canvas {
        - figures : List<Figure>
        - selected : Figure
        --
        + addFigure (Figure f) : void
        + removeFigure (Figure f) : void
        + findFigure (int x, int y) : Figure
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
    }

    interface Tool {
        + onPress (int x, int y) : void
        + onDrag (int x, int y) : void
        + onRelease (int x, int y) : void
    }

    class DrawLineTool {
        - startX : int
        - startY : int
    }

    class DrawRectTool {
        - startX : int
        - startY : int
    }

    class SelectTool {
        - dragging : boolean
    }

    Tool <|.. DrawLineTool
    Tool <|.. DrawRectTool
    Tool <|.. SelectTool
}

EditorFrame *-- Canvas
EditorController --> Canvas
EditorController --> Tool
Canvas --> "0..*" Figure

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
- `Tool` インタフェースにより，描画・選択ツールを統一的に扱う
- `EditorController` は `MouseListener`/`MouseMotionListener` を実装する
