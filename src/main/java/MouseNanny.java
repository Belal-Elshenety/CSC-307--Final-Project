import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * MouseNanny listens for mouse events.
 * Send information to Officer.getInstance().
 *
 * @author Belal Elshenety, Jackson McLaughlin
 * @version 3.0
 */
public class MouseNanny implements MouseListener {

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        System.out.println(e.getComponent().getClass().getSimpleName());
        if (e.getComponent() instanceof Point) {
            System.out.println("\nEntered\n");
            ((Point) e.getComponent()).displayData();
        }
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}