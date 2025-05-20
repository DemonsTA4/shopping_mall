package org.example.shoppingmall.controller; // 请替换为您的实际包路径

import lombok.RequiredArgsConstructor;
import org.example.shoppingmall.common.Result; // 假设这是您项目中通用的Result包装类
import org.example.shoppingmall.dto.BannerDto;
import org.example.shoppingmall.service.BannerService; // 引入您定义的BannerService接口
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // 用于校验请求体

import java.util.List;

@RestController
@RequestMapping("/api/banners") // 所有Banner相关API的基础路径
@RequiredArgsConstructor // Lombok: 自动生成依赖注入的构造函数 (final字段)
public class BannerController {

    private final BannerService bannerService; // 注入BannerService接口

    /**
     * 获取首页轮播图数据 (推荐)
     * 前端主要从此接口获取首页轮播图。
     * 对应 Service 中的 getHomeBanners() 方法。
     * 请求路径: GET /api/banners/home
     */
    @GetMapping("/home")
    public Result<List<BannerDto>> getHomeBanners() {
        List<BannerDto> banners = bannerService.getHomeBanners();
        return Result.success(banners);
    }

    /**
     * 根据位置获取激活状态的Banner列表
     * 这是一个更通用的接口，可以根据 position 参数获取不同位置的Banner。
     * 如果 position 参数为 "home" 或未提供，则默认也调用 getHomeBanners()。
     * 请求路径: GET /api/banners?position=some_position
     */
    @GetMapping
    public Result<List<BannerDto>> getBannersByPosition(
            @RequestParam(required = false, defaultValue = "home") String position) {
        // 如果请求的是首页Banner，或者未指定位置，则直接调用为首页优化的方法
        if ("home".equalsIgnoreCase(position)) {
            List<BannerDto> banners = bannerService.getHomeBanners();
            return Result.success(banners);
        }
        // 否则，根据指定位置获取
        List<BannerDto> banners = bannerService.getActiveBannersByPosition(position);
        return Result.success(banners);
    }

    /*
     * --- 以下是可选的管理端 CRUD 操作API ---
     * 通常这些API需要权限控制，例如仅管理员可访问。
     * 您可以根据需要取消注释并添加 @PreAuthorize("hasRole('ADMIN')") 等安全注解。
     */

    /**
     * 创建新的Banner
     * 请求路径: POST /api/banners
     * 请求体: BannerDto 对象 (JSON格式)
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // 示例：仅管理员可创建
    public Result<BannerDto> createBanner(@Valid @RequestBody BannerDto bannerDto) {
        BannerDto createdBanner = bannerService.createBanner(bannerDto);
        return Result.success(createdBanner);
    }

    /**
     * 根据ID获取Banner详情
     * 请求路径: GET /api/banners/{id}
     */
    @GetMapping("/{id}")
    public Result<BannerDto> getBannerById(@PathVariable Integer id) {
        BannerDto bannerDto = bannerService.getBannerById(id);
        return Result.success(bannerDto);
    }

    /**
     * 更新指定ID的Banner信息
     * 请求路径: PUT /api/banners/{id}
     * 请求体: BannerDto 对象 (JSON格式)
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 示例：仅管理员可更新
    public Result<BannerDto> updateBanner(@PathVariable Integer id, @Valid @RequestBody BannerDto bannerDto) {
        BannerDto updatedBanner = bannerService.updateBanner(id, bannerDto);
        return Result.success(updatedBanner);
    }

    /**
     * 删除指定ID的Banner
     * 请求路径: DELETE /api/banners/{id}
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // 示例：仅管理员可删除
    public Result<Void> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        return Result.success(); // 通常删除成功返回无数据的成功响应
    }

    /**
     * (管理端) 分页获取所有Banner列表 (包括非激活状态的)
     * 请求路径: GET /api/banners/all?page=0&size=10&sort=id,desc
     */
    @GetMapping("/all")
    // @PreAuthorize("hasRole('ADMIN')") // 示例：仅管理员可访问
    public Result<Page<BannerDto>> getAllBanners(@PageableDefault(size = 10, sort = "sortOrder") Pageable pageable) {
        Page<BannerDto> bannersPage = bannerService.getAllBanners(pageable);
        return Result.success(bannersPage);
    }

    /**
     * 获取所有激活状态的Banner列表 (不区分位置，按排序值排序)
     * 请求路径: GET /api/banners/active
     */
    @GetMapping("/active")
    public Result<List<BannerDto>> getAllActiveBanners() {
        List<BannerDto> activeBanners = bannerService.getAllActiveBanners();
        return Result.success(activeBanners);
    }
}