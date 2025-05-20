package org.example.shoppingmall.service.impl; // 请确认这是你正确的包名

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shoppingmall.common.ResultCode; // 假设你有这个枚举或类
import org.example.shoppingmall.dto.PageResult;
import org.example.shoppingmall.dto.ProductDTO;
import org.example.shoppingmall.entity.Category;
import org.example.shoppingmall.entity.Product;
import org.example.shoppingmall.exception.ApiException;
import org.example.shoppingmall.repository.CategoryRepository;
import org.example.shoppingmall.repository.ProductRepository;
import org.example.shoppingmall.service.FileService;
import org.example.shoppingmall.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 用于检查字符串是否为空
import org.springframework.web.multipart.MultipartFile;
import org.example.shoppingmall.entity.User; // 确保导入 User 实体
import org.example.shoppingmall.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${app.image.storage-path:./uploads/product-images/}") // 图片存储物理路径，提供默认值
    private String imageStoragePath;

    @Value("${app.image.public-base-url:/product-images/}")    // 图片可访问的URL前缀，提供默认值
    private String publicBaseUrl;

    /**
     * 内部辅助方法：处理单个Base64图片字符串，解码、保存到本地文件系统，并返回可公开访问的URL。
     *
     * @param base64ImageString Base64编码的图片字符串 (可能带有data URI scheme)
     * @param fieldNameForLog   用于日志记录的字段名 (例如 "主图", "附图[0]")
     * @return 保存后的图片的公共访问URL，如果输入为空或处理失败则可能返回null或抛出异常
     * @throws IOException              如果文件读写发生错误
     * @throws IllegalArgumentException 如果Base64数据无效
     */
    private String saveBase64ImageAndGetUrl(String base64ImageString, String fieldNameForLog) {
        if (!StringUtils.hasText(base64ImageString)) {
            log.warn("字段 {} 的Base64图片数据为空。", fieldNameForLog);
            return null;
        }

        String base64Data;
        String extension;

        if (base64ImageString.startsWith("data:image/png;base64,")) {
            base64Data = base64ImageString.substring("data:image/png;base64,".length());
            extension = ".png";
        } else if (base64ImageString.startsWith("data:image/jpeg;base64,")) {
            base64Data = base64ImageString.substring("data:image/jpeg;base64,".length());
            extension = ".jpg";
        } else if (base64ImageString.startsWith("data:image/gif;base64,")) {
            base64Data = base64ImageString.substring("data:image/gif;base64,".length());
            extension = ".gif";
        } else if (base64ImageString.startsWith("data:image/webp;base64,")) {
            base64Data = base64ImageString.substring("data:image/webp;base64,".length());
            extension = ".webp";
        } else {
            log.warn("字段 {} 的Base64图片头部未知或缺失: '{}'，将尝试直接解码并使用默认扩展名 .jpg",
                    fieldNameForLog, base64ImageString.substring(0, Math.min(30, base64ImageString.length())));
            base64Data = base64ImageString;
            extension = ".jpg"; // 默认或根据实际情况考虑更安全的处理
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            File directory = new File(imageStoragePath);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    log.error("无法创建图片存储目录: {}", imageStoragePath);
                    // 对于关键操作，应该抛出更具体的受检异常或运行时异常
                    throw new ApiException(500, "无法创建图片存储目录: " + imageStoragePath);
                }
            }

            Path filePath = Paths.get(imageStoragePath, uniqueFileName);
            Files.write(filePath, imageBytes);
            log.info("图片 {} (Base64解码后) 已保存至: {}", fieldNameForLog, filePath.toString());

            String resolvedPublicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
            return resolvedPublicBaseUrl + uniqueFileName;

        } catch (IllegalArgumentException e) {
            log.error("字段 {} 的Base64解码失败: {}", fieldNameForLog, e.getMessage(), e);
            throw new ApiException(400, "字段 " + fieldNameForLog + " 的Base64数据无效: " + e.getMessage());
        } catch (IOException e) {
            log.error("字段 {} 的文件保存失败: {}", fieldNameForLog, e.getMessage(), e);
            throw new ApiException(500, "字段 " + fieldNameForLog + " 的文件保存操作失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "products", key = "{#keyword, #categoryId, #pageNum, #pageSize, #sort, #order, #minPrice, #maxPrice, #type}")
    public PageResult<ProductDTO> getProducts(String keyword, Integer categoryId, int pageNum, int pageSize,
                                              String sort, String order, BigDecimal minPrice, BigDecimal maxPrice, String type) {
        log.info("获取商品列表参数: keyword='{}', categoryId={}, pageNum={}, pageSize={}, sort='{}', order='{}', minPrice={}, maxPrice={}, type='{}'",
                keyword, categoryId, pageNum, pageSize, sort, order, minPrice, maxPrice, type);

        Sort sortObj = createSort(sort, order);
        Pageable pageable = PageRequest.of(pageNum > 0 ? pageNum - 1 : 0, pageSize, sortObj);
        Page<Product> productPage;

        if ("featured".equalsIgnoreCase(type)) {
            log.debug("执行获取 'featured' 类型商品的查询");
            productPage = productRepository.findFeaturedProductsWithFilters(
                    (StringUtils.hasText(keyword)) ? keyword.trim() : null,
                    categoryId, minPrice, maxPrice, pageable);
        } else {
            boolean hasFilters = StringUtils.hasText(keyword) || categoryId != null || minPrice != null ||
                    (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0);
            if (hasFilters) {
                log.debug("执行高级搜索，条件: keyword='{}', categoryId={}, minPrice={}, maxPrice={}", keyword, categoryId, minPrice, maxPrice);
                productPage = productRepository.advancedSearch(
                        (StringUtils.hasText(keyword)) ? keyword.trim() : null,
                        categoryId, minPrice, maxPrice, pageable);
            } else {
                log.debug("执行无条件基础查询 (仅按状态和分页排序)");
                productPage = productRepository.findByStatus(1, pageable); // 假设1代表上架
            }
        }
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return PageResult.of(productDTOs, productPage.getTotalElements(), pageNum, pageSize);
    }

    @Override
    @Cacheable(value = "products", key = "'product_' + #id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.PRODUCT_NOT_EXISTS));
        return convertToDTO(product);
    }

    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.PRODUCT_NOT_EXISTS));
    }


    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> otherImageFiles, Long sellerId) {
        log.info("开始创建商品。DTO: {}, Seller ID: {}", productDTO, sellerId);

        Product product = new Product();

        // --- 1. 设置从 DTO 直接映射的属性 ---
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setOriginalPrice(productDTO.getOriginalPrice());
        product.setStock(productDTO.getStock());
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : 1); // 默认上架
        product.setDetail(productDTO.getDetail());
        product.setIsFeatured(productDTO.getIsFeatured() != null ? productDTO.getIsFeatured() : false);
        product.setSales(0); // 新商品销量通常为0

        // --- 2. 处理文件上传并设置图片URL ---
        // 主图 (假设主图是必需的)
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            try {
                String mainImageUrl = fileService.uploadFile(mainImageFile, "product"); // "product"是子目录
                product.setImageUrl(mainImageUrl);
                log.info("主图上传成功: {}", mainImageUrl);
            } catch (Exception e) {
                log.error("主图上传失败: {}", e.getMessage(), e);
                throw new ApiException(ResultCode.UPLOAD_FAILED, "主图上传失败: " + e.getMessage());
            }
        } else if (StringUtils.hasText(productDTO.getImageUrl())) { // 如果DTO中直接提供了imageUrl (例如编辑时未更改图片)
            product.setImageUrl(productDTO.getImageUrl());
            log.info("使用DTO中提供的imageUrl: {}", productDTO.getImageUrl());
        }
        else {
            log.error("创建商品时未提供主图，且DTO中imageUrl为空。");
            throw new ApiException(ResultCode.VALIDATION_ERROR, "必须提供商品主图。");
        }

        // 附图列表
        List<String> uploadedOtherImageUrls = new ArrayList<>();
        if (otherImageFiles != null && !otherImageFiles.isEmpty()) {
            for (MultipartFile file : otherImageFiles) {
                if (file != null && !file.isEmpty()) {
                    try {
                        uploadedOtherImageUrls.add(fileService.uploadFile(file, "product"));
                    } catch (Exception e) {
                        log.warn("一个附图上传失败: {}", e.getMessage()); // 可以选择记录警告并继续，或抛异常
                    }
                }
            }
        }
        // 如果DTO中也包含已有的附图URL列表 (例如编辑时)，需要合并或替换逻辑
        if (productDTO.getImages() != null) {
            // 这里需要根据您的业务逻辑决定是追加、替换还是智能合并
            // 简单示例：如果DTO中有images，就用DTO的，否则用新上传的
            if (!productDTO.getImages().isEmpty() && uploadedOtherImageUrls.isEmpty()){
                try {
                    product.setImages(objectMapper.writeValueAsString(productDTO.getImages()));
                } catch (JsonProcessingException e) { /* ... */ }
            } else if (!uploadedOtherImageUrls.isEmpty()) {
                try {
                    product.setImages(objectMapper.writeValueAsString(uploadedOtherImageUrls));
                } catch (JsonProcessingException e) { /* ... */ }
            } else {
                try {
                    product.setImages(objectMapper.writeValueAsString(new ArrayList<String>())); // 存空数组JSON "[]"
                } catch (JsonProcessingException e) { /* ... */ }
            }
        } else if (!uploadedOtherImageUrls.isEmpty()) {
            try {
                product.setImages(objectMapper.writeValueAsString(uploadedOtherImageUrls));
            } catch (JsonProcessingException e) { /* ... */ }
        } else {
            try {
                product.setImages(objectMapper.writeValueAsString(new ArrayList<String>())); // 存空数组JSON "[]"
            } catch (JsonProcessingException e) { /* ... */ }
        }


        // --- 3. 设置关联对象 (Category, Seller, Brand 等) ---
        // Category (必需)
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ApiException(ResultCode.CATEGORY_NOT_EXISTS, "分类ID: " + productDTO.getCategoryId() + " 不存在"));
            product.setCategory(category);
        } else {
            throw new ApiException(ResultCode.VALIDATION_ERROR, "商品分类不能为空。");
        }

        // Seller (必需，根据您的实体和之前的讨论)
        // 假设您的 Product 实体中关联卖家的字段是 `private User seller;`
        // 并且您已经注入了 `UserRepository userRepository`
        if (sellerId != null) {
            User sellerUser = userRepository.findById(sellerId) // 假设有 userRepository
                    .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_EXISTS, "卖家用户ID: " + sellerId + " 不存在"));
            product.setSeller(sellerUser); // ★★★ 使用这个设置关联的User实体 ★★★
        } else {
            throw new ApiException(ResultCode.VALIDATION_ERROR, "卖家ID不能为空。");
        }
        // 如果您的Product实体中是 product.setSellerid(Long sellerId)，那么您之前那行 product.setSellerid(sellerId); 是对的
        // 但通常推荐通过关联对象来设置，JPA会处理外键。请确认您Product实体的seller字段。

        // Brand (可选，如果您的Product实体中有Brand关联，并且DTO中有brandId)
        // if (productDTO.getBrandId() != null) {
        //     Brand brand = brandRepository.findById(productDTO.getBrandId())
        //             .orElseThrow(() -> new ApiException(ResultCode.BRAND_NOT_EXISTS, "品牌ID: " + productDTO.getBrandId() + " 不存在"));
        //     product.setBrand(brand);
        // }


        // --- 4. 序列化 List<String> params 和 specs 到 JSON 字符串 ---
        try {
            if (productDTO.getParams() != null && !productDTO.getParams().isEmpty()) {
                product.setParams(objectMapper.writeValueAsString(productDTO.getParams()));
            } else {
                product.setParams("[]"); // 或者 objectMapper.writeValueAsString(new ArrayList<String>())
            }
            if (productDTO.getSpecs() != null && !productDTO.getSpecs().isEmpty()) {
                product.setSpecs(objectMapper.writeValueAsString(productDTO.getSpecs()));
            } else {
                product.setSpecs("[]"); // 或者 objectMapper.writeValueAsString(new ArrayList<String>())
            }
        } catch (JsonProcessingException e) {
            log.error("序列化商品参数或规格为JSON时失败: {}", e.getMessage(), e);
            throw new ApiException(500, "处理商品参数/规格时发生内部错误");
        }

        // @CreationTimestamp 和 @UpdateTimestamp 会自动设置 createTime 和 updateTime

        log.info("准备保存到数据库的Product实体: {}", product);
        Product savedProduct;
        try {
            savedProduct = productRepository.save(product);
            log.info("商品已成功保存到数据库, ID: {}", savedProduct.getId());
        } catch (DataIntegrityViolationException dive) {
            log.error("保存商品到数据库时发生数据完整性约束异常: {}", dive.getMostSpecificCause().getMessage(), dive);
            throw new ApiException(ResultCode.VALIDATION_ERROR, "保存商品失败，数据不符合要求: " + dive.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("保存商品到数据库时发生未知错误: {}", e.getMessage(), e);
            throw new ApiException(ResultCode.ERROR, "保存商品时发生未知错误，请联系管理员。");
        }

        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.PRODUCT_NOT_EXISTS));

        // 仅复制非图片、非分类、非列表的简单属性
        // 注意：如果有些字段不允许在更新时修改，需要在此处或DTO中处理
        BeanUtils.copyProperties(productDTO, product, "id", "categoryId", "categoryName", "imageUrl", "images", "params", "specs", "createTime", "sales");

        if (productDTO.getCategoryId() != null && !productDTO.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ApiException(ResultCode.CATEGORY_NOT_EXISTS));
            product.setCategory(category);
        }

        // --- 处理主图更新 (如果DTO中imageUrl不为空，则认为是新的Base64) ---
        if (StringUtils.hasText(productDTO.getImageUrl())) {
            // 注意：这里可能需要先删除旧图片（如果存在且存储在本地）
            // String oldImageUrl = product.getImageUrl();
            // if (StringUtils.hasText(oldImageUrl)) { fileService.deleteFile(oldImageUrl); /* 假设FileService有此方法 */ }
            String actualMainImageUrl = saveBase64ImageAndGetUrl(productDTO.getImageUrl(), "更新主图(imageUrl)");
            product.setImageUrl(actualMainImageUrl);
        }

        // --- 处理附图列表更新 (如果DTO中images不为空，则认为是新的Base64列表) ---
        if (productDTO.getImages() != null) { // 允许传入空列表来清空图片
            // 注意：这里可能需要先删除所有旧的附图
            // List<String> oldImageUrls = objectMapper.readValue(product.getImages(), new TypeReference<List<String>>() {});
            // if (oldImageUrls != null) { for (String url : oldImageUrls) { fileService.deleteFile(url); } }

            List<String> actualImageUrls = new ArrayList<>();
            if (!productDTO.getImages().isEmpty()) {
                for (int i = 0; i < productDTO.getImages().size(); i++) {
                    String base64Image = productDTO.getImages().get(i);
                    if (StringUtils.hasText(base64Image)) {
                        // 检查这个base64Image是否是一个已存在的URL，如果是，则保留，否则认为是新的Base64数据
                        if (base64Image.startsWith(publicBaseUrl) || base64Image.startsWith("http")) { // 简单判断是否为URL
                            actualImageUrls.add(base64Image);
                        } else {
                            String actualUrl = saveBase64ImageAndGetUrl(base64Image, "更新附图列表[" + i + "]");
                            if (actualUrl != null) {
                                actualImageUrls.add(actualUrl);
                            }
                        }
                    }
                }
            }
            try {
                product.setImages(objectMapper.writeValueAsString(actualImageUrls));
            } catch (JsonProcessingException e) {
                log.error("更新商品时序列化处理后的附图URL列表为JSON时出错: {}", e.getMessage(), e);
                throw new ApiException(500, "更新商品时处理附图URL列表时发生错误");
            }
        }


        // 处理 params 和 specs (与创建时类似)
        try {
            if (productDTO.getParams() != null) {
                product.setParams(objectMapper.writeValueAsString(productDTO.getParams()));
            }
            if (productDTO.getSpecs() != null) {
                product.setSpecs(objectMapper.writeValueAsString(productDTO.getSpecs()));
            }
        } catch (JsonProcessingException e) {
            log.error("更新商品时JSON处理异常 (params/specs): {}", e.getMessage(), e);
            throw new ApiException(400, "商品参数或规格JSON处理异常");
        }

        // isFeatured 更新
        if (productDTO.getIsFeatured() != null) {
            product.setIsFeatured(productDTO.getIsFeatured());
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.PRODUCT_NOT_EXISTS));
        // 在删除产品前，可能需要删除其关联的图片文件
        // if (StringUtils.hasText(product.getImageUrl())) { fileService.deleteFileByUrl(product.getImageUrl(), publicBaseUrl, imageStoragePath); }
        // if (StringUtils.hasText(product.getImages())) {
        //     try {
        //         List<String> imageUrls = objectMapper.readValue(product.getImages(), new TypeReference<List<String>>() {});
        //         for (String url : imageUrls) { fileService.deleteFileByUrl(url, publicBaseUrl, imageStoragePath); }
        //     } catch (JsonProcessingException e) { log.error("删除产品附图时解析JSON失败", e); }
        // }
        productRepository.deleteById(id);
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        // 此方法使用FileService，这是处理MultipartFile的推荐方式
        // 确保FileService的uploadFile方法返回的是可公开访问的URL
        return fileService.uploadFile(file, "product"); // "product" 可能是子目录提示
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        // 先复制所有属性，然后对特殊字段（如JSON字符串）进行处理
        BeanUtils.copyProperties(product, dto, "images", "params", "specs");

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        // 确保DTO中有isFeatured字段，或者手动设置
        if(product.getIsFeatured() != null) { // Product中的isFeatured可能是Boolean包装类型
            dto.setIsFeatured(product.getIsFeatured());
        } else {
            dto.setIsFeatured(false); // 或者根据业务逻辑设默认值
        }


        try {
            if (StringUtils.hasText(product.getImages())) {
                dto.setImages(objectMapper.readValue(product.getImages(), new TypeReference<List<String>>() {}));
            } else {
                dto.setImages(new ArrayList<>());
            }
            if (StringUtils.hasText(product.getParams())) {
                dto.setParams(objectMapper.readValue(product.getParams(), new TypeReference<List<String>>() {}));
            } else {
                dto.setParams(new ArrayList<>());
            }
            if (StringUtils.hasText(product.getSpecs())) {
                dto.setSpecs(objectMapper.readValue(product.getSpecs(), new TypeReference<List<String>>() {}));
            } else {
                dto.setSpecs(new ArrayList<>());
            }
        } catch (JsonProcessingException e) {
            log.error("DTO转换时JSON反序列化异常, product ID: {}: {}", product.getId() != null ? product.getId() : "N/A", e.getMessage(), e);
            dto.setImages(new ArrayList<>());
            dto.setParams(new ArrayList<>());
            dto.setSpecs(new ArrayList<>());
        }
        return dto;
    }

    private Sort createSort(String sort, String order) {
        if (!StringUtils.hasText(sort) || "default".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "createTime"); // 确保Product实体有createTime字段
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 定义合法的排序字段，防止SQL注入或错误
        List<String> validSortFields = List.of("price", "sales", "createtime"); // 注意字段名与实体一致
        String lowerCaseSort = sort.toLowerCase();

        if (validSortFields.contains(lowerCaseSort)) {
            // 特别处理驼峰命名到下划线的转换，如果数据库列名是下划线风格
            // String dbColumn = convertCamelToSnake(lowerCaseSort); // 你可能需要这样一个工具方法
            // return Sort.by(direction, dbColumn);
            return Sort.by(direction, sort); // 如果JPA能自动处理或实体字段名与排序参数一致
        }
        return Sort.by(Sort.Direction.DESC, "createTime");
    }
}