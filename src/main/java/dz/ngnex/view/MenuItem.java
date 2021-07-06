package dz.ngnex.view;

import dz.ngnex.util.WebKit;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class MenuItem implements Serializable, Comparable<MenuItem> {

    private final String value;
    private final String label;

    public MenuItem(String value, String label) {
        this.value = Objects.requireNonNull(value);
        this.label = WebKit.fill(value, 14) + ' ' + WebKit.trim(label);
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int compareTo(@NotNull MenuItem anotherItem) {
        return value.compareTo(anotherItem.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(value, menuItem.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "value='" + value + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
