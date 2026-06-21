package kg.kstu.pcconfiguration.service;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.CartItem;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.CustomerOrder;
import kg.kstu.pcconfiguration.model.FavoriteItem;
import kg.kstu.pcconfiguration.model.ReadyPc;
import kg.kstu.pcconfiguration.repository.CartItemRepository;
import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.repository.FavoriteItemRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {
    private final CartItemRepository cartItems;
    private final FavoriteItemRepository favoriteItems;
    private final ComponentItemRepository components;
    private final ReadyPcRepository readyPcs;
    private final CustomerOrderRepository orders;
    private final OrderService orderService;

    public ShopService(CartItemRepository cartItems, FavoriteItemRepository favoriteItems,
                       ComponentItemRepository components, ReadyPcRepository readyPcs,
                       CustomerOrderRepository orders, OrderService orderService) {
        this.cartItems = cartItems;
        this.favoriteItems = favoriteItems;
        this.components = components;
        this.readyPcs = readyPcs;
        this.orders = orders;
        this.orderService = orderService;
    }

    @Transactional
    public void addComponentToCart(AppUser user, Long componentId) {
        ComponentItem component = components.findById(componentId).orElseThrow();
        CartItem item = cartItems.findByUserAndComponent(user, component).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setUser(user);
            created.setComponent(component);
            return created;
        });
        if (item.getId() != null) {
            item.setQuantity(item.getQuantity() + 1);
        }
        cartItems.save(item);
    }

    @Transactional
    public void addReadyPcToCart(AppUser user, Long readyPcId) {
        ReadyPc readyPc = readyPcs.findById(readyPcId).orElseThrow();
        CartItem item = cartItems.findByUserAndReadyPc(user, readyPc).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setUser(user);
            created.setReadyPc(readyPc);
            return created;
        });
        if (item.getId() != null) {
            item.setQuantity(item.getQuantity() + 1);
        }
        cartItems.save(item);
    }

    @Transactional
    public void addComponentToFavorites(AppUser user, Long componentId) {
        ComponentItem component = components.findById(componentId).orElseThrow();
        favoriteItems.findByUserAndComponent(user, component).orElseGet(() -> {
            FavoriteItem item = new FavoriteItem();
            item.setUser(user);
            item.setComponent(component);
            return favoriteItems.save(item);
        });
    }

    @Transactional
    public void addReadyPcToFavorites(AppUser user, Long readyPcId) {
        ReadyPc readyPc = readyPcs.findById(readyPcId).orElseThrow();
        favoriteItems.findByUserAndReadyPc(user, readyPc).orElseGet(() -> {
            FavoriteItem item = new FavoriteItem();
            item.setUser(user);
            item.setReadyPc(readyPc);
            return favoriteItems.save(item);
        });
    }

    @Transactional
    public void updateCart(AppUser user, List<Long> cartItemIds, List<Integer> quantities) {
        if (cartItemIds == null || quantities == null) {
            return;
        }
        for (int i = 0; i < cartItemIds.size() && i < quantities.size(); i++) {
            Long id = cartItemIds.get(i);
            int quantity = quantities.get(i);
            cartItems.findById(id)
                    .filter(item -> item.getUser().getId().equals(user.getId()))
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        cartItems.save(item);
                    });
        }
    }

    @Transactional
    public void deleteCartItems(AppUser user, List<Long> selectedIds) {
        selectedCartItems(user, selectedIds).forEach(cartItems::delete);
    }

    @Transactional
    public void addFavoritesToCart(AppUser user, List<Long> selectedIds) {
        List<FavoriteItem> selected = selectedFavoriteItems(user, selectedIds);
        selected.forEach(item -> {
            if (item.getComponent() != null) {
                addComponentToCart(user, item.getComponent().getId());
            } else {
                addReadyPcToCart(user, item.getReadyPc().getId());
            }
        });
    }

    @Transactional
    public void deleteFavorites(AppUser user, List<Long> selectedIds) {
        selectedFavoriteItems(user, selectedIds).forEach(favoriteItems::delete);
    }

    @Transactional
    public CustomerOrder orderCart(AppUser user, List<Long> selectedIds, String firstName, String lastName, String phone,
                                   String address, String cardNumber, String cardExpiry, String cardCvv, String comment) {
        List<CartItem> selected = selectedCartItems(user, selectedIds);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Выберите товары для оформления заказа");
        }
        CustomerOrder order = orderService.baseOrder(user, firstName, lastName, phone, address, cardNumber, cardExpiry, cardCvv, comment);
        order.setAmount(totalCart(selected));
        order.setItemSummary(summaryCart(selected));
        CustomerOrder saved = orders.save(order);
        selected.forEach(cartItems::delete);
        return saved;
    }

    public BigDecimal totalCart(List<CartItem> items) {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalFavorites(List<FavoriteItem> items) {
        return items.stream()
                .map(FavoriteItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CartItem> selectedCartItems(AppUser user, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }
        return cartItems.findByUserAndIdIn(user, selectedIds);
    }

    private List<FavoriteItem> selectedFavoriteItems(AppUser user, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }
        return favoriteItems.findByUserAndIdIn(user, selectedIds);
    }

    private String summaryCart(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getName() + " x" + item.getQuantity() + " - " + item.getTotalPrice() + " сом")
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }
}
