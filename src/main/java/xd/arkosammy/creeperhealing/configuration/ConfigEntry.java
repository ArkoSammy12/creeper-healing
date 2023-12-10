package xd.arkosammy.creeperhealing.configuration;

public class ConfigEntry<T> {

    private T value;
    private final T defaultValue;
    private final String name;
    private final String comment;

    public ConfigEntry(String name, T defaultValue){
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.comment = null;
    }

    public ConfigEntry(String name, T defaultValue, String comment) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.comment = comment;
    }

    public void setValue(T value){
        this.value = value;
    }

    public T getValue(){
        return this.value;
    }

    public T getDefaultValue(){
        return this.defaultValue;
    }

    public String getName(){
        return this.name;
    }

    public String getComment(){
        return this.comment;
    }

    public void resetValue(){
        this.value = this.defaultValue;
    }

}
