package org.example.shoppingmall.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.shoppingmall.validation.impl.ValidPhoneNumberWithCountryValidator; // 导入实现类

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidPhoneNumberWithCountryValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE}) // 应用于类级别
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumberWithCountry {

    String message() default "无效的电话号码或国家代码组合";

    String phoneField(); // 用于指定DTO中电话号码字段的名称

    String countryCodeField(); // 用于指定DTO中国家代码字段的名称

    boolean allowNullFields() default false; // 默认情况下，如果字段存在，则参与校验；
    // 如果设为 true，则校验器需要相应逻辑来处理

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}