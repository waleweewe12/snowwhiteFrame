package snowwhiteframe;

import static com.sun.java.accessibility.util.AWTEventMonitor.addActionListener;
import static com.sun.java.accessibility.util.AWTEventMonitor.addItemListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SnowwhiteFrame extends JFrame 
{    
    // components
    private JPanel             contentpane;
    private JLabel             drawpane;
    private JComboBox          combo;
    private JToggleButton []   tb;
    private JButton            moveButton, stopButton;
    private JTextField         scoreText;
    private JLabel             snowwhiteLabel, basketLabel;
    private ButtonGroup        bgroup;
    private MySoundEffect      hitSound, themeSound;

    // working variables - adjust the values as you want
    private int frameWidth = 1000, frameHeight = 650;
    private int snowwhiteWidth = 180,  snowwhiteHeight = 300; 
    private int snowwhiteCurX  = 700,  snowwhiteCurY   = 250;   
    private int basketWidth    = 100,  basketHeight    = 100;
    private int basketCurX     = 0,    basketCurY      = 0;
    private int snowwhiteSpeed = 1000, basketSpeed     = 1000;
    private boolean snowwhiteLeft = true, snowwhiteMove = true, basketMove = false;
    private int score=0;
    private int i=-100,check=0;

    public static void main(String[] args) 
    {
        new SnowwhiteFrame();
    }    

    //////////////////////////////////////////////////////////////////////////
    public SnowwhiteFrame()
    {   
        setTitle("Snow White");
        setBounds(50, 50, frameWidth, frameHeight);
        setResizable(true);
	setVisible(true);
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

        // (1) Total score when closing frame : add WindowListener (anonymous class) to frame
        close();
        
	contentpane = (JPanel)getContentPane(); // ใส่ content เข้าไปใน JFrame
	contentpane.setLayout( new FlowLayout() );   // GridLayout     
        AddComponents();
	setSnowwhiteThread();
    }

    //////////////////////////////////////////////////////////////////////////
    public void AddComponents()
    {
	MyImageIcon backgroundImg = new MyImageIcon("background.jpg").resize(frameWidth, frameHeight);
	MyImageIcon snowwhiteImg  = new MyImageIcon("snowwhite.png").resize(snowwhiteWidth, snowwhiteHeight);
	MyImageIcon basketImg     = new MyImageIcon("apple.png").resize(basketWidth, basketHeight);

	drawpane = new JLabel();
	drawpane.setIcon(backgroundImg);
        drawpane.setLayout(null);

        snowwhiteLabel = new JLabel(snowwhiteImg);
        snowwhiteLabel.setBounds(snowwhiteCurX, snowwhiteCurY, snowwhiteWidth, snowwhiteHeight);
        drawpane.add(snowwhiteLabel);

        basketLabel = new JLabel(basketImg);
        basketLabel.setBounds(basketCurX, basketCurY, basketWidth, basketHeight);
        drawpane.add(basketLabel);

	hitSound   = new MySoundEffect("blip.wav");
	themeSound = new MySoundEffect("theme.wav"); themeSound.playLoop();
		
	// (2) Snowwhite's speed : add ItemListener (anonymouse class) to combo
        
        
        String[] speed = { "fast", "medium", "slow"};
        combo = new JComboBox(speed);
	combo.setSelectedIndex(1);
        
        
        
	// (3) Snowwhite's direction : add ItemListener (anonymouse class) to tb[i]
        tb = new JToggleButton[2];
        bgroup = new ButtonGroup();      
        tb[0] = new JRadioButton("Left");   tb[0].setName("Left");
        tb[1] = new JRadioButton("Right");  tb[1].setName("Right"); 
	tb[0].setSelected(true);
        for (int i=0; i < 2; i++)
        {
            bgroup.add( tb[i] );
        }
        
        additem(combo,tb);
       // this.add(combo);
       // this.add(tb[0]);
       // this.add(tb[1]);
        // (4) Basket moves : add ActionListener (anonymous class) to moveButton
	moveButton = new JButton("Move");
        // (5) Basket stops : add ActionListener (anonymous class) to stopButton
	stopButton = new JButton("Stop");
        
        addaction(moveButton);
        addaction(stopButton);
        //this.add(moveButton);
       // this.add(stopButton);
        
        String str = "" + score;
        scoreText = new JTextField(str, 10);
        scoreText.setEditable(false);

        JPanel control  = new JPanel();
        control.setBounds(0,0,1000,50);
	control.add(new JLabel("Snow White Control : "));
        control.add(combo);
        control.add(tb[0]);
        control.add(tb[1]);
	control.add(new JLabel("                 "));
	control.add(new JLabel("Basket Control : "));
	control.add(moveButton);
        control.add(stopButton);  
	control.add(new JLabel("                 "));
	control.add(new JLabel("Score : "));
	control.add(scoreText);
        contentpane.add(control, BorderLayout.NORTH);
        contentpane.add(drawpane, BorderLayout.CENTER);      
        validate();  // ให้ JPanel แสดงผล    
    }
    
    //////////////////////////////////////////////////////////////////////////
    public void setSnowwhiteThread()
    {
	Thread snowwhiteThread = new Thread() {
            public void run()
            {
		while (snowwhiteMove)
		{
                    // (6) Update Snowwhite's location
                    snowwhiteCurX+=i;
                    if(snowwhiteCurX==1000 && i>0)
                    {
                        snowwhiteCurX=-100;
                    }
                    if(snowwhiteCurX==-200)
                    {
                        snowwhiteCurX=1000;
                    }
                    snowwhiteLabel.setBounds(snowwhiteCurX, snowwhiteCurY, snowwhiteWidth, snowwhiteHeight);
                    
                    //repaint(); 
                    collision(snowwhiteLabel,basketLabel);      // checked by either Snowwhite or basket

                    try { Thread.sleep(snowwhiteSpeed); } 
                    catch (InterruptedException e) { e.printStackTrace(); }

		} // end while
            } // end run
	}; // end thread creation
	snowwhiteThread.start();
    }

    //////////////////////////////////////////////////////////////////////////
    public void setBasketThread()
    {
	Thread basketThread = new Thread() {
            public void run()
            {
                int x,y;
		while (basketMove)
		{    
                    Random rand = new Random();
                    x = rand.nextInt(900) + 1;
                    y= rand.nextInt(550) + 1;
                    basketCurX     = x;
                    basketCurY      = y;
                    basketLabel.setBounds(basketCurX, basketCurY, basketWidth, basketHeight);
                    repaint(); 

                    try { Thread.sleep(basketSpeed); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
		} // end while
            } // end run
	}; // end thread creation
	basketThread.start();
    }
    
    //////////////////////////////////////////////////////////////////////////
    synchronized public void collision(JLabel label_1,JLabel label_2)
    {
        // (8) Play hit sound & update score 
        if ( label_1.getBounds().intersects(label_2.getBounds()) && basketMove==true)
        {
            score++;
            String str = "" + score;
            scoreText.setText(str);
            hitSound.playOnce();
        }
        else if( label_1.getBounds().intersects(label_2.getBounds()) && basketMove==false)
        {
             if(check==0)
             {
                 check=1;
                 score++;
                 String str = "" + score;
                 scoreText.setText(str);
                 hitSound.playOnce();
             }
        }
        else{
            check=0;
        }
    }
    
    public void close()
    {
        addWindowListener(new WindowAdapter(){
             @Override
             public void windowClosing(WindowEvent e)
             {
                 JOptionPane.showMessageDialog(null,"score = "+score);
             }
        });
    }
    
    public void additem(JComboBox JB,JToggleButton[] JT)
    {
            JB.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
               if(e.getSource()==combo)
               {
                   if(combo.getSelectedItem().equals("fast"))
                   {
                        snowwhiteSpeed=100;
                   }
                   else if(combo.getSelectedItem().equals("medium")){
                        snowwhiteSpeed=1000;
                   }
                   else{
                        snowwhiteSpeed=2000;
                   }
               }
            } 
            });
            
            JT[0].addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                     i=-100;
                }
            });
            JT[1].addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    i=100;
                }
            });
    }
    
    public void addaction(JButton JB)
    {
        JB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource()==moveButton)
                {
                    basketMove=true;
                    setBasketThread();
                }
                else
                {
                    basketMove=false;
                }
            }
        });
    }
}


// Auxiliary class to resize image
class MyImageIcon extends ImageIcon
{
    public MyImageIcon(String fname)  { super(fname); }
    public MyImageIcon(Image image)   { super(image); }

    public MyImageIcon resize(int width, int height)
    {
	Image oldimg = this.getImage();
	Image newimg = oldimg.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
	return new MyImageIcon(newimg);
    }
};


// Auxiliary class to play sound effect (support .wav or .mid file)
class MySoundEffect
{
    private java.applet.AudioClip audio;

    public MySoundEffect(String filename)
    {
	try
	{
            java.io.File file = new java.io.File(filename);
            audio = java.applet.Applet.newAudioClip(file.toURL());
	}
	catch (Exception e) { e.printStackTrace(); }
    }
    public void playOnce()   { audio.play(); }
    public void playLoop()   { audio.loop(); }
    public void stop()       { audio.stop(); }
}
