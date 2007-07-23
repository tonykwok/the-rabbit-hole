package furbelow;
/* Copyright (c) 2006 Timothy Wall, All Rights Reserved
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import junit.extensions.abbot.ComponentTestFixture;

public class AnimatedIconTest extends ComponentTestFixture {
    
    private class Timer {
        private long start;
        public Timer() {
            reset();
        }
        public void reset() {
            start = System.currentTimeMillis();
        }
        public long elapsed() {
            return System.currentTimeMillis() - start;
        }
    }
    
    AnimatedIcon icon;
    Icon staticIcon;
    volatile int painted;
    static final String ANIMATED_ICON = "anim.gif";

    protected void setUp() {
        //Log.addDebugClass(AnimatedIcon.class);
        URL url = getClass().getResource(ANIMATED_ICON);
        if (url == null)
            throw new Error("Animated icon '" + ANIMATED_ICON + "' is unavailable");
        icon = new AnimatedIcon(new ImageIcon(url)) {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                super.paintIcon(c, g, x, y);
                ++painted;
            }
        };
        staticIcon = UIManager.getIcon("Tree.leafIcon");
    }
    
    public void testJLabel() throws Exception {
        WeakReference ref = new WeakReference(icon);
        final JLabel label = new JLabel(icon);
        showFrame(label);
        painted = 0;
        Timer timer = new Timer();
        while (painted == 0) {
            Thread.sleep(20);
            if (timer.elapsed() > 1000)
                fail("Icon is not animated");
        }
        icon = null;
        invokeAndWait(new Runnable() { public void run() {
            label.setIcon(staticIcon);
            painted = 0;
        }});
        assertEquals("Still repainting after icon removed", 0, painted);
        System.gc();
        assertNull("Icon should be GC'd if no longer painting", ref.get());
    }
    
    public void testJList() throws Exception {
        Object[] DATA = { icon, icon, icon };
        final JList list = new JList(DATA);
        showFrame(list);
        painted = 0;
        Timer timer = new Timer();
        while (painted == 0) {
            Thread.sleep(10);
            if (timer.elapsed() > 1000)
                fail("Icon is not animated");
        }
        timer.reset();
        while (painted < DATA.length) {
            Thread.sleep(10);
            if (timer.elapsed() > 1000)
                fail("Not all icons animated: " + painted);
        }
        assertTrue("Not all cells animated: " + painted, painted >= DATA.length);
    }

    /*
    public void testJTreeTable() throws Exception {
        final JTreeTable table = new JTreeTable();
        table.setTreeCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree t, Object value, 
                                                          boolean sel, boolean exp,
                                                          boolean leaf,
                                                          int row, boolean focus) {
                Component c = super.getTreeCellRendererComponent(t, value, sel, exp, leaf, row, focus);
                setIcon(icon);
                return c;
            }
        });
        showFrame(table);
        painted = 0;
        Timer timer = new Timer();
        while (painted == 0) {
            Thread.sleep(10);
            if (timer.elapsed() > 1000)
                fail("Icon is not animated");
        }
        timer.reset();
        while (painted < table.getRowCount()) {
            Thread.sleep(10);
            if (timer.elapsed() > 1000)
                fail("Not all icons animated: " + painted);
        }
        assertTrue("Not all cells animated: " + painted, painted >= 4);
    }
    */
}
