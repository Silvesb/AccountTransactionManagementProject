package tuum.example.tuum.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import tuum.example.tuum.entity.AccountTransaction;

import java.util.List;

@Mapper
public interface TransactionRepository {

    @Insert("INSERT INTO account_transaction (account_id, amount, currency, direction, description, balance_after) " +
            "VALUES (#{accountId}, #{amount}, #{currency}, #{direction}, #{description}, #{balanceAfter})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AccountTransaction transaction);

    @Select("SELECT id, account_id, amount, currency, direction, description, balance_after, created_at " +
            "FROM account_transaction WHERE account_id = #{accountId} ORDER BY id")
    List<AccountTransaction> findByAccountId(Long accountId);
}
