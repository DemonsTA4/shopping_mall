package org.example.shoppingmall.validation.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.beanutils.BeanUtils;
import org.example.shoppingmall.validation.ValidPhoneNumberWithCountry;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class ValidPhoneNumberWithCountryValidator
        implements ConstraintValidator<ValidPhoneNumberWithCountry, Object> {

    // private static final Logger logger = LoggerFactory.getLogger(ValidPhoneNumberWithCountryValidator.class);
    private String phoneFieldName;
    private String countryCodeFieldName;
    private String defaultMessageTemplate;
    private boolean allowNullFieldsFlag; // 用于存储从注解获取的 allowNullFields 值

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public void initialize(ValidPhoneNumberWithCountry constraintAnnotation) {
        this.phoneFieldName = constraintAnnotation.phoneField();
        this.countryCodeFieldName = constraintAnnotation.countryCodeField();
        this.defaultMessageTemplate = constraintAnnotation.message();
        this.allowNullFieldsFlag = constraintAnnotation.allowNullFields(); // 获取 allowNullFields 的值
    }

    @Override
    public boolean isValid(Object dtoInstance, ConstraintValidatorContext context) {
        if (dtoInstance == null) {
            return true;
        }

        String phoneNumberValue;
        String countryCodeValue;

        try {
            phoneNumberValue = BeanUtils.getProperty(dtoInstance, phoneFieldName);
            countryCodeValue = BeanUtils.getProperty(dtoInstance, countryCodeFieldName);
        } catch (Exception e) {
            // logger.error("Error accessing DTO properties for validation", e);
            // 构建错误信息，通常不应该依赖默认模板，因为这是内部错误
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Internal error while accessing DTO properties for validation.")
                    .addConstraintViolation();
            return false;
        }

        boolean isPhoneProvided = phoneNumberValue != null && !phoneNumberValue.isBlank();
        boolean isCountryCodeProvided = countryCodeValue != null && !countryCodeValue.isBlank();

        // 如果 allowNullFieldsFlag 为 true，并且两个关键字段都为空，则跳过此校验，认为是有效的
        if (this.allowNullFieldsFlag && !isPhoneProvided && !isCountryCodeProvided) {
            return true;
        }

        // 如果只提供了一个字段 (phone 或 countryCode)，而 allowNullFieldsFlag 为 false (或即使为 true，也可能希望它们同时出现)
        // 这部分逻辑需要根据你的业务需求来定。
        // 对于 UserProfileUpdateDto，用户可能只想更新电话，或只想更新国家代码，或者两者都更新。
        // 或者要求如果更新电话，必须 همراه با 国家代码。

        // 场景1: 如果 allowNullFields 为 true，并且至少一个字段提供了值，但另一个关键字段缺失，如何处理？
        // 这是一个业务逻辑问题，可能需要更复杂的判断。
        // 对于更新，如果用户只提供了phone，我们可能需要使用已存在的countryCode，反之亦然。
        // 但这个校验器是通用的，它只知道DTO当前的快照。

        // 简化逻辑：如果任一关键字段为空，而我们期望它们一起被校验（除非 allowNullFields 且两者都为空）
        if (!isPhoneProvided || !isCountryCodeProvided) {
            // 如果 allowFields 为 true 且上面已经处理了两者都为空的情况，
            // 那么到这里说明至少一个不为空，但另一个为空。
            // 这种情况下是否算校验失败取决于你的业务。
            // 对于 UserProfileUpdateDto，如果用户只想更新非电话/国家代码的字段，
            // 且 phone 和 countryCode 都是 null，allowNullFields=true 应该已经让它通过了。
            // 如果 phone 是 " " 而 countryCode 是 null，allowNullFields=true 不会通过上面的判断。
            // 所以，这里我们需要一个明确的规则。

            // 规则调整：如果 allowNullFields 为 false，则 phone 和 countryCode 必须同时存在才能进行 libphonenumber 校验。
            // 如果 allowNullFields 为 true，则只有当 phone 和 countryCode 都提供了值时，才进行 libphonenumber 校验。
            // 其他情况（例如只提供一个，或一个为空字符串）可以由字段级别的 @NotBlank, @Size 处理。
            // 或者，我们在这里强制，如果其中一个被提供了，另一个也必须被提供才能进行联合校验。

            if (!isPhoneProvided && isCountryCodeProvided) {
                // 提供了国家代码但没提供电话号码
                if (!this.allowNullFieldsFlag) { // 如果不允许字段为空，且我们在这里做联合校验
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Phone number is required when country code is provided for validation.")
                            .addPropertyNode(phoneFieldName).addConstraintViolation();
                    return false;
                }
                return true; // 允许只更新国家代码而不更新电话号码（电话号码格式校验不在此执行）
            }
            if (isPhoneProvided && !isCountryCodeProvided) {
                // 提供了电话号码但没提供国家代码
                // 尝试按E.164解析
                try {
                    PhoneNumber internationalNumber = phoneNumberUtil.parse(phoneNumberValue, null);
                    if (phoneNumberUtil.isValidNumber(internationalNumber)) {
                        return true;
                    }
                } catch (NumberParseException e) {
                    // ignore, will be handled below if not E.164
                }
                if (!this.allowNullFieldsFlag) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Country code is required to validate the provided phone number.")
                            .addPropertyNode(countryCodeFieldName).addConstraintViolation();
                    return false;
                }
                return true; // 允许只更新电话号码而不更新国家代码（但格式可能无法精确校验）
            }
            // 如果两个都未提供，并且 allowNullFields 为 false
            if (!isPhoneProvided && !isCountryCodeProvided && !this.allowNullFieldsFlag){
                // 实际上这种情况不应该发生，因为 @NotBlank 会先处理单个字段
                // 但作为类级别校验，如果它们都为空且不允许，可以标记错误
                return true; // 或者根据业务返回false并提供消息
            }
            // 如果到这里，说明两个字段都未提供，且 allowNullFields 为 true，已经处理过了
            // 或者一个提供了，另一个没提供，且 allowNullFields 为 true，我们也决定跳过联合校验
            return true;
        }


        // --- 执行 libphonenumber 校验 ---
        // 只有当 phone 和 countryCode 都提供了值时才进行
        try {
            PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumberValue, countryCodeValue.toUpperCase());
            boolean isValid = phoneNumberUtil.isValidNumber(numberProto);
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(this.defaultMessageTemplate)
                        .addPropertyNode(phoneFieldName)
                        .addConstraintViolation();
            }
            return isValid;
        } catch (NumberParseException e) {
            // logger.info("Failed to parse phone number '{}' for country '{}'", phoneNumberValue, countryCodeValue, e);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The phone number format is not recognized for the country " + countryCodeValue.toUpperCase() + ".")
                    .addPropertyNode(phoneFieldName)
                    .addConstraintViolation();
            return false;
        }
    }
}