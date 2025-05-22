package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.AddressDto;

import java.util.List;

public interface AddressService {

    AddressDto createAddress(Long userId, AddressDto addressDto);

    AddressDto updateAddress(Long userId, Long addressId, AddressDto addressDto);

    void deleteAddress(Long userId, Long addressId);

    AddressDto getAddressById(Long userId, Long addressId);

    List<AddressDto> getUserAddresses(Long userId);

    AddressDto getUserDefaultAddress(Long userId);

    void setDefaultAddress(Long userId, Long addressId);
} 