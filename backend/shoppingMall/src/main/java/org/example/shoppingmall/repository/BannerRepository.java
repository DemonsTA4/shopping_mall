package org.example.shoppingmall.repository;

import org.example.shoppingmall.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    List<Banner> findByIsActiveTrueOrderBySortOrder();

    List<Banner> findByPositionAndIsActiveTrueOrderBySortOrder(String position);

    List<Banner> findTop5ByIsActiveTrueOrderBySortOrder();

    List<Banner> findTop5ByPositionAndIsActiveTrueOrderBySortOrder(String position);
} 