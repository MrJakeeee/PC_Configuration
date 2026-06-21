package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.CustomerOrder;
import kg.kstu.pcconfiguration.model.OrderStatus;
import kg.kstu.pcconfiguration.model.PcBuild;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(AppUser user);
    List<CustomerOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    List<CustomerOrder> findByBuild(PcBuild build);
}
