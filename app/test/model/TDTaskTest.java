/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package model;

import org.junit.Test;
import pw.ahs.app.anjez.model.TDTask;

import java.time.LocalDate;

import static junit.framework.Assert.*;

public class TDTaskTest {

//    @Test
//    public void testMisc() throws Exception {
//    }

    private TDTask tdTask;
    private String taskString;

    @Test
    public void testNoInfo() throws Exception {
        // start date
        taskString = "2014-01-01 ";
        tdTask = TDTask.parse(taskString);
        assertEquals("2014-01-01", tdTask.getInfo());

        // priority
        taskString = "(A)";
        tdTask = TDTask.parse(taskString);
        assertEquals("(A)", tdTask.getInfo());

        // done date
        taskString = "x 2014-01-01";
        tdTask = TDTask.parse(taskString);
        assertEquals("x 2014-01-01", tdTask.getInfo());

        // due date
        taskString = "due:2014-01-01";
        tdTask = TDTask.parse(taskString);
        assertEquals("due:2014-01-01", tdTask.getInfo());

        // duration
        taskString = "duration:2344443-2423434";
        tdTask = TDTask.parse(taskString);
        assertEquals("duration:2344443-2423434", tdTask.getInfo());

        // combination
        taskString = "x 2014-01-01 (A) 2013-12-31 due:2014-01-01 duration:2344443-2423434";
        tdTask = TDTask.parse(taskString);
        assertEquals(taskString, tdTask.getInfo());
    }

    @Test
    public void testValidSyntax() throws Exception {
        LocalDate date = LocalDate.of(2014, 1, 1);
        // valid, info only
        taskString = "do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // valid, with start date
        taskString = "2014-01-01 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertEquals(date, tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // valid, with priority
        taskString = "(V) do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("V", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // valid, with done date
        taskString = "x 2014-01-01 do this and that";
        tdTask = TDTask.parse(taskString);
        assertTrue(tdTask.isDone());
        assertEquals(date, tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // valid, with due date (at the beginning)
        taskString = "due:2014-01-01 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertEquals(date, tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("due:2014-01-01 do this and that", tdTask.toString());

        // valid, with due date (at the end)
        taskString = "do this and that due:2014-01-01";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertEquals(date, tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("due:2014-01-01 do this and that", tdTask.toString());

        // valid, with due date (at the middle)
        taskString = "do this due:2014-01-01 and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertEquals(date, tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("due:2014-01-01 do this and that", tdTask.toString());

        // valid, with duration (at the beginning)
        taskString = "duration:1391518005-1391518007-1391518010-1391518012 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(4, tdTask.getInstants().size());
        assertEquals("do this and that duration:1391518005-1391518007-1391518010-1391518012", tdTask.toString());

        // valid, with duration (at the end)
        taskString = "do this and that duration:1391518005-1391518007-1391518010-1391518012";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(4, tdTask.getInstants().size());
        assertEquals("do this and that duration:1391518005-1391518007-1391518010-1391518012", tdTask.toString());

        // valid, with duration (at the middle)
        taskString = "do this duration:1391518005-1391518007-1391518010-1391518012 and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(4, tdTask.getInstants().size());
        assertEquals("do this and that duration:1391518005-1391518007-1391518010-1391518012", tdTask.toString());

        // valid, full
        taskString = "x 2014-01-01 (V) 2013-12-31 due:2014-01-01 do this and that duration:1391518005-1391518007-1391518010-1391518012";
        tdTask = TDTask.parse(taskString);
        assertTrue(tdTask.isDone());
        assertEquals(date, tdTask.getDoneDate());
        assertEquals("V", tdTask.getPriority());
        assertEquals(LocalDate.of(2013, 12, 31), tdTask.getStartDate());
        assertEquals(date, tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(4, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());
    }

    @Test
    public void testTrailingSpaces() throws Exception {
        // trailing space
        taskString = "do this and that ";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("do this and that", tdTask.toString());

        // leading space
        taskString = " do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("do this and that", tdTask.toString());

        // leading and trailing spaces
        taskString = " do this and that ";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("do this and that", tdTask.toString());

        // leading and trailing spaces
        taskString = "  do this and that    ";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("do this and that", tdTask.toString());
    }

    @Test
    public void testDone() throws Exception {
        // no space
        taskString = "x2013-10-15";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // uppercase
        taskString = "X 2013-10-15";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // two spaces
        taskString = "x  2013-10-15";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("x 2013-10-15", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("x 2013-10-15", tdTask.toString());

        // no date
        taskString = "x";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());


        // no date, with space
        taskString = "x ";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("x", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("x", tdTask.toString());

        // two spaces
        taskString = "x 2014-01-01  do this";
        tdTask = TDTask.parse(taskString);
        assertTrue(tdTask.isDone());
        assertEquals(LocalDate.of(2014, 1, 1), tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("x 2014-01-01 do this", tdTask.toString());
    }

    @Test
    public void testPriority() throws Exception {
        // no space
        taskString = "(A)do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // lowercase
        taskString = "(a) do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // no parentheses
        taskString = "A do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // missing parentheses
        taskString = "A) do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // missing parentheses
        taskString = "(A do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // two spaces
        taskString = "(A)  do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("A", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("(A) do this and that", tdTask.toString());
    }

    @Test
    public void testStartDate() throws Exception {
        // leading space
        taskString = " 2014-01-01 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("2014-01-01 do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("2014-01-01 do this and that", tdTask.toString());

        // two spaces
        taskString = "2014-01-01  do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertEquals(LocalDate.of(2014, 1, 1), tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this and that", tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals("2014-01-01 do this and that", tdTask.toString());
    }

    @Test
    public void testDateFormat() throws Exception {
        taskString = "2013/10/15 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());
    }

    @Test
    public void testDueDate() throws Exception {
        // key is attached
        taskString = "bladue:2013-10-15 do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // value is attached
        taskString = "due:2013-10-15bla do this and that";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());
    }

    @Test
    public void testDuration() throws Exception {
        // odd
        taskString = "duration:304509345 do this";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals(taskString, tdTask.getInfo());
        assertEquals(0, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());

        // valid
        taskString = "do this duration:304509345-304509359-304509509-304509539";
        tdTask = TDTask.parse(taskString);
        assertFalse(tdTask.isDone());
        assertNull(tdTask.getDoneDate());
        assertEquals("", tdTask.getPriority());
        assertNull(tdTask.getStartDate());
        assertNull(tdTask.getDueDate());
        assertEquals("do this", tdTask.getInfo());
        assertEquals(4, tdTask.getInstants().size());
        assertEquals(taskString, tdTask.toString());
    }
}
