import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.Scanner;

/**
 * Test for
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8249592">JDK-8249592</a>.
 * It moves the mouse to different points on the screen(s) in a sequential manner and reads mouse pointer position back to confirm that
 * mouse moved to the correct position as expected.
 */
public class RobotMouseMoveScreens {
    private static final long DELAY = 150;

    private static int totalMouseClicks = 0;

    private static int mouseClickMatches = 0;

    private static float clickMatchPercentage = 0;

    private static int  dNumScreens = 0;

    private static String typeOfScaling ;

    public static void main(String[] args) throws AWTException, InterruptedException {

        /* Code below will not be part of the final test, it's being used for data collection  */

        System.out.println("Please enter type of Scaling (Per Monitor or System)");
        Scanner scanner = new Scanner(System.in);
        typeOfScaling = scanner.nextLine();
        /* END */

        final GraphicsDevice[] screens = GraphicsEnvironment
                                                 .getLocalGraphicsEnvironment()
                                                 .getScreenDevices();


        final Robot robot = new Robot();
        for (GraphicsDevice screen : screens) {
            Rectangle bounds = screen.getDefaultConfiguration()
                                       .getBounds();
            System.out.println("Screen Number  : " + dNumScreens++);
            System.out.println(bounds);
            moveMouseTo(robot, bounds);
            
        }

        /* Code below will not be part of the final test, it's being used for data collection  */

        printStats();

        /* END */
    }

    private static void moveMouseTo(final Robot robot, final Rectangle bounds)
            throws InterruptedException {

        for (int x= 0; x < (bounds.width - 1);x+=50)
        {
            for (int y= 0;y < (bounds.height - 1);y+=50)
            {
                int ptX = x + bounds.x;
                int ptY = y + bounds.y;

                Point p = new Point(ptX, ptY);

                robot.mouseMove(p.x, p.y);
                totalMouseClicks++;

                Thread.sleep(DELAY);

                final Point mouse = MouseInfo.getPointerInfo().getLocation();

                if(p.x == mouse.x && p.y == mouse.y)
                {
                    mouseClickMatches++;
                }

            }
        }
    }

    private static void printStats()
    {
        /* Code below will not be part of the final test, it's being used for data collection  */
        clickMatchPercentage = ((mouseClickMatches *100) / totalMouseClicks) ;
        System.out.println("************************************************");
        System.out.println(" Type of Scaling: " + typeOfScaling);
        System.out.println(" Click Match Percentage: " + clickMatchPercentage);
        System.out.println("************************************************");
        /* END */
    }
}
