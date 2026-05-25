import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Main Application Class for the School Management System.
 * Hosts the Main Frame, CardLayout navigation, Login module, and Dashboard.
 */
public class SchoolManagementSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // GUI panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;

    public SchoolManagementSystem() {
        setTitle("School Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null); // Center on screen

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        mainContainer.add(loginPanel, "login");

        // The Dashboard panel will be loaded after a successful login
        // to ensure it fetches fresh database stats upon entry.

        add(mainContainer);
        cardLayout.show(mainContainer, "login");
    }

    /**
     * Authenticates login credentials and switches to dashboard.
     */
    public boolean handleLogin(String username, String password) {
        String sql = "SELECT username FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String actualUser = rs.getString("username");
                    // Success: Initialise and switch to dashboard
                    dashboardPanel = new DashboardPanel(this, actualUser);
                    mainContainer.add(dashboardPanel, "dashboard");
                    cardLayout.show(mainContainer, "dashboard");
                    return true;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Login Verification Failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Authenticates student login credentials and switches to Student Dashboard.
     */
    public boolean handleStudentLogin(String rollNumber, String password) {
        String sql = "SELECT roll_number, name, class_name FROM students WHERE roll_number = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String roll = rs.getString("roll_number");
                    String name = rs.getString("name");
                    String className = rs.getString("class_name");

                    // Success: Initialise and switch to Student Dashboard
                    StudentDashboardPanel studentDashboard = new StudentDashboardPanel(this, roll, name, className);
                    mainContainer.add(studentDashboard, "student_dashboard");
                    cardLayout.show(mainContainer, "student_dashboard");
                    return true;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Student Login Verification Failed: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean changeStudentPassword(String rollNumber, String oldPassword, String newPassword) {
        String checkSql = "SELECT password FROM students WHERE roll_number = ?";
        String updateSql = "UPDATE students SET password = ? WHERE roll_number = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, rollNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String currentPass = rs.getString("password");
                        if (!currentPass.equals(oldPassword)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, rollNumber);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update password: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean changeAdminPassword(String username, String oldPassword, String newPassword) {
        String checkSql = "SELECT password FROM users WHERE username = ?";
        String updateSql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String currentPass = rs.getString("password");
                        if (!currentPass.equals(oldPassword)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update password: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Secures and logs out the session.
     */
    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit securely?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove dashboard from memory and show login again
            if (dashboardPanel != null) {
                mainContainer.remove(dashboardPanel);
                dashboardPanel = null;
            }
            // Remove student dashboard if exists
            Component[] comps = mainContainer.getComponents();
            for (Component c : comps) {
                if (c instanceof StudentDashboardPanel) {
                    mainContainer.remove(c);
                }
            }
            loginPanel.clearFields();
            cardLayout.show(mainContainer, "login");
        }
    }

    // ==========================================
    // 1. LOGIN PANEL IMPLEMENTATION (FACEBOOK-STYLE SPLIT LAYOUT)
    // ==========================================
    private static class LoginPanel extends JPanel {
        private SchoolManagementSystem parent;
        private JTextField txtUsername;
        private JPasswordField txtPassword;
        private JLabel formTitle, lblUsername, lblFormDesc;
        private JButton btnToggleLogin, btnLogin;
        private boolean isStudentLogin = true; // Default is student login

        public LoginPanel(SchoolManagementSystem parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            setBackground(ThemeConstants.COLOR_BG);

            // Container for split layout
            JPanel splitPanel = new JPanel(new GridLayout(1, 2, 60, 20));
            splitPanel.setBackground(ThemeConstants.COLOR_BG);
            splitPanel.setPreferredSize(new Dimension(950, 520));

            // Left Side: Brand & Info
            JPanel leftPanel = new JPanel(new GridBagLayout());
            leftPanel.setBackground(ThemeConstants.COLOR_BG);
            leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints lgbc = new GridBagConstraints();
            lgbc.gridx = 0;
            lgbc.gridy = 0;
            lgbc.anchor = GridBagConstraints.WEST;
            lgbc.fill = GridBagConstraints.HORIZONTAL;
            lgbc.weightx = 1.0;
            lgbc.insets = new Insets(10, 0, 10, 0);

            // Brand Header: Icon + Title
            JPanel brandHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            brandHeader.setBackground(ThemeConstants.COLOR_BG);

            JPanel iconPanel = new JPanel(new GridBagLayout());
            iconPanel.setBackground(ThemeConstants.COLOR_PRIMARY);
            iconPanel.setPreferredSize(new Dimension(54, 54));
            JLabel iconLabel = new JLabel(new VectorIcon(VectorIcon.Type.TEACHERS, 28, Color.WHITE));
            iconPanel.add(iconLabel);
            brandHeader.add(iconPanel);

            JLabel brandTitle = new JLabel("EduCore SMS");
            brandTitle.setFont(new Font("Inter", Font.BOLD, 36));
            brandTitle.setForeground(ThemeConstants.COLOR_PRIMARY);
            brandHeader.add(brandTitle);

            leftPanel.add(brandHeader, lgbc);

            // Heading
            lgbc.gridy++;
            lgbc.insets = new Insets(20, 0, 10, 0);
            JLabel headingLabel = new JLabel(
                    "<html>Centralized Academic Management<br>for Modern Institutions.</html>");
            headingLabel.setFont(new Font("Inter", Font.BOLD, 22));
            headingLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            leftPanel.add(headingLabel, lgbc);

            // Description
            lgbc.gridy++;
            lgbc.insets = new Insets(10, 0, 25, 0);
            JLabel descLabel = new JLabel(
                    "<html>EduCore provides a unified platform for students and faculty to track attendance, manage grades, and access institutional resources in real-time.</html>");
            descLabel.setFont(new Font("Inter", Font.PLAIN, 15));
            descLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
            leftPanel.add(descLabel, lgbc);

            // Badges Panel
            lgbc.gridy++;
            JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            badgesPanel.setBackground(ThemeConstants.COLOR_BG);

            JPanel secureBadge = createBadge("Secure Portal");
            JPanel dataBadge = createBadge("Real-time Data");
            badgesPanel.add(secureBadge);
            badgesPanel.add(dataBadge);
            leftPanel.add(badgesPanel, lgbc);

            // Right Side: Card containing Form
            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new GridBagLayout());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 0, 8, 0);
            gbc.weightx = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;

            // Form Title
            formTitle = new JLabel("Student Sign In");
            formTitle.setFont(new Font("Inter", Font.BOLD, 22));
            formTitle.setForeground(ThemeConstants.COLOR_PRIMARY);
            card.add(formTitle, gbc);

            // Form Description
            gbc.gridy++;
            gbc.insets = new Insets(2, 0, 15, 0);
            lblFormDesc = new JLabel("Enter your credentials to access your academic dashboard.");
            lblFormDesc.setFont(ThemeConstants.FONT_BODY);
            lblFormDesc.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
            card.add(lblFormDesc, gbc);

            // Input Fields
            gbc.gridy++;
            gbc.insets = new Insets(8, 0, 4, 0);
            lblUsername = ThemeConstants.createLabel("Roll Number:");
            card.add(lblUsername, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(4, 0, 8, 0);
            txtUsername = new JTextField();
            ThemeConstants.styleTextField(txtUsername);
            card.add(txtUsername, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(8, 0, 4, 0);
            card.add(ThemeConstants.createLabel("Password:"), gbc);

            gbc.gridy++;
            gbc.insets = new Insets(4, 0, 8, 0);
            txtPassword = new JPasswordField();
            txtPassword.setFont(ThemeConstants.FONT_BODY);
            txtPassword.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    txtPassword.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeConstants.COLOR_PRIMARY, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    txtPassword.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                }
            });
            card.add(txtPassword, gbc);

            // Remember me and Forgot password
            gbc.gridy++;
            gbc.insets = new Insets(8, 0, 12, 0);
            JPanel extraPanel = new JPanel(new BorderLayout());
            extraPanel.setBackground(Color.WHITE);
            JCheckBox chkRemember = new JCheckBox("Remember me");
            chkRemember.setFont(ThemeConstants.FONT_SMALL);
            chkRemember.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
            chkRemember.setBackground(Color.WHITE);
            extraPanel.add(chkRemember, BorderLayout.WEST);

            JLabel lblForgot = new JLabel("Forgot password?");
            lblForgot.setFont(ThemeConstants.FONT_SMALL);
            lblForgot.setForeground(ThemeConstants.COLOR_PRIMARY);
            lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
            extraPanel.add(lblForgot, BorderLayout.EAST);
            card.add(extraPanel, gbc);

            // Login Button
            gbc.gridy++;
            gbc.insets = new Insets(15, 0, 10, 0);
            btnLogin = new JButton("Login to Dashboard");
            ThemeConstants.styleButton(btnLogin, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                    ThemeConstants.COLOR_PRIMARY_HOVER);
            card.add(btnLogin, gbc);

            // Hyperlink Toggle Button
            gbc.gridy++;
            gbc.insets = new Insets(10, 0, 0, 0);
            btnToggleLogin = new JButton("Are you an administrator? Switch to Admin Portal");
            btnToggleLogin.setFont(ThemeConstants.FONT_BODY);
            btnToggleLogin.setForeground(ThemeConstants.COLOR_PRIMARY);
            btnToggleLogin.setBorderPainted(false);
            btnToggleLogin.setContentAreaFilled(false);
            btnToggleLogin.setFocusPainted(false);
            btnToggleLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.add(btnToggleLogin, gbc);

            splitPanel.add(leftPanel);
            splitPanel.add(card);

            add(splitPanel);

            // Action Listeners
            btnToggleLogin.addActionListener(e -> toggleLoginType());
            btnLogin.addActionListener(e -> triggerLogin());
            txtUsername.addActionListener(e -> triggerLogin());
            txtPassword.addActionListener(e -> triggerLogin());
        }

        private static JPanel createBadge(String text) {
            JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            badge.setBackground(new Color(241, 245, 249)); // light surface-container-low
            badge.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1));
            JLabel label = new JLabel(text);
            label.setFont(ThemeConstants.FONT_SMALL);
            label.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            badge.add(label);
            return badge;
        }

        private void toggleLoginType() {
            isStudentLogin = !isStudentLogin;
            txtUsername.setText("");
            txtPassword.setText("");
            if (isStudentLogin) {
                formTitle.setText("Student Sign In");
                lblUsername.setText("Roll Number:");
                lblFormDesc.setText("Enter your credentials to access your academic dashboard.");
                btnLogin.setText("Login to Dashboard");
                btnToggleLogin.setText("Are you an administrator? Switch to Admin Portal");
            } else {
                formTitle.setText("Admin Sign In");
                lblUsername.setText("Username:");
                lblFormDesc.setText("Restricted access for faculty and system staff.");
                btnLogin.setText("Verify Admin Credentials");
                btnToggleLogin.setText("Student? Return to Student Sign In");
                txtUsername.setText("admin");
                txtPassword.setText("admin123");
            }
            revalidate();
            repaint();
        }

        private void triggerLogin() {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out all fields.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isStudentLogin) {
                if (!parent.handleStudentLogin(user, pass)) {
                    JOptionPane.showMessageDialog(this, "Invalid student credentials! Access Denied.",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (!parent.handleLogin(user, pass)) {
                    JOptionPane.showMessageDialog(this, "Invalid admin credentials! Access Denied.",
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        public void clearFields() {
            txtUsername.setText("");
            txtPassword.setText("");
            if (!isStudentLogin) {
                toggleLoginType();
            }
        }
    }

    // ==========================================
    // 2. DASHBOARD PANEL IMPLEMENTATION
    // ==========================================
    // ==========================================
    // 2. DASHBOARD PANEL IMPLEMENTATION
    // ==========================================
    private static class DashboardPanel extends JPanel {
        private SchoolManagementSystem parent;
        private String username;
        private CardLayout contentCardLayout;
        private JPanel contentArea;
        private JLabel lblTopBarTitle;

        // Submodules
        private HomePanel homePanel;
        private StudentPanel studentPanel;
        private TeacherPanel teacherPanel;
        private AttendancePanel attendancePanel;
        private ResultPanel resultPanel;
        private FeePanel feePanel;
        private JPanel changePasswordPanel;

        // Navigation state
        private java.util.List<JButton> navButtons = new java.util.ArrayList<>();
        private JButton activeBtn = null;

        // Custom rounded-corner sidebar button
        private static class SidebarButton extends JButton {
            private boolean isActive = false;
            private boolean isHovered = false;

            public SidebarButton(String text) {
                super(text);
                setFont(new Font("Inter", Font.BOLD, 13));
                setForeground(new Color(148, 163, 184)); // Muted slate color by default
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(220, 42));
                setPreferredSize(new Dimension(220, 42));
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            public void setActive(boolean active) {
                this.isActive = active;
                setForeground(active ? Color.WHITE : new Color(148, 163, 184));
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2d.setColor(new Color(37, 99, 235)); // Vibrant blue #2563eb
                    g2d.fillRoundRect(5, 1, getWidth() - 10, getHeight() - 2, 8, 8);
                } else if (isHovered) {
                    g2d.setColor(ThemeConstants.COLOR_SIDEBAR_HOVER); // #1e293b
                    g2d.fillRoundRect(5, 1, getWidth() - 10, getHeight() - 2, 8, 8);
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        }

        private void setActiveButton(JButton btn) {
            activeBtn = btn;
            for (JButton b : navButtons) {
                if (b instanceof SidebarButton) {
                    ((SidebarButton) b).setActive(b == activeBtn);
                }
            }
        }

        public void selectTab(String tabName) {
            for (JButton btn : navButtons) {
                if (btn.getText().contains(tabName)) {
                    btn.doClick();
                    break;
                }
            }
        }

        public DashboardPanel(SchoolManagementSystem parent, String username) {
            this.parent = parent;
            this.username = username;
            setLayout(new BorderLayout());

            // Sidebar Panel (Left)
            JPanel sidebar = new JPanel();
            sidebar.putClientProperty("themeRole", "sidebar");
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setPreferredSize(new Dimension(240, 700));
            sidebar.setBackground(ThemeConstants.COLOR_SIDEBAR_BG);
            sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

            // Sidebar Brand Panel: Icon Badge + Text
            JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            brandPanel.putClientProperty("themeRole", "sidebar");
            brandPanel.setBackground(ThemeConstants.COLOR_SIDEBAR_BG);
            brandPanel.setMaximumSize(new Dimension(220, 50));

            JPanel brandIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(37, 99, 235)); // Blue icon container
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 18));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "E";
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            brandIcon.setPreferredSize(new Dimension(32, 32));
            brandIcon.setOpaque(false);

            JLabel brandLabel = new JLabel("EduCore SMS");
            brandLabel.setFont(new Font("Inter", Font.BOLD, 18));
            brandLabel.setForeground(Color.WHITE);

            brandPanel.add(brandIcon);
            brandPanel.add(brandLabel);

            sidebar.add(brandPanel);
            sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

            // Sidebar Navigation Buttons (Updated to match mockup naming & style)
            JButton btnHome = createSidebarButton("Dashboard", new VectorIcon(VectorIcon.Type.DASHBOARD));
            JButton btnStudents = createSidebarButton("Student Registry", new VectorIcon(VectorIcon.Type.STUDENTS));
            JButton btnTeachers = createSidebarButton("Faculty Management", new VectorIcon(VectorIcon.Type.TEACHERS));
            JButton btnAttendance = createSidebarButton("Attendance", new VectorIcon(VectorIcon.Type.ATTENDANCE));
            JButton btnFees = createSidebarButton("Fee Modules", new VectorIcon(VectorIcon.Type.FEES));
            JButton btnResults = createSidebarButton("Reports", new VectorIcon(VectorIcon.Type.REPORTS));

            JButton btnThemeToggle = createSidebarButton(
                    ThemeConstants.isDarkMode ? "Light Mode" : "Dark Mode",
                    new VectorIcon(
                            ThemeConstants.isDarkMode ? VectorIcon.Type.THEME_LIGHT : VectorIcon.Type.THEME_DARK));
            btnThemeToggle.addActionListener(e -> {
                ThemeConstants.setDarkMode(!ThemeConstants.isDarkMode);
                ThemeConstants.applyTheme(parent);
                btnThemeToggle.setText(ThemeConstants.isDarkMode ? "Light Mode" : "Dark Mode");
                btnThemeToggle.setIcon(new VectorIcon(
                        ThemeConstants.isDarkMode ? VectorIcon.Type.THEME_LIGHT : VectorIcon.Type.THEME_DARK));
                parent.repaint();
            });

            JButton btnLogout = createSidebarButton("Logout", new VectorIcon(VectorIcon.Type.LOGOUT));

            sidebar.add(btnHome);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnStudents);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnTeachers);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnAttendance);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnFees);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnResults);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnThemeToggle);
            sidebar.add(Box.createVerticalGlue()); // Push logout to bottom
            sidebar.add(btnLogout);
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

            // Profile Card (Bottom of Sidebar)
            JPanel profileCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(ThemeConstants.COLOR_SIDEBAR_HOVER);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.dispose();
                }
            };
            profileCard.putClientProperty("themeRole", "sidebar_profile");
            profileCard.setLayout(new BorderLayout(10, 0));
            profileCard.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            profileCard.setMaximumSize(new Dimension(220, 52));
            profileCard.setPreferredSize(new Dimension(220, 52));
            profileCard.setOpaque(false);

            // Avatar circular drawing
            JPanel avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(59, 130, 246)); // Slate-500 or bright blue
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 11));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "SA";
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            avatarPanel.setPreferredSize(new Dimension(34, 34));
            avatarPanel.setOpaque(false);

            // Admin metadata text
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel lblRole = new JLabel("System Administrator");
            lblRole.setFont(new Font("Inter", Font.BOLD, 12));
            lblRole.setForeground(Color.WHITE);

            JLabel lblSubtext = new JLabel("Main Campus • v2.4.0");
            lblSubtext.setFont(new Font("Inter", Font.PLAIN, 10));
            lblSubtext.setForeground(new Color(148, 163, 184)); // Muted text

            infoPanel.add(Box.createVerticalGlue());
            infoPanel.add(lblRole);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 1)));
            infoPanel.add(lblSubtext);
            infoPanel.add(Box.createVerticalGlue());

            profileCard.add(avatarPanel, BorderLayout.WEST);
            profileCard.add(infoPanel, BorderLayout.CENTER);
            sidebar.add(profileCard);

            add(sidebar, BorderLayout.WEST);

            // Persistent Content Wrapper
            JPanel contentWrapper = new JPanel(new BorderLayout());
            contentWrapper.setBackground(ThemeConstants.COLOR_BG);

            // Persistent Top Bar
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(ThemeConstants.COLOR_BG);
            topBar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(15, 24, 15, 24)));

            lblTopBarTitle = new JLabel("Dashboard Overview");
            lblTopBarTitle.setFont(new Font("Inter", Font.BOLD, 22));
            lblTopBarTitle.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            topBar.add(lblTopBarTitle, BorderLayout.WEST);

            // Search, notifications and circular avatar
            JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            topRightPanel.setOpaque(false);

            // Styled search box
            JTextField txtSearchAll = new JTextField("Search students, faculty..", 18);
            txtSearchAll.setFont(new Font("Inter", Font.PLAIN, 12));
            txtSearchAll.setBackground(Color.WHITE);
            txtSearchAll.setForeground(new Color(148, 163, 184));
            txtSearchAll.setBorder(BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(ThemeConstants.COLOR_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            txtSearchAll.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (txtSearchAll.getText().equals("Search students, faculty..")) {
                        txtSearchAll.setText("");
                        txtSearchAll.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (txtSearchAll.getText().isEmpty()) {
                        txtSearchAll.setText("Search students, faculty..");
                        txtSearchAll.setForeground(new Color(148, 163, 184));
                    }
                }
            });
            topRightPanel.add(txtSearchAll);

            // Bell icon with badge
            JLabel lblBell = new JLabel(new VectorIcon(VectorIcon.Type.BELL, 20, ThemeConstants.COLOR_TEXT_SECONDARY)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(Color.RED);
                    g2d.fillOval(getWidth() - 6, 2, 6, 6);
                    g2d.dispose();
                }
            };
            lblBell.setCursor(new Cursor(Cursor.HAND_CURSOR));
            topRightPanel.add(lblBell);

            // Circular user avatar
            JPanel userIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(ThemeConstants.COLOR_PRIMARY); // Standout colored bg
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 12));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "A";
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            userIcon.setPreferredSize(new Dimension(32, 32));
            userIcon.setOpaque(false);
            userIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            userIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setActiveButton(null);
                    lblTopBarTitle.setText("Security Settings");
                    contentCardLayout.show(contentArea, "password");
                }
            });
            topRightPanel.add(userIcon);

            topBar.add(topRightPanel, BorderLayout.EAST);
            contentWrapper.add(topBar, BorderLayout.NORTH);

            // Content Area (Right)
            contentCardLayout = new CardLayout();
            contentArea = new JPanel(contentCardLayout);
            contentArea.setBorder(new EmptyBorder(20, 24, 20, 24));
            contentArea.setBackground(ThemeConstants.COLOR_BG);

            // Instantiate subpanels
            homePanel = new HomePanel(this);
            studentPanel = new StudentPanel();
            teacherPanel = new TeacherPanel();
            attendancePanel = new AttendancePanel();
            resultPanel = new ResultPanel();
            feePanel = new FeePanel();
            changePasswordPanel = createChangePasswordPanel();

            contentArea.add(homePanel, "home");
            contentArea.add(studentPanel, "student");
            contentArea.add(teacherPanel, "teacher");
            contentArea.add(attendancePanel, "attendance");
            contentArea.add(resultPanel, "result");
            contentArea.add(feePanel, "fee");
            contentArea.add(changePasswordPanel, "password");

            contentWrapper.add(contentArea, BorderLayout.CENTER);
            add(contentWrapper, BorderLayout.CENTER);

            // Button Event Handlers (Dynamically updates top bar title)
            btnHome.addActionListener(e -> {
                setActiveButton(btnHome);
                lblTopBarTitle.setText("Dashboard Overview");
                homePanel.refreshStats();
                contentCardLayout.show(contentArea, "home");
            });
            btnStudents.addActionListener(e -> {
                setActiveButton(btnStudents);
                lblTopBarTitle.setText("Student Registry");
                studentPanel.loadData("");
                contentCardLayout.show(contentArea, "student");
            });
            btnTeachers.addActionListener(e -> {
                setActiveButton(btnTeachers);
                lblTopBarTitle.setText("Faculty Management");
                teacherPanel.loadData("");
                contentCardLayout.show(contentArea, "teacher");
            });
            btnAttendance.addActionListener(e -> {
                setActiveButton(btnAttendance);
                lblTopBarTitle.setText("Attendance");
                attendancePanel.loadData();
                contentCardLayout.show(contentArea, "attendance");
            });
            btnResults.addActionListener(e -> {
                setActiveButton(btnResults);
                lblTopBarTitle.setText("Reports & Results");
                resultPanel.loadData();
                contentCardLayout.show(contentArea, "result");
            });
            btnFees.addActionListener(e -> {
                setActiveButton(btnFees);
                lblTopBarTitle.setText("Fee Modules");
                feePanel.loadData();
                contentCardLayout.show(contentArea, "fee");
            });
            btnLogout.addActionListener(e -> parent.handleLogout());

            // Set active state on home initially
            setActiveButton(btnHome);
            homePanel.refreshStats();
        }

        private JButton createSidebarButton(String text, Icon icon) {
            SidebarButton btn = new SidebarButton(text);
            btn.putClientProperty("themeRole", "btn_sidebar");
            if (icon != null) {
                btn.setIcon(icon);
                btn.setIconTextGap(12);
            }
            navButtons.add(btn);
            return btn;
        }

        private JPanel createChangePasswordPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);
            panel.add(ThemeConstants.createHeader("Security Settings",
                    "Update your password to keep your administrator account secure."), BorderLayout.NORTH);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 15, 10, 15);
            gbc.weightx = 1.0;

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel lblTitle = new JLabel("Change Admin Password");
            lblTitle.setFont(ThemeConstants.FONT_SECTION);
            lblTitle.setForeground(ThemeConstants.COLOR_PRIMARY);
            card.add(lblTitle, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("Current Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtCurrentPass = new JPasswordField();
            ThemeConstants.styleTextField(txtCurrentPass);
            card.add(txtCurrentPass, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("New Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtNewPass = new JPasswordField();
            ThemeConstants.styleTextField(txtNewPass);
            card.add(txtNewPass, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("Confirm New Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtConfirmPass = new JPasswordField();
            ThemeConstants.styleTextField(txtConfirmPass);
            card.add(txtConfirmPass, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(20, 15, 10, 15);
            JButton btnUpdate = new JButton("Update Password");
            ThemeConstants.styleButton(btnUpdate, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                    ThemeConstants.COLOR_PRIMARY_HOVER);
            card.add(btnUpdate, gbc);

            // Spacer
            gbc.gridy++;
            gbc.weighty = 1.0;
            card.add(Box.createGlue(), gbc);

            panel.add(card, BorderLayout.CENTER);

            btnUpdate.addActionListener(e -> {
                String current = new String(txtCurrentPass.getPassword());
                String newPass = new String(txtNewPass.getPassword());
                String confirm = new String(txtConfirmPass.getPassword());

                if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please fill in all fields.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!newPass.equals(confirm)) {
                    JOptionPane.showMessageDialog(panel, "New passwords do not match.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (parent.changeAdminPassword(username, current, newPass)) {
                    JOptionPane.showMessageDialog(panel, "Password changed successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    txtCurrentPass.setText("");
                    txtNewPass.setText("");
                    txtConfirmPass.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Incorrect current password.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            return panel;
        }
    }

    // ==========================================
    // 3. DASHBOARD HOME PANEL (WITH STATS CARDS)
    // ==========================================
    private static class HomePanel extends JPanel {
        private DashboardPanel parentDashboard;
        private JLabel lblStudentsCount, lblTeachersCount, lblAttendanceRate, lblFeesAmount;
        private JTable enrollTable;
        private DefaultTableModel enrollModel;
        private JLabel lblFooterText;
        private int totalStudentsCount = 0;
        private int currentPage = 0;
        private JButton btnPrev;
        private JButton btnNext;

        public HomePanel(DashboardPanel dashboard) {
            this.parentDashboard = dashboard;
            setLayout(new GridBagLayout());
            setBackground(ThemeConstants.COLOR_BG);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 0, 10, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;

            // Row 1: Stat Cards Grid (1x4)
            JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
            statsGrid.setOpaque(false);

            // Card 1: Students
            lblStudentsCount = new JLabel("0");
            JPanel cardStudents = createStatCard("Total Students",
                    new VectorIcon(VectorIcon.Type.STUDENTS, 18, new Color(37, 99, 235)), lblStudentsCount, null,
                    null, null, new Color(239, 246, 255), new Color(37, 99, 235));

            // Card 2: Faculty
            lblTeachersCount = new JLabel("0");
            JPanel cardTeachers = createStatCard("Faculty Members",
                    new VectorIcon(VectorIcon.Type.TEACHERS, 18, new Color(22, 163, 74)), lblTeachersCount, null,
                    null, null, new Color(240, 253, 244), new Color(22, 163, 74));

            // Card 3: Attendance
            lblAttendanceRate = new JLabel("0.0%");
            JPanel cardAttendance = createStatCard("Today's Attendance",
                    new VectorIcon(VectorIcon.Type.ATTENDANCE, 18, new Color(220, 38, 38)), lblAttendanceRate, null,
                    null, null, new Color(254, 242, 242), new Color(220, 38, 38));

            // Card 4: Revenue
            lblFeesAmount = new JLabel("$0.00");
            JPanel cardFees = createStatCard("Revenue (MTD)",
                    new VectorIcon(VectorIcon.Type.FEES, 18, new Color(13, 148, 136)), lblFeesAmount, null,
                    null, null, new Color(240, 253, 250),
                    new Color(13, 148, 136));

            statsGrid.add(cardStudents);
            statsGrid.add(cardTeachers);
            statsGrid.add(cardAttendance);
            statsGrid.add(cardFees);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 0.0;
            add(statsGrid, gbc);

            // Row 2: Quick Management (Stretched across full width)
            JPanel manageCard = new JPanel(new BorderLayout(10, 15)) {
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
            manageCard.setOpaque(false);
            manageCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            JLabel lblManageTitle = new JLabel("Quick Management");
            lblManageTitle.setFont(new Font("Inter", Font.BOLD, 15));
            lblManageTitle.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            manageCard.add(lblManageTitle, BorderLayout.NORTH);

            // 1x4 Grid of Actions
            JPanel actionsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
            actionsGrid.setOpaque(false);

            JButton btnAddStudent = createQuickButton("Add Student",
                    new VectorIcon(VectorIcon.Type.ADD_STUDENT, 24, ThemeConstants.COLOR_PRIMARY),
                    ThemeConstants.COLOR_PRIMARY, () -> {
                        // Trigger transition to students screen
                        parentDashboard.selectTab("Student Registry");
                    });
            JButton btnPostNotice = createQuickButton("Post Notice",
                    new VectorIcon(VectorIcon.Type.POST_NOTICE, 24, new Color(16, 185, 129)), new Color(16, 185, 129),
                    () -> {
                        JOptionPane.showMessageDialog(this,
                                "Notice module active. Announcements can be managed under class boards.", "Post Notice",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
            JButton btnInvoicing = createQuickButton("Invoicing",
                    new VectorIcon(VectorIcon.Type.INVOICING, 24, new Color(245, 158, 11)), new Color(245, 158, 11),
                    () -> {
                        parentDashboard.selectTab("Fee Modules");
                    });
            JButton btnBulkSMS = createQuickButton("Bulk SMS",
                    new VectorIcon(VectorIcon.Type.BULK_SMS, 24, ThemeConstants.COLOR_PRIMARY),
                    ThemeConstants.COLOR_PRIMARY, () -> {
                        JOptionPane.showMessageDialog(this,
                                "SMS Integration loaded. Select recipient lists from the Registry tab.", "Bulk SMS",
                                JOptionPane.INFORMATION_MESSAGE);
                    });

            actionsGrid.add(btnAddStudent);
            actionsGrid.add(btnPostNotice);
            actionsGrid.add(btnInvoicing);
            actionsGrid.add(btnBulkSMS);

            manageCard.add(actionsGrid, BorderLayout.CENTER);

            // Health Status Bar
            JPanel healthWrapper = new JPanel(new BorderLayout(5, 8));
            healthWrapper.setOpaque(false);
            healthWrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeConstants.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(12, 0, 0, 0)));

            JPanel healthLabels = new JPanel(new BorderLayout());
            healthLabels.setOpaque(false);
            JLabel lblHealthName = new JLabel("System Health");
            lblHealthName.setFont(new Font("Inter", Font.BOLD, 11));
            lblHealthName.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            JLabel lblHealthPct = new JLabel("99.9%");
            lblHealthPct.setFont(new Font("Inter", Font.BOLD, 11));
            lblHealthPct.setForeground(new Color(16, 185, 129));
            healthLabels.add(lblHealthName, BorderLayout.WEST);
            healthLabels.add(lblHealthPct, BorderLayout.EAST);
            healthWrapper.add(healthLabels, BorderLayout.NORTH);

            JProgressBar healthBar = new JProgressBar(0, 100);
            healthBar.setValue(99);
            healthBar.setPreferredSize(new Dimension(100, 6));
            healthBar.setForeground(new Color(16, 185, 129));
            healthBar.setBackground(new Color(241, 245, 249));
            healthBar.setBorder(BorderFactory.createEmptyBorder());
            healthWrapper.add(healthBar, BorderLayout.SOUTH);

            manageCard.add(healthWrapper, BorderLayout.SOUTH);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weighty = 0.35;
            add(manageCard, gbc);

            // Row 3: Recent Enrollments Card
            JPanel recentCard = new JPanel(new BorderLayout(10, 12)) {
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
            recentCard.setOpaque(false);
            recentCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            JPanel recentHeader = new JPanel(new BorderLayout());
            recentHeader.setOpaque(false);
            JPanel recentTitlePanel = new JPanel();
            recentTitlePanel.setLayout(new BoxLayout(recentTitlePanel, BoxLayout.Y_AXIS));
            recentTitlePanel.setOpaque(false);
            JLabel lblRecentTitle = new JLabel("Recent Student Enrollments");
            lblRecentTitle.setFont(new Font("Inter", Font.BOLD, 15));
            lblRecentTitle.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            JLabel lblRecentSub = new JLabel("Reviewing the last 24 hours of admissions");
            lblRecentSub.setFont(new Font("Inter", Font.PLAIN, 11));
            lblRecentSub.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
            recentTitlePanel.add(lblRecentTitle);
            recentTitlePanel.add(Box.createRigidArea(new Dimension(0, 2)));
            recentTitlePanel.add(lblRecentSub);
            recentHeader.add(recentTitlePanel, BorderLayout.WEST);

            JLabel lblViewAll = new JLabel("View All Registry →");
            lblViewAll.setFont(new Font("Inter", Font.BOLD, 12));
            lblViewAll.setForeground(ThemeConstants.COLOR_PRIMARY);
            lblViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblViewAll.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    parentDashboard.selectTab("Student Registry");
                }
            });
            recentHeader.add(lblViewAll, BorderLayout.EAST);
            recentCard.add(recentHeader, BorderLayout.NORTH);

            // JTable Setup with custom renderers
            enrollModel = new DefaultTableModel(
                    new String[] { "STUDENT NAME", "ID NUMBER", "GRADE/LEVEL", "STATUS", "ENROLL DATE" }, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            enrollTable = new JTable(enrollModel);
            ThemeConstants.styleTable(enrollTable);

            // Custom avatar initials renderer
            enrollTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    String name = (value != null) ? value.toString() : "";

                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                    panel.setOpaque(true);
                    if (isSelected) {
                        panel.setBackground(table.getSelectionBackground());
                    } else {
                        panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                    }

                    String initials = "";
                    if (!name.isEmpty()) {
                        String[] parts = name.split(" ");
                        if (parts.length > 0 && !parts[0].isEmpty())
                            initials += parts[0].charAt(0);
                        if (parts.length > 1 && !parts[1].isEmpty())
                            initials += parts[1].charAt(0);
                    }
                    if (initials.isEmpty())
                        initials = "?";
                    initials = initials.toUpperCase();

                    final String finalInitials = initials;
                    final Color avatarBg = getAvatarColor(row);
                    JPanel avatar = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setColor(avatarBg);
                            g2d.fillOval(0, 0, getWidth(), getHeight());
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font("Inter", Font.BOLD, 10));
                            FontMetrics fm = g2d.getFontMetrics();
                            int x = (getWidth() - fm.stringWidth(finalInitials)) / 2;
                            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                            g2d.drawString(finalInitials, x, y);
                            g2d.dispose();
                        }
                    };
                    avatar.setPreferredSize(new Dimension(24, 24));
                    avatar.setOpaque(false);

                    JLabel nameLabel = new JLabel(name);
                    nameLabel.setFont(ThemeConstants.FONT_BODY);
                    nameLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);

                    panel.add(avatar);
                    panel.add(nameLabel);
                    return panel;
                }
            });

            // Custom pill badge status renderer
            enrollTable.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
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

                    boolean isVerified = status.equalsIgnoreCase("Verified");
                    Color bg = isVerified ? new Color(220, 252, 231) : new Color(254, 237, 222);
                    Color fg = isVerified ? new Color(22, 163, 74) : new Color(220, 95, 30);

                    JPanel badge = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setColor(bg);
                            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                            g2d.dispose();
                        }
                    };
                    badge.setOpaque(false);
                    badge.setLayout(new GridBagLayout());
                    badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

                    JLabel label = new JLabel(status);
                    label.setFont(new Font("Inter", Font.BOLD, 10));
                    label.setForeground(fg);
                    badge.add(label);

                    panel.add(badge);
                    return panel;
                }
            });

            enrollTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            enrollTable.getColumnModel().getColumn(1).setPreferredWidth(120);
            enrollTable.getColumnModel().getColumn(2).setPreferredWidth(120);
            enrollTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            enrollTable.getColumnModel().getColumn(4).setPreferredWidth(120);

            JScrollPane scrollPane = new JScrollPane(enrollTable);
            scrollPane.getViewport().setBackground(ThemeConstants.COLOR_CARD_BG);
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));
            recentCard.add(scrollPane, BorderLayout.CENTER);

            // Table Footer (Navigation and summary)
            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.setOpaque(false);
            lblFooterText = new JLabel("Showing 1-3 of 0 new enrollments");
            lblFooterText.setFont(new Font("Inter", Font.PLAIN, 11));
            lblFooterText.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
            footerPanel.add(lblFooterText, BorderLayout.WEST);

            JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            navPanel.setOpaque(false);
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
                    refreshStats();
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
                if ((currentPage + 1) * 3 < totalStudentsCount) {
                    currentPage++;
                    refreshStats();
                }
            });

            navPanel.add(btnPrev);
            navPanel.add(btnNext);
            footerPanel.add(navPanel, BorderLayout.EAST);

            recentCard.add(footerPanel, BorderLayout.SOUTH);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weighty = 0.5;
            add(recentCard, gbc);
        }

        private static Color getAvatarColor(int row) {
            Color[] colors = {
                    new Color(147, 197, 253), // Light Blue
                    new Color(253, 186, 116), // Light Orange
                    new Color(110, 231, 183), // Light Green
                    new Color(252, 165, 165), // Light Red
                    new Color(196, 181, 253) // Light Purple
            };
            return colors[row % colors.length];
        }

        private JButton createQuickButton(String text, Icon icon, Color accentColor, Runnable action) {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(ThemeConstants.COLOR_CARD_BG);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setColor(ThemeConstants.COLOR_BORDER);
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setLayout(new GridBagLayout());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel labelText = new JLabel(text, JLabel.CENTER);
            labelText.setFont(new Font("Inter", Font.BOLD, 11));
            labelText.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            labelText.setAlignmentX(Component.CENTER_ALIGNMENT);

            content.add(iconLabel);
            content.add(Box.createRigidArea(new Dimension(0, 6)));
            content.add(labelText);

            btn.add(content);

            if (action != null) {
                btn.addActionListener(e -> action.run());
            }
            return btn;
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

        public void loadRecentEnrollments(DefaultTableModel tableModel) {
            tableModel.setRowCount(0);
            int offset = currentPage * 3;
            String sql = "SELECT roll_number, name, class_name, status, enroll_date FROM students "
                    + "ORDER BY enroll_date DESC, roll_number DESC "
                    + "OFFSET ? ROWS FETCH NEXT 3 ROWS ONLY";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, offset);
                try (ResultSet rs = pstmt.executeQuery()) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
                    while (rs.next()) {
                        String roll = rs.getString("roll_number");
                        String name = rs.getString("name");
                        String className = rs.getString("class_name");
                        String status = rs.getString("status");
                        Timestamp enrollTs = rs.getTimestamp("enroll_date");
                        String enrollDate = enrollTs != null ? sdf.format(enrollTs) : "N/A";

                        tableModel.addRow(new Object[] { name, "#" + roll, className, status, enrollDate });
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading recent enrollments: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error loading recent enrollments: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void refreshStats() {
            try (Connection conn = DatabaseHelper.getConnection()) {
                // 1. Total Students
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                    if (rs.next()) {
                        totalStudentsCount = rs.getInt(1);
                        lblStudentsCount.setText(String.format("%,d", totalStudentsCount));
                    }
                }

                // 2. Total Teachers
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teachers")) {
                    if (rs.next()) {
                        lblTeachersCount.setText(String.valueOf(rs.getInt(1)));
                    }
                }

                // 3. Attendance Rate
                String attendanceSql = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) "
                        +
                        "FROM attendance";
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(attendanceSql)) {
                    if (rs.next()) {
                        double rate = rs.getDouble(1);
                        if (rs.wasNull()) {
                            lblAttendanceRate.setText("94.8%"); // Mocks visual default if DB empty
                        } else {
                            lblAttendanceRate.setText(String.format("%.1f%%", rate));
                        }
                    }
                }

                // 4. Total Fees Collected
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT SUM(paid_fee) FROM fees")) {
                    if (rs.next()) {
                        double totalCollected = rs.getDouble(1);
                        if (totalCollected > 1000) {
                            lblFeesAmount.setText(String.format("$%,.0fk", totalCollected / 1000.0));
                        } else {
                            lblFeesAmount.setText(String.format("$%,.2f", totalCollected));
                        }
                    }
                }

                // Load recent enrollments and update footer
                loadRecentEnrollments(enrollModel);
                int start = totalStudentsCount > 0 ? (currentPage * 3 + 1) : 0;
                int end = Math.min((currentPage + 1) * 3, totalStudentsCount);
                lblFooterText.setText("Showing " + start + "-" + end + " of " + totalStudentsCount
                        + " new enrollments");

                if (btnPrev != null) {
                    btnPrev.setEnabled(currentPage > 0);
                }
                if (btnNext != null) {
                    btnNext.setEnabled((currentPage + 1) * 3 < totalStudentsCount);
                }

            } catch (SQLException e) {
                System.err.println("Error loading dashboard summary stats: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // 3. STUDENT DASHBOARD PANEL IMPLEMENTATION
    // ==========================================
    private static class StudentDashboardPanel extends JPanel {
        private SchoolManagementSystem parent;
        private String rollNumber;
        private String studentName;
        private String className;

        private CardLayout studentCardLayout;
        private JPanel studentContentArea;

        // UI Labels in Home Panel
        private JLabel lblAttendanceRate, lblAverageMarks, lblOutstandingFees;

        // Tables
        private JTable tableAttendance, tableResults, tableFees, tableSubjects;
        private DefaultTableModel modelAttendance, modelResults, modelFees, modelSubjects;

        // Navigation state
        private java.util.List<JButton> navButtons = new java.util.ArrayList<>();
        private JButton activeBtn = null;

        // Sidebar Button rounded representation
        private static class SidebarButton extends JButton {
            private boolean isActive = false;
            private boolean isHovered = false;

            public SidebarButton(String text) {
                super(text);
                setFont(new Font("Inter", Font.BOLD, 13));
                setForeground(new Color(148, 163, 184)); // Muted slate color
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(220, 42));
                setPreferredSize(new Dimension(220, 42));
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }

            public void setActive(boolean active) {
                this.isActive = active;
                setForeground(active ? Color.WHITE : new Color(148, 163, 184));
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2d.setColor(new Color(37, 99, 235)); // Vibrant blue #2563eb
                    g2d.fillRoundRect(5, 1, getWidth() - 10, getHeight() - 2, 8, 8);
                } else if (isHovered) {
                    g2d.setColor(ThemeConstants.COLOR_SIDEBAR_HOVER); // #1e293b
                    g2d.fillRoundRect(5, 1, getWidth() - 10, getHeight() - 2, 8, 8);
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        }

        private void setActiveButton(JButton btn) {
            activeBtn = btn;
            for (JButton b : navButtons) {
                if (b instanceof SidebarButton) {
                    ((SidebarButton) b).setActive(b == activeBtn);
                }
            }
        }

        public StudentDashboardPanel(SchoolManagementSystem parent, String rollNumber, String studentName,
                String className) {
            this.parent = parent;
            this.rollNumber = rollNumber;
            this.studentName = studentName;
            this.className = className;

            setLayout(new BorderLayout());

            // Left Sidebar
            JPanel sidebar = new JPanel();
            sidebar.putClientProperty("themeRole", "sidebar");
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setPreferredSize(new Dimension(240, 700));
            sidebar.setBackground(ThemeConstants.COLOR_SIDEBAR_BG);
            sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

            // Brand
            JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            brandPanel.putClientProperty("themeRole", "sidebar");
            brandPanel.setBackground(ThemeConstants.COLOR_SIDEBAR_BG);
            brandPanel.setMaximumSize(new Dimension(220, 50));

            JPanel brandIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(16, 185, 129)); // Student portal green icon container
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 18));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "S";
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            brandIcon.setPreferredSize(new Dimension(32, 32));
            brandIcon.setOpaque(false);

            JLabel brandLabel = new JLabel("Student Portal");
            brandLabel.setFont(new Font("Inter", Font.BOLD, 18));
            brandLabel.setForeground(Color.WHITE);

            brandPanel.add(brandIcon);
            brandPanel.add(brandLabel);

            sidebar.add(brandPanel);
            sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

            // Profile Card (Top of Sidebar)
            JPanel profileCard = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(ThemeConstants.COLOR_SIDEBAR_HOVER);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.dispose();
                }
            };
            profileCard.putClientProperty("themeRole", "sidebar_profile");
            profileCard.setLayout(new BorderLayout(10, 0));
            profileCard.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            profileCard.setMaximumSize(new Dimension(220, 52));
            profileCard.setPreferredSize(new Dimension(220, 52));
            profileCard.setOpaque(false);

            // Circular avatar container
            JPanel avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(16, 185, 129)); // Student portal theme color
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 11));
                    FontMetrics fm = g2d.getFontMetrics();
                    String initials = "";
                    if (studentName != null && !studentName.isEmpty()) {
                        String[] parts = studentName.split("\\s+");
                        if (parts.length > 0 && !parts[0].isEmpty()) {
                            initials += parts[0].substring(0, 1).toUpperCase();
                        }
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            initials += parts[1].substring(0, 1).toUpperCase();
                        }
                    }
                    if (initials.isEmpty()) initials = "ST";
                    int x = (getWidth() - fm.stringWidth(initials)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(initials, x, y);
                    g2d.dispose();
                }
            };
            avatarPanel.setPreferredSize(new Dimension(34, 34));
            avatarPanel.setOpaque(false);

            // Text info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel lblRole = new JLabel(this.studentName);
            lblRole.setFont(new Font("Inter", Font.BOLD, 12));
            lblRole.setForeground(Color.WHITE);

            JLabel lblSubtext = new JLabel(this.rollNumber + " • Class " + this.className);
            lblSubtext.setFont(new Font("Inter", Font.PLAIN, 10));
            lblSubtext.setForeground(new Color(148, 163, 184));

            infoPanel.add(Box.createVerticalGlue());
            infoPanel.add(lblRole);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 1)));
            infoPanel.add(lblSubtext);
            infoPanel.add(Box.createVerticalGlue());

            profileCard.add(avatarPanel, BorderLayout.WEST);
            profileCard.add(infoPanel, BorderLayout.CENTER);
            sidebar.add(profileCard);
            sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

            // Sidebar Buttons
            JButton btnHome = createSidebarButton("Dashboard", new VectorIcon(VectorIcon.Type.DASHBOARD));
            JButton btnSubjects = createSidebarButton("My Subjects", new VectorIcon(VectorIcon.Type.SUBJECTS));
            JButton btnAttendance = createSidebarButton("My Attendance", new VectorIcon(VectorIcon.Type.ATTENDANCE));
            JButton btnResults = createSidebarButton("My Results", new VectorIcon(VectorIcon.Type.REPORTS));
            JButton btnFees = createSidebarButton("Tuition Fees", new VectorIcon(VectorIcon.Type.FEES));

            JButton btnThemeToggle = createSidebarButton(
                    ThemeConstants.isDarkMode ? "Light Mode" : "Dark Mode",
                    new VectorIcon(ThemeConstants.isDarkMode ? VectorIcon.Type.THEME_LIGHT : VectorIcon.Type.THEME_DARK));
            btnThemeToggle.addActionListener(e -> {
                ThemeConstants.setDarkMode(!ThemeConstants.isDarkMode);
                ThemeConstants.applyTheme(parent);
                btnThemeToggle.setText(ThemeConstants.isDarkMode ? "Light Mode" : "Dark Mode");
                btnThemeToggle.setIcon(new VectorIcon(ThemeConstants.isDarkMode ? VectorIcon.Type.THEME_LIGHT : VectorIcon.Type.THEME_DARK));
                setActiveButton(activeBtn);
                parent.repaint();
            });

            JButton btnLogout = createSidebarButton("Logout", new VectorIcon(VectorIcon.Type.LOGOUT));

            sidebar.add(btnHome);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnSubjects);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnAttendance);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnResults);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnFees);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
            sidebar.add(btnThemeToggle);
            sidebar.add(Box.createVerticalGlue());
            sidebar.add(btnLogout);
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

            add(sidebar, BorderLayout.WEST);

            // Right Content Wrapper
            JPanel contentWrapper = new JPanel(new BorderLayout());
            contentWrapper.setBackground(ThemeConstants.COLOR_BG);

            // Top Bar
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(ThemeConstants.COLOR_BG);
            topBar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.COLOR_BORDER),
                    BorderFactory.createEmptyBorder(15, 24, 15, 24)));

            JLabel lblTopBarTitle = new JLabel("Welcome Back, " + this.studentName);
            lblTopBarTitle.setFont(new Font("Inter", Font.BOLD, 22));
            lblTopBarTitle.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            topBar.add(lblTopBarTitle, BorderLayout.WEST);

            // Right elements in Top Bar
            JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            topRightPanel.setOpaque(false);

            // Academic Year Info Badge
            JPanel yearBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            yearBadge.setBackground(ThemeConstants.isDarkMode ? new Color(30, 41, 59) : new Color(241, 245, 249));
            yearBadge.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1));
            JLabel lblYear = new JLabel("AY 2023-24");
            lblYear.setFont(ThemeConstants.FONT_SMALL);
            lblYear.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
            yearBadge.add(lblYear);
            topRightPanel.add(yearBadge);

            // Circular user avatar
            JPanel userIcon = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(16, 185, 129));
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Inter", Font.BOLD, 12));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = studentName.substring(0, 1).toUpperCase();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            userIcon.setPreferredSize(new Dimension(32, 32));
            userIcon.setOpaque(false);
            userIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            userIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setActiveButton(null);
                    lblTopBarTitle.setText("Security Settings");
                    studentCardLayout.show(studentContentArea, "password");
                }
            });
            topRightPanel.add(userIcon);

            topBar.add(topRightPanel, BorderLayout.EAST);
            contentWrapper.add(topBar, BorderLayout.NORTH);

            // Right Content Area
            studentCardLayout = new CardLayout();
            studentContentArea = new JPanel(studentCardLayout);
            studentContentArea.setBorder(new EmptyBorder(20, 24, 20, 24));
            studentContentArea.setBackground(ThemeConstants.COLOR_BG);

            // Create Subpanels
            JPanel homePanel = createHomePanel();
            JPanel subjectsPanel = createSubjectsPanel();
            JPanel attendancePanel = createAttendancePanel();
            JPanel resultsPanel = createResultsPanel();
            JPanel feesPanel = createFeesPanel();
            JPanel changePasswordPanel = createChangePasswordPanel();

            studentContentArea.add(homePanel, "home");
            studentContentArea.add(subjectsPanel, "subjects");
            studentContentArea.add(attendancePanel, "attendance");
            studentContentArea.add(resultsPanel, "results");
            studentContentArea.add(feesPanel, "fees");
            studentContentArea.add(changePasswordPanel, "password");

            contentWrapper.add(studentContentArea, BorderLayout.CENTER);
            add(contentWrapper, BorderLayout.CENTER);

            // Action Listeners for Sidebar
            btnHome.addActionListener(e -> {
                setActiveButton(btnHome);
                lblTopBarTitle.setText("Dashboard Overview");
                refreshStats();
                studentCardLayout.show(studentContentArea, "home");
            });
            btnSubjects.addActionListener(e -> {
                setActiveButton(btnSubjects);
                lblTopBarTitle.setText("My Enrolled Subjects");
                loadSubjects();
                studentCardLayout.show(studentContentArea, "subjects");
            });
            btnAttendance.addActionListener(e -> {
                setActiveButton(btnAttendance);
                lblTopBarTitle.setText("My Attendance Log");
                loadAttendance();
                studentCardLayout.show(studentContentArea, "attendance");
            });
            btnResults.addActionListener(e -> {
                setActiveButton(btnResults);
                lblTopBarTitle.setText("My Grades & Performance");
                loadResults();
                studentCardLayout.show(studentContentArea, "results");
            });
            btnFees.addActionListener(e -> {
                setActiveButton(btnFees);
                lblTopBarTitle.setText("My Tuition Fee Status");
                loadFees();
                studentCardLayout.show(studentContentArea, "fees");
            });
            btnLogout.addActionListener(e -> this.parent.handleLogout());

            // Initial Load
            setActiveButton(btnHome);
            lblTopBarTitle.setText("Dashboard Overview");
            refreshStats();
        }

        private JButton createSidebarButton(String text, Icon icon) {
            SidebarButton btn = new SidebarButton(text);
            btn.putClientProperty("themeRole", "btn_sidebar");
            if (icon != null) {
                btn.setIcon(icon);
                btn.setIconTextGap(12);
            }
            navButtons.add(btn);
            return btn;
        }

        private JPanel createHomePanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel statsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
            statsGrid.setOpaque(false);

            // Cards
            lblAttendanceRate = new JLabel("0.0%");
            Color attBg = ThemeConstants.isDarkMode ? new Color(30, 58, 138) : new Color(239, 246, 255);
            JPanel cardAttendance = createStatCard("Attendance Rate",
                    new VectorIcon(VectorIcon.Type.ATTENDANCE, 20, ThemeConstants.COLOR_PRIMARY),
                    lblAttendanceRate, new JLabel("Overall attendance status"),
                    attBg, ThemeConstants.COLOR_PRIMARY, false);

            lblAverageMarks = new JLabel("N/A");
            Color gradeBg = ThemeConstants.isDarkMode ? new Color(6, 78, 59) : new Color(240, 253, 244);
            JPanel cardGrades = createStatCard("Average Score",
                    new VectorIcon(VectorIcon.Type.REPORTS, 20, new Color(16, 185, 129)),
                    lblAverageMarks, new JLabel("Based on recorded scores"),
                    gradeBg, new Color(16, 185, 129), false);

            lblOutstandingFees = new JLabel("$0.00");
            JPanel cardFees = createStatCard("Tuition Remaining",
                    new VectorIcon(VectorIcon.Type.FEES, 20, Color.WHITE),
                    lblOutstandingFees, new JLabel("Current outstanding balance"),
                    null, null, true);

            statsGrid.add(cardAttendance);
            statsGrid.add(cardGrades);
            statsGrid.add(cardFees);

            panel.add(statsGrid, BorderLayout.NORTH);
            return panel;
        }

        private JPanel createStatCard(String title, Icon icon, JLabel valLabel, JLabel subLabel,
                Color iconBg, Color iconFg, boolean isPrimaryBg) {
            JPanel card = new JPanel(new BorderLayout(10, 10)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(isPrimaryBg ? ThemeConstants.COLOR_PRIMARY : ThemeConstants.COLOR_CARD_BG);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2d.setColor(isPrimaryBg ? ThemeConstants.COLOR_PRIMARY : ThemeConstants.COLOR_BORDER);
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
                    g2d.setColor(isPrimaryBg ? new Color(255, 255, 255, 40) : iconBg);
                    g2d.fillOval(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            };
            iconContainer.setPreferredSize(new Dimension(36, 36));
            iconContainer.setOpaque(false);
            iconContainer.setLayout(new GridBagLayout());
            iconContainer.add(new JLabel(icon));
            topRow.add(iconContainer, BorderLayout.WEST);

            card.add(topRow, BorderLayout.NORTH);

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            JLabel titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setFont(new Font("Inter", Font.BOLD, 10));
            titleLabel.setForeground(isPrimaryBg ? new Color(221, 225, 255) : ThemeConstants.COLOR_TEXT_SECONDARY);

            valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
            valLabel.setForeground(isPrimaryBg ? Color.WHITE : ThemeConstants.COLOR_TEXT_PRIMARY);

            if (subLabel != null) {
                infoPanel.setLayout(new GridLayout(3, 1, 0, 2));
                subLabel.setFont(new Font("Inter", Font.PLAIN, 11));
                subLabel.setForeground(isPrimaryBg ? new Color(241, 245, 249) : ThemeConstants.COLOR_TEXT_SECONDARY);
                infoPanel.add(titleLabel);
                infoPanel.add(valLabel);
                infoPanel.add(subLabel);
            } else {
                infoPanel.setLayout(new GridLayout(2, 1, 0, 4));
                infoPanel.add(titleLabel);
                infoPanel.add(valLabel);
            }

            card.add(infoPanel, BorderLayout.CENTER);
            return card;
        }

        private JPanel createAttendancePanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new BorderLayout(10, 10));

            modelAttendance = new DefaultTableModel(new String[] { "Date", "Attendance Status" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableAttendance = new JTable(modelAttendance);
            ThemeConstants.styleTable(tableAttendance);

            tableAttendance.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(new Font("Inter", Font.PLAIN, 12));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return this;
                }
            });

            tableAttendance.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    String status = (value != null) ? value.toString().trim() : "Absent";
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                    panel.setOpaque(true);
                    if (isSelected) {
                        panel.setBackground(table.getSelectionBackground());
                    } else {
                        panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                    }

                    boolean isPresent = status.equalsIgnoreCase("Present");
                    Color bg = isPresent ? new Color(220, 252, 231) : new Color(254, 242, 242);
                    Color fg = isPresent ? new Color(22, 163, 74) : new Color(185, 28, 28);

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

            JScrollPane scrollPane = new JScrollPane(tableAttendance);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));

            card.add(scrollPane, BorderLayout.CENTER);
            panel.add(card, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createResultsPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new BorderLayout(10, 10));

            modelResults = new DefaultTableModel(new String[] { "Subject", "Marks Obtained", "Grade" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableResults = new JTable(modelResults);
            ThemeConstants.styleTable(tableResults);

            tableResults.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (value instanceof Integer) {
                        setText(value.toString() + "/100");
                    }
                    setFont(new Font("Inter", Font.PLAIN, 12));
                    setHorizontalAlignment(SwingConstants.CENTER);
                    return this;
                }
            });

            tableResults.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    String grade = (value != null) ? value.toString().trim() : "F";
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                    panel.setOpaque(true);
                    if (isSelected) {
                        panel.setBackground(table.getSelectionBackground());
                    } else {
                        panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                    }

                    String labelText = "Fail";
                    Color bg = new Color(254, 242, 242);
                    Color fg = new Color(185, 28, 28);

                    if (grade.equalsIgnoreCase("A")) {
                        labelText = "Distinction";
                        bg = new Color(220, 252, 231);
                        fg = new Color(22, 163, 74);
                    } else if (grade.equalsIgnoreCase("B") || grade.equalsIgnoreCase("C") || grade.equalsIgnoreCase("D")) {
                        labelText = "Pass";
                        bg = new Color(254, 237, 222);
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

                    JLabel label = new JLabel(labelText);
                    label.setFont(new Font("Inter", Font.BOLD, 10));
                    label.setForeground(finalFg);
                    badge.add(label);

                    panel.add(badge);
                    return panel;
                }
            });

            JScrollPane scrollPane = new JScrollPane(tableResults);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));

            card.add(scrollPane, BorderLayout.CENTER);
            panel.add(card, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createFeesPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new BorderLayout(10, 10));

            modelFees = new DefaultTableModel(
                    new String[] { "Billing Item", "Total Fee", "Paid Amount", "Remaining Balance", "Status" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableFees = new JTable(modelFees);
            ThemeConstants.styleTable(tableFees);

            // Money format renderers
            DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (value instanceof Double) {
                        setText(String.format("$%.2f", (Double) value));
                    }
                    setFont(new Font("Inter", Font.PLAIN, 12));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    return this;
                }
            };
            tableFees.getColumnModel().getColumn(1).setCellRenderer(moneyRenderer);
            tableFees.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);
            tableFees.getColumnModel().getColumn(3).setCellRenderer(moneyRenderer);

            // Fee status renderer
            tableFees.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    double remaining = 0;
                    double paid = 0;
                    if (row < table.getRowCount()) {
                        Object remObj = table.getModel().getValueAt(row, 3);
                        Object paidObj = table.getModel().getValueAt(row, 2);
                        if (remObj instanceof Double) remaining = (Double) remObj;
                        if (paidObj instanceof Double) paid = (Double) paidObj;
                    }

                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                    panel.setOpaque(true);
                    if (isSelected) {
                        panel.setBackground(table.getSelectionBackground());
                    } else {
                        panel.setBackground(row % 2 == 0 ? ThemeConstants.COLOR_CARD_BG : ThemeConstants.COLOR_BG);
                    }

                    String labelText = "Unpaid";
                    Color bg = new Color(254, 242, 242);
                    Color fg = new Color(185, 28, 28);

                    if (remaining == 0) {
                        labelText = "Settled";
                        bg = new Color(220, 252, 231);
                        fg = new Color(22, 163, 74);
                    } else if (paid > 0) {
                        labelText = "Partial";
                        bg = new Color(254, 237, 222);
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

                    JLabel label = new JLabel(labelText);
                    label.setFont(new Font("Inter", Font.BOLD, 10));
                    label.setForeground(finalFg);
                    badge.add(label);

                    panel.add(badge);
                    return panel;
                }
            });

            JScrollPane scrollPane = new JScrollPane(tableFees);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));

            card.add(scrollPane, BorderLayout.CENTER);
            panel.add(card, BorderLayout.CENTER);
            return panel;
        }

        private void refreshStats() {
            try (Connection conn = DatabaseHelper.getConnection()) {
                // 1. Attendance Rate
                String attendanceSql = "SELECT (COUNT(CASE WHEN status = 'Present' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0)) "
                        + "FROM attendance WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(attendanceSql)) {
                    pstmt.setString(1, rollNumber);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double rate = rs.getDouble(1);
                            if (rs.wasNull()) {
                                lblAttendanceRate.setText("100.0%");
                            } else {
                                lblAttendanceRate.setText(String.format("%.1f%%", rate));
                            }
                        }
                    }
                }

                // 2. Average Marks
                String marksSql = "SELECT AVG(CAST(marks AS FLOAT)) FROM results WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(marksSql)) {
                    pstmt.setString(1, rollNumber);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double avg = rs.getDouble(1);
                            if (rs.wasNull()) {
                                lblAverageMarks.setText("N/A");
                            } else {
                                lblAverageMarks.setText(String.format("%.1f%%", avg));
                            }
                        }
                    }
                }

                // 3. Outstanding Fees
                String feesSql = "SELECT remaining_fee FROM fees WHERE roll_number = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(feesSql)) {
                    pstmt.setString(1, rollNumber);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double rem = rs.getDouble(1);
                            lblOutstandingFees.setText(String.format("$%.2f", rem));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error refreshing student stats: " + e.getMessage());
            }
        }

        private void loadAttendance() {
            modelAttendance.setRowCount(0);
            String sql = "SELECT attendance_date, status FROM attendance WHERE roll_number = ? ORDER BY attendance_date DESC";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getDate("attendance_date").toString());
                        row.add(rs.getString("status"));
                        modelAttendance.addRow(row);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading student attendance log: " + e.getMessage());
            }
        }

        private void loadResults() {
            modelResults.setRowCount(0);
            String sql = "SELECT subject, marks, grade FROM results WHERE roll_number = ? ORDER BY subject ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString("subject"));
                        row.add(rs.getInt("marks"));
                        row.add(rs.getString("grade"));
                        modelResults.addRow(row);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading student results log: " + e.getMessage());
            }
        }

        private void loadFees() {
            modelFees.setRowCount(0);
            String sql = "SELECT total_fee, paid_fee, remaining_fee FROM fees WHERE roll_number = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add("Tuition Fee - Standard");
                        row.add(rs.getDouble("total_fee"));
                        row.add(rs.getDouble("paid_fee"));
                        row.add(rs.getDouble("remaining_fee"));
                        row.add(rs.getDouble("remaining_fee"));
                        modelFees.addRow(row);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading student fees: " + e.getMessage());
            }
        }

        private JPanel createSubjectsPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new BorderLayout(10, 10));

            modelSubjects = new DefaultTableModel(new String[] { "Subject Name" }, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            tableSubjects = new JTable(modelSubjects);
            ThemeConstants.styleTable(tableSubjects);

            tableSubjects.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(new Font("Inter", Font.BOLD, 12));
                    return this;
                }
            });

            JScrollPane scrollPane = new JScrollPane(tableSubjects);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER));

            card.add(scrollPane, BorderLayout.CENTER);
            panel.add(card, BorderLayout.CENTER);
            return panel;
        }

        private void loadSubjects() {
            modelSubjects.setRowCount(0);
            String sql = "SELECT subject_name FROM student_subjects WHERE roll_number = ? ORDER BY subject_name ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNumber);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString("subject_name"));
                        modelSubjects.addRow(row);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading student subjects: " + e.getMessage());
            }
        }

        private JPanel createChangePasswordPanel() {
            JPanel panel = new JPanel(new BorderLayout(15, 15));
            panel.setBackground(ThemeConstants.COLOR_BG);

            JPanel card = ThemeConstants.createCardPanel();
            card.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 15, 10, 15);
            gbc.weightx = 1.0;

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel lblTitle = new JLabel("Change Student Password");
            lblTitle.setFont(ThemeConstants.FONT_SECTION);
            lblTitle.setForeground(ThemeConstants.COLOR_PRIMARY);
            card.add(lblTitle, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("Current Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtCurrentPass = new JPasswordField();
            ThemeConstants.styleTextField(txtCurrentPass);
            card.add(txtCurrentPass, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("New Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtNewPass = new JPasswordField();
            ThemeConstants.styleTextField(txtNewPass);
            card.add(txtNewPass, gbc);

            gbc.gridy++;
            card.add(ThemeConstants.createLabel("Confirm New Password:"), gbc);

            gbc.gridy++;
            JPasswordField txtConfirmPass = new JPasswordField();
            ThemeConstants.styleTextField(txtConfirmPass);
            card.add(txtConfirmPass, gbc);

            gbc.gridy++;
            gbc.insets = new Insets(20, 15, 10, 15);
            JButton btnUpdate = new JButton("Update Password");
            ThemeConstants.styleButton(btnUpdate, ThemeConstants.COLOR_PRIMARY, Color.WHITE,
                    ThemeConstants.COLOR_PRIMARY_HOVER);
            card.add(btnUpdate, gbc);

            // Spacer
            gbc.gridy++;
            gbc.weighty = 1.0;
            card.add(Box.createGlue(), gbc);

            panel.add(card, BorderLayout.CENTER);

            btnUpdate.addActionListener(e -> {
                String current = new String(txtCurrentPass.getPassword());
                String newPass = new String(txtNewPass.getPassword());
                String confirm = new String(txtConfirmPass.getPassword());

                if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "Please fill in all fields.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!newPass.equals(confirm)) {
                    JOptionPane.showMessageDialog(panel, "New passwords do not match.", "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (parent.changeStudentPassword(rollNumber, current, newPass)) {
                    JOptionPane.showMessageDialog(panel, "Password changed successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    txtCurrentPass.setText("");
                    txtNewPass.setText("");
                    txtConfirmPass.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Incorrect current password.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            return panel;
        }
    }

    // ==========================================
    // 4. CONNECTION EXCEPTION POPUP EDITOR
    // ==========================================
    private static boolean showDbConfigDialog() {
        JTextField txtHost = new JTextField(DatabaseHelper.getHost());
        JTextField txtPort = new JTextField(DatabaseHelper.getPort());
        JTextField txtUser = new JTextField(DatabaseHelper.getUser());
        JPasswordField txtPass = new JPasswordField(DatabaseHelper.getPassword());

        Object[] message = {
                "Cannot connect to Microsoft SQL Server. Please check if your SQL Server is running.\nVerify connection parameters below:",
                "Host:", txtHost,
                "Port:", txtPort,
                "Username:", txtUser,
                "Password:", txtPass
        };

        int option = JOptionPane.showConfirmDialog(null, message,
                "Database Connection Configurator", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String newHost = txtHost.getText().trim();
            String newPort = txtPort.getText().trim();
            String newUser = txtUser.getText().trim();
            String newPass = new String(txtPass.getPassword());

            if (newHost.isEmpty() || newPort.isEmpty() || newUser.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Host, Port, and Username cannot be empty!",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return showDbConfigDialog();
            }

            if (DatabaseHelper.testConnection(newHost, newPort, newUser, newPass)) {
                DatabaseHelper.saveConfig(newHost, newPort, newUser, newPass);
                return true;
            } else {
                int retry = JOptionPane.showConfirmDialog(null,
                        "Connection failed with new parameters. Retry configuration?",
                        "Connection Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (retry == JOptionPane.YES_OPTION) {
                    return showDbConfigDialog();
                }
            }
        }
        return false; // user clicked cancel or closed
    }

    // ==========================================
    // MAIN RUNNER
    // ==========================================
    public static void main(String[] args) {
        // Attempt database initialization
        boolean dbReady = false;
        try {
            DatabaseHelper.initializeDatabase();
            dbReady = true;
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            // Show config dialog to edit connection credentials
            dbReady = showDbConfigDialog();
            if (dbReady) {
                try {
                    DatabaseHelper.initializeDatabase();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to initialize database structures: " + ex.getMessage(),
                            "Fatal Setup Error", JOptionPane.ERROR_MESSAGE);
                    dbReady = false;
                }
            }
        }

        if (!dbReady) {
            System.exit(1); // Exit if DB connection is unavailable
        }

        // Set modern Windows Look and Feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Custom tweaks for Nimbus Look and Feel to match style better
                    UIManager.put("nimbusBase", ThemeConstants.COLOR_PRIMARY);
                    UIManager.put("nimbusBlueGrey", ThemeConstants.COLOR_BORDER);
                    UIManager.put("control", ThemeConstants.COLOR_BG);
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback to system default
        }

        // Launch app GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new SchoolManagementSystem().setVisible(true);
        });
    }
}
