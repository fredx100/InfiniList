package uk.sensoryunderload.infinilist;

import android.util.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

enum STATUS {
    NONE(0),
    SUCCESS(1),
    FAIL(2),
    FLAG(3),
    QUERY(4);

    STATUS(int i) { val = i; }
    private int val;
    int getValue() { return val; }
}

// StatusFlag
// Records a status from STATUS
class StatusFlag {
    private int val;

    StatusFlag(STATUS s) { set(s); }
    StatusFlag() { val = STATUS.NONE.getValue(); }

    void cycle() { val = ((val + 1) % STATUS.values().length); }
    void set(STATUS s) { val = s.getValue(); }
    void set(StatusFlag s) { val = s.val; }
    boolean isEqual(STATUS s) { return (val == s.getValue()); }
}

class ListItem {
    private String title;
    private String content;
    private StatusFlag status;
    private ListItem parent;
    private ArrayList<ListItem> children;

    ListItem(String _title, String _content, StatusFlag _sFlag) {
        title = _title;
        content = _content;
        status = _sFlag;
        children = new java.util.ArrayList<>();
    }
    ListItem(String _title, String _content) {
        title = _title;
        content = _content;
        status = new StatusFlag();
        children = new java.util.ArrayList<>();
    }
    ListItem() {
        title = "";
        content = "";
        status = new StatusFlag();
        children = new java.util.ArrayList<>();
    }

    void writeToFile(File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            writeToStream (fos);
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error opening FileOutputStream from File.", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e("INFLIST-LOG", "Error writing lists to disk (closing)", e);
            }
        }
    }

    void writeToDescriptor(FileDescriptor fileDesc) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileDesc);
            writeToStream (fos);
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error opening FileOutputStream from FileDescriptor.", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e("INFLIST-LOG", "Error writing lists to disk (closing)", e);
            }
        }
    }

    private void writeToStream (FileOutputStream fos) throws IOException {
        OutputStreamWriter target = null;
        try {
            target = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writeToWriter(target, "");
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error writing lists to disk", e);
        } finally {
            try {
                target.close();
            } catch (IOException e) {
                Log.e("INFLIST-LOG", "Error closing OutputStreamWriter.", e);
            }
        }
    }

    private void writeToWriter(OutputStreamWriter target, String indent) throws IOException {
        String nl = System.getProperty("line.separator");
        target.write(indent);
        String newIndent = indent + "  "; // For inner content and children
        if (status.isEqual(STATUS.SUCCESS)) {
            target.write('v');
        } else if (status.isEqual(STATUS.FAIL)) {
            target.write('x');
        } else if (status.isEqual(STATUS.FLAG)) {
            target.write('*');
        } else if (status.isEqual(STATUS.QUERY)) {
            target.write('?');
        }
        target.write("[ ");
        String tempTitle = title;
        // Escape special characters in title.
        tempTitle = tempTitle.replaceAll("([\\[\\]])","\\\\$1");
        tempTitle = tempTitle.replaceAll("(\\r?\\n)","$1" + newIndent);
        target.write(tempTitle.replaceAll("([\\[\\]])","$1"));

        if (content.isEmpty() && children.isEmpty()) {
            target.write(" ]" + nl);
        } else {
            // Write content
            if (!content.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                Scanner s = new Scanner(content).useDelimiter(nl);
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

    void readFromFile(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            readFromInputStream (fis);
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error reading lists from disk", e);
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                Log.e("INFLIST-LOG", "Error reading lists from disk (closing)", e);
            }
        }
    }

    void readFromDescriptor(FileDescriptor fd) {
        FileInputStream fis = null;

        fis = new FileInputStream(fd);
        if (fis != null)
            readFromInputStream (fis);

        try {
            if (fis != null)
                fis.close();
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error reading lists from disk (closing)", e);
        }
    }

    private void readFromInputStream(FileInputStream fileInputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
        readFromBuffer("", br, 0);

        try {
            br.close();
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error reading lists from disk (closing)", e);
        }
    }

    // The following assumes the first unescaped '[' encountered is for
    // this item.
    private void readFromBuffer(String firstLine, BufferedReader reader, int lineNum) {
        boolean itemStarted = false;
        boolean itemEnded = false;
        boolean titleStarted = false;
        boolean titleEnded = false;
        String line;
        MyStringBuilder builder = new MyStringBuilder();
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
                    openIndex = line.indexOf("[", openIndex + 1);
                }
                // Act on item start.
                if (startIndex >= 0) {
                    if (!itemStarted) {
                        itemStarted = true;
                        titleStarted = titleEnded = false;
                        if (startIndex > 0) {
                            switch (line.codePointBefore(startIndex)) {
                                case 'V' :
                                case 'v' : status.set(STATUS.SUCCESS);
                                           break;
                                case 'X' :
                                case 'x' : status.set(STATUS.FAIL);
                                           break;
                                case '*' : status.set(STATUS.FLAG);
                                           break;
                                case '?' : status.set(STATUS.QUERY);
                            }
                        }
                        // Trim opening bracket
                        line = line.replaceFirst("^[\\t ]*[vx*?]?\\[ *", "");
                        builder.empty();
                    } else {
                        // Found child-item.
                        if (titleStarted && !titleEnded) {
                            titleEnded = true;
                            builder.trimNewlines();
                            title = builder.toString();
                            builder.empty();
                        }
                        ListItem li = new ListItem();
                        add(li);
                        li.readFromBuffer(line, reader, lineNum);
                        continue;
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

                // Act on remaining string
                if (line.matches("^[\\t ]*$")) {
                    if (titleStarted && !titleEnded) {
                        // Found blank line following title.
                        titleEnded = true;
                        builder.trimNewlines();
                        title = builder.toString();
                        builder.empty();
                    } else {
                        // Blank line, just append \n
                        builder.append(nl);
                    }
                } else {
                    // Non-empty normal line
                    if (itemStarted) {
                        titleStarted = true;
                        // Standard line. Unescape and append.
                        line = line.replaceAll("\\\\([\\]\\[])", "$1");
                        builder.append(line.trim());
                        builder.append(nl);
                    }
                }

                if (endIndex >= 0) {
                    // Ended this item.
                    builder.trimNewlines();
                    if (titleEnded) {
                        content = builder.toString();
                    } else {
                        title = builder.toString();
                    }
                    itemEnded = true;
                    break;
                }
            }
        } catch (IOException e) {
            builder.trimNewlines();
            if (!itemStarted) {
            } else if (titleEnded) {
                content = builder.toString();
            } else {
                title = builder.toString();
            }
        }
    }

    ArrayList<Integer> getAddress() {
        if (parent == null) {
            return new ArrayList<Integer>();
        } else {
            ArrayList<Integer> address = parent.getAddress();
            address.add(parent.children.indexOf(this));
            return address;
        }
    }
    String getAddressString() {
        ArrayList<Integer> address = getAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (address.size() - 1); ++i) {
            sb.append(address.get(i).toString());
            sb.append(",");
        }
        if (!address.isEmpty()) {
            sb.append(address.get(address.size() - 1).toString());
        }
        return sb.toString();
    }

    ListItem goToAddress(ArrayList<Integer> address) {
        return goToAddress(address, 0);
    }

    ListItem goToAddress(ArrayList<Integer> address, int index) {
        if (index >= address.size()) {
            return this;
        } else {
            ListItem child = children.get(address.get(index));
            return child.goToAddress(address, index + 1);
        }
    }

    boolean hasParent() {
        return (parent != null);
    }

    ListItem getParent() {
        return parent;
    }

    String getTitle() {
        return title;
    }
    void setTitle(String _title) {
        title = _title;
    }

    String getContent() {
        return content;
    }
    void setContent(String _content) {
        content = _content;
    }

    StatusFlag getStatus() {
        return status;
    }

    String getStatusString() {
        String statusString = " ";

        if (status.isEqual(STATUS.SUCCESS)) {
            statusString = "\u2713";
        } else if (status.isEqual(STATUS.FAIL)) {
            statusString = "x";
        } else if (status.isEqual(STATUS.FLAG)) {
            statusString = "*";
        } else if (status.isEqual(STATUS.QUERY)) {
            statusString = "?";
        }

        return statusString;
    }

    ArrayList<ListItem> getChildren() { return children; }
    ListItem getChild(int i) { return children.get(i); }
    int indexOf(ListItem li) { return children.indexOf(li); }

    int size() { return children.size(); }

    void setParent(ListItem li) { parent = li; }

    void insert(ListItem li) { li.setParent(this); children.add(0, li); }

    void add(ListItem li) { li.setParent(this); children.add(li); }

    void remove(int pos) { if ((pos >= 0) && (pos < children.size())) children.remove(pos); }

    void move(int from, int to) {
        if ((from != to) &&
            (from >= 0) && (from < children.size()) &&
            (to   >= 0) && (to   < children.size())) {
            int removeFrom = from;
            if (to > from)
                to = to + 1;
            else // if (to < from)
                removeFrom = from + 1;
            children.add(to, children.get(from));
            children.remove(removeFrom);
        }
    }

    void changeStatus() {
        status.cycle();
    }
    void setStatus(STATUS s) {
        status.set(s);
    }
    void setStatus(StatusFlag s) {
        status.set(s);
    }
    void uncheckAllChildren() {
        for(int i = 0; i < children.size(); ++i) {
            children.get(i).setStatus(STATUS.NONE);
        }
    }
}
