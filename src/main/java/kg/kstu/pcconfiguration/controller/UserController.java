package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.Category;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.PcBuild;
import kg.kstu.pcconfiguration.model.ReadyPc;
import kg.kstu.pcconfiguration.repository.CategoryRepository;
import kg.kstu.pcconfiguration.repository.CartItemRepository;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.repository.FavoriteBuildRepository;
import kg.kstu.pcconfiguration.repository.FavoriteItemRepository;
import kg.kstu.pcconfiguration.repository.PcBuildRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import kg.kstu.pcconfiguration.service.BuildValidationResult;
import kg.kstu.pcconfiguration.service.BuildService;
import kg.kstu.pcconfiguration.service.CheckoutValidationException;
import kg.kstu.pcconfiguration.service.OrderService;
import kg.kstu.pcconfiguration.service.ShopService;
import kg.kstu.pcconfiguration.service.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private final UserService users;
    private final CategoryRepository categories;
    private final ComponentItemRepository components;
    private final PcBuildRepository builds;
    private final FavoriteBuildRepository favorites;
    private final FavoriteItemRepository favoriteItems;
    private final CartItemRepository cartItems;
    private final CustomerOrderRepository orders;
    private final ReadyPcRepository readyPcs;
    private final BuildService buildService;
    private final OrderService orderService;
    private final ShopService shopService;

    public UserController(UserService users, CategoryRepository categories, ComponentItemRepository components,
                          PcBuildRepository builds, FavoriteBuildRepository favorites,
                          FavoriteItemRepository favoriteItems, CartItemRepository cartItems,
                          CustomerOrderRepository orders, ReadyPcRepository readyPcs,
                          BuildService buildService, OrderService orderService, ShopService shopService) {
        this.users = users;
        this.categories = categories;
        this.components = components;
        this.builds = builds;
        this.favorites = favorites;
        this.favoriteItems = favoriteItems;
        this.cartItems = cartItems;
        this.orders = orders;
        this.readyPcs = readyPcs;
        this.buildService = buildService;
        this.orderService = orderService;
        this.shopService = shopService;
    }

    @GetMapping("/configurator")
    public String configurator(Model model) {
        fillConfiguratorModel(model);
        return "user/configurator";
    }

    @PostMapping("/builds")
    public String saveBuild(@RequestParam String name,
                            @RequestParam(required = false) List<Long> componentIds,
                            Principal principal,
                            Model model) {
        try {
            buildService.saveBuild(currentUser(principal), name, componentIds);
            return "redirect:/builds";
        } catch (IllegalArgumentException ex) {
            fillConfiguratorModel(model);
            BuildValidationResult validation = buildService.validate(buildService.findComponents(componentIds));
            model.addAttribute("buildName", name);
            model.addAttribute("selectedIds", componentIds == null ? List.of() : componentIds);
            model.addAttribute("validation", validation);
            model.addAttribute("error", ex.getMessage());
            return "user/configurator";
        }
    }

    @PostMapping("/builds/check")
    public String checkBuild(@RequestParam String name,
                             @RequestParam(required = false) List<Long> componentIds,
                             Model model) {
        fillConfiguratorModel(model);
        BuildValidationResult validation = buildService.validate(buildService.findComponents(componentIds));
        model.addAttribute("buildName", name);
        model.addAttribute("selectedIds", componentIds == null ? List.of() : componentIds);
        model.addAttribute("validation", validation);
        model.addAttribute("checkPerformed", true);
        return "user/configurator";
    }

    @GetMapping("/builds")
    public String builds(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        model.addAttribute("builds", builds.findByUserOrderByIdDesc(user));
        model.addAttribute("favoriteBuildIds", favorites.findByUserOrderByIdDesc(user).stream()
                .map(favorite -> favorite.getBuild().getId())
                .collect(Collectors.toSet()));
        return "user/builds";
    }

    @PostMapping("/favorites")
    public String favorite(@RequestParam Long buildId,
                           Principal principal,
                           @RequestHeader(value = "Referer", required = false) String referer) {
        buildService.addFavorite(currentUser(principal), buildId);
        return redirectBack(referer, "/builds");
    }

    @PostMapping("/builds/delete")
    public String deleteBuild(@RequestParam Long buildId, Principal principal) {
        buildService.deleteBuild(currentUser(principal), buildId);
        return "redirect:/builds";
    }

    @GetMapping("/favorites")
    public String favorites(Model model, Principal principal) {
        List<kg.kstu.pcconfiguration.model.FavoriteItem> items = favoriteItems.findByUserOrderByIdDesc(currentUser(principal));
        model.addAttribute("favorites", items);
        model.addAttribute("total", shopService.totalFavorites(items));
        return "user/favorites";
    }

    @PostMapping("/favorites/component")
    public String favoriteComponent(@RequestParam Long componentId,
                                    Principal principal,
                                    @RequestHeader(value = "Referer", required = false) String referer) {
        shopService.addComponentToFavorites(currentUser(principal), componentId);
        return redirectBack(referer, "/catalog");
    }

    @PostMapping("/favorites/ready-pc")
    public String favoriteReadyPc(@RequestParam Long readyPcId,
                                  Principal principal,
                                  @RequestHeader(value = "Referer", required = false) String referer) {
        shopService.addReadyPcToFavorites(currentUser(principal), readyPcId);
        return redirectBack(referer, "/ready-pcs");
    }

    @PostMapping("/favorites/add-to-cart")
    public String favoritesToCart(@RequestParam(required = false) List<Long> selectedIds, Principal principal) {
        shopService.addFavoritesToCart(currentUser(principal), selectedIds);
        return "redirect:/cart";
    }

    @PostMapping("/favorites/delete")
    public String deleteFavorites(@RequestParam(required = false) List<Long> selectedIds, Principal principal) {
        shopService.deleteFavorites(currentUser(principal), selectedIds);
        return "redirect:/favorites";
    }

    @GetMapping("/cart")
    public String cart(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        List<kg.kstu.pcconfiguration.model.CartItem> items = cartItems.findByUserOrderByIdDesc(user);
        model.addAttribute("items", items);
        model.addAttribute("total", shopService.totalCart(items));
        fillCustomerModel(model, user);
        return "user/cart";
    }

    @PostMapping("/cart/component")
    public String cartComponent(@RequestParam Long componentId,
                                Principal principal,
                                @RequestHeader(value = "Referer", required = false) String referer) {
        shopService.addComponentToCart(currentUser(principal), componentId);
        return redirectBack(referer, "/catalog");
    }

    @PostMapping("/cart/ready-pc")
    public String cartReadyPc(@RequestParam Long readyPcId,
                              Principal principal,
                              @RequestHeader(value = "Referer", required = false) String referer) {
        shopService.addReadyPcToCart(currentUser(principal), readyPcId);
        return redirectBack(referer, "/ready-pcs");
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam(required = false) List<Long> cartItemIds,
                             @RequestParam(required = false) List<Integer> quantities,
                             Principal principal) {
        shopService.updateCart(currentUser(principal), cartItemIds, quantities);
        return "redirect:/cart";
    }

    @PostMapping("/cart/delete")
    public String deleteCart(@RequestParam(required = false) List<Long> selectedIds, Principal principal) {
        shopService.deleteCartItems(currentUser(principal), selectedIds);
        return "redirect:/cart";
    }

    @PostMapping("/orders/build")
    public String orderBuild(@RequestParam Long buildId,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam String cardNumber,
                             @RequestParam String cardExpiry,
                             @RequestParam String cardCvv,
                             @RequestParam(required = false) String comment,
                             Principal principal,
                             Model model) {
        try {
            orderService.createFromBuild(currentUser(principal), buildId, firstName, lastName, phone,
                    address, cardNumber, cardExpiry, cardCvv, comment);
            return "redirect:/orders";
        } catch (CheckoutValidationException ex) {
            return checkoutBuildWithErrors(buildId, principal, model, firstName, lastName, phone, address,
                    cardNumber, cardExpiry, comment, ex.getFieldErrors());
        }
    }

    @GetMapping("/checkout/build/{id}")
    public String checkoutBuild(@PathVariable Long id, Principal principal, Model model) {
        AppUser user = currentUser(principal);
        PcBuild build = builds.findById(id).orElseThrow();
        if (!build.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Можно оформить только свою сборку");
        }
        model.addAttribute("build", build);
        model.addAttribute("amount", build.getTotalPrice());
        fillCustomerModel(model, user);
        return "user/checkout-build";
    }

    @GetMapping("/checkout/ready-pc/{id}")
    public String checkoutReadyPc(@PathVariable Long id, Principal principal, Model model) {
        ReadyPc readyPc = readyPcs.findById(id).orElseThrow();
        model.addAttribute("pc", readyPc);
        model.addAttribute("amount", readyPc.getPrice());
        fillCustomerModel(model, currentUser(principal));
        return "user/checkout-ready-pc";
    }

    @PostMapping("/orders/ready-pc")
    public String orderReadyPc(@RequestParam Long readyPcId,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String phone,
                               @RequestParam String address,
                               @RequestParam String cardNumber,
                               @RequestParam String cardExpiry,
                               @RequestParam String cardCvv,
                               @RequestParam(required = false) String comment,
                               Principal principal,
                               Model model) {
        try {
            orderService.createFromReadyPc(currentUser(principal), readyPcId, firstName, lastName, phone,
                    address, cardNumber, cardExpiry, cardCvv, comment);
            return "redirect:/orders";
        } catch (CheckoutValidationException ex) {
            return checkoutReadyPcWithErrors(readyPcId, principal, model, firstName, lastName, phone, address,
                    cardNumber, cardExpiry, comment, ex.getFieldErrors());
        }
    }

    @PostMapping("/orders/cart")
    public String orderCart(@RequestParam(required = false) List<Long> selectedIds,
                            @RequestParam String firstName,
                            @RequestParam String lastName,
                            @RequestParam String phone,
                            @RequestParam String address,
                            @RequestParam String cardNumber,
                            @RequestParam String cardExpiry,
                            @RequestParam String cardCvv,
                            @RequestParam(required = false) String comment,
                            Principal principal,
                            Model model) {
        try {
            shopService.orderCart(currentUser(principal), selectedIds, firstName, lastName, phone,
                    address, cardNumber, cardExpiry, cardCvv, comment);
            return "redirect:/orders";
        } catch (CheckoutValidationException ex) {
            AppUser user = currentUser(principal);
            List<kg.kstu.pcconfiguration.model.CartItem> items = cartItems.findByUserOrderByIdDesc(user);
            model.addAttribute("items", items);
            model.addAttribute("total", shopService.totalCart(items));
            fillCustomerModel(model, user);
            fillCheckoutFormModel(model, firstName, lastName, phone, address, cardNumber, cardExpiry, comment, ex.getFieldErrors());
            return "user/cart";
        }
    }

    @GetMapping("/orders")
    public String orders(Model model, Principal principal) {
        model.addAttribute("orders", orders.findByUserOrderByCreatedAtDesc(currentUser(principal)));
        return "user/orders";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("buildsCount", builds.findByUserOrderByIdDesc(user).size());
        model.addAttribute("favoritesCount", favoriteItems.findByUserOrderByIdDesc(user).size());
        model.addAttribute("cartCount", cartItems.findByUserOrderByIdDesc(user).size());
        model.addAttribute("ordersCount", orders.findByUserOrderByCreatedAtDesc(user).size());
        return "user/profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String address,
                              Principal principal) {
        AppUser user = currentUser(principal);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        users.save(user);
        return "redirect:/profile?saved";
    }

    private AppUser currentUser(Principal principal) {
        return users.findByUsername(principal.getName());
    }

    private void fillCustomerModel(Model model, AppUser user) {
        String fullName = user.getFullName() == null ? "" : user.getFullName().trim();
        String[] parts = fullName.split("\\s+", 2);
        model.addAttribute("firstName", parts.length > 0 ? parts[0] : "");
        model.addAttribute("lastName", parts.length > 1 ? parts[1] : "");
        model.addAttribute("user", user);
    }

    private String checkoutBuildWithErrors(Long buildId, Principal principal, Model model, String firstName, String lastName,
                                           String phone, String address, String cardNumber, String cardExpiry,
                                           String comment, Map<String, String> fieldErrors) {
        AppUser user = currentUser(principal);
        PcBuild build = builds.findById(buildId).orElseThrow();
        if (!build.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Можно оформить только свою сборку");
        }
        model.addAttribute("build", build);
        model.addAttribute("amount", build.getTotalPrice());
        fillCustomerModel(model, user);
        fillCheckoutFormModel(model, firstName, lastName, phone, address, cardNumber, cardExpiry, comment, fieldErrors);
        return "user/checkout-build";
    }

    private String checkoutReadyPcWithErrors(Long readyPcId, Principal principal, Model model, String firstName, String lastName,
                                             String phone, String address, String cardNumber, String cardExpiry,
                                             String comment, Map<String, String> fieldErrors) {
        ReadyPc readyPc = readyPcs.findById(readyPcId).orElseThrow();
        model.addAttribute("pc", readyPc);
        model.addAttribute("amount", readyPc.getPrice());
        fillCustomerModel(model, currentUser(principal));
        fillCheckoutFormModel(model, firstName, lastName, phone, address, cardNumber, cardExpiry, comment, fieldErrors);
        return "user/checkout-ready-pc";
    }

    private void fillCheckoutFormModel(Model model, String firstName, String lastName, String phone, String address,
                                       String cardNumber, String cardExpiry, String comment, Map<String, String> fieldErrors) {
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("checkoutPhone", phone);
        model.addAttribute("checkoutAddress", address);
        model.addAttribute("cardNumber", cardNumber);
        model.addAttribute("cardExpiry", cardExpiry);
        model.addAttribute("comment", comment);
        model.addAttribute("fieldErrors", fieldErrors);
    }

    private String redirectBack(String referer, String fallback) {
        return "redirect:" + (referer == null || referer.isBlank() ? fallback : referer);
    }

    private void fillConfiguratorModel(Model model) {
        List<ComponentItem> componentItems = components.findByActiveTrueOrderByCategoryNameAscNameAsc();
        Map<String, List<ComponentItem>> byType = componentItems.stream()
                .collect(Collectors.groupingBy(this::partType));
        List<ConfiguratorGroup> groups = List.of(
                group(byType, "CASE", "Корпус", true, true),
                group(byType, "MOTHERBOARD", "Материнская плата", true, true),
                group(byType, "CPU", "Процессор", true, true),
                group(byType, "COOLING", "Система охлаждения", false, false),
                group(byType, "RAM", "Оперативная память", true, true),
                group(byType, "GPU", "Видеокарта", false, true),
                group(byType, "M2", "Накопитель SSD (M.2)", false, false),
                group(byType, "SATA", "Накопитель SSD (2.5\")", false, false),
                group(byType, "PSU", "Блок питания", true, true)
        );
        model.addAttribute("groups", groups);
        model.addAttribute("components", componentItems);
        model.addAttribute("readyPcs", readyPcs.findAll());
    }

    private ConfiguratorGroup group(Map<String, List<ComponentItem>> byType, String partType, String title,
                                    boolean required, boolean singleChoice) {
        List<ComponentItem> items = byType.getOrDefault(partType, List.of()).stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .toList();
        return new ConfiguratorGroup(partType, title, required, singleChoice, items);
    }

    private String partType(ComponentItem item) {
        return item.getPartType() == null || item.getPartType().isBlank() ? "OTHER" : item.getPartType();
    }
}
