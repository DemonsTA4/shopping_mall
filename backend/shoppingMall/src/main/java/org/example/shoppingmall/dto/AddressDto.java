package org.example.shoppingmall.dto; // 请将包名替换为您的实际项目路径

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank; // 用于校验
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern; // 可用于电话号码格式校验

import java.time.LocalDateTime;

/**
 * 地址数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private Long id; // 地址ID (在创建时通常为空，由后端生成)

    // userId 通常在创建/更新时从路径参数或认证信息获取，
    // 但在返回DTO列表时可能需要，这里暂时不包含，因为Service方法签名中userId是独立参数
    // private Long userId;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(min = 2, max = 50, message = "收货人姓名长度必须在2到50个字符之间")
    private String receiverName; // 收货人姓名

    @NotBlank(message = "联系电话不能为空")
    @Size(min = 7, max = 20, message = "联系电话长度不正确")
    // 简单的电话号码格式校验示例，您可以根据需要调整或使用更专业的库如libphonenumber
    // @Pattern(regexp = "^(\\+?[0-9\\s\\-()]{7,20})$", message = "电话号码格式不正确")
    private String receiverPhone; // 收货人联系电话

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份名称过长")
    private String province; // 省份

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市名称过长")
    private String city; // 城市

    @NotBlank(message = "区/县不能为空")
    @Size(max = 50, message = "区/县名称过长")
    private String district; // 区/县

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址过长")
    private String detailedAddress; // 详细街道地址

    @Size(max = 10, message = "邮政编码过长")
    private String postalCode; // 邮政编码 (可选)

    private Boolean isDefault = false; // 是否为默认地址，默认为false

    @Size(max = 20, message = "地址标签过长")
    private String tag; // 地址标签 (例如: 家, 公司) (可选)

    // 以下字段通常在查询结果中由后端填充，创建/更新时不需要客户端提供
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 最后更新时间

    // 示例构造函数 (用于创建新地址时，不包含id和时间戳)
    public AddressDto(String receiverName, String receiverPhone, String province, String city, String district, String detailedAddress, String postalCode, Boolean isDefault, String tag) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailedAddress = detailedAddress;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
        this.tag = tag;
    }
}
