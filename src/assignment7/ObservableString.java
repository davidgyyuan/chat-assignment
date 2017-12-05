package assignment7;

import java.util.Observable;
import java.util.Observer;

public class ObservableString extends Observable{
    private String value;

    public ObservableString (Observer o) {
        this.addObserver(o);
    }

    public void setValue(String value) {
        this.value = value;
        setChanged();
        notifyObservers();
    }

    public String getValue() {
        return value;
    }
}
