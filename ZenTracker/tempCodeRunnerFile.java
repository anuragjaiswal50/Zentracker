 UI",Font.PLAIN,13)); tag.setForeground(t2());

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
            