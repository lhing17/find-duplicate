package cn.gsein.duplicate;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DuplicateFinder {

    public static void findDuplicate(String root) throws IOException {
        String collect = Files.walk(Paths.get(root))
                .filter(Files::isRegularFile) // 去掉文件夹
                .filter(p -> p.toFile().length() > 0) // 去掉空文件
                .collect(Collectors.groupingBy(p -> p.toFile().length())) // 先按长度初步分组
                .values() // 只保留分组后的值，不关心长度
                .stream()
                .filter(l -> (l.size() >= 2)) // 只保留分组中元素超过2的，即可能有重复
                .flatMap(DuplicateFinder::groupByMd5) // 根据MD5二次筛选
                .filter(l -> (l.size() >= 2)) // 只保留分组中元素超过2的，即确定有重复
                .map(l -> String.join("\n", l.stream().map(Path::toString).toArray(String[]::new)))
                .collect(Collectors.joining("\n-----------------\n")); // 格式化输出

        Files.write(Paths.get("dup.txt"), collect.getBytes(StandardCharsets.UTF_8));

    }

    public static Stream<List<Path>> groupByMd5(List<Path> l) {
        return l.stream().collect(Collectors.groupingBy(
                p -> {
                    try {
                        return DigestUtils.md5Hex(Files.newInputStream(p));
                    } catch (IOException e) {
                        return "";
                    }
                }
        )).values().stream();
    }

    public static void main(String[] args) throws IOException {
        findDuplicate("C:\\Users\\Administrator.USER-20200220HF\\AppData\\Roaming\\JetBrains");
    }
}
