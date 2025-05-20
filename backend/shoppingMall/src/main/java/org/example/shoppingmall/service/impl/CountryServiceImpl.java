package org.example.shoppingmall.service.impl;

import org.example.shoppingmall.service.CountryService;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service // Spring注解，表示这是一个服务类
public class CountryServiceImpl implements CountryService {

    @Override
    public String getCountryName(String countryCode) {
        return getCountryName(countryCode, Locale.getDefault()); // 默认使用JVM的当前语言环境
    }

    @Override
    public String getCountryName(String countryCode, Locale targetLocale) {
        if (countryCode == null || countryCode.trim().length() != 2) {
            // 对于无效的代码，可以选择返回原始代码、null 或抛出异常
            // logger.warn("Invalid country code provided: {}", countryCode);
            return countryCode; // 或者 return null;
        }
        if (targetLocale == null) {
            targetLocale = Locale.getDefault();
        }

        try {
            // Locale 的第一个参数是 language code (可以为空字符串)，第二个是 country code
            Locale countrySpecificLocale = new Locale("", countryCode.trim().toUpperCase());
            String displayName = countrySpecificLocale.getDisplayCountry(targetLocale);

            // Locale.getDisplayCountry() 在找不到时可能会返回原始代码，需要检查一下
            // 如果返回的是两位代码本身，说明可能没找到对应的显示名称
            if (displayName.equalsIgnoreCase(countryCode.trim())) {
                // logger.warn("Display name for country code '{}' in locale '{}' resolved to the code itself. Assuming not found.", countryCode, targetLocale);
                return countryCode; // 或者返回一个更友好的 "Unknown Country" 等
            }
            return displayName;
        } catch (Exception e) { // 例如 MissingResourceException，虽然 Locale 通常不直接抛这个
            // logger.error("Error fetching country name for code: {} and locale: {}", countryCode, targetLocale, e);
            return countryCode; // 发生异常时返回原始代码或null
        }
    }

    // 你可以在这个类中添加一个 main 方法进行快速测试
    /*
    public static void main(String[] args) {
        CountryService countryService = new CountryServiceImpl();

        System.out.println("--- 使用默认 Locale ---");
        System.out.println("US: " + countryService.getCountryName("US"));
        System.out.println("CN: " + countryService.getCountryName("CN"));
        System.out.println("XX: " + countryService.getCountryName("XX")); // 无效代码

        System.out.println("\n--- 使用英文 Locale ---");
        System.out.println("US: " + countryService.getCountryName("US", Locale.ENGLISH)); // Expected: United States
        System.out.println("CN: " + countryService.getCountryName("CN", Locale.ENGLISH)); // Expected: China
        System.out.println("JP: " + countryService.getCountryName("JP", Locale.ENGLISH)); // Expected: Japan

        System.out.println("\n--- 使用简体中文 Locale ---");
        Locale simplifiedChinese = Locale.SIMPLIFIED_CHINESE; // new Locale("zh", "CN")
        System.out.println("US: " + countryService.getCountryName("US", simplifiedChinese)); // Expected: 美国
        System.out.println("CN: " + countryService.getCountryName("CN", simplifiedChinese)); // Expected: 中国
        System.out.println("JP: " + countryService.getCountryName("JP", simplifiedChinese)); // Expected: 日本

        System.out.println("\n--- 使用日文 Locale ---");
        System.out.println("US: " + countryService.getCountryName("US", Locale.JAPANESE)); // Expected: アメリカ合衆国
        System.out.println("CN: " + countryService.getCountryName("CN", Locale.JAPANESE)); // Expected: 中華人民共和国 or 中国
        System.out.println("JP: " + countryService.getCountryName("JP", Locale.JAPANESE)); // Expected: 日本
    }
     */
}