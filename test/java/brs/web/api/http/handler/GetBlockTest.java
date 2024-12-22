package brs.web.api.http.handler;

import brs.Block;
import brs.Blockchain;
import brs.Signum;
import brs.Constants;
import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.props.PropertyService;
import brs.props.Props;
import brs.services.BlockService;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.math.BigInteger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Signum.class)
public class GetBlockTest {

  private GetBlock t;

  private Blockchain blockchainMock;
  private BlockService blockServiceMock;

  @Before
  public void setUp() {
    blockchainMock = mock(Blockchain.class);
    blockServiceMock = mock(BlockService.class);

    mockStatic(Signum.class);
    PropertyService propertyService = mock(PropertyService.class);
    when(Signum.getPropertyService()).thenReturn(propertyService);
    doReturn((int)Constants.ONE_SIGNA).when(propertyService).getInt(eq(Props.ONE_COIN_NQT));

    t = new GetBlock(blockchainMock, blockServiceMock);
  }

  @Test
  public void processRequest_withBlockId() {
    long blockId = 2L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(BLOCK_PARAMETER, blockId)
    );

    final Block mockBlock = mock(Block.class);
    when(mockBlock.getCumulativeDifficulty()).thenReturn(BigInteger.valueOf(123456789L));

    when(blockchainMock.getBlock(eq(blockId))).thenReturn(mockBlock);

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertNotNull(result);
  }

  @Test
  public void processRequest_withBlockId_incorrectBlock() {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
      new MockParam(BLOCK_PARAMETER, "notALong")
    );

    assertEquals(INCORRECT_BLOCK, t.processRequest(req));
  }

  @Test
  public void processRequest_withHeight() {
    int blockHeight = 2;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(HEIGHT_PARAMETER, blockHeight)
    );

    final Block mockBlock = mock(Block.class);
    when(mockBlock.getCumulativeDifficulty()).thenReturn(BigInteger.valueOf(123456789L));

    when(blockchainMock.getHeight()).thenReturn(100);
    when(blockchainMock.getBlockAtHeight(eq(blockHeight))).thenReturn(mockBlock);

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertNotNull(result);
  }

  @Test
  public void processRequest_withHeight_incorrectHeight_unParsable() {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(HEIGHT_PARAMETER, "unParsable")
    );

    assertEquals(INCORRECT_HEIGHT, t.processRequest(req));
  }

  @Test
  public void processRequest_withHeight_incorrectHeight_isNegative() {
    final long heightValue = -1L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(HEIGHT_PARAMETER, heightValue)
    );

    assertEquals(INCORRECT_HEIGHT, t.processRequest(req));
  }

  @Test
  public void processRequest_withHeight_incorrectHeight_overCurrentBlockHeight() {
    final long heightValue = 10L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(HEIGHT_PARAMETER, heightValue)
    );

    when(blockchainMock.getHeight()).thenReturn(5);

    assertEquals(INCORRECT_HEIGHT, t.processRequest(req));
  }

  @Test
  public void processRequest_withTimestamp() {
    int timestamp = 2;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(TIMESTAMP_PARAMETER, timestamp)
    );

    final Block mockBlock = mock(Block.class);
    when(mockBlock.getCumulativeDifficulty()).thenReturn(BigInteger.valueOf(123456789L));

    when(blockchainMock.getLastBlock(eq(timestamp))).thenReturn(mockBlock);

    final JsonObject result = (JsonObject) t.processRequest(req);

    assertNotNull(result);
  }

  @Test
  public void processRequest_withTimestamp_incorrectTimeStamp_unParsable() {
    final HttpServletRequest req = QuickMocker.httpServletRequest(
      new MockParam(TIMESTAMP_PARAMETER, "unParsable")
    );

    assertEquals(INCORRECT_TIMESTAMP, t.processRequest(req));
  }

  @Test
  public void processRequest_withTimestamp_incorrectTimeStamp_negative() {
    final int timestamp = -1;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(TIMESTAMP_PARAMETER, timestamp)
    );

    assertEquals(INCORRECT_TIMESTAMP, t.processRequest(req));
  }


  @Test
  public void processRequest_unknownBlock() {
    final HttpServletRequest req = QuickMocker.httpServletRequest();

    assertEquals(UNKNOWN_BLOCK, t.processRequest(req));
  }

}
