package app.testutils;

import app.enums.UserRole;
import app.persistence.daos.UserDAO;
import app.persistence.daos.StationDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.Station;
import app.persistence.entities.User;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;
import java.util.Map;

public class TestPopulator
{
    private final UserDAO userDAO;
    private final StationDAO stationDAO;

    public TestPopulator(EntityManagerFactory emf)
    {
        this.userDAO = new UserDAO(emf);
        this.stationDAO = new StationDAO(emf);
    }

    public Map<String, IEntity> populate()
    {
        Map<String, IEntity> map = new HashMap<>();

        // --- Stations ---
        Station s1 = new Station("Cold Kitchen", "Salads & Starters");
        Station s2 = new Station("Hot Kitchen", "Main Courses");
        Station s3 = new Station("Pastry", "Desserts & Bread");
        Station s4 = new Station("Grill", "Steaks & BBQ");

        stationDAO.create(s1);
        stationDAO.create(s2);
        stationDAO.create(s3);
        stationDAO.create(s4);

        // --- Users ---
        User u1 = new User("Gordon", "Ramsay", "gordon@kitchen.com", "hash1", UserRole.HEAD_CHEF);
        User u2 = new User("Claire", "Smyth", "claire@pastry.com", "hash2", UserRole.LINE_COOK);
        User u3 = new User("Marco", "Pierre", "marco@grill.com", "hash3", UserRole.LINE_COOK);
        User u4 = new User("Rene", "Redzepi", "lars@cold.com", "hash4", UserRole.LINE_COOK);

        userDAO.create(u1);
        userDAO.create(u2);
        userDAO.create(u3);
        userDAO.create(u4);

        map.put("user1", u1);
        map.put("user2", u2);
        map.put("user3", u3);
        map.put("user4", u4);


        return map;
    }
}
