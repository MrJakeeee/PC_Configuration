package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.model.Category;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.CustomerOrder;
import kg.kstu.pcconfiguration.model.OrderStatus;
import kg.kstu.pcconfiguration.model.PaymentStatus;
import kg.kstu.pcconfiguration.model.Promotion;
import kg.kstu.pcconfiguration.model.ReadyPc;
import kg.kstu.pcconfiguration.repository.CategoryRepository;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.repository.PromotionRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final CategoryRepository categories;
    private final ComponentItemRepository components;
    private final ReadyPcRepository readyPcs;
    private final PromotionRepository promotions;
    private final CustomerOrderRepository orders;

    public AdminController(CategoryRepository categories, ComponentItemRepository components,
                           ReadyPcRepository readyPcs, PromotionRepository promotions,
                           CustomerOrderRepository orders) {
        this.categories = categories;
        this.components = components;
        this.readyPcs = readyPcs;
        this.promotions = promotions;
        this.orders = orders;
    }

    @GetMapping
    public String admin(Model model) {
        model.addAttribute("orders", orders.findAll());
        model.addAttribute("componentsCount", components.count());
        model.addAttribute("readyPcsCount", readyPcs.count());
        return "admin/index";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categories.findAll());
        model.addAttribute("category", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String description) {
        Category category = id == null ? new Category() : categories.findById(id).orElse(new Category());
        category.setName(name);
        category.setDescription(description);
        categories.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/components")
    public String components(Model model) {
        model.addAttribute("components", components.findAll());
        model.addAttribute("categories", categories.findAll());
        return "admin/components";
    }

    @PostMapping("/components")
    public String saveComponent(@RequestParam(required = false) Long id,
                                @RequestParam String name,
                                @RequestParam Long categoryId,
                                @RequestParam BigDecimal price,
                                @RequestParam(required = false) String brand,
                                @RequestParam(required = false) String partType,
                                @RequestParam(required = false) String socketType,
                                @RequestParam(required = false) String memoryType,
                                @RequestParam(required = false) String formFactor,
                                @RequestParam(required = false) String storageInterface,
                                @RequestParam(required = false) Integer wattage,
                                @RequestParam Integer stock,
                                @RequestParam(required = false) String specs) {
        ComponentItem item = id == null ? new ComponentItem() : components.findById(id).orElse(new ComponentItem());
        item.setName(name);
        item.setCategory(categories.findById(categoryId).orElseThrow());
        item.setPrice(price);
        item.setBrand(brand);
        item.setPartType(partType);
        item.setSocketType(socketType);
        item.setMemoryType(memoryType);
        item.setFormFactor(formFactor);
        item.setStorageInterface(storageInterface);
        item.setWattage(wattage);
        item.setStock(stock);
        item.setSpecs(specs);
        item.setActive(true);
        components.save(item);
        return "redirect:/admin/components";
    }

    @GetMapping("/ready-pcs")
    public String readyPcs(Model model) {
        model.addAttribute("pcs", readyPcs.findAll());
        return "admin/ready-pcs";
    }

    @PostMapping("/ready-pcs")
    public String saveReadyPc(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String purpose,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer stock,
                              @RequestParam String description) {
        ReadyPc pc = id == null ? new ReadyPc() : readyPcs.findById(id).orElse(new ReadyPc());
        pc.setName(name);
        pc.setPurpose(purpose);
        pc.setPrice(price);
        pc.setStock(stock);
        pc.setDescription(description);
        readyPcs.save(pc);
        return "redirect:/admin/ready-pcs";
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotions.findAll());
        return "admin/promotions";
    }

    @PostMapping("/promotions")
    public String savePromotion(Promotion promotion) {
        promotions.save(promotion);
        return "redirect:/admin/promotions";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orders.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        return "admin/orders";
    }

    @PostMapping("/orders")
    public String updateOrder(@RequestParam Long id,
                              @RequestParam OrderStatus status,
                              @RequestParam PaymentStatus paymentStatus) {
        CustomerOrder order = orders.findById(id).orElseThrow();
        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        orders.save(order);
        return "redirect:/admin/orders";
    }
}
