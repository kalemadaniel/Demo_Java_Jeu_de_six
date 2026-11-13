import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

/** Jeu de Ludo autonome : Java 8+, Swing et Nimbus, sans dependances. */
public class Demo_Java_Jeu_de_six {
    static final Color[] COLORS = {new Color(220, 54, 66), new Color(32, 120, 218),
        new Color(226, 175, 15), new Color(32, 153, 92)};
    static final String[] NAMES = {"Rouge", "Bleu", "Jaune", "Vert"};
    static final int[][] TRACK = {
        {6,1},{6,2},{6,3},{6,4},{6,5},{5,6},{4,6},{3,6},{2,6},{1,6},{0,6},{0,7},{0,8},
        {1,8},{2,8},{3,8},{4,8},{5,8},{6,9},{6,10},{6,11},{6,12},{6,13},{6,14},{7,14},
        {8,14},{8,13},{8,12},{8,11},{8,10},{8,9},{9,8},{10,8},{11,8},{12,8},{13,8},{14,8},
        {14,7},{14,6},{13,6},{12,6},{11,6},{10,6},{9,6},{8,5},{8,4},{8,3},{8,2},{8,1},{8,0},{7,0},{6,0}
    };
    static final int[][] BASES = {{0,0},{0,9},{9,9},{9,0}};
    static final Random RANDOM = new Random();

    static class Player {
        final String name;
        final int color;
        final boolean bot;
        final int[] pieces = {-1,-1,-1,-1};
        Player(String name, int color, boolean bot) { this.name=name; this.color=color; this.bot=bot; }
    }

    static class Game {
        final java.util.List<Player> players;
        int current, die;
        boolean rolled, over;
        Game(java.util.List<Player> players) { this.players=players; }
        Player active() { return players.get(current); }
        static int square(Player p, int progress) { return (p.color*13+progress)%52; }
        static boolean safe(int square) { return square%13==0 || square%13==8; }
        java.util.List<Integer> legal() {
            java.util.List<Integer> result=new ArrayList<Integer>();
            if (!rolled || over) return result;
            for (int i=0;i<4;i++) {
                int n=active().pieces[i];
                if ((n==-1 && die==6) || (n>=0 && n<56 && n+die<=56)) result.add(i);
            }
            return result;
        }
        void roll(int value) {
            if (over || rolled || value<1 || value>6) throw new IllegalStateException("Lancer impossible");
            die=value; rolled=true;
        }
        int move(int piece) {
            if (!legal().contains(piece)) throw new IllegalArgumentException("Pion non jouable");
            Player p=active();
            int destination=p.pieces[piece]<0 ? 0 : p.pieces[piece]+die;
            p.pieces[piece]=destination;
            int captured=0;
            if (destination<=50 && !safe(square(p,destination))) {
                for (Player other:players) if (other!=p) {
                    for (int i=0;i<4;i++) if (other.pieces[i]>=0 && other.pieces[i]<=50
                            && square(other,other.pieces[i])==square(p,destination)) {
                        other.pieces[i]=-1; captured++;
                    }
                }
            }
            over=true;
            for (int n:p.pieces) if (n!=56) over=false;
            return captured;
        }
        void next() {
            if (!rolled || over) return;
            if (die!=6) current=(current+1)%players.size();
            rolled=false;
        }
        int choose(int difficulty) {
            java.util.List<Integer> options=legal();
            if (options.isEmpty()) throw new IllegalStateException("Aucun coup");
            if (difficulty==0) return options.get(RANDOM.nextInt(options.size()));
            Player p=active();
            int best=options.get(0); double scoreBest=-Double.MAX_VALUE;
            for (int i:options) {
                int old=p.pieces[i], dest=old<0?0:old+die;
                double score=dest + (old<0?18:0) + (dest==56?1000:0);
                if (dest<=50) {
                    int cell=square(p,dest);
                    if (!safe(cell)) for (Player o:players) if (o!=p) {
                        for (int n:o.pieces) if (n>=0 && n<=50 && square(o,n)==cell) score+=150;
                    }
                    if (difficulty==2) {
                        if (safe(cell)) score+=35;
                        else for (Player o:players) if (o!=p) {
                            for (int n:o.pieces) if (n>=0 && n<=50) {
                                int distance=(cell-square(o,n)+52)%52;
                                if (distance>=1 && distance<=6 && n+distance<=50) score-=55;
                            }
                        }
                    }
                } else if (difficulty==2) score+=55;
                if (score>scoreBest) { scoreBest=score; best=i; }
            }
            return best;
        }
    }

    final JFrame frame=new JFrame("Demo_Java_Jeu_de_six — Petits chevaux");
    final JPanel root=new JPanel(new BorderLayout(18,18));
    final JLabel turn=new JLabel(), dice=new JLabel("—",SwingConstants.CENTER);
    final JLabel status=new JLabel(" ");
    final JTextArea scores=new JTextArea();
    final JButton roll=new JButton("Lancer le dé");
    final JButton[] moves=new JButton[4];
    final Board board=new Board();
    Game game;
    int difficulty=1;
    javax.swing.Timer pending;

    Demo_Java_Jeu_de_six(boolean preview) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        root.setBorder(new EmptyBorder(20,20,16,20));
        root.setBackground(new Color(243,246,250));
        JLabel title=new JLabel("JEU DE SIX  /  Petits chevaux");
        title.setFont(new Font("SansSerif",Font.BOLD,24));
        root.add(title,BorderLayout.NORTH);
        root.add(board,BorderLayout.CENTER);
        JPanel side=new JPanel(); side.setOpaque(false);
        side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(245,580));
        turn.setFont(new Font("SansSerif",Font.BOLD,19));
        side.add(turn); side.add(Box.createVerticalStrut(12));
        dice.setFont(new Font("SansSerif",Font.BOLD,62));
        dice.setOpaque(true); dice.setBackground(Color.WHITE);
        dice.setMaximumSize(new Dimension(225,100));
        dice.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(dice); side.add(Box.createVerticalStrut(12));
        roll.setMaximumSize(new Dimension(225,42)); roll.addActionListener(e->rollDice());
        side.add(roll); side.add(Box.createVerticalStrut(18));
        side.add(new JLabel("Choisir un pion :")); side.add(Box.createVerticalStrut(7));
        for (int i=0;i<4;i++) {
            final int piece=i;
            moves[i]=new JButton("Pion "+(i+1));
            moves[i].setMaximumSize(new Dimension(225,34));
            moves[i].addActionListener(e->play(piece));
            side.add(moves[i]); side.add(Box.createVerticalStrut(5));
        }
        side.add(Box.createVerticalStrut(16));
        scores.setEditable(false); scores.setOpaque(false);
        scores.setFont(new Font("SansSerif",Font.PLAIN,14));
        scores.setFocusable(false); side.add(scores);
        side.add(Box.createVerticalGlue());
        JButton fresh=new JButton("Nouvelle partie");
        fresh.addActionListener(e->configure()); side.add(fresh);
        side.add(Box.createVerticalStrut(8));
        JButton rules=new JButton("Règles du jeu");
        rules.addActionListener(e->JOptionPane.showMessageDialog(frame,
            "• Sortez un pion de sa base en obtenant un 6.\n"
            +"• Avancez dans le sens horaire du nombre indiqué.\n"
            +"• Un 6 permet de rejouer, même sans coup possible.\n"
            +"• Sur une case ordinaire, renvoyez les adversaires à leur base.\n"
            +"• Les cases marquées d'une étoile protègent tous les pions.\n"
            +"• Plusieurs pions peuvent partager une case, sans la bloquer.\n"
            +"• Il faut un résultat exact pour terminer le couloir d'arrivée.\n"
            +"• Le premier à rentrer ses quatre pions gagne.\n\n"
            +"Solo : un humain contre la machine. À 2–4 : joueurs humains.\n"
            +"Facile : coups aléatoires. Moyen : progression et captures.\n"
            +"Difficile : prend aussi en compte refuges et menaces.\n"
            +"Ces niveaux ne modifient pas le hasard du dé.","Règles — variante de Ludo",JOptionPane.INFORMATION_MESSAGE));
        side.add(rules);
        for(Component child:side.getComponents()) if(child instanceof JComponent)
            ((JComponent)child).setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(side,BorderLayout.EAST);
        status.setBorder(new EmptyBorder(8,0,0,0)); root.add(status,BorderLayout.SOUTH);
        frame.setContentPane(root); frame.setMinimumSize(new Dimension(860,700));
        frame.setSize(1000,800); frame.setLocationRelativeTo(null);
        if(preview) {
            game=new Game(Arrays.asList(new Player("Alice",0,false),new Player("Machine",2,true)));
            game.active().pieces[0]=12;game.active().pieces[1]=4;game.players.get(1).pieces[0]=18;
            game.roll(6);status.setText("Alice obtient 6. Choisissez un pion disponible.");refresh();
            frame.addNotify();frame.validate();
        } else {frame.setVisible(true); configure();}
    }

    void configure() {
        if (pending!=null) pending.stop();
        JPanel form=new JPanel(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(6,7,6,7); c.fill=GridBagConstraints.HORIZONTAL;
        JSpinner count=new JSpinner(new SpinnerNumberModel(1,1,4,1));
        JComboBox<String> level=new JComboBox<String>(new String[]{"Facile","Moyen","Difficile"});
        level.setSelectedIndex(difficulty);
        JTextField[] names=new JTextField[4];
        @SuppressWarnings("unchecked") JComboBox<String>[] colors=new JComboBox[4];
        c.gridx=0;c.gridy=0;form.add(new JLabel("Joueurs humains (1 à 4)"),c);
        c.gridx=1;c.gridwidth=2;form.add(count,c);c.gridwidth=1;
        c.gridx=0;c.gridy=1;form.add(new JLabel("Niveau de la machine"),c);
        c.gridx=1;c.gridwidth=2;form.add(level,c);c.gridwidth=1;
        for(int i=0;i<4;i++) {
            names[i]=new JTextField("Joueur "+(i+1),14);
            colors[i]=new JComboBox<String>(NAMES);colors[i].setSelectedIndex(i);
            c.gridy=i+2;c.gridx=0;form.add(new JLabel("Joueur "+(i+1)),c);
            c.gridx=1;form.add(names[i],c);c.gridx=2;form.add(colors[i],c);
        }
        Runnable enable=()->{int n=(Integer)count.getValue(); level.setEnabled(n==1);
            for(int i=0;i<4;i++){names[i].setEnabled(i<n);colors[i].setEnabled(i<n);}};
        count.addChangeListener(e->enable.run());enable.run();
        c.gridy=6;c.gridx=0;c.gridwidth=3;
        form.add(new JLabel("En solo, un adversaire machine est ajouté automatiquement."),c);
        while(true) {
            int answer=JOptionPane.showConfirmDialog(frame,form,"Configurer la partie",
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
            if(answer!=JOptionPane.OK_OPTION) {
                if(game==null) { frame.dispose(); return; }
                refresh(); scheduleBot(); return;
            }
            try { count.commitEdit(); } catch(java.text.ParseException ex) {
                JOptionPane.showMessageDialog(frame,"Indiquez un nombre de 1 à 4."); continue;
            }
            int n=(Integer)count.getValue();
            if(n<1 || n>4) {JOptionPane.showMessageDialog(frame,"Maximum 4 joueurs.");continue;}
            boolean[] used=new boolean[4]; boolean valid=true;
            java.util.List<Player> players=new ArrayList<Player>();
            for(int i=0;i<n;i++) {
                int color=colors[i].getSelectedIndex();String name=names[i].getText().trim();
                if(used[color] || name.isEmpty() || name.length()>20) valid=false;
                used[color]=true;players.add(new Player(name,color,false));
            }
            if(!valid) {JOptionPane.showMessageDialog(frame,
                "Choisissez des couleurs différentes et des noms de 1 à 20 caractères.");continue;}
            if(n==1) players.add(new Player("Machine",(players.get(0).color+2)%4,true));
            difficulty=level.getSelectedIndex();game=new Game(players);
            status.setText("La partie commence. Obtenez un 6 pour sortir un pion.");
            refresh();return;
        }
    }
    void later(int delay, Runnable action) {
        if(pending!=null)pending.stop();
        pending=new javax.swing.Timer(delay,e->action.run());pending.setRepeats(false);pending.start();
    }
    void scheduleBot() {
        if(game==null || game.over)return;
        if(game.rolled && game.legal().isEmpty()) {later(1100,()->finishTurn());return;}
        if(game.active().bot) later(750,()->{
            if(game.rolled)play(game.choose(difficulty));else rollDice();
        });
    }
    void rollDice() {
        if(game==null || game.rolled || game.over)return;
        game.roll(RANDOM.nextInt(6)+1);
        status.setText(game.active().name+" obtient "+game.die+". "
            +(game.legal().isEmpty()?"Aucun pion ne peut avancer.":"Choisissez un pion disponible."));
        refresh();scheduleBot();
    }
    void play(int piece) {
        if(!game.legal().contains(piece))return;
        String name=game.active().name;int captured=game.move(piece);
        status.setText(name+" déplace le pion "+(piece+1)+". "
            +(captured>0?captured+" pion(s) adverse(s) renvoyé(s) à la base. ":"")
            +(game.die==6?"Un 6 : rejouez !":""));
        if(game.over) {
            status.setText(name+" remporte la partie !");refresh();
            JOptionPane.showMessageDialog(frame,name+" a rentré ses quatre pions !","Victoire",JOptionPane.INFORMATION_MESSAGE);
        } else finishTurn();
    }
    void finishTurn(){game.next();refresh();scheduleBot();}
    void refresh() {
        if(game==null)return;
        Player p=game.active();turn.setText((game.over?"Victoire : ":"Tour : ")+p.name);
        turn.setForeground(COLORS[p.color].darker());dice.setText(game.die==0?"—":String.valueOf(game.die));
        roll.setEnabled(!game.over && !game.rolled && !p.bot);
        java.util.List<Integer> legal=game.legal();
        for(int i=0;i<4;i++) {
            moves[i].setEnabled(!p.bot && legal.contains(i));
            int pos=p.pieces[i];moves[i].setText("Pion "+(i+1)+"  ·  "+(pos<0?"Base":pos==56?"Arrivé":pos>=51?"Arrivée":"En piste"));
        }
        StringBuilder text=new StringBuilder("PIONS ARRIVÉS\n\n");
        for(Player player:game.players){int done=0;for(int v:player.pieces)if(v==56)done++;
            text.append(player.name).append(" · ").append(NAMES[player.color]).append("\n")
                .append(done).append(" / 4\n\n");}
        if(game.players.size()==2 && game.players.get(1).bot)
            text.append("Machine : ").append(new String[]{"Facile","Moyen","Difficile"}[difficulty]);
        scores.setText(text.toString());board.repaint();
    }

    class Board extends JPanel {
        final java.util.List<Hit> hits=new ArrayList<Hit>();
        Board(){setPreferredSize(new Dimension(650,650));setBackground(new Color(243,246,250));
            addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){
                if(game==null || game.active().bot || game.over)return;
                for(int i=hits.size()-1;i>=0;i--){Hit h=hits.get(i);
                    if(h.bounds.contains(e.getPoint()) && h.player==game.active() && game.legal().contains(h.piece)){
                        play(h.piece);return;}}
            }});
        }
        protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics); Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int s=Math.min(getWidth()-16,getHeight()-16)/15;
            if(s<1){g.dispose();return;}
            int x=(getWidth()-15*s)/2,y=(getHeight()-15*s)/2;
            g.setColor(Color.WHITE);g.fillRoundRect(x-5,y-5,15*s+10,15*s+10,16,16);
            for(int k=0;k<4;k++) {
                int bx=x+BASES[k][1]*s,by=y+BASES[k][0]*s;
                g.setColor(COLORS[k]);g.fillRect(bx,by,6*s,6*s);
                g.setColor(new Color(255,255,255,235));g.fillRoundRect(bx+s/2,by+s/2,5*s,5*s,s,s);
                g.setColor(COLORS[k].darker());g.setFont(new Font("SansSerif",Font.BOLD,Math.max(10,s/3)));
                String label=NAMES[k];if(game!=null)for(Player p:game.players)if(p.color==k)label=p.name;
                while(g.getFontMetrics().stringWidth(label)>4*s && label.length()>1)label=label.substring(0,label.length()-1);
                g.drawString(label,bx+s,by+s+3);
            }
            for(int i=0;i<52;i++) {
                int r=TRACK[i][0],c=TRACK[i][1];
                cell(g,x,y,s,r,c,i%13==0?COLORS[i/13]:new Color(248,249,251));
                if(Game.safe(i)){
                    g.setColor(i%13==0?Color.WHITE:new Color(96,111,132));
                    star(g,x+c*s+s/2,y+r*s+s/2,s/4);
                }
            }
            for(int k=0;k<4;k++)for(int i=51;i<=56;i++){
                int[] rc=position(k,i);cell(g,x,y,s,rc[0],rc[1],COLORS[k]);
            }
            int cx=x+7*s+s/2,cy=y+7*s+s/2;
            int[][] corners={{6,6,6,9},{6,9,9,9},{9,9,9,6},{9,6,6,6}};
            // Centre : quatre triangles orientés vers les couloirs d'arrivée.
            int[] order={1,2,3,0};
            for(int i=0;i<4;i++){int[] a=corners[i];g.setColor(COLORS[order[i]]);
                g.fillPolygon(new int[]{cx,x+a[1]*s,x+a[3]*s},new int[]{cy,y+a[0]*s,y+a[2]*s},3);}
            hits.clear();
            if(game!=null){
                Map<String,java.util.List<Hit>> groups=new LinkedHashMap<String,java.util.List<Hit>>();
                for(Player p:game.players)for(int i=0;i<4;i++) {
                    int pos=p.pieces[i];int r,c;
                    if(pos<0){r=BASES[p.color][0]+2+(i/2)*2;c=BASES[p.color][1]+2+(i%2)*2;}
                    else {int[] rc=position(p.color,pos);r=rc[0];c=rc[1];}
                    String key=r+","+c;
                    if(!groups.containsKey(key))groups.put(key,new ArrayList<Hit>());
                    groups.get(key).add(new Hit(p,i,new Rectangle(x+c*s,y+r*s,s,s)));
                }
                for(java.util.List<Hit> group:groups.values())for(int j=0;j<group.size();j++){
                    Hit h=group.get(j);int columns=(int)Math.ceil(Math.sqrt(group.size()));
                    int size=s/columns,px=h.bounds.x+(j%columns)*size,py=h.bounds.y+(j/columns)*size;
                    h.bounds=new Rectangle(px,py,size,size);hits.add(h);
                    boolean selected=h.player==game.active() && game.legal().contains(h.piece);
                    g.setColor(selected?new Color(25,34,52):Color.WHITE);
                    g.fillOval(px+1,py+1,size-2,size-2);
                    int inset=selected?4:3;
                    g.setColor(COLORS[h.player.color]);g.fillOval(px+inset,py+inset,size-2*inset,size-2*inset);
                    g.setColor(h.player.color==2?new Color(45,40,20):Color.WHITE);
                    g.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,size/2)));
                    String number=""+(h.piece+1);FontMetrics fm=g.getFontMetrics();
                    g.drawString(number,px+(size-fm.stringWidth(number))/2,py+(size+fm.getAscent()-fm.getDescent())/2);
                }
            }
            g.dispose();
        }
    }
    static class Hit {
        final Player player;final int piece;Rectangle bounds;
        Hit(Player p,int i,Rectangle r){player=p;piece=i;bounds=r;}
    }
    static void cell(Graphics2D g,int x,int y,int s,int r,int c,Color color){
        g.setColor(color);g.fillRect(x+c*s,y+r*s,s,s);
        g.setColor(new Color(140,151,166));g.drawRect(x+c*s,y+r*s,s,s);
    }
    static void star(Graphics2D g,int x,int y,int radius){
        Polygon p=new Polygon();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;
            double r=i%2==0?radius:radius*0.44;p.addPoint(x+(int)(Math.cos(a)*r),y+(int)(Math.sin(a)*r));}g.fillPolygon(p);
    }
    static int[] position(int color,int progress){
        if(progress<=50)return TRACK[(color*13+progress)%52];
        int n=progress-50;
        if(color==0)return new int[]{7,n};
        if(color==1)return new int[]{n,7};
        if(color==2)return new int[]{7,14-n};
        return new int[]{14-n,7};
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("--test")){Tests.run();return;}
        for(UIManager.LookAndFeelInfo info:UIManager.getInstalledLookAndFeels())
            if("Nimbus".equals(info.getName())){UIManager.setLookAndFeel(info.getClassName());break;}
        if(args.length>1 && args[0].equals("--preview")) {
            SwingUtilities.invokeAndWait(()->{
                Demo_Java_Jeu_de_six app=new Demo_Java_Jeu_de_six(true);
                try {
                    BufferedImage img=new BufferedImage(app.root.getWidth(),app.root.getHeight(),BufferedImage.TYPE_INT_RGB);
                    Graphics2D g=img.createGraphics();app.root.printAll(g);g.dispose();
                    ImageIO.write(img,"png",new File(args[1]));
                } catch(Exception ex){throw new RuntimeException(ex);} finally{app.frame.dispose();}
            });
        } else SwingUtilities.invokeLater(()->new Demo_Java_Jeu_de_six(false));
    }

    static class Tests {
        static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
        static Game create(){return new Game(Arrays.asList(new Player("A",0,false),new Player("B",2,true)));}
        static void run(){
            Game g=create();g.roll(3);check(g.legal().isEmpty(),"Sortie sans six");g.next();check(g.current==1,"Alternance");
            g=create();g.roll(6);check(g.legal().size()==4,"Sortie sur six");g.move(0);g.next();
            check(g.current==0 && g.active().pieces[0]==0 && !g.rolled,"Rejouer sur six");
            g=create();g.active().pieces[0]=4;g.players.get(1).pieces[0]=32;g.roll(2);
            check(g.move(0)==1 && g.players.get(1).pieces[0]==-1,"Capture");
            g=create();g.active().pieces[0]=6;g.players.get(1).pieces[0]=34;g.roll(2);
            check(g.move(0)==0 && g.players.get(1).pieces[0]==34,"Refuge");
            g=create();g.active().pieces[0]=55;g.roll(2);check(!g.legal().contains(0),"Arrivee exacte");
            g=create();Arrays.fill(g.active().pieces,56);g.active().pieces[0]=55;g.roll(1);g.move(0);check(g.over,"Victoire");
            for(int color=0;color<4;color++)for(int n=0;n<=56;n++){
                int[] rc=position(color,n);check(rc[0]>=0 && rc[0]<15 && rc[1]>=0 && rc[1]<15,"Coordonnees");
                if(n<56){int[] next=position(color,n+1);int distance=Math.abs(rc[0]-next[0])+Math.abs(rc[1]-next[1]);
                    check(distance==1 || distance==2,"Continuite du parcours");}
            }
            RANDOM.setSeed(42);
            for(int level=0;level<3;level++)for(int count=2;count<=4;count++){
                java.util.List<Player> ps=new ArrayList<Player>();for(int i=0;i<count;i++)ps.add(new Player("P"+i,i,true));
                g=new Game(ps);int turns=0;
                while(!g.over && turns++<20000){g.roll(RANDOM.nextInt(6)+1);if(!g.legal().isEmpty())g.move(g.choose(level));g.next();
                    for(Player p:ps)for(int n:p.pieces)check(n>=-1 && n<=56,"Position invalide");}
                check(g.over,"Partie terminee : niveau "+level+" joueurs "+count);
            }
            System.out.println("OK : sorties, tours, captures, refuges, arrivee, victoire et 9 parties simulees.");
        }
    }
}
