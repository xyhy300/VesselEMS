package vesselems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Model;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
}