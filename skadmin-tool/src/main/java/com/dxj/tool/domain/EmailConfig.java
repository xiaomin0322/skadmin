package com.dxj.tool.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 邮件配置类，数据存覆盖存入数据存
 * @author dxj
 * @date 2019-05-26
 */
@Entity
@Data
@Table(name = "email_config")
public class EmailConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *邮件服务器SMTP地址
     */
    @NotBlank
    private String host;

    /**
     * 邮件服务器SMTP端口
     */
    @NotBlank
    private String port;

    /**
     * 发件者用户名，默认为发件人邮箱前缀
     */
    @NotBlank
    private String user;

    @NotBlank
    private String pass;

    /**
     * 收件人
     */
    @NotBlank
    @Column(name = "from_user")
    private String fromUser;
}
