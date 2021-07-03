import java.io.File;

public class ParentFolderModel {
    private File file;
    private String locale;

    public ParentFolderModel(File file, String locale) {
        this.file = file;
        this.locale = locale;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
