package com.google.doubleclick.openrtb.json;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import com.google.doubleclick.AdxExt;
import com.google.doubleclick.AdxExt.BidExt;
import com.google.doubleclick.AdxExt.BidExt.EventNotificationToken;
import com.google.doubleclick.AdxExt.BidExt.ExchangeDealType;
import com.google.doubleclick.AdxExt.BidRequestExt;
import com.google.doubleclick.AdxExt.BidRequestExt.BidFeedback;
import com.google.doubleclick.AdxExt.BidResponseExt;
import com.google.doubleclick.AdxExt.ImpExt;
import com.google.doubleclick.AdxExt.ImpExt.AmpAdRequirementType;
import com.google.doubleclick.AdxExt.NativeRequestExt;
import com.google.doubleclick.AdxExt.NativeRequestExt.LayoutType;
import com.google.doubleclick.AdxExt.RegsExt;
import com.google.doubleclick.AdxExt.SiteExt;
import com.google.doubleclick.AdxExt.UserExt;
import com.google.doubleclick.AdxExt.UserExt.ConsentedProvidersSettings;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Native;
import com.google.openrtb.OpenRtb.BidRequest.Regs;
import com.google.openrtb.OpenRtb.BidRequest.Site;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.OpenRtb.NativeRequest;
import com.google.openrtb.json.OpenRtbJsonFactory;
import java.io.IOException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the AdX/OpenRTB JSON support.
 */
public class AdxExtJsonTest {
  private static final Logger logger = LoggerFactory.getLogger(AdxExtJsonTest.class);

  @Test
  public void testRequest() throws IOException {
    testRequest(BidRequest.newBuilder()
        .setId("0")
        .addImp(Imp.newBuilder()
            .setId("1")
            .setNative(Native.newBuilder()
                .setRequestNative(NativeRequest.newBuilder()
                      .setExtension(AdxExt.nativeExt, NativeRequestExt.newBuilder()
                          .setStyleId(12)
                          .setStyleWidth(200)
                          .setStyleHeight(100)
                          .setStyleLayoutType(LayoutType.PIXEL)
                          .build())))
            .setExtension(AdxExt.imp, ImpExt.newBuilder()
                .addAllBillingId(asList(100L, 101L, 999999999999999999L))
                .addAllPublisherSettingsListId(asList(200L, 201L, 8888888888888888888L))
                .addAllAllowedVendorType(asList(300, 301, 302))
                .addAllPublisherParameter(asList("a", "b", "c"))
                .setDfpAdUnitCode("abc")
                .setIsRewardedInventory(true)
                .setAmpad(AmpAdRequirementType.AMP_AD_REQUIRED)
                .build()))
        .setSite(Site.newBuilder()
            .setDomain("mysite.com")
            .setExtension(AdxExt.site, SiteExt.newBuilder()
                .setAmp(SiteExt.AmpPage.DIALECT_HTML_AMP)
                .build()))
        .setUser(User.newBuilder()
            .setExtension(AdxExt.user, UserExt.newBuilder()
                .setConsentedProvidersSettings(ConsentedProvidersSettings.newBuilder()
                    .addAllConsentedProviders(asList(100L, 101L)))
                .build()))
        .setRegs(Regs.newBuilder()
            .setExtension(AdxExt.regs, RegsExt.newBuilder().setGdpr(true).build()))
        .setExtension(AdxExt.bidRequest, BidRequestExt.newBuilder()
            .addBidFeedback(BidFeedback.newBuilder()
                .setBuyerCreativeId("cr1")
                .setCreativeStatusCode(99)
                .setPrice(1.2)
                .setRequestId("req1")
                .setEventNotificationToken(
                    BidFeedback.EventNotificationToken.newBuilder().setPayload("data")))
            .build())
        .build());
  }

  @Test
  public void testResponse() throws IOException {
    testResponse(BidResponse.newBuilder()
        .setId("0")
        .addSeatbid(SeatBid.newBuilder()
            .addBid(Bid.newBuilder()
                .setId("bid1")
                .setImpid("1")
                .setPrice(1.0)
                .setExtension(AdxExt.bid, BidExt.newBuilder()
                    .addAllImpressionTrackingUrl(asList("http://site.com/1", "http://site.com/2"))
                    .setAdChoicesDestinationUrl("http://adchoices.com")
                    .setBidderName("x")
                    .setExchangeDealType(ExchangeDealType.OPEN_AUCTION)
                    .setEventNotificationToken(EventNotificationToken.newBuilder()
                        .setPayload("token")
                        .build())
                    .addAllAttribute(asList(11, 22, 33))
                    .setAmpAdUrl("http://amp")
                    .build())))
        .setExtension(AdxExt.bidResponse,
            BidResponseExt.newBuilder().setProcessingTimeMs(99).build())
        .build());
  }

  static void testRequest(BidRequest req) throws IOException {
    OpenRtbJsonFactory jsonFactory = AdxExtUtils.registerAdxExt(OpenRtbJsonFactory.create());
    String jsonReq = jsonFactory.newWriter().writeBidRequest(req);
    logger.info(jsonReq);
    jsonFactory.setStrict(false).newWriter().writeBidRequest(req);
    BidRequest req2 = jsonFactory.newReader().readBidRequest(jsonReq);
    assertThat(req2).isEqualTo(req);
    jsonFactory.setStrict(false).newReader().readBidRequest(jsonReq);
  }

  static String testResponse(BidResponse resp) throws IOException {
    OpenRtbJsonFactory jsonFactory = AdxExtUtils.registerAdxExt(OpenRtbJsonFactory.create());
    String jsonResp = jsonFactory.newWriter().writeBidResponse(resp);
    logger.info(jsonResp);
    jsonFactory.setStrict(false).newWriter().writeBidResponse(resp);
    OpenRtb.BidResponse resp2 = jsonFactory.newReader().readBidResponse(jsonResp);
    assertThat(resp2).isEqualTo(resp);
    jsonFactory.setStrict(false).newReader().readBidResponse(jsonResp);
    return jsonResp;
  }
}
