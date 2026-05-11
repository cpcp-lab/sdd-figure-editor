import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
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
    // スケール操作用 (FigureGroup のローカル空間で管理する)
    private double scaleFixedX, scaleFixedY;       // 固定角のスクリーン座標
    private double localFixedX, localFixedY;        // 固定角のローカル座標
    // プレス時の絶対値基準 (累積ではなく毎フレーム絶対スケールを計算するために使用)
    private double origLocalDx, origLocalDy;        // プレス時のローカル空間変位
    private double origLocalDist;                   // プレス時のローカル空間距離 (Shift 用)
    private AffineTransform scaleOrigTransform;     // プレス時の transform のクローン
    private AffineTransform scaleOrigInvTransform;  // プレス時の transform の逆変換
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
                    // プレス時の transform を保存し，固定角とドラッグ開始点をローカル座標に変換する
                    AffineTransform t = f.getTransform();
                    scaleOrigTransform = new AffineTransform(t);
                    try {
                        scaleOrigInvTransform = scaleOrigTransform.createInverse();
                        Point2D lf = scaleOrigInvTransform.transform(
                            new Point2D.Double(scaleFixedX, scaleFixedY), null);
                        localFixedX = lf.getX(); localFixedY = lf.getY();
                        Point2D lp = scaleOrigInvTransform.transform(
                            new Point2D.Double(x, y), null);
                        origLocalDx = lp.getX() - localFixedX;
                        origLocalDy = lp.getY() - localFixedY;
                    } catch (NoninvertibleTransformException ex) {
                        // transform が縮退している場合はスクリーン座標で代替
                        scaleOrigInvTransform = null;
                        localFixedX = scaleFixedX; localFixedY = scaleFixedY;
                        origLocalDx = x - scaleFixedX;
                        origLocalDy = y - scaleFixedY;
                    }
                    // グループが縮退して全ハンドルが同一点に重なっている場合は
                    // ハンドルの種類から期待される方向を復元する
                    double signX = (type == Handle.Type.SCALE_NW || type == Handle.Type.SCALE_SW) ? -1 : 1;
                    double signY = (type == Handle.Type.SCALE_NW || type == Handle.Type.SCALE_NE) ? -1 : 1;
                    if (Math.abs(origLocalDx) < 1) origLocalDx = signX;
                    if (Math.abs(origLocalDy) < 1) origLocalDy = signY;
                    origLocalDist = Math.hypot(origLocalDx, origLocalDy);
                    if (origLocalDist < 1) origLocalDist = 1;
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
                if (activeHandleFigure instanceof FigureGroup fg) {
                    // ドラッグ点をプレス時の逆変換でローカル座標に変換する
                    // (現在の transform ではなくプレス時の transform を使うことで累積誤差を防ぐ)
                    double newLocalDx, newLocalDy;
                    if (scaleOrigInvTransform != null) {
                        Point2D lp = scaleOrigInvTransform.transform(
                            new Point2D.Double(x, y), null);
                        newLocalDx = lp.getX() - localFixedX;
                        newLocalDy = lp.getY() - localFixedY;
                    } else {
                        newLocalDx = x - scaleFixedX;
                        newLocalDy = y - scaleFixedY;
                    }
                    // プレス時基準の絶対スケールを計算し，transform をプレス時に戻してから適用する
                    fg.getTransform().setTransform(scaleOrigTransform);
                    if (e.isShiftDown()) {
                        // Shift: 均一スケール (プレス時距離比，常に正)
                        double newDist = Math.max(Math.hypot(newLocalDx, newLocalDy), 0.1);
                        double s = newDist / origLocalDist;
                        fg.scaleLocal(s, s, localFixedX, localFixedY);
                    } else {
                        // 非均一スケール (軸別絶対比率，符号反転による鏡像を許容)
                        double sx = nonzero(newLocalDx) / origLocalDx;
                        double sy = nonzero(newLocalDy) / origLocalDy;
                        fg.scaleLocal(sx, sy, localFixedX, localFixedY);
                    }
                }
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

    /** v が 0 に近すぎる場合に最小値を保証する (スケール計算で 0 除算を防ぐ)． */
    private static double nonzero(double v) {
        return Math.abs(v) >= 0.1 ? v : Math.copySign(0.1, v == 0 ? 1 : v);
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
