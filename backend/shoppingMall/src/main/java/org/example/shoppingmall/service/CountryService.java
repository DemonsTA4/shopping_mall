package org.example.shoppingmall.service;

import java.util.Locale;

/**
 * 提供国家相关信息的服务接口。
 */
public interface CountryService {

    /**
     * 根据 ISO 3166-1 alpha-2 国家代码获取国家全名。
     * 使用系统默认的语言环境返回国家名称。
     *
     * @param countryCode 两位的国家代码 (例如 "US", "CN", "JP")，不区分大小写。
     * @return 国家全名；如果代码无效或找不到，则返回原始代码或null。
     */
    String getCountryName(String countryCode);

    /**
     * 根据 ISO 3166-1 alpha-2 国家代码和指定的语言环境获取国家全名。
     *
     * @param countryCode 两位的国家代码 (例如 "US", "CN", "JP")，不区分大小写。
     * @param targetLocale 指定的语言环境 (例如 Locale.ENGLISH, Locale.JAPANESE)。
     * @return 指定语言的国家全名；如果代码无效或找不到，则返回原始代码或null。
     */
    String getCountryName(String countryCode, Locale targetLocale);
}