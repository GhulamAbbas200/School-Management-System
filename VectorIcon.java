import javax.swing.Icon;
import java.awt.*;

public class VectorIcon implements Icon {
    public enum Type {
        DASHBOARD, STUDENTS, TEACHERS, ATTENDANCE, FEES, REPORTS, PASSWORD, LOGOUT,
        ADD_STUDENT, POST_NOTICE, INVOICING, BULK_SMS, THEME_LIGHT, THEME_DARK, SUBJECTS,
        SEARCH, BELL, ARROW_LEFT, ARROW_RIGHT
    }

    private final Type type;
    private final int size;
    private final Color color; // if null, uses current component foreground

    public VectorIcon(Type type) {
        this(type, 16, null);
    }

    public VectorIcon(Type type, int size) {
        this(type, size, null);
    }

    public VectorIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component comp, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        Color drawColor = color;
        if (drawColor == null) {
            drawColor = comp.getForeground();
        }
        g2d.setColor(drawColor);

        switch (type) {
            case DASHBOARD:
                int gap = 2;
                int itemW = (size - gap) / 2;
                g2d.fillRoundRect(x, y, itemW, itemW, 2, 2);
                g2d.fillRoundRect(x + itemW + gap, y, itemW, itemW, 2, 2);
                g2d.fillRoundRect(x, y + itemW + gap, itemW, itemW, 2, 2);
                g2d.fillRoundRect(x + itemW + gap, y + itemW + gap, itemW, itemW, 2, 2);
                break;
            case STUDENTS:
                // Draw overlapping user silhouettes
                // Person 1 (background/left-slightly offset)
                g2d.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 140));
                int headSize1 = (int)(size * 0.35);
                int headX1 = x + (int)(size * 0.1);
                int headY1 = y + (int)(size * 0.1);
                g2d.fillOval(headX1, headY1, headSize1, headSize1);
                g2d.fillArc(x, y + (int)(size * 0.45), (int)(size * 0.65), (int)(size * 0.5), 0, 180);

                // Person 2 (foreground/right-slightly offset)
                g2d.setColor(drawColor);
                int headSize2 = (int)(size * 0.35);
                int headX2 = x + (int)(size * 0.45);
                int headY2 = y + (int)(size * 0.1);
                g2d.fillOval(headX2, headY2, headSize2, headSize2);
                g2d.fillArc(x + (int)(size * 0.35), y + (int)(size * 0.45), (int)(size * 0.65), (int)(size * 0.5), 0, 180);
                break;
            case TEACHERS:
                // Draw a graduation cap
                int cy = y + size / 2;
                int[] px = { x + size / 2, x + size - 1, x + size / 2, x + 1 };
                int[] py = { cy - size / 4, cy, cy + size / 4, cy };
                g2d.fillPolygon(px, py, 4);
                g2d.fillArc(x + size / 4, cy + size / 8, size / 2, size / 3, 0, -180);
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawLine(x + size / 2, cy, x + size * 7 / 8, cy + size / 4);
                g2d.fillOval(x + size * 7 / 8 - 1, cy + size / 4 - 1, 2, 2);
                break;
            case ATTENDANCE:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(x, y + 2, size - 1, size - 3, 2, 2);
                g2d.drawLine(x, y + size / 3 + 1, x + size - 1, y + size / 3 + 1);
                g2d.fillRect(x + size / 4 - 1, y, 2, 3);
                g2d.fillRect(x + size * 3 / 4 - 1, y, 2, 3);
                break;
            case FEES:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(x, y + 1, size - 1, size - 3, 2, 2);
                g2d.fillRect(x, y + size / 4 + 1, size, size / 5);
                g2d.fillRect(x + 2, y + size * 3 / 5, size / 4, size / 5);
                break;
            case REPORTS:
                int colW = size / 4;
                int gGap = 1;
                g2d.fillRect(x, y + size * 3 / 5, colW - gGap, size * 2 / 5);
                g2d.fillRect(x + colW, y + size / 3, colW - gGap, size * 2 / 3);
                g2d.fillRect(x + colW * 2, y + size / 5, colW - gGap, size * 4 / 5);
                g2d.fillRect(x + colW * 3, y, colW - gGap, size);
                break;
            case PASSWORD:
                g2d.fillRoundRect(x, y + size / 2, size, size / 2, 2, 2);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawArc(x + size / 4, y + 1, size / 2, size / 2, 0, 180);
                break;
            case LOGOUT:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRect(x, y, size * 2 / 3, size - 1);
                int arrowY = y + size / 2;
                g2d.drawLine(x + size / 3, arrowY, x + size - 1, arrowY);
                g2d.drawLine(x + size - 1, arrowY, x + size - 3, arrowY - 3);
                g2d.drawLine(x + size - 1, arrowY, x + size - 3, arrowY + 3);
                break;
            case ADD_STUDENT:
                int uh = (int)(size * 0.4);
                g2d.fillOval(x, y + (int)(size * 0.05), uh, uh);
                g2d.fillArc(x - (int)(size * 0.1), y + (int)(size * 0.45), (int)(size * 0.6), (int)(size * 0.5), 0, 180);
                g2d.setStroke(new BasicStroke(1.5f));
                int px1 = x + (int)(size * 0.75);
                int py1 = y + (int)(size * 0.5);
                g2d.drawLine(px1 - 3, py1, px1 + 3, py1);
                g2d.drawLine(px1, py1 - 3, px1, py1 + 3);
                break;
            case POST_NOTICE:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(x + 1, y + 2, size - 2, size - 3, 2, 2);
                g2d.fillRect(x + size / 3, y, size / 3, 3);
                g2d.drawLine(x + 4, y + size / 3 + 1, x + size - 4, y + size / 3 + 1);
                g2d.drawLine(x + 4, y + size / 2 + 1, x + size - 4, y + size / 2 + 1);
                break;
            case INVOICING:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRect(x + 1, y, size - 2, size - 2);
                g2d.drawLine(x + 4, y + 4, x + size - 4, y + 4);
                g2d.drawLine(x + 4, y + 8, x + size - 4, y + 8);
                g2d.drawLine(x + 4, y + 12, x + size - 4, y + 12);
                break;
            case BULK_SMS:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(x, y + 2, size - 1, size - 4, 2, 2);
                g2d.drawLine(x, y + 3, x + size / 2, y + size / 2 + 1);
                g2d.drawLine(x + size - 1, y + 3, x + size / 2, y + size / 2 + 1);
                break;
            case THEME_LIGHT:
                g2d.drawOval(x + size / 4, y + size / 4, size / 2, size / 2);
                g2d.setStroke(new BasicStroke(1.0f));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = (int)(x + size / 2 + Math.cos(angle) * (size / 3));
                    int y1 = (int)(y + size / 2 + Math.sin(angle) * (size / 3));
                    int x2 = (int)(x + size / 2 + Math.cos(angle) * (size / 2));
                    int y2 = (int)(y + size / 2 + Math.sin(angle) * (size / 2));
                    g2d.drawLine(x1, y1, x2, y2);
                }
                break;
            case THEME_DARK:
                // Draw a simple crescent moon shape
                g2d.fillArc(x + 1, y + 1, size - 2, size - 2, 60, 240);
                break;
            case SUBJECTS:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(x, y + 1, size - 2, size - 3, 2, 2);
                g2d.drawLine(x + 3, y + 1, x + 3, y + size - 2);
                g2d.drawLine(x + 5, y + 4, x + size - 3, y + 4);
                g2d.drawLine(x + 5, y + 8, x + size - 3, y + 8);
                break;
            case SEARCH:
                int d = size * 3 / 5;
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(x, y, d, d);
                g2d.drawLine(x + d - 1, y + d - 1, x + size - 1, y + size - 1);
                break;
            case BELL:
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawArc(x + size / 4, y + 1, size / 2, size / 2, 0, 180);
                g2d.drawLine(x + 1, y + size * 3 / 4, x + size - 1, y + size * 3 / 4);
                g2d.drawLine(x + 1, y + size * 3 / 4, x + size / 4, y + size / 4 + 1);
                g2d.drawLine(x + size - 1, y + size * 3 / 4, x + size * 3 / 4, y + size / 4 + 1);
                g2d.fillOval(x + size / 2 - 2, y + size * 3 / 4 + 1, 4, 2);
                break;
            case ARROW_LEFT:
                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + size * 2 / 3, y + size / 4, x + size / 3, y + size / 2);
                g2d.drawLine(x + size / 3, y + size / 2, x + size * 2 / 3, y + size * 3 / 4);
                break;
            case ARROW_RIGHT:
                g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + size / 3, y + size / 4, x + size * 2 / 3, y + size / 2);
                g2d.drawLine(x + size * 2 / 3, y + size / 2, x + size / 3, y + size * 3 / 4);
                break;
        }

        g2d.dispose();
    }
}
