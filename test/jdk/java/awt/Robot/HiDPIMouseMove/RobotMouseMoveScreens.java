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

    private static float clickMatchPercentage;

    private static int  dNumScreens;

    private static String typeOfScaling ;

    private static final int tolerance[] = {0,1};
    private static final int offset[] = {53, 75, 100};

    private static final int numOffsets = offset.length;

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

        for (int tol: tolerance) {
            clickMatchPercentage = 0;
            for (int off : offset) {
                System.out.println("Offset: " + off);
                dNumScreens = 0;
                for (GraphicsDevice screen : screens) {
                    Rectangle bounds = screen.getDefaultConfiguration()
                                               .getBounds();
                    System.out.println("Screen Number  : " + dNumScreens++);
                    System.out.println(bounds);
                    moveMouseTo(robot, bounds, off, tol);
                }

                /* Code below will not be part of the final test, it's being used for data collection  */

                clickMatchPercentage += ((mouseClickMatches * 100) / totalMouseClicks);

                mouseClickMatches = 0;
                totalMouseClicks = 0;


            }

            clickMatchPercentage /= numOffsets;
            printStats(tol);
        }
        /* END */
    }

    private static void moveMouseTo(final Robot robot, final Rectangle bounds, int offset, int tolerance)
            throws InterruptedException {

            for (int x = 0; x < (bounds.width - 1); x += offset) {
                for (int y = 0; y < (bounds.height - 1); y += offset) {
                    int ptX = x + bounds.x;
                    int ptY = y + bounds.y;

                    Point p = new Point(ptX, ptY);

                    robot.mouseMove(p.x, p.y);
                    totalMouseClicks++;

                    Thread.sleep(DELAY);

                    final Point mouse = MouseInfo.getPointerInfo().getLocation();

                    int xDiff = Math.abs(p.x - mouse.x);
                    int yDiff = Math.abs(p.y - mouse.y);

                    if ((xDiff<= tolerance) && (yDiff <= tolerance)) {
                        mouseClickMatches++;
                    }

                }
            }
    }

    private static void printStats(int tolerance)
    {
        /* Code below will not be part of the final test, it's being used for data collection  */

        System.out.println("************************************************");
        System.out.println(" Tolerance: " + tolerance);
        System.out.println(" Type of Scaling: " + typeOfScaling);
        System.out.println(" Click Match Percentage: " + clickMatchPercentage);
        System.out.println("************************************************");
        /* END */
    }
}
