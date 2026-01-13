package tuum.example.tuum.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import tuum.example.tuum.entity.Account;

@Mapper
public interface AccountRepository {

    @Insert("INSERT INTO account (customer_id, country) VALUES (#{customerId}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Account account);

    @Select("SELECT id, customer_id, country FROM account WHERE id = #{id}")
    Account findById(Long id);
}
