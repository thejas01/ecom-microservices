package com.ecommerce.user.mapper;

import com.ecommerce.common.dto.user.AddressDTO;
import com.ecommerce.user.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }

        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setType(address.getType() != null ? address.getType().name() : null);
        dto.setLabel(address.getLabel());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setDefault(address.isDefault());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());

        return dto;
    }

    public Address toEntity(AddressDTO dto) {
        if (dto == null) {
            return null;
        }

        return Address.builder()
                .id(dto.getId())
                .type(dto.getType() != null ? Address.AddressType.valueOf(dto.getType()) : null)
                .label(dto.getLabel())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .isDefault(dto.isDefault())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}