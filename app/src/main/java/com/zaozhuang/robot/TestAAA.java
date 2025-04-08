// TestAAA.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.zaozhuang.robot;
import java.util.List;
public class TestAAA {
    private String msg;
    private long total;
    private long code;
    private long pageSize;
    private List<Row> rows;
    private long pageNum;

    public String getMsg() { return msg; }
    public void setMsg(String value) { this.msg = value; }

    public long getTotal() { return total; }
    public void setTotal(long value) { this.total = value; }

    public long getCode() { return code; }
    public void setCode(long value) { this.code = value; }

    public long getPageSize() { return pageSize; }
    public void setPageSize(long value) { this.pageSize = value; }

    public List<Row> getRows() { return rows; }
    public void setRows(List<Row> value) { this.rows = value; }

    public long getPageNum() { return pageNum; }
    public void setPageNum(long value) { this.pageNum = value; }
}

// Row.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation



class Row {
    private long tabId;
    private String imageLink;
    private String imageName;
    private String createTime;
    private String imageUrl;
    private String imageServiceName;
    private long orderNum;
    private long id;
    private long imageStatus;
    private long imageType;
    private long imageOption;

    public long getTabId() { return tabId; }
    public void setTabId(long value) { this.tabId = value; }

    public String getImageLink() { return imageLink; }
    public void setImageLink(String value) { this.imageLink = value; }

    public String getImageName() { return imageName; }
    public void setImageName(String value) { this.imageName = value; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { this.createTime = value; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String value) { this.imageUrl = value; }

    public String getImageServiceName() { return imageServiceName; }
    public void setImageServiceName(String value) { this.imageServiceName = value; }

    public long getOrderNum() { return orderNum; }
    public void setOrderNum(long value) { this.orderNum = value; }

    public long getid() { return id; }
    public void setid(long value) { this.id = value; }

    public long getImageStatus() { return imageStatus; }
    public void setImageStatus(long value) { this.imageStatus = value; }

    public long getImageType() { return imageType; }
    public void setImageType(long value) { this.imageType = value; }

    public long getImageOption() { return imageOption; }
    public void setImageOption(long value) { this.imageOption = value; }
}
