package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import org.junit.Assert;
import org.junit.Test;

public class HeredocLexingStateTest {
    @Test
    public void testInitialState() throws Exception {
        HeredocLexingState s = new HeredocLexingState();
        Assert.assertTrue(s.isEmpty());

        Assert.assertFalse(s.isNextMarker("x"));
        Assert.assertFalse(s.isNextMarker("a"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInitialStateAssertion1() throws Exception {
        HeredocLexingState s = new HeredocLexingState();
        Assert.assertFalse(s.isExpectingEvaluatingHeredoc());
    }

    @Test(expected = IllegalStateException.class)
    public void testInitialStateAssertion2() throws Exception {
        HeredocLexingState s = new HeredocLexingState();
        Assert.assertFalse(s.isIgnoringTabs());
    }

    @Test
    public void testStateChange() throws Exception {
        HeredocLexingState s = new HeredocLexingState();

        s.pushMarker("a", false);
        Assert.assertTrue(s.isNextMarker("a"));
        Assert.assertFalse(s.isNextMarker("x"));

        s.popMarker("a");

        Assert.assertFalse(s.isNextMarker("a"));
        Assert.assertFalse(s.isNextMarker("x"));
    }

    @Test
    public void testEvaluatingHeredoc() throws Exception {
        HeredocLexingState s = new HeredocLexingState();

        s.pushMarker("\"a\"", false);
        Assert.assertFalse(s.isExpectingEvaluatingHeredoc());
        s.popMarker("a");

        s.pushMarker("a", false);
        Assert.assertTrue(s.isExpectingEvaluatingHeredoc());
        s.popMarker("a");
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testInvalidStateChange() throws Exception {
        HeredocLexingState s = new HeredocLexingState();

        s.pushMarker("a", false);
        s.pushMarker("b", false);

        s.popMarker("x");
    }

    @Test(expected = java.lang.IllegalStateException.class)
    public void testInvalidStateWrongOrder() throws Exception {
        HeredocLexingState s = new HeredocLexingState();

        s.pushMarker("a", false);
        s.pushMarker("b", false);

        s.popMarker("b");
        s.popMarker("a");
    }

    @Test
    public void testIsIgnoringTabs() throws Exception {
        HeredocLexingState s = new HeredocLexingState();

        s.pushMarker("a", false);
        Assert.assertFalse(s.isIgnoringTabs());
        s.popMarker("a");

        s.pushMarker("b", true);
        Assert.assertTrue(s.isIgnoringTabs());
        s.popMarker("b");
    }

    @Test
    public void testEquals() throws Exception {
        Class innerClass = HeredocLexingState.class.getDeclaredClasses()[0];
        Object hmi1 = innerClass.getDeclaredConstructors()[0].newInstance("a", Boolean.FALSE);
        Object hmi2 = innerClass.getDeclaredConstructors()[0].newInstance("a", Boolean.FALSE);
        Object hmi3 = innerClass.getDeclaredConstructors()[0].newInstance("a", Boolean.TRUE);
        Object hmi4 = innerClass.getDeclaredConstructors()[0].newInstance("\"a\"", Boolean.FALSE);
        Object hmi5 = innerClass.getDeclaredConstructors()[0].newInstance("\"c\"", Boolean.FALSE);

        Assert.assertTrue(hmi1.equals(hmi1));
        Assert.assertFalse(hmi1.equals(null));
        Assert.assertFalse(hmi1.equals(""));
        Assert.assertTrue(hmi1.equals(hmi2));
        Assert.assertFalse(hmi1.equals(hmi3));
        Assert.assertFalse(hmi1.equals(hmi4));
        Assert.assertFalse(hmi4.equals(hmi5));
    }

    @Test
    public void testHashCode() throws Exception {
        Class innerClass = HeredocLexingState.class.getDeclaredClasses()[0];
        Object hmi1 = innerClass.getDeclaredConstructors()[0].newInstance("a", Boolean.FALSE);
        Assert.assertEquals(getHashCode(false, true, "a"), hmi1.hashCode());

        Object hmi2 = innerClass.getDeclaredConstructors()[0].newInstance("a", Boolean.TRUE);
        Assert.assertEquals(getHashCode(true, true, "a"), hmi2.hashCode());

        Object hmi3 = innerClass.getDeclaredConstructors()[0].newInstance("\"a\"", Boolean.FALSE);
        Assert.assertEquals(getHashCode(false, false, "a"), hmi3.hashCode());

        Object hmi4 = innerClass.getDeclaredConstructors()[0].newInstance("\"a\"", Boolean.TRUE);
        Assert.assertEquals(getHashCode(true, false, "a"), hmi4.hashCode());

        Object hmi5 = innerClass.getDeclaredConstructors()[0].newInstance("", Boolean.TRUE);
        Assert.assertEquals(getHashCode(true, true, null), hmi5.hashCode());
    }


    private int getHashCode(boolean ignoreLeadingTabs, boolean evaluating, String markerName) {
        int result = (ignoreLeadingTabs ? 1 : 0);
        result = 31 * result + (evaluating ? 1 : 0);
        result = 31 * result + (markerName != null ? markerName.hashCode() : 0);
        return result;
    }
}
