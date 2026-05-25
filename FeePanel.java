import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Redesigned Fee Panel to manage school fees: ledger accounts, payments, and total fee adjustments.
 * Integrates premium mockup cards, full-width paginated table, and modal dialog forms.
 */
public class FeePanel extends JPanel {
    private JTable tableFees;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCollected, lblPendingDues, lblCollectionRate;
    private JLabel lblPaginationText;
    private JButton btnPrev, btnNext;
    private JButton btnNewPaymentTop;
    private int currentPage = 0;
    private final int pageSize = 10;
    private int totalFeesCount = 0;

    public FeePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ThemeConstants.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Header (Title, Subtitle, and New Payment Button)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(ThemeConstants.COLOR_BG);

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titleTextPanel.setBackground(ThemeConstants.COLOR_BG);
        
        JLabel titleLabel = new JLabel("Fee Management & Ledger");
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Process student tuition payments and track outstanding balances.");
        subtitleLabel.setFont(ThemeConstants.FONT_BODY);
        subtitleLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        
        titleTextPanel.add(titleLabel);
        titleTextPanel.add(subtitleLabel);
        headerPanel.add(titleTextPanel, BorderLayout.WEST);

        // New Payment Button on the right
        btnNewPaymentTop = new JButton("New Payment");
        btnNewPaymentTop.setIcon(new VectorIcon(VectorIcon.Type.FEES, 16, Color.WHITE));
        btnNewPaymentTop.setIconTextGap(8);
        ThemeConstants.styleButton(btnNewPaymentTop, ThemeConstants.COLOR_PRIMARY, Color.WHITE, ThemeConstants.COLOR_PRIMARY_HOVER);
        btnNewPaymentTop.addActionListener(e -> showPaymentDialog(null));

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        buttonContainer.setBackground(ThemeConstants.COLOR_BG);
        buttonContainer.add(btnNewPaymentTop);
        headerPanel.add(buttonContainer, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Main content stack (Stats Row + Financial Ledger Card)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ThemeConstants.COLOR_BG);

        // Stats Card Row (1x4 grid for Financial stats)
        JPanel statsRow = createStatsRow();
        mainContent.add(statsRow);
        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // Ledger Table Card Panel
        JPanel listCard = ThemeConstants.createCardPanel();
        listCard.setLayout(new BorderLayout(12, 12));

        // JTable initialization
        tableModel = new DefaultTableModel(
                new String[] { "Roll Number", "Student Name", "Total Fee", "Paid", "Remaining", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableFees = new JTable(tableModel);
        ThemeConstants.styleTable(tableFees);

        // Setup status column pill renderer
        tableFees.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                String status = (value != null) ? value.toString() : "Unpaid";
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                panel.setOpaque(true);
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                }

                Color bg = new Color(254, 226, 226); // default Red bg
                Color fg = new Color(220, 38, 38);   // default Red fg

                if (status.equalsIgnoreCase("Settled")) {
                    bg = new Color(220, 252, 231); // Green
                    fg = new Color(22, 163, 74);
                } else if (status.equalsIgnoreCase("Partial")) {
                    bg = new Color(254, 237, 222); // Orange
                    fg = new Color(220, 95, 30);
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

        // Double-click row to open Payment Dialog
        tableFees.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableFees.getSelectedRow();
                    if (row != -1) {
                        int modelRow = tableFees.convertRowIndexToModel(row);
                        String roll = (String) tableModel.getValueAt(modelRow, 0);
                        showPaymentDialog(roll);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableFees);
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

        lblPaginationText = new JLabel("Showing 0-0 of 0 accounts");
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
                loadLedger();
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
            if ((currentPage + 1) * pageSize < totalFeesCount) {
                currentPage++;
                loadLedger();
            }
        });

        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        paginationRow.add(navPanel, BorderLayout.EAST);
        footerContainer.add(paginationRow);

        // 2. Actions Panel (New Payment, Update Total Fee, Reset Fees)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setBackground(Color.WHITE);
        actionRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 0, 0, 0)));

        JButton btnPayment = new JButton("New Payment");
        ThemeConstants.styleButton(btnPayment, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnPayment.addActionListener(e -> {
            int selectedRow = tableFees.getSelectedRow();
            String selectedRoll = null;
            if (selectedRow != -1) {
                int modelRow = tableFees.convertRowIndexToModel(selectedRow);
                selectedRoll = (String) tableModel.getValueAt(modelRow, 0);
            }
            showPaymentDialog(selectedRoll);
        });

        JButton btnUpdateTotal = new JButton("Update Total Fee");
        ThemeConstants.styleButton(btnUpdateTotal, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                ThemeConstants.COLOR_PRIMARY_HOVER);
        btnUpdateTotal.addActionListener(e -> {
            int selectedRow = tableFees.getSelectedRow();
            String selectedRoll = null;
            if (selectedRow != -1) {
                int modelRow = tableFees.convertRowIndexToModel(selectedRow);
                selectedRoll = (String) tableModel.getValueAt(modelRow, 0);
            }
            showUpdateTotalDialog(selectedRoll);
        });

        JButton btnReset = new JButton("Reset Student Fees");
        ThemeConstants.styleButton(btnReset, ThemeConstants.COLOR_DANGER, Color.WHITE,
                ThemeConstants.COLOR_DANGER_HOVER);
        btnReset.addActionListener(e -> resetStudentFees());

        actionRow.add(btnPayment);
        actionRow.add(btnUpdateTotal);
        actionRow.add(btnReset);
        footerContainer.add(actionRow);

        listCard.add(footerContainer, BorderLayout.SOUTH);
        mainContent.add(listCard);

        add(mainContent, BorderLayout.CENTER);

        // Load data
        loadData();
    }

    public void loadData() {
        loadLedger();
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblTotalCollected = new JLabel("$0.00");
        Color collectIconBg = ThemeConstants.isDarkMode ? new Color(6, 78, 59) : new Color(240, 253, 244);
        JPanel cardCollected = createStatCard("Total Collected",
                new VectorIcon(VectorIcon.Type.FEES, 20, new Color(22, 163, 74)),
                lblTotalCollected, null,
                null,
                new Color(22, 163, 74),
                collectIconBg, new Color(22, 163, 74));

        lblPendingDues = new JLabel("$0.00");
        Color duesIconBg = ThemeConstants.isDarkMode ? new Color(127, 29, 29) : new Color(254, 242, 242);
        JPanel cardDues = createStatCard("Pending Dues",
                new VectorIcon(VectorIcon.Type.REPORTS, 20, ThemeConstants.COLOR_DANGER),
                lblPendingDues, null,
                null,
                ThemeConstants.COLOR_DANGER,
                duesIconBg, ThemeConstants.COLOR_DANGER);

        JLabel lblScholars = new JLabel("248");
        Color scholarsIconBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
        JPanel cardScholars = createStatCard("Scholars (Active)",
                new VectorIcon(VectorIcon.Type.STUDENTS, 20, ThemeConstants.COLOR_PRIMARY),
                lblScholars, null,
                null,
                ThemeConstants.COLOR_PRIMARY,
                scholarsIconBg, ThemeConstants.COLOR_PRIMARY);

        lblCollectionRate = new JLabel("88.5%");
        Color rateIconBg = ThemeConstants.isDarkMode ? new Color(6, 78, 59) : new Color(240, 253, 244);
        JPanel cardRate = createStatCard("Collection Rate",
                new VectorIcon(VectorIcon.Type.INVOICING, 20, new Color(13, 148, 136)),
                lblCollectionRate, null,
                null,
                new Color(13, 148, 136),
                rateIconBg, new Color(13, 148, 136));

        row.add(cardCollected);
        row.add(cardDues);
        row.add(cardScholars);
        row.add(cardRate);
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

    private void showPaymentDialog(String preSelectedRoll) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Submit Tuition Payment", Dialog.ModalityType.APPLICATION_MODAL);
        ThemeConstants.styleDialog(dialog);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Submit Tuition Payment");
        title.setFont(ThemeConstants.FONT_SUBTITLE);
        title.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Select Student:"), gbc);

        gbc.gridy++;
        JComboBox<StudentComboItem> comboStudents = new JComboBox<>();
        comboStudents.setFont(ThemeConstants.FONT_BODY);
        comboStudents.setBackground(Color.WHITE);
        card.add(comboStudents, gbc);

        // Balance summaries inside dialog
        gbc.gridy++;
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        summaryPanel.setBackground(ThemeConstants.COLOR_CARD_BG);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Student Account Balance"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        summaryPanel.add(new JLabel("Total Fee:"));
        JLabel dlgTotal = new JLabel("$0.00");
        dlgTotal.setFont(ThemeConstants.FONT_SECTION);
        dlgTotal.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        summaryPanel.add(dlgTotal);

        summaryPanel.add(new JLabel("Amount Paid:"));
        JLabel dlgPaid = new JLabel("$0.00");
        dlgPaid.setFont(ThemeConstants.FONT_SECTION);
        dlgPaid.setForeground(ThemeConstants.COLOR_ACCENT);
        summaryPanel.add(dlgPaid);

        summaryPanel.add(new JLabel("Remaining Fee:"));
        JLabel dlgRemaining = new JLabel("$0.00");
        dlgRemaining.setFont(ThemeConstants.FONT_SECTION);
        dlgRemaining.setForeground(ThemeConstants.COLOR_DANGER);
        summaryPanel.add(dlgRemaining);
        card.add(summaryPanel, gbc);

        Runnable updateSummary = () -> {
            StudentComboItem selectedStudent = (StudentComboItem) comboStudents.getSelectedItem();
            if (selectedStudent == null) {
                dlgTotal.setText("$0.00");
                dlgPaid.setText("$0.00");
                dlgRemaining.setText("$0.00");
                return;
            }
            String sql = "SELECT total_fee, paid_fee, remaining_fee FROM fees WHERE roll_number = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, selectedStudent.getRoll());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dlgTotal.setText(String.format("$%.2f", rs.getDouble("total_fee")));
                        dlgPaid.setText(String.format("$%.2f", rs.getDouble("paid_fee")));
                        dlgRemaining.setText(String.format("$%.2f", rs.getDouble("remaining_fee")));
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Error updating summary: " + ex.getMessage());
            }
        };

        comboStudents.addActionListener(e -> updateSummary.run());

        // Load student combobox
        String sql = "SELECT roll_number, name FROM students ORDER BY name ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboStudents.addItem(new StudentComboItem(
                    rs.getString("roll_number"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error loading student combo: " + ex.getMessage());
        }

        // Prefill student
        if (preSelectedRoll != null) {
            for (int i = 0; i < comboStudents.getItemCount(); i++) {
                StudentComboItem item = comboStudents.getItemAt(i);
                if (item.getRoll().equals(preSelectedRoll)) {
                    comboStudents.setSelectedIndex(i);
                    break;
                }
            }
        }

        updateSummary.run();

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Payment Amount ($):"), gbc);

        gbc.gridy++;
        JTextField txtAmount = new JTextField();
        ThemeConstants.styleTextField(txtAmount);
        card.add(txtAmount, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JButton btnSave = new JButton("Submit Payment");
        ThemeConstants.styleButton(btnSave, ThemeConstants.COLOR_PRIMARY, Color.WHITE, ThemeConstants.COLOR_PRIMARY_HOVER);

        JButton btnCancel = new JButton("Cancel");
        ThemeConstants.styleButton(btnCancel, ThemeConstants.COLOR_TEXT_SECONDARY, Color.WHITE, ThemeConstants.COLOR_TEXT_SECONDARY.brighter());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        card.add(btnPanel, gbc);

        dialog.add(card, BorderLayout.CENTER);

        // Action Listeners
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            StudentComboItem student = (StudentComboItem) comboStudents.getSelectedItem();
            String amountStr = txtAmount.getText().trim();

            if (student == null) {
                JOptionPane.showMessageDialog(dialog, "No student selected!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double paymentAmount;
            try {
                paymentAmount = Double.parseDouble(amountStr);
                if (paymentAmount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Payment amount must be a positive number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);

                double remainingFee = 0;
                double paidFee = 0;

                String selectSql = "SELECT paid_fee, remaining_fee FROM fees WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setString(1, student.getRoll());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            paidFee = rs.getDouble("paid_fee");
                            remainingFee = rs.getDouble("remaining_fee");
                        }
                    }
                }

                if (paymentAmount > remainingFee) {
                    JOptionPane.showMessageDialog(dialog, 
                            String.format("Payment amount exceeds student's remaining balance of $%.2f!", remainingFee), 
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    conn.rollback();
                    return;
                }

                double newPaid = paidFee + paymentAmount;
                double newRemaining = remainingFee - paymentAmount;

                String updateSql = "UPDATE fees SET paid_fee = ?, remaining_fee = ? WHERE roll_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newPaid);
                    updateStmt.setDouble(2, newRemaining);
                    updateStmt.setString(3, student.getRoll());
                    updateStmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(dialog, "Payment processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadLedger();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            card.getBorder()
        ));

        ThemeConstants.applyTheme(dialog);
        dialog.setVisible(true);
    }

    private void showUpdateTotalDialog(String preSelectedRoll) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Update Student Total Fee", Dialog.ModalityType.APPLICATION_MODAL);
        ThemeConstants.styleDialog(dialog);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel card = ThemeConstants.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Manage Fee Structure");
        title.setFont(ThemeConstants.FONT_SUBTITLE);
        title.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(ThemeConstants.createLabel("Select Student:"), gbc);

        gbc.gridy++;
        JComboBox<StudentComboItem> comboStudents = new JComboBox<>();
        comboStudents.setFont(ThemeConstants.FONT_BODY);
        comboStudents.setBackground(Color.WHITE);
        card.add(comboStudents, gbc);

        // Account balances
        gbc.gridy++;
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        summaryPanel.setBackground(ThemeConstants.COLOR_CARD_BG);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Student Account Balance"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        summaryPanel.add(new JLabel("Total Fee:"));
        JLabel dlgTotal = new JLabel("$0.00");
        dlgTotal.setFont(ThemeConstants.FONT_SECTION);
        dlgTotal.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        summaryPanel.add(dlgTotal);

        summaryPanel.add(new JLabel("Amount Paid:"));
        JLabel dlgPaid = new JLabel("$0.00");
        dlgPaid.setFont(ThemeConstants.FONT_SECTION);
        dlgPaid.setForeground(ThemeConstants.COLOR_ACCENT);
        summaryPanel.add(dlgPaid);

        summaryPanel.add(new JLabel("Remaining Fee:"));
        JLabel dlgRemaining = new JLabel("$0.00");
        dlgRemaining.setFont(ThemeConstants.FONT_SECTION);
        dlgRemaining.setForeground(ThemeConstants.COLOR_DANGER);
        summaryPanel.add(dlgRemaining);
        card.add(summaryPanel, gbc);

        Runnable updateSummary = () -> {
            StudentComboItem selectedStudent = (StudentComboItem) comboStudents.getSelectedItem();
            if (selectedStudent == null) {
                dlgTotal.setText("$0.00");
                dlgPaid.setText("$0.00");
                dlgRemaining.setText("$0.00");
                return;
            }
            String sql = "SELECT total_fee, paid_fee, remaining_fee FROM fees WHERE roll_number = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, selectedStudent.getRoll());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dlgTotal.setText(String.format("$%.2f", rs.getDouble("total_fee")));
                        dlgPaid.setText(String.format("$%.2f", rs.getDouble("paid_fee")));
                        dlgRemaining.setText(String.format("$%.2f", rs.getDouble("remaining_fee")));
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Error updating summary: " + ex.getMessage());
            }
        };

        comboStudents.addActionListener(e -> updateSummary.run());

        // Load student combobox
        String sql = "SELECT roll_number, name FROM students ORDER BY name ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboStudents.addItem(new StudentComboItem(
                    rs.getString("roll_number"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error loading student combo: " + ex.getMessage());
        }

        // Prefill student
        if (preSelectedRoll != null) {
            for (int i = 0; i < comboStudents.getItemCount(); i++) {
                StudentComboItem item = comboStudents.getItemAt(i);
                if (item.getRoll().equals(preSelectedRoll)) {
                    comboStudents.setSelectedIndex(i);
                    break;
                }
            }
        }

        updateSummary.run();

        gbc.gridy++;
        card.add(ThemeConstants.createLabel("New Total Fee ($):"), gbc);

        gbc.gridy++;
        JTextField txtTotalFee = new JTextField();
        ThemeConstants.styleTextField(txtTotalFee);
        card.add(txtTotalFee, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(ThemeConstants.COLOR_CARD_BG);

        JButton btnSave = new JButton("Update Total");
        ThemeConstants.styleButton(btnSave, ThemeConstants.COLOR_PRIMARY, Color.WHITE, ThemeConstants.COLOR_PRIMARY_HOVER);

        JButton btnCancel = new JButton("Cancel");
        ThemeConstants.styleButton(btnCancel, ThemeConstants.COLOR_TEXT_SECONDARY, Color.WHITE, ThemeConstants.COLOR_TEXT_SECONDARY.brighter());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        card.add(btnPanel, gbc);

        dialog.add(card, BorderLayout.CENTER);

        // Action Listeners
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            StudentComboItem student = (StudentComboItem) comboStudents.getSelectedItem();
            String totalStr = txtTotalFee.getText().trim();

            if (student == null) {
                JOptionPane.showMessageDialog(dialog, "No student selected!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double newTotal;
            try {
                newTotal = Double.parseDouble(totalStr);
                if (newTotal < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Total fee must be a valid non-negative number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);

                double paidFee = 0;

                String selectSql = "SELECT paid_fee FROM fees WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setString(1, student.getRoll());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            paidFee = rs.getDouble("paid_fee");
                        }
                    }
                }

                double newRemaining = newTotal - paidFee;
                if (newRemaining < 0) {
                    JOptionPane.showMessageDialog(dialog, 
                            String.format("New total fee is less than the already paid amount of $%.2f!\nPlease reset fees first if you want to lower it below the paid amount.", paidFee), 
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    conn.rollback();
                    return;
                }

                String updateSql = "UPDATE fees SET total_fee = ?, remaining_fee = ? WHERE roll_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newTotal);
                    updateStmt.setDouble(2, newRemaining);
                    updateStmt.setString(3, student.getRoll());
                    updateStmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(dialog, "Total fee structure updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadLedger();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            card.getBorder()
        ));

        ThemeConstants.applyTheme(dialog);
        dialog.setVisible(true);
    }

    private void loadLedger() {
        tableModel.setRowCount(0);

        // Count total ledger items for pagination calculations
        String countSql = "SELECT COUNT(*) FROM fees";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                totalFeesCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting ledger: " + e.getMessage());
        }

        // Calculate dynamic stats
        double totalCollected = 0.0;
        double pendingDues = 0.0;
        double collectionRate = 88.5; // fallback

        String statsSql = "SELECT SUM(total_fee), SUM(paid_fee), SUM(remaining_fee) FROM fees";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(statsSql)) {
            if (rs.next()) {
                double total = rs.getDouble(1);
                double paid = rs.getDouble(2);
                double remaining = rs.getDouble(3);

                if (!rs.wasNull()) {
                    totalCollected = paid;
                    pendingDues = remaining;
                    if (total > 0) {
                        collectionRate = (paid * 100.0) / total;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating ledger stats: " + e.getMessage());
        }

        // Update stats labels
        if (lblTotalCollected != null) {
            if (totalCollected >= 1000) {
                lblTotalCollected.setText(String.format("$%,.0fk", totalCollected / 1000.0));
            } else {
                lblTotalCollected.setText(String.format("$%,.2f", totalCollected));
            }
        }
        if (lblPendingDues != null) {
            if (pendingDues >= 1000) {
                lblPendingDues.setText(String.format("$%,.0fk", pendingDues / 1000.0));
            } else {
                lblPendingDues.setText(String.format("$%,.2f", pendingDues));
            }
        }
        if (lblCollectionRate != null) {
            lblCollectionRate.setText(String.format("%.1f%%", collectionRate));
        }

        int totalPages = (int) Math.ceil((double) totalFeesCount / pageSize);
        if (totalPages == 0)
            totalPages = 1;
        if (currentPage >= totalPages)
            currentPage = totalPages - 1;
        if (currentPage < 0)
            currentPage = 0;

        int offset = currentPage * pageSize;

        // Load paginated data
        String sql = "SELECT f.roll_number, s.name, f.total_fee, f.paid_fee, f.remaining_fee " +
                     "FROM fees f " +
                     "JOIN students s ON f.roll_number = s.roll_number " +
                     "ORDER BY s.name ASC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String roll = rs.getString("roll_number");
                    String name = rs.getString("name");
                    double total = rs.getDouble("total_fee");
                    double paid = rs.getDouble("paid_fee");
                    double remaining = rs.getDouble("remaining_fee");

                    String status = "Unpaid";
                    if (remaining == 0) {
                        status = "Settled";
                    } else if (paid > 0) {
                        status = "Partial";
                    }

                    tableModel.addRow(new Object[] {
                        roll,
                        name,
                        String.format("$%.2f", total),
                        String.format("$%.2f", paid),
                        String.format("$%.2f", remaining),
                        status
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load fee ledger: " + e.getMessage());
        }

        // Update pagination text
        int start = totalFeesCount > 0 ? (offset + 1) : 0;
        int end = Math.min(offset + pageSize, totalFeesCount);
        if (lblPaginationText != null) {
            lblPaginationText.setText("Showing " + start + "-" + end + " of " + totalFeesCount + " accounts");
        }

        // Update nav buttons active state
        if (btnPrev != null)
            btnPrev.setEnabled(currentPage > 0);
        if (btnNext != null)
            btnNext.setEnabled((currentPage + 1) * pageSize < totalFeesCount);
    }

    private void resetStudentFees() {
        int selectedRow = tableFees.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "No student selected. Select a student first!", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tableFees.convertRowIndexToModel(selectedRow);
        String roll = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to reset all payment details for student " + name + " (" + roll + ")?\nThis will set Paid Fee to $0.00 and Remaining Fee equal to the Total Fee.", 
                "Confirm Fee Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);

                double totalFee = 0;

                String selectSql = "SELECT total_fee FROM fees WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setString(1, roll);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            totalFee = rs.getDouble("total_fee");
                        }
                    }
                }

                String updateSql = "UPDATE fees SET paid_fee = 0.00, remaining_fee = ? WHERE roll_number = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, totalFee);
                    updateStmt.setString(2, roll);
                    updateStmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Student fees reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadLedger();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class StudentComboItem {
        private final String roll;
        private final String name;

        public StudentComboItem(String roll, String name) {
            this.roll = roll;
            this.name = name;
        }

        public String getRoll() { return roll; }

        @Override
        public String toString() {
            return name + " (" + roll + ")";
        }
    }
}
