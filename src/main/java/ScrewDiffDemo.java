import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.metadata.model.ColumnModel;
import cn.smallbun.screw.core.metadata.model.DataModel;
import cn.smallbun.screw.core.metadata.model.TableModel;
import cn.smallbun.screw.core.process.DataModelProcess;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author : gaozhiwen
 * @date : 2020/10/23
 */
public class ScrewDiffDemo {

    private static final String filePath = "/Users/gaozhiwen/Downloads/";
    private static final String createFilePath = "0120";
    private static final String readFilePath = "1218";

    public static void main(String[] args) throws IOException {

        // 获取配置
        Configuration configuration = getConfiguration();

        // 查询表信息
        DataModel dataModel = (new DataModelProcess(configuration)).process();

        // 存表信息
        for (TableModel tableModel : dataModel.getTables()) {
            create(tableModel);
        }

        // 对比
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~~")      //introduce markdown style for strikethrough
                .newTag(f -> "**")     //introduce markdown style for bold
                .build();

        //compute the differences for two test texts.
        for (TableModel tableModel : dataModel.getTables()) {
            TableModel read = read(tableModel.getTableName());
            if (read == null) {
                continue;
            }
            List<DiffRow> diffRows = generator.generateDiffRows(convert(tableModel), convert(read));
            boolean allEqual = diffRows.stream().allMatch(diffRow -> "EQUAL".equals(diffRow.getTag().toString()));
            if (allEqual) {
                // 无变化的表不需要输出
                continue;
            }
            for (DiffRow row : diffRows) {
                System.out.println(row.getOldLine() + " === " + row.getNewLine() + " === " + row.getTag() + "</br>");
            }
            System.out.println();
        }
    }


    public static void create(TableModel tableModel) {
        Path fpath = Paths.get(filePath + createFilePath);
        //创建文件夹
        if (!Files.exists(fpath)) {
            try {
                Files.createDirectory(fpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fpath = Paths.get(filePath + createFilePath + "/" + tableModel.getTableName() + ".txt");
        //创建文件
        if (!Files.exists(fpath)) {
            try {
                Files.createFile(fpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //创建BufferedWriter
        try {
            BufferedWriter bfw = Files.newBufferedWriter(fpath);
            bfw.write(JsonUtils.objectToJSONString(tableModel));
            bfw.flush();
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TableModel read(String tableName) {
        Path fpath = Paths.get(filePath + readFilePath + tableName + ".txt");
        if (!Files.exists(fpath)) {
            return null;
        }
        //创建BufferedReader
        TableModel tableModel = null;
        try {
            BufferedReader bfr=Files.newBufferedReader(fpath);
            tableModel = JsonUtils.stringToObject(bfr.readLine(), TableModel.class);
//            System.out.println(bfr.readLine());
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableModel;
    }

    public static List<String> convert(TableModel tableModel) {
        List<String> strings = new ArrayList<>();
        strings.add(tableModel.getTableName());
        strings.add(tableModel.getRemarks());
        for (ColumnModel columnModel : tableModel.getColumns()) {
            strings.add(columnModel.getColumnName() + "/" + columnModel.getTypeName());
        }
        return strings;
    }

    public static Configuration getConfiguration() throws IOException {

        Properties properties = getProperties();

        DataSource dataSource = getDataSource(properties);


        ProcessConfig processConfig = ProcessConfig.builder()
                //根据名称指定表生成
                .designatedTableName(getTableName(properties)).build();

        EngineConfig engineConfig = EngineConfig.builder().fileType(EngineFileType.MD).build();

        return Configuration.builder().dataSource(dataSource).produceConfig(processConfig).engineConfig(engineConfig).build();
    }

    public static DataSource getDataSource(Properties properties) throws IOException {
        //数据源
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(properties.getProperty("datasource.driver.class.name"));
        hikariConfig.setJdbcUrl(properties.getProperty("datasource.jdbc.url"));
        hikariConfig.setUsername(properties.getProperty("datasource.username"));
        hikariConfig.setPassword(properties.getProperty("datasource.password"));
        //设置可以获取tables remarks信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        DataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }

    public static Properties getProperties() throws IOException {
        Properties properties = new Properties();
//        File file = new File("classpath:application-local.properties");
        properties.load(new FileInputStream(ScrewDiffDemo.class.getResource("").getPath() + File.separator + "application-local.properties"));
        return properties;
    }

    public static List<String> getTableName(Properties properties) {
        if (properties == null || properties.getProperty("table.name.list") == null) {
            return new ArrayList<>();
        }
        String[] split = properties.getProperty("table.name.list").split(";");
        return Arrays.asList(split);
    }
}
