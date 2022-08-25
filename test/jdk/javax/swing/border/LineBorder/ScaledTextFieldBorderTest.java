/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * @test
 * @bug 8279614
 * @summary The left line of the TitledBorder is not painted on 150 scale factor
 * @requires (os.family == "windows")
 * @run main ScaledEtchedBorderTest
 */

public class ScaledTextFieldBorderTest {
    public static final Dimension SIZE = new Dimension(125, 25);

    private static final Color BORDER_COLOR = Color.BLACK;

    private static final double[] scales = {
            1.00, 1.25, 1.50, 1.75,
            2.00, 2.25, 2.50, 2.75,
            3.00
    };

    private static final List<BufferedImage> images =
            new ArrayList<>(scales.length);

    private static final List<Point> panelLocations =
            new ArrayList<>(4);

    public static void main(String[] args) throws Exception {
        Collection<String> params = Arrays.asList(args);
        final boolean showFrame = params.contains("-show");
        final boolean saveImages = params.contains("-save");
        SwingUtilities.invokeAndWait(() -> testScaling(showFrame, saveImages));
    }

    private static void testScaling(boolean showFrame, boolean saveImages) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        createGUI(showFrame, saveImages);

        String errorMessage = null;
        int errorCount = 0;
        for (int i = 0; i < images.size(); i++) {
            BufferedImage img = images.get(i);
            double scaling = scales[i];
            try {
                int thickness = (int) Math.floor(scaling);

                checkVerticalBorders(SIZE.width / 2, thickness, img);

                for (Point p : panelLocations) {
                    int y = (int) (p.y * scaling) + SIZE.height / 2;
                    checkHorizontalBorder(y, thickness, img);
                }
            } catch (Error | Exception e) {
                if (errorMessage == null) {
                    errorMessage = e.getMessage();
                }
                errorCount++;

                System.err.printf("Scaling: %.2f\n", scaling);
                e.printStackTrace();

                // Save the image if it wasn't already saved
                if (!saveImages) {
                    saveImage(img, getImageFileName(scaling));
                }
                break;
            }
        }

        if (errorCount > 0) {
            throw new Error("Test failed: "
                    + errorCount + " error(s) detected - "
                    + errorMessage);
        }
    }

    private static void checkVerticalBorders(final int x,
                                             final int thickness,
                                             final BufferedImage img) {
        checkBorder(x, 0,
                    0, 1,
                    thickness, img);
    }

    private static void checkHorizontalBorder(final int y,
                                              final int thickness,
                                              final BufferedImage img) {
        checkBorder(0, y,
                    1, 0,
                    thickness, img);
    }

    private static void checkBorder(final int xStart, final int yStart,
                                    final int xStep,  final int yStep,
                                    final int thickness,
                                    final BufferedImage img) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        int borderThickness = -1;

        int x = xStart;
        int y = yStart;
        do {
            do {
                System.out.println(x + ", " + y);
                int color = img.getRGB(x, y);
                if (color == BORDER_COLOR.getRGB()) {
                    if (borderThickness < 0) {
                        borderThickness = 1;
                    } else {
                        borderThickness++;
                    }
                } else {
                    if (borderThickness > 0) {
                        if (borderThickness != thickness) {
                            throw new Error(
                                    String.format("Wrong border thickness at %d, %d: %d vs %d",
                                            x, y, borderThickness, thickness));
                        }
                        borderThickness = 0;
                    }
                }
            } while (yStep > 0 && ((y += yStep) < height));
        } while (xStep > 0 && ((x += xStep) < width));

        if (borderThickness < 0) {
            throw new Error(String.format("No border found at %d, %d", x, y));
        }
    }

    private static void createGUI(boolean showFrame, boolean saveImages) {
        // Render content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        Dimension textFieldSize = null;
        for (int i = 0; i < 4; i++) {
            JTextField textField = new JTextField(10);
            Box childPanel = Box.createHorizontalBox();
            childPanel.add(Box.createHorizontalStrut(i));
            childPanel.add(textField);
            childPanel.add(Box.createHorizontalStrut(4));

            contentPanel.add(childPanel);
            if (textFieldSize == null) {
                textFieldSize = textField.getPreferredSize();
            }
            textField.setBounds(i, 0, textFieldSize.width, textFieldSize.height);
            childPanel.setBounds(0, (textFieldSize.height + 4) * i,
                                 textFieldSize.width + i + 4, textFieldSize.height);
        }

        contentPanel.setSize(textFieldSize.width + 4,
                             (textFieldSize.height + 4) * 4);

        for (double scaling : scales) {
            // Create BufferedImage
            BufferedImage image =
                    new BufferedImage((int) Math.ceil(contentPanel.getWidth() * scaling),
                                      (int) Math.ceil(contentPanel.getHeight() * scaling),
                                      BufferedImage.TYPE_INT_ARGB);
            Graphics2D graph = image.createGraphics();
            graph.scale(scaling, scaling);
            // Painting panel onto BufferedImage
            contentPanel.paint(graph);
            graph.dispose();

            if (saveImages) {
                saveImage(image, getImageFileName(scaling));
            }
            images.add(image);
        }

        // Save coordinates of the panels
        for (Component comp : contentPanel.getComponents()) {
            panelLocations.add(comp.getLocation());
        }

        if (showFrame) {
            JFrame frame = new JFrame("Text Field Border Test");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    private static String getImageFileName(final double scaling) {
        return String.format("test%.2f.png", scaling);
    }

    private static void saveImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, "png", new File(filename));
        } catch (IOException e) {
            // Don't propagate the exception
            e.printStackTrace();
        }
    }
}
