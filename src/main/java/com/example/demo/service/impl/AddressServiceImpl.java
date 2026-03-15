package com.example.demo.service.impl;

import com.example.demo.mapper.AddressMapper;
import com.example.demo.pojo.dto.AddressDTO;
import com.example.demo.pojo.entity.Address;
import com.example.demo.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.listByUser(userId);
    }

    @Override
    public Address create(Long userId, AddressDTO dto) {
        validate(dto);
        Address addr = toEntity(dto);
        addr.setUserId(userId);
        addr.setCreateTime(LocalDateTime.now());
        addr.setUpdateTime(LocalDateTime.now());
        if (addr.getIsDefault() == null) {
            addr.setIsDefault(0);
        }
        if (addr.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.insert(addr);
        return addressMapper.findById(addr.getAddressId());
    }

    @Override
    public Address update(Long userId, Long addressId, AddressDTO dto) {
        Address existing = addressMapper.findById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        validate(dto);
        existing.setContactName(dto.getContactName());
        existing.setContactPhone(dto.getContactPhone());
        existing.setAddress(dto.getAddress());
        existing.setBuilding(dto.getBuilding());
        existing.setRoom(dto.getRoom());
        existing.setIsDefault(dto.getIsDefault());
        existing.setUpdateTime(LocalDateTime.now());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.clearDefault(userId);
        }
        addressMapper.update(existing);
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.setDefault(addressId);
        }
        return addressMapper.findById(addressId);
    }

    @Override
    public void delete(Long userId, Long addressId) {
        Address existing = addressMapper.findById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        addressMapper.delete(addressId);
    }

    @Override
    public void setDefault(Long userId, Long addressId) {
        Address existing = addressMapper.findById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(addressId);
    }

    private void validate(AddressDTO dto) {
        if (!StringUtils.hasText(dto.getContactName())) {
            throw new IllegalArgumentException("收货人不能为空");
        }
        if (!StringUtils.hasText(dto.getContactPhone())) {
            throw new IllegalArgumentException("联系电话不能为空");
        }
        if (!StringUtils.hasText(dto.getAddress())) {
            throw new IllegalArgumentException("地址不能为空");
        }
    }

    private Address toEntity(AddressDTO dto) {
        Address a = new Address();
        a.setContactName(dto.getContactName());
        a.setContactPhone(dto.getContactPhone());
        a.setAddress(dto.getAddress());
        a.setBuilding(dto.getBuilding());
        a.setRoom(dto.getRoom());
        a.setLongitude(dto.getLongitude());
        a.setLatitude(dto.getLatitude());
        a.setIsDefault(dto.getIsDefault());
        return a;
    }
}

