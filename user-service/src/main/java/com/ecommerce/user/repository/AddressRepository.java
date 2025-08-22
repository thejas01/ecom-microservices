package com.ecommerce.user.repository;

import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUserOrderByIsDefaultDescCreatedAtAsc(User user);

    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtAsc(String userId);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);

    Optional<Address> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :addressId")
    void unsetDefaultAddressesExcept(@Param("userId") String userId, @Param("addressId") String addressId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetAllDefaultAddresses(@Param("userId") String userId);

    boolean existsByUserIdAndIsDefaultTrue(String userId);

    long countByUserId(String userId);
}