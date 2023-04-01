package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

public class Futil {
    public static void processDir(String dirName, String resultFileName) {
        try {
            final FileChannel fileOutChannel = FileChannel.open(Paths.get(resultFileName), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            final Charset charsetIn = Charset.forName("Cp1250");
            final Charset charsetOut = StandardCharsets.UTF_8;
            Files.walkFileTree(Paths.get(dirName), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileChannel fChannel = FileChannel.open(file);
                    int size = (int) fChannel.size();
                    ByteBuffer buf = ByteBuffer.allocate(size);
                    fChannel.read(buf);
                    buf.flip(); //must be flipped after saving to read
                    CharBuffer cbuf = charsetIn.decode(buf);
                    buf = charsetOut.encode(cbuf);
                    fileOutChannel.write(buf);
                    fChannel.close();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
            fileOutChannel.close();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
