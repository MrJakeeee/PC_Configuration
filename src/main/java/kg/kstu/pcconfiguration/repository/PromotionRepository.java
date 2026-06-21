package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.Promotion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("""
            select p from Promotion p
            where p.active = true and (p.activeUntil is null or p.activeUntil >= :today)
            order by p.discountPercent desc
            """)
    List<Promotion> findActive(@Param("today") LocalDate today);
}
