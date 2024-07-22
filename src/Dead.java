import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Dead implements MouseListener // Game over screen
{

    // Images to use in the menu
    Image died_icon;
    Image quit_button;
    Image return_button;

    /*
     * Method to render menu components
     */
    public void render(Graphics g)
    {
        Graphics2D g3 = (Graphics2D) g;

        // Draw images to the screen
        ImageIcon died_i = new ImageIcon("images/dead_icon_re.png");
        died_icon = died_i.getImage(); 
        g.drawImage(died_icon, 160, 50, null); 


        ImageIcon quit_b = new ImageIcon("images/quit_icon.png");
        quit_button = quit_b.getImage();
        g.drawImage(quit_button, 160, 250, null);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) // Handlers for clicking on the menu items
    {
        //x and y coordinates of the mouse pointer
        int mx = e.getX();
        int my = e.getY();

        if (mx >= 160 && mx <= 340)
        {
            
            if (my >= 250 && my <= 290)
            {
                System.exit(0); // Exit the program
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
