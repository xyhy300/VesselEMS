package vesselems.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vesselems.repository.MenuRepository;

@Component
public class NL2SQLMenuInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    private final MenuRepository menuRepo;

    public NL2SQLMenuInitializer(MenuRepository menuRepo) {
        this.menuRepo = menuRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        insertIfAbsent(4L, null, "NL2SQL分析", "/workspace/nlquery", 0, 8);
        insertIfAbsent(41L, 4L, "数据源管理", "/workspace/datasources", 1, 1);
        insertIfAbsent(42L, 4L, "自然语言查询", "/workspace/nlquery", 1, 2);
        insertIfAbsent(43L, 4L, "查询历史", "/workspace/dialogs", 1, 3);
        insertIfAbsent(44L, 4L, "模型配置", "/workspace/models", 1, 4);
    }

    private void insertIfAbsent(Long id, Long parentId, String name, String path, int type, int sort) {
        if (menuRepo.existsById(id)) return;
        entityManager.createNativeQuery(
                "INSERT INTO menu(id,parent_id,menu_name,menu_path,menu_type,sort_order,status,visible,is_frame,create_time) VALUES(?,?,?,?,?,?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, parentId)
                .setParameter(3, name)
                .setParameter(4, path)
                .setParameter(5, type)
                .setParameter(6, sort)
                .setParameter(7, 1)
                .setParameter(8, 1)
                .setParameter(9, 1)
                .setParameter(10, LocalDateTime.now())
                .executeUpdate();
    }
}