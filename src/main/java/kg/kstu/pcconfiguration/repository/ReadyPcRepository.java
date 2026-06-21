package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.ReadyPc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadyPcRepository extends JpaRepository<ReadyPc, Long> {
    Optional<ReadyPc> findByName(String name);
    List<ReadyPc> findAllByOrderByPriceAsc();

    default List<ReadyPc> search(String keyword) {
        return keyword == null ? findAllByOrderByPriceAsc() : searchByKeyword(keyword);
    }

    @Query("""
            select p from ReadyPc p
            where lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(p.purpose) like lower(concat('%', :keyword, '%'))
               or lower(p.description) like lower(concat('%', :keyword, '%'))
            order by p.price
            """)
    List<ReadyPc> searchByKeyword(@Param("keyword") String keyword);
}
