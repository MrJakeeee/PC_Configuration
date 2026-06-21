package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.repository.CategoryRepository;
import kg.kstu.pcconfiguration.repository.CartItemRepository;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.FavoriteItemRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import kg.kstu.pcconfiguration.service.UserService;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogController {
    private final CategoryRepository categories;
    private final ComponentItemRepository components;
    private final ReadyPcRepository readyPcs;
    private final UserService users;
    private final CartItemRepository cartItems;
    private final FavoriteItemRepository favoriteItems;

    public CatalogController(CategoryRepository categories, ComponentItemRepository components, ReadyPcRepository readyPcs,
                             UserService users, CartItemRepository cartItems, FavoriteItemRepository favoriteItems) {
        this.categories = categories;
        this.components = components;
        this.readyPcs = readyPcs;
        this.users = users;
        this.cartItems = cartItems;
        this.favoriteItems = favoriteItems;
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String q,
                          Model model,
                          Principal principal) {
        model.addAttribute("categories", categories.findAll());
        model.addAttribute("items", components.search(categoryId, emptyToNull(q)));
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("q", q);
        fillProductState(model, principal);
        return "catalog";
    }

    @GetMapping("/ready-pcs")
    public String readyPcs(@RequestParam(required = false) String q, Model model, Principal principal) {
        model.addAttribute("pcs", readyPcs.search(emptyToNull(q)));
        model.addAttribute("q", q);
        fillProductState(model, principal);
        return "ready-pcs";
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void fillProductState(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("cartComponentIds", Set.of());
            model.addAttribute("cartReadyPcIds", Set.of());
            model.addAttribute("favoriteComponentIds", Set.of());
            model.addAttribute("favoriteReadyPcIds", Set.of());
            return;
        }

        var user = users.findByUsername(principal.getName());
        model.addAttribute("cartComponentIds", cartItems.findByUserOrderByIdDesc(user).stream()
                .filter(item -> item.getComponent() != null)
                .map(item -> item.getComponent().getId())
                .collect(Collectors.toSet()));
        model.addAttribute("cartReadyPcIds", cartItems.findByUserOrderByIdDesc(user).stream()
                .filter(item -> item.getReadyPc() != null)
                .map(item -> item.getReadyPc().getId())
                .collect(Collectors.toSet()));
        model.addAttribute("favoriteComponentIds", favoriteItems.findByUserOrderByIdDesc(user).stream()
                .filter(item -> item.getComponent() != null)
                .map(item -> item.getComponent().getId())
                .collect(Collectors.toSet()));
        model.addAttribute("favoriteReadyPcIds", favoriteItems.findByUserOrderByIdDesc(user).stream()
                .filter(item -> item.getReadyPc() != null)
                .map(item -> item.getReadyPc().getId())
                .collect(Collectors.toSet()));
    }
}
