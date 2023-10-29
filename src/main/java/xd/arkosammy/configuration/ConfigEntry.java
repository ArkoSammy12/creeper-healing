package xd.arkosammy.configuration;

import java.util.List;

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

    public static <S> S getValueForNameFromMemory(String entryName, List<ConfigEntry<S>> entries){
        for(ConfigEntry<S> entry : entries){
            if(entry.getName().equals(entryName)){
                return entry.getValue();
            }
        }
        return null;
    }

}
