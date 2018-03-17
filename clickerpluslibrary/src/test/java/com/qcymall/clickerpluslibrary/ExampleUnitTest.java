package com.qcymall.clickerpluslibrary;

import com.qcymall.clickerpluslibrary.utils.BLECMDUtil;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void packageCMD_isCorrect() throws Exception{
        byte[] result;
        byte[] data = {(byte) 0xfe, (byte) 0xcf};
        result = BLECMDUtil.INSTANCE.packageCMD(0x5002, data);
        byte[] assertBytes = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0e, 0x50, 0x02, result[8], result[9], 0x00, 0x00, (byte) 0xfe, (byte) 0xcf};
        assertArrayEquals(result, assertBytes);

        result = BLECMDUtil.INSTANCE.packageCMD(0x5003, null);
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0c, 0x50, 0x03, result[8], result[9], 0x00, 0x00};
        assertArrayEquals(result, assertBytes2);
    }
    @Test
    public void parseCMD_isCorrect() throws Exception {
        BLECMDUtil.ParseResult parseResult;
        byte[] bytes = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0c, 0x50, 0x01, 0x00, 0x00, 0x00, 0x00};
        parseResult = BLECMDUtil.INSTANCE.parseCMD(bytes);
        assert parseResult != null;
        assertEquals(0, parseResult.getError());
        assertEquals(0, parseResult.getIndex());
        assertEquals(1, parseResult.getVersion());
        assertEquals(0x5001, parseResult.getId());
        assertEquals(0, parseResult.getData().length);

        byte[] bytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0d, 0x50, 0x03, 0x00, 0x04, 0x00, 0x00, 0x01};
        parseResult = BLECMDUtil.INSTANCE.parseCMD(bytes2);
        assert parseResult != null;
        assertEquals(0, parseResult.getError());
        assertEquals(4, parseResult.getIndex());
        assertEquals(1, parseResult.getVersion());
        assertEquals(0x5003, parseResult.getId());
        assertEquals(1, parseResult.getData().length);

        byte[] bytes3 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0d, 0x50, 0x03, 0x00, 0x04, 0x00, 0x00, 0x01, 0x00};
        parseResult = BLECMDUtil.INSTANCE.parseCMD(bytes3);
        assert parseResult != null;
    }

    @Test
    public void createpair_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createPariCMD("123456");
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x14, 0x50, 0x01, result[8], result[9], 0x00, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x00, 0x01};
        assertArrayEquals(result, assertBytes2);

    }

    @Test
    public void createUnpair_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createUnpariCMD("123456", false);
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x14, 0x50, 0x02, result[8], result[9], 0x00, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x00, 0x01};
        assertArrayEquals(result, assertBytes2);

        byte[] result2 = BLECMDUtil.INSTANCE.createUnpariCMD("12345678", false);
        byte[] assertBytes3 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x14, 0x50, 0x02, result2[8], result2[9], 0x00, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x01};
        assertArrayEquals(result2, assertBytes3);
    }

    @Test
    public void createConnectBack_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createConnectBackCMD("123456");
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x14, 0x50, 0x03, result[8], result[9], 0x00, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x00, 0x01};
        assertArrayEquals(result, assertBytes2);

    }

    @Test
    public void createFind_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createFindCMD();
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0c, 0x50, 0x10, result[8], result[9], 0x00, 0x00};
        assertArrayEquals(result, assertBytes2);

    }
    @Test
    public void createBattery_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createBatteryCMD();
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0c, 0x50, 0x11, result[8], result[9], 0x00, 0x00};
        assertArrayEquals(result, assertBytes2);

    }

    @Test
    public void createOTA_isCorrect() throws Exception{
        byte[] result = BLECMDUtil.INSTANCE.createOTACMD();
        byte[] assertBytes2 = {(byte) 0xfe, (byte) 0xcf, 0x00, 0x01, 0x00, 0x0c, 0x50, 0x12, result[8], result[9], 0x00, 0x00};
        assertArrayEquals(result, assertBytes2);

    }

    @Test
    public void test_isCorrect() throws Exception{
        int result = BLECMDUtil.INSTANCE.getInt(new byte[]{0x00, (byte) 0xf4});
        assertEquals(result, 244);

    }
}