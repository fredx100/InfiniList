package uk.sensoryunderload.infinilist;

import org.junit.Test;
import java.io.*;

//import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class FilerTest {
    @Test
    public void WriteTest() {
        ListItem top = new ListItem ("Main list", "This is the main list. It may or may not contain others");
        top.add(new ListItem("Item 1 [oh really?]", ""));
        ListItem i2 = new ListItem("Item 2", "This one has children");
        i2.add(new ListItem("Multi-line\ntitle", "", new StatusFlag(STATUS.FAIL)));
        i2.add(new ListItem("Item 2.1", "Yada [yada]"));
        i2.add(new ListItem("Item 2.2", ""));
        top.add(i2);
        top.add(new ListItem("Item 3", "", new StatusFlag(STATUS.SUCCESS)));
        ListItem i4 = new ListItem("Item 4", "");
        i4.add(new ListItem("Item 4.0", "", new StatusFlag(STATUS.IMPORTANT)));
        i4.add(new ListItem("Item 4.1", "Yada yada", new StatusFlag(STATUS.QUERY)));
        i4.add(new ListItem("Item 4.2", ""));
        top.add(i4);

        String outputDirName = "TestOutput";
        File directory = new File(outputDirName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(outputDirName, "WriteTest.todo");

        try {
            top.writeToFile (file);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
