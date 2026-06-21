package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.model.ComponentItem;
import java.util.List;

public record ConfiguratorGroup(
        String partType,
        String title,
        boolean required,
        boolean singleChoice,
        List<ComponentItem> items
) {
    public int count() {
        return items.size();
    }
}
