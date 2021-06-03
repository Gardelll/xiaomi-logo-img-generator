# 小米设备 "logo.img" 生成器

## logo.img 格式说明（以 Redmi K20 为例，其他设备可能略有差异）

文件的开头全为 0x00，从第 0x4000 字节开始，为下面的内容：（为了方便说明，四个字节一换行）
```text
4c 4f 47 4f // ASCII 字符 LOGO
21 21 21 21 // ASCII 字符 !!!! （前 8 字节作为“魔数”识别文件）
05 00 00 00 // 第 1 张图片的地址偏移量 （格式转换见下方）
3b 07 00 00 // 第 1 张图片的大小
40 07 00 00 // 第 2 张图片的地址偏移量
ef 05 00 00 // 第 2 张图片的大小
2f 0d 00 00 // 第 3 张图片的地址偏移量
3b 07 00 00 // 第 3 张图片的大小
6a 14 00 00 // 第 4 张图片的地址偏移量
ef 05 00 00 // 第 4 张图片的大小
```

此文件中的数字储存方式比较特殊：

储存时把数字右移 24 位，溢出的部分向上取整，然后转为小端存储为 32 位。

对于 Redmi K20 (davinci) 来说，4 张图片的含义依次是：

1. 正常启动画面
2. fastboot 界面
3. 解锁后的启动画面
4. 系统损坏时的界面

## 生成工具用法

此工具使用 Java 编写。

### 构建

```shell
./gradlew shadowJar
```

生成于 `build/libs/gen-logo.jar`

### 运行

```shell
# 查看帮助
java -jar build/libs/gen-logo.jar -h

# 生成镜像
java -jar build/libs/gen-logo.jar -b boot.bmp -d damaged.bmp -f fastboot.bmp -u unlocked.bmp -o logo.img
```

### 没有 Java 环境

[Release](https://github.com/Gardelll/xiaomi-logo-img-generator/releases) 页面提供本地编译的 Windows 版本可执行文件。

## 其他

欢迎提交其他设备的生成方法，欢迎分享到其他网站。
