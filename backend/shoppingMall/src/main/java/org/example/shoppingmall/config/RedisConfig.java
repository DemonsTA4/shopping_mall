package org.example.shoppingmall.config;

import com.fasterxml.jackson.databind.JavaType; // ★ 导入 JavaType ★
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory; // ★ 导入 TypeFactory ★
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.shoppingmall.dto.PageResult; // ★ 导入 PageResult ★
import org.example.shoppingmall.dto.ProductDTO; // ★ 导入 ProductDTO ★
import org.example.shoppingmall.dto.BannerDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer; // ★ 改回使用 Jackson2JsonRedisSerializer ★
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List; // ★ 导入 List ★
import java.util.Map;
import java.util.HashMap;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() { // 这个 ObjectMapper 用于通用目的，例如Spring MVC
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // ★★★ 确保这里没有 activateDefaultTyping ★★★
        return objectMapper;
    }

    /**
     * 创建专门用于 PageResult<ProductDTO> 类型的 Jackson2JsonRedisSerializer。
     * 通过构建精确的 JavaType，帮助 Jackson 正确反序列化泛型。
     *
     * @param objectMapper 上面定义的、配置好 JavaTimeModule 的 ObjectMapper
     * @return Jackson2JsonRedisSerializer<PageResult<ProductDTO>>
     */
    @Bean("pageResultProductDTORedisSerializer") // 给这个特殊的序列化器一个名字
    public Jackson2JsonRedisSerializer<PageResult<ProductDTO>> pageResultProductDTORedisSerializer(ObjectMapper objectMapper) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        // 1. 构建内部 List<ProductDTO> 的类型
        JavaType listProductDtoType = typeFactory.constructCollectionType(List.class, ProductDTO.class);
        // 2. 构建 PageResult<List<ProductDTO>> 的类型，但由于 PageResult<T> 中的 T 是 ProductDTO，
        //    而 PageResult 内部的 list 字段是 List<ProductDTO>，所以我们需要构建 PageResult<ProductDTO>
        //    Jackson2JsonRedisSerializer<T> 的构造函数需要一个 Class<T> 或 JavaType。
        //    对于 PageResult<ProductDTO>，直接使用 PageResult.class 可能不足以让Jackson知道内部List的元素类型。

        // ★★★ 更准确的方式是构建 PageResult<ProductDTO> 的完整 JavaType ★★★
        // 这是一个技巧，因为 PageResult<T> 的 'list' 字段是 List<T>
        // 我们真正需要的是 PageResult<ProductDTO> 这个类型
        JavaType pageResultType = typeFactory.constructParametricType(PageResult.class, ProductDTO.class);

        Jackson2JsonRedisSerializer<PageResult<ProductDTO>> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, pageResultType); // ★ 传入构建好的 JavaType ★

        return serializer;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) { // 注入通用的 ObjectMapper，如果需要存其他类型的对象

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 对于通用的 RedisTemplate，其 ValueSerializer 仍然可以使用 GenericJackson2JsonRedisSerializer
        // 这样它可以尝试处理多种类型。
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper); // 使用没有 activateDefaultTyping 的 objectMapper

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(genericJackson2JsonRedisSerializer); // ★ 通用场景 ★
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /*@Bean
    public RedisCacheConfiguration cacheConfiguration(
            Jackson2JsonRedisSerializer<PageResult<ProductDTO>> pageResultProductDTORedisSerializer) { // ★ 注入特定类型的序列化器 ★
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                // ★★★ 关键：为值序列化使用我们专门为 PageResult<ProductDTO> 创建的序列化器 ★★★
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pageResultProductDTORedisSerializer));
    }*/

    @Bean("listBannerDtoRedisSerializer") // 给这个序列化器一个名字
    public Jackson2JsonRedisSerializer<List<BannerDto>> listBannerDtoRedisSerializer(ObjectMapper objectMapper) {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType listBannerDtoType = typeFactory.constructCollectionType(List.class, BannerDto.class);
        return new Jackson2JsonRedisSerializer<>(objectMapper, listBannerDtoType);
    }

    // 如果您有其他类型的对象也需要缓存，并且它们也遇到了泛型反序列化问题，
    // 您可能需要为它们也创建类似的特定类型的 Jackson2JsonRedisSerializer bean，
    // 然后在 RedisCacheManager 中为不同的缓存名称 (cache names) 配置不同的 RedisCacheConfiguration。
    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration(ObjectMapper objectMapper) { // 修改参数
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 对于默认情况，可以考虑使用 GenericJackson2JsonRedisSerializer
        // 但要注意，如果没有 activateDefaultTyping，它对于复杂泛型的反序列化可能不够健壮
        // 或者，如果您的大多数缓存都是 PageResult<ProductDTO>，可以保留之前的做法，但要确保其他缓存有自己的配置
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);


        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                // 这里使用一个更通用的序列化器作为默认，或者您也可以不设置，让 CacheManager 的配置覆盖
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer));
    }


    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            RedisCacheConfiguration defaultCacheConfiguration, // 注入基础默认配置
            @Qualifier("pageResultProductDTORedisSerializer") Jackson2JsonRedisSerializer<PageResult<ProductDTO>> pageResultProductDTORedisSerializer,
            @Qualifier("listBannerDtoRedisSerializer") Jackson2JsonRedisSerializer<List<BannerDto>> listBannerDtoRedisSerializer,
            ObjectMapper objectMapper) { // 可能需要 ObjectMapper 来创建通用的序列化器，或者直接使用 defaultCacheConfiguration 的组件

        // 获取 defaultCacheConfiguration 中的通用设置
        // 注意：您需要检查您当前版本的 RedisCacheConfiguration 提供了哪些 getter 方法
        // 以下是一些常见的 getter 方法，您的版本可能略有不同或不全
        Duration defaultTtl = defaultCacheConfiguration.getTtl();
        RedisSerializationContext.SerializationPair<String> keySerializationPair = defaultCacheConfiguration.getKeySerializationPair();
        // RedisSerializationContext.SerializationPair<Object> valueSerializationPairForDefault = defaultCacheConfiguration.getValueSerializationPair(); // 这是默认的值序列化器
        boolean allowNullValues = defaultCacheConfiguration.getAllowCacheNullValues();
        // Function<String, String> keyPrefixFunction = defaultCacheConfiguration.getKeyPrefixFunction(); // 如果您使用了前缀
        // ConversionService conversionService = defaultCacheConfiguration.getConversionService(); // 如果您使用了 ConversionService


        // 为 bannerCache 创建特定的配置
        RedisCacheConfiguration bannerCacheConfig = RedisCacheConfiguration.defaultCacheConfig() // 从头构建
                .entryTtl(defaultTtl) // 应用默认的 TTL
                .serializeKeysWith(keySerializationPair); // 应用默认的 Key 序列化

        if (!allowNullValues) { // 应用是否允许null值的设置
            bannerCacheConfig = bannerCacheConfig.disableCachingNullValues();
        }
        // 如果有其他通用设置，例如 keyPrefix, conversionService 等，也在这里应用
        // bannerCacheConfig = bannerCacheConfig.computePrefixWith(keyPrefixFunction);
        // bannerCacheConfig = bannerCacheConfig.withConversionService(conversionService);

        // 最后设置特定的值序列化器
        bannerCacheConfig = bannerCacheConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(listBannerDtoRedisSerializer));


        // 为 productsCache 创建特定的配置
        RedisCacheConfiguration productsCacheConfig = RedisCacheConfiguration.defaultCacheConfig() // 从头构建
                .entryTtl(defaultTtl)
                .serializeKeysWith(keySerializationPair);

        if (!allowNullValues) {
            productsCacheConfig = productsCacheConfig.disableCachingNullValues();
        }
        // ... 应用其他通用设置 ...

        // 最后设置特定的值序列化器
        productsCacheConfig = productsCacheConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pageResultProductDTORedisSerializer));


        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("bannerCache", bannerCacheConfig);
        cacheConfigurations.put("products", productsCacheConfig); // ★ 修改这里为 "products" ★

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfiguration) // 这里的 defaultCacheConfiguration 仍然是你定义的那个 Bean
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}