package org.example.shoppingmall.service;

import org.example.shoppingmall.dto.PageResult;
import org.example.shoppingmall.dto.ProductDTO;
import org.example.shoppingmall.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    /**
     * 获取商品列表（支持搜索和筛选）
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param sort 排序字段
     * @param order 排序方式
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 分页商品列表
     */
    PageResult<ProductDTO> getProducts(String keyword, Integer categoryId, int pageNum, int pageSize,
                                       String sort, String order, BigDecimal minPrice, BigDecimal maxPrice, String type);

    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品信息
     */
    ProductDTO getProductById(Long id);

    /**
     * 根据ID获取商品实体
     * @param id 商品ID
     * @return 商品实体
     */
    Product getProductEntityById(Long id);

    /**
     * 创建商品
     * @param productDTO 商品信息
     * @return 创建后的商品
     */
    ProductDTO createProduct(ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> otherImageFiles, Long sellerId); // ★★★ 添加 sellerId 参数 ★★★

    /**
     * 更新商品
     * @param id 商品ID
     * @param productDTO 商品信息
     * @return 更新后的商品
     */
    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    /**
     * 删除商品
     * @param id 商品ID
     */
    void deleteProduct(Long id);

    /**
     * 上传商品图片
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadProductImage(MultipartFile file);
}