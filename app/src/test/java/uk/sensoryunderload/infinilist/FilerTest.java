package uk.sensoryunderload.infinilist;

import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

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
        ListItem i2 = new ListItem("Item 2", "This one only has children");
        i2.add(new ListItem("Multi-line\ntitle", "", new StatusFlag(STATUS.FAIL)));
        i2.add(new ListItem("Item 2.1", "Yada [yada]"));
        i2.add(new ListItem("Item 2.2", ""));
        top.add(i2);
        top.add(new ListItem("Item 3", "", new StatusFlag(STATUS.SUCCESS)));
        ListItem i4 = new ListItem("Item 4", "");
        i4.add(new ListItem("Item 4.0", "", new StatusFlag(STATUS.FLAG)));
        i4.add(new ListItem("Item 4.1", "Yada yada", new StatusFlag(STATUS.QUERY)));
        i4.add(new ListItem("Item 4.2", ""));
        top.add(i4);
        ListItem i5 = new ListItem("Item 2", "This one only\n\nhas children");

        String outputDirName = "TestOutput";
        File directory = new File(outputDirName);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(outputDirName, "WriteTest.todo");

        top.writeToFile (file);
    }

    @Test
    public void ReadTest() {
        String inputDirName = "TestOutput";
        File file = new File(inputDirName, "WriteTest.todo");
        if (file.exists()) {
            ListItem top = new ListItem();
            top.readFromFile(file);

            // Children sizes
            assertTrue(top.getChildren().size() == 4);
            assertTrue((top.getChildren()).get(0).getChildren().size() == 0);
            assertTrue(top.getChildren().get(1).getChildren().size() == 3);
            assertTrue(top.getChildren().get(2).getChildren().size() == 0);
            assertTrue(top.getChildren().get(3).getChildren().size() == 3);

            // Status flags
            assertTrue(top.getStatus().isEqual (STATUS.NONE));
            assertTrue(top.getChildren().get(1).getChildren().get(0).getStatus().isEqual (STATUS.FAIL));
            assertTrue(top.getChildren().get(2).getStatus().isEqual (STATUS.SUCCESS));
            assertTrue(top.getChildren().get(3).getChildren().get(0).getStatus().isEqual (STATUS.FLAG));
            assertTrue(top.getChildren().get(3).getChildren().get(1).getStatus().isEqual (STATUS.QUERY));

            // Title contents
            assertEquals(top.getTitle(),
                         "Main list");
            assertEquals(top.getChildren().get(0).getTitle(),
                         "Item 1 [oh really?]");
            assertEquals(top.getChildren().get(1).getTitle(),
                         "Item 2");
            assertEquals(top.getChildren().get(1).getChildren().get(0).getTitle(),
                         "Multi-line\ntitle");

            // Content... contents
            assertEquals(top.getChildren().get(1).getContent(), "This one only has children");
            assertEquals(top.getChildren().get(1).getChildren().get(0).getContent(), "");
            assertEquals(top.getChildren().get(1).getChildren().get(1).getContent(), "Yada [yada]");
        }
    }
}
