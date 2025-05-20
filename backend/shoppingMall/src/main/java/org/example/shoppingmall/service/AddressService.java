package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.AddressDto;

import java.util.List;

public interface AddressService {

    AddressDto createAddress(Long userId, AddressDto addressDto);

    AddressDto updateAddress(Long userId, Integer addressId, AddressDto addressDto);

    void deleteAddress(Long userId, Integer addressId);

    AddressDto getAddressById(Long userId, Integer addressId);

    List<AddressDto> getUserAddresses(Long userId);

    AddressDto getUserDefaultAddress(Long userId);

    void setDefaultAddress(Long userId, Integer addressId);
} 