package uk.sensoryunderload.infinilist;

// This mess is necessitated by StringBuilder being final. Maybe there
// was a good reason...
class MyStringBuilder {
    private StringBuilder sb;

    MyStringBuilder() {
        sb = new StringBuilder();
    }

    void empty() {
        setLength(0);
    }

    // Trim any number of trailing Newlines.
    void trimNewlines() {
        String nl = System.lineSeparator();
        int i = nl.codePointAt(0);
        while ((length() > 0) && (codePointAt(length() - 1) == i)) {
            setLength(length() - 1);
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    void append(String s) {
        sb.append(s);
    }

    private int codePointAt(int i) {
        return sb.codePointAt(i);
    }

    private int length() {
        return sb.length();
    }

    private void setLength(int i) {
        sb.setLength(i);
    }
}
