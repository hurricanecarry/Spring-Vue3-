package com.yunpan.backend;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;

public class fastgengerate {
    public static void main(String[] args) {
        FastAutoGenerator
                .create("jdbc:mysql://localhost:3306/test", "root", "@Daisy8888")
                .globalConfig(builder -> builder
                        .author("hurricanecarry")         // @author 注释
                        .outputDir("D:/Vscode_work1/Vue/full/backend/src/main/java")
                )
                .packageConfig(builder -> builder
                        .parent("com.yunpan.backend")  // 包名
                )
                .strategyConfig(builder -> builder
                         .addInclude("user_info", "file_info", "share_info")  // 只生成这三张表,默认是所有表

                        .addTablePrefix("")        // 表名前缀过滤
                                //比如你的表叫 tb_user_info，设 .addTablePrefix("tb_") → 生成的实体类叫 UserInfo，而不是 TbUserInfo。

                        .entityBuilder()
                        .enableLombok()        // 生成的实体带 @Data
                        .enableTableFieldAnnotation()  // 带 @TableField
                          //不加这个，MyBatis-Plus 默认驼峰转下划线（userName → user_name），一样能正确映射。所以不开也没问题，开了就是-------显式写出对应的列名
                )
                .execute();
    }
}
