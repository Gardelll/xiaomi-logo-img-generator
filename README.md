# Xiaomi Devices "logo.img" Generator

[中文版本](README.zh.md)

## logo.img Format Description (For Redmi K20/Xiaomi MI 9T, other devices maybe difference)

The beginning of the file is all 0x00, starting from the 0x4000 byte, it is the following content: (For the convenience of explanation, four bytes each wrap)
```text
4c 4f 47 4f // ASCII String LOGO
21 21 21 21 // ASCII String !!!! (The first 8 bytes are used as the "magic number" to identify the file)
05 00 00 00 // The address offset of the 1st picture (see below for conversion)
3b 07 00 00 // The size of the 1st image in bytes
40 07 00 00 // The address offset of the 2nd picture
ef 05 00 00 // The size of the 2nd image in bytes
2f 0d 00 00 // The address offset of the 3ed picture
3b 07 00 00 // The size of the 3ed image in bytes
6a 14 00 00 // The address offset of the 4th picture
ef 05 00 00 // The size of the 4th image in bytes
```

The number storage method in this file is special:

When storing, the number is shifted to the right by 24 bits, the overflowed part is rounded up, and then converted to little endian and stored as 32 bits.

For Redmi K20 (davinci), the meanings of the 4 pictures are:

1. Normal startup screen
2. fastboot interface
3. Start screen after unlocking
4. Interface when the system is damaged

## Generate Tool Usage

This tool is written in Java.

### Compile

```shell
./gradlew shadowJar
```

Output in `build/libs/gen-logo.jar`

### Run

```shell
# Show Usage Help
java -jar build/libs/gen-logo.jar -h

# Output the Binary
java -jar build/libs/gen-logo.jar -b boot.bmp -d damaged.bmp -f fastboot.bmp -u unlocked.bmp -o logo.img
```

### No Java Environment

Please visit [Release](https://github.com/Gardelll/xiaomi-logo-img-generator/releases) for natively compiled Windows version executable files.

## Others

Welcome to submit generation methods for other devices, and welcome to share this to other websites.
