package com.regis.emirrefit.batch.job.common;

import com.regis.emirrefit.batch.model.ImpalaRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvItemWriterTest {

    @TempDir Path tmp;
    private Path out;

    @BeforeEach void setUp() { out = tmp.resolve("test.csv"); }

    @Test void headerAndData() throws Exception {
        var w = new CsvItemWriter(out.toString(), ";");
        var r = new ImpalaRow(); r.put("A","hello"); r.put("B",42L);
        w.write(new Chunk<>(List.of(r)));
        var lines = Files.readAllLines(out);
        assertEquals(2, lines.size());
        assertEquals("A;B", lines.get(0));
        assertEquals("hello;42", lines.get(1));
    }
    @Test void headerOnce() throws Exception {
        var w = new CsvItemWriter(out.toString(), ";");
        var r1 = new ImpalaRow(); r1.put("X","a");
        var r2 = new ImpalaRow(); r2.put("X","b");
        w.write(new Chunk<>(List.of(r1)));
        w.write(new Chunk<>(List.of(r2)));
        assertEquals(3, Files.readAllLines(out).size());
    }
    @Test void emptyChunk() throws Exception {
        new CsvItemWriter(out.toString(), ";").write(new Chunk<>(List.of()));
        assertFalse(Files.exists(out));
    }
    @Test void nullValue() throws Exception {
        var w = new CsvItemWriter(out.toString(), ";");
        var r = new ImpalaRow(); r.put("A","v"); r.put("B",null); r.put("C","e");
        w.write(new Chunk<>(List.of(r)));
        assertEquals("v;;e", Files.readAllLines(out).get(1));
    }
    @Test void pipeDelimiter() throws Exception {
        var w = new CsvItemWriter(out.toString(), "|");
        var r = new ImpalaRow(); r.put("X","1"); r.put("Y","2");
        w.write(new Chunk<>(List.of(r)));
        assertEquals("X|Y", Files.readAllLines(out).get(0));
    }
    @Test void createsParentDirs() throws Exception {
        Path nested = tmp.resolve("a/b/c/out.csv");
        var w = new CsvItemWriter(nested.toString(), ";");
        var r = new ImpalaRow(); r.put("K","v");
        w.write(new Chunk<>(List.of(r)));
        assertTrue(Files.exists(nested));
    }
    @Test void multipleRows() throws Exception {
        var w = new CsvItemWriter(out.toString(), ";");
        var r1 = new ImpalaRow(); r1.put("N","1");
        var r2 = new ImpalaRow(); r2.put("N","2");
        var r3 = new ImpalaRow(); r3.put("N","3");
        w.write(new Chunk<>(List.of(r1,r2,r3)));
        assertEquals(4, Files.readAllLines(out).size());
    }
}
