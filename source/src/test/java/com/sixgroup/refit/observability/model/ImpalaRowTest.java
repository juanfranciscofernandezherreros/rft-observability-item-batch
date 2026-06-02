package com.sixgroup.refit.observability.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImpalaRowTest {
    @Test void putAndGet()       { var r=new ImpalaRow(); r.put("A","v"); assertEquals("v",r.get("A")); }
    @Test void getMissing()      { assertNull(new ImpalaRow().get("X")); }
    @Test void putNull()         { var r=new ImpalaRow(); r.put("N",null); assertTrue(r.getColumns().containsKey("N")); assertNull(r.get("N")); }
    @Test void overwrite()       { var r=new ImpalaRow(); r.put("K","old"); r.put("K","new"); assertEquals("new",r.get("K")); assertEquals(1,r.getColumns().size()); }
    @Test void preservesOrder()  { var r=new ImpalaRow(); r.put("B",2); r.put("A",1); var k=r.getColumns().keySet().stream().toList(); assertEquals("B",k.get(0)); assertEquals("A",k.get(1)); }
    @Test void toStringContent() { var r=new ImpalaRow(); r.put("X",1); r.put("Y","z"); String s=r.toString(); assertTrue(s.contains("X")); assertTrue(s.contains("z")); }
}
