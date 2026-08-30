import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Drives the real Swing app with java.awt.Robot and screenshots what renders.
 * See .claude/skills/gui-smoke-test/SKILL.md for usage and cleanup steps.
 */
public class GuiRobotHarness {
    public final Robot robot;
    public final JFrame frame;
    public final Object app; // the Application instance
    public final Object gridWorld; // the private GridWorld field pulled out by reflection
    public final File outDir;
    private int shot = 0;

    public GuiRobotHarness(File outDir) throws Exception {
        this.outDir = outDir;
        outDir.mkdirs();
        this.robot = new Robot();

        Class<?> appClass = Class.forName("Application");
        Object[] appHolder = new Object[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                appHolder[0] = appClass.getDeclaredConstructor().newInstance();
                appClass.getMethod("execute").invoke(appHolder[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.app = appHolder[0];
        Thread.sleep(500);

        JFrame found = null;
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && "App".equals(f.getTitle())) found = (JFrame) f;
        }
        if (found == null) throw new IllegalStateException("could not find the 'App' JFrame");
        this.frame = found;

        // Doesn't have real OS focus (nothing moved the physical mouse), so bring it
        // to the front or the screenshot captures whatever window actually has focus.
        final JFrame f0 = frame;
        SwingUtilities.invokeAndWait(() -> {
            f0.setAlwaysOnTop(true);
            f0.toFront();
        });
        Thread.sleep(300);

        Field gridWorldField = appClass.getDeclaredField("gridWorld");
        gridWorldField.setAccessible(true);
        this.gridWorld = gridWorldField.get(app);
    }

    // --- model introspection -------------------------------------------------

    /** Prints the current human and food positions, tagged with a label, to stdout. */
    public void logModel(String label) throws Exception {
        Object humans = invokePublic(gridWorld, "getAllHumanPositions");
        Object food = invokePublic(gridWorld, "getAllFoodPositions");
        System.out.println("[MODEL] " + label + " -> humans=" + humans + " food=" + food);
    }

    public int humanCount() throws Exception {
        return ((List<?>) invokePublic(gridWorld, "getAllHumanPositions")).size();
    }

    public int foodCount() throws Exception {
        return ((List<?>) invokePublic(gridWorld, "getAllFoodPositions")).size();
    }

    private static Object invokePublic(Object target, String method) throws Exception {
        return target.getClass().getMethod(method).invoke(target);
    }

    // --- screenshots -----------------------------------------------------------

    /** Captures the app window and saves it as "<counter>-<name>.png" under outDir. */
    public void screenshot(String name) throws Exception {
        Thread.sleep(80);
        Toolkit.getDefaultToolkit().sync();
        Thread.sleep(80);
        Rectangle bounds = frame.getBounds();
        BufferedImage img = robot.createScreenCapture(bounds);
        File out = new File(outDir, String.format("%02d", shot++) + "-" + name + ".png");
        ImageIO.write(img, "png", out);
        System.out.println("[DIAG] screenshot: " + out.getPath());
    }

    // --- driving the UI ---------------------------------------------------------

    private List<JButton> allButtons() {
        List<JButton> buttons = new ArrayList<>();
        collectByType(frame, JButton.class, buttons);
        return buttons;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> void collectByType(Container c, Class<T> type, List<T> out) {
        for (Component comp : c.getComponents()) {
            if (type.isInstance(comp)) out.add((T) comp);
            if (comp instanceof Container) collectByType((Container) comp, type, out);
        }
    }

    public JButton findButtonByText(String text) {
        for (JButton b : allButtons()) if (text.equals(b.getText())) return b;
        return null;
    }

    /** Clicks a button found by its visible text, via the same doClick() path a real click takes. */
    public void clickButtonByText(String text) throws Exception {
        JButton b = findButtonByText(text);
        if (b == null) throw new IllegalStateException("button not found: " + text);
        SwingUtilities.invokeAndWait(b::doClick);
        System.out.println("[DIAG] clicked: " + text);
    }

    /** Clicks the grid cell at model coordinates (x, y), same indexing GridPosition uses. */
    public void clickGridButton(int x, int y) throws Exception {
        Object guiController = getPrivateField(app, "guiController");
        Object gui = getPrivateField(guiController, "gui");
        Object gridPanel = getPrivateField(gui, "gridPanel");
        JButton[][] grid = (JButton[][]) getPrivateField(gridPanel, "grid");
        JButton button = grid[y][x];
        SwingUtilities.invokeAndWait(button::doClick);
        System.out.println("[DIAG] clicked grid button at (" + x + "," + y + ")");
    }

    /** Sets the first JTextArea sharing a parent panel with the named button, e.g. an amount field. */
    public void setTextAreaNear(String buttonText, String value) throws Exception {
        JButton b = findButtonByText(buttonText);
        if (b == null) throw new IllegalStateException("button not found: " + buttonText);
        List<JTextArea> areas = new ArrayList<>();
        collectByType(b.getParent(), JTextArea.class, areas);
        if (areas.isEmpty()) throw new IllegalStateException("no textarea near: " + buttonText);
        SwingUtilities.invokeAndWait(() -> areas.get(0).setText(value));
        System.out.println("[DIAG] set textarea near '" + buttonText + "' to " + value);
    }

    /** Sets the JTextArea paired with a JLabel whose text starts with labelPrefix (the parameter panels). */
    public void setTextAreaByLabel(String labelPrefix, String value) throws Exception {
        List<JLabel> labels = new ArrayList<>();
        collectByType(frame, JLabel.class, labels);
        for (JLabel label : labels) {
            if (label.getText() != null && label.getText().startsWith(labelPrefix)) {
                List<JTextArea> areas = new ArrayList<>();
                collectByType(label.getParent(), JTextArea.class, areas);
                if (!areas.isEmpty()) {
                    SwingUtilities.invokeAndWait(() -> areas.get(0).setText(value));
                    System.out.println("[DIAG] set '" + labelPrefix + "' to " + value);
                    return;
                }
            }
        }
        throw new IllegalStateException("label not found: " + labelPrefix);
    }

    private static Object getPrivateField(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    // --- default smoke scenario -------------------------------------------------

    public static void main(String[] args) throws Exception {
        File outDir = new File(args.length > 0 ? args[0] : "gui-smoke-out");
        GuiRobotHarness h = new GuiRobotHarness(outDir);

        h.screenshot("start");
        h.logModel("after start");

        h.clickButtonByText("Add");
        h.setTextAreaNear("Add Human Random", "5");
        h.clickButtonByText("Add Human Random");
        h.screenshot("after-add-human-random");
        h.logModel("after Add Human Random");

        h.setTextAreaNear("Add Food Random", "5");
        h.clickButtonByText("Add Food Random");
        h.screenshot("after-add-food-random");
        h.logModel("after Add Food Random");

        h.clickButtonByText("Return");
        h.clickButtonByText("Advance");
        for (int i = 1; i <= 3; i++) {
            h.clickButtonByText("Advance Time");
            Thread.sleep(100);
            h.screenshot("advance-" + i);
            h.logModel("after Advance Time #" + i);
        }

        h.clickButtonByText("Automatic");
        for (int i = 1; i <= 3; i++) {
            Thread.sleep(600);
            h.screenshot("automatic-" + i);
            h.logModel("during Automatic, tick " + i);
        }
        h.clickButtonByText("Automatic");

        System.out.println("[RESULT] humans=" + h.humanCount() + " food=" + h.foodCount());
        System.out.println("DONE");
        System.exit(0);
    }
}
