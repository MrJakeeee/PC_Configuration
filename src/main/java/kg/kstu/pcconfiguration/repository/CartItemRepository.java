package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.CartItem;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.ReadyPc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserOrderByIdDesc(AppUser user);
    List<CartItem> findByUserAndIdIn(AppUser user, List<Long> ids);
    Optional<CartItem> findByUserAndComponent(AppUser user, ComponentItem component);
    Optional<CartItem> findByUserAndReadyPc(AppUser user, ReadyPc readyPc);
}
