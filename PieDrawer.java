package final_prj;

import javax.swing.*;
import java.awt.*;



class PieChartPanel extends JPanel {
    Fraction[] slices;                              // list of fractions

    public PieChartPanel(int[] ans) {
        slices = new Fraction[4];
        slices[0] = new Fraction(ans[0], Color.RED);  // Option A
        slices[1] = new Fraction(ans[1], Color.BLUE); // Option B
        slices[2] = new Fraction(ans[2], Color.GREEN); //Option C
        slices[3] = new Fraction(ans[3], Color.YELLOW); //Option D
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPie((Graphics2D) g, getBounds(), slices);
    }

    void drawPie(Graphics2D g, Rectangle canvas, Fraction[] parts) {
        double total = 0;
        for (int i = 0; i < 4; i++) {
            total += parts[i].value;
        }
        double curValue = 0;           // let the start point of the arc be 0 dgr
        int startAngle;
        for (int i = 0; i < 4; i++) {
            startAngle = (int) (curValue * 360 / total);
            int arcAngle = (int) (parts[i].value * 360 / total);       //   end place: # choose this option *360 / # all students
            g.setColor(parts[i].color);         // set color
            g.fillArc(canvas.x, canvas.y, canvas.width-150, canvas.height, startAngle, arcAngle);
            curValue += parts[i].value;     // update the start place
        }
    }

    private class Fraction {
        double value;   // The value/fraction
        Color color;    // Color

        public Fraction(double value, Color color) {
            this.value = value;
            this.color = color;
        }
    }
}
/*
public class PieDrawer {
    public static void main(String[] args) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
        Date date = new Date();
        String dir = System.getProperty("user.dir");  // get the current dir.
        String fileName = formatter.format(date)+"_" + ".txt";
        File file = new File(dir, fileName);
        String path=file.getPath();
        try {
            FileWriter writer = new FileWriter(path);

            writer.write("-------------------------------------------------------------\n");
            writer.write("Option | name");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println("error");
        }
    }
}
*/
