package furbelow;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import junit.framework.TestCase;
/* -*- java -*-
 * $Id:$
 * Copyright (c) 2006 Oculus Technologies Corporation, All Rights Reserved
 */
public class TreeAnimatorTest extends TestCase {
    TestAnimator animator;
    TreePath fromPath;
    TreePath toPath;
    int toIndex;
    JTree tree;
    protected void setUp() {
        tree = new JTree();
        animator = new TestAnimator(tree);
        expandAll((DefaultMutableTreeNode)tree.getModel().getRoot());
    }
    
    protected void tearDown() {
        try {
            animator.endDrag(new Point(0, 0));
        }
        catch(Exception e) {
        }
    }

    private Point getRow(int row) {
        Rectangle bounds = tree.getRowBounds(row);
        return new Point(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
    }
    
    private void expandAll(DefaultMutableTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path);
        for (int i=0;i < node.getChildCount();i++) {
            expandAll((DefaultMutableTreeNode)node.getChildAt(i));
        }
    }

    // X->     X->
    //   _A_      B
    //    B       A
    public void testMoveLeafDownPastSibling() {
        int row = tree.getRowCount()-4;
        TreePath path = tree.getPathForRow(row);
        Rectangle dragged = tree.getRowBounds(row);
        Point where = getRow(row);
        animator.startDrag(where);
        assertEquals("Wrong insertion row", row, animator.getPlaceholderRow());
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 0, animator.getInsertionIndex(where));
        where = getRow(row+1);
        assertEquals("Wrong insertion row", row+1, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 1, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+1, animator.getPlaceholderRow());
        where = getRow(row+2);
        assertEquals("Wrong insertion row", row+2, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 2, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+2, animator.getPlaceholderRow());
        where = getRow(row+3);
        assertEquals("Wrong insertion row", row+3, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 3, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+3, animator.getPlaceholderRow());
        // drag off the end
        where.y += dragged.height;
        assertEquals("Wrong insertion row", row+3, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 3, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+3, animator.getPlaceholderRow());
    }

    // Y->   Y->
    //    A     B
    //   _B_    A
    public void testMoveLeafUpPastSibling() {
        int row = tree.getRowCount()-1;
        TreePath path = tree.getPathForRow(row);
        Rectangle dragged = tree.getRowBounds(row);
        Point where = getRow(row);
        animator.startDrag(where);
        assertEquals("Wrong insertion row", row, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 3, animator.getInsertionIndex(where));
        where.y += dragged.height;
        assertEquals("Wrong insertion row", row, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 3, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row, animator.getPlaceholderRow());
        where = getRow(row-1);
        assertEquals("Wrong insertion row", row-1, animator.getInsertionRow(where));
        assertEquals("Wrong insertion row", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 2, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row-1, animator.getPlaceholderRow());
        where = getRow(row-2);
        assertEquals("Wrong insertion row", row-2, animator.getInsertionRow(where));
        assertEquals("Wrong insertion row", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 1, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row-2, animator.getPlaceholderRow());
        where = getRow(row-3);
        assertEquals("Wrong insertion row", row-3, animator.getInsertionRow(where));
        assertEquals("Wrong insertion path", path.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 0, animator.getInsertionIndex(where));
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row-3, animator.getPlaceholderRow());
    }
    
    // X->     X->
    // Y->        A
    //    A    Y->
    public void testMoveLeafUpOntoParentMovesIntoPreviousSiblingOfParent() {
        int row = tree.getRowCount()-4;
        TreePath draggedPath = tree.getPathForRow(row);
        TreePath targetPath = tree.getPathForRow(6);
        Point where = getRow(row);
        animator.startDrag(where);
        assertEquals("Wrong insertion row", row, animator.getPlaceholderRow());
        assertEquals("Wrong insertion path", draggedPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 0, animator.getInsertionIndex(where));
        where = getRow(row-1);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong insertion row", row-1, animator.getPlaceholderRow());
        assertEquals("Wrong insertion path", targetPath, animator.getInsertionPath(where));
        assertEquals("Wrong insertion index", 4, animator.getInsertionIndex(where));
    }
    
    // ______   ______
    // R->      R->X->   
    //    X->
    public void testProvideNoInsertionOnOrAboveRoot() {
        int row = 1;
        Rectangle dragged = tree.getRowBounds(row);
        Point where = getRow(row);
        animator.startDrag(where);
        where = getRow(row-1);
        assertNull("Should be no target", animator.getDragDestination(where));
        where.y -= dragged.height;
        assertNull("Should be no target", animator.getDragDestination(where));
    }

    // X->     X->     X->
    //   _A_      B       A
    //    B       A       B
    public void testMoveDownThenUpWithoutSideEffects() {
        int row = 2;
        Point where = getRow(row);
        animator.startDrag(where);
        where = getRow(row+1);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row + 1, animator.getPlaceholderRow());
        where = getRow(row);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row, animator.getPlaceholderRow());
    }
    
    // X->     X->     X->
    //    A       B       A
    //   _B_      A       B
    public void testMoveUpThenDownWithoutSideEffects() {
        int row = 3;
        Point where = getRow(row);
        animator.startDrag(where);
        where = getRow(row-1);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row - 1, animator.getPlaceholderRow());
        where = getRow(row);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row, animator.getPlaceholderRow());
    }

    // X->     X->
    //   _A_      B
    //    B    Y->
    // Y->        A
    //    C       C
    //
    // Test with and without B present
    public void testMoveLeafDownOntoParentsExpandedSibling() {
        int row = 5; // Colors->yellow
        TreePath siblingPath = tree.getPathForRow(row+1);
        Point where = getRow(row);
        animator.startDrag(where);
        where = getRow(row+1);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+1, animator.getPlaceholderRow());
        assertEquals("Wrong target path", siblingPath, animator.getInsertionPath(where));
        assertEquals("Wrong target index", 0, animator.getInsertionIndex(where));
        animator.endDrag(getRow(row));

        row = 4; // node previous to yellow
        where = getRow(row);
        animator.startDrag(where);
        where = getRow(row+2);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+2, animator.getPlaceholderRow());
        assertEquals("Wrong target path", siblingPath, animator.getInsertionPath(where));
        assertEquals("Wrong target index", 0, animator.getInsertionIndex(where));
    }
    
    // X->     X->
    //   _A_      B
    //    B    Y
    // Y       A
    public void testMoveLeafDownOntoParentsCollapsedSibling() throws Exception {
        int row = 5;
        final TreePath siblingPath = tree.getPathForRow(row+1);
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            tree.collapsePath(siblingPath);
        }});
        Point where = getRow(row);
        animator.startDrag(where);
        where = getRow(row+1);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+1, animator.getPlaceholderRow());
        assertEquals("Wrong target path", siblingPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index", 2, animator.getInsertionIndex(where));

        row = 4;
        where = getRow(row);
        animator.startDrag(where);
        where = getRow(row+2);
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after setInsertLocation", row+2, animator.getPlaceholderRow());
        assertEquals("Wrong target path", siblingPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index", 2, animator.getInsertionIndex(where));
    }
    
    // X->     X->     X->     X->     X->
    //    A    A       A          A       Z
    // Y       Y       Y       Y       Y
    public void testMoveHorizontaltoChangeParent() throws Exception {
        int row = 5;
        Point where = getRow(row);
        TreePath dragPath = tree.getPathForRow(row);
        animator.startDrag(where);
        
        where.x -= TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after drag left", row, animator.getPlaceholderRow());
        assertEquals("Wrong target path after drag left", dragPath.getParentPath().getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index after drag left", 1, animator.getInsertionIndex(where));
        
        where.x -= TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after drag left (limit)", row, animator.getPlaceholderRow());
        assertEquals("Wrong target path after drag left (limit)", dragPath.getParentPath().getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index after drag left (limit)", 1, animator.getInsertionIndex(where));

        where.x += TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after drag right", row, animator.getPlaceholderRow());
        assertEquals("Wrong target path after drag right", dragPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index after drag right", 3, animator.getInsertionIndex(where));

        where.x += TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Wrong target row after drag right (limit)", row, animator.getPlaceholderRow());
        assertEquals("Wrong target path after drag right (limit)", dragPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Wrong target index after drag right (limit)", 3, animator.getInsertionIndex(where));
    }
    
    // X->    X->
    //    A      A
    //   _B_    _B_
    //    C      C
    public void testNoHorizontalMoveBetweenSiblings() {
        int row = 3;
        Point where = getRow(row);
        TreePath dragPath = tree.getPathForRow(row);
        animator.startDrag(where);
        
        where.x -= TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Expect no row change", row, animator.getPlaceholderRow());
        assertEquals("Expect no path change", dragPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Expect no index change", 1, animator.getInsertionIndex(where));
    }
    
    // X->    X->
    //   _A_     A
    //    B      B
    public void testNoHorizontalMoveWhenFirstChild() {
        int row = 2;
        Point where = getRow(row);
        TreePath dragPath = tree.getPathForRow(row);
        animator.startDrag(where);
        
        where.x -= TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Expect no row change", row, animator.getPlaceholderRow());
        assertEquals("Expect no path change", dragPath.getParentPath(), animator.getInsertionPath(where));
        assertEquals("Expect no index change", 0, animator.getInsertionIndex(where));
    }
    
    // X->     X->
    //    A       A
    // Y->        Y
    public void testMoveIntoPreviousExpandedSibling() {
        int row = 6; // sports: second open folder
        Point where = getRow(row);
        TreePath siblingPath = tree.getPathForRow(1);
        animator.startDrag(where);
        
        where.x += TreeAnimator.HORIZONTAL_THRESHOLD;
        animator.setPlaceholderLocation(where);
        assertEquals("Expect no row change", row, animator.getPlaceholderRow());
        assertEquals("Expect parent path change to previous sibling", siblingPath, animator.getInsertionPath(where));
        assertEquals("Expect index change", 4, animator.getInsertionIndex(where));
    }
    
    public class TestAnimator extends TreeAnimator {
        public TestAnimator(JTree tree) {
            super(tree);
        }
        protected void moveNode(TreePath fromPath, TreePath toPath, int index) {
            TreeAnimatorTest.this.fromPath = fromPath;
            TreeAnimatorTest.this.toPath = toPath;
            TreeAnimatorTest.this.toIndex = index;
        }
        public int getInsertionRow(Point where) {
            return getDragDestination(where).placeholderRow;
        }
        public TreePath getInsertionPath(Point where) {
            return getDragDestination(where).parentPath;
        }
        public int getInsertionIndex(Point where) {
            return getDragDestination(where).index;
        }
        public int getPlaceholderRow() {
            return super.getPlaceholderRow();
        }
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TreeAnimatorTest.class);
    }
}
