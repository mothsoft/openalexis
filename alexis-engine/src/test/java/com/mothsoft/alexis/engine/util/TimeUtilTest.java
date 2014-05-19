package com.mothsoft.alexis.engine.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class TimeUtilTest {

    @Test
    public void testFloor() {
        final Date test37 = new Date(2014, 0, 1, 23, 37);
        final Date test30 = new Date(2014, 0, 1, 23, 30);
        assertEquals(test30, TimeUtil.floor(test37));

        final Date test3 = new Date(2014, 0, 1, 23, 3);
        final Date test0 = new Date(2014, 0, 1, 23, 0);
        assertEquals(test0, TimeUtil.floor(test3));
    }

}
