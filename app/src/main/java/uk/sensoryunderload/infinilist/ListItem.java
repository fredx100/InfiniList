package uk.sensoryunderload.infinilist;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Possible status values for a ListItem
enum STATUS {
    NONE(0),
    SUCCESS(1),
    FAIL(2),
    IMPORTANT(3),
    QUERY(4);

    STATUS(int i) { value = i; }
    private int value;
    public void cycle() { value = ((value + 1) % 4); }
}

// StatusFlag
// Records a status from
//     none(0)
//     success(1)
//     fail(2)
//     important(3)
//     query(4)
class StatusFlag {
    private STATUS flag;

    StatusFlag(STATUS s) { flag = s; }
    StatusFlag() { flag = STATUS.NONE; }

    public void set(STATUS s) { flag = s; }
    void cycle() { flag.cycle(); }
    boolean equalTo(STATUS s) { return (flag == s); }
}

class ListItem {
    private String title;
    private String content;
    private StatusFlag status;
    private ListItem parent;
    private List<ListItem> children;

    public ListItem(String _title, String _content, StatusFlag _sFlag) {
        title = _title;
        content = _content;
        status = _sFlag;
        children = new java.util.ArrayList<>();
    }

    public ListItem(String _title, String _content) {
        title = _title;
        content = _content;
        status = new StatusFlag();
        children = new java.util.ArrayList<>();
    }

    public ListItem() {
        title = "";
        content = "";
        status = new StatusFlag();
        children = new java.util.ArrayList<>();
    }

    void writeToFile(File file) throws IOException {
//        File path = context.getFilesDir();
//        File file = new File(path, "infinilist.todo");
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter target = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        writeToWriter(target, "");
        fos.close();
        target.close();
//        finally {
//           target.close();
//        }
    }

    private void writeToWriter(OutputStreamWriter target, String indent) throws IOException {
        String nl = System.getProperty("line.separator");
        target.write(indent);
        String newIndent = indent + "  "; // For inner content and children
        if (status.equalTo (STATUS.SUCCESS)) {
            target.write('v');
        } else if (status.equalTo (STATUS.FAIL)) {
            target.write('x');
        } else if (status.equalTo (STATUS.IMPORTANT)) {
            target.write('!');
        } else if (status.equalTo (STATUS.QUERY)) {
            target.write('?');
        }
        target.write("[ ");
        String tempTitle = title;
        tempTitle = tempTitle.replaceAll("([\\[\\]])","\\\\$1");
        tempTitle = tempTitle.replaceAll("(\\r?\\n)","$1" + newIndent);
        target.write(tempTitle.replaceAll("([\\[\\]])","$1"));

        if (content.isEmpty() && children.isEmpty()) {
            target.write(" ]" + nl);
        } else {
            // Write content
            if (!content.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                Scanner s = new Scanner(content).useDelimiter("nl");
                while (s.hasNext()) {
                    sb.append(newIndent);
                    sb.append(s.next().replaceAll("([\\[\\]])","\\\\$1"));
                    sb.append(nl);
                }

                target.write(nl + nl);
                target.write(sb.toString());
            } else {
                target.write(nl);
            }

            // Recurse through children
            for (int i = 0; i < children.size(); ++i) {
                children.get(i).writeToWriter(target, newIndent);
            }

            target.write(indent);
            target.write("]" + nl);
        }
        target.flush();
    }

    public void readFromFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        readFromBuffer("", br, 0);
        fis.close();
    }

    // The following assumes the first unescaped '[' encountered is for
    // this item.
    private int readFromBuffer(String firstLine, BufferedReader reader, int lineNum) {
        boolean itemStarted = false;
        boolean itemEnded = false;
        boolean titleStarted = false;
        boolean titleEnded = false;
        String line;
        StringBuilder builder = new StringBuilder();
        String nl = System.getProperty("line.separator");

        try {
            for (line = firstLine;
                 !itemEnded && (line != null);
                 line = reader.readLine(), ++lineNum) {
                // Detect Item start
                int openIndex = line.indexOf("[");
                int startIndex = -1;
                while ((openIndex != -1) && (startIndex == -1)) {
                    if (openIndex == 0) {
                        startIndex = 0;
                    } else if (line.codePointBefore(openIndex) != '\\') {
                        startIndex = openIndex;
                    }
                    openIndex = line.indexOf("]", openIndex + 1);
                }

                // Act on item start.
                if (startIndex >= 0) {
                    if (!itemStarted) {
                        itemStarted = true;
                        titleStarted = titleEnded = false;
                        switch (line.codePointBefore(startIndex)) {
                            case 'v' : status.set(STATUS.SUCCESS);
                            case 'x' : status.set(STATUS.FAIL);
                            case '!' : status.set(STATUS.IMPORTANT);
                            case '?' : status.set(STATUS.QUERY);
                        }
                        // Trim openning bracket
                        line.replaceFirst("^[\\t ]*[vx!?]?\\[ *", "");
                        builder = new StringBuilder();
                    } else {
                        // Found child-item.
                        if (titleStarted) {
                            titleEnded = true;
                            title = builder.toString();
                        }
                        ListItem li = new ListItem();
                        children.add(li);
                        li.readFromBuffer(line, reader, lineNum);
                    }
                }

                // Detect Item end
                // This must occur after acting on item start due to
                // possibility of child items on line.
                int closeIndex = line.indexOf("]");
                int endIndex = -1;
                while ((closeIndex != -1) && (endIndex == -1)) {
                    if (closeIndex == 0) {
                        endIndex = 0;
                    } else if (line.codePointBefore(closeIndex) != '\\') {
                        endIndex = closeIndex;
                    }
                    closeIndex = line.indexOf("]", closeIndex + 1);
                }
                if (endIndex >= 0) {
                    // Trim any ending ']', as we don't want to append it to
                    // a title or contents later.
                    line = line.substring(0, endIndex).trim();
                }

                if (titleStarted && !titleEnded && line.matches("^[\\t ]*$")) {
                    // Found blank line following title.
                    titleEnded = true;
                    title = builder.toString();
                    builder = new StringBuilder();
                } else {
                    titleStarted = true;
                    // Standard line. Unescape and append.
                    line.replaceAll("\\\\([\\]\\[])", "\\1");
                    builder.append(line);
                    builder.append(nl);
                }

                if (endIndex >= 0) {
                    // Ended this item.
                    if (titleEnded) {
                        content = builder.toString();
                    } else {
                        title = builder.toString();
                    }
                    itemEnded = true;
                }
            }
        } catch (IOException e) {
            if (!itemStarted) {
            } else if (!titleEnded) {
                title = builder.toString();
            } else {
                content = builder.toString();
            }
        }

        return lineNum;
    }

    public ArrayList<Integer> getAddress() {
        if (parent == null) {
            return new ArrayList<Integer>();
        } else {
            ArrayList<Integer> address = parent.getAddress();
            address.add(parent.children.indexOf(this));
            return address;
        }
    }

    public ListItem goToAddress(ArrayList<Integer> address) {
        if (address.isEmpty()) {
            return this;
        } else {
            ListItem child = children.get(address.get(0));
            address.remove(0);
            return child.goToAddress(address);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public StatusFlag getStatus() {
        return status;
    }

    public List<ListItem> getChildren() { return children; }

    public void setParent(ListItem li) { parent = li; }

    public void add(ListItem li) { li.setParent(this); children.add(li); }

    public void changeStatus() {
        status.cycle();
    }
}
