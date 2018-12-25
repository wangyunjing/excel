package test;

public class Introduction {

    private Integer width;

    private Integer height;

    private String remark;

    public Introduction() {
    }

    public Introduction(Integer width, Integer height, String remark) {
        this.width = width;
        this.height = height;
        this.remark = remark;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Introduction{" +
                "width=" + width +
                ", height=" + height +
                ", remark='" + remark + '\'' +
                '}';
    }
}
