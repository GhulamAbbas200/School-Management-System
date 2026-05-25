import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Redesigned Student Panel to manage student records.
 * Integrates premium mockup cards, full-width paginated table, and modal dialog
 * forms.
 */
public class StudentPanel extends JPanel {
    private JTable tableStudents;
    private DefaultTableModel tableModel;
    private JLabel lblTotalStudents;
    private JLabel lblPaginationText;
    private JButton btnPrev, btnNext;
    private JButton btnAddStudentTop;
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalStudentsCount = 0;

    public StudentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Header (Title, Subtitle, and Add Student Button)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(ThemeConstants.COLOR_BG);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titleTextPanel.setBackground(ThemeConstants.COLOR_BG);

        JLabel titleLabel = new JLabel("Student Registry");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("View, update, and manage student records in the database.");
        subtitleLabel.setFont(ThemeConstants.FONT_BODY);
        subtitleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);

        titleTextPanel.add(titleLabel);
        titleTextPanel.add(subtitleLabel);
        headerPanel.add(titleTextPanel, BorderLayout.WEST);

        // Add New Student Button on the right
        btnAddStudentTop = new JButton("Add New Student");
        btnAddStudentTop.setIcon(new VectorIcon(VectorIcon.Type.ADD_STUDENT, 16, Color.WHITE));
        btnAddStudentTop.setIconTextGap(8);
        ThemeConstants.styleButton(btnAddStudentTop, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnAddStudentTop.addActionListener(e -> showStudentDialog(null, null, null, false));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonContainer.setBackground(ThemeConstants.COLOR_BG);
        buttonContainer.add(btnAddStudentTop);
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main content stack (Stats Row + Student List Card)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ThemeConstants.COLOR_BG);

        // Stats Card Row
        JPanel statsRow = createStatsRow();
        mainContent.add(statsRow);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // Student Table Card Panel
        JPanel listCard = ThemeConstants.createCardPanel();
        listCard.setLayout(new BorderLayout(12, 12));

        // JTable initialization
        tableModel = new DefaultTableModel(
                new String[] { "Roll Number", "Name", "Class / Semester", "Status", "Enroll Date" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableStudents = new JTable(tableModel);
        ThemeConstants.styleTable(tableStudents);

        // Setup status column pill renderer
        tableStudents.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String status = (value != null) ? value.toString() : "Verified";
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                panel.setOpaque(true);
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                }

                boolean isVerified = status.equalsIgnoreCase("Verified") || status.equalsIgnoreCase("Active");
                Color bg = isVerified ? new Color(220, 252, 231) : new Color(254, 237, 222);
                Color fg = isVerified ? new Color(22, 163, 74) : new Color(220, 95, 30);
                if (status.equalsIgnoreCase("Suspended")) {
                    bg = new Color(241, 245, 249);
                    fg = ThemeConstants.COLOR_TEXT_SECONDARY;
                }

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
        tableStudents.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableStudents.getSelectedRow();
                    if (row != -1) {
                        editSelectedStudent(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableStudents);
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

        lblPaginationText = new JLabel("Showing 0-0 of 0 students");
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
            if ((currentPage + 1) * pageSize < totalStudentsCount) {
                currentPage++;
                loadData();
            }
        });

        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        paginationRow.add(navPanel, BorderLayout.EAST);
        footerContainer.add(paginationRow);

        // 2. Actions Panel (Edit, Manage Subjects, Delete)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setBackground(Color.WHITE);
        actionRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)));

        JButton btnEdit = new JButton("Edit Selected");
        ThemeConstants.styleButton(btnEdit, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnEdit.addActionListener(e -> {
            int selectedRow = tableStudents.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student first.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            editSelectedStudent(selectedRow);
        });

        JButton btnManageSubjects = new JButton("Manage Enrolled Subjects");
        ThemeConstants.styleButton(btnManageSubjects, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnManageSubjects.addActionListener(e -> {
            int selectedRow = tableStudents.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student first.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = tableStudents.convertRowIndexToModel(selectedRow);
            String roll = (String) tableModel.getValueAt(modelRow, 0);
            String name = (String) tableModel.getValueAt(modelRow, 1);
            showManageSubjectsDialog(roll, name);
        });

        JButton btnDelete = new JButton("Delete Selected Student");
        ThemeConstants.styleButton(btnDelete, ThemeConstants.COLOR_DANGER, Color.WHITE,
                ThemeConstants.COLOR_DANGER_HOVER);
        btnDelete.addActionListener(e -> deleteStudent());

        actionRow.add(btnEdit);
        actionRow.add(btnManageSubjects);
        actionRow.add(btnDelete);
        footerContainer.add(actionRow);

        listCard.add(footerContainer, BorderLayout.SOUTH);
        mainContent.add(listCard);

        add(mainContent, BorderLayout.CENTER);

        // Load data
        loadData();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblTotalStudents = new JLabel("0");

        Color totalIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardTotal = createStatCard("Total Students",
                new VectorIcon(VectorIcon.Type.STUDENTS, 20, ThemeConstants.COLOR_PRIMARY),
                lblTotalStudents, null,
                null,
                ThemeConstants.isDarkMode ? new Color(52, 211, 153) : new Color(22, 163, 74),
                totalIconBg, ThemeConstants.COLOR_PRIMARY);

        JLabel lblEnrollmentRate = new JLabel("94.8%");
        Color enrollIconBg = ThemeConstants.isDarkMode ? new Color(6, 78, 59) : new Color(240, 253, 244);
        JPanel cardEnrollment = createStatCardWithProgressBar("Enrollment Rate",
                new VectorIcon(VectorIcon.Type.TEACHERS, 20, new Color(16, 185, 129)),
                lblEnrollmentRate, 0.948,
                enrollIconBg, new Color(16, 185, 129));

        JLabel lblPendingAdmissions = new JLabel("156");
        Color pendingIconBg = ThemeConstants.isDarkMode ? new Color(127, 29, 29) : new Color(254, 242, 242);
        JPanel cardPending = createStatCard("Pending Admissions",
                new VectorIcon(VectorIcon.Type.REPORTS, 20, ThemeConstants.COLOR_DANGER),
                lblPendingAdmissions, null,
                null,
                ThemeConstants.COLOR_DANGER,
                pendingIconBg, ThemeConstants.COLOR_DANGER);

        row.add(cardTotal);
        row.add(cardEnrollment);
        row.add(cardPending);
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

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) (progressPct * 100));
        bar.setForeground(iconFg);
        bar.setBackground(ThemeConstants.isDarkMode ? new Color(51, 65, 85) : new Color(241, 245, 249));
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setPreferredSize(new Dimension(120, 6));
        bar.setMaximumSize(new Dimension(32767, 6));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(valLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(bar);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private void editSelectedStudent(int selectedRow) {
        int modelRow = tableStudents.convertRowIndexToModel(selectedRow);
        String roll = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String className = (String) tableModel.getValueAt(modelRow, 2);
        showStudentDialog(roll, name, className, true);
    }

    private void showStudentDialog(String roll, String name, String className, boolean isEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, isEdit ? "Edit Student Details" : "Add Student Details",
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

        JLabel title = new JLabel(isEdit ? "Edit Student Details" : "Add Student Details");
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
        card.add(ThemeConstants.createLabel("Roll Number:"), gbc);

        gbc.gridy++;
        JTextField txtRoll = new JTextField(isEdit ? roll : "");
        ThemeConstants.styleTextField(txtRoll);
        if (isEdit) {
            txtRoll.setEditable(false);
        }
        card.add(txtRoll, gbc);

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Class / Semester:"), gbc);

        gbc.gridy++;
        JTextField txtClass = new JTextField(isEdit ? className : "");
        ThemeConstants.styleTextField(txtClass);
        card.add(txtClass, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JButton btnSave = new JButton(isEdit ? "Update Student" : "Add Student");
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
            String enteredRoll = txtRoll.getText().trim();
            String enteredClass = txtClass.getText().trim();

            if (enteredName.isEmpty() || enteredRoll.isEmpty() || enteredClass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                String sql = "UPDATE students SET name = ?, class_name = ? WHERE roll_number = ?";
                try (Connection conn = DatabaseHelper.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, enteredName);
                    pstmt.setString(2, enteredClass);
                    pstmt.setString(3, enteredRoll);

                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(dialog, "Student updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Student not found.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    conn.setAutoCommit(false);

                    String checkSql = "SELECT roll_number FROM students WHERE roll_number = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setString(1, enteredRoll);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                JOptionPane.showMessageDialog(dialog, "Roll number already exists!",
                                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

                    String insertStudent = "INSERT INTO students (roll_number, name, class_name) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertStudent)) {
                        stmt.setString(1, enteredRoll);
                        stmt.setString(2, enteredName);
                        stmt.setString(3, enteredClass);
                        stmt.executeUpdate();
                    }

                    String insertFee = "INSERT INTO fees (roll_number, total_fee, paid_fee, remaining_fee) VALUES (?, 50000.00, 0.00, 50000.00)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertFee)) {
                        stmt.setString(1, enteredRoll);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(dialog, "Student added successfully!",
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

        // Re-apply theme specifically to dialog components to ensure consistency
        // (especially in dark mode)
        ThemeConstants.applyTheme(dialog);

        dialog.setVisible(true);
    }

    public void loadData(String filter) {
        // Ignore the filter as requested (no search filter bar implemented)
        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);

        // Count total students for pagination calculations
        String countSql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(countSql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalStudentsCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting students: " + e.getMessage());
        }

        // Dynamically update total students card label
        if (lblTotalStudents != null) {
            lblTotalStudents.setText(String.format("%,d", totalStudentsCount));
        }

        int totalPages = (int) Math.ceil((double) totalStudentsCount / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage >= totalPages)
            currentPage = totalPages - 1;
        if (currentPage < 0)
            currentPage = 0;

        int offset = currentPage * pageSize;

        // Load paginated data
        String sql = "SELECT roll_number, name, class_name, status, enroll_date FROM students " +
                "ORDER BY name ASC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
                while (rs.next()) {
                    String roll = rs.getString("roll_number");
                    String name = rs.getString("name");
                    String className = rs.getString("class_name");
                    String status = rs.getString("status");
                    Timestamp enrollTs = rs.getTimestamp("enroll_date");
                    String enrollDate = (enrollTs != null) ? sdf.format(enrollTs) : "N/A";

                    tableModel.addRow(new Object[] { roll, name, className, status, enrollDate });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Update pagination text
        int start = totalStudentsCount > 0 ? (offset + 1) : 0;
        int end = Math.min(offset + pageSize, totalStudentsCount);
        if (lblPaginationText != null) {
            lblPaginationText.setText("Showing " + start + "-" + end + " of " + totalStudentsCount + " students");
        }

        // Update nav buttons active state
        if (btnPrev != null)
            btnPrev.setEnabled(currentPage > 0);
        if (btnNext != null)
            btnNext.setEnabled((currentPage + 1) * pageSize < totalStudentsCount);
    }

    private void deleteStudent() {
        int selectedRow = tableStudents.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tableStudents.convertRowIndexToModel(selectedRow);
        String roll = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student: " + name + " (" + roll
                        + ")?\nThis will remove all their grades, attendance, and fee logs.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM students WHERE roll_number = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roll);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showManageSubjectsDialog(String roll, String name) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Manage Enrolled Subjects - " + name, true);
        ThemeConstants.styleDialog(dialog);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel lblTitle = new JLabel("Manage Subjects for " + name);
        lblTitle.setFont(ThemeConstants.FONT_SUBTITLE);
        lblTitle.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Roll Number: " + roll);
        lblSub.setFont(ThemeConstants.FONT_BODY);
        lblSub.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.CENTER);

        // Center card with table and inputs
        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        DefaultTableModel dialogModel = new DefaultTableModel(new String[] { "Subject Name" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable dialogTable = new JTable(dialogModel);
        ThemeConstants.styleTable(dialogTable);
        JScrollPane scrollPane = new JScrollPane(dialogTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        // Helper load function
        Runnable loadEnrolledSubjects = () -> {
            dialogModel.setRowCount(0);
            String sql = "SELECT subject_name FROM student_subjects WHERE roll_number = ? ORDER BY subject_name ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roll);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        dialogModel.addRow(new Object[] { rs.getString("subject_name") });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error loading subjects: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        loadEnrolledSubjects.run();

        // Input and Add button panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField txtNewSubject = new JTextField();
        ThemeConstants.styleTextField(txtNewSubject);
        inputPanel.add(txtNewSubject, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 5, 5, 0);
        JButton btnAddSubject = new JButton("Enroll");
        ThemeConstants.styleButton(btnAddSubject, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        inputPanel.add(btnAddSubject, gbc);

        // Group header and inputs in a top wrapper inside the card
        JPanel topWrapper = new JPanel(new BorderLayout(5, 5));
        topWrapper.setOpaque(false);
        topWrapper.add(headerPanel, BorderLayout.NORTH);
        topWrapper.add(inputPanel, BorderLayout.SOUTH);
        card.add(topWrapper, BorderLayout.NORTH);

        // Bottom actions (Delete & Close)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnRemoveSubject = new JButton("Remove Selected");
        ThemeConstants.styleButton(btnRemoveSubject, ThemeConstants.COLOR_DANGER, Color.WHITE,
                ThemeConstants.COLOR_DANGER_HOVER);
        bottomPanel.add(btnRemoveSubject);

        JButton btnClose = new JButton("Close");
        ThemeConstants.styleButton(btnClose, ThemeConstants.COLOR_TEXT_SECONDARY, Color.WHITE,
                ThemeConstants.COLOR_TEXT_SECONDARY.brighter());
        bottomPanel.add(btnClose);

        card.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(card, BorderLayout.CENTER);

        // Action Listeners
        btnAddSubject.addActionListener(e -> {
            String subject = txtNewSubject.getText().trim();
            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a subject name.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sql = "INSERT INTO student_subjects (roll_number, subject_name) VALUES (?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, roll);
                pstmt.setString(2, subject);
                pstmt.executeUpdate();
                txtNewSubject.setText("");
                loadEnrolledSubjects.run();
            } catch (SQLException ex) {
                if (ex.getMessage().contains("PRIMARY KEY")
                        || ex.getMessage().contains("Violation of PRIMARY KEY constraint")) {
                    JOptionPane.showMessageDialog(dialog, "Student is already enrolled in this subject.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRemoveSubject.addActionListener(e -> {
            int selectedRow = dialogTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Select a subject to remove.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = dialogTable.convertRowIndexToModel(selectedRow);
            String subject = (String) dialogModel.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to remove student from subject: " + subject + "?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM student_subjects WHERE roll_number = ? AND subject_name = ?";
                try (Connection conn = DatabaseHelper.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, roll);
                    pstmt.setString(2, subject);
                    pstmt.executeUpdate();
                    loadEnrolledSubjects.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 15, 15),
                card.getBorder()));

        ThemeConstants.applyTheme(dialog);

        dialog.setVisible(true);
    }
}
