package com.ecommerce.user.config;

import com.ecommerce.user.mapper.AddressMapper;
import com.ecommerce.user.mapper.UserMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AddressMapper addressMapper() {
        return new AddressMapper();
    }

    @Bean
    @Primary
    public UserMapper userMapper(AddressMapper addressMapper) {
        return new UserMapper(addressMapper);
    }
}