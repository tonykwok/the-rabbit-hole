package furbelow;

/* Copyright (c) 2006 Timothy Wall, All Rights Reserved
 */
import java.awt.Point;
import java.awt.dnd.DropTargetEvent;

/** Provides a callback for {@link DropHandler} to customize drop target
 * feedback.
 * @author twall
 */
public interface DropTargetPainter {
    void paintDropTarget(DropTargetEvent e, int action, Point location);
}
