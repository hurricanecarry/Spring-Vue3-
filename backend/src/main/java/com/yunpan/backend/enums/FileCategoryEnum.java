package com.yunpan.backend.enums;

import java.util.Set;

/**
 * 文件分类枚举
 * 对应 file_info.file_category 字段
 */
public enum FileCategoryEnum {

    VIDEO(1, "视频", Set.of(
        "mp4", "avi", "mkv", "mov", "wmv", "flv",
        "webm", "rmvb", "m4v", "ts", "3gp", "mpeg", "mpg"
    )),

    AUDIO(2, "音频", Set.of(
        "mp3", "wav", "flac", "aac", "ogg", "wma",
        "m4a", "ape", "opus", "amr", "mid"
    )),

    DOC(3, "文档", Set.of(
        "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx",
        "txt", "csv", "md", "html", "htm", "xml", "json",
        "wps", "et", "dps", "pages", "numbers", "keynote"
    )),

    ARCHIVE(4, "压缩包", Set.of(
        "zip", "rar", "7z", "tar", "gz", "bz2",
        "xz", "iso", "cab", "tgz", "tbz2", "zst", "lz4"
    )),

    OTHER(5, "其他", Set.of()),

    IMAGE(6, "图片", Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "svg",
        "webp", "ico", "tiff", "tif", "heic", "heif", "raw"
    ));

    private final int code;
    private final String label;
    private final Set<String> extensions;

    FileCategoryEnum(int code, String label, Set<String> extensions) {
        this.code = code;
        this.label = label;
        this.extensions = extensions;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
    public Set<String> getExtensions() { return extensions; }

    /**
     * 根据文件扩展名匹配分类，未匹配返回 OTHER
     */
    public static FileCategoryEnum fromExtension(String ext) {
        if (ext == null || ext.isEmpty()) return OTHER;
        // 去掉开头的点
        String key = ext.startsWith(".") ? ext.substring(1).toLowerCase() : ext.toLowerCase();
        for (FileCategoryEnum cat : values()) {
            if (cat.extensions.contains(key)) {
                return cat;
            }
        }
        return OTHER;
    }

    /**
     * 根据 code 获取枚举
     */
    public static FileCategoryEnum fromCode(int code) {
        for (FileCategoryEnum cat : values()) {
            if (cat.code == code) return cat;
        }
        return OTHER;
    }
}
