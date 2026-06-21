package kg.kstu.pcconfiguration.service;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.CustomerOrder;
import kg.kstu.pcconfiguration.model.FavoriteBuild;
import kg.kstu.pcconfiguration.model.PcBuild;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.repository.FavoriteBuildRepository;
import kg.kstu.pcconfiguration.repository.PcBuildRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildService {
    private final ComponentItemRepository components;
    private final PcBuildRepository builds;
    private final FavoriteBuildRepository favorites;
    private final CustomerOrderRepository orders;

    public BuildService(ComponentItemRepository components, PcBuildRepository builds,
                        FavoriteBuildRepository favorites, CustomerOrderRepository orders) {
        this.components = components;
        this.builds = builds;
        this.favorites = favorites;
        this.orders = orders;
    }

    @Transactional
    public PcBuild saveBuild(AppUser user, String name, List<Long> componentIds) {
        List<ComponentItem> selected = findComponents(componentIds);
        BuildValidationResult validation = validate(selected);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.toMessage());
        }

        PcBuild build = new PcBuild();
        build.setUser(user);
        build.setName(name == null || name.isBlank() ? "Мой ПК" : name);
        build.setComponents(selected);
        build.setTotalPrice(selected.stream()
                .map(ComponentItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return builds.save(build);
    }

    @Transactional
    public void addFavorite(AppUser user, Long buildId) {
        PcBuild build = builds.findById(buildId).orElseThrow();
        if (!build.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Можно добавлять только свои сборки");
        }
        if (!favorites.existsByUserAndBuild(user, build)) {
            FavoriteBuild favorite = new FavoriteBuild();
            favorite.setUser(user);
            favorite.setBuild(build);
            favorites.save(favorite);
        }
    }

    @Transactional
    public void deleteBuild(AppUser user, Long buildId) {
        PcBuild build = builds.findById(buildId).orElseThrow();
        if (!build.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("РњРѕР¶РЅРѕ СѓРґР°Р»СЏС‚СЊ С‚РѕР»СЊРєРѕ СЃРІРѕРё СЃР±РѕСЂРєРё");
        }
        favorites.findByBuild(build).forEach(favorites::delete);
        for (CustomerOrder order : orders.findByBuild(build)) {
            order.setBuild(null);
            orders.save(order);
        }
        builds.delete(build);
    }

    public List<ComponentItem> findComponents(List<Long> componentIds) {
        if (componentIds == null) {
            return new ArrayList<>();
        }
        return components.findAllById(componentIds);
    }

    public BuildValidationResult validate(List<ComponentItem> selected) {
        BuildValidationResult result = new BuildValidationResult();
        if (selected == null || selected.isEmpty()) {
            result.addError("Выберите комплектующие для сборки");
            return result;
        }

        Optional<ComponentItem> caseItem = firstByType(selected, "CASE");
        Optional<ComponentItem> motherboard = firstByType(selected, "MOTHERBOARD");
        Optional<ComponentItem> cpu = firstByType(selected, "CPU");
        Optional<ComponentItem> ram = firstByType(selected, "RAM");
        Optional<ComponentItem> psu = firstByType(selected, "PSU");
        List<ComponentItem> storage = selected.stream().filter(item -> isType(item, "M2") || isType(item, "SATA")).toList();
        List<ComponentItem> gpu = selected.stream().filter(item -> isType(item, "GPU")).toList();
        List<ComponentItem> cooling = selected.stream().filter(item -> isType(item, "COOLING")).toList();

        require(result, caseItem, "Выберите корпус");
        require(result, motherboard, "Выберите материнскую плату");
        require(result, cpu, "Выберите процессор");
        require(result, ram, "Выберите оперативную память");
        require(result, psu, "Выберите блок питания");

        checkSingle(result, selected, "CASE", "Корпус");
        checkSingle(result, selected, "MOTHERBOARD", "Материнская плата");
        checkSingle(result, selected, "CPU", "Процессор");
        checkSingle(result, selected, "RAM", "Оперативная память");
        checkSingle(result, selected, "PSU", "Блок питания");

        if (motherboard.isPresent() && cpu.isPresent()
                && bothPresent(motherboard.get().getSocketType(), cpu.get().getSocketType())
                && !motherboard.get().getSocketType().equalsIgnoreCase(cpu.get().getSocketType())) {
            result.addError("Сокет процессора " + cpu.get().getSocketType()
                    + " не подходит к материнской плате " + motherboard.get().getSocketType());
        }

        if (motherboard.isPresent() && ram.isPresent()
                && bothPresent(motherboard.get().getMemoryType(), ram.get().getMemoryType())
                && !motherboard.get().getMemoryType().equalsIgnoreCase(ram.get().getMemoryType())) {
            result.addError("Тип памяти " + ram.get().getMemoryType()
                    + " не подходит к материнской плате " + motherboard.get().getMemoryType());
        }

        if (caseItem.isPresent() && motherboard.isPresent()
                && bothPresent(caseItem.get().getFormFactor(), motherboard.get().getFormFactor())
                && !caseSupports(caseItem.get().getFormFactor(), motherboard.get().getFormFactor())) {
            result.addError("Корпус " + caseItem.get().getFormFactor()
                    + " не поддерживает плату " + motherboard.get().getFormFactor());
        }

        if (motherboard.isPresent()) {
            for (ComponentItem drive : storage) {
                if (bothPresent(drive.getStorageInterface(), motherboard.get().getStorageInterface())
                        && !supportsToken(motherboard.get().getStorageInterface(), drive.getStorageInterface())) {
                    result.addError("Накопитель " + drive.getName() + " с интерфейсом "
                            + drive.getStorageInterface() + " не поддерживается выбранной платой");
                }
            }
        }

        int requiredWattage = selected.stream()
                .filter(item -> !isType(item, "PSU"))
                .map(ComponentItem::getWattage)
                .filter(Objects::nonNull)
                .filter(value -> value > 0)
                .reduce(0, Integer::sum) + 100;
        if (psu.isPresent() && psu.get().getWattage() != null && psu.get().getWattage() < requiredWattage) {
            result.addError("Мощности блока питания недостаточно: нужно минимум "
                    + requiredWattage + "W, выбрано " + psu.get().getWattage() + "W");
        }

        if (cpu.isPresent() && cpu.get().getWattage() != null && cpu.get().getWattage() >= 120 && cooling.isEmpty()) {
            result.addWarning("Для горячего процессора желательно добавить систему охлаждения");
        }
        if (gpu.isEmpty()) {
            result.addWarning("Видеокарта не выбрана. Сборка сохранится, если процессор подходит для ваших задач.");
        }

        return result;
    }

    private void require(BuildValidationResult result, Optional<ComponentItem> item, String message) {
        if (item.isEmpty()) {
            result.addError(message);
        }
    }

    private Optional<ComponentItem> firstByType(List<ComponentItem> selected, String type) {
        return selected.stream().filter(item -> isType(item, type)).findFirst();
    }

    private boolean isType(ComponentItem item, String type) {
        if (item.getPartType() != null && item.getPartType().equalsIgnoreCase(type)) {
            return true;
        }
        String category = item.getCategory() == null || item.getCategory().getName() == null
                ? ""
                : item.getCategory().getName().toLowerCase(Locale.ROOT);
        return switch (type) {
            case "CASE" -> category.contains("корпус");
            case "MOTHERBOARD" -> category.contains("материн");
            case "CPU" -> category.contains("процессор");
            case "COOLING" -> category.contains("охлаж");
            case "RAM" -> category.contains("оператив");
            case "GPU" -> category.contains("видеокарт");
            case "M2" -> category.contains("m.2");
            case "SATA" -> category.contains("2.5") || category.contains("sata");
            case "PSU" -> category.contains("питания");
            default -> false;
        };
    }

    private void checkSingle(BuildValidationResult result, List<ComponentItem> selected, String type, String label) {
        long count = selected.stream().filter(item -> isType(item, type)).count();
        if (count > 1) {
            result.addError(label + ": выберите только один вариант");
        }
    }

    private boolean bothPresent(String first, String second) {
        return first != null && !first.isBlank() && second != null && !second.isBlank();
    }

    private boolean supportsToken(String source, String token) {
        return source.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT));
    }

    private boolean caseSupports(String caseFormFactor, String motherboardFormFactor) {
        String box = caseFormFactor.toUpperCase(Locale.ROOT).replace(" ", "");
        String board = motherboardFormFactor.toUpperCase(Locale.ROOT).replace(" ", "");
        if (box.equals("ATX")) {
            return true;
        }
        if (box.equals("MATX") || box.equals("M-ATX")) {
            return board.equals("MATX") || board.equals("M-ATX") || board.equals("MINI-ITX");
        }
        if (box.equals("MINI-ITX")) {
            return board.equals("MINI-ITX");
        }
        return box.contains(board);
    }
}
