package top.gardel.genlogo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static org.apache.commons.cli.PatternOptionBuilder.STRING_VALUE;

public class App {
    private static void printUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(Integer.MAX_VALUE);
        helpFormatter.setSyntaxPrefix("Usage: ");
        PrintWriter pw = new PrintWriter(System.err);
        helpFormatter.printHelp(pw,
            helpFormatter.getWidth(),
            "gen-logo",
            "Xiaomi Devices \"logo.img\" Generator",
            options,
            helpFormatter.getLeftPadding(),
            helpFormatter.getDescPadding(),
            "\nReport issues at https://github.com/Gardelll/xiaomi-logo-img-generator/issues",
            true);
        pw.flush();
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("b")
            .argName("LOCKED Boot Screen")
            .longOpt("boot")
            .hasArg()
            .type(STRING_VALUE)
            .desc("Splash screen when LOCKED, BMP format, default is `boot.bmp'.")
            .build());
        options.addOption(Option.builder("f")
            .argName("Fastboot Screen")
            .longOpt("fastboot")
            .hasArg()
            .type(STRING_VALUE)
            .desc("Fastboot screen, BMP format, default is `fastboot.bmp'.")
            .build());
        options.addOption(Option.builder("u")
            .argName("UNLOCKED Boot Screen")
            .longOpt("unlocked")
            .hasArg()
            .type(STRING_VALUE)
            .desc("Splash screen after UNLOCKED, BMP format, default is `unlocked.bmp'.")
            .build());
        options.addOption(Option.builder("d")
            .argName("System Damaged Screen")
            .longOpt("damaged")
            .hasArg()
            .type(STRING_VALUE)
            .desc("Screen when the system is damaged, BMP format, default is `damaged.bmp'.")
            .build());
        options.addOption(Option.builder("o")
            .argName("Output File")
            .longOpt("output")
            .hasArg()
            .type(STRING_VALUE)
            .desc("The output binary file, default is `logo_new.img'.")
            .build());
        options.addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show usage help.")
            .build());
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        String boot, fastboot, unlocked, damaged, output;
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption("h")) {
                printUsage(options);
                System.exit(1);
                return;
            }
            boot = commandLine.getOptionValue("b", "boot.bmp");
            fastboot = commandLine.getOptionValue("f", "fastboot.bmp");
            unlocked = commandLine.getOptionValue("u", "unlocked.bmp");
            damaged = commandLine.getOptionValue("d", "damaged.bmp");
            output = commandLine.getOptionValue("o", "logo_new.img");
        } catch (ParseException | NumberFormatException e) {
            System.err.println(e.getLocalizedMessage());
            printUsage(options);
            System.exit(3);
            return;
        }
        long offset0 = 0x4000,
            offset1 = 0x5000,
            offset2,
            offset3,
            offset4;
        byte[] magicBytes = {
            0x4c, 0x4f, 0x47, 0x4f, // LOGO
            0x21, 0x21, 0x21, 0x21, // Magic Number
        };
        try (
            FileOutputStream outputStream = new FileOutputStream(output);
            FileInputStream bootFileStream = new FileInputStream(boot);
            FileInputStream fastbootFileStream = new FileInputStream(fastboot);
            FileInputStream unlockedFileStream = new FileInputStream(unlocked);
            FileInputStream destroyedFileStream = new FileInputStream(damaged)
        ) {
            long written = 0;
            for (long i = written; i < offset0; i++) {
                outputStream.write(0);
            }
            long image1size = normalizeNumber(bootFileStream.available());
            long image2size = normalizeNumber(fastbootFileStream.available());
            long image3size = normalizeNumber(unlockedFileStream.available());
            long image4size = normalizeNumber(destroyedFileStream.available());
            outputStream.write(magicBytes, 0, 8);
            outputStream.write(long2LeBytes(offset1));
            outputStream.write(long2LeBytes(image1size));
            offset2 = normalizeNumber(offset1 + image1size);
            outputStream.write(long2LeBytes(offset2));
            outputStream.write(long2LeBytes(image2size));
            offset3 = normalizeNumber(offset2 + image2size);
            outputStream.write(long2LeBytes(offset3));
            outputStream.write(long2LeBytes(image3size));
            offset4 = normalizeNumber(offset3 + image3size);
            outputStream.write(long2LeBytes(offset4));
            outputStream.write(long2LeBytes(image4size));
            written = offset0 + 40;
            for (long i = written; i < offset1; i++) {
                outputStream.write(0);
            }
            written = offset1 + bootFileStream.transferTo(outputStream);
            for (long i = written; i < offset2; i++) {
                outputStream.write(0);
            }
            written = offset2 + fastbootFileStream.transferTo(outputStream);
            for (long i = written; i < offset3; i++) {
                outputStream.write(0);
            }
            written = offset3 + unlockedFileStream.transferTo(outputStream);
            for (long i = written; i < offset4; i++) {
                outputStream.write(0);
            }
            written = offset4 + destroyedFileStream.transferTo(outputStream);
            outputStream.flush();
            System.out.printf("Saved as %s, %d bytes written\n", output, written);
        } catch (IOException e) {
            System.err.print("Unexpected Error: ");
            System.err.println(e.getLocalizedMessage());
            // IO Failed, so we delete the output file to prevent users to flash it
            try {
                Files.delete(Path.of(output));
            } catch (IOException ignored) {
            }
            printUsage(options);
            System.exit(2);
        }
    }

    // Functions to covert number to suit the "logo.img" file format.
    private static byte[] long2LeBytes(long x) {
        x >>>= 12;
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (x & 0xff);
        bytes[1] = (byte) ((x >>> 8) & 0xff);
        bytes[2] = (byte) ((x >>> 16) & 0xff);
        bytes[3] = (byte) ((x >>> 24) & 0xff);
        return bytes;
    }

    private static long normalizeNumber(long x) {
        if ((x & 0xfff) != 0) {
            x += (0x1000 - (x & 0xfff));
        }
        return x;
    }
}
