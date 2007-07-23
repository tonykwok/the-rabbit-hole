package furbelow;
/* Copyright (c) 2006 Timothy Wall, All Rights Reserved
 */

import java.awt.Component;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import junit.extensions.abbot.ComponentTestFixture;
import java.awt.Graphics;

public class SpinningDialTest extends ComponentTestFixture {

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

    volatile int painted;
    SpinningDial icon;
    Set locations = new HashSet();
    protected void setUp() {
        icon = new SpinningDial(16, 16) {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                super.paintIcon(c, g, x, y);
                ++painted;
                Component ancestor = SwingUtilities.getAncestorOfClass(CellRendererPane.class, c);
                Point where = new Point(x, y);
                if (ancestor != null && ancestor.getParent() != null) {
                    ancestor = ancestor.getParent();
                    where = SwingUtilities.convertPoint(c, where, ancestor);
                    c = ancestor;
                }
                locations.add(where);
            }
        };
    }
    
    
    public void testJLabel() throws Exception {
        int interval = icon.getFrameInterval();
        WeakReference ref = new WeakReference(icon);
        final JLabel label = new JLabel(icon);
        showFrame(label);
        painted = 0;
        Timer timer = new Timer();
        while (painted < 5) {
            Thread.sleep(interval);
            if (timer.elapsed() > 1000)
                fail("Icon is not animated");
        }
        icon = null;
        invokeAndWait(new Runnable() { public void run() {
            label.setIcon(null);
        }});
        timer.reset();
        while (painted != 0) {
            painted = 0;
            Thread.sleep(interval);
            if (timer.elapsed() > 1000)
                fail("Timed out waiting for animation to stop");
        }
        System.gc();
        assertNull("Icon should be GC'd if not visible", ref.get());
    }
    
    public void testJList() throws Exception {
        int interval = icon.getFrameInterval();
        Object[] DATA = { icon, icon, icon };
        final JList list = new JList(DATA);
        showFrame(list);
        painted = 0;
        Timer timer = new Timer();
        while (locations.size() <= 1) {
            Thread.sleep(interval);
            if (timer.elapsed() > 1000)
                fail("Timed out waiting for more animated locations");
        }
        assertEquals("Not all cells animated", DATA.length, locations.size());
    }
    
    public void testJTable() throws Exception {
        int interval = icon.getFrameInterval();
        String[] COLS = { "one", "two" };
        Object[][] DATA = { { icon, icon }, { icon, icon } };
        final JTable table = new JTable(DATA, COLS) {
            public Class getColumnClass(int c) {
                return Icon.class;
            }
        };
        showFrame(table);
        painted = 0;
        Timer timer = new Timer();
        while (locations.size() <= 1) {
            Thread.sleep(interval);
            if (timer.elapsed() > 1000)
                fail("Timed out waiting for more animated locations");
        }
        assertTrue("Not all cells animated: " + locations, 
                   locations.size() >= table.getRowCount()*table.getColumnCount());
    }
}
