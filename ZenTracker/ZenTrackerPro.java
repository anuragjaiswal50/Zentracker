import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO; 
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI; 
import javax.swing.Timer; 
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * ZenTrackerPro — Multi-Page Premium Expense Tracker
 */
public class ZenTrackerPro {

    // ── Database ──────────────────────────────────────────────────────────────
    static final String DB_URL = "jdbc:mysql://localhost:3306/ZenTrackerDB";
    static final String DB_USER = "zentracker_app"; 
    static final String DB_PASS = "SecurePass123";

    // ── SECURITY ENGINE ───────────────────────────────────────────────────────
    static class Security {
        private static final byte[] KEY = "Z3nTr@ck3rS3cur3".getBytes(); 
        
        static String hash(String base) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(base.getBytes("UTF-8"));
                StringBuilder hex = new StringBuilder();
                for (byte b : hash) hex.append(String.format("%02x", b));
                return hex.toString();
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        
        static String encrypt(String val) {
            if (val == null || val.trim().isEmpty()) return val;
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(KEY, "AES"));
                return Base64.getEncoder().encodeToString(cipher.doFinal(val.getBytes("UTF-8")));
            } catch (Exception e) { return val; }
        }
        
        static String decrypt(String val) {
            if (val == null || val.trim().isEmpty()) return val;
            try {
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(KEY, "AES"));
                return new String(cipher.doFinal(Base64.getDecoder().decode(val)), "UTF-8");
            } catch (Exception e) { return val; } 
        }
    }

    // ── PALETTES & FONTS ──────────────────────────────────────────────────────
    static final Color D_BASE    = new Color(0x0A0A0C), D_PANEL   = new Color(0x111114), D_CARD    = new Color(0x18181C);
    static final Color D_RAISED  = new Color(0x202026), D_INPUT   = new Color(0x16161A), D_BDR     = new Color(0x26262C);
    static final Color D_BDR2    = new Color(0x333339), D_TXT1    = new Color(0xF2F2F7), D_TXT2    = new Color(0x8A8A96);
    static final Color D_TXT3    = new Color(0x6E6E7A), D_NAV     = new Color(0x0E0E11);

    static final Color L_BASE    = new Color(0xF0EFF6), L_PANEL   = new Color(0xFAF9FF), L_CARD    = new Color(0xFFFFFF);
    static final Color L_RAISED  = new Color(0xEDEBF8), L_INPUT   = new Color(0xF4F3FC), L_BDR     = new Color(0xDEDCF0);
    static final Color L_BDR2    = new Color(0xC8C5E8), L_TXT1    = new Color(0x16162A), L_TXT2    = new Color(0x68667E);
    static final Color L_TXT3    = new Color(0x8A8A96), L_NAV     = new Color(0xFFFFFF);

    static final Color PURPLE  = new Color(0x8B5CF6), PURPLE2 = new Color(0xA78BFA), PINK    = new Color(0xEC4899);
    static final Color CYAN    = new Color(0x06B6D4), GREEN   = new Color(0x10B981), RED     = new Color(0xF43F5E);
    static final Color AMBER   = new Color(0xF59E0B), INDIGO  = new Color(0x6366F1);

    static final Color[] CAT_COLORS = {
        new Color(0xEC4899), new Color(0x8B5CF6), new Color(0x06B6D4), new Color(0x10B981), new Color(0xF59E0B), 
        new Color(0xF43F5E), new Color(0x6366F1), new Color(0x14B8A6), new Color(0xF97316), new Color(0x84CC16), 
        new Color(0xA855F7), new Color(0x22D3EE), new Color(0xFB7185), new Color(0x34D399)
    };

    static final Font F_BODY   = new Font("Segoe UI", Font.PLAIN,  14); 
    static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD,   14); 
    static final Font F_H1     = new Font("Segoe UI", Font.BOLD,   26);
    static final Font F_H2     = new Font("Segoe UI", Font.BOLD,   18);
    static final Font F_H3     = new Font("Segoe UI", Font.BOLD,   15);
    static final Font F_SMALL  = new Font("Segoe UI", Font.BOLD,   12);
    static final Font F_TINY   = new Font("Segoe UI", Font.BOLD,   10); 
    static final Font F_MONO   = new Font("Consolas",  Font.BOLD,  15);
    static final Font F_MONO_S = new Font("Consolas",  Font.PLAIN, 13);
    static final Font F_EMOJI  = new Font("Segoe UI Emoji", Font.PLAIN, 15);

    static boolean dark = true;

    static Color base()   { return dark ? D_BASE   : L_BASE;   }
    static Color panel()  { return dark ? D_PANEL  : L_PANEL;  }
    static Color card()   { return dark ? D_CARD   : L_CARD;   }
    static Color raised() { return dark ? D_RAISED : L_RAISED; }
    static Color input()  { return dark ? D_INPUT  : L_INPUT;  }
    static Color bdr()    { return dark ? D_BDR    : L_BDR;    }
    static Color bdr2()   { return dark ? D_BDR2   : L_BDR2;   }
    static Color t1()     { return dark ? D_TXT1   : L_TXT1;   }
    static Color t2()     { return dark ? D_TXT2   : L_TXT2;   }
    static Color t3()     { return dark ? D_TXT3   : L_TXT3;   }
    static Color nav()    { return dark ? D_NAV    : L_NAV;    }

    static final Object[][] RANKS = {
        {"\uD83C\uDF0C","Galactic Finance God",0x0D1B2A,0x00B4D8,365},
        {"\uD83D\uDC51","King of Capital",      0x2D1B0E,0xFFD700,240},
        {"\uD83D\uDC09","Dragon of Discipline", 0x1A0A00,0xFF6B35,180},
        {"\uD83E\uDDD9","Wizard of Wealth",      0x1A0A2E,0x9B5DE5,120},
        {"\uD83D\uDC8E","Diamond Hands",         0x0A1628,0x48CAE4, 60},
        {"\uD83E\uDD47","Golden Piggy",          0x2A1800,0xFFD700, 30},
        {"\uD83E\uDD48","Silver Stasher",        0x1A1A1A,0xC0C0C0, 14},
        {"\uD83E\uDD49","Copper Hoarder",        0x1A0F00,0xCD7F32,  7},
        {"\uD83C\uDF31","Sprout Saver",          0x0A1A0A,0x52B788,  0}
    };

    static final String[] CATEGORIES = {
        "Food","Subscriptions","Skincare","Daily Needs","Movie",
        "Travel","Gifts","Medicines","Outfits","Fitness",
        "Gym","Haircare","Miscellaneous"
    };

    static void initDatabase() {
        try (Connection c = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(255) UNIQUE NOT NULL,password VARCHAR(255) NOT NULL,role ENUM('ADMIN','USER') DEFAULT 'USER',monthly_target DECIMAL(10,2) DEFAULT 3000.00,daily_limit DECIMAL(10,2) DEFAULT 0.00,streak_categories VARCHAR(255) DEFAULT 'Food,Miscellaneous',last_month_checked VARCHAR(7),theme VARCHAR(5) DEFAULT 'dark')");
            s.execute("CREATE TABLE IF NOT EXISTS transactions (id INT AUTO_INCREMENT PRIMARY KEY,user_id INT,amount DECIMAL(10,2) NOT NULL,category VARCHAR(50) NOT NULL,sub_category VARCHAR(255),note VARCHAR(255),type ENUM('Income','Expense') NOT NULL,trans_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(user_id))");
            s.execute("CREATE TABLE IF NOT EXISTS savings_goals (goal_id INT AUTO_INCREMENT PRIMARY KEY,user_id INT,goal_name VARCHAR(255) NOT NULL,target_amount DECIMAL(10,2) NOT NULL,saved_amount DECIMAL(10,2) DEFAULT 0.00,emoji VARCHAR(10) DEFAULT '\uD83C\uDFAF',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(user_id))");
            s.execute("CREATE TABLE IF NOT EXISTS budgets (budget_id INT AUTO_INCREMENT PRIMARY KEY,user_id INT,category VARCHAR(50) NOT NULL,budget_amount DECIMAL(10,2) NOT NULL,month_year VARCHAR(7),FOREIGN KEY(user_id) REFERENCES users(user_id))");
            s.execute("CREATE TABLE IF NOT EXISTS recurring (rec_id INT AUTO_INCREMENT PRIMARY KEY,user_id INT,amount DECIMAL(10,2),category VARCHAR(50),note VARCHAR(255),type ENUM('Income','Expense'),day_of_month INT DEFAULT 1,FOREIGN KEY(user_id) REFERENCES users(user_id))");
            s.execute("CREATE TABLE IF NOT EXISTS system_logs (log_id INT AUTO_INCREMENT PRIMARY KEY,action_description VARCHAR(255),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            for (String col : new String[]{ "email VARCHAR(255) UNIQUE", "sub_category VARCHAR(255)", "note VARCHAR(255)", "last_month_checked VARCHAR(7)", "daily_limit DECIMAL(10,2) DEFAULT 0.00", "streak_categories VARCHAR(255) DEFAULT 'Food,Miscellaneous'", "theme VARCHAR(5) DEFAULT 'dark'" }) 
                try { s.execute("ALTER TABLE users ADD COLUMN " + col); } catch (SQLException ignored) {}
            
            ResultSet rs = s.executeQuery("SELECT * FROM users WHERE username='" + Security.encrypt("admin") + "'");
            if (!rs.next()) s.execute("INSERT INTO users (username,email,password,role) VALUES ('" + Security.encrypt("admin") + "','" + Security.encrypt("admin@zen.com") + "','" + Security.hash("admin123") + "','ADMIN')");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    static Connection conn() throws SQLException { return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); }

    // ── UI FACTORIES ──
    static BufferedImage getAppIcon(int sz) {
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, PURPLE, sz, sz, PINK)); g2.fillRoundRect(0, 0, sz, sz, sz/4, sz/4);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, sz/2)); FontMetrics fm = g2.getFontMetrics();
        g2.drawString("Z", (sz - fm.stringWidth("Z")) / 2, (sz + fm.getAscent() - fm.getDescent()) / 2); g2.dispose(); return img;
    }

    static JButton primaryBtn(String text) {
        JButton b = new JButton(text) {
            float glow = 0;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){ glow=.28f; repaint(); }
                public void mouseExited (MouseEvent e){ glow=0;    repaint(); }
                public void mousePressed(MouseEvent e){ glow=.5f;  repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color a=blend(PURPLE,Color.WHITE,glow), b2=blend(PINK,Color.WHITE,glow);
                g2.setPaint(new GradientPaint(0,0,a,getWidth(),getHeight(),b2)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if(glow>0){g2.setColor(new Color(139,92,246,(int)(glow*90)));g2.setStroke(new BasicStroke(2.5f));g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);}
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI Emoji",Font.BOLD,14)); b.setForeground(Color.WHITE); b.setContentAreaFilled(false); 
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false); b.setBorder(new EmptyBorder(11,26,11,26)); 
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    static Color blend(Color c, Color w, float f) {
        return new Color(Math.min(255,(int)(c.getRed()+(w.getRed()-c.getRed())*f)), Math.min(255,(int)(c.getGreen()+(w.getGreen()-c.getGreen())*f)), Math.min(255,(int)(c.getBlue()+(w.getBlue()-c.getBlue())*f)));
    }

    static JButton ghostBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(bdr2()); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setBackground(raised()); b.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13)); b.setForeground(t1());
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
        b.setBorder(new EmptyBorder(9,16,9,16)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color h=dark?new Color(0x2C2C34):new Color(0xE4E2F4);
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(h);b.repaint();}
            public void mouseExited (MouseEvent e){b.setBackground(raised());b.repaint();}
        }); return b;
    }

    static JButton dangerBtn(String t) { JButton b=ghostBtn(t); b.setForeground(RED); return b; }

    static JTextField field() {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(input()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(isFocusOwner()?PURPLE:bdr()); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        f.setFont(F_BODY); f.setBackground(input()); f.setForeground(t1()); f.setCaretColor(PURPLE2); 
        f.setBorder(new EmptyBorder(9,12,9,12)); f.setOpaque(false); f.setPreferredSize(new Dimension(0,38)); return f;
    }

    static JPasswordField passField() {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(input()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(isFocusOwner()?PURPLE:bdr()); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        f.setFont(F_BODY); f.setBackground(input()); f.setForeground(t1()); f.setCaretColor(PURPLE2); 
        f.setBorder(new EmptyBorder(9,12,9,12)); f.setOpaque(false); return f;
    }
    
    static JPanel createPasswordWrapper(JPasswordField pField) {
        JPanel pw=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics gg){
                Graphics2D g2=(Graphics2D)gg.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(input()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(pField.isFocusOwner()?PURPLE:bdr()); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        pw.setOpaque(false); pField.setBorder(new EmptyBorder(9,12,9,6));
        JButton eye=new JButton("\uD83D\uDC41"){
            @Override protected void paintComponent(Graphics gg){
                Graphics2D g2=(Graphics2D)gg.create(); g2.setColor(input()); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(gg);
            }
        };
        eye.setFont(new Font("Segoe UI Emoji",Font.PLAIN,15)); eye.setBorder(new EmptyBorder(2,8,2,10));
        eye.setContentAreaFilled(false); eye.setFocusPainted(false); eye.setOpaque(false);
        eye.setForeground(t2()); eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addActionListener(e->pField.setEchoChar(pField.getEchoChar()==(char)0?'•':(char)0));
        pw.add(pField,BorderLayout.CENTER); pw.add(eye,BorderLayout.EAST); pw.setPreferredSize(new Dimension(0,38)); return pw;
    }

    static JComboBox<String> combo(String[] items) {
        JComboBox<String> b = new JComboBox<>(items) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(input()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.setColor(bdr()); g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        b.setBackground(input()); b.setForeground(t1()); b.setFont(F_BODY); b.setOpaque(false); b.setBorder(new EmptyBorder(4,8,4,4));
        b.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean foc){
                super.getListCellRendererComponent(l,v,i,sel,foc);
                setBackground(sel?PURPLE:raised()); setForeground(sel?Color.WHITE:t1());
                setOpaque(true); setBorder(new EmptyBorder(6,12,6,12)); return this;
            }
        }); return b;
    }

    static JComboBox<String> editableCombo(String[] items) {
        JComboBox<String> b = combo(items); b.setEditable(true);
        JTextField ed=(JTextField)b.getEditor().getEditorComponent();
        ed.setBackground(input()); ed.setForeground(t1()); ed.setCaretColor(PURPLE2);
        ed.setFont(F_BODY); ed.setOpaque(true); ed.setBorder(new EmptyBorder(4,4,4,4)); return b;
    }

    static JLabel pill(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        l.setFont(F_TINY); l.setBackground(bg); l.setForeground(fg); l.setOpaque(false); l.setBorder(new EmptyBorder(3,8,3,8)); return l;
    }

    static JLabel micro(String t) { JLabel l=new JLabel(t.toUpperCase()); l.setFont(F_TINY); l.setForeground(t3()); return l; }

    static JLabel logoLabel(int sz) {
        JLabel l=new JLabel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,PURPLE,sz,sz,PINK)); g2.fillRoundRect(0,0,sz,sz,sz/4,sz/4);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,sz/2)); FontMetrics fm=g2.getFontMetrics();
                g2.drawString("Z",(sz-fm.stringWidth("Z"))/2,(sz+fm.getAscent()-fm.getDescent())/2); g2.dispose();
            }
        }; l.setPreferredSize(new Dimension(sz,sz)); return l;
    }

    static JPanel cardPanel(LayoutManager lm) {
        JPanel p=new JPanel(lm){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(card()); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.setColor(bdr()); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); g2.dispose();
            }
        }; p.setBackground(card()); p.setOpaque(false); return p;
    }

    static JPanel statChip(String lbl, JLabel val, Color accent) {
        JPanel chip=new JPanel(new GridLayout(2,1,0,4)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,raised(),getWidth(),getHeight(),dark?new Color(0x222228):new Color(0xEAE8F8)));
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),60)));
                g2.fillRoundRect(0,0,3,getHeight()-1,3,3); g2.setColor(bdr()); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.dispose();
            }
        };
        chip.setOpaque(false); chip.setBorder(new EmptyBorder(13,16,13,16));
        JLabel lb=new JLabel(lbl); lb.setFont(F_TINY); lb.setForeground(t3()); chip.add(lb); chip.add(val); return chip;
    }

    static JTable styledTable(DefaultTableModel model) {
        JTable t=new JTable(model); t.setFont(new Font("Segoe UI", Font.PLAIN, 15)); t.setRowHeight(48); t.setShowVerticalLines(false);
        t.setGridColor(bdr()); t.setIntercellSpacing(new Dimension(0,1)); t.setBackground(card()); t.setForeground(t1());
        t.setSelectionBackground(dark?new Color(0x2D1B69):new Color(0xD8D0F7)); t.setSelectionForeground(t1()); 
        
        JTableHeader h = t.getTableHeader(); h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBackground(raised()); setForeground(t2()); setFont(F_TINY);
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, bdr()), new EmptyBorder(12, 16, 12, 16)));
                return this;
            }
        }); return t;
    }

    static JScrollPane scroll(Component c) {
        JScrollPane sp=new JScrollPane(c); sp.setBorder(null); sp.getViewport().setBackground(card());
        sp.getVerticalScrollBar().setBackground(base()); sp.getVerticalScrollBar().setUnitIncrement(16); return sp;
    }

    static void applyUIDefaults() {
        UIManager.put("Panel.background", card()); UIManager.put("Label.foreground", t1());
        UIManager.put("TextField.background", input()); UIManager.put("TextField.foreground", t1());
        UIManager.put("TextField.caretForeground",PURPLE2); UIManager.put("PasswordField.background",input());
        UIManager.put("PasswordField.foreground",t1()); UIManager.put("ComboBox.background", input());
        UIManager.put("ComboBox.foreground", t1()); UIManager.put("ComboBox.selectionBackground",PURPLE);
        UIManager.put("ComboBox.selectionForeground",Color.WHITE); UIManager.put("List.background", raised());
        UIManager.put("List.foreground", t1()); UIManager.put("List.selectionBackground",PURPLE);
        UIManager.put("List.selectionForeground",Color.WHITE); UIManager.put("PopupMenu.background", raised());
        UIManager.put("PopupMenu.foreground", t1()); UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(bdr2(),1));
        UIManager.put("MenuItem.background", raised()); UIManager.put("MenuItem.foreground", t1());
        UIManager.put("MenuItem.selectionBackground",PURPLE); UIManager.put("ScrollPane.background", card());
        UIManager.put("Viewport.background", card()); UIManager.put("ScrollBar.background", card());
        UIManager.put("ScrollBar.thumb", bdr2()); UIManager.put("ScrollBar.track", base());
        UIManager.put("Table.background", card()); UIManager.put("Table.foreground", t1());
        UIManager.put("TableHeader.background", raised()); UIManager.put("TableHeader.foreground", t3());
        UIManager.put("TabbedPane.background", base()); UIManager.put("TabbedPane.foreground", t2());
        UIManager.put("TabbedPane.selected", card()); UIManager.put("OptionPane.background", card());
        UIManager.put("OptionPane.messageForeground",t1()); UIManager.put("Button.background", raised());
        UIManager.put("Button.foreground", t1()); UIManager.put("CheckBox.background", card());
        UIManager.put("CheckBox.foreground", t1()); UIManager.put("ToolTip.background", raised());
        UIManager.put("ToolTip.foreground", t1()); UIManager.put("Spinner.background", input());
        UIManager.put("Spinner.foreground", t1());
    }

    // ── CUSTOM STYLED MESSAGE DIALOG ──
    static void showMessage(Component parent, String title, String msg, boolean isSuccess) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true); d.setSize(360, 200); d.setLocationRelativeTo(parent);
        
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(panel());
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(24,24,24,24)));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(0,0,12,0); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        
        int r=0;
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER); lblTitle.setFont(F_H2); lblTitle.setForeground(t1());
        gbc.gridy=r++; p.add(lblTitle, gbc);
        
        JLabel lblMsg = new JLabel("<html><center>" + msg.replaceAll("\n", "<br>") + "</center></html>", SwingConstants.CENTER); 
        lblMsg.setFont(F_BODY); lblMsg.setForeground(isSuccess ? GREEN : t2());
        gbc.gridy=r++; gbc.insets = new Insets(0,0,24,0); p.add(lblMsg, gbc);
        
        JButton ok = primaryBtn("OK"); ok.addActionListener(e -> d.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); bp.setOpaque(false); bp.add(ok);
        gbc.gridy=r++; gbc.insets = new Insets(0,0,0,0); p.add(bp, gbc);
        
        d.add(p); d.setVisible(true);
    }

    // ── REAL EMAIL SENDER ──
    static void sendOTPEmail(String recipientEmail, String otp) throws Exception {
        // REPLACE THESE WITH YOUR OWN GMAIL CREDENTIALS
        final String senderEmail = "anuragjais358@gmail.com"; 

       final String senderPassword = "tlasjdyrmaboyijy"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() { 
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session); 
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("ZenTracker - Security Verification");
        message.setText("Your ZenTracker OTP is: " + otp + "\n\nDo not share this code with anyone.");
        Transport.send(message);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        
        try {
            File desktop = new File(System.getProperty("user.home"), "Desktop");
            if (!desktop.exists()) desktop = new File(System.getProperty("user.home"));
            ImageIO.write(getAppIcon(1024), "PNG", new File(desktop, "ZenTracker_HD_Icon.png"));
            Taskbar.getTaskbar().setIconImage(getAppIcon(64));
        } catch (Exception ignored) {} 
        
        applyUIDefaults();
        initDatabase();
        SwingUtilities.invokeLater(() -> new AuthFrame().setVisible(true));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AUTH FRAME (Multi-Panel Architecture)
    // ══════════════════════════════════════════════════════════════════════════
    static class AuthFrame extends JFrame {
        
        JTextField loginUserField = field(), regUserField = field(), regEmailField = field();
        JPasswordField loginPassField = passField(), regPassField = passField();
        JTextField forgotEmailField = field(), forgotOtpField = field();
        JPasswordField forgotPassField = passField();
        
        String generatedOtp = null, verifyingEmail = null;
        CardLayout authCardLayout = new CardLayout();
        JPanel authContainer = new JPanel(authCardLayout);

        AuthFrame() {
            setTitle("ZenTracker"); setIconImage(getAppIcon(64)); setSize(460, 680); setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE); setLayout(new BorderLayout());
            
            JPanel bg = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create(); g2.setColor(base()); g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setPaint(new RadialGradientPaint(new Point2D.Float(60,70),200,new float[]{0f,1f},new Color[]{new Color(139,92,246,60),new Color(0,0,0,0)}));
                    g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth()-50,getHeight()-80),180,new float[]{0f,1f},new Color[]{new Color(236,72,153,45),new Color(0,0,0,0)}));
                    g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
                }
            };
            bg.setBackground(base());
            
            authContainer.setOpaque(false);
            authContainer.add(buildLoginCard(), "LOGIN");
            authContainer.add(buildRegisterCard(), "REGISTER");
            authContainer.add(buildForgotCard(), "FORGOT");

            bg.add(authContainer); add(bg, BorderLayout.CENTER);

            JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,16,10)); footer.setBackground(base());
            JLabel al=new JLabel("Admin Portal  →"); al.setFont(new Font("Segoe UI",Font.PLAIN,12));
            al.setForeground(t3()); al.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            al.addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){al.setForeground(t2());}
                public void mouseExited (MouseEvent e){al.setForeground(t3());}
                public void mouseClicked(MouseEvent e){openAdmin();}
            });
            footer.add(al); add(footer,BorderLayout.SOUTH);
            
            authCardLayout.show(authContainer, "LOGIN");
        }
        
        JPanel createBaseAuthCard() {
            JPanel card = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    for(int i=6;i>0;i--){ g2.setColor(new Color(0,0,0,9*i)); g2.fillRoundRect(i,i+4,getWidth()-i*2,getHeight()-i*2,20,20); }
                    g2.setColor(card()); g2.fillRoundRect(0,0,getWidth()-5,getHeight()-5,20,20);
                    g2.setPaint(new GradientPaint(40,1,PURPLE,getWidth()-45,1,PINK));
                    g2.setStroke(new BasicStroke(2f)); g2.drawLine(40,1,getWidth()-45,1);
                    g2.setColor(bdr()); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-6,getHeight()-6,20,20); g2.dispose();
                }
            };
            card.setOpaque(false); card.setPreferredSize(new Dimension(380, 560)); card.setBorder(new EmptyBorder(40, 42, 36, 42)); return card;
        }

        JPanel buildLoginCard() {
            JPanel card = createBaseAuthCard(); GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;

            JPanel brand=new JPanel(new FlowLayout(FlowLayout.CENTER,8,0)); brand.setOpaque(false); brand.add(logoLabel(32));
            JLabel name=new JLabel("ZenTracker"); name.setFont(F_H1); name.setForeground(t1()); brand.add(name);
            JLabel tag=new JLabel("Mindful money · Zero stress",SwingConstants.CENTER); tag.setFont(new Font("Segoe UI",Font.PLAIN,13)); tag.setForeground(t2());

            JPanel pw = createPasswordWrapper(loginPassField);
            JButton loginBtn=primaryBtn("Sign In  →"); loginBtn.setPreferredSize(new Dimension(0,44)); loginBtn.addActionListener(e -> login());
            
            JPanel linkRow=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0)); linkRow.setOpaque(false);
            JLabel lt=new JLabel("New here?"); lt.setFont(F_BODY); lt.setForeground(t2());
            JLabel rl=new JLabel("Create account"); rl.setFont(F_BOLD); rl.setForeground(PURPLE2); rl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rl.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){ authCardLayout.show(authContainer, "REGISTER"); }});
            linkRow.add(lt); linkRow.add(rl);
            
            JLabel forgotLbl = new JLabel("Forgot Password?"); forgotLbl.setFont(F_SMALL); forgotLbl.setForeground(t3());
            forgotLbl.setHorizontalAlignment(SwingConstants.RIGHT); forgotLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            forgotLbl.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){ authCardLayout.show(authContainer, "FORGOT"); }});

            int r=0; g.gridy=r++;g.insets=new Insets(0,0,4,0); card.add(brand,g);
            g.gridy=r++;g.insets=new Insets(0,0,30,0); card.add(tag,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("username or email"),g);
            g.gridy=r++;g.insets=new Insets(0,0,14,0); card.add(loginUserField,g);
            
            JPanel pLabelRow = new JPanel(new BorderLayout()); pLabelRow.setOpaque(false);
            pLabelRow.add(micro("password"), BorderLayout.WEST); pLabelRow.add(forgotLbl, BorderLayout.EAST);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(pLabelRow,g);
            
            g.gridy=r++;g.insets=new Insets(0,0,26,0); card.add(pw,g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(loginBtn,g);
            g.gridy=r++;g.insets=new Insets(0,0,0,0); card.add(linkRow,g); return card;
        }
        
        JPanel buildRegisterCard() {
            JPanel card = createBaseAuthCard(); GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;

            JLabel name=new JLabel("Create Account", SwingConstants.CENTER); name.setFont(F_H1); name.setForeground(t1()); 
            JPanel pw = createPasswordWrapper(regPassField);
            JButton regBtn=primaryBtn("Sign Up  →"); regBtn.setPreferredSize(new Dimension(0,44)); regBtn.addActionListener(e -> register());
            
            JPanel linkRow=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0)); linkRow.setOpaque(false);
            JLabel lt=new JLabel("Already have an account?"); lt.setFont(F_BODY); lt.setForeground(t2());
            JLabel rl=new JLabel("Sign in"); rl.setFont(F_BOLD); rl.setForeground(PURPLE2); rl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rl.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){ authCardLayout.show(authContainer, "LOGIN"); }});
            linkRow.add(lt); linkRow.add(rl);

            int r=0; g.gridy=r++;g.insets=new Insets(0,0,24,0); card.add(name,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("username"),g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(regUserField,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("email id"),g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(regEmailField,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("password"),g);
            g.gridy=r++;g.insets=new Insets(0,0,26,0); card.add(pw,g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(regBtn,g);
            g.gridy=r++;g.insets=new Insets(0,0,0,0); card.add(linkRow,g); return card;
        }
        
        JPanel buildForgotCard() {
            JPanel card = createBaseAuthCard(); GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;

            JLabel name=new JLabel("Reset Password", SwingConstants.CENTER); name.setFont(F_H1); name.setForeground(t1()); 
            JLabel tag=new JLabel("Verify your email to continue",SwingConstants.CENTER); tag.setFont(new Font("Segoe UI",Font.PLAIN,13)); tag.setForeground(t2());
            JPanel pw = createPasswordWrapper(forgotPassField);
            
            forgotOtpField.setEnabled(false); forgotPassField.setEnabled(false);
            JButton sendOtpBtn=ghostBtn("Send OTP"); sendOtpBtn.setPreferredSize(new Dimension(100, 38));
            
            JPanel emailRow = new JPanel(new BorderLayout(8,0)); emailRow.setOpaque(false);
            emailRow.add(forgotEmailField, BorderLayout.CENTER); emailRow.add(sendOtpBtn, BorderLayout.EAST);

            JButton resetBtn=primaryBtn("Update Password"); resetBtn.setPreferredSize(new Dimension(0,44)); resetBtn.setEnabled(false);
            
            sendOtpBtn.addActionListener(e -> {
                String mail = forgotEmailField.getText().trim();
                if(mail.isEmpty()) { showMessage(this, "Error", "Enter email first.", false); return; } 
                String encMail = Security.encrypt(mail);
                
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT 1 FROM users WHERE email=?")){
                    ps.setString(1, encMail);
                    if(ps.executeQuery().next()) {
                        generatedOtp = String.format("%06d", new Random().nextInt(999999));
                        verifyingEmail = encMail;
                        
                        try {
                            sendOTPEmail(mail, generatedOtp); 
                            forgotOtpField.setEnabled(true); forgotPassField.setEnabled(true);
                            resetBtn.setEnabled(true); forgotEmailField.setEnabled(false); sendOtpBtn.setEnabled(false);
                            showMessage(this, "Email Verification", "OTP successfully sent to your email:\n" + mail, true); 
                        } catch (Exception ex) {
                            showMessage(this, "Email Error", "Failed to send email.\nPlease check your App Password settings.", false); 
                            ex.printStackTrace();
                        }
                    } else { showMessage(this, "Error", "No account found with this email.", false); }
                }catch(Exception ex){ex.printStackTrace();}
            });
            
            resetBtn.addActionListener(e -> {
                if(!forgotOtpField.getText().trim().equals(generatedOtp)) { showMessage(this, "Error", "Invalid OTP.", false); return; }
                String newPass = new String(forgotPassField.getPassword()).trim();
                if(newPass.isEmpty()) { showMessage(this, "Error", "Enter a new password.", false); return; } 
                
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("UPDATE users SET password=? WHERE email=?")){
                    ps.setString(1, Security.hash(newPass)); ps.setString(2, verifyingEmail); ps.executeUpdate();
                    showMessage(this, "Success", "Password updated successfully!\nYou can now log in.", true); 
                    
                    forgotEmailField.setText(""); forgotEmailField.setEnabled(true); forgotOtpField.setText(""); forgotOtpField.setEnabled(false);
                    forgotPassField.setText(""); forgotPassField.setEnabled(false); sendOtpBtn.setEnabled(true); resetBtn.setEnabled(false);
                    authCardLayout.show(authContainer, "LOGIN");
                }catch(Exception ex){ex.printStackTrace();}
            });
            
            JPanel linkRow=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0)); linkRow.setOpaque(false);
            JLabel rl=new JLabel("← Back to Login"); rl.setFont(F_BOLD); rl.setForeground(PURPLE2);
            rl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rl.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){ authCardLayout.show(authContainer, "LOGIN"); }});
            linkRow.add(rl);

            int r=0; g.gridy=r++;g.insets=new Insets(0,0,4,0); card.add(name,g);
            g.gridy=r++;g.insets=new Insets(0,0,24,0); card.add(tag,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("registered email"),g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(emailRow,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("6-digit otp"),g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(forgotOtpField,g);
            g.gridy=r++;g.insets=new Insets(0,0,3,0); card.add(micro("new password"),g);
            g.gridy=r++;g.insets=new Insets(0,0,26,0); card.add(pw,g);
            g.gridy=r++;g.insets=new Insets(0,0,12,0); card.add(resetBtn,g);
            g.gridy=r++;g.insets=new Insets(0,0,0,0); card.add(linkRow,g); return card;
        }

        String[] showStyledAdminLogin() {
            JDialog dlg = new JDialog(this, "Admin Portal", true); dlg.setUndecorated(true); dlg.setSize(400, 320); dlg.setLocationRelativeTo(this);
            JPanel p = new JPanel(new GridBagLayout()); p.setBackground(panel()); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(0,0,10,0); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth=2;

            int is = 50; BufferedImage img = new BufferedImage(is, is, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, PURPLE, is, is, PINK)); g2.fillRoundRect(0, 0, is, is, is/4, is/4);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, is/2)); FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Z", (is - fm.stringWidth("Z")) / 2, (is + fm.getAscent() - fm.getDescent()) / 2); g2.dispose();
            
            gbc.gridy=0; p.add(new JLabel(new ImageIcon(img), SwingConstants.CENTER), gbc);
            gbc.gridy=1; JLabel title = new JLabel("Admin Access", SwingConstants.CENTER); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(t1()); p.add(title, gbc);
            gbc.gridy=2; gbc.gridwidth=1; gbc.weightx=0.3; p.add(micro("ADMIN USERNAME"), gbc);
            JTextField tfUser = field(); gbc.gridx=1; gbc.weightx=0.7; p.add(tfUser, gbc);
            gbc.gridx=0; gbc.gridy=3; gbc.weightx=0.3; p.add(micro("PASSWORD"), gbc);
            JPasswordField tfPass = passField(); gbc.gridx=1; gbc.weightx=0.7; p.add(tfPass, gbc);

            final String[] out = new String[]{null, null};
            JButton ok = primaryBtn("OK"); ok.addActionListener(e -> { out[0]=tfUser.getText(); out[1]=new String(tfPass.getPassword()); dlg.dispose(); });
            JButton cancel = ghostBtn("Cancel"); cancel.addActionListener(e -> dlg.dispose());
            
            gbc.gridy=4; gbc.gridx=0; gbc.gridwidth=1; gbc.weightx=0.5; gbc.insets = new Insets(10,10,0,10); p.add(cancel, gbc);
            gbc.gridx=1; p.add(ok, gbc); dlg.add(p); dlg.setVisible(true); return out;
        }

        void login() {
            String uInput = loginUserField.getText().trim(), pInput = new String(loginPassField.getPassword()).trim();
            if(uInput.isEmpty() || pInput.isEmpty()) { showMessage(this, "Error", "Enter credentials.", false); return; }
            String encInput = Security.encrypt(uInput), hashedP = Security.hash(pInput);
            
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT user_id, username, role, theme FROM users WHERE (username=? OR email=?) AND password=?")){
                ps.setString(1,encInput); ps.setString(2,encInput); ps.setString(3,hashedP); ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    if("ADMIN".equals(rs.getString("role"))){showMessage(this, "Access Denied", "Use Admin Portal.", false); return;}
                    dark="dark".equals(rs.getString("theme")); applyUIDefaults();
                    String decUname = Security.decrypt(rs.getString("username"));
                    dispose(); new MainFrame(rs.getInt("user_id"), decUname).setVisible(true);
                } else showMessage(this, "Error", "Incorrect credentials.", false);
            }catch(SQLException ex){showMessage(this, "Error", "DB Error: "+ex.getMessage(), false);}
        }

        void register() {
            String rawU = regUserField.getText().trim(), rawE = regEmailField.getText().trim(), rawP = new String(regPassField.getPassword()).trim();
            if(rawU.isEmpty()||rawE.isEmpty()||rawP.isEmpty()){showMessage(this, "Error", "Fill in all fields.", false); return;}
            if(!rawE.contains("@")){showMessage(this, "Error", "Invalid email format.", false); return;}
            
            String u = Security.encrypt(rawU), e = Security.encrypt(rawE), p = Security.hash(rawP);
            
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO users (username,email,password,monthly_target) VALUES (?,?,?,3000)")){
                ps.setString(1,u);ps.setString(2,e);ps.setString(3,p);ps.executeUpdate();
                showMessage(this, "Welcome to ZenTracker!", "Account created successfully.\nYou can now sign in.", true);
                regUserField.setText(""); regEmailField.setText(""); regPassField.setText(""); authCardLayout.show(authContainer, "LOGIN");
            }catch(SQLException ex){ showMessage(this, "Error", "Username or Email already taken.", false); }
        }

        void openAdmin() {
            String[] credentials = showStyledAdminLogin();
            String unameInput = credentials[0], passInput = credentials[1];
            if (unameInput != null && passInput != null && !unameInput.isEmpty() && !passInput.isEmpty()) {
                String u = Security.encrypt(unameInput.trim()), p = Security.hash(passInput.trim());
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT user_id FROM users WHERE username=? AND password=? AND role='ADMIN'")){
                    ps.setString(1,u);ps.setString(2,p);
                    if(ps.executeQuery().next()){
                        try(PreparedStatement logPs = c.prepareStatement("INSERT INTO system_logs (action_description) VALUES (?)")) { logPs.setString(1, "Admin [" + unameInput + "] logged in successfully."); logPs.executeUpdate(); } catch(Exception ignored) {}
                        dispose();new AdminFrame().setVisible(true);
                    } else {
                        try(PreparedStatement logPs = c.prepareStatement("INSERT INTO system_logs (action_description) VALUES (?)")) { logPs.setString(1, "Failed login attempt for username: " + unameInput); logPs.executeUpdate(); } catch(Exception ignored) {}
                        showMessage(this, "Error", "Invalid Admin Credentials.", false);
                    }
                }catch(SQLException ex){ex.printStackTrace();}
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ADMIN FRAME
    // ══════════════════════════════════════════════════════════════════════════
    static class AdminFrame extends JFrame {
        AdminFrame() {
            setTitle("ZenTracker · Admin"); setIconImage(getAppIcon(64)); setSize(1000,680); setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE); setLayout(new BorderLayout()); getContentPane().setBackground(base());

            JPanel headerBar = new JPanel(new BorderLayout()); headerBar.setBackground(panel()); 
            headerBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()), new EmptyBorder(15, 25, 15, 25)));
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); left.setOpaque(false); left.add(pill("ADMIN", new Color(0x2D1B69), PURPLE2)); 
            JLabel ttl = new JLabel("Control Center"); ttl.setFont(F_H2); ttl.setForeground(t1()); left.add(ttl);
            JButton logout = ghostBtn("← Logout"); logout.addActionListener(e->{ dispose(); new AuthFrame().setVisible(true); });
            headerBar.add(left, BorderLayout.WEST); headerBar.add(logout, BorderLayout.EAST); add(headerBar, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT); tabs.setUI(new DarkTabbedPaneUI());
            tabs.addTab("Users", getAdminIcon("USER", t2()), buildTable("SELECT user_id,username,role,monthly_target,daily_limit FROM users"));
            tabs.addTab("System Logs", getAdminIcon("LOG", t2()), buildTable("SELECT * FROM system_logs ORDER BY created_at DESC"));
            tabs.addTab("All Transactions", getAdminIcon("TRANS", t2()), buildTable("SELECT t.id,u.username,t.amount,t.category,t.type,t.trans_date FROM transactions t JOIN users u ON t.user_id=u.user_id ORDER BY t.trans_date DESC"));
            
            JPanel w=new JPanel(new BorderLayout()); w.setBackground(base()); w.setBorder(new EmptyBorder(18,18,18,18)); w.add(tabs); add(w,BorderLayout.CENTER);
        }

        Icon getAdminIcon(String type, Color c) {
            int sz = 16; BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if(type.equals("USER")) { g2.drawOval(sz/4, 2, sz/2, sz/2); g2.drawArc(1, sz/2+2, sz-2, sz/2-2, 0, 180); }
            else if(type.equals("LOG")) { g2.drawRoundRect(2, 1, sz-4, sz-2, 2, 2); for(int i=0;i<3;i++) g2.drawLine(6, 6+i*3, sz-6, 6+i*3); }
            else if(type.equals("TRANS")) { g2.drawRoundRect(1, 2, sz-2, sz-4, 2, 2); g2.drawLine(4, sz/2, sz-4, sz/2); g2.drawLine(sz/2, 5, sz/2, sz-5); }
            g2.dispose(); return new ImageIcon(img);
        }

        JPanel buildTable(String sql){
            JPanel p=new JPanel(new BorderLayout()); p.setBackground(card());
            DefaultTableModel m=new DefaultTableModel(){public boolean isCellEditable(int r,int c){return false;}};
            JTable t=styledTable(m); t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); 
            DefaultTableCellRenderer cr=new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable tb,Object v,boolean is,boolean hf,int row,int col){
                    super.getTableCellRendererComponent(tb,v,is,hf,row,col);
                    setBackground(is?(dark?new Color(0x2D1B69):new Color(0xD8D0F7)):(row%2==0?card():raised()));
                    setForeground(t1()); setOpaque(true); setBorder(new EmptyBorder(0,16,0,16)); return this;
                }
            };
            try(Connection c2=conn();Statement st=c2.createStatement();ResultSet rs=st.executeQuery(sql)){
                ResultSetMetaData md=rs.getMetaData();
                for(int i=1;i<=md.getColumnCount();i++) m.addColumn(md.getColumnName(i));
                while(rs.next()){
                    Object[] row=new Object[md.getColumnCount()];
                    for(int i=1;i<=md.getColumnCount();i++) {
                        Object val = rs.getObject(i); String colName = md.getColumnName(i);
                        if(val instanceof String && (colName.equals("username") || colName.equals("note") || colName.equals("sub_category") || colName.equals("goal_name"))) val = Security.decrypt((String)val);
                        row[i-1] = val;
                    } m.addRow(row);
                }
            }catch(SQLException e){e.printStackTrace();}
            for(int i=0; i<t.getColumnCount(); i++) {
                t.getColumnModel().getColumn(i).setCellRenderer(cr); String colName = t.getColumnName(i).toUpperCase();
                if(colName.contains("ID")) { t.getColumnModel().getColumn(i).setMaxWidth(60); }
                else if(colName.contains("DATE") || colName.contains("CREATED")) { t.getColumnModel().getColumn(i).setMinWidth(180); t.getColumnModel().getColumn(i).setMaxWidth(180); }
                else if(colName.contains("AMOUNT") || colName.contains("TARGET") || colName.contains("LIMIT")) { t.getColumnModel().getColumn(i).setMinWidth(130); t.getColumnModel().getColumn(i).setMaxWidth(130); }
                else if(colName.contains("USER") || colName.contains("CATEGORY") || colName.contains("TYPE") || colName.contains("ROLE")) { t.getColumnModel().getColumn(i).setMinWidth(150); t.getColumnModel().getColumn(i).setMaxWidth(150); }
                else { t.getColumnModel().getColumn(i).setMinWidth(250); } 
            } p.add(scroll(t)); return p;
        }

        class DarkTabbedPaneUI extends BasicTabbedPaneUI {
            @Override protected void installDefaults() { super.installDefaults(); tabAreaInsets = new Insets(10, 15, 10, 15); contentBorderInsets = new Insets(0,0,0,0); }
            @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) { return new Insets(0, 0, 0, 0); }
            @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) { return 190; }
            @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) { return 46; }
            @Override protected int getTabRunOverlay(int tabPlacement) { return 0; }
            @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
            @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}
            @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
            @Override protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected); iconRect.x = tabRect.x + 16; textRect.x = tabRect.x + 42;
            }
            @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int pad = 4; 
                if(isSelected) { g2.setPaint(new GradientPaint(x, y+pad, new Color(0x3B1F7A), x+w, y+pad, new Color(0x5B2188))); g2.fillRoundRect(x, y+pad, w-5, h, 12, 12); } 
                else { g2.setColor(raised()); g2.fillRoundRect(x, y+pad, w-5, h, 12, 12); } g2.dispose();
            }
            @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) { 
                textRect.x -= 2; g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); g.setColor(isSelected ? Color.WHITE : t3()); 
                int txtY = textRect.y + metrics.getAscent() + (textRect.height - metrics.getHeight()) / 2; g.drawString(title, textRect.x, txtY); 
            }
            @Override protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect, boolean isSelected) {
                iconRect.x -= 2; super.paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN FRAME
    // ══════════════════════════════════════════════════════════════════════════
    static class MainFrame extends JFrame {
        final int userId; final String userName;
        double monthlyTarget=3000, dailyLimit=0; String streakCategories="Food,Miscellaneous"; String currentPage="dashboard";
        JButton[] navBtns; String[] pages = {"dashboard","activity","analytics","goals","settings"};
        String[] pageLabels = {"\uD83C\uDFE0  Dashboard","\uD83D\uDCCB  Activity","\uD83D\uDCCA  Analytics","\uD83C\uDFAF  Goals","\u2699\uFE0F  Settings"};
        JPanel contentArea; CardLayout cardLayout;
        DashboardPage dashPage; ActivityPage actPage; AnalyticsPage analyticsPage; GoalsPage goalsPage;
        JLabel clockLabel=new JLabel(); JLabel rankBadgeLabel=new JLabel();
        LocalDate lastCheckDate = LocalDate.now(); boolean hasNotifiedToday = false;

        MainFrame(int uid, String uname) {
            this.userId=uid; this.userName=uname; setTitle("ZenTracker"); setIconImage(getAppIcon(64)); setSize(1300,820);
            setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE); setLayout(new BorderLayout(0,0)); getContentPane().setBackground(base());
            loadUserPrefs(); buildFrame(); navigateTo("dashboard");
            new Timer(1000,e->clockLabel.setText(new SimpleDateFormat("EEE dd MMM · hh:mm:ss a").format(new Date()))).start();
            setupDailyReminder();
        }
        
        void setupDailyReminder() {
            if (!SystemTray.isSupported()) return;
            try {
                SystemTray tray = SystemTray.getSystemTray(); TrayIcon trayIcon = new TrayIcon(getAppIcon(16), "ZenTracker Pro - Add Transaction");
                trayIcon.setImageAutoSize(true); trayIcon.addActionListener(e -> { setVisible(true); setExtendedState(JFrame.NORMAL); showAddDialog(); });
                tray.add(trayIcon);
                Timer reminderTimer = new Timer(60000, e -> { 
                    LocalDate today = LocalDate.now(); if (!today.equals(lastCheckDate)) { hasNotifiedToday = false; lastCheckDate = today; }
                    java.time.LocalTime now = java.time.LocalTime.now();
                    if (!hasNotifiedToday && now.getHour() == 21 && now.getMinute() >= 30) {
                        boolean loggedToday = false;
                        try(Connection c = conn(); PreparedStatement ps = c.prepareStatement("SELECT 1 FROM transactions WHERE user_id=? AND DATE(trans_date)=CURDATE()")) {
                            ps.setInt(1, userId); ResultSet rs = ps.executeQuery(); if(rs.next()) loggedToday = true;
                        } catch(Exception ex){}
                        if(!loggedToday) { trayIcon.displayMessage("ZenTracker Reminder", "It's 9:30 PM! Don't forget to log today's transactions.", TrayIcon.MessageType.INFO); hasNotifiedToday = true; }
                    }
                }); reminderTimer.start();
            } catch(Exception e) {}
        }

        void loadUserPrefs() {
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT monthly_target,daily_limit,streak_categories,last_month_checked,theme FROM users WHERE user_id=?")){
                ps.setInt(1,userId); ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    monthlyTarget=rs.getDouble("monthly_target"); dailyLimit=rs.getDouble("daily_limit");
                    String sc=rs.getString("streak_categories"); if(sc!=null)streakCategories=sc;
                    String th=rs.getString("theme"); if(th!=null){ dark="dark".equals(th); applyUIDefaults(); }
                    String now=new SimpleDateFormat("yyyy-MM").format(new Date()); String last=rs.getString("last_month_checked");
                    if(last!=null&&!last.equals(now)){
                        showMonthPeek(last); monthlyTarget=0;dailyLimit=0;
                        try(Connection c2=conn();PreparedStatement p2=c2.prepareStatement("UPDATE users SET monthly_target=0,daily_limit=0,last_month_checked=? WHERE user_id=?")){ p2.setString(1,now);p2.setInt(2,userId);p2.executeUpdate(); }catch(SQLException ex){}
                        showMessage(this,"Fresh Start","\uD83C\uDF31 New month! Set fresh goals.",true);
                    } else if(last==null){ try(Connection c2=conn();PreparedStatement p2=c2.prepareStatement("UPDATE users SET last_month_checked=? WHERE user_id=?")){p2.setString(1,now);p2.setInt(2,userId);p2.executeUpdate();}catch(SQLException ex){} }
                }
            }catch(SQLException e){e.printStackTrace();}
        }

        void showMonthPeek(String past){
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT type,SUM(amount) FROM transactions WHERE user_id=? AND DATE_FORMAT(trans_date,'%Y-%m')=? GROUP BY type")){
                ps.setInt(1,userId);ps.setString(2,past);ResultSet rs=ps.executeQuery(); double inc=0,exp=0;
                while(rs.next()){if("Income".equals(rs.getString(1)))inc=rs.getDouble(2);else exp=rs.getDouble(2);}
                DecimalFormat df=new DecimalFormat("#,##0.00");
                showMessage(this,"Last Month",String.format("\uD83D\uDCCA %s Summary\n\n  Income:   ₹%s\n  Expenses: ₹%s\n  Saved:    ₹%s",past,df.format(inc),df.format(exp),df.format(inc-exp)),true);
            }catch(Exception ex){}
        }

        void buildFrame() {
            JPanel sidebar=new JPanel(new BorderLayout(0,0)); sidebar.setBackground(nav()); sidebar.setPreferredSize(new Dimension(270,0)); sidebar.setBorder(BorderFactory.createMatteBorder(0,0,0,1,bdr()));
            JPanel sTop=new JPanel(); sTop.setLayout(new BoxLayout(sTop,BoxLayout.Y_AXIS)); sTop.setBackground(nav()); sTop.setBorder(new EmptyBorder(24,20,20,20));
            JPanel brandRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); brandRow.setOpaque(false); brandRow.add(logoLabel(26));
            JLabel appName=new JLabel("ZenTracker"); appName.setFont(new Font("Segoe UI",Font.BOLD,16)); appName.setForeground(t1()); brandRow.add(appName);
            JLabel welcomeLbl=new JLabel("  Hey, "+userName+" \uD83D\uDC4B"); welcomeLbl.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14)); welcomeLbl.setForeground(t2());
            clockLabel.setFont(F_MONO_S); clockLabel.setForeground(t3()); clockLabel.setBorder(new EmptyBorder(2,2,0,0));
            sTop.add(brandRow); sTop.add(Box.createVerticalStrut(8)); sTop.add(welcomeLbl); sTop.add(Box.createVerticalStrut(4)); sTop.add(clockLabel);
            rankBadgeLabel.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13)); rankBadgeLabel.setBorder(new EmptyBorder(2,2,0,0));
            sTop.add(Box.createVerticalStrut(6)); sTop.add(rankBadgeLabel);
            
            JPanel navPanel=new JPanel(); navPanel.setLayout(new BoxLayout(navPanel,BoxLayout.Y_AXIS)); navPanel.setBackground(nav()); navPanel.setBorder(new EmptyBorder(8,12,8,12));
            navBtns=new JButton[pages.length];
            for(int i=0;i<pages.length;i++){ final String pg=pages[i]; JButton nb=navButton(pageLabels[i]); nb.addActionListener(e->navigateTo(pg)); navBtns[i]=nb; navPanel.add(nb); navPanel.add(Box.createVerticalStrut(4)); }

            JPanel sBot=new JPanel(); sBot.setLayout(new BoxLayout(sBot,BoxLayout.Y_AXIS)); sBot.setBackground(nav()); sBot.setBorder(new EmptyBorder(10,12,20,12));
            JButton addBtn=primaryBtn("+ Add Transaction"); addBtn.setAlignmentX(Component.LEFT_ALIGNMENT); addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,42)); addBtn.addActionListener(e->showAddDialog());
            JButton logoutBtn=ghostBtn("  ← Logout"); logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT); logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); logoutBtn.setForeground(t2()); logoutBtn.addActionListener(e->{dispose();new AuthFrame().setVisible(true);});
            sBot.add(addBtn); sBot.add(Box.createVerticalStrut(6)); sBot.add(logoutBtn);
            sidebar.add(sTop,BorderLayout.NORTH); sidebar.add(navPanel,BorderLayout.CENTER); sidebar.add(sBot,BorderLayout.SOUTH);

            cardLayout=new CardLayout(); contentArea=new JPanel(cardLayout); contentArea.setBackground(base());
            dashPage = new DashboardPage(this); actPage = new ActivityPage(this); analyticsPage = new AnalyticsPage(this); goalsPage = new GoalsPage(this); SettingsPage settingsPage = new SettingsPage(this);
            contentArea.add(dashPage, "dashboard"); contentArea.add(actPage, "activity"); contentArea.add(analyticsPage, "analytics"); contentArea.add(goalsPage, "goals"); contentArea.add(settingsPage, "settings");
            add(sidebar,BorderLayout.WEST); add(contentArea,BorderLayout.CENTER);
        }

        JButton navButton(String label) {
            JButton b=new JButton(label){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(getBackground().equals(PURPLE)){
                        g2.setPaint(new GradientPaint(0,0,new Color(0x3B1F7A),getWidth(),0,new Color(0x5B2188))); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                        g2.setPaint(new GradientPaint(0,0,PURPLE,getWidth(),0,PINK)); g2.setStroke(new BasicStroke(2f)); g2.fillRoundRect(0,getHeight()-3,getWidth(),3,2,2);
                    } else { g2.setColor(getBackground()); if(!getBackground().equals(nav())) g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            b.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14)); b.setForeground(t2()); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(false);
            b.setBorder(new EmptyBorder(10,14,10,14)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42)); b.setHorizontalAlignment(SwingConstants.LEFT); b.setBackground(nav());
            b.addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){if(!b.getBackground().equals(PURPLE)){b.setBackground(dark?new Color(0x1A1A22):new Color(0xECEAF8));b.setForeground(t1());b.repaint();}}
                public void mouseExited (MouseEvent e){if(!b.getBackground().equals(PURPLE)){b.setBackground(nav());b.setForeground(t2());b.repaint();}}
            }); return b;
        }

        void navigateTo(String page) {
            currentPage=page;
            for(int i=0;i<pages.length;i++){ boolean sel=pages[i].equals(page); navBtns[i].setBackground(sel?PURPLE:nav()); navBtns[i].setForeground(sel?Color.WHITE:t2()); }
            cardLayout.show(contentArea,page);
            if("dashboard".equals(page)) dashPage.refresh(); else if("activity".equals(page)) actPage.refresh(); else if("analytics".equals(page)) analyticsPage.refresh(); else if("goals".equals(page)) goalsPage.refresh();
            updateRankBadge();
        }

        void refreshAll() { dashPage.refresh(); actPage.refresh(); analyticsPage.refresh(); goalsPage.refresh(); updateRankBadge(); }

        void updateRankBadge() {
            int streak=calcStreak(); String emoji="\uD83C\uDF31"; String rankName="Sprout Saver"; Color accent=new Color(0x52B788);
            for(Object[] r:RANKS){if(streak>=(int)r[4]){emoji=(String)r[0];rankName=(String)r[1];accent=new Color((int)r[3]);break;}}
            rankBadgeLabel.setText("  \uD83D\uDD25 "+streak+"d  "+emoji+"  "+rankName); rankBadgeLabel.setForeground(accent);
        }

        int calcStreak() {
            if(dailyLimit<=0||streakCategories.isEmpty()) return 0; int streak=0;
            try(Connection c=conn()){
                String[] cats=streakCategories.split(","); StringBuilder sb=new StringBuilder();
                for(int i=0;i<cats.length;i++){sb.append("'").append(cats[i].replace("'","''")).append("'");if(i<cats.length-1)sb.append(",");}
                PreparedStatement ps=c.prepareStatement("SELECT DATE(trans_date) d,SUM(amount) s FROM transactions WHERE user_id=? AND type='Expense' AND category IN("+sb+") GROUP BY DATE(trans_date)");
                ps.setInt(1,userId); ResultSet rs=ps.executeQuery();
                Map<String,Double> daily=new HashMap<>(); while(rs.next())daily.put(rs.getString(1),rs.getDouble(2));
                PreparedStatement pm=c.prepareStatement("SELECT MIN(DATE(trans_date)) FROM transactions WHERE user_id=?");
                pm.setInt(1,userId); ResultSet rm=pm.executeQuery();
                if(!rm.next()||rm.getDate(1)==null) return 0;
                LocalDate cur=LocalDate.now(), minD=rm.getDate(1).toLocalDate();
                while(!cur.isBefore(minD)){if(daily.getOrDefault(cur.toString(),0.0)<=dailyLimit)streak++;else break;cur=cur.minusDays(1);}
            }catch(Exception e){e.printStackTrace();} return streak;
        }

        void showAddDialog() {
            JDialog dlg=new JDialog(this,"Add Transaction",true); dlg.setSize(420,620); dlg.setLocationRelativeTo(this); dlg.setLayout(new BorderLayout());
            JPanel body=new JPanel(new GridBagLayout()); body.setBackground(panel()); body.setBorder(new EmptyBorder(28,28,24,28));
            GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;
            JTextField amtF=field(), dateF=field(); dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            String[] catsWithPrompt=new String[CATEGORIES.length+1]; catsWithPrompt[0]="Select Category"; System.arraycopy(CATEGORIES,0,catsWithPrompt,1,CATEGORIES.length);
            JComboBox<String> catBox=combo(catsWithPrompt);
            List<String> subCats=new ArrayList<>(); subCats.add(""); List<String> notes=new ArrayList<>();   notes.add("");
            try(Connection c=conn()){
                PreparedStatement p=c.prepareStatement("SELECT DISTINCT sub_category FROM transactions WHERE user_id=? AND sub_category IS NOT NULL AND sub_category!='' ORDER BY sub_category");
                p.setInt(1,userId); ResultSet rs=p.executeQuery(); while(rs.next()){ String dec = Security.decrypt(rs.getString(1)); if(!subCats.contains(dec)) subCats.add(dec); }
                p=c.prepareStatement("SELECT DISTINCT note FROM transactions WHERE user_id=? AND note IS NOT NULL AND note!='' ORDER BY note");
                p.setInt(1,userId); rs=p.executeQuery(); while(rs.next()){ String dec = Security.decrypt(rs.getString(1)); if(!notes.contains(dec)) notes.add(dec); }
            }catch(SQLException e){e.printStackTrace();}
            JComboBox<String> subBox=editableCombo(subCats.toArray(new String[0])); JComboBox<String> noteBox=editableCombo(notes.toArray(new String[0])); JComboBox<String> typeBox=combo(new String[]{"Expense","Income"});

            int r=0; JLabel title=new JLabel("New Transaction"); title.setFont(F_H2); title.setForeground(t1()); gc.gridy=r++;gc.insets=new Insets(0,0,20,0); body.add(title,gc);
            addRow(body,gc,r++,"DATE",dateF); r++; addRow(body,gc,r++,"AMOUNT ₹",amtF); r++; addRow(body,gc,r++,"CATEGORY",catBox); r++; addRow(body,gc,r++,"COLLECTION (optional)",subBox); r++; addRow(body,gc,r++,"NOTE (optional)",noteBox); r++; addRow(body,gc,r++,"TYPE",typeBox); r++;

            JPanel btnRow=new JPanel(new GridLayout(1,2,10,0)); btnRow.setOpaque(false); JButton cancel=ghostBtn("Cancel"); JButton save=primaryBtn("Save");
            btnRow.add(cancel); btnRow.add(save); gc.gridy=r; gc.insets=new Insets(22,0,0,0); body.add(btnRow,gc);

            dlg.add(body,BorderLayout.CENTER); cancel.addActionListener(e->dlg.dispose());
            save.addActionListener(e->{
                if(catBox.getSelectedIndex()==0){ showMessage(dlg, "Error", "Select a category.", false); return; }
                try{
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); sdf.setLenient(false);
                    java.sql.Timestamp ts=new java.sql.Timestamp(sdf.parse(dateF.getText().trim()).getTime());
                    double amt=Double.parseDouble(amtF.getText().trim());
                    String sub=Security.encrypt(fmtStr(subBox.getSelectedItem()!=null?subBox.getSelectedItem().toString():""));
                    String note=Security.encrypt(noteBox.getSelectedItem()!=null?noteBox.getSelectedItem().toString():"");
                    String type=typeBox.getSelectedItem().toString();
                    try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO transactions (user_id,amount,category,sub_category,note,type,trans_date) VALUES(?,?,?,?,?,?,?)")){
                        ps.setInt(1,userId);ps.setDouble(2,amt);ps.setString(3,catBox.getSelectedItem().toString()); ps.setString(4,sub);ps.setString(5,note);ps.setString(6,type);ps.setTimestamp(7,ts); ps.executeUpdate();
                    } dlg.dispose(); refreshAll();
                }catch(Exception ex){ showMessage(dlg, "Error", "Invalid input: " + ex.getMessage(), false); }
            }); dlg.getContentPane().setBackground(panel()); dlg.setVisible(true);
        }

        static void addRow(JPanel p, GridBagConstraints gc, int y, String lbl, JComponent comp) {
            gc.gridy=y; gc.insets=new Insets(10,0,2,0); JLabel l=new JLabel(lbl.toUpperCase()); l.setFont(F_TINY); l.setForeground(t3()); p.add(l,gc); gc.gridy=y+1; gc.insets=new Insets(0,0,0,0); p.add(comp,gc);
        }

        static String fmtStr(String s) {
            if(s==null||s.trim().isEmpty()) return ""; String[] w=s.trim().split("\\s+"); StringBuilder sb=new StringBuilder();
            for(String ww:w){if(!ww.isEmpty())sb.append(Character.toUpperCase(ww.charAt(0))).append(ww.substring(1).toLowerCase()).append(" ");} return sb.toString().trim();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAGE: DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    static class DashboardPage extends JPanel {
        final MainFrame mf; JLabel expLbl=mkMono(), incLbl=mkMono(), balLbl=mkMono(), savLbl=mkMono();
        JPanel heatmapPanel=new JPanel(null), recentPanel=new JPanel(), insightPanel=new JPanel(); JLabel streakLbl=new JLabel();
        static JLabel mkMono(){ JLabel l=new JLabel("₹0"); l.setFont(F_MONO); l.setForeground(t1()); return l; }

        DashboardPage(MainFrame mf) { this.mf=mf; setLayout(new BorderLayout(0,0)); setBackground(base()); buildUI(); }

        void buildUI() {
            JPanel header=new JPanel(new BorderLayout()); header.setBackground(panel()); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(18,28,18,28)));
            JLabel title=new JLabel("Dashboard"); title.setFont(F_H2); title.setForeground(t1()); JLabel sub=new JLabel(new SimpleDateFormat("MMMM yyyy").format(new Date())); sub.setFont(F_BODY); sub.setForeground(t2());
            JPanel hl=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); hl.setOpaque(false); hl.add(title); hl.add(sub); header.add(hl,BorderLayout.WEST); add(header,BorderLayout.NORTH);
            JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(base()); content.setBorder(new EmptyBorder(22,24,24,24));

            JPanel statsRow=new JPanel(new GridLayout(1,4,14,0)); statsRow.setOpaque(false); statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
            expLbl.setForeground(RED); incLbl.setForeground(GREEN); balLbl.setForeground(PURPLE2); savLbl.setForeground(AMBER);
            statsRow.add(statChip("MONTH SPENT",expLbl,RED)); statsRow.add(statChip("INCOME",incLbl,GREEN)); statsRow.add(statChip("BUDGET LEFT",balLbl,PURPLE)); statsRow.add(statChip("SAVINGS",savLbl,AMBER));

            JPanel budgetCard=cardPanel(new BorderLayout(0,10)); budgetCard.setBorder(new EmptyBorder(16,18,16,18)); budgetCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,90));
            JPanel budgetTop=new JPanel(new BorderLayout()); budgetTop.setOpaque(false); JLabel bLbl=new JLabel("Monthly Budget"); bLbl.setFont(F_BOLD); bLbl.setForeground(t1()); JLabel bPct=new JLabel("0%"); bPct.setFont(F_MONO_S); bPct.setForeground(t2());
            budgetTop.add(bLbl,BorderLayout.WEST); budgetTop.add(bPct,BorderLayout.EAST);
            JPanel bar=new JPanel(null){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(raised()); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                    double pct=mf.monthlyTarget>0?Math.min(1.0,getSpentThisMonth()/mf.monthlyTarget):0; int w=(int)(getWidth()*pct);
                    if(w>0){ Color c1=pct>.9?RED:pct>.7?AMBER:PURPLE; Color c2=pct>.9?new Color(0xFB7185):pct>.7?new Color(0xFBBF24):PINK; g2.setPaint(new GradientPaint(0,0,c1,w,0,c2)); g2.fillRoundRect(0,0,w,getHeight(),getHeight(),getHeight()); }
                    bPct.setText((int)(pct*100)+"%"); g2.dispose();
                }
            }; bar.setPreferredSize(new Dimension(0,8)); bar.setOpaque(false); budgetCard.add(budgetTop,BorderLayout.NORTH); budgetCard.add(bar,BorderLayout.CENTER);

            JPanel streakCard=new JPanel(new BorderLayout()){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0,0,dark?new Color(0x1C1400):new Color(0xFFF8E0),getWidth(),0,dark?new Color(0x2A2000):new Color(0xFFF3C4))); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                    g2.setColor(new Color(0xF59E0B,false)); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); g2.dispose();
                }
            }; streakCard.setOpaque(false); streakCard.setBorder(new EmptyBorder(14,18,14,18)); streakCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,62));
            streakLbl.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14)); streakLbl.setForeground(AMBER); streakCard.add(streakLbl,BorderLayout.CENTER);

            JPanel hmCard=cardPanel(new BorderLayout(0,10)); hmCard.setBorder(new EmptyBorder(16,18,16,18)); hmCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,230)); 
            JLabel hmTitle=new JLabel("Spending Heatmap  ·  Last 7 weeks"); hmTitle.setFont(F_BOLD); hmTitle.setForeground(t1()); heatmapPanel.setOpaque(false); heatmapPanel.setPreferredSize(new Dimension(0,100));
            hmCard.add(hmTitle,BorderLayout.NORTH); hmCard.add(heatmapPanel,BorderLayout.CENTER);

            JPanel bottomRow=new JPanel(new GridLayout(1,2,14,0)); bottomRow.setOpaque(false); bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,260));
            JPanel recentCard=cardPanel(new BorderLayout(0,10)); recentCard.setBorder(new EmptyBorder(16,18,16,18)); JPanel recentHdr=new JPanel(new BorderLayout()); recentHdr.setOpaque(false);
            JLabel recTitle=new JLabel("Recent"); recTitle.setFont(F_BOLD); recTitle.setForeground(t1()); JButton seeAll=new JButton("See all →"); seeAll.setFont(new Font("Segoe UI",Font.PLAIN,13));
            seeAll.setForeground(PURPLE2); seeAll.setContentAreaFilled(false); seeAll.setBorderPainted(false); seeAll.setFocusPainted(false); seeAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            seeAll.addActionListener(e->mf.navigateTo("activity")); recentHdr.add(recTitle,BorderLayout.WEST); recentHdr.add(seeAll,BorderLayout.EAST); recentPanel.setLayout(new BoxLayout(recentPanel,BoxLayout.Y_AXIS)); recentPanel.setOpaque(false);
            recentCard.add(recentHdr,BorderLayout.NORTH); recentCard.add(scroll(recentPanel),BorderLayout.CENTER);

            JPanel insightCard=cardPanel(new BorderLayout(0,10)); insightCard.setBorder(new EmptyBorder(16,18,16,18)); 
            JLabel inTitle=new JLabel("\uD83D\uDCA1  Smart Insights"); inTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15)); inTitle.setForeground(t1());
            insightPanel.setLayout(new BoxLayout(insightPanel,BoxLayout.Y_AXIS)); insightPanel.setOpaque(false); insightCard.add(inTitle,BorderLayout.NORTH); insightCard.add(scroll(insightPanel),BorderLayout.CENTER);

            bottomRow.add(recentCard); bottomRow.add(insightCard);
            content.add(statsRow); content.add(Box.createVerticalStrut(14)); content.add(budgetCard); content.add(Box.createVerticalStrut(14)); content.add(streakCard); content.add(Box.createVerticalStrut(14)); content.add(hmCard); content.add(Box.createVerticalStrut(14)); content.add(bottomRow);
            add(scroll(content),BorderLayout.CENTER);
        }

        double getSpentThisMonth(){
            String m=new SimpleDateFormat("MMM yyyy").format(new Date());
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT SUM(amount) FROM transactions WHERE user_id=? AND type='Expense' AND DATE_FORMAT(trans_date,'%b %Y')=?")){ ps.setInt(1,mf.userId);ps.setString(2,m);ResultSet rs=ps.executeQuery(); if(rs.next())return rs.getDouble(1); }catch(Exception e){} return 0;
        }

        void refresh() {
            DecimalFormat df=new DecimalFormat("#,##0.00"); String m=new SimpleDateFormat("MMM yyyy").format(new Date()); double inc=0,exp=0;
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT type,SUM(amount) t FROM transactions WHERE user_id=? AND DATE_FORMAT(trans_date,'%b %Y')=? GROUP BY type")){ ps.setInt(1,mf.userId);ps.setString(2,m);ResultSet rs=ps.executeQuery(); while(rs.next()){if("Income".equals(rs.getString("type")))inc=rs.getDouble("t");else exp=rs.getDouble("t");} }catch(Exception e){e.printStackTrace();}
            expLbl.setText("₹"+df.format(exp)); incLbl.setText("₹"+df.format(inc)); double rem=mf.monthlyTarget-exp; balLbl.setText("₹"+df.format(rem)); balLbl.setForeground(rem<0?RED:PURPLE2); savLbl.setText("₹"+df.format(Math.max(0,inc-exp)));
            int streak=mf.calcStreak(); String emoji="\uD83C\uDF31";String rname="Sprout Saver"; for(Object[] r:RANKS){if(streak>=(int)r[4]){emoji=(String)r[0];rname=(String)r[1];break;}} streakLbl.setText("  \uD83D\uDD25  "+streak+" day streak   "+emoji+"  "+rname+"   ·   "+mf.streakCategories.replace(","," · "));
            refreshHeatmap(); recentPanel.removeAll();
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT category,amount,type,trans_date,note FROM transactions WHERE user_id=? ORDER BY trans_date DESC LIMIT 7")){
                ps.setInt(1,mf.userId);ResultSet rs=ps.executeQuery();
                while(rs.next()){ boolean isInc="Income".equals(rs.getString("type")); String dt=new SimpleDateFormat("MMM dd").format(rs.getTimestamp("trans_date")); String note=Security.decrypt(rs.getString("note")); if(note==null||note.isEmpty())note="";
                    recentPanel.add(recentRow(rs.getString("category"),note,dt,rs.getDouble("amount"),isInc)); recentPanel.add(Box.createVerticalStrut(1)); }
            }catch(Exception e){e.printStackTrace();} recentPanel.revalidate(); recentPanel.repaint(); buildInsights(exp,inc,m); repaint();
        }

        void refreshHeatmap(){
            heatmapPanel.removeAll(); heatmapPanel.setLayout(null); int cols=7,rows=7,sz=16,gap=3; int totalW=cols*(sz+gap); int totalH=rows*(sz+gap)+20; heatmapPanel.setPreferredSize(new Dimension(totalW+100,totalH));
            Map<String,Double> spending=new HashMap<>(); LocalDate today=LocalDate.now(); LocalDate start=today.minusDays((long)(cols*rows-1));
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DATE(trans_date) d,SUM(amount) s FROM transactions WHERE user_id=? AND type='Expense' AND trans_date>=? GROUP BY DATE(trans_date)")){ ps.setInt(1,mf.userId);ps.setString(2,start.toString());ResultSet rs=ps.executeQuery(); while(rs.next())spending.put(rs.getString("d"),rs.getDouble("s")); }catch(Exception e){}
            double max=spending.values().stream().mapToDouble(d->d).max().orElse(1); String[] days={"S","M","T","W","T","F","S"};
            for(int row=0;row<rows;row++){ JLabel dl=new JLabel(days[row]); dl.setFont(F_TINY); dl.setForeground(t3()); dl.setBounds(0,(row+1)*(sz+gap)+sz/2-6,14,14); heatmapPanel.add(dl); }
            for(int col=0;col<cols;col++){
                for(int row=0;row<rows;row++){
                    LocalDate d=start.plusDays((long)(col*rows+row)); if(d.isAfter(today)) continue; double amt=spending.getOrDefault(d.toString(),0.0); float intensity=(float)(amt/max); final Color cellColor;
                    if(amt==0) cellColor=dark?new Color(0x1E1E24):new Color(0xE8E6F4); else { int r=(int)(RED.getRed()*intensity+AMBER.getRed()*(1-intensity)); int g=(int)(RED.getGreen()*intensity+AMBER.getGreen()*(1-intensity)); int b=(int)(RED.getBlue()*intensity+AMBER.getBlue()*(1-intensity)); cellColor=new Color(Math.min(255,r),Math.min(255,g),Math.min(255,b),180+(int)(intensity*75)); }
                    final String dateStr=d.toString(); final double amtFinal=amt;
                    JPanel cell=new JPanel(){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(cellColor); g2.fillRoundRect(0,0,sz,sz,4,4); g2.dispose(); } };
                    cell.setOpaque(false); cell.setToolTipText(dateStr+" · ₹"+new DecimalFormat("#,##0").format(amtFinal)); cell.setBounds(18+col*(sz+gap),(row+1)*(sz+gap),sz,sz);
                    if(row==0){ String mo=d.format(DateTimeFormatter.ofPattern("MMM")); JLabel ml=new JLabel(mo); ml.setFont(F_TINY); ml.setForeground(t3()); ml.setBounds(18+col*(sz+gap),0,30,14); heatmapPanel.add(ml); }
                    heatmapPanel.add(cell);
                }
            } heatmapPanel.revalidate(); heatmapPanel.repaint();
        }

        JPanel recentRow(String cat, String note, String date, double amt, boolean isInc){
            JPanel row=new JPanel(new BorderLayout(8,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,54)); row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(8,4,8,4)));
            int ci=Math.abs(cat.hashCode())%CAT_COLORS.length; JLabel dot=new JLabel("●"); dot.setFont(new Font("Segoe UI",Font.BOLD,18)); dot.setForeground(CAT_COLORS[ci]);
            JPanel left=new JPanel(new GridLayout(2,1,0,2)); left.setOpaque(false); JLabel catLbl=new JLabel(cat); catLbl.setFont(F_BOLD); catLbl.setForeground(t1()); JLabel noteLbl=new JLabel(note.isEmpty()?date:note+" · "+date); noteLbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); noteLbl.setForeground(t2()); left.add(catLbl); left.add(noteLbl); 
            JLabel amtLbl=new JLabel((isInc?"+₹":"-₹")+new DecimalFormat("#,##0.00").format(amt)); amtLbl.setFont(F_MONO_S); amtLbl.setForeground(isInc?GREEN:RED); row.add(dot,BorderLayout.WEST); row.add(left,BorderLayout.CENTER); row.add(amtLbl,BorderLayout.EAST); return row;
        }

        void buildInsights(double exp, double inc, String month){
            insightPanel.removeAll(); DecimalFormat df=new DecimalFormat("#,##0"); List<String[]> tips=new ArrayList<>();
            if(mf.monthlyTarget>0){ double pct=exp/mf.monthlyTarget*100; if(pct>90) tips.add(new String[]{"\uD83D\uDD34","Over budget!","You've used "+df.format(pct)+"% of your budget."}); else if(pct>70) tips.add(new String[]{"\uD83D\uDFE1","Budget warning",""+df.format(pct)+"% of monthly budget used."}); else tips.add(new String[]{"\uD83D\uDFE2","On track","Only "+df.format(pct)+"% budget used."}); }
            if(inc>0){ double sRate=(inc-exp)/inc*100; if(sRate>30) tips.add(new String[]{"\uD83C\uDF1F","Great savings rate!","You're saving "+df.format(sRate)+"% of income."}); else if(sRate<10) tips.add(new String[]{"\uD83D\uDCA1","Boost savings","Your savings rate is only "+df.format(Math.max(0,sRate))+"%. Aim for 20%."}); }
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT category,SUM(amount) s FROM transactions WHERE user_id=? AND type='Expense' AND DATE_FORMAT(trans_date,'%b %Y')=? GROUP BY category ORDER BY s DESC LIMIT 1")){ ps.setInt(1,mf.userId);ps.setString(2,month);ResultSet rs=ps.executeQuery(); if(rs.next()) tips.add(new String[]{"\uD83D\uDCCC","Top spend","₹"+df.format(rs.getDouble("s"))+" on "+rs.getString("category")+"."}); }catch(Exception e){}
            LocalDate now=LocalDate.now(); int dayOfMonth=now.getDayOfMonth(); if(dayOfMonth>0&&exp>0){ double avg=exp/dayOfMonth; double proj=avg*now.lengthOfMonth(); tips.add(new String[]{"\uD83D\uDCC8","Daily avg","₹"+df.format(avg)+"/day · projected ₹"+df.format(proj)+"/month."}); }
            int streak=mf.calcStreak(); if(streak>0) tips.add(new String[]{"\uD83D\uDD25","Streak active!",streak+" days under daily limit. Keep going!"}); else if(mf.dailyLimit>0) tips.add(new String[]{"⚠\uFE0F","Streak broken","Set a goal & stay under ₹"+df.format(mf.dailyLimit)+" today."});
            for(String[] tip:tips) insightPanel.add(insightRow(tip[0],tip[1],tip[2])); insightPanel.revalidate(); insightPanel.repaint();
        }

        JPanel insightRow(String emoji, String title, String body){
            JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,56)); row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(8,4,8,4)));
            JLabel el=new JLabel(emoji); el.setFont(new Font("Segoe UI Emoji",Font.PLAIN,20)); el.setForeground(t1()); JPanel txt=new JPanel(new BorderLayout(0,2)); txt.setOpaque(false); JLabel tl=new JLabel(title); tl.setFont(F_BOLD); tl.setForeground(t1()); JLabel bl=new JLabel(body); bl.setFont(new Font("Segoe UI",Font.PLAIN,12)); bl.setForeground(t2());
            txt.add(tl,BorderLayout.NORTH); txt.add(bl,BorderLayout.SOUTH); row.add(el,BorderLayout.WEST); row.add(txt,BorderLayout.CENTER); return row;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAGE: ACTIVITY
    // ══════════════════════════════════════════════════════════════════════════
    static class ActivityPage extends JPanel {
        final MainFrame mf; DefaultTableModel txModel; JTable table; JComboBox<String> monthBox, catBox, colBox; JLabel totalLbl=new JLabel(); String selectedMonth;
        ActivityPage(MainFrame mf){ this.mf=mf; setLayout(new BorderLayout(0,0)); setBackground(base()); selectedMonth=new SimpleDateFormat("MMM yyyy").format(new Date()); buildUI(); }

        void buildUI(){
            JPanel header=new JPanel(new BorderLayout(0,8)); header.setBackground(panel()); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(14,24,14,24)));
            JLabel title=new JLabel("Activity"); title.setFont(F_H2); title.setForeground(t1()); JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); filters.setOpaque(false);
            List<String> months=new ArrayList<>(); months.add(selectedMonth);
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DISTINCT DATE_FORMAT(trans_date,'%b %Y') m,DATE_FORMAT(trans_date,'%Y-%m') sm FROM transactions WHERE user_id=? ORDER BY sm DESC")){ ps.setInt(1,mf.userId);ResultSet rs=ps.executeQuery(); while(rs.next()){String mo=rs.getString("m");if(!mo.equals(selectedMonth))months.add(mo);} }catch(Exception e){}
            monthBox=combo(months.toArray(new String[0])); String[] cats=new String[CATEGORIES.length+1]; cats[0]="All Categories"; System.arraycopy(CATEGORIES,0,cats,1,CATEGORIES.length); catBox=combo(cats); colBox=combo(new String[]{"All Collections"});
            styleSmallCombo(monthBox); styleSmallCombo(catBox); styleSmallCombo(colBox); totalLbl.setFont(F_MONO_S); totalLbl.setForeground(PURPLE2);
            filters.add(microLabel("MONTH")); filters.add(monthBox); filters.add(microLabel("CAT")); filters.add(catBox); filters.add(microLabel("COL")); filters.add(colBox); filters.add(Box.createHorizontalStrut(14)); filters.add(totalLbl);
            header.add(title,BorderLayout.NORTH); header.add(filters,BorderLayout.SOUTH); add(header,BorderLayout.NORTH);

            txModel=new DefaultTableModel(null,new String[]{"ID","Date","Category","Collection","Note","Amount"}){public boolean isCellEditable(int r,int c){return false;}};
            table=styledTable(txModel); table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0); table.getColumnModel().getColumn(0).setPreferredWidth(0);
            table.getColumnModel().getColumn(1).setPreferredWidth(160); table.getColumnModel().getColumn(1).setMinWidth(160); table.getColumnModel().getColumn(1).setMaxWidth(160);
            table.getColumnModel().getColumn(2).setPreferredWidth(160); table.getColumnModel().getColumn(2).setMinWidth(160); table.getColumnModel().getColumn(2).setMaxWidth(160);
            table.getColumnModel().getColumn(3).setPreferredWidth(160); table.getColumnModel().getColumn(3).setMinWidth(160); table.getColumnModel().getColumn(3).setMaxWidth(160);
            table.getColumnModel().getColumn(4).setPreferredWidth(300); table.getColumnModel().getColumn(4).setMinWidth(250); 
            table.getColumnModel().getColumn(5).setPreferredWidth(140); table.getColumnModel().getColumn(5).setMinWidth(140); table.getColumnModel().getColumn(5).setMaxWidth(140);

            table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){ @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean is,boolean hf,int row,int col){ super.getTableCellRendererComponent(t,v,is,hf,row,col); String cat=v!=null?v.toString():""; int ci=Math.abs(cat.hashCode())%CAT_COLORS.length; setForeground(is?t1():CAT_COLORS[ci]); setBackground(is?(dark?new Color(0x2D1B69):new Color(0xD8D0F7)):(row%2==0?card():raised())); setFont(F_BOLD); setOpaque(true); setBorder(new EmptyBorder(0,16,0,16)); return this; } });
            table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){ @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean is,boolean hf,int row,int col){ super.getTableCellRendererComponent(t,v,is,hf,row,col); setFont(F_MONO_S); setHorizontalAlignment(SwingConstants.RIGHT); setForeground(v!=null&&v.toString().startsWith("+")?GREEN:RED); setBackground(is?(dark?new Color(0x2D1B69):new Color(0xD8D0F7)):(row%2==0?card():raised())); setOpaque(true); setBorder(new EmptyBorder(0,16,0,16)); return this; } });
            DefaultTableCellRenderer stripe=new DefaultTableCellRenderer(){ @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean is,boolean hf,int row,int col){ super.getTableCellRendererComponent(t,v,is,hf,row,col); setBackground(is?(dark?new Color(0x2D1B69):new Color(0xD8D0F7)):(row%2==0?card():raised())); setForeground(is?t1():t2()); setOpaque(true); setBorder(new EmptyBorder(0,16,0,16)); return this; } };
            table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer(){ @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean is,boolean hf,int row,int col){ super.getTableCellRendererComponent(t,v,is,hf,row,col); setFont(F_MONO_S); setForeground(is?t1():t2()); setBackground(is?(dark?new Color(0x2D1B69):new Color(0xD8D0F7)):(row%2==0?card():raised())); setOpaque(true); setBorder(new EmptyBorder(0,16,0,16)); return this; } });
            table.getColumnModel().getColumn(3).setCellRenderer(stripe); table.getColumnModel().getColumn(4).setCellRenderer(stripe);

            add(scroll(table),BorderLayout.CENTER);

            JPanel bot=new JPanel(new BorderLayout()); bot.setBackground(panel()); bot.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,bdr()),new EmptyBorder(10,20,10,20)));
            JLabel hint=new JLabel("Click to select  ·  Shift+Click multi-select"); hint.setFont(new Font("Segoe UI",Font.PLAIN,12)); hint.setForeground(t3());
            JPanel botBtns=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); botBtns.setOpaque(false); JButton delBtn=dangerBtn("\uD83D\uDDD1  Delete Selected"); delBtn.addActionListener(e->deleteSelected()); botBtns.add(delBtn);
            bot.add(hint,BorderLayout.WEST); bot.add(botBtns,BorderLayout.EAST); add(bot,BorderLayout.SOUTH);

            monthBox.addActionListener(e->refresh()); catBox.addActionListener(e->refresh()); colBox.addActionListener(e->{if(!colBox.getSelectedItem().equals("All Collections"))refresh();});
        }

        static void styleSmallCombo(JComboBox<String> b){
            b.setFont(new Font("Segoe UI",Font.BOLD,13)); b.setPreferredSize(new Dimension(140,32)); b.setBackground(input()); b.setForeground(t1()); b.setOpaque(false);
            b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr2(),1,true),new EmptyBorder(2,6,2,4)));
            b.setRenderer(new DefaultListCellRenderer(){ @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean foc){ super.getListCellRendererComponent(l,v,i,sel,foc); setBackground(sel?PURPLE:input()); setForeground(sel?Color.WHITE:t1()); setOpaque(true); setBorder(new EmptyBorder(6,12,6,12)); return this; } });
        }

        static JLabel microLabel(String t){ JLabel l=new JLabel(t.toUpperCase()); l.setFont(F_TINY); l.setForeground(t3()); return l; }

        void refresh(){
            String curMonth=monthBox.getSelectedItem()!=null?monthBox.getSelectedItem().toString():new SimpleDateFormat("MMM yyyy").format(new Date());
            ActionListener[] als=monthBox.getActionListeners(); for(ActionListener a:als)monthBox.removeActionListener(a); monthBox.removeAllItems();
            List<String> months=new ArrayList<>(); months.add(new SimpleDateFormat("MMM yyyy").format(new Date()));
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DISTINCT DATE_FORMAT(trans_date,'%b %Y') m,DATE_FORMAT(trans_date,'%Y-%m') sm FROM transactions WHERE user_id=? ORDER BY sm DESC")){ ps.setInt(1,mf.userId);ResultSet rs=ps.executeQuery(); while(rs.next()){String mo=rs.getString("m");if(!months.contains(mo))months.add(mo);} }catch(Exception e){}
            for(String mo:months)monthBox.addItem(mo); monthBox.setSelectedItem(curMonth); for(ActionListener a:als)monthBox.addActionListener(a);

            String curCol=colBox.getSelectedItem()!=null?colBox.getSelectedItem().toString():"All Collections";
            ActionListener[] cals=colBox.getActionListeners(); for(ActionListener a:cals)colBox.removeActionListener(a); colBox.removeAllItems(); colBox.addItem("All Collections");
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DISTINCT sub_category FROM transactions WHERE user_id=? AND sub_category IS NOT NULL AND sub_category!=''")){ ps.setInt(1,mf.userId);ResultSet rs=ps.executeQuery(); while(rs.next()) { String dec = Security.decrypt(rs.getString(1)); boolean exists = false; for(int i=0; i<colBox.getItemCount(); i++) if(colBox.getItemAt(i).equals(dec)) exists = true; if(!exists) colBox.addItem(dec); } }catch(Exception e){}
            colBox.setSelectedItem(curCol); for(ActionListener a:cals)colBox.addActionListener(a);

            String month=monthBox.getSelectedItem()!=null?monthBox.getSelectedItem().toString():new SimpleDateFormat("MMM yyyy").format(new Date()); String cat=catBox.getSelectedItem()!=null?catBox.getSelectedItem().toString():"All Categories"; String col=colBox.getSelectedItem()!=null?colBox.getSelectedItem().toString():"All Collections"; DecimalFormat df=new DecimalFormat("#,##0.00"); txModel.setRowCount(0);
            String q="SELECT id,trans_date,category,sub_category,note,amount,type FROM transactions WHERE user_id=? AND DATE_FORMAT(trans_date,'%b %Y')=?";
            if(!"All Categories".equals(cat)) q+=" AND category='"+cat.replace("'","''")+"'"; if(!"All Collections".equals(col)) q+=" AND sub_category='"+Security.encrypt(col).replace("'","''")+"'"; q+=" ORDER BY trans_date DESC"; double total=0;
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement(q)){
                ps.setInt(1,mf.userId);ps.setString(2,month);ResultSet rs=ps.executeQuery();
                while(rs.next()){ boolean isInc="Income".equals(rs.getString("type")); String date=new SimpleDateFormat("MMM dd").format(rs.getTimestamp("trans_date")); String note=Security.decrypt(rs.getString("note")); if(note==null)note=""; String sub=Security.decrypt(rs.getString("sub_category")); if(sub==null)sub=""; double amt=rs.getDouble("amount"); if(!isInc)total+=amt; txModel.addRow(new Object[]{rs.getInt("id"),date,rs.getString("category"),sub,note,(isInc?"+₹":"-₹")+df.format(amt)}); }
            }catch(Exception e){e.printStackTrace();} totalLbl.setText("Total: ₹"+df.format(total));
        }

        void deleteSelected(){
            int[] rows=table.getSelectedRows(); if(rows.length==0){showMessage(mf, "Error", "Select rows to delete.", false);return;}
            if(JOptionPane.showConfirmDialog(this,"Delete "+rows.length+" transaction(s)?","Confirm",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.OK_OPTION){
                Connection c = null;
                try { c = conn(); c.setAutoCommit(false); 
                    try(PreparedStatement ps=c.prepareStatement("DELETE FROM transactions WHERE id=?")){ for(int row:rows){ps.setInt(1,(int)txModel.getValueAt(row,0));ps.addBatch();} ps.executeBatch(); }
                    c.commit(); mf.refreshAll();
                } catch(SQLException ex) { if (c != null) { try { c.rollback(); } catch (SQLException rollbackEx) {} } showMessage(mf, "Error", "Delete failed: Data rolled back safely.", false);
                } finally { if (c != null) { try { c.setAutoCommit(true); c.close(); } catch (SQLException closeEx) {} } }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAGE: ANALYTICS
    // ══════════════════════════════════════════════════════════════════════════
    static class AnalyticsPage extends JPanel {
        final MainFrame mf; JPanel piePanel=new JPanel(null); JPanel barPanel=new JPanel(null); JPanel trendPanel=new JPanel(null); 
        JComboBox<String> timeFilterBox, monthBox; JPanel legendPanel=new JPanel(); String activeFilter = "PERIOD";

        AnalyticsPage(MainFrame mf){this.mf=mf;setLayout(new BorderLayout(0,0));setBackground(base());buildUI();}

        void buildUI(){
            JPanel header=new JPanel(new BorderLayout()); header.setBackground(panel()); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(14,24,14,24)));
            JLabel title=new JLabel("Analytics"); title.setFont(F_H2); title.setForeground(t1());
            
            timeFilterBox = combo(new String[]{"1 Week", "2 Weeks", "1 Month", "2 Months", "3 Months", "6 Months", "1 Year"}); 
            timeFilterBox.setSelectedIndex(2);
            ActivityPage.styleSmallCombo(timeFilterBox);
            
            monthBox = combo(new String[]{}); 
            ActivityPage.styleSmallCombo(monthBox);
            
            String selectedMonth = new SimpleDateFormat("MMM yyyy").format(new Date());
            List<String> months=new ArrayList<>(); months.add(selectedMonth);
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DISTINCT DATE_FORMAT(trans_date,'%b %Y') m,DATE_FORMAT(trans_date,'%Y-%m') sm FROM transactions WHERE user_id=? ORDER BY sm DESC")){
                ps.setInt(1,mf.userId);ResultSet rs=ps.executeQuery(); while(rs.next()){String mo=rs.getString("m");if(!mo.equals(selectedMonth))months.add(mo);}
            }catch(Exception e){}
            for(String mo:months) monthBox.addItem(mo);
            
            JPanel hr=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); hr.setOpaque(false); 
            hr.add(ActivityPage.microLabel("PERIOD")); hr.add(timeFilterBox); 
            hr.add(Box.createHorizontalStrut(10));
            hr.add(ActivityPage.microLabel("OR MONTH")); hr.add(monthBox); 
            
            header.add(title,BorderLayout.WEST); header.add(hr,BorderLayout.EAST); add(header,BorderLayout.NORTH);
            JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(base()); content.setBorder(new EmptyBorder(20,22,20,22));

            JPanel row1=new JPanel(new GridLayout(1,2,16,0)); row1.setOpaque(false); row1.setMaximumSize(new Dimension(Integer.MAX_VALUE,280));
            JPanel donutCard=cardPanel(new BorderLayout(0,8)); donutCard.setBorder(new EmptyBorder(16,16,16,16)); JLabel dt=new JLabel("Category Split"); dt.setFont(F_BOLD); dt.setForeground(t1()); piePanel.setOpaque(false); piePanel.setPreferredSize(new Dimension(0,180)); legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT,8,4)); legendPanel.setOpaque(false); donutCard.add(dt,BorderLayout.NORTH); donutCard.add(piePanel,BorderLayout.CENTER); donutCard.add(legendPanel,BorderLayout.SOUTH);
            JPanel barCard=cardPanel(new BorderLayout(0,8)); barCard.setBorder(new EmptyBorder(16,16,16,16)); JLabel bt=new JLabel("Category Bars"); bt.setFont(F_BOLD); bt.setForeground(t1()); barPanel.setOpaque(false); barCard.add(bt,BorderLayout.NORTH); barCard.add(barPanel,BorderLayout.CENTER);
            row1.add(donutCard); row1.add(barCard);

            JPanel trendCard=cardPanel(new BorderLayout(0,8)); trendCard.setBorder(new EmptyBorder(16,16,16,16)); trendCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,220)); JLabel tt=new JLabel("Spending Trend"); tt.setFont(F_BOLD); tt.setForeground(t1()); trendPanel.setOpaque(false); trendCard.add(tt,BorderLayout.NORTH); trendCard.add(trendPanel,BorderLayout.CENTER);

            content.add(row1); content.add(Box.createVerticalStrut(16)); content.add(trendCard); add(scroll(content),BorderLayout.CENTER); 
            
            timeFilterBox.addActionListener(e -> { activeFilter = "PERIOD"; refresh(); });
            monthBox.addActionListener(e -> { activeFilter = "MONTH"; refresh(); });
        }

        void refresh(){
            if(activeFilter.equals("PERIOD")) {
                String sel = timeFilterBox.getSelectedItem().toString(); LocalDate start = LocalDate.now();
                if(sel.equals("1 Week")) start = start.minusWeeks(1); else if(sel.equals("2 Weeks")) start = start.minusWeeks(2); else if(sel.equals("1 Month")) start = start.minusMonths(1);
                else if(sel.equals("2 Months")) start = start.minusMonths(2); else if(sel.equals("3 Months")) start = start.minusMonths(3); else if(sel.equals("6 Months")) start = start.minusMonths(6);
                else if(sel.equals("1 Year")) start = start.minusYears(1);
                Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());

                Map<String,Double> catData=new LinkedHashMap<>();
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT category,SUM(amount) s FROM transactions WHERE user_id=? AND type='Expense' AND trans_date>=? GROUP BY category ORDER BY s DESC")){
                    ps.setInt(1,mf.userId);ps.setTimestamp(2,startTs);ResultSet rs=ps.executeQuery(); while(rs.next())catData.put(rs.getString("category"),rs.getDouble("s"));
                }catch(Exception e){e.printStackTrace();}
                double total=catData.values().stream().mapToDouble(d->d).sum(); drawDonut(catData,total); drawBars(catData,total); 
                
                boolean groupDaily = sel.contains("Week") || sel.equals("1 Month") || sel.equals("2 Months");
                String groupSql = groupDaily ? "DATE(trans_date)" : "DATE_FORMAT(trans_date, '%Y-%m')";
                
                List<String> labels=new ArrayList<>(); List<Double> incomes=new ArrayList<>(); List<Double> expenses=new ArrayList<>();
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT " + groupSql + " as grp, type, SUM(amount) t FROM transactions WHERE user_id=? AND trans_date>=? GROUP BY grp, type ORDER BY grp ASC")){
                    ps.setInt(1,mf.userId); ps.setTimestamp(2,startTs); ResultSet rs=ps.executeQuery(); Map<String, Double> incMap = new LinkedHashMap<>(); Map<String, Double> expMap = new LinkedHashMap<>();
                    while(rs.next()){ String grp = rs.getString("grp"); if(!labels.contains(grp)) labels.add(grp); if("Income".equals(rs.getString("type"))) incMap.put(grp, rs.getDouble("t")); else expMap.put(grp, rs.getDouble("t")); }
                    for(String l : labels) { incomes.add(incMap.getOrDefault(l, 0.0)); expenses.add(expMap.getOrDefault(l, 0.0)); }
                }catch(Exception e){e.printStackTrace();}
                drawTrendChart(labels, incomes, expenses, groupDaily);
                
            } else {
                String month = monthBox.getSelectedItem() != null ? monthBox.getSelectedItem().toString() : new SimpleDateFormat("MMM yyyy").format(new Date());
                Map<String,Double> catData=new LinkedHashMap<>();
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT category,SUM(amount) s FROM transactions WHERE user_id=? AND type='Expense' AND DATE_FORMAT(trans_date,'%b %Y')=? GROUP BY category ORDER BY s DESC")){
                    ps.setInt(1,mf.userId);ps.setString(2,month);ResultSet rs=ps.executeQuery(); while(rs.next())catData.put(rs.getString("category"),rs.getDouble("s"));
                }catch(Exception e){e.printStackTrace();}
                double total=catData.values().stream().mapToDouble(d->d).sum(); drawDonut(catData,total); drawBars(catData,total); 
                
                List<String> labels=new ArrayList<>(); List<Double> incomes=new ArrayList<>(); List<Double> expenses=new ArrayList<>();
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT DATE(trans_date) as grp, type, SUM(amount) t FROM transactions WHERE user_id=? AND DATE_FORMAT(trans_date,'%b %Y')=? GROUP BY grp, type ORDER BY grp ASC")){
                    ps.setInt(1,mf.userId); ps.setString(2,month); ResultSet rs=ps.executeQuery(); Map<String, Double> incMap = new LinkedHashMap<>(); Map<String, Double> expMap = new LinkedHashMap<>();
                    while(rs.next()){ String grp = rs.getString("grp"); if(!labels.contains(grp)) labels.add(grp); if("Income".equals(rs.getString("type"))) incMap.put(grp, rs.getDouble("t")); else expMap.put(grp, rs.getDouble("t")); }
                    for(String l : labels) { incomes.add(incMap.getOrDefault(l, 0.0)); expenses.add(expMap.getOrDefault(l, 0.0)); }
                }catch(Exception e){e.printStackTrace();}
                drawTrendChart(labels, incomes, expenses, true);
            }
        }

        void drawDonut(Map<String,Double> data, double total){
            piePanel.removeAll(); if(total==0){piePanel.revalidate();piePanel.repaint();return;}
            JComponent donut=new JComponent(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    int w=getWidth(),h=getHeight(),r=Math.min(w,h)/2-12,cx=w/2,cy=h/2; int inner=(int)(r*.55); double angle=90; int ci=0;
                    for(Map.Entry<String,Double> en:data.entrySet()){ double sweep=en.getValue()/total*360; g2.setColor(CAT_COLORS[ci%CAT_COLORS.length]); g2.fill(new Arc2D.Double(cx-r,cy-r,r*2,r*2,angle,-sweep,Arc2D.PIE)); angle-=sweep; ci++; }
                    g2.setColor(card()); g2.fillOval(cx-inner,cy-inner,inner*2,inner*2); DecimalFormat df=new DecimalFormat("#,##0"); String tot="₹"+df.format(total); g2.setFont(new Font("Consolas",Font.BOLD,13)); g2.setColor(t1()); FontMetrics fm=g2.getFontMetrics(); g2.drawString(tot,cx-fm.stringWidth(tot)/2,cy+4); g2.setFont(new Font("Segoe UI",Font.BOLD,10)); g2.setColor(t2()); g2.drawString("TOTAL",cx-g2.getFontMetrics().stringWidth("TOTAL")/2,cy+18); g2.dispose();
                }
            }; donut.setOpaque(false); piePanel.setLayout(new BorderLayout()); piePanel.add(donut,BorderLayout.CENTER);
            legendPanel.removeAll(); int ci=0;
            for(Map.Entry<String,Double> en:data.entrySet()){
                int pct=(int)(en.getValue()/total*100); JPanel leg=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); leg.setOpaque(false); JLabel dot=new JLabel("●"); dot.setFont(new Font("Segoe UI",Font.BOLD,14)); dot.setForeground(CAT_COLORS[ci%CAT_COLORS.length]); JLabel name=new JLabel(en.getKey()+" "+pct+"%"); name.setFont(new Font("Segoe UI",Font.PLAIN,12)); name.setForeground(t2()); leg.add(dot); leg.add(name); legendPanel.add(leg); ci++;
            } legendPanel.revalidate(); legendPanel.repaint(); piePanel.revalidate(); piePanel.repaint();
        }

        void drawBars(Map<String,Double> data, double total){
            barPanel.removeAll(); barPanel.setLayout(new BorderLayout());
            JPanel bars=new JPanel(){
                @Override protected void paintComponent(Graphics g){
                    super.paintComponent(g); if(data.isEmpty()) return;
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    double maxVal=data.values().stream().mapToDouble(d->d).max().orElse(1); int n=data.size(), barW=(getWidth()-20)/(n==0?1:n)-8, barH=getHeight()-40, x=10; int ci=0; DecimalFormat df=new DecimalFormat("#,##0");
                    for(Map.Entry<String,Double> en:data.entrySet()){
                        int bh=(int)(en.getValue()/maxVal*barH); int y=barH-bh+10; Color c=CAT_COLORS[ci%CAT_COLORS.length]; g2.setPaint(new GradientPaint(x,y,c,x,y+bh,new Color(c.getRed(),c.getGreen(),c.getBlue(),100))); g2.fillRoundRect(x,y,barW,bh,6,6); g2.setFont(new Font("Segoe UI",Font.BOLD,10)); g2.setColor(t3()); String cat=en.getKey().length()>6?en.getKey().substring(0,5)+"…":en.getKey(); FontMetrics fm=g2.getFontMetrics(); g2.drawString(cat,x+(barW-fm.stringWidth(cat))/2,getHeight()-6); g2.setFont(new Font("Consolas",Font.BOLD,10)); g2.setColor(t2()); String val="₹"+df.format(en.getValue()); g2.drawString(val,x+(barW-g2.getFontMetrics().stringWidth(val))/2,y-4); x+=barW+8; ci++;
                    } g2.dispose();
                }
            }; bars.setBackground(card()); bars.setOpaque(true); barPanel.add(bars,BorderLayout.CENTER); barPanel.revalidate(); barPanel.repaint();
        }

        void drawTrendChart(List<String> labels, List<Double> incomes, List<Double> expenses, boolean showDayOnly){
            trendPanel.removeAll(); trendPanel.setLayout(new BorderLayout());
            JComponent chart=new JComponent(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    int pad=40, w=getWidth()-pad*2, h=getHeight()-pad*2; double maxV=Math.max(incomes.stream().mapToDouble(d->d).max().orElse(1), expenses.stream().mapToDouble(d->d).max().orElse(1)); if(maxV==0)maxV=1;
                    g2.setColor(bdr()); g2.setStroke(new BasicStroke(0.5f)); for(int i=0;i<=4;i++){int y=pad+h*i/4;g2.drawLine(pad,y,pad+w,y);}
                    int n=labels.size(); 
                    if(n > 0) {
                        int step=n==1 ? w : w/(n-1); drawLine(g2,incomes,n,step,pad,h,maxV,GREEN); drawLine(g2,expenses,n,step,pad,h,maxV,RED);
                        g2.setFont(F_TINY); g2.setColor(t3()); int labelStep = Math.max(1, n / 6); 
                        for(int i=0;i<n;i+=labelStep){ String l=labels.get(i); if(l.length() >= 8 && showDayOnly) l = l.substring(5); FontMetrics fm=g2.getFontMetrics(); g2.drawString(l,pad+i*step-fm.stringWidth(l)/2,pad+h+16); }
                    }
                    g2.setColor(GREEN); g2.fillRoundRect(pad,10,12,5,3,3); g2.setColor(t2()); g2.setFont(F_TINY); g2.drawString("Income",pad+16,16); g2.setColor(RED); g2.fillRoundRect(pad+70,10,12,5,3,3); g2.drawString("Expenses",pad+86,16); g2.dispose();
                }
                void drawLine(Graphics2D g2, List<Double> vals, int n, int step, int pad, int h, double maxV, Color color){
                    if(n==0) return; int[] xs=new int[n]; int[] ys=new int[n]; for(int i=0;i<n;i++){xs[i]=pad+i*step;ys[i]=(int)(pad+h-(vals.get(i)/maxV)*h);}
                    if(n==1) { g2.setColor(color); g2.fillOval(xs[0]-4,ys[0]-4,8,8); return; }
                    g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),60));
                    int[] fillX=new int[n+2]; int[] fillY=new int[n+2]; System.arraycopy(xs,0,fillX,0,n); System.arraycopy(ys,0,fillY,0,n); fillX[n]=xs[n-1]; fillY[n]=pad+h; fillX[n+1]=xs[0]; fillY[n+1]=pad+h; g2.fillPolygon(fillX,fillY,n+2);
                    g2.setColor(color); g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); for(int i=0;i<n-1;i++)g2.drawLine(xs[i],ys[i],xs[i+1],ys[i+1]); for(int i=0;i<n;i++){g2.setColor(card());g2.fillOval(xs[i]-4,ys[i]-4,8,8);g2.setColor(color);g2.drawOval(xs[i]-4,ys[i]-4,8,8);}
                }
            }; chart.setOpaque(false); trendPanel.add(chart,BorderLayout.CENTER); trendPanel.revalidate(); trendPanel.repaint();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAGE: GOALS
    // ══════════════════════════════════════════════════════════════════════════
    static class GoalsPage extends JPanel {
        final MainFrame mf; JPanel goalsGrid=new JPanel(); JPanel budgetGrid=new JPanel(); JPanel recurGrid=new JPanel();

        GoalsPage(MainFrame mf){this.mf=mf;setLayout(new BorderLayout(0,0));setBackground(base());buildUI();}

        void buildUI(){
            JPanel header=new JPanel(new BorderLayout()); header.setBackground(panel()); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(14,24,14,24))); JLabel title=new JLabel("Goals & Planning"); title.setFont(F_H2); title.setForeground(t1()); header.add(title,BorderLayout.WEST); add(header,BorderLayout.NORTH);
            JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(base()); content.setBorder(new EmptyBorder(20,22,20,22));

            JPanel sgHdr=new JPanel(new BorderLayout()); sgHdr.setOpaque(false); sgHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); JLabel sgTitle=new JLabel("Savings Goals"); sgTitle.setFont(F_H3); sgTitle.setForeground(t1()); JButton addGoal=ghostBtn("+ Add Goal"); addGoal.addActionListener(e->showAddGoalDialog()); sgHdr.add(sgTitle,BorderLayout.WEST); sgHdr.add(addGoal,BorderLayout.EAST); goalsGrid.setLayout(new GridLayout(0,3,12,12)); goalsGrid.setOpaque(false); goalsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
            JPanel beHdr=new JPanel(new BorderLayout()); beHdr.setOpaque(false); beHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); JLabel beTitle=new JLabel("Budget Envelopes"); beTitle.setFont(F_H3); beTitle.setForeground(t1()); JButton addBudget=ghostBtn("+ Set Budget"); addBudget.addActionListener(e->showAddBudgetDialog()); beHdr.add(beTitle,BorderLayout.WEST); beHdr.add(addBudget,BorderLayout.EAST); budgetGrid.setLayout(new GridLayout(0,3,12,12)); budgetGrid.setOpaque(false); budgetGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
            JPanel recHdr=new JPanel(new BorderLayout()); recHdr.setOpaque(false); recHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); JLabel recTitle=new JLabel("Recurring Transactions"); recTitle.setFont(F_H3); recTitle.setForeground(t1()); JButton addRec=ghostBtn("+ Add Recurring"); addRec.addActionListener(e->showAddRecurringDialog()); recHdr.add(recTitle,BorderLayout.WEST); recHdr.add(addRec,BorderLayout.EAST); recurGrid.setLayout(new BoxLayout(recurGrid,BoxLayout.Y_AXIS)); recurGrid.setOpaque(false);

            content.add(sgHdr); content.add(Box.createVerticalStrut(12)); content.add(goalsGrid); content.add(Box.createVerticalStrut(24)); content.add(beHdr); content.add(Box.createVerticalStrut(12)); content.add(budgetGrid); content.add(Box.createVerticalStrut(24)); content.add(recHdr); content.add(Box.createVerticalStrut(12)); content.add(recurGrid); add(scroll(content),BorderLayout.CENTER);
        }

        void refresh(){ refreshGoals(); refreshBudgets(); refreshRecurring(); }

        void refreshGoals(){
            goalsGrid.removeAll();
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT * FROM savings_goals WHERE user_id=? ORDER BY created_at DESC")){
                ps.setInt(1,mf.userId); ResultSet rs=ps.executeQuery();
                while(rs.next()){ int id=rs.getInt("goal_id"); String name=Security.decrypt(rs.getString("goal_name")); String emoji=rs.getString("emoji"); double target=rs.getDouble("target_amount"), saved=rs.getDouble("saved_amount"); goalsGrid.add(goalCard(id,emoji,name,saved,target)); }
            }catch(Exception e){}
            if(goalsGrid.getComponentCount()==0){JLabel empty=new JLabel("  No goals yet. Add one to start saving!"); empty.setForeground(t2()); empty.setFont(F_BODY); goalsGrid.add(empty);} goalsGrid.revalidate(); goalsGrid.repaint();
        }

        JPanel goalCard(int id, String emoji, String name, double saved, double target){
            JPanel card=cardPanel(new BorderLayout(0,8)); card.setBorder(new EmptyBorder(16,16,16,16)); double pct=target>0?Math.min(1.0,saved/target):0; JLabel emoLbl=new JLabel(emoji+" "+name); emoLbl.setFont(new Font("Segoe UI Emoji",Font.BOLD,14)); emoLbl.setForeground(t1()); DecimalFormat df=new DecimalFormat("#,##0"); JLabel amtLbl=new JLabel("₹"+df.format(saved)+" / ₹"+df.format(target)); amtLbl.setFont(F_MONO_S); amtLbl.setForeground(t2()); JLabel pctLbl=new JLabel((int)(pct*100)+"%"); pctLbl.setFont(F_BOLD); pctLbl.setForeground(pct>=1?GREEN:PURPLE2);
            JPanel bar=new JPanel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(raised()); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                    if(pct>0){ int w=(int)(getWidth()*pct); g2.setPaint(new GradientPaint(0,0,pct>=1?GREEN:PURPLE,w,0,pct>=1?new Color(0x34D399):PINK)); g2.fillRoundRect(0,0,w,getHeight(),getHeight(),getHeight()); } g2.dispose();
                }
            }; bar.setPreferredSize(new Dimension(0,8)); bar.setOpaque(false);
            JPanel botRow=new JPanel(new BorderLayout()); botRow.setOpaque(false); JButton addSaving=new JButton("+ Add"); addSaving.setFont(new Font("Segoe UI",Font.PLAIN,12)); addSaving.setForeground(PURPLE2); addSaving.setContentAreaFilled(false); addSaving.setBorderPainted(false); addSaving.setFocusPainted(false); addSaving.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addSaving.addActionListener(e->{
                String v=SettingsPage.showCustomInputDialog(mf, "Add Savings", "AMOUNT TO ADD (₹)", "");
                if(v!=null&&!v.trim().isEmpty()) try{ double add=Double.parseDouble(v); try(Connection c=conn();PreparedStatement ps=c.prepareStatement("UPDATE savings_goals SET saved_amount=saved_amount+? WHERE goal_id=?")){ps.setDouble(1,add);ps.setInt(2,id);ps.executeUpdate();} refresh(); }catch(Exception ex){}
            });
            JButton del=new JButton("×"); del.setFont(new Font("Segoe UI",Font.BOLD,15)); del.setForeground(t3()); del.setContentAreaFilled(false); del.setBorderPainted(false); del.setFocusPainted(false); del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            del.addActionListener(e->{ if(JOptionPane.showConfirmDialog(this,"Delete this goal?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){ try(Connection c=conn();PreparedStatement ps=c.prepareStatement("DELETE FROM savings_goals WHERE goal_id=?")){ps.setInt(1,id);ps.executeUpdate();refresh();}catch(Exception ex){} } });
            botRow.add(addSaving,BorderLayout.WEST); botRow.add(del,BorderLayout.EAST); card.add(emoLbl,BorderLayout.NORTH); JPanel mid=new JPanel(new BorderLayout(0,4)); mid.setOpaque(false); mid.add(amtLbl,BorderLayout.NORTH); mid.add(bar,BorderLayout.CENTER); mid.add(pctLbl,BorderLayout.SOUTH); card.add(mid,BorderLayout.CENTER); card.add(botRow,BorderLayout.SOUTH); return card;
        }

        void refreshBudgets(){
            budgetGrid.removeAll(); String curMo=new SimpleDateFormat("yyyy-MM").format(new Date());
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT b.*,COALESCE((SELECT SUM(amount) FROM transactions t WHERE t.user_id=b.user_id AND t.category=b.category AND t.type='Expense' AND DATE_FORMAT(t.trans_date,'%Y-%m')=b.month_year),0) spent FROM budgets b WHERE b.user_id=? AND b.month_year=?")){
                ps.setInt(1,mf.userId); ps.setString(2,curMo); ResultSet rs=ps.executeQuery();
                while(rs.next()){ int bid=rs.getInt("budget_id"); String cat=rs.getString("category"); double budget=rs.getDouble("budget_amount"), spent=rs.getDouble("spent"); budgetGrid.add(budgetCard(bid,cat,spent,budget)); }
            }catch(Exception e){}
            if(budgetGrid.getComponentCount()==0){JLabel e=new JLabel("  No budgets set for this month."); e.setForeground(t2()); e.setFont(F_BODY); budgetGrid.add(e);} budgetGrid.revalidate(); budgetGrid.repaint();
        }

        JPanel budgetCard(int id, String cat, double spent, double budget){
            JPanel card=cardPanel(new BorderLayout(0,8)); card.setBorder(new EmptyBorder(14,14,14,14)); double pct=budget>0?Math.min(1.0,spent/budget):0; int ci=Math.abs(cat.hashCode())%CAT_COLORS.length; JLabel catLbl=new JLabel("  "+cat); catLbl.setFont(F_BOLD); catLbl.setForeground(CAT_COLORS[ci]); DecimalFormat df=new DecimalFormat("#,##0"); JLabel amtLbl=new JLabel("₹"+df.format(spent)+" / ₹"+df.format(budget)); amtLbl.setFont(F_MONO_S); amtLbl.setForeground(t2());
            JPanel bar=new JPanel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(raised()); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                    if(pct>0){ Color c=pct>=1?RED:pct>=.75?AMBER:GREEN; g2.setColor(c); g2.fillRoundRect(0,0,(int)(getWidth()*pct),getHeight(),getHeight(),getHeight()); } g2.dispose();
                }
            }; bar.setPreferredSize(new Dimension(0,6)); bar.setOpaque(false); JPanel row=new JPanel(new BorderLayout()); row.setOpaque(false); row.add(amtLbl,BorderLayout.WEST); JLabel pctLbl=new JLabel((int)(pct*100)+"%"); pctLbl.setFont(new Font("Segoe UI",Font.BOLD,12)); pctLbl.setForeground(pct>=1?RED:pct>=.75?AMBER:GREEN); row.add(pctLbl,BorderLayout.EAST); card.add(catLbl,BorderLayout.NORTH); card.add(bar,BorderLayout.CENTER); card.add(row,BorderLayout.SOUTH); return card;
        }

        void refreshRecurring(){
            recurGrid.removeAll();
            try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT * FROM recurring WHERE user_id=? ORDER BY day_of_month")){
                ps.setInt(1,mf.userId); ResultSet rs=ps.executeQuery();
                while(rs.next()){ int rid=rs.getInt("rec_id"); double amt=rs.getDouble("amount"); String cat=rs.getString("category"); String note=Security.decrypt(rs.getString("note")); String type=rs.getString("type"); int day=rs.getInt("day_of_month"); recurGrid.add(recurRow(rid,cat,note,amt,type,day)); recurGrid.add(Box.createVerticalStrut(4)); }
            }catch(Exception e){}
            if(recurGrid.getComponentCount()==0){JLabel e=new JLabel("  No recurring transactions."); e.setForeground(t2()); e.setFont(F_BODY); recurGrid.add(e);} recurGrid.revalidate(); recurGrid.repaint();
        }

        JPanel recurRow(int id, String cat, String note, double amt, String type, int day){
            JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(10,4,10,4)));
            boolean isInc="Income".equals(type); JLabel catLbl=new JLabel(cat+(note!=null&&!note.isEmpty()?" · "+note:"")); catLbl.setFont(F_BOLD); catLbl.setForeground(t1()); JLabel dayLbl=new JLabel("Every "+day+(day==1?"st":day==2?"nd":day==3?"rd":"th")); dayLbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); dayLbl.setForeground(t2()); JPanel left=new JPanel(new BorderLayout(0,2)); left.setOpaque(false); left.add(catLbl,BorderLayout.NORTH); left.add(dayLbl,BorderLayout.SOUTH);
            JLabel amtLbl=new JLabel((isInc?"+₹":"-₹")+new DecimalFormat("#,##0.00").format(amt)); amtLbl.setFont(F_MONO_S); amtLbl.setForeground(isInc?GREEN:RED); JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); right.setOpaque(false); JButton use=new JButton("Log Now"); use.setFont(new Font("Segoe UI",Font.PLAIN,12)); use.setForeground(PURPLE2); use.setContentAreaFilled(false); use.setBorderPainted(false); use.setFocusPainted(false); use.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            use.addActionListener(e->{
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO transactions (user_id,amount,category,note,type,trans_date) VALUES(?,?,?,?,?,NOW())")){ ps.setInt(1,mf.userId);ps.setDouble(2,amt);ps.setString(3,cat); ps.setString(4,Security.encrypt(note));ps.setString(5,type);ps.executeUpdate(); mf.refreshAll(); showMessage(this,"Success","Logged!",true); }catch(Exception ex){ex.printStackTrace();}
            });
            JButton del=new JButton("×"); del.setFont(new Font("Segoe UI",Font.BOLD,14)); del.setForeground(t3()); del.setContentAreaFilled(false); del.setBorderPainted(false); del.setFocusPainted(false); del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            del.addActionListener(e->{ try(Connection c=conn();PreparedStatement ps=c.prepareStatement("DELETE FROM recurring WHERE rec_id=?")){ps.setInt(1,id);ps.executeUpdate();refresh();}catch(Exception ex){} });
            right.add(amtLbl); right.add(use); right.add(del); row.add(left,BorderLayout.CENTER); row.add(right,BorderLayout.EAST); return row;
        }

        void showAddGoalDialog(){
            JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"New Savings Goal",true); d.setSize(380,320); d.setLocationRelativeTo(this);
            JPanel body=new JPanel(new GridBagLayout()); body.setBackground(panel()); body.setBorder(new EmptyBorder(24,24,20,24)); GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;
            JTextField nameF=field(), targetF=field(), emojiF=field(); emojiF.setText("\uD83C\uDFAF"); JLabel title=new JLabel("New Goal"); title.setFont(F_H3); title.setForeground(t1());
            int r=0; gc.gridy=r++;gc.insets=new Insets(0,0,16,0); body.add(title,gc); addRow(body,gc,r++,"GOAL NAME",nameF); r++; addRow(body,gc,r++,"TARGET AMOUNT ₹",targetF); r++; addRow(body,gc,r++,"EMOJI",emojiF); r++;
            JPanel btns=new JPanel(new GridLayout(1,2,8,0)); btns.setOpaque(false); JButton cancel=ghostBtn("Cancel"), save=primaryBtn("Save"); btns.add(cancel); btns.add(save); gc.gridy=r; gc.insets=new Insets(16,0,0,0); body.add(btns,gc);
            d.add(body); cancel.addActionListener(e->d.dispose());
            save.addActionListener(e->{
                String n=nameF.getText().trim(); if(n.isEmpty()){showMessage(d,"Error","Enter a name.",false);return;}
                try{double t=Double.parseDouble(targetF.getText().trim()); try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO savings_goals (user_id,goal_name,target_amount,emoji) VALUES(?,?,?,?)")){ ps.setInt(1,mf.userId);ps.setString(2,Security.encrypt(n));ps.setDouble(3,t);ps.setString(4,emojiF.getText().trim());ps.executeUpdate();} d.dispose(); refresh();
                }catch(Exception ex){showMessage(d,"Error","Invalid amount.",false);}
            }); d.getContentPane().setBackground(panel()); d.setVisible(true);
        }

        static void addRow(JPanel p, GridBagConstraints gc, int y, String lbl, JComponent comp) { gc.gridy=y; gc.insets=new Insets(8,0,2,0); JLabel l=new JLabel(lbl.toUpperCase()); l.setFont(F_TINY); l.setForeground(t3()); p.add(l,gc); gc.gridy=y+1; gc.insets=new Insets(0,0,0,0); p.add(comp,gc); }

        void showAddBudgetDialog(){
            JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Set Budget Envelope",true); d.setSize(360,300); d.setLocationRelativeTo(this);
            JPanel body=new JPanel(new GridBagLayout()); body.setBackground(panel()); body.setBorder(new EmptyBorder(24,24,20,24)); GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;
            JComboBox<String> catBox=combo(CATEGORIES); JTextField amtF=field(); JLabel title=new JLabel("Budget Envelope"); title.setFont(F_H3); title.setForeground(t1());
            int r=0; gc.gridy=r++;gc.insets=new Insets(0,0,16,0); body.add(title,gc); addRow(body,gc,r++,"CATEGORY",catBox); r++; addRow(body,gc,r++,"MONTHLY BUDGET ₹",amtF); r++;
            JPanel btns=new JPanel(new GridLayout(1,2,8,0)); btns.setOpaque(false); JButton cancel=ghostBtn("Cancel"), save=primaryBtn("Save"); btns.add(cancel); btns.add(save); gc.gridy=r; gc.insets=new Insets(16,0,0,0); body.add(btns,gc);
            d.add(body); cancel.addActionListener(e->d.dispose());
            save.addActionListener(e->{
                try{double amt=Double.parseDouble(amtF.getText().trim()); String cat=catBox.getSelectedItem().toString(); String mo=new SimpleDateFormat("yyyy-MM").format(new Date());
                    try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO budgets (user_id,category,budget_amount,month_year) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE budget_amount=?")){ ps.setInt(1,mf.userId);ps.setString(2,cat);ps.setDouble(3,amt);ps.setString(4,mo);ps.setDouble(5,amt);ps.executeUpdate();}
                    d.dispose(); refresh();
                }catch(Exception ex){showMessage(d,"Error","Invalid amount.",false);}
            }); d.getContentPane().setBackground(panel()); d.setVisible(true);
        }

        void showAddRecurringDialog(){
            JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Recurring",true); d.setSize(380,380); d.setLocationRelativeTo(this);
            JPanel body=new JPanel(new GridBagLayout()); body.setBackground(panel()); body.setBorder(new EmptyBorder(24,24,20,24)); GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;
            JComboBox<String> catBox=combo(CATEGORIES); JTextField amtF=field(), noteF=field(), dayF=field(); dayF.setText("1"); JComboBox<String> typeBox=combo(new String[]{"Expense","Income"});
            JLabel title=new JLabel("Recurring Transaction"); title.setFont(F_H3); title.setForeground(t1()); int r=0; gc.gridy=r++;gc.insets=new Insets(0,0,16,0); body.add(title,gc);
            addRow(body,gc,r++,"CATEGORY",catBox); r++; addRow(body,gc,r++,"AMOUNT ₹",amtF); r++; addRow(body,gc,r++,"NOTE",noteF); r++; addRow(body,gc,r++,"DAY OF MONTH",dayF); r++; addRow(body,gc,r++,"TYPE",typeBox); r++;
            JPanel btns=new JPanel(new GridLayout(1,2,8,0)); btns.setOpaque(false); JButton cancel=ghostBtn("Cancel"), save=primaryBtn("Save"); btns.add(cancel); btns.add(save); gc.gridy=r; gc.insets=new Insets(16,0,0,0); body.add(btns,gc);
            d.add(body); cancel.addActionListener(e->d.dispose());
            save.addActionListener(e->{
                try{double amt=Double.parseDouble(amtF.getText().trim()); int day=Integer.parseInt(dayF.getText().trim()); String cat=catBox.getSelectedItem().toString(), type=typeBox.getSelectedItem().toString(); String note=Security.encrypt(noteF.getText().trim());
                    try(Connection c=conn();PreparedStatement ps=c.prepareStatement("INSERT INTO recurring (user_id,amount,category,note,type,day_of_month) VALUES(?,?,?,?,?,?)")){ ps.setInt(1,mf.userId);ps.setDouble(2,amt);ps.setString(3,cat);ps.setString(4,note);ps.setString(5,type);ps.setInt(6,day);ps.executeUpdate();}
                    d.dispose(); refresh();
                }catch(Exception ex){showMessage(d,"Error","Invalid input.",false);}
            }); d.getContentPane().setBackground(panel()); d.setVisible(true);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAGE: SETTINGS
    // ══════════════════════════════════════════════════════════════════════════
    static class SettingsPage extends JPanel {
        final MainFrame mf;

        SettingsPage(MainFrame mf){this.mf=mf;setLayout(new BorderLayout(0,0));setBackground(base());buildUI();}

        static String showCustomInputDialog(Frame parent, String title, String prompt, String initialValue) {
            JDialog dlg = new JDialog(parent, title, true); dlg.setUndecorated(true); dlg.setSize(340, 220); dlg.setLocationRelativeTo(parent);
            JPanel p = new JPanel(new GridBagLayout()); p.setBackground(panel()); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(20,20,20,20)));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(0,0,10,0); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(F_H3); lblTitle.setForeground(t1()); JLabel lblPrompt = new JLabel(prompt.toUpperCase()); lblPrompt.setFont(F_TINY); lblPrompt.setForeground(t3());
            JTextField tf = field(); tf.setText(initialValue);
            JPanel btns = new JPanel(new GridLayout(1,2,10,0)); btns.setOpaque(false); JButton cancel = ghostBtn("Cancel"); JButton ok = primaryBtn("OK"); btns.add(cancel); btns.add(ok);
            int r=0; gbc.gridy=r++; gbc.insets = new Insets(0,0,16,0); p.add(lblTitle, gbc); gbc.gridy=r++; gbc.insets = new Insets(0,0,4,0); p.add(lblPrompt, gbc); gbc.gridy=r++; gbc.insets = new Insets(0,0,24,0); p.add(tf, gbc); gbc.gridy=r++; gbc.insets = new Insets(0,0,0,0); p.add(btns, gbc);
            final String[] res = {null}; cancel.addActionListener(e -> dlg.dispose()); ok.addActionListener(e -> { res[0] = tf.getText().trim(); dlg.dispose(); });
            dlg.add(p); dlg.setVisible(true); return res[0];
        }

        void buildUI(){
            JPanel header=new JPanel(new BorderLayout()); header.setBackground(panel()); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(14,24,14,24)));
            JLabel title=new JLabel("Settings"); title.setFont(F_H2); title.setForeground(t1()); header.add(title,BorderLayout.WEST); add(header,BorderLayout.NORTH);
            JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(base()); content.setBorder(new EmptyBorder(22,24,24,24));

            content.add(sectionTitle("Profile & Preferences")); content.add(Box.createVerticalStrut(10));
            
            JLabel userLbl = new JLabel(mf.userName); userLbl.setFont(F_BODY); userLbl.setForeground(t2());
            JButton editUnameBtn = ghostBtn("Edit"); editUnameBtn.addActionListener(e -> showSecurityUpdateDialog("USERNAME"));
            content.add(settingRow("Username", userLbl, editUnameBtn));

            JLabel passLbl = new JLabel("••••••••"); passLbl.setFont(F_MONO_S); passLbl.setForeground(t2());
            JButton editPassBtn = ghostBtn("Edit"); editPassBtn.addActionListener(e -> showSecurityUpdateDialog("PASSWORD"));
            content.add(settingRow("Password", passLbl, editPassBtn));

            content.add(settingRow("Theme",buildThemeToggle())); content.add(Box.createVerticalStrut(20));

            content.add(sectionTitle("Budget & Limits")); content.add(Box.createVerticalStrut(10));
            JLabel targetVal=new JLabel("₹"+new DecimalFormat("#,##0.00").format(mf.monthlyTarget)); targetVal.setFont(F_MONO_S); targetVal.setForeground(PURPLE2);
            JButton editTarget=ghostBtn("Edit");
            editTarget.addActionListener(e->{
                String v = showCustomInputDialog(mf, "Monthly Budget", "MONTHLY SPENDING TARGET (₹)", String.valueOf(mf.monthlyTarget));
                if(v!=null&&!v.isEmpty()) try{mf.monthlyTarget=Double.parseDouble(v);
                    try(Connection c=conn();PreparedStatement ps=c.prepareStatement("UPDATE users SET monthly_target=? WHERE user_id=?")){ps.setDouble(1,mf.monthlyTarget);ps.setInt(2,mf.userId);ps.executeUpdate();}
                    targetVal.setText("₹"+new DecimalFormat("#,##0.00").format(mf.monthlyTarget)); mf.refreshAll();
                }catch(Exception ex){}
            });
            content.add(settingRow("Monthly Target",targetVal,editTarget));

            JLabel limitVal=new JLabel("₹"+new DecimalFormat("#,##0.00").format(mf.dailyLimit)); limitVal.setFont(F_MONO_S); limitVal.setForeground(AMBER);
            JButton editLimit=ghostBtn("Edit");
            editLimit.addActionListener(e->{
                String v = showCustomInputDialog(mf, "Daily Limit", "DAILY SPENDING LIMIT (₹)", String.valueOf(mf.dailyLimit));
                if(v!=null&&!v.isEmpty()) try{mf.dailyLimit=Double.parseDouble(v);
                    try(Connection c=conn();PreparedStatement ps=c.prepareStatement("UPDATE users SET daily_limit=? WHERE user_id=?")){ps.setDouble(1,mf.dailyLimit);ps.setInt(2,mf.userId);ps.executeUpdate();}
                    limitVal.setText("₹"+new DecimalFormat("#,##0.00").format(mf.dailyLimit)); mf.refreshAll();
                }catch(Exception ex){}
            });
            content.add(settingRow("Daily Limit",limitVal,editLimit)); content.add(Box.createVerticalStrut(20));

            content.add(sectionTitle("Streak Tracking")); content.add(Box.createVerticalStrut(10));
            JLabel streakCatVal=new JLabel(mf.streakCategories.isEmpty()?"None":mf.streakCategories); streakCatVal.setFont(F_BODY); streakCatVal.setForeground(t2());
            JButton editStreak=ghostBtn("Edit");
            editStreak.addActionListener(e->{
                JDialog d = new JDialog(mf, "Streak Categories", true); d.setUndecorated(true); d.setSize(400, 360); d.setLocationRelativeTo(mf);
                JPanel p = new JPanel(new BorderLayout()); p.setBackground(panel()); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(24,24,24,24)));
                JLabel titleLbl = new JLabel("Track for Streak"); titleLbl.setFont(F_H2); titleLbl.setForeground(t1()); JLabel subLbl = new JLabel("SELECT CATEGORIES"); subLbl.setFont(F_TINY); subLbl.setForeground(t3());
                JPanel top = new JPanel(new GridLayout(2,1,0,4)); top.setOpaque(false); top.add(titleLbl); top.add(subLbl); top.setBorder(new EmptyBorder(0,0,16,0));
                JPanel grid = new JPanel(new GridLayout(0,2,8,8)); grid.setOpaque(false); JCheckBox[] boxes = new JCheckBox[CATEGORIES.length];
                for(int i=0;i<CATEGORIES.length;i++){
                    boxes[i] = new JCheckBox(CATEGORIES[i]); boxes[i].setFont(F_BODY); boxes[i].setBackground(panel()); boxes[i].setForeground(t2()); boxes[i].setFocusPainted(false);
                    if(mf.streakCategories.contains(CATEGORIES[i])) boxes[i].setSelected(true); grid.add(boxes[i]);
                }
                JPanel btns = new JPanel(new GridLayout(1,2,12,0)); btns.setOpaque(false); btns.setBorder(new EmptyBorder(20,0,0,0)); JButton cancel = ghostBtn("Cancel"); JButton save = primaryBtn("Save");
                cancel.addActionListener(cE -> d.dispose());
                save.addActionListener(sE -> {
                    StringBuilder sb = new StringBuilder(); for(JCheckBox b:boxes) if(b.isSelected()) { if(sb.length()>0) sb.append(","); sb.append(b.getText()); }
                    mf.streakCategories = sb.toString();
                    try(Connection c = conn(); PreparedStatement ps = c.prepareStatement("UPDATE users SET streak_categories=? WHERE user_id=?")){ ps.setString(1, mf.streakCategories); ps.setInt(2, mf.userId); ps.executeUpdate(); } catch(Exception ex){}
                    streakCatVal.setText(mf.streakCategories.isEmpty() ? "None" : mf.streakCategories); mf.refreshAll(); d.dispose();
                });
                btns.add(cancel); btns.add(save); p.add(top, BorderLayout.NORTH); p.add(grid, BorderLayout.CENTER); p.add(btns, BorderLayout.SOUTH); d.add(p); d.setVisible(true);
            });
            content.add(settingRow("Streak Categories",streakCatVal,editStreak)); content.add(Box.createVerticalStrut(20));

            content.add(sectionTitle("Account")); content.add(Box.createVerticalStrut(10));
            JButton logout=dangerBtn("← Logout"); logout.setAlignmentX(Component.LEFT_ALIGNMENT); logout.addActionListener(e->{mf.dispose();new AuthFrame().setVisible(true);});
            JPanel logRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); logRow.setOpaque(false); logRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
            logRow.add(logout); content.add(logRow);

            add(scroll(content),BorderLayout.CENTER);
        }
        
        void showSecurityUpdateDialog(String mode) {
            String titleStr = mode.equals("USERNAME") ? "Update Username" : "Update Password";
            JDialog dlg = new JDialog(mf, titleStr, true); dlg.setUndecorated(true); dlg.setSize(380, 420); dlg.setLocationRelativeTo(mf);
            CardLayout cl = new CardLayout(); JPanel cards = new JPanel(cl); cards.setOpaque(false); String[] generatedOtp = new String[1];
            
            // CARD 1
            JPanel p1 = new JPanel(new GridBagLayout()); p1.setBackground(panel()); p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(20,20,20,20)));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(0,0,10,0); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; int r = 0;
            gbc.gridy = r++; p1.add(new JLabel("Security Verification"){ {setFont(F_H2); setForeground(t1());} }, gbc);
            gbc.gridy = r++; p1.add(micro("REGISTERED EMAIL"), gbc); JTextField emailF = field(); gbc.gridy = r++; p1.add(emailF, gbc);
            JPanel b1 = new JPanel(new GridLayout(1,2,10,0)); b1.setOpaque(false); JButton c1 = ghostBtn("Cancel"); JButton n1 = primaryBtn("Send OTP");
            b1.add(c1); b1.add(n1); gbc.gridy = r++; gbc.insets = new Insets(10,0,0,0); p1.add(b1, gbc);
            
            // CARD 2
            JPanel p2 = new JPanel(new GridBagLayout()); p2.setBackground(panel()); p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(20,20,20,20)));
            r=0; gbc.insets = new Insets(0,0,10,0); gbc.gridy = r++; p2.add(new JLabel("Enter OTP"){ {setFont(F_H2); setForeground(t1());} }, gbc);
            gbc.gridy = r++; p2.add(micro("6-DIGIT OTP"), gbc); JTextField otpF = field(); gbc.gridy = r++; p2.add(otpF, gbc);
            JPanel b2 = new JPanel(new GridLayout(1,2,10,0)); b2.setOpaque(false); JButton c2 = ghostBtn("Cancel"); JButton n2 = primaryBtn("Verify");
            b2.add(c2); b2.add(n2); gbc.gridy = r++; gbc.insets = new Insets(10,0,0,0); p2.add(b2, gbc);

            // CARD 3
            JPanel p3 = new JPanel(new GridBagLayout()); p3.setBackground(panel()); p3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bdr(), 1), new EmptyBorder(20,20,20,20)));
            r=0; gbc.insets = new Insets(0,0,10,0); gbc.gridy = r++; p3.add(new JLabel(titleStr){ {setFont(F_H2); setForeground(t1());} }, gbc);
            
            JTextField newUF = field(); JPasswordField newPF = passField();
            if(mode.equals("USERNAME")) {
                gbc.gridy = r++; gbc.insets = new Insets(0,0,2,0); p3.add(micro("NEW USERNAME"), gbc);
                gbc.gridy = r++; gbc.insets = new Insets(0,0,20,0); p3.add(newUF, gbc);
            } else {
                gbc.gridy = r++; gbc.insets = new Insets(0,0,2,0); p3.add(micro("NEW PASSWORD"), gbc);
                gbc.gridy = r++; gbc.insets = new Insets(0,0,20,0); p3.add(newPF, gbc);
            }

            JPanel b3 = new JPanel(new GridLayout(1,2,10,0)); b3.setOpaque(false); JButton c3 = ghostBtn("Cancel"); JButton saveBtn = primaryBtn("Save");
            b3.add(c3); b3.add(saveBtn); gbc.gridy = r++; gbc.insets = new Insets(0,0,0,0); p3.add(b3, gbc);

            cards.add(p1, "C1"); cards.add(p2, "C2"); cards.add(p3, "C3");
            ActionListener closeAct = e -> dlg.dispose(); c1.addActionListener(closeAct); c2.addActionListener(closeAct); c3.addActionListener(closeAct);

            n1.addActionListener(e -> {
                String em = emailF.getText().trim();
                try(Connection c=conn();PreparedStatement ps=c.prepareStatement("SELECT 1 FROM users WHERE user_id=? AND email=?")){
                    ps.setInt(1, mf.userId); ps.setString(2, Security.encrypt(em)); ResultSet rs = ps.executeQuery();
                    if(rs.next()){
                        generatedOtp[0] = String.format("%06d", new Random().nextInt(999999));
                        try {
                            sendOTPEmail(em, generatedOtp[0]);
                            showMessage(dlg, "Email Verification", "OTP successfully sent to your email:\n" + em, true);
                            cl.show(cards, "C2");
                        } catch (Exception ex) {
                            showMessage(dlg, "Email Error", "Failed to send email.\nPlease check your App Password settings.", false);
                            ex.printStackTrace();
                        }
                    } else { showMessage(dlg, "Error", "Incorrect registered email.", false); }
                }catch(Exception ex){}
            });

            n2.addActionListener(e -> {
                if(otpF.getText().trim().equals(generatedOtp[0])){ cl.show(cards, "C3"); } else { showMessage(dlg, "Error", "Invalid OTP.", false); }
            });

            saveBtn.addActionListener(e -> {
                try(Connection c=conn()) {
                    if(mode.equals("USERNAME")){
                        String nu = newUF.getText().trim(); if(nu.isEmpty()) { showMessage(dlg, "Error", "Username cannot be empty.", false); return; }
                        PreparedStatement ps=c.prepareStatement("UPDATE users SET username=? WHERE user_id=?"); ps.setString(1, Security.encrypt(nu)); ps.setInt(2, mf.userId); ps.executeUpdate();
                    } else {
                        String np = new String(newPF.getPassword()).trim(); if(np.isEmpty()) { showMessage(dlg, "Error", "Password cannot be empty.", false); return; }
                        PreparedStatement ps=c.prepareStatement("UPDATE users SET password=? WHERE user_id=?"); ps.setString(1, Security.hash(np)); ps.setInt(2, mf.userId); ps.executeUpdate();
                    }
                    showMessage(dlg, "Success", "Credentials updated successfully.\nPlease log in again.", true);
                    dlg.dispose(); mf.dispose(); new AuthFrame().setVisible(true);
                }catch(Exception ex){ showMessage(dlg, "Error", "Update failed (Username may be taken).", false); }
            });
            dlg.add(cards); dlg.setVisible(true);
        }

        JComponent buildThemeToggle(){
            JButton toggle=new JButton(dark?"\uD83C\uDF19  Dark":"☀  Light"){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0,0,dark?new Color(0x1A0A2E):new Color(0xFFF8DC),getWidth(),0,dark?new Color(0x2A1060):new Color(0xFFF0B0))); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    g2.setColor(dark?PURPLE2:AMBER); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
                }
            };
            toggle.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13)); toggle.setForeground(dark?PURPLE2:AMBER); toggle.setContentAreaFilled(false); toggle.setBorderPainted(false); toggle.setFocusPainted(false); toggle.setBorder(new EmptyBorder(8,16,8,16)); toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            toggle.addActionListener(e->{ dark=!dark; applyUIDefaults(); try(Connection c=conn();PreparedStatement ps=c.prepareStatement("UPDATE users SET theme=? WHERE user_id=?")){ps.setString(1,dark?"dark":"light");ps.setInt(2,mf.userId);ps.executeUpdate();}catch(Exception ex){} int uid=mf.userId; String uname=mf.userName; mf.dispose(); new MainFrame(uid,uname).setVisible(true); }); return toggle;
        }

        JLabel sectionTitle(String t){JLabel l=new JLabel(t.toUpperCase());l.setFont(new Font("Segoe UI",Font.BOLD,11));l.setForeground(t3());l.setBorder(new EmptyBorder(0,0,0,0));l.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));return l;}
        JPanel settingRow(String label, JComponent val){return settingRow(label,val,null);}
        JPanel settingRow(String label, JComponent val, JButton action){
            JPanel row=new JPanel(new BorderLayout(12,0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,54)); row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,bdr()),new EmptyBorder(12,0,12,0)));
            JLabel lbl=new JLabel(label); lbl.setFont(F_BODY); lbl.setForeground(t1()); lbl.setPreferredSize(new Dimension(200,20));
            if (val instanceof JLabel) {
                JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); leftSide.setOpaque(false);
                leftSide.add(lbl); leftSide.setPreferredSize(new Dimension(200, 20));
                row.add(leftSide, BorderLayout.WEST);
                JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); rightSide.setOpaque(false);
                rightSide.add(val); if(action!=null) rightSide.add(action);
                row.add(rightSide, BorderLayout.CENTER);
            } else {
                row.add(lbl,BorderLayout.WEST); row.add(val,BorderLayout.CENTER); if(action!=null) row.add(action,BorderLayout.EAST); 
            }
            return row;
        }
    }
}