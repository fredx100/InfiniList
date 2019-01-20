package uk.sensoryunderload.infinilist;

// Possible status values for a ListItem
enum Status {
    NONE,
    SUCCESS,
    FAIL,
    IMPORTANT
}

// StatusFlag
// Records a status from
//     none (0)
//     success (1)
//     fail (2)
//     important (3)
class StatusFlag {
    private int flag;

    StatusFlag() {
        flag = 0;
    }
    void cycle() { flag = (flag + 1) % 4; }
}

class ListItem {
    private String title;
    private String content;
    private StatusFlag status;
    private java.util.List<ListItem> children;

    public ListItem (String _title, String _content, StatusFlag _sFlag) {
        title = _title;
        content = _content;
        status = _sFlag;
        children = new java.util.ArrayList<ListItem>();
    }

    public ListItem (String _title, String _content) {
        title = _title;
        content = _content;
        status = new StatusFlag();
        children = new java.util.ArrayList<ListItem>();
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

    public java.util.List<ListItem> getChildren() { return children; }

    public void changeStatus() {
        status.cycle();
    }
}
