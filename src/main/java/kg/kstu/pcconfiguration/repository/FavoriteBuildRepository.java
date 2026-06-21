package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.FavoriteBuild;
import kg.kstu.pcconfiguration.model.PcBuild;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteBuildRepository extends JpaRepository<FavoriteBuild, Long> {
    List<FavoriteBuild> findByUserOrderByIdDesc(AppUser user);
    Optional<FavoriteBuild> findByUserAndBuild(AppUser user, PcBuild build);
    List<FavoriteBuild> findByBuild(PcBuild build);
    boolean existsByUserAndBuild(AppUser user, PcBuild build);
}
