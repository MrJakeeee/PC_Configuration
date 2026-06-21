package kg.kstu.pcconfiguration.controller;

import kg.kstu.pcconfiguration.model.OrderStatus;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.service.OrderService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/deliverer")
public class DelivererController {
    private final CustomerOrderRepository orders;
    private final OrderService orderService;

    public DelivererController(CustomerOrderRepository orders, OrderService orderService) {
        this.orders = orders;
        this.orderService = orderService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("readyOrders", orders.findByStatusOrderByCreatedAtAsc(OrderStatus.READY_FOR_DELIVERY));
        model.addAttribute("orders", orders.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "deliverer/index";
    }

    @PostMapping("/orders")
    public String update(@RequestParam Long id,
                         @RequestParam OrderStatus status,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDueDate,
                         @RequestParam(required = false) String comment) {
        orderService.updateDelivery(id, status, deliveryDueDate, comment);
        return "redirect:/deliverer";
    }
}
