import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Redesigned Teacher Panel to manage teacher records.
 * Integrates premium mockup cards, full-width paginated table, and modal dialog
 * forms.
 */
public class TeacherPanel extends JPanel {
    private JTable tableTeachers;
    private DefaultTableModel tableModel;
    private JLabel lblTotalTeachers;
    private JLabel lblPaginationText;
    private JButton btnPrev, btnNext;
    private JButton btnAddTeacherTop;
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalTeachersCount = 0;

    public TeacherPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Header (Title, Subtitle, and Add Teacher Button)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(ThemeConstants.COLOR_BG);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titleTextPanel.setBackground(ThemeConstants.COLOR_BG);

        JLabel titleLabel = new JLabel("Faculty Management");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage academic staff profiles and assignments.");
        subtitleLabel.setFont(ThemeConstants.FONT_BODY);
        subtitleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);

        titleTextPanel.add(titleLabel);
        titleTextPanel.add(subtitleLabel);
        headerPanel.add(titleTextPanel, BorderLayout.WEST);

        // Add New Teacher Button on the right
        btnAddTeacherTop = new JButton("Add New Teacher");
        btnAddTeacherTop.setIcon(new VectorIcon(VectorIcon.Type.ADD_STUDENT, 16, Color.WHITE));
        btnAddTeacherTop.setIconTextGap(8);
        ThemeConstants.styleButton(btnAddTeacherTop, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnAddTeacherTop.addActionListener(e -> showTeacherDialog(null, null, null, false));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonContainer.setBackground(ThemeConstants.COLOR_BG);
        buttonContainer.add(btnAddTeacherTop);
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main content stack (Stats Row + Teacher List Card)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ThemeConstants.COLOR_BG);

        // Stats Card Row (1x4 grid for Faculty)
        JPanel statsRow = createStatsRow();
        mainContent.add(statsRow);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // Teacher Table Card Panel
        JPanel listCard = ThemeConstants.createCardPanel();
        listCard.setLayout(new BorderLayout(12, 12));

        // JTable initialization
        tableModel = new DefaultTableModel(
                new String[] { "Teacher ID", "Name", "Subject", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTeachers = new JTable(tableModel);
        ThemeConstants.styleTable(tableTeachers);

        // Setup status column pill renderer
        tableTeachers.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String status = (value != null) ? value.toString() : "Active";
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                panel.setOpaque(true);
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                }

                boolean isActive = status.equalsIgnoreCase("Active");
                Color bg = isActive ? new Color(220, 252, 231) : new Color(254, 237, 222);
                Color fg = isActive ? new Color(22, 163, 74) : new Color(220, 95, 30);

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
        tableTeachers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableTeachers.getSelectedRow();
                    if (row != -1) {
                        editSelectedTeacher(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableTeachers);
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

        lblPaginationText = new JLabel("Showing 0-0 of 0 teachers");
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
                loadData();
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
            if ((currentPage + 1) * pageSize < totalTeachersCount) {
                currentPage++;
                loadData();
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
            int selectedRow = tableTeachers.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a teacher first.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            editSelectedTeacher(selectedRow);
        });

        JButton btnDelete = new JButton("Delete Selected Teacher");
        ThemeConstants.styleButton(btnDelete, ThemeConstants.COLOR_DANGER, Color.WHITE,
                ThemeConstants.COLOR_DANGER_HOVER);
        btnDelete.addActionListener(e -> deleteTeacher());

        actionRow.add(btnEdit);
        actionRow.add(btnDelete);
        footerContainer.add(actionRow);

        listCard.add(footerContainer, BorderLayout.SOUTH);
        mainContent.add(listCard);

        add(mainContent, BorderLayout.CENTER);

        // Load data
        loadData();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblTotalTeachers = new JLabel("0");

        Color totalIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardTotal = createStatCard("Total Faculty",
                new VectorIcon(VectorIcon.Type.STUDENTS, 20, ThemeConstants.COLOR_PRIMARY),
                lblTotalTeachers, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                totalIconBg, ThemeConstants.COLOR_PRIMARY);

        JLabel lblDepts = new JLabel("14");
        Color deptsIconBg = ThemeConstants.isDarkMode ? new Color(6, 78, 59) : new Color(240, 253, 244);
        JPanel cardDepts = createStatCard("Departments",
                new VectorIcon(VectorIcon.Type.TEACHERS, 20, new Color(16, 185, 129)),
                lblDepts, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                deptsIconBg, new Color(16, 185, 129));

        JLabel lblLeave = new JLabel("09");
        Color leaveIconBg = ThemeConstants.isDarkMode ? new Color(127, 29, 29) : new Color(254, 242, 242);
        JPanel cardLeave = createStatCard("On Leave",
                new VectorIcon(VectorIcon.Type.REPORTS, 20, ThemeConstants.COLOR_DANGER),
                lblLeave, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                leaveIconBg, ThemeConstants.COLOR_DANGER);

        JLabel lblVerified = new JLabel("241");
        Color verifiedIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardVerified = createStatCard("Verified Profile",
                new VectorIcon(VectorIcon.Type.BELL, 20, ThemeConstants.COLOR_PRIMARY),
                lblVerified, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                verifiedIconBg, ThemeConstants.COLOR_PRIMARY);

        row.add(cardTotal);
        row.add(cardDepts);
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

    private void editSelectedTeacher(int selectedRow) {
        int modelRow = tableTeachers.convertRowIndexToModel(selectedRow);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String subject = (String) tableModel.getValueAt(modelRow, 2);
        showTeacherDialog(id, name, subject, true);
    }

    private void showTeacherDialog(String id, String name, String subject, boolean isEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, isEdit ? "Edit Teacher Details" : "Add Teacher Details",
                Dialog.ModalityType.APPLICATION_MODAL);
        ThemeConstants.styleDialog(dialog);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel title = new JLabel(isEdit ? "Edit Teacher Details" : "Add Teacher Details");
        title.setFont(ThemeConstants.FONT_SUBTITLE);
        title.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Full Name:"), gbc);

        gbc.gridy++;
        JTextField txtName = new JTextField(isEdit ? name : "");
        ThemeConstants.styleTextField(txtName);
        card.add(txtName, gbc);

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Teacher ID:"), gbc);

        gbc.gridy++;
        JTextField txtID = new JTextField(isEdit ? id : "");
        ThemeConstants.styleTextField(txtID);
        if (isEdit) {
            txtID.setEditable(false);
        }
        card.add(txtID, gbc);

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Subject:"), gbc);

        gbc.gridy++;
        JTextField txtSubject = new JTextField(isEdit ? subject : "");
        ThemeConstants.styleTextField(txtSubject);
        card.add(txtSubject, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JButton btnSave = new JButton(isEdit ? "Update Teacher" : "Add Teacher");
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
            String enteredName = txtName.getText().trim();
            String enteredID = txtID.getText().trim();
            String enteredSubject = txtSubject.getText().trim();

            if (enteredName.isEmpty() || enteredID.isEmpty() || enteredSubject.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                String sql = "UPDATE teachers SET name = ?, subject = ? WHERE teacher_id = ?";
                try (Connection conn = DatabaseHelper.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, enteredName);
                    pstmt.setString(2, enteredSubject);
                    pstmt.setString(3, enteredID);

                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(dialog, "Teacher updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Teacher not found.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    String checkSql = "SELECT teacher_id FROM teachers WHERE teacher_id = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, enteredID);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                JOptionPane.showMessageDialog(dialog, "Teacher ID already exists!",
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

                    String insertSql = "INSERT INTO teachers (teacher_id, name, subject) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                        stmt.setString(1, enteredID);
                        stmt.setString(2, enteredName);
                        stmt.setString(3, enteredSubject);
                        stmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(dialog, "Teacher added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                card.getBorder()));

        ThemeConstants.applyTheme(dialog);

        dialog.setVisible(true);
    }

    public void loadData(String filter) {
        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);

        // Count total teachers for pagination calculations
        String countSql = "SELECT COUNT(*) FROM teachers";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(countSql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalTeachersCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting teachers: " + e.getMessage());
        }

        // Dynamically update total faculty card label
        if (lblTotalTeachers != null) {
            lblTotalTeachers.setText(String.format("%,d", totalTeachersCount));
        }

        int totalPages = (int) Math.ceil((double) totalTeachersCount / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage >= totalPages)
            currentPage = totalPages - 1;
        if (currentPage < 0)
            currentPage = 0;

        int offset = currentPage * pageSize;

        // Load paginated data
        String sql = "SELECT teacher_id, name, subject FROM teachers " +
                "ORDER BY name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                int rIdx = offset;
                while (rs.next()) {
                    String id = rs.getString("teacher_id");
                    String name = rs.getString("name");
                    String subject = rs.getString("subject");

                    // Variety of statuses matching mockup (e.g. Row % 3 == 1 is "On Leave", others
                    // "Active")
                    String status = (rIdx % 3 == 1) ? "On Leave" : "Active";

                    tableModel.addRow(new Object[] { id, name, subject, status });
                    rIdx++;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load teachers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Update pagination text
        int start = totalTeachersCount > 0 ? (offset + 1) : 0;
        int end = Math.min(offset + pageSize, totalTeachersCount);
        if (lblPaginationText != null) {
            lblPaginationText.setText("Showing " + start + "-" + end + " of " + totalTeachersCount + " teachers");
        }

        // Update nav buttons active state
        if (btnPrev != null)
            btnPrev.setEnabled(currentPage > 0);
        if (btnNext != null)
            btnNext.setEnabled((currentPage + 1) * pageSize < totalTeachersCount);
    }

    private void deleteTeacher() {
        int selectedRow = tableTeachers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to delete.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tableTeachers.convertRowIndexToModel(selectedRow);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete teacher: " + name + " (" + id + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM teachers WHERE teacher_id = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Teacher deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
