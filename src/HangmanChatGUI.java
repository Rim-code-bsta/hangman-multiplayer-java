/*
 * HangmanChatGUI.java
 * Royal Court — Moroccan themed Multiplayer Hangman GUI
 * @author Rim
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.Arc2D;
import java.awt.geom.QuadCurve2D;


public class HangmanChatGUI extends JFrame {
    
    // The Colors Inspired by the Moroccan Royal Court
    private static final Color BG_DEEP         = new Color(13,  31,  20);
    private static final Color BG_PANEL        = new Color(9,   20,  13);
    private static final Color BG_CARD         = new Color(18,  42,  28);
    private static final Color EMERALD         = new Color(27,  67,  50);
    private static final Color GOLD            = new Color(255, 215,   0);
    private static final Color GOLD_DIM        = new Color(180, 148,  60);
    private static final Color CRIMSON         = new Color(139,   0,   0);
    private static final Color CRIMSON_BRIGHT  = new Color(220,  20,  60);
    private static final Color IVORY           = new Color(232, 213, 163);
    private static final Color IVORY_DIM       = new Color(140, 125,  90);
    private static final Color GREEN_CORRECT   = new Color(30,  120,  60);
    private static final Color GREEN_TEXT      = new Color(80,  210, 120);
    private static final Color TARBOUCH_RED    = new Color(139,  15,  15);
    
    // Palette for the Moroccan Zellige
    
    private static final int[] ZELLIGE_HEX = {
        0x8B0000, 0xFFD700, 0x1B4332, 0xC4762A,
        0xFFD700, 0x6B3A8B, 0x1B4332, 0x8B0000
    };
    private static final int ZELLIGE_CELL = 12;
    
    // The actual state
    private ChatClient client;
    private String role        = "UNKNOWN";
    private String currentWord = "";
    private int    attemptsLeft = 6;
    private char   lastGuess   = 0;
    
    // The layout
    
    private CardLayout cardLayout;
    private JPanel     cardPanel;
    
    // The Setter View
    
    private JPasswordField setterWordField;
    private JButton        setterSendBtn;
    private JLabel         setterStatusLbl;
    
    
    //  The Guesser View
    
    private GallowsPanel   gallowsPanel;
    private WordTilesPanel wordTilesPanel;
    private JLabel         attemptsLbl;
    private JLabel         flavorLbl;
    private JPanel         wrongGridPanel;
    private JButton[]      letterBtns = new JButton[26];
    
    // Flavor Text
    
    private static final String[] FLAVOR_CORRECT = {
        "— The Royal Court applauds your wisdom. —",
        "— A letter emerges from the shadows. —",
        "— Fortune favors the scholar of words. —",
        "— The golden tiles shift in your favor. —"
    };
    private static final String[] FLAVOR_WRONG = {
        "— The court grows restless. Choose carefully. —",
        "— Another misstep in the palace halls. —",
        "— The crimson banner sways in warning. —",
        "— The noose tightens above the mosaic floor. —",
        "— Silence falls over the royal chamber. —"
    };
    private static final String[] FLAVOR_IDLE = {
        "— The Royal Court awaits your answer. Choose wisely. —",
        "— Lanterns flicker across the zellige tiles. —",
        "— Between each guess, an eternity echoes. —"
    };
    
    // Constructor
    
    public HangmanChatGUI() {
        initWindow();
        buildLayout();
        showConnectionDialog();
        setVisible(true);
    }
 
    private void initWindow() {
        setTitle("The Gallows  ·  Royal Court  ·  Morocco ");
        setSize(960, 700);
        setMinimumSize(new Dimension(820, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DEEP);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
    }
    
    // Layout
    
        private void buildLayout() {
        add(new ZelligeStrip(), BorderLayout.NORTH);
 
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DEEP);
        center.add(buildHeader(), BorderLayout.NORTH);
 
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DEEP);
        cardPanel.add(buildWaitingView(), "WAITING");
        cardPanel.add(buildSetterView(),  "SETTER");
        cardPanel.add(buildGuesserView(), "GUESSER");
        center.add(cardPanel, BorderLayout.CENTER);
 
        add(center, BorderLayout.CENTER);
        add(new ZelligeStrip(), BorderLayout.SOUTH);
 
        cardLayout.show(cardPanel, "WAITING");
    }
        
    // Header
    
        private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 215, 0, 12));
                for (int x = 0; x < getWidth(); x += 42) g2.drawLine(x, 0, x, getHeight());
                g2.dispose();
            }
        };
        header.setBackground(BG_PANEL);
        header.setBorder(new MatteBorder(0, 0, 2, 0, GOLD));
        header.setOpaque(true);
 
        JLabel crown = new JLabel("♛  ✦  ♛", SwingConstants.CENTER);
        crown.setFont(royalFont(14, Font.PLAIN));
        crown.setForeground(new Color(255, 215, 0, 110));
        crown.setBorder(new EmptyBorder(10, 0, 2, 0));
 
        JLabel title = new JLabel("THE  GALLOWS", SwingConstants.CENTER);
        title.setFont(royalFont(26, Font.BOLD));
        title.setForeground(GOLD);
        title.setBorder(new EmptyBorder(0, 0, 2, 0));
 
        JLabel sub = new JLabel("Royal Court of Words  ·  Est. Marrakech", SwingConstants.CENTER);
        sub.setFont(royalFont(10, Font.PLAIN));
        sub.setForeground(new Color(255, 215, 0, 95));
        sub.setBorder(new EmptyBorder(0, 0, 12, 0));
 
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.add(crown); stack.add(title); stack.add(sub);
        header.add(stack, BorderLayout.CENTER);
        return header;
    }
        
    // The Waiting view
    
        private JPanel buildWaitingView() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DEEP);
 
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(EMERALD, 1), new EmptyBorder(40, 60, 40, 60)));
 
        JLabel icon = lbl("♛", royalFont(36, Font.PLAIN), GOLD);
        JLabel msg  = lbl("Awaiting the second player...", royalFont(16, Font.PLAIN), IVORY);
        JLabel sub  = lbl("The lanterns are lit. The court stands ready.", georgeFont(12, Font.ITALIC), IVORY_DIM);
        icon.setBorder(new EmptyBorder(0,0,16,0));
        msg.setBorder(new EmptyBorder(0,0,10,0));
        icon.setAlignmentX(CENTER_ALIGNMENT);
        msg.setAlignmentX(CENTER_ALIGNMENT);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(icon); card.add(msg); card.add(sub);
        p.add(card);
        return p;
    }
        
    // The view of the Setter of the word that should be guessed
        
       private JPanel buildSetterView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DEEP);
 
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(GOLD_DIM, 1), new EmptyBorder(40, 56, 40, 56)));
        card.setMaximumSize(new Dimension(520, 460));
 
        JLabel badge   = lbl("✦  WORD SETTER  ✦",         royalFont(10, Font.PLAIN),   GOLD_DIM);
        JLabel heading = lbl("Inscribe the Secret Word",   royalFont(20, Font.BOLD),    GOLD);
        JLabel instruct= lbl("Your opponent shall attempt to guess it, letter by letter.",
                              georgeFont(12, Font.ITALIC), IVORY_DIM);
        badge.setBorder(new EmptyBorder(0,0,22,0));
        heading.setBorder(new EmptyBorder(0,0,8,0));
        instruct.setBorder(new EmptyBorder(0,0,28,0));
        badge.setAlignmentX(CENTER_ALIGNMENT);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        instruct.setAlignmentX(CENTER_ALIGNMENT);
 
        setterWordField = new JPasswordField(18);
        setterWordField.setFont(royalFont(18, Font.PLAIN));
        setterWordField.setBackground(BG_PANEL);
        setterWordField.setForeground(GOLD);
        setterWordField.setCaretColor(GOLD);
        setterWordField.setEchoChar('◆');
        setterWordField.setBorder(new CompoundBorder(
            new MatteBorder(0,0,2,0, GOLD_DIM), new EmptyBorder(10,14,10,14)));
        setterWordField.setMaximumSize(new Dimension(400, 56));
        setterWordField.setAlignmentX(CENTER_ALIGNMENT);
        setterWordField.addActionListener(e -> sendWord());
 
        JLabel hint = lbl("Letters only  ·  Minimum 3 characters", royalFont(9, Font.PLAIN), IVORY_DIM);
        hint.setBorder(new EmptyBorder(6,0,24,0));
        hint.setAlignmentX(CENTER_ALIGNMENT);
 
        setterSendBtn = new JButton("♛  Seal the Royal Word");
        styleRoyalButton(setterSendBtn);
        setterSendBtn.setAlignmentX(CENTER_ALIGNMENT);
        setterSendBtn.addActionListener(e -> sendWord());
 
        setterStatusLbl = lbl("", georgeFont(11, Font.ITALIC), IVORY_DIM);
        setterStatusLbl.setBorder(new EmptyBorder(18,0,0,0));
        setterStatusLbl.setAlignmentX(CENTER_ALIGNMENT);
 
        card.add(badge); card.add(heading); card.add(instruct);
        card.add(Box.createRigidArea(new Dimension(0,4)));
        card.add(setterWordField); card.add(hint);
        card.add(setterSendBtn);  card.add(setterStatusLbl);
 
        outer.add(card);
        return outer;
    }
       
     // The view of the Guesser of the word
       
        private JPanel buildGuesserView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        
        // The left side
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG_PANEL);
        left.setBorder(new CompoundBorder(
            new MatteBorder(0,0,0,2, EMERALD), new EmptyBorder(16,14,16,14)));
        left.setPreferredSize(new Dimension(260, 0));
 
        JLabel roleBadge = lbl("✦  GUESSER  ✦", royalFont(9, Font.PLAIN), GOLD_DIM);
        roleBadge.setBorder(new EmptyBorder(0,0,12,0));
        roleBadge.setAlignmentX(CENTER_ALIGNMENT);
 
        gallowsPanel = new GallowsPanel();
        gallowsPanel.setAlignmentX(CENTER_ALIGNMENT);
 
        attemptsLbl = lbl("Pages remaining: 6", georgeFont(12, Font.ITALIC), GOLD_DIM);
        attemptsLbl.setBorder(new EmptyBorder(8,0,14,0));
        attemptsLbl.setAlignmentX(CENTER_ALIGNMENT);
 
        StarDivider starDiv = new StarDivider();
        starDiv.setAlignmentX(CENTER_ALIGNMENT);
        starDiv.setMaximumSize(new Dimension(220, 16));
 
        JLabel wrongTitle = lbl("WRONG GUESSES", royalFont(8, Font.PLAIN), new Color(255,215,0,80));
        wrongTitle.setBorder(new EmptyBorder(10,0,8,0));
        wrongTitle.setAlignmentX(CENTER_ALIGNMENT);
 
        wrongGridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
        wrongGridPanel.setBackground(BG_PANEL);
        wrongGridPanel.setMaximumSize(new Dimension(230, 120));
        wrongGridPanel.setAlignmentX(CENTER_ALIGNMENT);
 
        left.add(roleBadge); left.add(gallowsPanel); left.add(attemptsLbl);
        left.add(starDiv);   left.add(wrongTitle);   left.add(wrongGridPanel);
        
        // the right side
        
        JPanel right = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(27, 67, 50, 28));
                for (int x = 0; x < getWidth();  x += 40) g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 40) g2.drawLine(0, y, getWidth(), y);
                g2.dispose();
            }
        };
        right.setBackground(BG_DEEP);
        right.setOpaque(true);
        right.setBorder(new EmptyBorder(20, 28, 16, 28));
 
        flavorLbl = lbl(FLAVOR_IDLE[0], georgeFont(12, Font.ITALIC), new Color(255,215,0,90));
 
        wordTilesPanel = new WordTilesPanel();
        wordTilesPanel.setPreferredSize(new Dimension(0, 140));
 
        JPanel centerPane = new JPanel(new BorderLayout());
        centerPane.setOpaque(false);
        centerPane.add(flavorLbl,      BorderLayout.NORTH);
        centerPane.add(wordTilesPanel, BorderLayout.CENTER);
 
        right.add(centerPane,      BorderLayout.CENTER);
        right.add(buildKeyboard(), BorderLayout.SOUTH);
 
        root.add(left,  BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        return root;
}       
        // keyboard
        
        private JPanel buildKeyboard() {
        JPanel kb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Gold gradient separator
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(255,215,0,0), getWidth()/2f,0,new Color(255,215,0,130),false));
                g2.fillRect(0, 0, getWidth()/2, 2);
                g2.setPaint(new GradientPaint(getWidth()/2f,0,new Color(255,215,0,130), getWidth(),0,new Color(255,215,0,0),false));
                g2.fillRect(getWidth()/2, 0, getWidth()/2, 2);
                g2.dispose();
            }
        };
        kb.setBackground(BG_DEEP);
        kb.setLayout(new BoxLayout(kb, BoxLayout.Y_AXIS));
        kb.setBorder(new EmptyBorder(14, 0, 4, 0));
 
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        int idx = 0;
        for (String row : rows) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
            rowPanel.setOpaque(false);
            for (char c : row.toCharArray()) {
                final char fc = c;
                JButton btn = new JButton(String.valueOf(c)) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color bg = (Color) getClientProperty("keyBg");
                        if (bg == null) bg = new Color(27,67,50,100);
                        Color bc = (Color) getClientProperty("keyBorder");
                        if (bc == null) bc = new Color(255,215,0,50);
                        int w = getWidth(), h = getHeight(), bv = 5;
                        int[] xs = {bv,w-bv,w,w,w-bv,bv,0,0};
                        int[] ys = {0,0,bv,h-bv,h,h,h-bv,bv};
                        g2.setColor(bg);
                        g2.fillPolygon(xs, ys, 8);
                        g2.setColor(bc);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawPolygon(xs, ys, 8);
                        g2.setColor(getForeground());
                        g2.setFont(getFont());
                        FontMetrics fm = g2.getFontMetrics();
                        int sw = fm.stringWidth(getText());
                        g2.drawString(getText(), (w-sw)/2, (h+fm.getAscent()-fm.getDescent())/2);
                        g2.dispose();
                    }
                    @Override protected void paintBorder(Graphics g) {}
                };
                btn.setFont(royalFont(11, Font.PLAIN));
                btn.setForeground(IVORY);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(38, 38));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.putClientProperty("keyBg",     new Color(27,67,50,100));
                btn.putClientProperty("keyBorder", new Color(255,215,0,50));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (!btn.isEnabled()) return;
                        btn.putClientProperty("keyBg",     new Color(255,215,0,35));
                        btn.putClientProperty("keyBorder", new Color(255,215,0,160));
                        btn.setForeground(GOLD); btn.repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        if (!btn.isEnabled()) return;
                        btn.putClientProperty("keyBg",     new Color(27,67,50,100));
                        btn.putClientProperty("keyBorder", new Color(255,215,0,50));
                        btn.setForeground(IVORY); btn.repaint();
                    }
                });
                btn.addActionListener(e -> {
                    lastGuess = Character.toLowerCase(fc);
                    client.sendMessage("GUESS " + fc);
                    btn.setEnabled(false);
                });
                letterBtns[idx++] = btn;
                rowPanel.add(btn);
            }
            kb.add(rowPanel);
        }
        return kb;
    }
        
    // Connection Dialogue now
        
        private void showConnectionDialog() {
        JDialog dialog = new JDialog(this, "Enter the Royal Court", true);
        dialog.setLayout(new BorderLayout(10,10));
        dialog.getContentPane().setBackground(BG_PANEL);
        dialog.setSize(440, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
 
        JLabel title = new JLabel("♛  Enter the Royal Court  ♛", SwingConstants.CENTER);
        title.setFont(royalFont(16, Font.BOLD));
        title.setForeground(GOLD);
        title.setBorder(new EmptyBorder(22,20,4,20));
 
        JLabel sub = new JLabel("Identify yourself, scholar.", SwingConstants.CENTER);
        sub.setFont(georgeFont(11, Font.ITALIC));
        sub.setForeground(IVORY_DIM);
        sub.setBorder(new EmptyBorder(0,0,14,0));
 
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_PANEL);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub,   BorderLayout.CENTER);
 
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 16));
        form.setBackground(BG_PANEL);
        form.setBorder(new EmptyBorder(10, 34, 10, 34));
 
        JTextField serverField   = dialogField("localhost");
        JTextField portField     = dialogField("8989");
        JTextField usernameField = dialogField("");
 
        form.add(dialogLbl("Server"));     form.add(serverField);
        form.add(dialogLbl("Port"));       form.add(portField);
        form.add(dialogLbl("Your Name")); form.add(usernameField);
 
        JButton connectBtn = new JButton("♛  Open the Gate");
        styleRoyalButton(connectBtn);
        connectBtn.setAlignmentX(CENTER_ALIGNMENT);
 
        JLabel errLbl = new JLabel("", SwingConstants.CENTER);
        errLbl.setFont(royalFont(10, Font.PLAIN));
        errLbl.setForeground(CRIMSON_BRIGHT);
 
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG_PANEL);
        bottom.setBorder(new EmptyBorder(4,34,22,34));
        bottom.add(connectBtn);
        bottom.add(Box.createRigidArea(new Dimension(0,8)));
        bottom.add(errLbl);
 
        connectBtn.addActionListener(e -> {
            String server   = serverField.getText().trim();
            String portStr  = portField.getText().trim();
            String username = usernameField.getText().trim();
            if (server.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
                errLbl.setText("All fields are required."); return;
            }
            try {
                int port = Integer.parseInt(portStr);
                client = new ChatClient(server, port, this, username);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                errLbl.setText("Port must be a valid number.");
            }
        });
 
        dialog.add(titlePanel, BorderLayout.NORTH);
        dialog.add(form,       BorderLayout.CENTER);
        dialog.add(bottom,     BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
        
    // Actions of the game
    
        private void sendWord() {
        String word = new String(setterWordField.getPassword()).trim();
        if (word.isEmpty() || word.length() < 3 || !word.matches("[a-zA-Z]+")) {
            setterStatusLbl.setText("Please enter a valid word (letters only, min 3 chars).");
            setterStatusLbl.setForeground(CRIMSON_BRIGHT);
            return;
        }
        client.sendMessage("SETWORD " + word);
        setterWordField.setEnabled(false);
        setterSendBtn.setEnabled(false);
        setterStatusLbl.setText("— The word is sealed. Your opponent now faces their fate. —");
        setterStatusLbl.setForeground(IVORY_DIM);
    }

    
    //  displayMessage : called by ChatClient receiver thread
    
    public void displayMessage(String msg) {
        SwingUtilities.invokeLater(() -> handleMessage(msg.trim()));
    }
 
    private void handleMessage(String msg) {
        if (msg.contains("you are the word Setter") || msg.contains("Dear Player")) {
            role = "SETTER"; cardLayout.show(cardPanel, "SETTER"); return;
        }
        if (msg.contains("You are the GUESSER")) {
            role = "GUESSER"; cardLayout.show(cardPanel, "WAITING"); return;
        }
        if (msg.contains("SPECTATOR")) {
            showEndScreen(false, "The game is full. You are a spectator."); return;
        }
        if (msg.startsWith("Game Started")) {
            if (role.equals("GUESSER")) cardLayout.show(cardPanel, "GUESSER");
            flavor(FLAVOR_IDLE[(int)(Math.random()*FLAVOR_IDLE.length)]); return;
        }
        if (msg.startsWith("Word: ")) {
            currentWord = msg.substring(6).trim().replace(" ","");
            wordTilesPanel.setWord(currentWord); return;
        }
        if (msg.startsWith("Attempts left:")) {
            try {
                attemptsLeft = Integer.parseInt(msg.substring(14).trim());
                attemptsLbl.setText("Pages remaining: " + attemptsLeft);
                gallowsPanel.setWrongCount(6 - attemptsLeft);
            } catch (NumberFormatException ignored) {}
            return;
        }
        if (msg.contains("guess is correct")) {
            flavor(FLAVOR_CORRECT[(int)(Math.random()*FLAVOR_CORRECT.length)]);
            markKey(true); 
            SoundEngine.playApplause();
            return;
        }
        if (msg.contains("guess is wrong")) {
            flavor(FLAVOR_WRONG[(int)(Math.random()*FLAVOR_WRONG.length)]);
            markKey(false); addWrongChip(lastGuess); 
            SoundEngine.playWrong();
            return;
        }
        if (msg.contains("already guessed")) {
            flavor(" That letter has already been tried, scholar. "); return;
        }
        if (msg.contains("Guesser Wins")) { showEndScreen(true,  msg); return; }
        if (msg.contains("Setter Wins"))  { showEndScreen(false, msg); return; }
        if (msg.startsWith("You are not") || msg.startsWith("Game") || msg.startsWith("Please"))
            flavor("⚠  " + msg);
    }
    
    // User Interface Helper
    
    private void flavor(String text) { if (flavorLbl != null) flavorLbl.setText(text); }
 
    private void markKey(boolean correct) {
        String target = String.valueOf(Character.toUpperCase(lastGuess));
        for (JButton btn : letterBtns) {
            if (btn != null && btn.getText().equals(target)) {
                if (correct) {
                    btn.putClientProperty("keyBg",     new Color(30,100,55,180));
                    btn.putClientProperty("keyBorder", GREEN_CORRECT);
                    btn.setForeground(GREEN_TEXT);
                } else {
                    btn.putClientProperty("keyBg",     new Color(100,0,0,160));
                    btn.putClientProperty("keyBorder", CRIMSON);
                    btn.setForeground(CRIMSON_BRIGHT);
                }
                btn.repaint(); break;
            }
        }
    }
 
    private void addWrongChip(char letter) {
        if (wrongGridPanel == null) return;
        JLabel chip = new JLabel(String.valueOf(Character.toUpperCase(letter))) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight(), b=6;
                int[] xs={b,w-b,w,w,w-b,b,0,0}, ys={0,0,b,h-b,h,h,h-b,b};
                g2.setColor(new Color(100,0,0,160)); g2.fillPolygon(xs,ys,8);
                g2.setColor(CRIMSON);                g2.drawPolygon(xs,ys,8);
                g2.setColor(CRIMSON_BRIGHT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w-fm.stringWidth(getText()))/2,
                    (h+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        chip.setFont(royalFont(12, Font.PLAIN));
        chip.setPreferredSize(new Dimension(30,30));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        wrongGridPanel.add(chip);
        wrongGridPanel.revalidate();
        wrongGridPanel.repaint();
    }
 
    private void showEndScreen(boolean win, String msg) {
        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setBackground(BG_DEEP);
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(win ? EMERALD : CRIMSON, 2),
            new EmptyBorder(44,64,44,64)));
        JLabel icon   = lbl(win ? "♛" : "✦", royalFont(40, Font.PLAIN), win ? GOLD : CRIMSON_BRIGHT);
        JLabel head   = lbl(win ? "The Scholar Prevails" : "The Gallows Claims Another",
                            royalFont(22, Font.BOLD), win ? GOLD : CRIMSON_BRIGHT);
        JLabel detail = lbl(msg, georgeFont(12, Font.ITALIC), IVORY_DIM);
        icon.setBorder(new EmptyBorder(0,0,14,0));
        head.setBorder(new EmptyBorder(0,0,12,0));
        icon.setAlignmentX(CENTER_ALIGNMENT);
        head.setAlignmentX(CENTER_ALIGNMENT);
        detail.setAlignmentX(CENTER_ALIGNMENT);
        card.add(icon); card.add(head); card.add(detail);
        overlay.add(card);
        cardPanel.add(overlay, "END");
        cardLayout.show(cardPanel, "END");
    }
    
    // Font and Label Helper
    
    private Font royalFont(int size, int style) {
        Font f = new Font("Palatino Linotype", style, size);
        if (f.getFamily().equals("Dialog")) f = new Font("Georgia", style, size);
        return f;
    }
    private Font georgeFont(int size, int style) { return new Font("Georgia", style, size); }
 
    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font); l.setForeground(color); return l;
    }
    private JLabel dialogLbl(String text) {
        JLabel l = new JLabel(text); l.setFont(royalFont(11, Font.PLAIN)); l.setForeground(GOLD_DIM); return l;
    }
    private JTextField dialogField(String def) {
        JTextField tf = new JTextField(def);
        tf.setFont(royalFont(12, Font.PLAIN));
        tf.setBackground(BG_DEEP); tf.setForeground(IVORY); tf.setCaretColor(GOLD);
        tf.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,GOLD_DIM), new EmptyBorder(5,8,5,8)));
        return tf;
    }
    private void styleRoyalButton(JButton btn) {
        btn.setFont(royalFont(13, Font.PLAIN));
        btn.setBackground(BG_CARD); btn.setForeground(GOLD);
        btn.setFocusPainted(false); btn.setContentAreaFilled(true);
        btn.setBorder(new CompoundBorder(new LineBorder(GOLD_DIM,1), new EmptyBorder(11,30,11,30)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.isEnabled()) return;
                btn.setBackground(new Color(45,90,55));
                btn.setBorder(new CompoundBorder(new LineBorder(GOLD,1), new EmptyBorder(11,30,11,30)));
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.isEnabled()) return;
                btn.setBackground(BG_CARD);
                btn.setBorder(new CompoundBorder(new LineBorder(GOLD_DIM,1), new EmptyBorder(11,30,11,30)));
            }
        });
    }
    
    // Inner : ZelligStrip
    
        class ZelligeStrip extends JPanel {
        ZelligeStrip() { setPreferredSize(new Dimension(0,7)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            int n = ZELLIGE_HEX.length;
            int cells = getWidth() / ZELLIGE_CELL + 2;
            for (int i = 0; i < cells; i++) {
                g.setColor(new Color(ZELLIGE_HEX[i % n]));
                g.fillRect(i * ZELLIGE_CELL, 0, ZELLIGE_CELL, getHeight());
            }
        }
    }
        
    // Inner : StarDivider (Moroccan 8-point star)
        
    class StarDivider extends JPanel {
        StarDivider() { setPreferredSize(new Dimension(220,16)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cy = getHeight()/2, cx = getWidth()/2;
            g2.setColor(new Color(27,67,50,180));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(10, cy, cx-10, cy);
            g2.drawLine(cx+10, cy, getWidth()-10, cy);
            // 8-point star
            g2.setColor(new Color(196,118,42,200));
            double r1=6, r2=3; int pts=8;
            int[] sx=new int[pts*2], sy=new int[pts*2];
            for (int i=0; i<pts*2; i++) {
                double angle = Math.PI*i/pts - Math.PI/2;
                double r = (i%2==0) ? r1 : r2;
                sx[i]=(int)(cx+r*Math.cos(angle));
                sy[i]=(int)(cy+r*Math.sin(angle));
            }
            g2.fillPolygon(sx,sy,pts*2);
            g2.dispose();
        }
    }
    
    //  Inner: GallowsPanel
    //  Moorish arch + Tarbouch figure, 6 progressive stages
    
        class GallowsPanel extends JPanel {
        private int wrongCount = 0;
        GallowsPanel() { setPreferredSize(new Dimension(220,220)); setOpaque(false); }
        public void setWrongCount(int n) { wrongCount=n; repaint(); }
 
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
            int w=getWidth(), h=getHeight(), cx=w/2;
            
            // Arch
            
                        // Crimson outer border
            g2.setColor(new Color(139,0,0,130));
            g2.setStroke(new BasicStroke(2f));
            drawArchOutline(g2, 8,8,w-8,h-8);
            // Gold inner ring
            g2.setColor(new Color(255,215,0,55));
            g2.setStroke(new BasicStroke(1f));
            drawArchOutline(g2, 14,14,w-14,h-14);
            // Fill
            fillArchShape(g2, 15,15,w-15,h-15, BG_PANEL);
 
            // Keystone diamond
            g2.setColor(GOLD);
            int[] kx={cx,cx+7,cx,cx-7}, ky={1,8,15,8};
            g2.fillPolygon(kx,ky,4);
            g2.setColor(BG_DEEP);
            int[] kx2={cx,cx+3,cx,cx-3}, ky2={5,9,13,9};
            g2.fillPolygon(kx2,ky2,4);
 
            // Side jewels
            g2.setColor(new Color(220,20,60,130));
            g2.fillOval(8,h/2-4,7,7);
            g2.fillOval(w-15,h/2-4,7,7);
            g2.setColor(new Color(255,215,0,55));
            g2.fillOval(10,h/2+8,4,4);
            g2.fillOval(w-14,h/2+8,4,4);
 
            // Zellige floor tiles
            int[] zc={0x1B4332,0x8B0000,0xC4762A,0xFFD700};
            for (int i=0;i<4;i++) {
                g2.setColor(new Color(zc[i]));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.7f));
                g2.fillRect(15+i*12,h-20,11,5);
                g2.fillRect(w-59+i*12,h-20,11,5);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
            
            // Gallows
            g2.setColor(EMERALD);
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int baseY=h-22, poleX=cx-38;
            g2.drawLine(30,baseY,w-30,baseY);
            g2.drawLine(poleX,baseY,poleX,50);
            g2.drawLine(poleX,50,cx,50);
            g2.setColor(new Color(74,122,90));
            g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(cx,50,cx,72);
 
            Color body = IVORY;
            g2.setStroke(new BasicStroke(2.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            
            // Stage 1 : Moroccan Tarbouch + head
            
                      if (wrongCount >= 1) {
                // Brim
                g2.setColor(new Color(107,15,15));
                g2.fillOval(cx-17,84,34,9);
                g2.setColor(new Color(75,8,8));
                g2.drawOval(cx-17,84,34,9);
                // Body (trapezoid)
                int[] hx2={cx-14,cx+14,cx+10,cx-10};
                int[] hy2={88,88,66,66};
                g2.setColor(TARBOUCH_RED);
                g2.fillPolygon(hx2,hy2,4);
                g2.setColor(new Color(100,10,10));
                g2.drawPolygon(hx2,hy2,4);
                // Top
                g2.setColor(new Color(120,15,15));
                g2.fillOval(cx-10,62,20,7);
                g2.setColor(new Color(90,8,8));
                g2.drawOval(cx-10,62,20,7);
                // Tassel cord
                g2.setColor(new Color(15,15,15));
                g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.draw(new QuadCurve2D.Double(cx+10,65,cx+18,62,cx+22,56));
                // Puff
                g2.setColor(new Color(10,10,10));
                g2.fillOval(cx+19,52,7,7);
                // Threads
                g2.setStroke(new BasicStroke(0.9f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(cx+20,58,cx+17,65);
                g2.drawLine(cx+22,59,cx+22,66);
                g2.drawLine(cx+24,58,cx+27,65);
 
                // Face
                g2.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.setColor(body);
                g2.drawOval(cx-14,88,28,28);
                // Eyes
                if (wrongCount >= 5) {
                    g2.setColor(CRIMSON_BRIGHT);
                    g2.setStroke(new BasicStroke(1.6f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-9,97,cx-5,101); g2.drawLine(cx-5,97,cx-9,101);
                    g2.drawLine(cx+5,97,cx+9,101); g2.drawLine(cx+9,97,cx+5,101);
                } else {
                    g2.setColor(body);
                    g2.fillOval(cx-9,97,4,4);
                    g2.fillOval(cx+5,97,4,4);
                }
                // Mouth
                g2.setStroke(new BasicStroke(1.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                if (wrongCount >= 6) {
                    g2.setColor(CRIMSON_BRIGHT);
                    g2.draw(new Arc2D.Double(cx-7,108,14,8,0,180,Arc2D.OPEN));
                } else {
                    g2.setColor(body);
                    g2.draw(new Arc2D.Double(cx-7,105,14,8,0,-180,Arc2D.OPEN));
                }
            }
 
            g2.setColor(body);
            g2.setStroke(new BasicStroke(2.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            if (wrongCount>=2) g2.drawLine(cx,116,cx,162);
            if (wrongCount>=3) g2.drawLine(cx,128,cx-18,148);
            if (wrongCount>=4) g2.drawLine(cx,128,cx+18,148);
            if (wrongCount>=5) g2.drawLine(cx,162,cx-16,192);
            if (wrongCount>=6) g2.drawLine(cx,162,cx+16,192);
 
            g2.dispose();
        }
 
        private void drawArchOutline(Graphics2D g2, int x1, int y1, int x2, int y2) {
            int w=x2-x1;
            g2.draw(new Arc2D.Double(x1,y1,w,w,90,90,Arc2D.OPEN));
            g2.draw(new Arc2D.Double(x2-w,y1,w,w,0,90,Arc2D.OPEN));
            g2.drawLine(x1,y1+w/2,x1,y2);
            g2.drawLine(x2,y1+w/2,x2,y2);
        }
 
        private void fillArchShape(Graphics2D g2, int x1, int y1, int x2, int y2, Color c) {
            g2.setColor(c);
            int w=x2-x1;
            g2.fillRect(x1,y1+w/2,w+1,y2-y1-w/2+1);
            g2.fill(new Arc2D.Double(x1,y1,w,w,90,90,Arc2D.PIE));
            g2.fill(new Arc2D.Double(x2-w,y1,w,w,0,90,Arc2D.PIE));
        }
    }
    
    // Inner: WordTilesPanel
        
      class WordTilesPanel extends JPanel {
        private String word = "";
        WordTilesPanel() { setOpaque(false); }
        public void setWord(String w) { word=w; repaint(); }
 
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (word.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
            int tileW=44, tileH=58, gap=12;
            int total = word.length()*tileW + (word.length()-1)*gap;
            int startX = (getWidth()-total)/2;
            int startY = (getHeight()-tileH)/2;
 
            Font f = royalFont(24, Font.BOLD);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
 
            for (int i=0; i<word.length(); i++) {
                int x = startX + i*(tileW+gap);
                char ch = word.charAt(i);
                // Underline
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x+4, startY+tileH, x+tileW-4, startY+tileH);
                // Diamond pip
                g2.setColor(new Color(196,118,42,170));
                int px=x+tileW/2, py=startY+tileH+6;
                int[] dpx={px,px+4,px,px-4}, dpy={py-4,py,py+4,py};
                g2.fillPolygon(dpx,dpy,4);
                // Letter
                if (ch!='_') {
                    g2.setColor(GOLD);
                    String s=String.valueOf(ch).toUpperCase();
                    g2.drawString(s, x+(tileW-fm.stringWidth(s))/2, startY+tileH-10);
                }
            }
            g2.dispose();
        }
    }
      
    // Now the main:
    
        public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(HangmanChatGUI::new);
    }
}

     
    
