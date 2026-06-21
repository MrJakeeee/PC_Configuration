package kg.kstu.pcconfiguration.config;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.Category;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.Promotion;
import kg.kstu.pcconfiguration.model.ReadyPc;
import kg.kstu.pcconfiguration.model.Role;
import kg.kstu.pcconfiguration.repository.AppUserRepository;
import kg.kstu.pcconfiguration.repository.CategoryRepository;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.PromotionRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(AppUserRepository users,
                               CategoryRepository categories,
                               ComponentItemRepository components,
                               ReadyPcRepository readyPcs,
                               PromotionRepository promotions,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            createUser(users, passwordEncoder, "admin", "Администратор", Role.ADMIN);
            createUser(users, passwordEncoder, "deliverer", "Доставщик", Role.DELIVERER);
            createUser(users, passwordEncoder, "user", "Покупатель", Role.USER);

            Map<String, Category> c = new LinkedHashMap<>();
            c.put("case", category(categories, "Корпус", "Корпуса ATX, mATX и Mini-ITX"));
            c.put("motherboard", category(categories, "Материнская плата", "Платы с сокетом, типом памяти и поддержкой накопителей"));
            c.put("cpu", category(categories, "Процессор", "CPU для офисных, игровых и рабочих сборок"));
            c.put("cooling", category(categories, "Система охлаждения", "Кулеры и жидкостные системы охлаждения"));
            c.put("ram", category(categories, "Оперативная память", "DDR4 и DDR5 комплекты"));
            c.put("gpu", category(categories, "Видеокарта", "GPU для игр, монтажа и 3D"));
            c.put("m2", category(categories, "Накопитель SSD (M.2)", "Быстрые NVMe накопители"));
            c.put("sata", category(categories, "Накопитель SSD (2.5\")", "SATA SSD для системы и файлов"));
            c.put("psu", category(categories, "Блок питания", "Блоки питания с запасом мощности"));

            component(components, c.get("case"), "Zalman i3 Neo ATX", "Zalman", null, null, "ATX", null, 0, 5200, "ATX, 4 вентилятора, хорошая продуваемость");
            component(components, c.get("case"), "DeepCool CH370 mATX", "DeepCool", null, null, "mATX", null, 0, 6100, "Компактный корпус для mATX плат");
            component(components, c.get("case"), "Cooler Master NR200P Mini-ITX", "Cooler Master", null, null, "Mini-ITX", null, 0, 12400, "Малый корпус для компактной сборки");
            component(components, c.get("case"), "NZXT H5 Flow ATX", "NZXT", null, null, "ATX", null, 0, 13200, "ATX корпус с хорошим воздушным потоком");
            component(components, c.get("case"), "Fractal Design Pop Mini Air", "Fractal", null, null, "mATX", null, 0, 10900, "mATX корпус с аккуратным дизайном");

            component(components, c.get("motherboard"), "MSI B650M Gaming WiFi", "MSI", "AM5", "DDR5", "mATX", "M.2,SATA", 60, 9800, "AM5, DDR5, M.2, Wi-Fi");
            component(components, c.get("motherboard"), "ASUS PRIME B760-PLUS", "ASUS", "LGA1700", "DDR5", "ATX", "M.2,SATA", 65, 10500, "LGA1700, DDR5, PCIe 4.0");
            component(components, c.get("motherboard"), "Gigabyte B550M DS3H", "Gigabyte", "AM4", "DDR4", "mATX", "M.2,SATA", 50, 6900, "AM4, DDR4, доступная плата для Ryzen");
            component(components, c.get("motherboard"), "ASRock A520M-HDV", "ASRock", "AM4", "DDR4", "mATX", "SATA", 45, 4300, "AM4, DDR4, без M.2");
            component(components, c.get("motherboard"), "ASUS ROG STRIX Z790-I", "ASUS", "LGA1700", "DDR5", "Mini-ITX", "M.2,SATA", 75, 27800, "Компактная Mini-ITX плата");

            component(components, c.get("cpu"), "AMD Ryzen 5 5600", "AMD", "AM4", "DDR4", null, null, 65, 8200, "6 ядер, выгодная игровая база");
            component(components, c.get("cpu"), "AMD Ryzen 5 7600", "AMD", "AM5", "DDR5", null, null, 65, 13500, "6 ядер, современная AM5 платформа");
            component(components, c.get("cpu"), "AMD Ryzen 7 7700X", "AMD", "AM5", "DDR5", null, null, 105, 22800, "8 ядер для игр и работы");
            component(components, c.get("cpu"), "Intel Core i5-14400F", "Intel", "LGA1700", "DDR5", null, null, 148, 12600, "10 ядер, без встроенной графики");
            component(components, c.get("cpu"), "Intel Core i7-14700K", "Intel", "LGA1700", "DDR5", null, null, 253, 33200, "Мощный CPU для работы и игр");

            component(components, c.get("cooling"), "DeepCool AG400", "DeepCool", null, null, null, null, 5, 2500, "Башенный кулер для средних CPU");
            component(components, c.get("cooling"), "ID-Cooling SE-226-XT", "ID-Cooling", null, null, null, null, 5, 3900, "Кулер для горячих процессоров");
            component(components, c.get("cooling"), "DeepCool LS520 240mm", "DeepCool", null, null, null, null, 8, 9200, "Жидкостное охлаждение 240 мм");
            component(components, c.get("cooling"), "be quiet! Pure Rock 2", "be quiet!", null, null, null, null, 4, 5200, "Тихий башенный кулер");
            component(components, c.get("cooling"), "Arctic Liquid Freezer III 280", "Arctic", null, null, null, null, 9, 11800, "СЖО 280 мм для мощных процессоров");

            component(components, c.get("ram"), "Kingston Fury Beast 16GB DDR4", "Kingston", null, "DDR4", null, null, 8, 3600, "2x8 GB, 3200 MHz");
            component(components, c.get("ram"), "Kingston Fury Beast 32GB DDR5", "Kingston", null, "DDR5", null, null, 10, 7200, "2x16 GB, 6000 MHz");
            component(components, c.get("ram"), "G.Skill Trident Z5 64GB DDR5", "G.Skill", null, "DDR5", null, null, 12, 16800, "2x32 GB, 6400 MHz");
            component(components, c.get("ram"), "Corsair Vengeance 32GB DDR4", "Corsair", null, "DDR4", null, null, 9, 6900, "2x16 GB, 3600 MHz");
            component(components, c.get("ram"), "ADATA XPG Lancer 32GB DDR5", "ADATA", null, "DDR5", null, null, 10, 7600, "2x16 GB, 6000 MHz");

            component(components, c.get("gpu"), "NVIDIA GTX 1650 4GB", "NVIDIA", null, null, null, null, 75, 11800, "Бюджетная видеокарта для Full HD");
            component(components, c.get("gpu"), "NVIDIA RTX 4060 8GB", "NVIDIA", null, null, null, null, 115, 28500, "Full HD / 2K игры");
            component(components, c.get("gpu"), "AMD Radeon RX 7700 XT", "AMD", null, null, null, null, 245, 39800, "2K игры и монтаж");
            component(components, c.get("gpu"), "NVIDIA RTX 4070 Super", "NVIDIA", null, null, null, null, 220, 64600, "2K/4K игры, CUDA для работы");
            component(components, c.get("gpu"), "NVIDIA RTX 3050 6GB", "NVIDIA", null, null, null, null, 70, 19500, "Экономичная видеокарта для Full HD");

            component(components, c.get("m2"), "Samsung 980 1TB NVMe", "Samsung", null, null, null, "M.2", 6, 6200, "M.2 NVMe, высокая скорость загрузки");
            component(components, c.get("m2"), "Kingston NV2 500GB", "Kingston", null, null, null, "M.2", 4, 3100, "Бюджетный M.2 SSD");
            component(components, c.get("m2"), "WD Black SN850X 2TB", "WD", null, null, null, "M.2", 8, 15400, "Быстрый SSD для игр и монтажа");
            component(components, c.get("m2"), "Crucial P3 Plus 1TB", "Crucial", null, null, null, "M.2", 5, 5600, "NVMe SSD для системы и игр");
            component(components, c.get("m2"), "ADATA Legend 800 1TB", "ADATA", null, null, null, "M.2", 5, 4900, "M.2 накопитель для бюджетной сборки");
            component(components, c.get("sata"), "Crucial BX500 1TB", "Crucial", null, null, null, "SATA", 4, 4300, "2.5\" SATA SSD");
            component(components, c.get("sata"), "Samsung 870 EVO 2TB", "Samsung", null, null, null, "SATA", 5, 11800, "Надёжный SATA SSD для файлов");
            component(components, c.get("sata"), "Kingston A400 480GB", "Kingston", null, null, null, "SATA", 3, 2600, "Недорогой SATA SSD");
            component(components, c.get("sata"), "WD Blue SA510 1TB", "WD", null, null, null, "SATA", 4, 5200, "SATA SSD для хранения данных");
            component(components, c.get("sata"), "Patriot P220 512GB", "Patriot", null, null, null, "SATA", 3, 2900, "2.5\" SSD для недорогого ПК");

            component(components, c.get("psu"), "DeepCool PK550D 550W", "DeepCool", null, null, null, null, 550, 3900, "80 Plus Bronze для офисных и начальных игровых ПК");
            component(components, c.get("psu"), "DeepCool PK650D 650W", "DeepCool", null, null, null, null, 650, 4500, "80 Plus Bronze, запас для среднего ПК");
            component(components, c.get("psu"), "Chieftec Proton 750W", "Chieftec", null, null, null, null, 750, 7100, "Запас для мощной видеокарты");
            component(components, c.get("psu"), "Corsair RM850e 850W", "Corsair", null, null, null, null, 850, 11200, "Gold, тихий и мощный блок питания");
            component(components, c.get("psu"), "Seasonic Focus GX-1000", "Seasonic", null, null, null, null, 1000, 16800, "Gold, запас для топовой сборки");

            ensureMinimum(components, c.get("case"), "Корпус Extra", "PC Case", null, null, "ATX", null, 0, 7600);
            ensureMinimum(components, c.get("motherboard"), "Материнская плата Extra", "Motherboard", "AM5", "DDR5", "ATX", "M.2,SATA", 60, 11900);
            ensureMinimum(components, c.get("cpu"), "Процессор Extra", "CPU", "AM5", "DDR5", null, null, 90, 15900);
            ensureMinimum(components, c.get("cooling"), "Охлаждение Extra", "Cooling", null, null, null, null, 6, 4300);
            ensureMinimum(components, c.get("ram"), "Оперативная память Extra", "RAM", null, "DDR5", null, null, 10, 8200);
            ensureMinimum(components, c.get("gpu"), "Видеокарта Extra", "GPU", null, null, null, null, 160, 33400);
            ensureMinimum(components, c.get("m2"), "SSD M.2 Extra", "SSD", null, null, null, "M.2", 5, 6100);
            ensureMinimum(components, c.get("sata"), "SSD SATA Extra", "SSD", null, null, null, "SATA", 4, 3900);
            ensureMinimum(components, c.get("psu"), "Блок питания Extra", "PSU", null, null, null, null, 700, 6500);

            readyPc(readyPcs, "Office Start", "Офис", 28500, "Ryzen 5 5600G, 16 GB DDR4, 500 GB SSD. Для учёбы, документов и браузера.");
            readyPc(readyPcs, "Home Plus", "Дом", 42000, "Core i5, 16 GB RAM, 1 TB SSD. Универсальный домашний ПК.");
            readyPc(readyPcs, "Gaming Optimal", "Игры", 74500, "Ryzen 5 7600, RTX 4060, 32 GB DDR5, 1 TB NVMe SSD.");
            readyPc(readyPcs, "Gaming Pro 2K", "Игры 2K", 118000, "Ryzen 7, RX 7700 XT, 32 GB DDR5, быстрый NVMe SSD.");
            readyPc(readyPcs, "Creator Pro", "Монтаж", 156000, "Core i7, RTX 4070 Super, 64 GB DDR5, 2 TB NVMe SSD.");
            readyPc(readyPcs, "Compact Mini", "Компактный", 98000, "Mini-ITX сборка для рабочего стола с малым корпусом.");

            if (promotions.count() == 0) {
                Promotion promotion = new Promotion();
                promotion.setTitle("Скидка на первую сборку");
                promotion.setDiscountPercent(10);
                promotion.setActiveUntil(LocalDate.now().plusMonths(1));
                promotion.setDescription("Для новых пользователей при оформлении конфигурации через сайт.");
                promotions.save(promotion);
            }
        };
    }

    private void createUser(AppUserRepository users, PasswordEncoder encoder, String username, String fullName, Role role) {
        if (users.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(username));
        user.setFullName(fullName);
        user.setEmail(username + "@pc.local");
        user.setPhone("+996 555 000 000");
        user.setAddress("Бишкек");
        user.setRole(role);
        users.save(user);
    }

    private Category category(CategoryRepository categories, String name, String description) {
        return categories.findByName(name).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            return categories.save(category);
        });
    }

    private void component(ComponentItemRepository components, Category category, String name, String brand,
                           String socket, String memory, String formFactor, String storageInterface,
                           Integer wattage, int price, String specs) {
        ComponentItem item = components.findByName(name).orElseGet(ComponentItem::new);
        item.setCategory(category);
        item.setName(name);
        item.setBrand(brand);
        item.setPartType(partTypeForCategory(category.getName()));
        item.setSocketType(socket);
        item.setMemoryType(memory);
        item.setFormFactor(formFactor);
        item.setStorageInterface(storageInterface);
        item.setWattage(wattage);
        item.setPrice(BigDecimal.valueOf(price));
        item.setStock(12);
        item.setSpecs(specs);
        item.setActive(true);
        components.save(item);
    }

    private void ensureMinimum(ComponentItemRepository components, Category category, String prefix, String brand,
                               String socket, String memory, String formFactor, String storageInterface,
                               Integer wattage, int price) {
        int count = components.findByCategoryIdAndActiveTrueOrderByNameAsc(category.getId()).size();
        for (int i = count + 1; i <= 7; i++) {
            component(
                    components,
                    category,
                    prefix + " " + i,
                    brand,
                    socket,
                    memory,
                    formFactor,
                    storageInterface,
                    wattage,
                    price + i * 350,
                    "Дополнительный вариант для выбора в конфигураторе"
            );
        }
    }

    private String partTypeForCategory(String categoryName) {
        String value = categoryName.toLowerCase();
        if (value.contains("корпус")) {
            return "CASE";
        }
        if (value.contains("материн")) {
            return "MOTHERBOARD";
        }
        if (value.contains("процессор")) {
            return "CPU";
        }
        if (value.contains("охлаж")) {
            return "COOLING";
        }
        if (value.contains("оператив")) {
            return "RAM";
        }
        if (value.contains("видеокарт")) {
            return "GPU";
        }
        if (value.contains("m.2")) {
            return "M2";
        }
        if (value.contains("2.5") || value.contains("sata")) {
            return "SATA";
        }
        if (value.contains("питания")) {
            return "PSU";
        }
        return "OTHER";
    }

    private void readyPc(ReadyPcRepository readyPcs, String name, String purpose, int price, String description) {
        ReadyPc pc = readyPcs.findByName(name).orElseGet(ReadyPc::new);
        pc.setName(name);
        pc.setPurpose(purpose);
        pc.setPrice(BigDecimal.valueOf(price));
        pc.setStock(5);
        pc.setDescription(description);
        readyPcs.save(pc);
    }
}
