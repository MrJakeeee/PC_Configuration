package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.ComponentItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComponentItemRepository extends JpaRepository<ComponentItem, Long> {
    Optional<ComponentItem> findByName(String name);
    List<ComponentItem> findByActiveTrueOrderByCategoryNameAscNameAsc();
    List<ComponentItem> findByCategoryIdAndActiveTrueOrderByNameAsc(Long categoryId);

    default List<ComponentItem> search(Long categoryId, String keyword) {
        if (keyword == null) {
            return categoryId == null
                    ? findByActiveTrueOrderByCategoryNameAscNameAsc()
                    : findByCategoryIdAndActiveTrueOrderByNameAsc(categoryId);
        }
        return categoryId == null
                ? searchByKeyword(keyword)
                : searchByCategoryAndKeyword(categoryId, keyword);
    }

    @Query("""
            select c from ComponentItem c
            where c.active = true
              and (lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(c.brand) like lower(concat('%', :keyword, '%'))
                   or lower(c.specs) like lower(concat('%', :keyword, '%')))
            order by c.category.name, c.name
            """)
    List<ComponentItem> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
            select c from ComponentItem c
            where c.active = true
              and c.category.id = :categoryId
              and (lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(c.brand) like lower(concat('%', :keyword, '%'))
                   or lower(c.specs) like lower(concat('%', :keyword, '%')))
            order by c.category.name, c.name
            """)
    List<ComponentItem> searchByCategoryAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
}
