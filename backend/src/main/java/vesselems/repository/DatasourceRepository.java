package vesselems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Datasource;

@Repository
public interface DatasourceRepository extends JpaRepository<Datasource, Long> {
}