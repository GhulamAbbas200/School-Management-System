import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Common configuration for color palette, typography, and UI component styling.
 * Used to give the application a premium, modern, flat-design look.
 */
public class ThemeConstants {
    // Color Palette (Non-final to support dynamic theme switching)
    public static Color COLOR_PRIMARY = new Color(37, 99, 235);        // Vibrant Blue (#2563eb)
    public static Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);  // Darker Blue (#1d4ed8)
    public static Color COLOR_BG = new Color(248, 250, 252);          // Light Slate Surface (#f8fafc)
    public static Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);     // Dark Slate (#0f172a)
    public static Color COLOR_CARD_BG = Color.WHITE;
    public static Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);    // Near Black / Slate-900 (#0f172a)
    public static Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139); // Muted Slate-500 (#64748b)
    public static Color COLOR_TEXT_LIGHT = new Color(241, 245, 249);   // Light Gray Text (#f1f5f9)
    public static Color COLOR_ACCENT = new Color(16, 185, 129);        // Success Green (#10b981)
    public static Color COLOR_ACCENT_HOVER = new Color(52, 211, 153); // Light Success (#34d399)
    public static Color COLOR_DANGER = new Color(239, 68, 68);         // Error Red (#ef4444)
    public static Color COLOR_DANGER_HOVER = new Color(248, 113, 113); // Light Error (#f87171)
    public static Color COLOR_BORDER = new Color(226, 232, 240);       // Border Slate-200 (#e2e8f0)
    public static Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);   // Sidebar item hover (#1e293b)

    // Font Fallback Helper
    private static Font getFont(String name, int style, int size) {
        Font font = new Font(name, style, size);
        if (font.getFamily().equals("Dialog") || font.getFamily().equals("SansSerif") || font.getFamily().equals("Dialog.plain")) {
            return new Font("Segoe UI", style, size);
        }
        return font;
    }

    // Typography
    public static final Font FONT_TITLE = getFont("Inter", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = getFont("Inter", Font.BOLD, 18);
    public static final Font FONT_SECTION = getFont("Inter", Font.BOLD, 14);
    public static final Font FONT_BODY = getFont("Inter", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = getFont("Inter", Font.BOLD, 13);
    public static final Font FONT_SMALL = getFont("Inter", Font.PLAIN, 11);

    /**
     * Styles a standard button with modern colors, margins, and hover effects.
     */
    public static void styleButton(JButton btn, Color bg, Color fg, Color hoverBg) {
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color drawBg = b.getBackground();
                if (model.isPressed()) {
                    drawBg = drawBg.darker();
                }

                g2.setColor(drawBg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);

                String role = (String) b.getClientProperty("themeRole");
                if ("btn_normal".equals(role)) {
                    g2.setColor(COLOR_BORDER);
                    g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 12, 12);
                }

                g2.dispose();
                super.paint(g, c);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    /**
     * Styles a JDialog to be undecorated, translucent, and adds dragging support.
     */
    public static void styleDialog(JDialog dialog) {
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        if (dialog.getContentPane() instanceof JComponent) {
            ((JComponent) dialog.getContentPane()).setOpaque(false);
        }

        // Add drag support to the content pane
        final Point[] dragPoint = new Point[1];
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                dialog.setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
            }
        };

        dialog.getContentPane().addMouseListener(mouseAdapter);
        dialog.getContentPane().addMouseMotionListener(mouseAdapter);
    }


    /**
     * Styles input text fields with custom borders and padding.
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_PRIMARY, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    /**
     * Styles tables to have cleaner lines, larger cells, and custom headers.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(221, 225, 255)); // primary-fixed (#dde1ff)
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SECTION);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(isDarkMode ? COLOR_BG : new Color(241, 245, 249)); // Headers #f1f5f9 or dark background
                setForeground(COLOR_TEXT_PRIMARY);
                setFont(FONT_SECTION);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                return this;
            }
        });

        // Custom Cell Padding and Alternating Colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (isSelected) {
                    c.setBackground(t.getSelectionBackground());
                    c.setForeground(t.getSelectionForeground());
                } else {
                    c.setBackground(row % 2 == 0 ? COLOR_CARD_BG : COLOR_BG);
                    c.setForeground(COLOR_TEXT_PRIMARY);
                }
                return c;
            }
        });
    }

    /**
     * Helper to create a card panel with a shadow-like white background and padding.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(COLOR_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        card.setBackground(COLOR_CARD_BG);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return card;
    }

    /**
     * Helper to generate a unified header panel for module panels.
     */
    public static JPanel createHeader(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBackground(COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(10, 5, 20, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_BODY);
        subtitleLabel.setForeground(COLOR_TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    /**
     * Generates standard styled form label.
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    public static boolean isDarkMode = false;

    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
        if (isDarkMode) {
            // Dark Mode colors
            COLOR_PRIMARY = new Color(59, 130, 246);       // Blue (#3b82f6)
            COLOR_PRIMARY_HOVER = new Color(96, 165, 250); // Light Blue (#60a5fa)
            COLOR_BG = new Color(15, 23, 42);              // Very Dark Slate (#0f172a)
            COLOR_SIDEBAR_BG = new Color(30, 41, 59);      // Dark Slate (#1e293b)
            COLOR_SIDEBAR_HOVER = new Color(51, 65, 85);   // Lighter Slate (#334155)
            COLOR_CARD_BG = new Color(30, 41, 59);         // Card Background (#1e293b)
            COLOR_TEXT_PRIMARY = new Color(241, 245, 249);  // White (#f1f5f9)
            COLOR_TEXT_SECONDARY = new Color(148, 163, 184); // Muted Slate (#94a3b8)
            COLOR_TEXT_LIGHT = new Color(241, 245, 249);
            COLOR_ACCENT = new Color(16, 185, 129);        // Emerald Green
            COLOR_ACCENT_HOVER = new Color(52, 211, 153);
            COLOR_DANGER = new Color(239, 68, 68);         // Rose Red
            COLOR_DANGER_HOVER = new Color(248, 113, 113);
            COLOR_BORDER = new Color(51, 65, 85);          // Dark Border (#334155)
        } else {
            // Light Mode colors (Mockup Precision)
            COLOR_PRIMARY = new Color(37, 99, 235);        // Vibrant Blue (#2563eb)
            COLOR_PRIMARY_HOVER = new Color(29, 78, 216);  // Darker Blue (#1d4ed8)
            COLOR_BG = new Color(248, 250, 252);          // Light Slate Surface (#f8fafc)
            COLOR_SIDEBAR_BG = new Color(15, 23, 42);     // Dark Slate (#0f172a)
            COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);   // Sidebar item hover (#1e293b)
            COLOR_CARD_BG = Color.WHITE;
            COLOR_TEXT_PRIMARY = new Color(15, 23, 42);    // Near Black (#0f172a)
            COLOR_TEXT_SECONDARY = new Color(100, 116, 139); // Muted Slate (#64748b)
            COLOR_TEXT_LIGHT = new Color(241, 245, 249);   // Light Gray Text (#f1f5f9)
            COLOR_ACCENT = new Color(16, 185, 129);        // Success Green (#10b981)
            COLOR_ACCENT_HOVER = new Color(52, 211, 153);
            COLOR_DANGER = new Color(239, 68, 68);         // Error Red
            COLOR_DANGER_HOVER = new Color(248, 113, 113);
            COLOR_BORDER = new Color(226, 232, 240);       // Outline Variant (#e2e8f0)
        }
    }

    public static void applyTheme(Component c) {
        if (c == null) return;

        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            String role = (String) jc.getClientProperty("themeRole");
            if (role == null) {
                // Initialize role based on light mode colors
                if (c instanceof JPanel) {
                    Color bg = c.getBackground();
                    if (bg != null) {
                        if (bg.equals(Color.WHITE) || bg.equals(new Color(30, 41, 59))) {
                            role = "card";
                        } else if (bg.equals(new Color(15, 23, 42))) {
                            role = "sidebar";
                        } else {
                            role = "bg";
                        }
                    }
                } else if (c instanceof JLabel) {
                    Color fg = c.getForeground();
                    if (fg != null) {
                        if (fg.equals(Color.WHITE) || fg.equals(new Color(239, 241, 243)) || fg.equals(new Color(241, 245, 249))) {
                            role = "text_light";
                        } else if (fg.equals(new Color(100, 116, 139)) || fg.equals(new Color(68, 70, 83)) || fg.equals(new Color(148, 163, 184))) {
                            role = "text_secondary";
                        } else if (fg.equals(new Color(37, 99, 235)) || fg.equals(new Color(59, 130, 246)) || fg.equals(new Color(0, 40, 142))) {
                            role = "text_primary_blue";
                        } else {
                            role = "text_primary";
                        }
                    }
                } else if (c instanceof JButton) {
                    JButton b = (JButton) c;
                    Color fg = b.getForeground();
                    Color bg = b.getBackground();
                    if (fg != null && (fg.equals(new Color(241, 245, 249)) || fg.equals(new Color(239, 241, 243)))) {
                        role = "btn_sidebar";
                    } else if (bg != null) {
                        if (bg.equals(new Color(37, 99, 235)) || bg.equals(new Color(59, 130, 246)) || bg.equals(new Color(0, 40, 142))) {
                            role = "btn_primary";
                        } else if (bg.equals(new Color(239, 68, 68)) || bg.equals(new Color(186, 26, 26))) {
                            role = "btn_danger";
                        } else {
                            role = "btn_normal";
                        }
                    }
                }
                if (role != null) {
                    jc.putClientProperty("themeRole", role);
                }
            }

            // Now apply colors based on role
            role = (String) jc.getClientProperty("themeRole");
            if (role != null) {
                switch (role) {
                    case "card":
                        jc.setBackground(COLOR_CARD_BG);
                        if (jc.getBorder() != null && jc.getBorder().toString().contains("CompoundBorder")) {
                            jc.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        }
                        break;
                    case "sidebar":
                        jc.setBackground(COLOR_SIDEBAR_BG);
                        break;
                    case "bg":
                        jc.setBackground(COLOR_BG);
                        break;
                    case "text_light":
                        jc.setForeground(COLOR_TEXT_LIGHT);
                        break;
                    case "text_secondary":
                        jc.setForeground(COLOR_TEXT_SECONDARY);
                        break;
                    case "text_primary_blue":
                        jc.setForeground(COLOR_PRIMARY);
                        break;
                    case "text_primary":
                        jc.setForeground(COLOR_TEXT_PRIMARY);
                        break;
                    case "btn_sidebar":
                        jc.setBackground(COLOR_SIDEBAR_BG);
                        jc.setForeground(COLOR_TEXT_LIGHT);
                        break;
                    case "btn_primary":
                        jc.setBackground(COLOR_PRIMARY);
                        jc.setForeground(Color.WHITE);
                        break;
                    case "btn_danger":
                        jc.setBackground(COLOR_DANGER);
                        jc.setForeground(Color.WHITE);
                        break;
                    case "btn_normal":
                        jc.setBackground(COLOR_CARD_BG);
                        jc.setForeground(COLOR_TEXT_PRIMARY);
                        jc.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                        break;
                }
            }

            // Other components that need direct handling
            if (c instanceof JTextField) {
                JTextField f = (JTextField) c;
                f.setBackground(COLOR_CARD_BG);
                f.setForeground(COLOR_TEXT_PRIMARY);
                f.setCaretColor(COLOR_TEXT_PRIMARY);
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            } else if (c instanceof JTable) {
                JTable t = (JTable) c;
                t.setBackground(COLOR_CARD_BG);
                t.setForeground(COLOR_TEXT_PRIMARY);
                t.setSelectionBackground(isDarkMode ? COLOR_PRIMARY : new Color(221, 225, 255));
                t.setSelectionForeground(isDarkMode ? Color.BLACK : COLOR_TEXT_PRIMARY);
                t.setGridColor(COLOR_BORDER);
                
                JTableHeader header = t.getTableHeader();
                if (header != null) {
                    header.setBackground(COLOR_BG);
                    header.setForeground(COLOR_TEXT_PRIMARY);
                    header.repaint();
                }
            } else if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.getViewport().setBackground(COLOR_CARD_BG);
                sp.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
            } else if (c instanceof JComboBox) {
                JComboBox<?> cb = (JComboBox<?>) c;
                cb.setBackground(COLOR_CARD_BG);
                cb.setForeground(COLOR_TEXT_PRIMARY);
            } else if (c instanceof JRadioButton) {
                JRadioButton rb = (JRadioButton) c;
                rb.setBackground(COLOR_CARD_BG);
                rb.setForeground(COLOR_TEXT_PRIMARY);
            } else if (c instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) c;
                cb.setBackground(COLOR_CARD_BG);
                cb.setForeground(COLOR_TEXT_PRIMARY);
            }
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                applyTheme(child);
            }
        }
    }
}
