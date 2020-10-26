package cn.lzl.fckexportword;

/**
 * @Author: Dream
 * 2018/4/7
 *
 * 富文本实体
 */
public class FckDTO {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FckDTO(String text) {
        this.text = text;
    }

    public FckDTO() {
    }
}
