package wishlist.repository;

import wishlist.exception.ErrorCode;
import wishlist.exception.ItemNotFoundException;
import wishlist.model.Item;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTemplateRepository implements ItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(Item item) {
        var sql = "insert into item (name,price,imgUrl) values (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getImgUrl());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    @Override
    public Item findById(Long id) {
        try {
            Item item = jdbcTemplate.queryForObject(
                "select * from item where id =?",
                (rs, rowNum) -> new Item(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("price"),
                    rs.getString("imgUrl")
                ),
                id
            );
            return item;
        } catch (Exception e) {
            throw new ItemNotFoundException(ErrorCode.ITEM_NOT_FOUND);
        }
    }

    @Override
    public List<Item> findAll() {
        return jdbcTemplate.query(
            "select* from item",
            (rs, rowNum) -> new Item(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("price"),
                rs.getString("imgUrl")
            )
        );
    }

    @Override
    public void update(Item item) {
        jdbcTemplate.update(
            "update item set name = ?, price = ?, imgurl = ? where id = ?",
            item.getName(), item.getPrice(), item.getImgUrl(), item.getId()
        );
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("delete from item where id = ?", id);
    }
}
