package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.ComponentItem;
import kg.kstu.pcconfiguration.model.FavoriteItem;
import kg.kstu.pcconfiguration.model.ReadyPc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
    List<FavoriteItem> findByUserOrderByIdDesc(AppUser user);
    List<FavoriteItem> findByUserAndIdIn(AppUser user, List<Long> ids);
    Optional<FavoriteItem> findByUserAndComponent(AppUser user, ComponentItem component);
    Optional<FavoriteItem> findByUserAndReadyPc(AppUser user, ReadyPc readyPc);
}
