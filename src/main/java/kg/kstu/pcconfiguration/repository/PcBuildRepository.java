package kg.kstu.pcconfiguration.repository;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.PcBuild;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcBuildRepository extends JpaRepository<PcBuild, Long> {
    List<PcBuild> findByUserOrderByIdDesc(AppUser user);
}
