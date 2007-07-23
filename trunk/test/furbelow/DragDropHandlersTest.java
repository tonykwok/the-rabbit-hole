package furbelow;

/*
 * Copyright (c) 2006 Timothy Wall, All Rights Reserved
 */
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DragDropHandlersTest {
    
    private static final int MOVE = DnDConstants.ACTION_MOVE;
    private static final int COPY = DnDConstants.ACTION_COPY;
    private static final int LINK = DnDConstants.ACTION_LINK;
    private static final int NONE = DnDConstants.ACTION_NONE;

    private JLabel drag;
    private JLabel drop;
    private JLabel noHandler;
    private TestDragHandler dragHandler;
    private TestDropHandler dropHandler;
    
    public DragDropHandlersTest() {
        drag = new JLabel("Drag Me");
        drag.putClientProperty("decorator", new AbstractComponentDecorator(drag, -1) {
            public void paint(Graphics graphics) { 
                Rectangle r = getDecorationBounds();
                Graphics2D g = (Graphics2D)graphics;
                Paint p = new GradientPaint(r.x-r.width, r.y + r.height/2, 
                                            drag.getBackground().darker(),
                                            r.x + r.width, r.y + r.height/2, 
                                            drag.getBackground(), true);
                g.setPaint(p);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        drop = new JLabel("Copy Only");
        noHandler = new JLabel("No Handler");
        Font font = drag.getFont();
        font = font.deriveFont(Font.BOLD, font.getSize()*2);
        drag.setFont(font);
        drag.setBorder(new LineBorder(Color.black));
        drop.setFont(font);
        drop.setBorder(new LineBorder(Color.black));
        noHandler.setFont(font);
    }

    private Frame showFrame(Component c) {
        return showFrame(c, null);
    }
    
    private Frame showFrame(Component c, Dimension d) {
        JFrame f = new JFrame("Drag/Drop Demo");
        JPanel p = (JPanel)f.getContentPane();
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(c);
        f.pack();
        if (d != null) {
            f.setSize(d);
        }
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return f;
    }
    
    private Frame showFrame(int src, int dst) {
        dragHandler = new TestDragHandler(drag, src);
        dropHandler = new TestDropHandler(drop, dst);
        BorderLayout layout = new BorderLayout(8, 8);
        JPanel p = new JPanel(layout);
        p.add(new JLabel("<html>The left label allows any drop, while the right<br>"
                         + "label only allows copies.  Drag over the other<br>"
                         + "frames to see the extended ghost effect.</html>"),
                         BorderLayout.NORTH);
        p.add(drag, BorderLayout.WEST);
        p.add(drop, BorderLayout.EAST);
        p.add(noHandler, BorderLayout.SOUTH);
        noHandler.setHorizontalAlignment(SwingConstants.CENTER);
        return showFrame(p);
    }
    
    public void testReveal() {
        //Log.addDebugClass(DragHandler.class);
        //Log.addDebugClass(DropHandler.class);
        //abbot.Log.addDebugClass(abbot.tester.Robot.class);
        Frame f0 = showFrame(MOVE|COPY, COPY);
        f0.setLocation(100, 100);
        Frame f1 = showFrame(new JLabel("Drag Over Me"), new Dimension(200, 200));
        f1.setLocation(300, 300);
        Frame f2 = showFrame(new JLabel("Me Too"), new Dimension(200, 200));
        f2.setLocation(400, 400);
        new TestDropHandler(drag, MOVE|COPY);
    }
    
    private class TestDragHandler extends DragHandler {
        public volatile boolean dragged;
        public volatile Cursor cursor;
        private Component component;
        public TestDragHandler(Component drag, int actions) {
            super(drag, actions);
            this.component = drag;
        }
        protected Icon getDragIcon(DragGestureEvent e, Point offset) {
            return new Icon() {
                public int getIconWidth() { return component.getWidth(); }
                public int getIconHeight() { return component.getHeight(); }
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    boolean db = component.isDoubleBuffered();
                    JComponent jc = component instanceof JComponent 
                        ? (JComponent)component : null;
                    if (jc != null) {
                        jc.setDoubleBuffered(false);
                    }
                    try {
                        g = g.create(x, y, getIconWidth(), getIconHeight());
                        component.paint(g);
                    }
                    finally {
                        if (jc != null) {
                            jc.setDoubleBuffered(db);
                        }
                    }
                }
            };
        }
        protected Cursor getCursorForAction(int targetAction) {
            return cursor = super.getCursorForAction(targetAction);
        }
        protected void dragStarted(DragGestureEvent e) {
            dragged = true;
        }
        protected Transferable getTransferable(DragGestureEvent e) {
            return new StringSelection("Some Text");
        }
    }
    
    private class TestDropHandler extends DropHandler {
        public volatile boolean dropped;
        public TestDropHandler(Component drop, int actions) {
            super(drop, actions);
        }
        protected boolean canDrop(DropTargetDropEvent e, int action) {
            return true;
        }
        protected void drop(DropTargetDropEvent e, int action) {
            dropped = true;
        }
        protected void paintDropTarget(DropTargetEvent e, int action) {
            Component c = e.getDropTargetContext().getComponent();
            if (action != 0) {
                c.setForeground(Color.red);
            }
            else {
                c.setForeground(Color.black);
            }
            ((JComponent)c).paintImmediately(c.getBounds());
        }
        protected boolean isSupported(List flavors) {
            return true;
        }
    }
    
    public static void main(String[] args) {
        new DragDropHandlersTest().testReveal();
    }
}
