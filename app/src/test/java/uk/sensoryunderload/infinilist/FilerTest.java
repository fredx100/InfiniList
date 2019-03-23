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
        top.add (new ListItem ("Item 1", ""));
        ListItem i2 = new ListItem ("Item 2", "This one has children");
        i2.add (new ListItem ("Item 2.0", ""));
        i2.add (new ListItem ("Item 2.1", "Yada yada"));
        i2.add (new ListItem ("Item 2.2", ""));
        top.add (new ListItem ("Item 3", ""));
        ListItem i4 = new ListItem ("Item 4", "");
        i4.add (new ListItem ("Item 4.0", ""));
        i4.add (new ListItem ("Item 4.1", "Yada yada"));
        i4.add (new ListItem ("Item 4.2", ""));

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
