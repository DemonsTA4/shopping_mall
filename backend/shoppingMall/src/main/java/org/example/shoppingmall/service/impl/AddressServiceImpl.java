package org.example.shoppingmall.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.dto.AddressDto;
import org.example.shoppingmall.entity.Address;
import org.example.shoppingmall.entity.User;
import org.example.shoppingmall.repository.AddressRepository;
import org.example.shoppingmall.repository.UserRepository;
import org.example.shoppingmall.service.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressDto createAddress(Long userId, AddressDto addressDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在，ID: " + userId));

        Address address = new Address();
        BeanUtils.copyProperties(addressDto, address, "id", "user", "createTime", "updateTime");
        address.setUser(user);

        boolean isFirstAddress = addressRepository.countByUserId(userId) == 0;
        boolean makeThisDefault = Boolean.TRUE.equals(addressDto.getIsDefault()) || isFirstAddress;

        if (makeThisDefault) {
            // 先将该用户的所有地址设为非默认
            // 这需要一个只接受 userId 的方法
            // 假设我们添加/使用 clearDefaultAddressForUser(userId)
            addressRepository.clearDefaultAddressForUser(userId); // <--- 假设这个方法存在且将用户所有地址设为非默认
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }

    private AddressDto convertToDto(Address address) {
        if (address == null) return null;
        AddressDto dto = new AddressDto();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long userId, Long addressId, AddressDto addressDto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在，ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("无权访问此地址");
        }

        BeanUtils.copyProperties(addressDto, address, "id", "user");

        // 如果设置为默认地址，需要将其他地址设为非默认
        if (addressDto.getIsDefault() && !address.getIsDefault()) {
            addressRepository.unsetDefaultAddressesExcept(userId, addressId);
        }

        Address updatedAddress = addressRepository.save(address);
        AddressDto updatedDto = new AddressDto();
        BeanUtils.copyProperties(updatedAddress, updatedDto);
        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在，ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("无权访问此地址");
        }

        // 如果删除的是默认地址，并且还有其他地址，需要将其他某个地址设为默认
        if (address.getIsDefault()) {
            List<Address> otherAddresses = addressRepository.findByUserId(userId)
                    .stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .collect(Collectors.toList());

            if (!otherAddresses.isEmpty()) {
                Address newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        addressRepository.deleteById(addressId);
    }

    @Override
    public AddressDto getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在，ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("无权访问此地址");
        }

        AddressDto addressDto = new AddressDto();
        BeanUtils.copyProperties(address, addressDto);
        return addressDto;
    }

    @Override
    public List<AddressDto> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在，ID: " + userId);
        }

        return addressRepository.findByUserId(userId).stream()
                .map(address -> {
                    AddressDto dto = new AddressDto();
                    BeanUtils.copyProperties(address, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AddressDto getUserDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(address -> {
                    AddressDto dto = new AddressDto();
                    BeanUtils.copyProperties(address, dto);
                    return dto;
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("地址不存在，ID: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("无权访问此地址");
        }

        // 将其他地址设为非默认
        addressRepository.unsetDefaultAddressesExcept(userId, addressId);
        
        // 设置当前地址为默认
        address.setIsDefault(true);
        addressRepository.save(address);
    }
} 