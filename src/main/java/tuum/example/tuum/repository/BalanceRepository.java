package tuum.example.tuum.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tuum.example.tuum.entity.Balance;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BalanceRepository {

    @Insert("INSERT INTO balance (account_id, currency, available_amount) VALUES (#{accountId}, #{currency}, #{availableAmount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Balance balance);

    @Select("SELECT id, account_id, currency, available_amount FROM balance WHERE account_id = #{accountId} ORDER BY currency")
    List<Balance> findByAccountId(Long accountId);

    @Select("SELECT id, account_id, currency, available_amount FROM balance WHERE account_id = #{accountId} AND currency = #{currency}")
    Balance findByAccountIdAndCurrency(@Param("accountId") Long accountId, @Param("currency") String currency);

    @Update("UPDATE balance SET available_amount = available_amount + #{delta} WHERE account_id = #{accountId} AND currency = #{currency}")
    int updateAvailableAmount(@Param("accountId") Long accountId, @Param("currency") String currency, @Param("delta") BigDecimal delta);
}
