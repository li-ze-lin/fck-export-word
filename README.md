# fck富文本导出成word

## pom引入poi相关的包
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>3.15</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-examples</artifactId>
    <version>3.14</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-excelant</artifactId>
    <version>3.14</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>3.15</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml-schemas</artifactId>
    <version>3.15</version>
</dependency>
<dependency>
    <groupId>poi</groupId>
    <artifactId>poi-scratchpad</artifactId>
    <version>2.5.1-final-20040804</version>
</dependency>
<dependency>
    <groupId>fr.opensagres.xdocreport</groupId>
    <artifactId>org.apache.poi.xwpf.converter.xhtml</artifactId>
    <version>1.0.5</version>
</dependency>
```

## 导出方法
```java
@Controller
@RequestMapping("/fck")
public class FckController {
    @RequestMapping("/export")
    public void exportWord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Objects.requireNonNull(fckDTO);
        //word内容
        String content = "<html><meta charset=\"utf-8\" /><body>";
        content += fckDTO.getText();
        content += "</body></html>";
        //获取绝对路径 这里要与文件存储的路径一样
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "static/fck/";
        path = path.substring(1);
        //替换里面的所有图片路径
        content = content.replaceAll("<img alt=\"\" src=\"", "<img alt=\"\" src=\"" + path);
    
        byte b[] = content.getBytes("utf-8");  //这里是必须要设置编码的，不然导出中文就会乱码。
        ByteArrayInputStream bais = new ByteArrayInputStream(b);//将字节数组包装到流中
    
        //* 关键地方
        //* 生成word格式
        POIFSFileSystem poifs = new POIFSFileSystem();
        DirectoryEntry directory = poifs.getRoot();
        DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
    
        //设置文件名
        String fileName = UUID.randomUUID().toString();
        //处理文件名乱码
        String userAgent = request.getHeader("User-Agent");
        // 针对IE或者以IE为内核的浏览器：
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } else {
            // 非IE浏览器的处理：
            fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
        }
    
        //输出文件
        request.setCharacterEncoding("utf-8");
        response.setContentType("application/msword");//导出word格式
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName + ".doc");
        OutputStream ostream = response.getOutputStream();
        poifs.writeFilesystem(ostream);
        bais.close();
        ostream.close();
    }
}
```
