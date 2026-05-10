import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

public class SelectTool implements Tool {
    private final Canvas canvas;
    private Figure dragTarget;
    private int pressX, pressY;
    private int lastX, lastY;
    private boolean rubberBanding;

    // ハンドル操作用
    private Handle activeHandle;
    private Figure activeHandleFigure;
    // スケール操作用
    private double scaleFixedX, scaleFixedY;
    private double lastScaleDist;
    private double lastScaleDx, lastScaleDy; // 非均一スケール用
    // Line Shift 拘束用 (もう一方の端点のスクリーン座標)
    private double lineAnchorX, lineAnchorY;
    // 回転操作用
    private double rotateCenterX, rotateCenterY;
    private double pressRotateAngle;
    private double totalRotation;

    public SelectTool(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onPressEvent(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        pressX = x; pressY = y;
        lastX  = x; lastY  = y;
        rubberBanding = false;
        activeHandle = null;
        activeHandleFigure = null;

        // 選択中の図形のハンドルを先に確認 (bbox ハンドル優先，次に端点ハンドル)
        outer:
        for (Figure f : canvas.getSelection()) {
            // bbox ハンドル (SCALE / ROTATE) を先にチェック
            List<Handle> bboxHandles = f.getBboxHandles();
            for (Handle h : bboxHandles) {
                if (!h.contains(x, y)) continue;
                activeHandle = h;
                activeHandleFigure = f;
                Handle.Type type = h.getType();
                if (type == Handle.Type.ROTATE) {
                    double cx = 0, cy = 0, cnt = 0;
                    for (Handle ah : bboxHandles) {
                        if (ah.getType() != Handle.Type.ROTATE) {
                            cx += ah.getCx(); cy += ah.getCy(); cnt++;
                        }
                    }
                    if (cnt == 0) {
                        // SCALEハンドルがない場合 (FigureGroup以外のRotatable): 図形自身のハンドルから中心を計算
                        for (Handle ah : f.getHandles()) {
                            cx += ah.getCx(); cy += ah.getCy(); cnt++;
                        }
                    }
                    rotateCenterX = cnt > 0 ? cx / cnt : h.getCx();
                    rotateCenterY = cnt > 0 ? cy / cnt : h.getCy() + 25;
                    pressRotateAngle = Math.atan2(y - rotateCenterY, x - rotateCenterX);
                    totalRotation = 0;
                } else if (isScaleType(type)) {
                    Handle.Type opp = oppositeType(type);
                    for (Handle ah : bboxHandles) {
                        if (ah.getType() == opp) {
                            scaleFixedX = ah.getCx();
                            scaleFixedY = ah.getCy();
                            break;
                        }
                    }
                    lastScaleDist = Math.hypot(x - scaleFixedX, y - scaleFixedY);
                    lastScaleDx = x - scaleFixedX;
                    lastScaleDy = y - scaleFixedY;
                }
                break outer;
            }
            // 端点ハンドルをチェック
            List<Handle> epHandles = f.getHandles();
            for (Handle h : epHandles) {
                if (!h.contains(x, y)) continue;
                activeHandle = h;
                activeHandleFigure = f;
                if (h.getType() == Handle.Type.ENDPOINT && f instanceof Line) {
                    for (Handle ah : epHandles) {
                        if (ah != h) {
                            lineAnchorX = ah.getCx();
                            lineAnchorY = ah.getCy();
                            break;
                        }
                    }
                }
                break outer;
            }
        }

        if (activeHandle != null) return;

        // 通常の図形選択 / 移動
        dragTarget = canvas.figureAt(pressX, pressY);
        if (dragTarget != null) {
            if (!canvas.getSelection().contains(dragTarget)) {
                canvas.select(dragTarget, e.isControlDown());
            }
        } else {
            if (!e.isControlDown()) canvas.clearSelection();
        }
    }

    @Override
    public void onPress(int x, int y) {}

    @Override
    public void onDragEvent(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        if (activeHandle != null) {
            Handle.Type type = activeHandle.getType();
            if (type == Handle.Type.ROTATE) {
                double angle = Math.atan2(y - rotateCenterY, x - rotateCenterX);
                double total = angle - pressRotateAngle;
                if (e.isShiftDown()) {
                    double snap = Math.PI / 12; // 15°
                    total = Math.round(total / snap) * snap;
                }
                double delta = total - totalRotation;
                if (activeHandleFigure instanceof Rotatable r)
                    r.rotate(delta, rotateCenterX, rotateCenterY);
                totalRotation = total;
            } else if (isScaleType(type)) {
                double newDx = x - scaleFixedX;
                double newDy = y - scaleFixedY;
                if (activeHandleFigure instanceof FigureGroup fg) {
                    if (e.isShiftDown()) {
                        // Shift: 均一スケール (距離比)
                        double newDist = Math.hypot(newDx, newDy);
                        if (lastScaleDist > 0.5) {
                            double s = newDist / lastScaleDist;
                            fg.scale(s, s, scaleFixedX, scaleFixedY);
                        }
                        lastScaleDist = newDist;
                    } else {
                        // 非均一スケール (軸別比率)
                        double sx = Math.abs(lastScaleDx) > 0.5 ? newDx / lastScaleDx : 1.0;
                        double sy = Math.abs(lastScaleDy) > 0.5 ? newDy / lastScaleDy : 1.0;
                        fg.scale(sx, sy, scaleFixedX, scaleFixedY);
                    }
                }
                lastScaleDx = newDx;
                lastScaleDy = newDy;
                lastScaleDist = Math.hypot(newDx, newDy);
            } else {
                // ENDPOINT ハンドル (個別図形の制御点)
                if (e.isShiftDown() && activeHandleFigure instanceof Line) {
                    int[] snapped = shiftSnap(x, y, (int) lineAnchorX, (int) lineAnchorY);
                    activeHandle.drag(snapped[0], snapped[1]);
                } else {
                    activeHandle.drag(x, y);
                }
            }
            canvas.repaint();
            return;
        }
        onDrag(x, y);
    }

    @Override
    public void onDrag(int x, int y) {
        if (dragTarget != null) {
            for (Figure f : canvas.getSelection()) f.moveInScreen(x - lastX, y - lastY);
            canvas.repaint();
        } else {
            rubberBanding = true;
            canvas.setSelectionRect(makeRect(pressX, pressY, x, y));
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void onReleaseEvent(MouseEvent e) {
        if (activeHandle != null) {
            activeHandle = null;
            activeHandleFigure = null;
            canvas.repaint();
            return;
        }
        onRelease(e.getX(), e.getY());
    }

    @Override
    public void onRelease(int x, int y) {
        if (rubberBanding) {
            Rectangle rect = makeRect(pressX, pressY, x, y);
            canvas.setSelectionRect(null);
            for (Figure f : canvas.figuresInRect(rect)) canvas.select(f, true);
            rubberBanding = false;
        }
        dragTarget = null;
    }

    private static int[] shiftSnap(int x, int y, int ax, int ay) {
        double dx = x - ax, dy = y - ay;
        double r = Math.hypot(dx, dy);
        double snap = Math.PI / 12; // 15° (24等分)
        double angle = Math.round(Math.atan2(dy, dx) / snap) * snap;
        return new int[]{ax + (int) Math.round(r * Math.cos(angle)),
                         ay + (int) Math.round(r * Math.sin(angle))};
    }

    private static boolean isScaleType(Handle.Type t) {
        return t == Handle.Type.SCALE_NW || t == Handle.Type.SCALE_NE
            || t == Handle.Type.SCALE_SE || t == Handle.Type.SCALE_SW;
    }

    private static Handle.Type oppositeType(Handle.Type t) {
        return switch (t) {
            case SCALE_NW -> Handle.Type.SCALE_SE;
            case SCALE_NE -> Handle.Type.SCALE_SW;
            case SCALE_SE -> Handle.Type.SCALE_NW;
            case SCALE_SW -> Handle.Type.SCALE_NE;
            default -> t;
        };
    }

    private static Rectangle makeRect(int x1, int y1, int x2, int y2) {
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2),
                             Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}
