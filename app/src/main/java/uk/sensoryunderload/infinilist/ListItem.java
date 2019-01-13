package uk.sensoryunderload.infinilist;

// StatusFlag
// Records a status from
//     none
//     success
//     fail
//     important
class StatusFlag {
    private int flag;

    public void cycle() { flag = (flag + 1) % 4; }
}

class ListItem {
    private String title;
    private String content;
    private StatusFlag status;
    private java.util.List<ListItem> children;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public StatusFlag getStatus() {
        return status;
    }

    public changeStatus() {
        status.cycle();
    }
}
