package idm;

/** Entry
 * 
 * Class object used in screens dynamics
 * 
 * @author Igor.Souza
 */
public class Entry {
	
    private String value;
    private String key;

    /*
	 *
	 * @author Igor.Souza
	 */
    public Entry(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}