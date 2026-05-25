import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Redesigned Attendance Panel to record and view student attendance logs.
 * Integrates premium mockup cards, full-width paginated table, and modal dialog
 * forms.
 */
public class AttendancePanel extends JPanel {
    private JTable tableAttendance;
    private DefaultTableModel tableModel;
    private JLabel lblAvgAttendance;
    private JLabel lblAbsenteesToday;
    private JProgressBar progressAvg;
    private JLabel lblPaginationText;
    private JButton btnPrev, btnNext;
    private JButton btnMarkAttendanceTop;
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalAttendanceCount = 0;

    public AttendancePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Header (Title, Subtitle, and Mark Attendance Button)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(ThemeConstants.COLOR_BG);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titleTextPanel.setBackground(ThemeConstants.COLOR_BG);

        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Mark daily attendance and view student attendance logs.");
        subtitleLabel.setFont(ThemeConstants.FONT_BODY);
        subtitleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);

        titleTextPanel.add(titleLabel);
        titleTextPanel.add(subtitleLabel);
        headerPanel.add(titleTextPanel, BorderLayout.WEST);

        // Mark Attendance Button on the right
        btnMarkAttendanceTop = new JButton("Mark Attendance");
        btnMarkAttendanceTop.setIcon(new VectorIcon(VectorIcon.Type.ATTENDANCE, 16, Color.WHITE));
        btnMarkAttendanceTop.setIconTextGap(8);
        ThemeConstants.styleButton(btnMarkAttendanceTop, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnMarkAttendanceTop.addActionListener(e -> showAttendanceDialog(null, null, null, false));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonContainer.setBackground(ThemeConstants.COLOR_BG);
        buttonContainer.add(btnMarkAttendanceTop);
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main content stack (Stats Row + Attendance List Card)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ThemeConstants.COLOR_BG);

        // Stats Card Row (1x3 grid)
        JPanel statsRow = createStatsRow();
        mainContent.add(statsRow);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // Attendance Table Card Panel
        JPanel listCard = ThemeConstants.createCardPanel();
        listCard.setLayout(new BorderLayout(12, 12));

        // JTable initialization
        tableModel = new DefaultTableModel(
                new String[] { "Roll Number", "Student Name", "Date", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableAttendance = new JTable(tableModel);
        ThemeConstants.styleTable(tableAttendance);

        // Setup status column pill renderer
        tableAttendance.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String status = (value != null) ? value.toString() : "Present";
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                panel.setOpaque(true);
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                }

                boolean isPresent = status.equalsIgnoreCase("Present");
                Color bg = isPresent ? new Color(220, 252, 231) : new Color(254, 226, 226);
                Color fg = isPresent ? new Color(22, 163, 74) : new Color(220, 38, 38);

                final Color finalBg = bg;
                final Color finalFg = fg;
                JPanel badge = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(finalBg);
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2d.dispose();
                    }
                };
                badge.setOpaque(false);
                badge.setLayout(new GridBagLayout());
                badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

                JLabel label = new JLabel(status);
                label.setFont(new Font("Inter", Font.BOLD, 10));
                label.setForeground(finalFg);
                badge.add(label);

                panel.add(badge);
                return panel;
            }
        });

        // Double-click row to open Edit Dialog
        tableAttendance.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableAttendance.getSelectedRow();
                    if (row != -1) {
                        editSelectedAttendance(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableAttendance);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));
        listCard.add(scrollPane, BorderLayout.CENTER);

        // Footer Area (Pagination & Action Row)
        JPanel footerContainer = new JPanel();
        footerContainer.setLayout(new BoxLayout(footerContainer, BoxLayout.Y_AXIS));
        footerContainer.setBackground(Color.WHITE);

        // 1. Pagination controls
        JPanel paginationRow = new JPanel(new BorderLayout());
        paginationRow.setBackground(Color.WHITE);
        paginationRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        lblPaginationText = new JLabel("Showing 0-0 of 0 logs");
        lblPaginationText.setFont(ThemeConstants.FONT_BODY);
        lblPaginationText.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        paginationRow.add(lblPaginationText, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navPanel.setBackground(Color.WHITE);

        btnPrev = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ThemeConstants.COLOR_CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.setColor(ThemeConstants.COLOR_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnPrev.setIcon(new VectorIcon(VectorIcon.Type.ARROW_LEFT, 12, ThemeConstants.COLOR_TEXT_PRIMARY));
        btnPrev.setOpaque(false);
        btnPrev.setContentAreaFilled(false);
        btnPrev.setBorderPainted(false);
        btnPrev.setFocusPainted(false);
        btnPrev.setPreferredSize(new Dimension(28, 28));
        btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadLogs();
            }
        });

        btnNext = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ThemeConstants.COLOR_CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.setColor(ThemeConstants.COLOR_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btnNext.setIcon(new VectorIcon(VectorIcon.Type.ARROW_RIGHT, 12, ThemeConstants.COLOR_TEXT_PRIMARY));
        btnNext.setOpaque(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setBorderPainted(false);
        btnNext.setFocusPainted(false);
        btnNext.setPreferredSize(new Dimension(28, 28));
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> {
            if ((currentPage + 1) * pageSize < totalAttendanceCount) {
                currentPage++;
                loadLogs();
            }
        });

        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        paginationRow.add(navPanel, BorderLayout.EAST);
        footerContainer.add(paginationRow);

        // 2. Actions Panel (Edit, Delete)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setBackground(Color.WHITE);
        actionRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)));

        JButton btnEdit = new JButton("Edit Selected");
        ThemeConstants.styleButton(btnEdit, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnEdit.addActionListener(e -> {
            int selectedRow = tableAttendance.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an attendance log first.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            editSelectedAttendance(selectedRow);
        });

        JButton btnDelete = new JButton("Delete Selected Log");
        ThemeConstants.styleButton(btnDelete, ThemeConstants.COLOR_DANGER, Color.WHITE,
                ThemeConstants.COLOR_DANGER_HOVER);
        btnDelete.addActionListener(e -> deleteAttendance());

        actionRow.add(btnEdit);
        actionRow.add(btnDelete);
        footerContainer.add(actionRow);

        listCard.add(footerContainer, BorderLayout.SOUTH);
        mainContent.add(listCard);

        add(mainContent, BorderLayout.CENTER);

        // Load dynamic data
        loadData();
    }

    public void loadData() {
        loadLogs();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblAvgAttendance = new JLabel("94.2%");
        Color avgIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardAvg = createStatCardWithProgressBar("Average Attendance",
                new VectorIcon(VectorIcon.Type.STUDENTS, 20, ThemeConstants.COLOR_PRIMARY),
                lblAvgAttendance, 0.942,
                avgIconBg, ThemeConstants.COLOR_PRIMARY);

        lblAbsenteesToday = new JLabel("0");
        Color leaveIconBg = ThemeConstants.isDarkMode ? new Color(127, 29, 29) : new Color(254, 242, 242);
        JPanel cardLeave = createStatCard("Absentees Today",
                new VectorIcon(VectorIcon.Type.REPORTS, 20, ThemeConstants.COLOR_DANGER),
                lblAbsenteesToday, null,
                null,
                ThemeConstants.COLOR_DANGER,
                leaveIconBg, ThemeConstants.COLOR_DANGER);

        JLabel lblVerified = new JLabel("04");
        Color verifiedIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardVerified = createStatCard("Pending Approvals",
                new VectorIcon(VectorIcon.Type.BELL, 20, ThemeConstants.COLOR_PRIMARY),
                lblVerified, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                verifiedIconBg, ThemeConstants.COLOR_PRIMARY);

        row.add(cardAvg);
        row.add(cardLeave);
        row.add(cardVerified);
        return row;
    }

    private JPanel createStatCard(String title, Icon icon, JLabel valLabel, String trendText,
            Color trendBg, Color trendFg, Color iconBg, Color iconFg) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ThemeConstants.COLOR_CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(ThemeConstants.COLOR_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(iconBg);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        iconContainer.setPreferredSize(new Dimension(36, 36));
        iconContainer.setOpaque(false);
        iconContainer.setLayout(new GridBagLayout());
        JLabel iconLabel = new JLabel(icon);
        iconContainer.add(iconLabel);
        topRow.add(iconContainer, BorderLayout.WEST);

        if (trendText != null) {
            JPanel trendBadge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(trendBg);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.dispose();
                }
            };
            trendBadge.setOpaque(false);
            trendBadge.setLayout(new GridBagLayout());
            trendBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            JLabel trendLabel = new JLabel(trendText);
            trendLabel.setFont(new Font("Inter", Font.BOLD, 10));
            trendLabel.setForeground(trendFg);
            trendBadge.add(trendLabel);
            topRow.add(trendBadge, BorderLayout.EAST);
        }
        card.add(topRow, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Inter", Font.BOLD, 10));
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);

        infoPanel.add(titleLabel);
        infoPanel.add(valLabel);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatCardWithProgressBar(String title, Icon icon, JLabel valLabel, double progressPct,
            Color iconBg, Color iconFg) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ThemeConstants.COLOR_CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(ThemeConstants.COLOR_BORDER);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(iconBg);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        iconContainer.setPreferredSize(new Dimension(36, 36));
        iconContainer.setOpaque(false);
        iconContainer.setLayout(new GridBagLayout());
        JLabel iconLabel = new JLabel(icon);
        iconContainer.add(iconLabel);
        topRow.add(iconContainer, BorderLayout.WEST);
        card.add(topRow, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Inter", Font.BOLD, 10));
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        valLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressAvg = new JProgressBar(0, 100);
        progressAvg.setValue((int) (progressPct * 100));
        progressAvg.setForeground(iconFg);
        progressAvg.setBackground(ThemeConstants.isDarkMode ? new Color(51, 65, 85) : new Color(241, 245, 249));
        progressAvg.setBorder(BorderFactory.createEmptyBorder());
        progressAvg.setPreferredSize(new Dimension(120, 6));
        progressAvg.setMaximumSize(new Dimension(32767, 6));
        progressAvg.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(valLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(progressAvg);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private void editSelectedAttendance(int selectedRow) {
        int modelRow = tableAttendance.convertRowIndexToModel(selectedRow);
        String roll = (String) tableModel.getValueAt(modelRow, 0);
        String date = (String) tableModel.getValueAt(modelRow, 2);
        String status = (String) tableModel.getValueAt(modelRow, 3);
        showAttendanceDialog(roll, date, status, true);
    }

    private void showAttendanceDialog(String roll, String date, String status, boolean isEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, isEdit ? "Edit Attendance Record" : "Mark Student Attendance",
                Dialog.ModalityType.APPLICATION_MODAL);
        ThemeConstants.styleDialog(dialog);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel title = new JLabel(isEdit ? "Edit Student Attendance" : "Mark Student Attendance");
        title.setFont(ThemeConstants.FONT_SUBTITLE);
        title.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Select Student:"), gbc);

        gbc.gridy++;
        JComboBox<StudentComboItem> comboStudents = new JComboBox<>();
        comboStudents.setFont(ThemeConstants.FONT_BODY);
        comboStudents.setBackground(Color.WHITE);
        card.add(comboStudents, gbc);

        // Load student list
        String sql = "SELECT roll_number, name FROM students ORDER BY name ASC";
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboStudents.addItem(new StudentComboItem(
                        rs.getString("roll_number"),
                        rs.getString("name")));
            }
        } catch (SQLException ex) {
            System.err.println("Error loading student combo: " + ex.getMessage());
        }

        // Prefill combo
        if (isEdit) {
            comboStudents.setEnabled(false);
            for (int i = 0; i < comboStudents.getItemCount(); i++) {
                StudentComboItem item = comboStudents.getItemAt(i);
                if (item.getRoll().equals(roll)) {
                    comboStudents.setSelectedIndex(i);
                    break;
                }
            }
        }

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridy++;
        JTextField txtDate = new JTextField();
        ThemeConstants.styleTextField(txtDate);
        if (isEdit) {
            txtDate.setText(date);
            txtDate.setEditable(false);
        } else {
            txtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        card.add(txtDate, gbc);

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Status:"), gbc);

        gbc.gridy++;
        JPanel statusRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusRadioPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JRadioButton rbPresent = new JRadioButton("Present", true);
        JRadioButton rbAbsent = new JRadioButton("Absent");
        rbPresent.setFont(ThemeConstants.FONT_BODY);
        rbPresent.setBackground(ThemeConstants.COLOR_CARD_BG);
        rbAbsent.setFont(ThemeConstants.FONT_BODY);
        rbAbsent.setBackground(ThemeConstants.COLOR_CARD_BG);

        ButtonGroup statusGrp = new ButtonGroup();
        statusGrp.add(rbPresent);
        statusGrp.add(rbAbsent);
        statusRadioPanel.add(rbPresent);
        statusRadioPanel.add(rbAbsent);
        card.add(statusRadioPanel, gbc);

        if (isEdit) {
            if ("Present".equalsIgnoreCase(status)) {
                rbPresent.setSelected(true);
            } else {
                rbAbsent.setSelected(true);
            }
        }

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JButton btnSave = new JButton(isEdit ? "Update Attendance" : "Save Attendance");
        ThemeConstants.styleButton(btnSave, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);

        JButton btnCancel = new JButton("Cancel");
        ThemeConstants.styleButton(btnCancel, ThemeConstants.COLOR_TEXT_SECONDARY, Color.WHITE,
                ThemeConstants.COLOR_TEXT_SECONDARY.brighter());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        card.add(btnPanel, gbc);

        dialog.add(card, BorderLayout.CENTER);

        // Action Listeners
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            StudentComboItem selectedStudent = (StudentComboItem) comboStudents.getSelectedItem();
            String dateStr = txtDate.getText().trim();
            String finalStatus = rbPresent.isSelected() ? "Present" : "Absent";

            if (selectedStudent == null) {
                JOptionPane.showMessageDialog(dialog, "No student selected. Add students first!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid date (YYYY-MM-DD)!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.sql.Date.valueOf(dateStr);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid Date format! Please use YYYY-MM-DD.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                String checkSql = "SELECT id FROM attendance WHERE roll_number = ? AND attendance_date = ?";
                boolean exists = false;
                int recordId = -1;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, selectedStudent.getRoll());
                    checkStmt.setDate(2, java.sql.Date.valueOf(dateStr));
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                            recordId = rs.getInt("id");
                        }
                    }
                }

                if (exists) {
                    String updateSql = "UPDATE attendance SET status = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, finalStatus);
                        updateStmt.setInt(2, recordId);
                        updateStmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Attendance updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String insertSql = "INSERT INTO attendance (roll_number, attendance_date, status) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, selectedStudent.getRoll());
                        insertStmt.setDate(2, java.sql.Date.valueOf(dateStr));
                        insertStmt.setString(3, finalStatus);
                        insertStmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Attendance marked successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }

                dialog.dispose();
                loadLogs();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                card.getBorder()));

        ThemeConstants.applyTheme(dialog);

        dialog.setVisible(true);
    }

    private void loadLogs() {
        tableModel.setRowCount(0);

        // Count total attendance logs for pagination calculations
        String countSql = "SELECT COUNT(*) FROM attendance";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(countSql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalAttendanceCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting attendance: " + e.getMessage());
        }

        // Dynamically calculate average attendance rate and update card
        double avgRate = 94.2;
        String avgSql = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) FROM attendance";
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(avgSql)) {
            if (rs.next()) {
                double rate = rs.getDouble(1);
                if (!rs.wasNull()) {
                    avgRate = rate;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average attendance: " + e.getMessage());
        }

        if (lblAvgAttendance != null) {
            lblAvgAttendance.setText(String.format("%.1f%%", avgRate));
        }
        if (progressAvg != null) {
            progressAvg.setValue((int) avgRate);
        }

        // Dynamically calculate absentees today and update card
        int absenteesToday = 12;
        String absenteesSql = "SELECT COUNT(*) FROM attendance WHERE status = 'Absent' AND attendance_date = CAST(GETDATE() AS DATE)";
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(absenteesSql)) {
            if (rs.next()) {
                absenteesToday = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating absentees today: " + e.getMessage());
        }

        if (lblAbsenteesToday != null) {
            lblAbsenteesToday.setText(String.valueOf(absenteesToday));
        }

        int totalPages = (int) Math.ceil((double) totalAttendanceCount / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage >= totalPages)
            currentPage = totalPages - 1;
        if (currentPage < 0)
            currentPage = 0;

        int offset = currentPage * pageSize;

        // Load paginated data
        String sql = "SELECT a.roll_number, s.name, a.attendance_date, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.roll_number = s.roll_number " +
                "ORDER BY a.attendance_date DESC, s.name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String roll = rs.getString("roll_number");
                    String name = rs.getString("name");
                    String date = rs.getDate("attendance_date").toString();
                    String status = rs.getString("status");

                    tableModel.addRow(new Object[] { roll, name, date, status });
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load attendance logs: " + e.getMessage());
        }

        // Update pagination text
        int start = totalAttendanceCount > 0 ? (offset + 1) : 0;
        int end = Math.min(offset + pageSize, totalAttendanceCount);
        if (lblPaginationText != null) {
            lblPaginationText.setText("Showing " + start + "-" + end + " of " + totalAttendanceCount + " logs");
        }

        // Update nav buttons active state
        if (btnPrev != null)
            btnPrev.setEnabled(currentPage > 0);
        if (btnNext != null)
            btnNext.setEnabled((currentPage + 1) * pageSize < totalAttendanceCount);
    }

    private void deleteAttendance() {
        int selectedRow = tableAttendance.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an attendance log to delete.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tableAttendance.convertRowIndexToModel(selectedRow);
        String roll = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String dateStr = (String) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete attendance record for " + name + " on " + dateStr + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM attendance WHERE roll_number = ? AND attendance_date = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roll);
                pstmt.setDate(2, java.sql.Date.valueOf(dateStr));
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Attendance record deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadLogs();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Inner helper class for Student JComboBox items in the dialog.
     */
    private static class StudentComboItem {
        private final String roll;
        private final String name;

        public StudentComboItem(String roll, String name) {
            this.roll = roll;
            this.name = name;
        }

        public String getRoll() {
            return roll;
        }

        @Override
        public String toString() {
            return name + " (" + roll + ")";
        }
    }
}
